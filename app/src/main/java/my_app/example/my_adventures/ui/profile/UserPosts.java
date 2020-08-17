package my_app.example.my_adventures.ui.profile;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import my_app.example.my_adventures.Communication.MessageFetcher_UsersPosts;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.ui.item.Item;
import my_app.example.my_adventures.RecyclerViewAdapter;


public class UserPosts extends Fragment {
    private String userPublicKey;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<Item> messageContainer;
    private boolean isLoading = true;
    private MessageFetcher_UsersPosts messageFetcher;

    public UserPosts(String key) {
        this.userPublicKey = key;
        this.messageContainer = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_myposts, container, false);
        recyclerView = root.findViewById(R.id.recycleview);
        messageContainer.clear();
        initAdapter();
        initScrollListener();
        messageFetcher = new MessageFetcher_UsersPosts(userPublicKey);

        if (messageContainer.size() == 0)
            startAnimateAndGetData();

        return root;
    }

    private void getData() {
        messageFetcher.fetchMessagesFromServer().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                isLoading = false;
                finishedGettingMsgs();
            }
        });
    }

    private void finishedGettingMsgs() {
        if (messageContainer.size() > 0) {
            messageContainer.remove(messageContainer.size() - 1);
            int scrollPosition = messageContainer.size();
            recyclerViewAdapter.notifyItemRemoved(scrollPosition);
        }
        int before = messageContainer.size();
        int after = messageFetcher.getSizeOfFreshMsgs();

        messageContainer.addAll(messageFetcher.getFreshMessages());

        recyclerViewAdapter.notifyItemRangeInserted(before, after-before);
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(messageContainer, R.layout.item_row);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initScrollListener() {
        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == messageContainer.size() - 3) {
                        System.out.println("@@@@@@" + messageFetcher.getUpNext());
                        if (!messageFetcher.getUpNext().equals(MessageFetcher.DEFAULT_END_OF_LIST)) {
                            startAnimateAndGetData();
                        }
                    }
                }
            }
        });*/

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !isLoading && messageFetcher.areThereMore()) {
                    messageContainer.add(null);
                    isLoading = true;
                    recyclerViewAdapter.notifyItemInserted(messageContainer.size()-1);
                    getData();
                }
            }
        });
    }

    private void startAnimateAndGetData() {
        isLoading = true;
        messageContainer.add(null);
        recyclerViewAdapter.notifyItemInserted(messageContainer.size() - 1);
        getData();
    }
}
