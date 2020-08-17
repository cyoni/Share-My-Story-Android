package my_app.example.my_adventures.notifications;

public class NotificationPost extends NotificationItem {

   // private final String userId;
    private final String postId;

    public NotificationPost(String userId, String postId, String message, long timestamp) {
        super("post", message, timestamp);
       metadata = userId;
        this.postId = postId;
    }

    //public String getUserId(){
      //  return userId;
    ///}

    public String getPostId(){
        return postId;
    }


}
