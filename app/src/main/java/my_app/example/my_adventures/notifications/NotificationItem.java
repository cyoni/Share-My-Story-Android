package my_app.example.my_adventures.notifications;

public class NotificationItem {

    private final String message;
    private final String type;
    private final long timestamp;
    private boolean newNotification;
    String metadata;

    public NotificationItem(String type, String message, long timestamp){
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.newNotification = true;
    }

    public void setNewNotification(boolean state){
        this.newNotification = state;
    }

    public String getMetadata(){
         return metadata;
    }

    public boolean getNewNotification(){
        return newNotification;
    }

    public String getMessage() {
        return message;
    }

    public String getType(){
        return type;
    }

    public long getTimestamp(){
        return timestamp;
    }


}
