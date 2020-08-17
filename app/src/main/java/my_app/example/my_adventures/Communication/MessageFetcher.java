package my_app.example.my_adventures.Communication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.ui.account.BlockList;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.ui.item.ItemComment;
import my_app.example.my_adventures.ui.item.ItemPost;

public class MessageFetcher {
    public static String DEFAULT_UP_NEXT = "not_define";
    public static String DEFAULT_END_OF_LIST = "end_of_list";
    private String upNext = DEFAULT_UP_NEXT;
    private String category;
    private ArrayList<Item> messageContainer = new ArrayList<>();
    private String functionToCall;
    private Map<String, Object> data = new HashMap<>();


    public MessageFetcher(String functionToCall, String category){
        this.functionToCall = functionToCall;
        this.category = category;
    }

  public void addMetadataToRequest(String name, String text){
      data.put(name, text);
  }

    public Task<String> fetchMessagesFromServer(){
        messageContainer.clear();

        data.put("startFrom", upNext);
        data.put("category", category);
        data.put("myPublicKey", User.getPublicKey());

        System.out.println("start from: " + upNext  + "," + " cat: " + category + ", userid " + User.getPublicKey() + ", itemID: " );

        return MainActivity.mFunctions
                .getHttpsCallable(functionToCall)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {
                        String answer;
                        answer = (String) task.getResult().getData();
                        System.out.println("ANSWER: "  + answer);
                        if (answer != null && !answer.isEmpty()){
                            parseMessages(answer);
                        }

                            return "";
                    }
                });
    }

    public void reset(){
        upNext = DEFAULT_UP_NEXT;
        messageContainer.clear();
    }

    public void parseMessages(String fresh_msgs){
      String tmp_category;
        try {

            JSONObject obj = new JSONObject(fresh_msgs);
            JSONArray messages_array = obj.getJSONArray("posts");

                if (obj.has("upNext")) {
                    upNext = obj.getString("upNext");
                }
                else
                    upNext = DEFAULT_END_OF_LIST;

            for (int i = 0; i < messages_array.length(); i++) {
                int likes = 0, comments = 0;
                boolean doILike = false, isAuthor = false, has_profile_img = false;

                String user_public_key = messages_array.getJSONObject(i).getString("user_public_key");
                String messages = messages_array.getJSONObject(i).getString("message");
                String nickname = messages_array.getJSONObject(i).getString("nickname");
                String time = messages_array.getJSONObject(i).getString("timestamp");
                String messageId = messages_array.getJSONObject(i).getString("postId");

                if (messages_array.getJSONObject(i).has("likes_count")) {
                    likes = Integer.parseInt(messages_array.getJSONObject(i).getString("likes_count"));
                }
                if (messages_array.getJSONObject(i).has("comments_count")) {
                    comments = Integer.parseInt(messages_array.getJSONObject(i).getString("comments_count"));
                }
                if (messages_array.getJSONObject(i).has("doILike")) {
                    doILike = messages_array.getJSONObject(i).getBoolean("doILike");
                }
                if (messages_array.getJSONObject(i).has("isauthor")) {
                    isAuthor = messages_array.getJSONObject(i).getBoolean("isauthor");
                }
                if (messages_array.getJSONObject(i).has("has_p_img")) {
                    has_profile_img = messages_array.getJSONObject(i).getBoolean("has_p_img");
                }
                if (messages_array.getJSONObject(i).has("cat")){
                    tmp_category = messages_array.getJSONObject(i).getString("cat");
                } else
                    tmp_category = category;

                if (! (BlockList.isUserBlocked(user_public_key) || BlockList.isMessageBlocked(messageId))) {
                    Item item;
                    if (functionToCall.equals(MessageFetcher_Comments.FUNCTION_NAME)) {
                        String postId = data.get("postId").toString();
                        item = new ItemComment(tmp_category, user_public_key, postId, messages, nickname, time);
                        ((ItemComment) item).setCommentId(messageId);
                    } else {
                        item = new ItemPost(tmp_category, user_public_key, messageId, messages, nickname, time);
                    }

                    item.setLikes(likes);
                    item.setComments(comments);
                    item.setIsAuthor(isAuthor);
                    item.setDoILikeThis(doILike);

                    item.setHasProfilePicture(has_profile_img);
                    messageContainer.add(item);
                }
            }
        }
        catch(Exception e){
            System.out.println("Error caught in message fetcher: " +e.getMessage());
        }
    }

    public String getUpNext(){
        return upNext;
    }

    public ArrayList<Item> getFreshMessages() {
        System.out.println("#### " + messageContainer.size());
      return messageContainer; }

    public boolean areThereMore() {
      return !upNext.equals(DEFAULT_END_OF_LIST);
    }

    public int getSizeOfFreshMsgs() {
        return getFreshMessages().size();
    }

    public void doNotLoadForNow() {
        upNext = DEFAULT_END_OF_LIST;
    }
}
