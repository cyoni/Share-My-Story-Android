package my_app.example.my_adventures.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;

import my_app.example.my_adventures.Files;
import my_app.example.my_adventures.ImageManager;
import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.toast;
import my_app.example.my_adventures.ui.account.BlockList;
import jp.wasabeef.blurry.Blurry;

public class ProfileActivity extends AppCompatActivity {

    private String user_public_key;
    private TextView nickname, age_gender_country, status;
    private Button follow, edit, report;
    private ShimmerFrameLayout container;
    public ImageView profile_image;
    private ImageView flag_image;
    private boolean fastProfileImageLoad;
    public static final String PROFILE_FOLDER = "Profile";
    private Context context;
    public String imageCode;
    private ImageManager imageManager;
    private static final int CAMERA_REQUEST_CODE = 5;
    private static final int SELECT_PICTURE_CODE = 1;
    private String age, gender, country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = this;
        nickname = findViewById(R.id.nickname);
        age_gender_country = findViewById(R.id.age_gender_country);
        status = findViewById(R.id.status);
        follow = findViewById(R.id.follow);
        edit = findViewById(R.id.edit_profile);
        report = findViewById(R.id.report_button);
        flag_image = findViewById(R.id.image_country_flag);

        TabLayout tabLayout = findViewById(R.id.materialup_tabs);
        ViewPager viewPager = findViewById(R.id.materialup_viewpager);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        container = findViewById(R.id.shimmer_view_container);
        profile_image = findViewById(R.id.profile_image);
        imageManager = new ImageManager(this);

