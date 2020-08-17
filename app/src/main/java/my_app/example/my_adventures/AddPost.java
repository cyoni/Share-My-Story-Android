package my_app.example.my_adventures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class AddPost extends AppCompatActivity {

    private TextView content;
    private Button submit;
    private ProgressBar progressbar;
    private CheckBox private_user;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Write a new post");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit_button_click();

            }
        });
        content = findViewById(R.id.txt_content);
        progressbar = findViewById(R.id.progressBar);
        private_user = findViewById(R.id.checkBox);
        category = getIntent().getStringExtra("category");
    }

    private void submit_button_click() {
        if (isMessageValid()){
            submit();
        }
        else{
            toast.showMsg(getApplicationContext(), "Post should be above 5 letters");
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private boolean isMessageValid() {
        String message = content.getText().toString().trim();
        return (message.length() > 5);
    }


    private void submit() {

        progressbar.setVisibility(View.VISIBLE);
        final String message = content.getText().toString();
        submit.setEnabled(false);
        Map<String, Object> data = new HashMap<>();

        data.put("message", message);
        data.put("isVisible", (private_user.isChecked() ? "false" : "true"));
        data.put("category", category);

        MainActivity.mFunctions
                .getHttpsCallable("setMsg")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {

                        String post_id_from_server = String.valueOf(task.getResult().getData());
                        System.out.println("answer:" + post_id_from_server);

                        if (!post_id_from_server.equals("ERROR") && !post_id_from_server.isEmpty()) {
                          /*  String nickname, publicKey;

                            ItemPost item = new ItemPost(category, "", post_id_from_server, message, "", "NOW");

                            if (private_user.isChecked()) {
                                nickname = "private";
                                publicKey = "PRIVATE";
                            } else {
                                nickname = User.getNickname();
                                publicKey = User.getPublicKey();
                            }
                            item.setNickname(nickname);
                            item.setPublicKey(publicKey);

                            ImageManager im = new ImageManager((Activity) getApplicationContext());
                            if (!nickname.equals("private") && im.isImageOnDevice(ProfileActivity.PROFILE_FOLDER, User.getPublicKey()))
                                item.setHasProfilePicture(true);*/

                            sendItemBackToHomeAndClose();
                        } else {
                            notifyUser_error();
                        }
                        return "";
                    }
                });
    }

    private void sendItemBackToHomeAndClose() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);

     //   toast.showMsg_long(this, "Your post is live. Refresh to view.");
        finish();
    }

    private void notifyUser_error() {
        progressbar.setVisibility(View.INVISIBLE);
        toast.showMsg(getApplicationContext(), "Could not post at this moment. Check your internet connection or try again later.");
        submit.setEnabled(true);
    }


}
