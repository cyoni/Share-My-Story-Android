package my_app.example.my_adventures.ui.item;

public class ItemComment extends Item {

    private String commentId;

    public ItemComment(String category, String userPublicKey, String postId, String msg, String nickname, String publish_date) {
        super(category, userPublicKey, postId, msg, nickname, publish_date);
    }

    public void setCommentId(String commentId){
        this.commentId = commentId;
    }

    public String getCommentId(){
        return commentId;
    }

}
