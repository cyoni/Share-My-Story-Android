const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


async function getMessage(path, number_of_letters){

  const p = await admin.database().ref(path).once('value')
  var msg = p.val();

  if (msg.length > number_of_letters){
    msg = msg.substring(0, number_of_letters);
    msg = msg + "..."
  }
  return msg;
}

async function setNotification(type, my_public_key, his_public_key, metadata) {
    const limit = 20; // msg limit

    if (my_public_key === his_public_key)
      return "";

    const nickname = await getNickname(my_public_key);

    if (type === "like_post") {
        const postId = metadata.postId;
        const category = metadata.category;
        const path = category + "/" + postId + "/message";

        const msg = await getMessage(path , limit)
        const text = "<b>"+nickname + "</b> likes your post: " + msg;

        await admin.database().ref('notifications').child(his_public_key).child(type).push().set({
            user_id: my_public_key,
            timestamp: Date.now(),
            postId: postId,
            text: text,
        });
    }

    if (type === "like_comment") {

        const postId = metadata.postId;
        const commentId = metadata.commentId;
        const category = metadata.category;

        const comment = await getMessage(category + "/" + postId + "/comments/" + commentId + "/message", limit)
        const text = "<b>"+nickname + "</b> likes your comment: " + comment;

        await admin.database().ref('notifications').child(his_public_key).child(type).push().set({
            user_id: my_public_key,
            timestamp: Date.now(),
            postId: postId,
            commentId: commentId,
            text: text,
        });
    }

    if (type === "follow") {
        await admin.database().ref('notifications').child(his_public_key).child(type).push().set({
            user_id: my_public_key,
            timestamp: Date.now(),
            nickname: nickname,
        });
    }

    if (type === "commenting") {
        const myComment = metadata.myComment;
        const commentId = metadata.commentId;
        const postId = metadata.postId;

        const text = "<b>" + nickname + "</b> replied to you: " + myComment;
        await admin.database().ref('notifications').child(his_public_key).child(type).push().set({
            user_id: my_public_key,
            commentId: commentId,
            postId: metadata.postId,
            timestamp: Date.now(),
            text: text,
        });
    }

    return "ok";
}


exports.follow = functions.database.ref('user_public/{my_public_key}/following/{user_public_key}').onWrite(async (snapshot, context) => {
    const new_data = snapshot.after.val();
    const user_public_key = context.params.user_public_key;
    const my_public_key = context.params.my_public_key;

    const path = admin.database().ref('user_public').child(user_public_key).child('follows');

    if (new_data !== "t") { // removing data
        return path.child(my_public_key).remove();
    } else {
        await setNotification("follow", my_public_key, user_public_key, "");
        return path.child(my_public_key).set('t');
    }

})

function verifyUser(userPrivateKey) {
    const account = ((resolve, reject) => {
        return admin.database().ref('users').child(userPrivateKey).once('value').then(snapshot => {
            if (snapshot.exists() && snapshot.child('accountStatus').val() !== 'X') {
                return resolve({
                    publicKey: snapshot.child('user_public').val(),
                    status: snapshot.child('accountStatus').val(),
                })
            } else
                return null;
        })
    })
    return new Promise(account);
}

function isNicknameValid(nickname) {
    return !(nickname.length > 20 || nickname.toLowerCase() === "private" || nickname.includes("$") ||
        nickname.includes(".") || nickname.includes("/") ||
        nickname.includes("[") || nickname.includes("]") ||
        nickname.includes("\\"))
}

function isNicknameTaken(inputNickname) {
    const nickname = inputNickname.toLowerCase();
    return admin.database().ref('nicknames').child(nickname).once('value').then(result => {
        return (result.exists())
    })
}

async function setNewNickname(publicKey, newNickname) {
    const a = admin.database().ref('nicknames').child(newNickname.toLowerCase()).set(publicKey);
    const b = admin.database().ref('user_public').child(publicKey).child('profile').child('nickname').set(newNickname);

    await a;
    await b;
}

function throwError(reason) {
    console.log("throwing an error: " + reason);
    throw reason;
}

function removeOldNickname(oldNickname) {
    return admin.database().ref('nicknames').child(oldNickname).remove();
}

