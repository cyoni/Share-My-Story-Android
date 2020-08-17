package my_app.example.my_adventures;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;


public class CustomDialog extends Dialog {
    public Activity c;
    private int layout;


    public CustomDialog(Activity activity, int layout) {
        super(activity);
        this.c = activity;
        this.layout = layout;

        Window window = getWindow();
        window.setBackgroundDrawableResource(R.drawable.dialog_background);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout);
    }

}