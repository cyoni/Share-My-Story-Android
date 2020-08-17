package my_app.example.my_adventures.ui.item;

import java.io.Serializable;

import my_app.example.my_adventures.Time;

// serializable -> to convert an item object to an object that will pass with intent

public class Item implements Serializable {
    private long time;
    private String user_public_key;
    private final String category;
    private boolean isAuthor;
    private boolean doILikeThis;
    private String message;
    private String nickname;
    private String publish_date;
    private int likes;
    private int comments;
    private String container = "post";
    protected String postId;
    private boolean hasProfilePicture;


    public Item(String category, String user_public_key, String postId, String content, String nickname, String publish_date){
        this.category = category;
        this.user_public_key = user_public_key;
        this.postId = postId;
        this.message = content;
        this.nickname = nickname;
        this.likes = 0;
        this.comments = 0;
        this.doILikeThis = false;
        this.isAuthor = false;

        setTime(publish_date);
        hasProfilePicture = false;
    }

    public void setLikes(int likes){
        this.likes = likes;
    }

    public void setComments(int comments){
        this.comments = comments;
    }

    private void setTime(String publish_date) {
        if (publish_date.equals("NOW")) {
            this.publish_date = "Just now";
            time = Time.now();
        }
        else {
            this.publish_date = Time.howLongPassed(Long.parseLong(publish_date));
            time = Long.parseLong(publish_date);
        }
    }

    public String getCategory(){
        return category;
    }

    public String getMessage(){
        return message;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTimestamp() {
        return publish_date;
    }

    public long time(){
        return time;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public boolean getDoILikeThisMsg(){
        return doILikeThis;
    }

    public String getPublicKey() {
        return user_public_key;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public void setDoILikeThis(boolean doI) {
        this.doILikeThis = doI;
    }

    public boolean isAuthor(){return isAuthor;}

    public String getPostId(){
        return postId;
    }

    public String getContainer() {
        return container;
    }

    public void setPostId(String postID) {
        this.postId = postID;
    }

    public void decrementLike() {
        this.likes--;
    }

    public void increaseLikes() {
        this.likes++;
    }

    public boolean hasProfilePicture() {
        return hasProfilePicture;
    }

    public void setHasProfilePicture(boolean bool){
        this.hasProfilePicture = bool;
    }

    public void incrementCommentCounter() {
        comments++;
    }

    public void decrementComments() {
        comments--;
    }

    public void setIsAuthor(boolean b) {
        this.isAuthor = b;
    }

    public void setPublicKey(String publicKey) {
        user_public_key = publicKey;
    }
}
