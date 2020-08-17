package my_app.example.my_adventures.Communication;

public class MessageFetcher_Comments extends MessageFetcher {

    public static String FUNCTION_NAME = "fetchComments";
    public MessageFetcher_Comments(String category, String postId) {
        super(FUNCTION_NAME, category);
        addMetadataToRequest("postId", postId);
    }
}
