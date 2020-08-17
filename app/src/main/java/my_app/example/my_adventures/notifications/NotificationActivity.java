package my_app.example.my_adventures.notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import my_app.example.my_adventures.MyPreferences;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.ui.account.User;

public class NotificationActivity extends AppCompatActivity implements NotificationRecycleViewAdapter.ItemClickListener {

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private ArrayList<NotificationItem> localItems = new ArrayList<>();
    private View rootLayout;
    private int revealX, revealY;
    private NotificationRecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setStatusBar();
        initAnimation(savedInstanceState);
        getSavedNotifications();
        initRecycleview();
        setButtons();
    }

    private void setStatusBar() {
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initAnimation(Bundle savedInstanceState) {
        final Intent intent = getIntent();
        rootLayout = findViewById(R.id.root_layout);
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setButtons() {

        Button close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unRevealActivity();
            }
        });

        Button text_clear = findViewById(R.id.clear);
        text_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.notifyDataSetChanged();
                localItems.clear();
                changeStatusOfNotificationsToRead();
                unRevealActivity();
            }
        });
    }

    private void getSavedNotifications() {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String serializedObject = prefs.getString(Notifications.NOTIFICATIONS + User.getPublicKey(), "-");

        if (!serializedObject.equals("-") && serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<NotificationItem>>(){}.getType();
            localItems = gson.fromJson(serializedObject, type);
        }
        showOrHideLabel();
    }

    private void showOrHideLabel() {
        TextView label_no_new_notifications = findViewById(R.id.label_no_new_notifications);
        if (localItems.isEmpty())
            label_no_new_notifications.setVisibility(View.VISIBLE);
        else
            label_no_new_notifications.setVisibility(View.INVISIBLE);
    }

    private void initRecycleview() {
        RecyclerView recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationRecycleViewAdapter(this, localItems);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        changeStatusOfNotificationsToRead();
        unRevealActivity();
    }

    private void changeStatusOfNotificationsToRead() {
        for (int i=0; i<localItems.size(); i++) {
            NotificationItem tmp = localItems.get(i);
            tmp.setNewNotification(false);
        }
        MyPreferences.saveNotifications(this, localItems);
    }


    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                    overridePendingTransition(0, 0);
                }
            });
            circularReveal.start();
        }
    }

    @Override
    public void onItemClick(View view, NotificationItem notificationItem) {
        System.out.println("clicked ");
    }

}