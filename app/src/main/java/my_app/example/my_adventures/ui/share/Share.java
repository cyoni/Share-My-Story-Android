package my_app.example.my_adventures.ui.share;

import android.app.Activity;
import android.content.Intent;

public class Share {

    public static void share(String subject, String text, Activity activity) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
