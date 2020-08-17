package my_app.example.my_adventures.ui.account;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.HttpsCallableResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.MyPreferences;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.toast;

public class NicknameManagerActivity extends AppCompatActivity {
    private TextView nickname, error_txt;
    private Button submit;
    private boolean valid_input = false;
    private int limit = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Set your nickname");

        nickname = findViewById(R.id.txt_nickname);
        submit = findViewById(R.id.submit);
        error_txt = findViewById(R.id.taken_nickname_txt);

        if (!User.getNickname().isEmpty()) {
            nickname.setText(User.getNickname());
        }

        setTextListener();

        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_nickname = nickname.getText().toString();
                str_nickname = str_nickname.trim();

                if (User.getNickname().equals(str_nickname))
                    finish();
                else if (valid_input) {
                    submit.setEnabled(false);
                    setNickname(str_nickname);
                }
            }
        });
    }


    private void setTextListener() {
        nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String str_nickname = editable.toString().trim();

                if (str_nickname.equals(User.getNickname()))
                    showErrorText("");
                else if (str_nickname.length() == 0) {
                    showErrorText("Nickname cannot be empty.");
                } else if (str_nickname.length() > limit) {
                    showErrorText("Nickname length cannot exceed " + limit + " letters.");
                } else if (str_nickname.contains("$") || str_nickname.contains(".") || str_nickname.contains("\\") || str_nickname.contains("/") || str_nickname.contains("[") || str_nickname.contains("]")) {
                    showErrorText("Nickname cannot contain $,.,\\,/,[,]");
                } else {
                    valid_input = true;
                    doesNicknameExist(str_nickname);
                }
            }
        });
    }

    private void showErrorText(String s) {
        error_txt.setTextColor(Color.RED);
        error_txt.setText(s);
        error_txt.setVisibility(View.VISIBLE);
        valid_input = false;
    }

    private void doesNicknameExist(String str_nickname) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("nicknames").child(str_nickname.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showErrorText("This nickname is taken. ❌");
                } else {
                    String s = "This nickname is free.\uD83D\uDE00";
                    error_txt.setTextColor(Color.parseColor("#10c257"));
                    error_txt.setText(s);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void setNickname(final String nickname_str) {

        if (valid_input) {
            System.out.println(nickname_str + "," + User.getPrivateKey());
            submit.setEnabled(false);
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nickname_str);
            MainActivity.mFunctions
                    .getHttpsCallable("updateNickname")
                    .call(data)
                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                        @Override
                        public String then(@NonNull Task<HttpsCallableResult> task) {
                            String answer = task.getResult().getData().toString();
                            System.out.println("nickname from server, answer: " + answer);
                            if (answer.equals("S")) {
                                User.setNickname(nickname_str.trim());
                                MyPreferences.saveNickname(NicknameManagerActivity.this, nickname_str.trim());
                                closeActivity();
                                return "";
                            } else {
                                toast.showMsg(getApplicationContext(), "An error occurred. Please try again later.");
                                System.out.println("update nickname " + task.getResult().getData());
                                submit.setEnabled(true);
                            }
                            return "";
                        }
                    });
        }
    }

    private void closeActivity() {
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
