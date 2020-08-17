package my_app.example.my_adventures;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import my_app.example.my_adventures.ui.account.BlockList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my_app.example.my_adventures.ui.comments.PostViewerActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.ui.item.ItemComment;
import my_app.example.my_adventures.ui.item.ItemPost;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int rowLayout;
    private Activity activity;
    public List<Item> mItemList;
    private ViewGroup parent;
    private final RecyclerViewAdapter thisClass = this;
    private ImageManager imageManager;

    public RecyclerViewAdapter(ArrayList<Item> itemList, int rowLayout) {
        mItemList = itemList;
        this.rowLayout = rowLayout;
    }

    public RecyclerViewAdapter(Activity activity, ArrayList<Item> itemList, int rowLayout) {
        mItemList = itemList;
        this.rowLayout = rowLayout;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        imageManager = new ImageManager((Activity) parent.getContext());

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).like_button.setImageBitmap(null);
            ((ItemViewHolder) viewHolder).profileImage.setImageBitmap(null);
            populateItemRows((ItemViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }


    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private PostButtons postButtons;
        public int position;
        public Item item;

        TextView message, nickname, date, likes, comments, postID, userPublicKey, category;
        ImageButton comment_button, like_button, menuButton, shareButton, trashButton;
        CircleImageView profileImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            menuButton = itemView.findViewById(R.id.menu_button);
            postID = itemView.findViewById(R.id.postID);
            userPublicKey = itemView.findViewById(R.id.userPublicKey);
            nickname = itemView.findViewById(R.id.nickname);
            message = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            profileImage = itemView.findViewById(R.id.profile_image);
            like_button = itemView.findViewById(R.id.like_button);
            comment_button = itemView.findViewById(R.id.comment_button);
            category = itemView.findViewById(R.id.category);
            trashButton = itemView.findViewById(R.id.trash_button);
            shareButton = itemView.findViewById(R.id.share_button);

            startOnClickListener();
        }


        private void startOnClickListener() {

            like_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postButtons.setLike();
                }
            });

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postButtons.sharePost();
                }
            });

            trashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postButtons.confirmRemovePost(activity, thisClass, position);
                }
            });

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((item instanceof ItemPost)) {
                        openPost();
                    }
                }
            });

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postButtons.openProfile();
                }
            });

            if (comment_button != null)
                comment_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openPost();
                    }
                });
        }

        private void openPost() {
            Intent intent = new Intent(parent.getContext(), PostViewerActivity.class);
            intent.putExtra("item", item);
            parent.getContext().startActivity(intent);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    private void populateItemRows(ItemViewHolder viewHolder, int position) {
        Item item = mItemList.get(position);
        viewHolder.item = item;

        viewHolder.position = position;
        viewHolder.nickname.setText(item.getNickname());
        viewHolder.likes.setText(String.valueOf(item.getLikes()));
        viewHolder.date.setText(item.getTimestamp());
        viewHolder.postID.setText(String.valueOf(item.getPostId()));
        viewHolder.userPublicKey.setText(String.valueOf(item.getPublicKey()));
        viewHolder.category.setText(item.getCategory());
        viewHolder.postButtons = new PostButtons(parent.getContext(), viewHolder.like_button, viewHolder.likes, item);

        if (viewHolder.comments != null)
            viewHolder.comments.setText(String.valueOf(item.getComments()));

        if (item.getDoILikeThisMsg()) {
            viewHolder.postButtons.setLikeImage(true);
        } else {
            viewHolder.postButtons.setLikeImage(false);
        }

        if (item instanceof ItemComment) {
            viewHolder.message.setText(item.getMessage());
        } else if (item instanceof ItemPost)
            viewHolder.message.setText(item.getMessage());

        if (item.hasProfilePicture() && viewHolder.profileImage != null && !item.getNickname().equals("private")) {
            imageManager.getProfilePicture(item.getPublicKey(), "profile-" + item.getPublicKey(), viewHolder.profileImage);
        } else {
            imageManager.setProfileBlankPicture(parent.getContext(), viewHolder.profileImage);
        }
        checkAndEnableTrashButton(item, viewHolder);
        enableItemMenu(viewHolder);
    }

    private void checkAndEnableTrashButton(Item item, ItemViewHolder viewHolder) {
        if (item.isAuthor()) {
            viewHolder.trashButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isItem_rowLayout() {
        return R.layout.item_row == rowLayout;
    }

    private void enableItemMenu(final ItemViewHolder holder) {

        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(parent.getContext(), holder.menuButton);
                popup.inflate(R.menu.post_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.report_option:
                                reportPost(holder.item, holder.position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });
    }

    private void reportPost(Item item, int position) {
        if (item.isAuthor()) {
            toast.showMsg(parent.getContext(), "You cannot report posts of your own.");
        } else {
            toast.showMsg(parent.getContext(), "Thanks for reporting. You will no longer see this message.");
            if (item instanceof ItemComment) {
                String commentId = ((ItemComment) item).getCommentId();
                BlockList.blockMessage((Activity) parent.getContext(), commentId);
                MainActivity.mDatabase.child("reports").child(item.getCategory()).child("comments").child(item.getPostId()).child(commentId).setValue("t");
            } else {
                BlockList.blockMessage((Activity) parent.getContext(), item.getPostId());
                MainActivity.mDatabase.child("reports").child(item.getCategory()).child("posts").child(item.getPostId()).setValue("t");
            }

            mItemList.remove(position);
            notifyItemRemoved(position);
        }
    }


}