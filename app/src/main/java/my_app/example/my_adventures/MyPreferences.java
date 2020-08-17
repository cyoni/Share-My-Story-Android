package my_app.example.my_adventures;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import my_app.example.my_adventures.notifications.NotificationItem;
import my_app.example.my_adventures.notifications.Notifications;
import my_app.example.my_adventures.ui.account.User;

import static android.content.Context.MODE_PRIVATE;

public class MyPreferences {


    public static final String USER_FOLDER = "user";
    public static String DOES_NOT_EXIST_CODE = "-";

    public static void setSharedPreference(Activity activity, String folder, String key, String value){
        SharedPreferences.Editor editor = activity.getSharedPreferences(folder, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSharedPreference(Activity activity, String folder, String key){
        SharedPreferences prefs = activity.getSharedPreferences(folder, MODE_PRIVATE);
        return prefs.getString(key, DOES_NOT_EXIST_CODE);
    }

    public static String getUserPublicKey(Activity activity){
        return getSharedPreference(activity, MyPreferences.USER_FOLDER, "user_public_key");
    }

    public static void saveNickname(Activity activity, String nickname) {
        MyPreferences.setSharedPreference(activity, MyPreferences.USER_FOLDER, "nickname", nickname);
    }

    public static void savePublicKey(Activity activity, String key) {
        MyPreferences.setSharedPreference(activity, MyPreferences.USER_FOLDER, "user_public_key", key);
    }

    public static String getNickname(Activity activity) {
        return getSharedPreference(activity, MyPreferences.USER_FOLDER, "nickname");
    }

    public static void remove(String folder, String text, Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(folder, MODE_PRIVATE);
        prefs.edit().remove(text).apply();
    }

    public static void saveNotifications(Activity activity, ArrayList<NotificationItem> items) {
        SharedPreferences prefs = activity.getSharedPreferences(USER_FOLDER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(items);
        editor.putString(Notifications.NOTIFICATIONS + User.getPublicKey(), json);
        editor.apply();
    }

    public static ArrayList<NotificationItem> getLocalNotifications(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("user", MODE_PRIVATE);
        String serializedObject = prefs.getString(Notifications.NOTIFICATIONS + User.getPublicKey(), "-");

        ArrayList<NotificationItem> arrayItems = new ArrayList<>();
        if (serializedObject != null && !serializedObject.equals("-")) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<NotificationItem>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public static void saveBlockList(Activity activity, HashSet<String> list, String key) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(USER_FOLDER, MODE_PRIVATE).edit();
        editor.putStringSet(key, list);
        editor.apply();
    }
}
