package my_app.example.my_adventures.ui.item;

import java.io.Serializable;

public class ItemPost extends Item  implements Serializable {

    // postid, message, nickname, timestamp
    public ItemPost(String category, String user_public_key, String postId, String content, String nickname, String publish_date) {
        super(category, user_public_key, postId, content, nickname, publish_date);
    }



}
