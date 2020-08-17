package my_app.example.my_adventures.ui.account;

import android.app.Activity;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.MyPreferences;
import my_app.example.my_adventures.notifications.Notifications;
import my_app.example.my_adventures.ui.home.Home;

public class User {

    private static String public_key = "";
    private static String nickname = "";


    public static String getNickname() {
        return nickname;
    }

    public static void setNickname(String new_nickname) {
        nickname = new_nickname;
    }

    public static String getPublicKey() {
        return User.public_key;
    }

    public static void signOut(Activity activity) {
        MainActivity.mAuth.signOut();
        deleteSharedPreference(activity);
        Home.dontLoadMsgs = false;
        public_key = "";
        nickname = "";
    }

    public static boolean isSignIn() {
        return (MainActivity.mAuth.getCurrentUser() != null);
    }

    private static void deleteSharedPreference(Activity activity) {
        MyPreferences.remove(MyPreferences.USER_FOLDER, "user_public_key", activity);
        MyPreferences.remove(MyPreferences.USER_FOLDER, "user_nickname", activity);
        MyPreferences.remove(MyPreferences.USER_FOLDER, BlockList.MESSAGES_BLOCK_LIST + User.getPublicKey(), activity);
        MyPreferences.remove(MyPreferences.USER_FOLDER, BlockList.USERS_BLOCK_LIST + User.getPublicKey(), activity);
        MyPreferences.remove(MyPreferences.USER_FOLDER, Notifications.NOTIFICATIONS + User.getPublicKey(), activity);
        MyPreferences.remove(MyPreferences.USER_FOLDER, Notifications.UNREAD_NOTIFICATIONS, activity);
        BlockList.clearUsers();
        BlockList.clearMsgs();
    }


    public static void setPublicKey(String key) {
        User.public_key = key;
    }

    public static String getPrivateKey() {
        return MainActivity.mAuth.getUid();
    }
}