function setAccountStatus(privateKey, newStatus) {
    return admin.database().ref('users').child(privateKey).child('accountStatus').set(newStatus);
}

exports.updateNickname = functions.https.onCall(async (data, context) => {
    const privateKey = context.auth.uid;
    var myPublicKey;
    var inputNickname = data.nickname.trim();

    const account = await verifyUser(privateKey);

    if (account === null)
        return "AF";

    const validNickname = isNicknameValid(inputNickname);

    if (!validNickname)
        return "IN"

    const takenNickname = await isNicknameTaken(inputNickname);

    if (takenNickname)
        return "NT"

    const oldNickname = await getNickname(account.publicKey);
    if (oldNickname !== "")
        await removeOldNickname(oldNickname.toLowerCase());

    await setNewNickname(account.publicKey, inputNickname);
    return "S";

});

exports.sendComment = functions.https.onCall(async (snapshot, context) => {
    const user_message = snapshot.message.trim();
    const now = Date.now();
    const category = snapshot.category;
    var txt_user_public_key;
    const post_id = snapshot.postID;

    if (user_message.length === 0) return "FAILURE";

    const account = await verifyUser(context.auth.uid);
    const userPublicKey = account.publicKey;

    var data = {
        message: user_message,
        timestamp: now,
        user_public_key: userPublicKey,
    };

    const comment_id = await admin.database().ref(category).child(post_id).child('comments').push().key;

    const one = await admin.database().ref(category).child(post_id).once('value');

    if (!one.exists()) return "failed";
    else{
       const two = admin.database().ref(category).child(post_id).child('comments').child(comment_id).set(data);
       var shorter_message = user_message;

       if (shorter_message.length > 20){
         shorter_message = shorter_message.substring(0, 20)
         shorter_message = shorter_message + "..."
       }

        var metadata = {
          commentId: comment_id,
          postId: post_id,
          myComment: shorter_message,
          category: category,
        }

        const ref = admin.database().ref(category).child(post_id).child('comments_count');

        await ref.transaction((current) => {
            return (current || 0) + 1;
        })

        await two;
        await setNotification("commenting", userPublicKey, one.child('user_public_key').val(), metadata);

    return comment_id;
  }
});

//////////////////////// like functions

exports.countlikeComments = functions.database.ref('/{category}/{postid}/comments/{commentId}/likes/{userPublicKey}').onWrite(async (change, context) => {

    const collectionRef = change.after.ref.parent;
    const countRef = collectionRef.parent.child('likes_count');

    const author_public_key = await admin.database().ref(context.params.category).child(context.params.postid).child('comments').child(context.params.commentId).child('user_public_key').once('value');

    // this scope invokes only when the user deletes his post.
    if (!author_public_key.exists()) {
        return;
    }
    var a;

    let increment;
    if (change.after.exists() && !change.before.exists()){
        increment = 1;
        var metadata = {
          postId: context.params.postid,
          category: context.params.category,
          commentId: context.params.commentId,
        }
        a = setNotification("like_comment", context.params.userPublicKey, author_public_key.val(), metadata)
    }
    else
        increment = -1

    await countRef.transaction((current) => {
        return (0 || current) + increment;
    });
    await a;

});

exports.countlikePosts = functions.database.ref('/{category}/{postid}/likes/{userPublicKey}').onWrite(async (change, context) => {

    const collectionRef = change.after.ref.parent;
    const countRef = collectionRef.parent.child('likes_count');

    const author_public_key = await admin.database().ref(context.params.category).child(context.params.postid).child('user_public_key').once('value');

    // this scope invokes only when the user deletes his post.
    if (!author_public_key.exists()) {
        return;
    }

    let increment;
    var a;
    if (change.after.exists() && !change.before.exists()) {
        increment = 1;
        var metadata = {
          postId: context.params.postid,
          category: context.params.category,
        }
        a = setNotification("like_post", context.params.userPublicKey, author_public_key.val(), metadata)
    } else {
        increment = -1
    }

    await countRef.transaction((current) => {
        return (0 || current) + increment;
    });
    await a;

});

///////////////////////////////// end like functions

