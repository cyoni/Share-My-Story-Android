package my_app.example.my_adventures;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.ui.profile.ProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class ImageManager {
    private final long DO_NOT_CHECK_FOR_UPDATES = -1;
    private long AN_HOUR = 1000 * 60 * 60;

    private final Context context;
    SharedPreferences sharedPref;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public ImageManager(Activity context) {
        this.context = context;
        sharedPref = context.getPreferences(Context.MODE_PRIVATE);
    }

    public Task<byte[]> downloadImage(String address) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child(address);
        final long ONE_MEGABYTE = 1024 * 1024;
        return islandRef.getBytes(ONE_MEGABYTE);
    }


    public Task<StorageMetadata> getMetadata(String address) {
        StorageReference islandRef = storageRef.child(address);
        return islandRef.getMetadata();
    }

    public void saveImageOnDevice(Bitmap bitmapImage, String folder, String fileName) {
        saveImage(bitmapImage, folder, fileName);
    }

    public void saveImage(Bitmap bitmapImage, String folder, String fileName) {
        ContextWrapper cw = new ContextWrapper(context);

        File directory = cw.getDir(folder, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            System.out.println("wrote " + fileName + " successfully");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImage(String address, @NotNull final ImageView image) {
        Picasso.with(context).load(new File(address)).fit().noFade().into(image, new Callback() {

            @Override
            public void onSuccess() { // animation
                image.setAlpha(0f);
                image.animate().setDuration(200).alpha(1f).start();
            }

            @Override
            public void onError() {
            }
        });
    }

    public void setImageNoPicasso(String address, @NotNull final ImageView dest) {
        File imgFile = new File(address);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            dest.setImageBitmap(myBitmap);
        }
    }

    public boolean isImageOnDevice(String folder, String filename) {
        return Files.doesExist(context, folder, filename);
    }

    public void removePictureFromDevice(String user_public_key) {
        removeCache(user_public_key);
        Files.removeFile(ProfileActivity.PROFILE_FOLDER, user_public_key, context);
    }

    private void removeCache(String user_public_key) {
        Picasso.with(context).invalidate(new File(Files.getPath(context, ProfileActivity.PROFILE_FOLDER, user_public_key)));
    }

    public Task<Void> removePictureFromServer(String userKey) {
        return MainActivity.mDatabase.child("user_public").child(userKey).child("profile").child("profileImage").removeValue();
    }

    public DatabaseReference hasProfilePicture(String uid) {
        return MainActivity.mDatabase.child("user_public").child(uid).child("profile").child("profileImage");
    }

    public void setProfileBlankPicture(Context context, CircleImageView dest) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_blank);
        dest.setImageBitmap(Bitmap.createScaledBitmap(icon, 100, 100, false));
    }

    public void uploadImage(final ProfileActivity context, Bitmap bitmap) { // todo: move to another class!!
        byte[] byteArray = convertBitmapToByteArray(bitmap);
        final Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArray));
        final ProgressDialog progressDialog = new ProgressDialog(context);

        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference ref = storageRef.child("tmp/" + User.getPrivateKey());

        UploadTask uploadTask = ref.putBytes(byteArray);

        progressDialog.setMessage("Uploading image");
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                toast.showMsg_long(context, "Your picture was uploaded successfully.");
                onSuccessUploadingImage(context, decoded, progressDialog);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitmap.recycle();
        return stream.toByteArray();
    }

    private void onSuccessUploadingImage(ProfileActivity profileActivity, Bitmap decoded, ProgressDialog progressDialog) {
        removeTimestamp(profileActivity.imageCode);
        removePictureFromDevice(profileActivity.getUserPublicKey());
        saveImage(decoded, ProfileActivity.PROFILE_FOLDER, User.getPublicKey());
        removeTimestamp("profile-" + User.getPublicKey());
        setImageNoPicasso(Files.getPath(profileActivity, ProfileActivity.PROFILE_FOLDER, User.getPublicKey()), profileActivity.profile_image);
        progressDialog.dismiss();
        profileActivity.setBlurryBackground();
        updateTimestampOfProfilePictureLocally("profile-" + User.getPublicKey() , DO_NOT_CHECK_FOR_UPDATES);
    }

    public Bitmap compressImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, 100, 100, true);
    }

    private long getTimestampOfProfilePicture(String imageCode) {
        String record_time = MyPreferences.getSharedPreference((Activity) context, ProfileActivity.PROFILE_FOLDER, imageCode);
        if (record_time.equals("-")) return 0;
        else return Long.parseLong(record_time);
       // return sharedPref.getLong(imageCode, 0);
    }

    private void updateTimestampOfProfilePictureLocally(String imageCode, long updatedTimeMillis) {
      /*  SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(imageCode, updatedTimeMillis);
        editor.apply();*/
      MyPreferences.setSharedPreference((Activity) context, ProfileActivity.PROFILE_FOLDER, imageCode, updatedTimeMillis+"");
    }

    public void removeTimestamp(String imageCode) {
        MyPreferences.remove(ProfileActivity.PROFILE_FOLDER, imageCode, (Activity) context);
      //  sharedPref.edit().remove(imageCode).apply();
    }

    public void getProfilePicture(final String user_public_key, final String imageCode, final ImageView dest) {
        final String address = getAddress(user_public_key);
        if (isImageOnDevice(ProfileActivity.PROFILE_FOLDER, user_public_key)) {
            setImage(Files.getPath(context, ProfileActivity.PROFILE_FOLDER, user_public_key), dest);
            checkAndUpdateOldPicture(user_public_key, address, imageCode, dest);
        } else {
            System.out.println("Started downloading..." + address);
            downloadImage(address).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    System.out.println("success downloading");
                    onSuccessDownloadingImage(bytes, ProfileActivity.PROFILE_FOLDER, user_public_key, dest);
                    checkAndUpdateOldPicture(user_public_key, address, imageCode, dest);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    System.out.println("errr");
                    toast.showMsg(context, "Could not download the picture.");
                }
            });
        }
    }

    public Task<byte[]> getProfilePictureWithReturnListener(final String user_public_key, final String imageCode, final ImageView dest) {
        Task<byte[]> promise = null;
        final String address = getAddress(user_public_key);
        if (isImageOnDevice(ProfileActivity.PROFILE_FOLDER, user_public_key)) {
            setImage(Files.getPath(context, ProfileActivity.PROFILE_FOLDER, user_public_key), dest);
            checkAndUpdateOldPicture(user_public_key, address, imageCode, dest);
        } else {
            System.out.println("STARTED downloading..." + address);
            promise = downloadImage(address);
        }
        return promise;
    }

    public void onSuccessDownloadingImage(byte[] bytes, String folder, String fileName, ImageView img) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        System.out.println("Downloaded from server  " + fileName + " successfully");
        saveImageOnDevice(bmp, folder, fileName);
        setImage(Files.getPath(context, folder, fileName), img);
    }

    public String getAddress(String user_key) {
        return "users/" + user_key + "/profile";
    }

    public void checkAndUpdateOldPicture(final String user_public_key, final String address, final String imageCode, final ImageView dest) {
        final int NOT_DEFINE = 0;
        final long local_timestamp = getTimestampOfProfilePicture(imageCode);

      //  if (DO_NOT_CHECK_FOR_UPDATES == -1){
      //      System.out.println("Image is updated. Not checking for updates.");
     //   }

        if (local_timestamp != DO_NOT_CHECK_FOR_UPDATES) {
            long window = local_timestamp + AN_HOUR;
            long now = Time.now();

            if (window > now) // a window of 1 hour before checking again if the picture has changed.
                System.out.println("Checking profile pic again in " + (window - now) + "ms...");
            else {

                getMetadata(address).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {

                        System.out.println(storageMetadata.getUpdatedTimeMillis() + " -> record time");

                        if (local_timestamp == NOT_DEFINE) { // first time getting image, record data
                            updateTimestampOfProfilePictureLocally(imageCode, storageMetadata.getUpdatedTimeMillis());
                            System.out.println("SAVED PREFERENCE FOR IMAGE");
                        } else if (storageMetadata.getUpdatedTimeMillis() != local_timestamp) {
                            removeTimestamp(imageCode);
                            // old file, update it
                            removePictureFromDevice(user_public_key);
                            System.out.println("That's an old file " + storageMetadata.getUpdatedTimeMillis() + "===" + local_timestamp);
                            getProfilePicture(user_public_key, imageCode, dest); // get the new image
                        } else {
                            System.out.println("all is good");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // file does not exist
                        System.out.println("Error downloading picture :: " + address);

                    }
                });
            }
        }
    }

}
