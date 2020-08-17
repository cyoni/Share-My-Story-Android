package my_app.example.my_adventures;

import android.content.Context;
import android.widget.Toast;

public class toast {

    public static void showMsg(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showMsg_long(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
