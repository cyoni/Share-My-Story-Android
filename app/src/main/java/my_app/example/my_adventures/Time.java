package my_app.example.my_adventures;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Time {

/*    public static String getDay(int day){
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[day];
    }*/

    public static String howLongPassed(long time_in_ms){

        Date sys_date = new Date();
        long local_time_right_now = sys_date.getTime();
        long sub = (local_time_right_now - time_in_ms)/ (1000 * 60);
        int minutes = (int) Math.floor(sub);
        int seconds = (int) (local_time_right_now-time_in_ms) / 1000;
        int hours_past_since_message_sent = minutes/60;
        int hours_past_since_start_of_day = (int) Math.floor((local_time_right_now / (1000 * 60 * 60)) % 24);
        String AM_PM = "";
        if (TimeZone.getTimeZone("America") == TimeZone.getDefault())
            AM_PM = "a";
        String pattern = "MMM dd,HH:mm "+ AM_PM +",EEEE";
        DateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());

        String[] local_time_and_date = formatter.format(new Date(time_in_ms)).split(",");
        String date = local_time_and_date[0];
        String time = local_time_and_date[1];
        String day = local_time_and_date[2];

        if (minutes < 1)
            return "Just now";
        else if (minutes == 1)
            return "1m";
        else if (minutes < 60)
            return minutes + "ms";
        else if (hours_past_since_message_sent <= hours_past_since_start_of_day)
            return time;
        else if (hours_past_since_message_sent < 24) // yesterday
            return "Yesterday " + time;
        else if (hours_past_since_message_sent < (168-24)) // the past week
            return day + " " + time;
        else{ // old - more than a week
            return date;
        }
    }

    public static long now(){
        return System.currentTimeMillis();
    }

}
