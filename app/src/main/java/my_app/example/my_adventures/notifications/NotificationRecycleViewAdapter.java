package my_app.example.my_adventures.notifications;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import my_app.example.my_adventures.ImageManager;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.Time;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecycleViewAdapter extends RecyclerView.Adapter<NotificationRecycleViewAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<NotificationItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor

    public NotificationRecycleViewAdapter(Context context, ArrayList<NotificationItem> items) {
        this.mData = items;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.profile_picture.setImageBitmap(null);

        String text = mData.get(position).getMessage();
        holder.timestamp.setText(Time.howLongPassed(mData.get(position).getTimestamp()));
        holder.message.setText(Html.fromHtml(text));

        if (mData.get(position).getNewNotification()) {
            holder.newMsg.setVisibility(View.VISIBLE);
        } else {
            holder.newMsg.setVisibility(View.INVISIBLE);
        }

        final String uid = hasPublicId(position);
        final ImageManager imageManager = new ImageManager((Activity) context);
        if (!(uid == null)) {
           // imageManager.getProfilePicture(uid, "profile-" + uid, holder.profile_picture);

            imageManager.hasProfilePicture(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getValue().equals("t")) {
                        imageManager.getProfilePicture(uid, "profile-" +uid, holder.profile_picture);
                    }
                    else{
                        imageManager.setProfileBlankPicture(context, holder.profile_picture);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

        }
        else{
            imageManager.setProfileBlankPicture(context, holder.profile_picture);
        }
    }

    private String hasPublicId(int position) {
        return mData.get(position).getMetadata();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView message, newMsg, timestamp;
        CircleImageView profile_picture;

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            newMsg = itemView.findViewById(R.id.newMsg);
            timestamp = itemView.findViewById(R.id.timestamp);
            profile_picture = itemView.findViewById(R.id.profile_picture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, mData.get(getAdapterPosition()));
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {

        return "not implemented yet";
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, NotificationItem position);
    }
}