        getIntentData();
        setBackButtonInToolbar();
        setReportButton();
        setEditButton();
        setFollowButton();
        setFollowButtonListener();
        setProfileImage();
        checkIfFollowing();
    }

    public void setBlurryBackground() { // to improve!
        AppBarLayout appBar = findViewById(R.id.appbar);
        ImageView aa = findViewById(R.id.aaa);
        aa.setBackground(profile_image.getBackground());
        imageManager.setImage(PROFILE_FOLDER + "/" + user_public_key, aa);
        BitmapDrawable drawable = (BitmapDrawable) profile_image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Blurry.with(context).from(bitmap).into(aa);
        drawable = (BitmapDrawable) aa.getDrawable();
        bitmap = drawable.getBitmap();
        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
        appBar.setBackground(ob);
    }

    private void setInfoLabel(){
        int comma_counter = 0;
        String str = "";
        if (gender != null && !gender.isEmpty()){
            str = gender;
            comma_counter++;
        }

        if (age != null && !age.isEmpty()){
            str += addComma(comma_counter) + age;
            comma_counter++;
        }

        if (!country.isEmpty()){
            str+= addComma(comma_counter) + " from " + country;
        }

        age_gender_country.setText(str);

    }

    private String addComma(int comma_counter) {
        return (comma_counter > 0) ? ", " : " ";
    }

    private void setProfileImage() {

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.getPublicKey().equals(user_public_key)) {
                    boolean hasProfilePicture = imageManager.isImageOnDevice(PROFILE_FOLDER, user_public_key);
                    //   profilePictureSheetDialog bottomSheet = new profilePictureSheetDialog(hasProfilePicture);
                    //   bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
                    final BottomSheetDialog bs = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                    View bsv = LayoutInflater.from(context).inflate(R.layout.activity_profile_bottom_sheet, (LinearLayout) findViewById(R.id.bottomContainer));
                    bs.setContentView(bsv);

                    if (!hasProfilePicture) {
                        bsv.findViewById(R.id.button_remove_picture).setVisibility(View.GONE);
                    } else {
                        bsv.findViewById(R.id.button_remove_picture)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        removePicture();
                                        bs.cancel();
                                    }
                                });
                    }

                    bsv.findViewById(R.id.picture_locally)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadPictureFromGallery();
                                    bs.cancel();
                                }
                            });

                    bsv.findViewById(R.id.picture_camera)
                            .setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View view) {
                                    takeCameraPicture();
                                    bs.cancel();
                                }
                            });

                    bs.show();
                }
            }
        });
    }

    private void takeCameraPicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void removePicture() {
        toast.showMsg(context, "Removing picture...");
        imageManager.removePictureFromServer(user_public_key).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                imageManager.removePictureFromDevice(user_public_key);
               // imageManager.removePictureFromServer(user_public_key);
                imageManager.removeTimestamp(imageCode);
                profile_image.setImageResource(R.drawable.profile_blank);
                setDefaultBackgroundInAppBar();
                toast.showMsg(context, "Done!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toast.showMsg(context, "There was an error while processing the request.");
            }
        });

    }

    private void setDefaultBackgroundInAppBar() {
        AppBarLayout appBar = findViewById(R.id.appbar);
        appBar.setBackgroundColor(Color.parseColor("#673AB7"));
    }

    private void loadPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap out = imageManager.compressImage(bitmap);
                imageManager.uploadImage(this, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            Bitmap out = imageManager.compressImage(image);
            imageManager.uploadImage(this, out);
        }
    }

    private void setFollowButton() {
        if (user_public_key.equals(User.getPublicKey()) || !User.isSignIn() || user_public_key.equals("private"))
            follow.setVisibility(View.GONE);
        else
            follow.setVisibility(View.VISIBLE);
    }

    private void setReportButton() {
        if (user_public_key.equals(User.getPublicKey())) {
            report.setVisibility(View.INVISIBLE);
        } else {
            report.setVisibility(View.VISIBLE);
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reportUser();
                }
            });
        }
    }

    private void reportUser() {
        new AlertDialog.Builder(this)
                .setTitle("Report & block")
                .setMessage("Are you sure?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        reportUser(user_public_key);
                        BlockList.blockUser((Activity) getApplicationContext(), user_public_key);
                        finish();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    private void reportUser(String user_public_key) {
        MainActivity.mDatabase.child("reports").child("users").child(user_public_key).setValue("t");
        toast.showMsg_long(getApplicationContext(), "Your report was sent successfully. You will no longer see posts by this user.");
    }

    private void setBackButtonInToolbar() {
        Toolbar toolbar = findViewById(R.id.materialup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setEditButton() {
        if (user_public_key.equals(User.getPublicKey())) {
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditProfileDialog();
                }
            });
        } else {
            edit.setVisibility(View.GONE);
        }
    }

    private void showEditProfileDialog() {
        if (container.isActivated()){
            toast.showMsg(this, "Please wait until your profile is set.");
        }
        else {
            EditProfileDialog edit_profile_dialog = new EditProfileDialog(this);
            edit_profile_dialog.show();
            edit_profile_dialog.setValues(age, gender, country, status.getText().toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfileInfo();
    }

    private void checkIfFollowing() {

        MainActivity.mDatabase.child("user_public").child(user_public_key).child("follows")
                .child(User.getPublicKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String following = "Following";
                    follow.setText(following);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setFollowButtonListener() {
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (follow.getText().equals("Following")) {
                    //follow.setBackgroundColor(Color.parseColor("#5595c2"));
                    unFollowUser();
                    follow.setText("Follow");
                } else {
                    // follow.setBackgroundColor(Color.parseColor("#7c3ec2"));
                    follow.setText("Following");
                    followUser();
                }
            }
        });
    }

    private void followUser() { // todo increment follower in db
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("following").child(user_public_key).setValue("t");
        toast.showMsg(getApplicationContext(), "You're now following " + nickname.getText());
    }

    private void unFollowUser() {
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("following").child(user_public_key).removeValue();
        toast.showMsg(getApplicationContext(), "You stopped following " + nickname.getText());
    }

    public void getProfileInfo() {
        if (imageManager.isImageOnDevice(PROFILE_FOLDER, user_public_key))
            profilePictureFastLoad();

        container.showShimmer(true);
        MainActivity.mDatabase.child("user_public").child(user_public_key).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //noinspection unchecked
                    final HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String str_nickname = String.valueOf(dataMap.get("nickname"));
                    String str_status = String.valueOf(dataMap.get("status"));
                    String str_age = String.valueOf(dataMap.get("age"));
                    String str_gender = String.valueOf(dataMap.get("gender"));
                    String str_country = String.valueOf(dataMap.get("country"));
                    //  String following = String.valueOf(dataMap.get("following"));
                    //   String follows = String.valueOf(dataMap.get("follows"));
                    String doesHaveProfileImage = String.valueOf(dataMap.get("profileImage"));
                    if (str_nickname.equals("null")) str_nickname = "No Nickname";


                    nickname.setText(str_nickname);

                    if (doesHaveProfileImage.equals("null")) {
                        int profile = R.drawable.profile_blank;
                        profile_image.setImageResource(profile);
                        imageManager.removeTimestamp(imageCode);
                    } else {
                        initProfilePicture();
                    }

                    if (str_gender.equals("0")) str_gender = "Male";
                    else if (str_gender.equals("1")) str_gender = "Female";
                    else str_gender = "";

                    setProfile(str_age, str_status, str_country, str_gender);

                    container.stopShimmer();
                    container.hideShimmer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initProfilePicture() {
        final String address = imageManager.getAddress(user_public_key);
        if (!fastProfileImageLoad) {
            Task<byte[]> promise = imageManager.getProfilePictureWithReturnListener(user_public_key, imageCode, profile_image);
            if (promise != null) {
                promise.addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        onSuccessDownloadingImage(bytes, PROFILE_FOLDER);
                        imageManager.checkAndUpdateOldPicture(user_public_key, address, imageCode, profile_image);
                        setBlurryBackground();
                    }
                });
            }
        }
    }

    private void onSuccessDownloadingImage(byte[] bytes, String fileName) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        System.out.println("Downloaded from server  " + fileName + " successfully");
        imageManager.saveImageOnDevice(bmp, PROFILE_FOLDER, fileName);
        imageManager.setImageNoPicasso(Files.getPath(context, PROFILE_FOLDER, fileName), profile_image);
    }

    private void profilePictureFastLoad() {
        imageManager.setImageNoPicasso(Files.getPath(this, PROFILE_FOLDER, user_public_key), profile_image);
        setBlurryBackground();
        fastProfileImageLoad = true;
        System.out.println("Fast load of profile picture has been loaded successfully.");
    }

    private void initCountryFlag(String country) {
        final String folder = "Countries";
        country = country.toLowerCase();
        country = country.replace(" ", "-");
        final String fileName = country + ".png";
        final String address = folder + "/" + country + ".png";

        if (imageManager.isImageOnDevice(folder, fileName)) {
            imageManager.setImage(Files.getPath(context, folder, fileName), flag_image);
            flag_image.setVisibility(View.VISIBLE);
        } else {
            imageManager.downloadImage(address).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    imageManager.onSuccessDownloadingImage(bytes, folder, fileName, flag_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        }
    }

    private void getIntentData() {
        user_public_key = getIntent().getStringExtra("user_key");
        if (user_public_key == null) {
            user_public_key = User.getPublicKey();
        }
        imageCode = "profile-" + user_public_key;
    }

    public void setProfile( String age, String status, String country, String gender) {

        this.age = age;
        this.country = country;
        this.gender = gender;
        this.status.setText(status);

        if (country.equals("null")) {
            this.country = "";
            flag_image.setVisibility(View.GONE);
        } else {
            initCountryFlag(country);
        }

        if (status.equals("null")) {
            status = "";
        }

        this.status.setText(status);

        if (gender.equals("null")) this.gender = "";
        if (age.equals("null") || age.equals("Not specified")) this.age = "";

        setInfoLabel();

    }

    public String getUserPublicKey() {
        return user_public_key;
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        private static final int TAB_COUNT = 3;

        TabsAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new UserPosts(user_public_key);
                case 1:
                    return new ListOfUsers("following", user_public_key);
                case 2:
                    return new ListOfUsers("follows", user_public_key);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Posts";
                case 1:
                    return "Following";
                case 2:
                    return "Follows";
            }
            return "";
        }
    }

}
