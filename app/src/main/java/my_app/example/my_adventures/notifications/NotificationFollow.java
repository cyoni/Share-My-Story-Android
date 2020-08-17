package my_app.example.my_adventures.notifications;

public class NotificationFollow extends NotificationItem {


    public NotificationFollow(String userId, String nickname, long timestamp) {
        super("follow", "<b>"+ nickname +"</b> started following you.", timestamp);
        metadata = userId;
    }

}