exports.setMsg = functions.https.onCall(async (snapshot, context) => {
    const user_message = snapshot.message.trim();
    const isVisible = (snapshot.isVisible === "true") ? true : false;
    const myPrivateKey = context.auth.uid;
    const now = Date.now();
    var category = snapshot.category;

    if (user_message.length < 2 || user_message.length > 5000) return "FAILURE";

    try {
        const account = await verifyUser(myPrivateKey);
        if (account === null)
            throwError("auth_failed");

        const myPublicKey = account.publicKey;

        var data = {
            message: user_message,
            timestamp: now,
            user_public_key: myPublicKey,
        };

        if (!isVisible) {
            data.visible = isVisible;
        }

        const where = (isVisible ? "posts" : "private_posts");
        const post_id = await (admin.database().ref(category).push().key);
        const set_data = admin.database().ref(category).child(post_id).set(data);
        const set_category = admin.database().ref('user_public').child(myPublicKey).child(where).child(post_id).set(category);
        const ref = admin.database().ref('user_public').child(myPublicKey).child('profile').child('posts_count');
        const incrementMyPostsCounter = ref.transaction((current) => {
            return (current || 0) + 1;
        });


        await set_data;
        await set_category;
        await incrementMyPostsCounter;

        return post_id;
    } catch (info) {
        console.log("error->" + info)
        return "ERROR";
    }
});

exports.addUserToDB = functions.auth.user().onCreate(event => {
    const timestamp = Date.now();
    var public_user_key = admin.database().ref('user_public').push().key;
    return admin.database().ref('users/' + event.uid).set({
        email: event.email,
        joinTimestamp: timestamp,
        user_public: public_user_key,
        accountStatus: 'A',
    })
});


function getNickname(publicKey) {
    return admin.database().ref('user_public').child(publicKey).child('profile').child('nickname').once('value').then(res => {
        let user_nickname = res.val();
        if (user_nickname === null)
            user_nickname = "No Nickname";
        return user_nickname;
    })
}

async function getMessages(category, query, limit, myPrivateKey) {
    var postsArray = {};
    postsArray['posts'] = [];
    var promiseArray = [];
    const path = query;

    return query.once('value').then(snapshot => {

        var counter = -1;
        if (snapshot.numChildren() < limit) counter = 0;

        snapshot.forEach(childSnapshot => {

            const postId = childSnapshot.key;

            if (query.toString().includes("user_public")) { // check whether reading user's posts. If so, get the right category of a post.
                category = childSnapshot.val();
                query = category + "/" + postId;
            } else if (query.toString().includes("comments")) {
                query = path;
                query = query.ref.child(postId);
            } else
                query = category + "/" + postId;

            if (counter === -1) {
                postsArray['upNext'] = postId;
                counter++;
            } else {
                promiseArray[counter++] = getPost(myPrivateKey, category, query, postId, "");
            }
        })
        return Promise.all(promiseArray).then(values => { // waits until it finishes getting all posts
            for (var i = 0; i < promiseArray.length; i++) {
                const current_value = values[i];
                if (current_value !== "") {
                    postsArray.posts[i] = current_value; // converting promise into object text
                }
            }
            return "";
        }).then(() => {
            postsArray.posts.reverse();
            return JSON.stringify(postsArray);
        })
    })

}


exports.fetchComments = functions.https.onCall(async (data, context) => {

    const startFrom = data.startFrom;
    const category = data.category;
    const postId = data.postId;
    var myPublicKey = "";
    const limit = (6 + 1);
    var myPrivateKey = "";

    if (context.hasOwnProperty('auth')) {
        var uid = context.auth.uid;
        if (uid !== undefined && uid !== "") {
              myPrivateKey = uid;
        }
    }

    let query = admin.database().ref(category).child(postId).child('comments');

    return fetchMsgsAssistance(category, query, limit, myPrivateKey, startFrom, false);

})


function findIndex(array, value){
  let i = 0;
  for (i; i < array.length; i++){
    if (array[i] === value)
        return i;
  }
  return -1;
}

function cloneArray(array){
  var newArray = [];
  let i = 0;
  for (i; i< array.length; i++){
    newArray = array[i];
  }
  return newArray;

}

