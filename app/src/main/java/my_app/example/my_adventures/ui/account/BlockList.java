package my_app.example.my_adventures.ui.account;

import android.app.Activity;

import java.util.HashSet;
import java.util.Set;

import my_app.example.my_adventures.MyPreferences;

// fix me
public class BlockList {

    private static HashSet<String> usersBlockList = new HashSet<>();
    private static HashSet<String> messagesBlockList = new HashSet<>();
    public static String USERS_BLOCK_LIST = "usersBlockList-";
    public static String MESSAGES_BLOCK_LIST = "messagesBlockList-";

    public static boolean blockMessage(Activity activity, String id){
        if (messagesBlockList.contains(id))
            return false;
        else{
            messagesBlockList.add(id);
            one(activity);
            return true;
        }
    }

    private static void one(Activity activity) {
        MyPreferences.saveBlockList(activity, messagesBlockList, MESSAGES_BLOCK_LIST + User.getPublicKey());
    }

    private static void two(Activity activity) {
        MyPreferences.saveBlockList(activity, usersBlockList, USERS_BLOCK_LIST + User.getPublicKey());
    }

    public static boolean blockUser(Activity activity, String id){
        if (usersBlockList.contains(id))
            return false;
        else{
            usersBlockList.add(id);
            two(activity);
            return true;
        }
    }

    public static boolean isUserBlocked(String user_public_key) {
        return usersBlockList.contains(user_public_key);
    }

    public static boolean isMessageBlocked(String messageId) {
        return messagesBlockList.contains(messageId);
    }

    public static boolean unBlockUser(Activity activity, String user_public_key){
        if (isUserBlocked(user_public_key)) {
            usersBlockList.remove(user_public_key);
            one(activity);
        return true;
        }
        else
            return false;
    }

    public static boolean unBlockMessage(Activity activity, String messageId){
        if (isMessageBlocked(messageId)) {
            messagesBlockList.remove(messageId);
            two(activity);
            return true;
        }
        else
            return false;
    }

    public static void clearUsers(){
        usersBlockList.clear();
    }

    public static void clearMsgs(){
        messagesBlockList.clear();
    }

    public static void initUsersBlockList(Set<String> set) {
        usersBlockList.addAll(set);
    }
    public static void initMessagesBlockList(Set<String> set) {
        messagesBlockList.addAll(set);
    }
}
