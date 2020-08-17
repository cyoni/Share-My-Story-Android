package my_app.example.my_adventures.notifications;

public class NotificationStatus extends NotificationItem {

    public NotificationStatus(String userId, String text, long timestamp) {
        super("status", text, timestamp);
        metadata = userId;
    }


}