function evaluateArray(array){

  const LIKE = 1;
  const COMMENT = 2;


  var points_array = [];
  let i = 0;
  for (i=0; i<array.length; i++){
    const current = array[i];
    var points = 0;

    if (current.child('likes_count').exists())
      points+=current.child('likes_count').val()*LIKE;
    if (current.child('comments_count').exists())
      points+=current.child('comments_count').val()*COMMENT;

      points_array[i] = points;
  }

  console.log(points_array + "... points array")


  var sorted_points_array = [];

  for (i=0; i< points_array.length; i++){ // shallow copying
    sorted_points_array[i] = points_array[i];
  }

  sorted_points_array.sort(function(a, b) {
    return b-a;
  });

  console.log(sorted_points_array + "... sorted points array")

  var array_data = [];

  i = 0;
  for (i; i<sorted_points_array.length; i++){
    const index = findIndex(points_array, sorted_points_array[i]);
    points_array[index] = -1;
    console.log(index)
    array_data[i] = array[index];
  }

  return array_data;

}

function copySomeElementsFromTmp(array, index_start, howManyItemsToCopy){
  var newArray = [];

  let i=0;
  for (i; i<howManyItemsToCopy; i++){
      if (array.length > index_start+i){
        newArray[i] = array[index_start+i];
      }
      else
        break;
  }

  console.log(JSON.stringify(newArray)  + "... keep taking from past-week-array")

  return newArray;
}

async function getTopPosts(category, myPrivateKey, startFrom){

  var myPublicKey = "";

      if (myPrivateKey !== "") {
          const account = await verifyUser(myPrivateKey);
          if (account !== null)
            myPublicKey = account.publicKey;
      }


  const week = 1000*60 * 60 * 24 * 7;
  const limit = 6+1;
  var array = []; // posts from the past week

  let query =  admin.database().ref(category).orderByChild("timestamp").startAt(Date.now()-week); //  limitToLast(limit);
  // if query returns less then 'limit' results then execute it again but now with endAt ^ and with a limit


  var postsArray = {};
  postsArray['posts'] = [];
  var counter = 0;

  const a = query.once('value').then(snapshot => {
      snapshot.forEach(childSnapshot => {
      array[counter++] = childSnapshot;
      })
      return null;
    })


  await a;

  // now evaluate the first array:
  var tmp = evaluateArray(array);
  var output = [];

  if (startFrom === "not_define"){

      for (i=0; i<tmp.length; i++){
        if (limit > i)
          output[i] = tmp[i];
      }
  }
  else{
    // todo. serach it in tmp array.
    console.log(JSON.stringify(tmp) + " ==> array of tmp")
      for (i=0; i<tmp.length; i++){
        console.log(tmp[i].key + "........." + startFrom + ",, output len: " + output.length)
          if (tmp[i].key === startFrom && output.length < limit){
            output = copySomeElementsFromTmp(tmp, i, limit);
            break;
          }
      }

  }

  const howManyMore = (limit-output.length);


  if (howManyMore > 0 && array.length > 0){
    // not working

  /*  console.log('how many more ?? ' + howManyMore)
    console.log(JSON.stringify(array) + "..  array")


    var timestamp_of_last_item = array[0].child('timestamp').val() - 1;
    console.log(timestamp_of_last_item + " ... timestamp_of_last_item")

    query = admin.database().ref(category).orderByChild("timestamp").endAt(timestamp_of_last_item).limitToLast(howManyMore);

     var tmp_array = [];
     const b = query.once('value').then(snapshot => {
        counter = 0;
         snapshot.forEach(childSnapshot => {
            if (counter === 0) {
              postsArray['upNext'] = childSnapshot.key;
              counter++;
            }
            else
              tmp_array[counter-1] = childSnapshot;
         })
         return null;
       })

       await b;

       console.log(JSON.stringify(tmp_array) + " ......... tmp_array; how many more" + howManyMore )

   var tmp2 = evaluateArray(tmp_array);

    i=0;
    for (i; i < tmp_array.length; i++){
        output[output.length] = tmp2[i];
    }
*/
  }
  else if (limit === output.length && howManyMore === 0){
     postsArray['upNext'] = output[output.length-1].key;
     output[output.length-1] = -1;
  }

// convert objects to posts:

var promiseArray = [];
for (i=0; i<output.length; i++){
  if (output[i] !== -1)
    promiseArray[i] = getPost(myPrivateKey, category, "", output[i].key , output[i])
}


return Promise.all(promiseArray).then(values => { // waits until it finishes getting all posts
  let counter_arr = 0;
    for (i=0; i < promiseArray.length; i++) {
        const current_value = values[i];
        if (current_value !== undefined){
            postsArray.posts[counter_arr] = current_value; // converting promise into object text
            counter_arr++;
        }
    }
    return "";
}).then(() => {
    return JSON.stringify(postsArray);
})

}

