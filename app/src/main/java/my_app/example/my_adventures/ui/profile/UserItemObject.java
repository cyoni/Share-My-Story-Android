package my_app.example.my_adventures.ui.profile;

public class UserItemObject {


    private String publicKey;
    private String nickname;

    public UserItemObject(String publicKey, String nickname){
        this.publicKey = publicKey;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
