package my_app.example.my_adventures;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import my_app.example.my_adventures.ui.account.AccountActivity;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.ui.comments.PostViewerActivity;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.ui.item.ItemComment;
import my_app.example.my_adventures.ui.profile.ProfileActivity;
import my_app.example.my_adventures.ui.share.Share;

public class PostButtons {

    private final Context context;
    private ImageButton like_button;
    private Item item;
    private TextView likes_label;

    public PostButtons(Context context, ImageButton like_button, TextView likes_label, Item item){
        this.context = context;
        this.like_button = like_button;
        this.likes_label = likes_label;
        this.item = item;
    }

    public void setLike(){
        if (!User.isSignIn()) {
            Intent intent = new Intent(context, AccountActivity.class);
            context.startActivity(intent);
            return;
        }

        int increment = 1;
        if (like_button.getTag() != null && like_button.getTag().equals("pressed")) {
            setLikeImage(false);
            item.decrementLike();
            increment = -1;
        } else {
            item.increaseLikes();
            setLikeImage(true);
            like_button.setTag("pressed");
        }

        String change;
        if (item.getDoILikeThisMsg()) {
            change = null;
            item.setDoILikeThis(false);
        }
        else {
            item.setDoILikeThis(true);
            change = "t";
        }

        if (item instanceof ItemComment){
            System.out.println("comment like");
            String commentId = ((ItemComment) item).getCommentId();
            MainActivity.mDatabase.child(item.getCategory()).child(item.getPostId()).child("comments").child(commentId).child("likes").child(User.getPublicKey()).setValue(change);
        }
        else {
            System.out.println("info: " + item.getPostId() + "CAT" + item.getCategory());
            MainActivity.mDatabase.child(item.getCategory()).child(item.getPostId()).child("likes").child(User.getPublicKey()).setValue(change);
        }

        int likes_num = Integer.parseInt(likes_label.getText().toString());
        String num = likes_num + increment + "";
        likes_label.setText(num);
    }

    public void setLikeImage(boolean state) {

        int like_not_pressed = R.drawable.ic_like;
        int like_pressed = R.drawable.ic_like_pressed;

        if (state) {
            like_button.setTag("pressed");
            like_button.setImageResource(like_pressed);
        } else {
            like_button.setImageResource(like_not_pressed);
            like_button.setTag("unpressed");
        }
    }

    public void sharePost() {
        String message = item.getNickname() + " from ShareMyStory says: " + item.getMessage();
        Share.share("Subject", message, (Activity) context);
    }

    public void confirmRemovePost(final Activity activity, final RecyclerViewAdapter father, final int position) {

        new AlertDialog.Builder(context)
                .setTitle("Remove post")
                .setMessage("Are you sure to delete this post?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeMsg();
                        if (position != -1) {
                            father.mItemList.remove(position);
                           // father.notifyItemRemoved(position);
                            father.notifyDataSetChanged();
                            if (activity instanceof PostViewerActivity){
                                int now =  ((PostViewerActivity) activity).getCommentCount();
                                ((PostViewerActivity) activity).updateCounterLabel(now-1);
                            }

                        }
                        else{
                            activity.finish();
                        }
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    private void removeMsg() {
        String deleteMe = "delete-me";
        if (item instanceof ItemComment){ // comment
            MainActivity.mDatabase.child(item.getCategory()).child(item.getPostId()).child("comments").child(((ItemComment) item).getCommentId()).child("status").setValue(deleteMe);
        }
        else { // post
            MainActivity.mDatabase.child(item.getCategory()).child(item.getPostId()).child("status").setValue(deleteMe);
        }
        item.decrementComments();
    }

    public void openProfile() {
        if (item.getNickname().equals("private")) {
            toast.showMsg(context, "Profile does not exist.");
        }
        else{
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("user_key", item.getPublicKey());
            context.startActivity(intent);
        }
    }

    public TextView getLikesLabel() {
        return likes_label;
    }

    public ImageButton getLikeButton() {
        return like_button;
    }
}
