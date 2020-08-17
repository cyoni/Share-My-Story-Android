package my_app.example.my_adventures.ui.comments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my_app.example.my_adventures.Files;
import my_app.example.my_adventures.ImageManager;
import my_app.example.my_adventures.Communication.MessageFetcher_Comments;
import my_app.example.my_adventures.PostButtons;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.RecyclerViewAdapter;
import my_app.example.my_adventures.Utils;
import my_app.example.my_adventures.ui.account.AccountActivity;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.ui.item.ItemComment;
import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.toast;
import my_app.example.my_adventures.ui.profile.ProfileActivity;
import jp.wasabeef.blurry.Blurry;


public class PostViewerActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private boolean isLoading, mIsImageHidden, disableTextListener = false;
    private ArrayList<Item> container = new ArrayList<>();
    ;
    private EditText comment;
    private String category, postId;
    private Button submit_button;
    private MessageFetcher_Comments messageFetcher;
    private FloatingActionButton profileButton;
    private TextView label_comments;
    private int mMaxScrollSize, commentCount;
    private PostButtons postButtons;
    private Item item;
    private ImageManager imageManager;
  //  private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_viewer);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initItem();
        initFields();
        initAdapter();
        initComments();
        setToolBarTitle();
        setTextListener();
        initScrollListener();
        initMessageDetails();
        startButtonsListener();
       // initPullToUpdate();
        setStatusBarColor();

    }

    private void setStatusBarColor() {
        if (item.getNickname().equals("private"))
            Utils.changeStatusbarColor(this, R.color.purple);
        else
            Utils.changeStatusbarColor(this, R.color.colorAccent);
    }

    private void initFields() {

        ImageButton like_button = findViewById(R.id.like_button);
        TextView likes_label = findViewById(R.id.likes);

//        swipeContainer = findViewById(R.id.swipeContainer);
        label_comments = findViewById(R.id.label_comments);
        submit_button = findViewById(R.id.submit);
        comment = findViewById(R.id.message);
        submit_button.setVisibility(View.INVISIBLE);
        profileButton = findViewById(R.id.profile_button);
        recyclerView = findViewById(R.id.recycle_view_for_post_viewer);
        postButtons = new PostButtons(this, like_button, likes_label, item);
        imageManager = new ImageManager(this);
        postId = item.getPostId();
        category = item.getCategory();
        commentCount = item.getComments();
        messageFetcher = new MessageFetcher_Comments(category, postId);

        if (userHasProfilePicture(item.getPublicKey()))
            setBlurryBackgroundPicture(item.getPublicKey());

        if (item.getDoILikeThisMsg())
            postButtons.setLikeImage(true);

        if (item.isAuthor())
            initTrashButton();
    }

    private void initItem() {
        item = (Item) getIntent().getSerializableExtra("item");
    }

    private void initMessageDetails() {
        TextView message_label = findViewById(R.id.content);
        String str_message = item.getMessage();
        int likes_counter = item.getLikes();
        message_label.setText(str_message);
        postButtons.getLikesLabel().setText(String.valueOf(likes_counter));
    }

    public void decreaseCommentCount() {
        commentCount = Math.max(0, commentCount - 1);
    }

    public int getCommentCount() {
        return commentCount;
    }

    private void setBlurryBackgroundPicture(String userPublicKey) {
        ImageManager imageManager = new ImageManager(this);
        ImageView backdrop = findViewById(R.id.backdrop);
        imageManager.setImageNoPicasso(Files.getPath(this, ProfileActivity.PROFILE_FOLDER, userPublicKey), backdrop);
        BitmapDrawable drawable = (BitmapDrawable) backdrop.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Blurry.with(this).from(bitmap).into(backdrop);
    }

    private boolean userHasProfilePicture(String userPublicKey) {
        return Files.doesExist(this, ProfileActivity.PROFILE_FOLDER, userPublicKey);
    }

    private void setToolBarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(item.getNickname() + " says");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initTrashButton() {
        ImageButton trash_button = findViewById(R.id.trash_button);
        trash_button.setVisibility(View.VISIBLE);
    }

    private void startButtonsListener() {
        ImageButton trash_button = findViewById(R.id.trash_button);
        ImageButton share_button = findViewById(R.id.share_button);
        ImageButton comment_button = findViewById(R.id.comment_button);

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!comment.getText().toString().trim().isEmpty()) {
                    closeKeyboard(view);
                    submitComment();
                }
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postButtons.openProfile();
            }
        });

        postButtons.getLikeButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postButtons.setLike();
            }
        });

        comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("todo"); // todo
            }
        });

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postButtons.sharePost();
            }
        });

        trash_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postButtons.confirmRemovePost(PostViewerActivity.this, null, -1);
            }
        });
    }

    private void closeKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initComments() {
        updateCounterLabel(commentCount);
        if (commentCount == 0) {
            messageFetcher.doNotLoadForNow();
        } else if (commentCount > 0) {
            getComments();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setTextListener() {
        comment.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!User.isSignIn()) {
                    Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
                    startActivity(intent);
                    return;
                }
                if (!disableTextListener) {
                    if (editable.length() > 0) {
                        submit_button.setVisibility(View.VISIBLE);
                    } else
                        submit_button.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(this, container, R.layout.item_comment);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getComments() {
        startLoadingAnimation();
        isLoading = true;

        messageFetcher.fetchMessagesFromServer().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                finishedGettingMessages();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                toast.showMsg(getApplicationContext(), "There was an error while downloading the comments.");
                isLoading = false;
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });

    }

    private void finishedGettingMessages() {

  /*      if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
*/
        if (container.size() > 0) {
            container.remove(container.size() - 1);
            int scrollPosition = container.size();
            recyclerViewAdapter.notifyItemRemoved(scrollPosition);
        }

        int num_of_items_before_adding = container.size();
        int num_of_items_after_adding = messageFetcher.getSizeOfFreshMsgs();

        addNewItemsToContainer();
        recyclerViewAdapter.notifyItemRangeInserted(num_of_items_before_adding, num_of_items_after_adding - num_of_items_before_adding);
        isLoading = false;
    }

    private void addNewItemsToContainer() {
        ArrayList<Item> tmp_items = messageFetcher.getFreshMessages();
        ArrayList<ItemComment> items = new ArrayList<>();
        for (int i = 0; i < tmp_items.size(); i++) {
            ItemComment currentItem = (ItemComment) tmp_items.get(i);
            items.add(currentItem);
        }
        container.addAll(items);
    }

    private void startLoadingAnimation() {
        container.add(null);
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
        isLoading = true;
    }

    public void submitComment() {

        final String comment_txt = comment.getText().toString().trim();
        if (comment_txt.length() == 0) return;

        submit_button.setEnabled(false);
        Map<String, Object> data = new HashMap<>();

        data.put("message", comment_txt);
        data.put("postID", postId);
        data.put("category", category);


        MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {

                        String answer = String.valueOf(task.getResult().getData());
                        System.out.println("commentId:" + answer);

                        if (!answer.equals("failed")) {
                            ItemComment item = new ItemComment(category, User.getPublicKey(), postId, comment_txt, User.getNickname(), "NOW");
                            item.setDoILikeThis(false);
                            item.setIsAuthor(true);
                            item.setCommentId(answer);
                            initProfilePicIfExists(item);
                            item.incrementCommentCounter();
                            disableTextListener = true;
                            onFinish();
                            container.add(0, item);
                            incrementCounterLabel();
                            recyclerViewAdapter.notifyDataSetChanged();
                        } else {
                            notifyUser_error();
                        }
                        return "";
                    }

                    private void initProfilePicIfExists(ItemComment item) {
                        if (imageManager.isImageOnDevice(ProfileActivity.PROFILE_FOLDER, User.getPublicKey()))
                            item.setHasProfilePicture(true);
                    }
                });
    }

    public void onFinish() {
        comment.setText("");
        comment.clearFocus();
        if (comment.getText().length() == 0)
            submit_button.setVisibility(View.INVISIBLE);
        submit_button.setEnabled(true);
        disableTextListener = false;
    }

    private void incrementCounterLabel() {
        commentCount++;
        String str = commentCount + " comments";
        label_comments.setText(str);
    }

    public void updateCounterLabel(int count) {
        count = Math.max(0, count);
        String value;
        commentCount = count;
        if (commentCount == 0)
            value = "No comments";
        else if (commentCount == 1)
            value = "1 comment";
        else
            value = commentCount + " comments";
        label_comments.setText(value);
    }

    private void notifyUser_error() {
        toast.showMsg(this, "Could not post this comment at this moment.");
        submit_button.setEnabled(true);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) { // display or hide floating button while scrolling
        final int PERCENTAGE_TO_SHOW_IMAGE = 20;
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;
                ViewCompat.animate(profileButton).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(profileButton).scaleY(1).scaleX(1).start();
            }
        }
    }

    private void initScrollListener() {
        NestedScrollView nestedSV = findViewById(R.id.scroll);
        if (nestedSV != null) {
            nestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        if (!isLoading && messageFetcher.areThereMore()/* && !swipeContainer.isRefreshing()*/) {
                            initComments();
                        }
                    }
                }
            });
        }
    }

}