function fetchMsgsAssistance(container, query, limit, myPrivateKey, startFrom, topPosts) {

    if (topPosts){
        return getTopPosts(container, myPrivateKey, startFrom);
    }

    if (startFrom === "end_of_list")
        return "";
  //  if (topPosts && startFrom === "not_define")
    //    query = query.orderByChild('likes_count').limitToLast(limit); // fix me!!!
    else if (!topPosts && startFrom === "not_define")
        query = query.limitToLast(limit);
    else
        query = query.orderByKey().endAt(startFrom).limitToLast(limit);


    return getMessages(container, query, limit, myPrivateKey);
}

exports.comments_garbage_collector = functions.database.ref('/{category}/{postid}/comments/{comment_id}/status').onWrite(async (change, context) => {

    if (change.after.val() === 'delete-me') {
        const one = admin.database().ref(context.params.category).child(context.params.postid).child('comments').child(context.params.comment_id).remove();

        const two = change.after.ref.parent.parent.parent.child('comments_count').transaction((current) => {
            return (current || 0) - 1;
        })

        await one;
        await two;
        return "ok"
    } else
        return "na"

})

exports.posts_garbage_collector = functions.database.ref('/{category}/{postid}/status').onWrite(async (change, context) => {

    const user_public_key = await admin.database().ref(context.params.category).child(context.params.postid).child('user_public_key').once('value');

    if (change.after.val() === 'delete-me') {
        const ref = admin.database().ref('user_public').child(user_public_key.val()).child('profile').child('posts_count');
        const one = admin.database().ref(context.params.category).child(context.params.postid).remove();
        const two = admin.database().ref('user_public').child(user_public_key.val()).child('posts').child(context.params.postid).remove();
        const three = ref.transaction((current) => {
            return (current || 0) - 1;
        })

        await one;
        await two;
        await three;
        return "ok";
    } else
        return "na";
})


exports.fetchMsgs = functions.https.onCall(async (data, context) => {

    const startFrom = data.startFrom;
    const category = data.category;
    const limit = (6 + 1);
    const container = category;
    var myPrivateKey = "";

  if (context.hasOwnProperty('auth')) {
      var uid = context.auth.uid;
      if (uid !== undefined && uid !== "") {
        myPrivateKey = uid;
      }
  }

    var topPosts = false;
    if (data.hasOwnProperty('topPosts')) {
        console.log("top posts!")
        topPosts = true;
    }

    let query = admin.database().ref(container);

    return fetchMsgsAssistance(container, query, limit, myPrivateKey, startFrom, topPosts);
})

async function isModerator(privateKey){
  const result = await admin.database().ref('app').child('moderators').child(privateKey).once('value');
  return result.exists();
}

