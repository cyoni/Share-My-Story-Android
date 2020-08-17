package my_app.example.my_adventures;

import java.util.Comparator;

import my_app.example.my_adventures.notifications.NotificationItem;

public class comparator implements Comparator<NotificationItem> {
    public int compare(NotificationItem s1, NotificationItem s2) {
        if (s1.getTimestamp() < s2.getTimestamp())
            return 1;
        else if (s1.getTimestamp() > s2.getTimestamp())
            return -1;
        return 0;
    }
}