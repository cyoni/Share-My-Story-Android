package my_app.example.my_adventures.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.agrawalsuneet.dotsloader.loaders.LazyLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import my_app.example.my_adventures.Communication.MessageFetcher;
import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.Communication.MessageFetcher_Posts;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.RecyclerViewAdapter;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.toast;
import my_app.example.my_adventures.ui.item.ItemPost;

public class Home extends Fragment {

    private MainActivity mainActivity;
    private boolean topPosts;
    private String str_category;
    private ArrayList<Item> container;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private boolean isLoading = false;
    private LazyLoader loader;
    private SwipeRefreshLayout swipeContainer;
    private MessageFetcher_Posts messageFetcher;
    public static int ADD_POST = 1010;
    public static boolean dontLoadMsgs = false;

    public Home(String category, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.str_category = category;
        if (category.equals(MainActivity.CATEGORY)) {
            container = new ArrayList<>();
        }
        messageFetcher = new MessageFetcher_Posts(category);
    }

    public Home(String category, boolean topPosts, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.str_category = category;
        this.topPosts = topPosts;
        messageFetcher = new MessageFetcher_Posts(category);
        messageFetcher.addMetadataToRequest("topPosts", "true");
        container = new ArrayList<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_fragment, container, false);

        loader = root.findViewById(R.id.progressBar);
        recyclerView = root.findViewById(R.id.recycleview);
        swipeContainer = root.findViewById(R.id.swipeContainer);
        initAdapter();
        initScrollListener();

        recyclerViewAdapter.notifyDataSetChanged();
        initPullToUpdate();

        return root;
    }


    private void initPullToUpdate() {

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (User.isSignIn())
            checkUserValidation();

        if (User.isSignIn() && !dontLoadMsgs || container.isEmpty() && User.isSignIn() && !dontLoadMsgs) { // with listening
            hideLoaderAndRecycleview();
            mainActivity.notifications.startListeningForNotificationsAndSaveThemLocally();
            setStopRefreshingOnAndGetMsgs();
        } else if (!User.isSignIn() && !dontLoadMsgs || container.isEmpty()) { // without listening
            hideLoaderAndRecycleview();
            fetchTimelineAsync();
            dontLoadMsgs = true;
        }
    }

    private void checkUserValidation() {
        if (User.getPublicKey().isEmpty() || User.getNickname().equals("-") || User.getNickname() == null) {
            User.signOut(getActivity());
            mainActivity.Home();
            toast.showMsg_long(getContext(), "Please login again.");
        }
    }

    private void setStopRefreshingOnAndGetMsgs() {
        dontLoadMsgs = true;
        fetchTimelineAsync();
    }

    private void hideLoaderAndRecycleview() {
        loader.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_POST) {
            if (resultCode == Activity.RESULT_OK) {
                ItemPost tmp = (ItemPost) data.getSerializableExtra("item");
                container.add(0, tmp);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    private void fetchTimelineAsync() {
        System.out.println("refreshing....");
        container.clear();
        messageFetcher.reset();
        recyclerViewAdapter.notifyDataSetChanged();
        getMessagesFromServer();
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(container, R.layout.item_row);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // show_or_hide_floating_button(dy);
                hideSnackBarIfItsOnDisplay();
                if (floatingButtonIsHidden())
                    showFloatingButton();

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading && !swipeContainer.isRefreshing()) {

                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == container.size() - 1) {
                        if (messageFetcher.getUpNext().equals(MessageFetcher.DEFAULT_END_OF_LIST)) {
                         //   toast.showMsg(getContext(), "You've reached the bottom.");
                        } else {
                            System.out.println("is loading? " + isLoading + "," + swipeContainer.isRefreshing() + ", endoflist:" + messageFetcher.getUpNext());

                            container.add(null);

                            recyclerView.post(new Runnable() {
                                public void run() {
                                    // There is no need to use notifyDataSetChanged()
                                    recyclerViewAdapter.notifyItemInserted(container.size() - 1);
                                }
                            });

                            getMessagesFromServer();
                        }
                    }
                }
            }
        });
    }

    private boolean floatingButtonIsHidden() {
        return  (mainActivity.floating_button.getVisibility() == View.INVISIBLE);
    }

    private void showFloatingButton() {
        mainActivity.floating_button.setVisibility(View.VISIBLE);
    }

    private void hideSnackBarIfItsOnDisplay() {
        if (mainActivity.snackbar != null && mainActivity.snackbar.isShown()) {
            mainActivity.snackbar.dismiss();
        }
    }

   // private void show_or_hide_floating_button(int dy) {
/*        if (dy > 0 && mainActivity.floating_button.getVisibility() == View.VISIBLE) {
            mainActivity.floating_button.hide();
        } else if (dy < 0 && mainActivity.floating_button.getVisibility() != View.VISIBLE) {
            mainActivity.floating_button.show();
        }*/
    //}

    private void getMessagesFromServer() {

        setIsLoading(true);
        System.out.println("getting....");
        messageFetcher.fetchMessagesFromServer().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                finishedGettingMsgs();
            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                errorLoadingMessages();
            }
        });


        System.out.println("starting from " + messageFetcher.getUpNext());
        System.out.println(str_category + " category is");

    }

    private void errorLoadingMessages() {
        setIsLoading(false);
        toast.showMsg_long(getContext(), "There was an error while downloading the posts.");
        removeLastElement();
        loader.setVisibility(View.INVISIBLE);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void setIsLoading(boolean bool) {
        this.isLoading = bool;
    }

    private void finishedGettingMsgs() {

        removeLastElement();
        int sizeBefore = container.size();
        container.addAll(messageFetcher.getFreshMessages());
        recyclerViewAdapter.notifyItemRangeInserted(sizeBefore, container.size() - 1);
        setIsLoading(false);
        if (swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }

        if (recyclerView.getVisibility() == View.INVISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            loader.setVisibility(View.INVISIBLE);
        }
    }

    private void removeLastElement() {
        if (container.size() > 0) {
            container.remove(container.size() - 1);
            int scrollPosition = container.size();
            recyclerViewAdapter.notifyItemRemoved(scrollPosition);
        }
    }
}