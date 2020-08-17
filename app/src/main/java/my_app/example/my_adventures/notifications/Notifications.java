package my_app.example.my_adventures.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.MyPreferences;
import my_app.example.my_adventures.comparator;
import my_app.example.my_adventures.ui.account.User;


public class Notifications {

    private final MainActivity mainActivity;
    public int newNotifications = 0;
    public static String UNREAD_NOTIFICATIONS = "unread-notifications";
    public static String NOTIFICATIONS = "notifications-";

    PriorityQueue<NotificationItem> notifications = new PriorityQueue<>(5, new comparator());
    public Notifications(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void setUnreadNotifications(int count) {
        this.newNotifications = count;
    }

    public void startListeningForNotificationsAndSaveThemLocally() {

        MainActivity.mDatabase.child("notifications").child(User.getPublicKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    System.out.println("GOT: " + snapshot.getValue().toString());
                    if (snapshot.child("like_comment").exists())
                        like_comment(snapshot);

                    if (snapshot.child("like_post").exists())
                        like_post(snapshot);

                    if (snapshot.child("follow").exists())
                        follow(snapshot);

                    if (snapshot.child("announcement").exists())
                        announcement(snapshot);

                    if (snapshot.child("commenting").exists())
                        commenting(snapshot);

                    if (snapshot.child("status").exists())
                        status(snapshot);

                    saveNotificationsLocally();
                    deleteNotificationsFromServer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void status(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.child("status").getChildren().iterator();
        DataSnapshot tmp;

        while (iterator.hasNext()) {
            tmp = iterator.next();
            //noinspection unchecked
            HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();

            long timestamp = (long) dataMap.get("timestamp");
            String user_id = (String) dataMap.get("user_id");
            String text = (String) dataMap.get("text");

            notifications.add(new NotificationStatus(user_id, text, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }


    private void like_comment(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.child("like_comment").getChildren().iterator();
        DataSnapshot tmp;

        while (iterator.hasNext()) {
            tmp = iterator.next();
            //noinspection unchecked
            HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();

            long timestamp = (long) dataMap.get("timestamp");
            String postId = (String) dataMap.get("postId");
            String user_id = (String) dataMap.get("user_id");
            String text = (String) dataMap.get("text");
            String commentId = (String) dataMap.get("commentId");

            notifications.add(new NotificationComment(user_id, postId, commentId, text, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }

    public void incrementNotifications() {
        newNotifications++;
    }

    private void like_post(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.child("like_post").getChildren().iterator();
        DataSnapshot tmp;

        while (iterator.hasNext()) {
            tmp = iterator.next();

            //noinspection unchecked
            HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();
            long timestamp = (long) dataMap.get("timestamp");
            String postId = (String) dataMap.get("postId");
            String user_id = (String) dataMap.get("user_id");
            String text = (String) dataMap.get("text");

            notifications.add(new NotificationPost(user_id, postId, text, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }

    private void follow(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.child("follow").getChildren().iterator();
        DataSnapshot tmp;

        while (iterator.hasNext()) {
            tmp = iterator.next();
            //noinspection unchecked
            HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();

            long timestamp = (long) dataMap.get("timestamp");
            String user_id = (String) dataMap.get("user_id");
            String nickname = (String) dataMap.get("nickname");

            notifications.add(new NotificationFollow(user_id, nickname, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }

    private void announcement(DataSnapshot snapshot) {
        for (DataSnapshot tmp : snapshot.child("announcement").getChildren()) {
            //noinspection unchecked
            HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();
            long timestamp = (long) dataMap.get("timestamp");
            String text = (String) dataMap.get("text");

            notifications.add(new NotificationItem("announcement", text, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }

    private void commenting(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.child("commenting").getChildren().iterator();
        DataSnapshot tmp;
        HashMap<String, Object> dataMap;

        while (iterator.hasNext()) {
            tmp = iterator.next();
            //noinspection unchecked
            dataMap = (HashMap<String, Object>) tmp.getValue();

            long timestamp = (long) dataMap.get("timestamp");
            String postId = (String) dataMap.get("postId");
            String commentId = (String) dataMap.get("commentId");
            String user_id = (String) dataMap.get("user_id");
            String text = (String) dataMap.get("text");

            notifications.add(new NotificationComment(user_id, postId, commentId, text, timestamp));
            incrementNotifications();
            mainActivity.updateBadgeNotification();
        }
    }

    private void saveNotificationsLocally() {
        ArrayList<NotificationItem> items = new ArrayList<>();
        items.addAll(notifications); // add new notifications to the list
        items.addAll(MyPreferences.getLocalNotifications(mainActivity));  // get old notifications and add them
        MyPreferences.saveNotifications(mainActivity, items); // apply
        notifications.clear();
    }

    private void deleteNotificationsFromServer() {
        MainActivity.mDatabase.child("notifications").child(User.getPublicKey()).removeValue();
    }

    public int getCount() {
        return newNotifications;
    }

}
