package my_app.example.my_adventures.Communication;

public class MessageFetcher_UsersPosts extends MessageFetcher{

    public MessageFetcher_UsersPosts(String userPublicKey) {
        super("fetchUserMsgs", "");
        addMetadataToRequest("publicKey", userPublicKey);
    }

}
