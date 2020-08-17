package my_app.example.my_adventures.ui.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.toast;

public class contactUsActivity extends AppCompatActivity {

    private Button submit;
    private EditText email, name, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        submit = findViewById(R.id.submit);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        message = findViewById(R.id.txt_content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendForm();
            }
        });

       if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Contact us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    private void sendForm() {

        String txt_name = name.getText().toString();
        String txt_email = email.getText().toString();
        String txt_message = message.getText().toString();

        if (txt_message.trim().isEmpty()){
            toast.showMsg(getApplicationContext(), "Please enter a message.");
        }
        else {
            Map<String, Object> data = new HashMap<>();
            data.put("name", txt_name);
            data.put("email", txt_email);
            data.put("message", txt_message);

            submit.setEnabled(false);
            String key = MainActivity.mDatabase.child("contact_us").push().getKey();
            MainActivity.mDatabase.child("contact_us").child(key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    toast.showMsg(getApplicationContext(), "Thank you!");
                    finish();
                }
            });
        }
    }


}
