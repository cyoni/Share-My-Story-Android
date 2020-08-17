package my_app.example.my_adventures.ui.profile;

import android.app.Activity;
import android.content.Context;
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
import de.hdodenhof.circleimageview.CircleImageView;
import my_app.example.my_adventures.R;

public class Recycleview_ListUsers extends RecyclerView.Adapter<Recycleview_ListUsers.ViewHolder> {

    private final Context context;
    private ArrayList<UserItemObject> mData;
    private LayoutInflater mInflater;
    private Recycleview_ListUsers.ItemClickListener mClickListener;

    // data is passed into the constructor

    public Recycleview_ListUsers(Context context, ArrayList<UserItemObject> items) {
        this.mData = items;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.listview_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.profile_picture.setImageBitmap(null);

        String nickname = mData.get(position).getNickname();
        final String uid = mData.get(position).getPublicKey();

        holder.nickname.setText(nickname);

        final ImageManager imageManager = new ImageManager((Activity) context);

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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nickname;
        CircleImageView profile_picture;

        ViewHolder(View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.nickname);
            profile_picture = itemView.findViewById(R.id.profile_picture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, mData.get(getAdapterPosition()));
        }
    }

    String getItem(int id) {
        return "not implemented yet";
    }

    // allows clicks events to be caught
    void setClickListener(Recycleview_ListUsers.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, UserItemObject position);
    }
}