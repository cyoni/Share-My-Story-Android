package my_app.example.my_adventures.notifications;

public class NotificationComment extends NotificationItem {

    private String commentId;
    private String postId;
    private String userId;

    public NotificationComment(String user_id, String postId, String commentId, String message, long timestamp) {
        super("commenting", message, timestamp);
        metadata = user_id;
        this.postId = postId;
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

}
