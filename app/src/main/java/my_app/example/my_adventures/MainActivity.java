package my_app.example.my_adventures;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import my_app.example.my_adventures.notifications.NotificationActivity;
import my_app.example.my_adventures.notifications.Notifications;
import my_app.example.my_adventures.ui.account.AccountActivity;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.ui.account.BlockList;
import my_app.example.my_adventures.ui.contact.contactUsActivity;
import my_app.example.my_adventures.ui.home.Pager_Adapter;
import my_app.example.my_adventures.ui.profile.ProfileActivity;


public class MainActivity extends AppCompatActivity {
    private double APP_VERSION = 1.02;
    private TextView textCartItemCount;
    public static final String CATEGORY = "stories";
    public static FirebaseAuth mAuth;
    public static DatabaseReference mDatabase;
    public static FirebaseFunctions mFunctions;
    private ViewPager pager;
    public Notifications notifications;
    private int POST_CODE = 5;
    public FloatingActionButton floating_button;
    public Snackbar snackbar;
    private CustomDialog dialogHome;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFirebase();
        setFloatingButtonOnClick();
        setToolbar();
        setStatusBar();
        setUpTabs();
        initUser();
        initBlockLists();
        initNotifications();
        checkForInternet();

        initAppVersion();
        showWelcomeMessageIfNeeded();
    }

    private void initAppVersion() {

        MainActivity.mDatabase.child("app").child("version").child("min_version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (APP_VERSION < (long)dataSnapshot.getValue()){
                        CustomDialog customDialog = new CustomDialog(MainActivity.this, R.layout.update_app_dialog);
                        customDialog.show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showWelcomeMessageIfNeeded() {
        String result = MyPreferences.getSharedPreference(this, "user", "welcome");
        if (result.equals(MyPreferences.DOES_NOT_EXIST_CODE)) {
            dialogHome = new CustomDialog(this, R.layout.welcome_dialog);
            dialogHome.show();
        }
    }

    public void closeWelcomeDialog(View view) {
        MyPreferences.setSharedPreference(this, "user", "welcome", "1");
        dialogHome.cancel();
    }


    private void initNotifications() {
        notifications = new Notifications(this);
    }

    private void setStatusBar() {
        getSupportActionBar().setTitle("Share My Story");
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        changeStatusbarColor();
    }

    private void setFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFunctions = FirebaseFunctions.getInstance();
    }

    private void setFloatingButtonOnClick() {
        floating_button = findViewById(R.id.fab);
        floating_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingButtonClick();
            }
        });
    }

    private void setUpTabs() {
        pager = findViewById(R.id.viewpager);
        TabLayout mTabLayout = findViewById(R.id.tablayout);
        PagerAdapter adapter = new Pager_Adapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mTabLayout.getTabCount(), this);
        pager.setAdapter(adapter);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }

    private void checkForInternet() {
        if (!isInternetAvailable()) {
            floating_button.setVisibility(View.INVISIBLE);
            final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
            snackbar = Snackbar
                    .make(coordinatorLayout, "You're not connected to the internet.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("REFRESH", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isInternetAvailable()) {
                                //  Home();
                            }
                        }
                    });
            snackbar.show();
        }
    }

    public boolean isInternetAvailable() { // todo
        return true;
    }

    private void initUser() {
        if (User.isSignIn()) {
            setUser();
        }
    }

    private void floatingButtonClick() {

        System.out.println("Got Nickname" + User.getNickname());
        if (User.isSignIn() && User.getNickname().equals("-")) {
            User.signOut(this);
            Home();
        } else if (User.isSignIn()) {
            Intent intent = new Intent(this, AddPost.class);
            intent.putExtra("category", CATEGORY);
            startActivityForResult(intent, POST_CODE);
        } else {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == POST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                floating_button.setVisibility(View.INVISIBLE);
                showSnackbar();
            }
        }
    }

    private void showSnackbar() {
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        snackbar = Snackbar
                .make(coordinatorLayout, "Your post is live. Refresh to view.", Snackbar.LENGTH_INDEFINITE)

                .setAction("REFRESH", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Home();
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();
    }


    private void changeStatusbarColor() {
        Utils.changeStatusbarColor(this, R.color.colorAccent);
    }

    private void setUnreadNotifications() {
        String count = MyPreferences.getSharedPreference(this, "user", Notifications.UNREAD_NOTIFICATIONS);
        if (!count.equals(MyPreferences.DOES_NOT_EXIST_CODE)) {
            notifications.setUnreadNotifications(Integer.parseInt(count));
            updateBadgeNotification();
        }
    }

    private void initBlockLists() {
        if (User.isSignIn()) {
            SharedPreferences preferences = getSharedPreferences(MyPreferences.USER_FOLDER, Context.MODE_PRIVATE);
            Set<String> stringSet = preferences.getStringSet(BlockList.USERS_BLOCK_LIST + User.getPublicKey(), new HashSet<String>());
            BlockList.initUsersBlockList(stringSet);
            stringSet = preferences.getStringSet(BlockList.MESSAGES_BLOCK_LIST + User.getPublicKey(), new HashSet<String>());
            BlockList.initMessagesBlockList(stringSet);

        }
    }

    public void updateBadgeNotification() {

        if (textCartItemCount != null) {
            if (notifications.getCount() == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {

                MyPreferences.setSharedPreference(this, "user", Notifications.UNREAD_NOTIFICATIONS, notifications.getCount() + "");

                String str;
                if (notifications.getCount() > 99)
                    str = "99+";
                else
                    str = String.valueOf(notifications.getCount());
                textCartItemCount.setText(str);

                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setUser() {
        User.setPublicKey(MyPreferences.getUserPublicKey(this));
        User.setNickname(MyPreferences.getNickname(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_home, menu);
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem menuItem = menu.findItem(R.id.notification_button);
        View actionView = menuItem.getActionView();
        textCartItemCount = actionView.findViewById(R.id.cart_badge);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        setUnreadNotifications();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.login_logout);
        if (User.isSignIn()) {
            item.setTitle("Log out");
        } else {
            item.setTitle("Login");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        View notification_button = findViewById(R.id.menu_button);

        if (id == R.id.notification_button && User.isSignIn()) { //notification button
            openNotificationActivityWithAnimation(notification_button);
            notifications.newNotifications = 0;
            updateBadgeNotification();
            return true;
        } else if (id == R.id.my_profile && User.isSignIn()) {
            openProfile();
            return true;
        } else if (id == R.id.my_profile && !User.isSignIn()) {
            openAccount();
        } else if (id == R.id.contact) {
            openContactUs();
        } else if (id == R.id.login_logout) {
            if (User.isSignIn()) {
                notifications.setUnreadNotifications(0);
                updateBadgeNotification();
                User.signOut(this);
                Home();
            } else
                openAccount();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openNotificationActivityWithAnimation(View view) {
        if (view != null) {

            MyPreferences.setSharedPreference(this, "user", Notifications.UNREAD_NOTIFICATIONS, "0");

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "transition");
            int revealX = (int) (view.getX() + view.getWidth() / 2);
            int revealY = (int) (view.getY() + view.getHeight() / 2);

            Intent intent = new Intent(this, NotificationActivity.class);
            intent.putExtra(NotificationActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
            intent.putExtra(NotificationActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);
            // intent.putExtra("dd", (Parcelable) this);

            ActivityCompat.startActivity(this, intent, options.toBundle());
        }
    }


    public void Home() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void openAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    private void openProfile() {
        if (User.isSignIn()) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void openContactUs() {
        Intent intent = new Intent(this, contactUsActivity.class);
        startActivity(intent);
    }

    public void exitApp(View view) {
        System.exit(0);
    }
}