async function getPost(myPrivateKey, category, query, postId, snapshot_if_exists) {

var my_public_key = "";

  if (myPrivateKey !== "") {
        const account = await verifyUser(myPrivateKey);
        if (account !== null)
          my_public_key = account.publicKey;
  }

    var data = {};
    const snapshot = (snapshot_if_exists !== "") ? snapshot_if_exists : (await admin.database().ref(query).once('value'));
    if (snapshot.numChildren() < 3 || !snapshot.exists()) // skip if a post is damaged
        return null;

    const likes_count = snapshot.child('likes_count').val();
    const comments_count = snapshot.child('comments_count').val();
    const isVisible = snapshot.child('visible').val();

    data.message = snapshot.child('message').val();
    if (likes_count !== null) data.likes_count = likes_count;
    if (comments_count !== null) data.comments_count = comments_count;
    data.cat = category;

    const user_public_key = snapshot.child('user_public_key').val();

    if (isVisible !== null && isVisible === false) {
        data.nickname = "private"
        data.user_public_key = "private";
    } else {
        data.user_public_key = user_public_key;
        const nickname = await getNickname(user_public_key);
        data.nickname = nickname;

        const has_profile_pic = await hasProfilePicture(user_public_key);
        if (has_profile_pic) {
            data.has_p_img = true;
        }
    }

    var moderator = false;

    if (myPrivateKey !== ""){
       moderator = await isModerator(myPrivateKey);
    }

    if (my_public_key === user_public_key || moderator) {  // perform check!!
        data.isauthor = true;
    }

    if ((my_public_key !== "" && my_public_key !== "-") && snapshot.child('likes').child(my_public_key).exists()) // if the user likes the post
        data.doILike = true;

    data.postId = postId;
    data.timestamp = snapshot.child('timestamp').val();
    return data;
}

function hasProfilePicture(user_public_key) {
    return admin.database().ref('user_public').child(user_public_key).child('profile').child('profileImage').once('value').then(res => {
        return res.exists();
    });
}

exports.fetchUserMsgs = functions.https.onCall(async (data, context) => {

    const user_public_key = data.publicKey;
    var myPrivateKey = "";

    if (context.hasOwnProperty('auth')) {
        var uid = context.auth.uid;
        if (uid !== undefined && uid !== "") {
              myPrivateKey = uid;
        }
    }

    const startFrom = data.startFrom;
    const limit = (6 + 1);
    //  const myPublicKey = data.myPublicKey;

    let query = admin.database().ref('user_public').child(user_public_key).child('posts');

    return fetchMsgsAssistance("", query, limit, myPrivateKey, startFrom, false);

})

/////////////////////////////////////////// firebase storage functions:



const mkdirp = require('mkdirp');
const path = require('path');
const os = require('os');
const fs = require('fs');

exports.myStorageInspector = functions.storage.object().onFinalize(async (object) => {
  const fileBucket = object.bucket; // The Storage bucket that contains the file.
  const filePath = object.name; // File path in the bucket.
  const contentType = object.contentType; // File content type.
  const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.
  const fileName = path.basename(filePath);


  if (fileName !== 'profile'){ // check if you're uploading a file

       const account = await verifyUser(fileName);

       if (account === null || !contentType.startsWith('image/')) { // auth failed, remove uploaded picture
           const file_to_remove = bucket.file(filePath); // get a reference to the file
           await file_to_remove.delete();  // Delete the file
           return console.log("auth failed")
       }
       else{
          const publicKey = account.publicKey;
          console.log("public key " + publicKey)
          const bucket = admin.storage().bucket(object.bucket);
          const file = bucket.file(filePath);
          const tempFilePath = path.join(os.tmpdir(), fileName);

          const metadata = {
            contentType: contentType,
          };
          await bucket.file(filePath).download({destination: tempFilePath});
          console.log('Image downloaded locally to', tempFilePath);
          const path_to_profile_dir = "users/" + publicKey;

          await bucket.upload(tempFilePath, {
            destination: path.join(path_to_profile_dir, "profile"),
            metadata: metadata,
          });
           fs.unlinkSync(tempFilePath); //delete tmp pic
           const file_to_remove = bucket.file(filePath); // Get a reference to the storage service, which is used to create references in your storage bucket
           await file_to_remove.delete(); // Delete uploaded file in 'tmp'

           await admin.database().ref('user_public').child(publicKey).child('profile').child('profileImage').set('t');

           return "OK";
        }
    }
    else{
      console.log("already done")
    }
 });



 exports.removeProfilePicture = functions.database.ref('user_public/{user_public_key}/profile/profileImage').onDelete(async (snapshot, context) => {

   const bucket = admin.storage().bucket();
   const filePath = "users/" + context.params.user_public_key + "/profile"
   const file = bucket.file(filePath); // reference

   const file_to_remove = bucket.file(filePath); // Get a reference to the storage service, which is used to create references in your storage bucket
   return file_to_remove.delete(); // Delete uploaded file in 'tmp'

 });
