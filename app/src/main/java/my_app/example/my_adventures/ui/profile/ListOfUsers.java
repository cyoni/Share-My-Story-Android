package my_app.example.my_adventures.ui.profile;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.R;


public class ListOfUsers extends Fragment implements Recycleview_ListUsers.ItemClickListener {

    private String user_public_key;
    private String section;
    private RecyclerView recyclerView;
    private ArrayList<UserItemObject> users_list = new ArrayList<>();
    private TextView txt_empty_list;
    private Recycleview_ListUsers adapter;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private int limit = 10;
    private String upNext = "";

    public ListOfUsers(String section, String key) {
        this.user_public_key = key;
        this.section = section;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_users_list, container, false);
        initRecycleview(root);
        initScrollListener();
        txt_empty_list = root.findViewById(R.id.txt_empty_list);
        initText();

        return root;
    }

    private void initText() {
        if (users_list.size() == 0)
            executeQuery();
        else
            txt_empty_list.setVisibility(View.INVISIBLE);
    }

    private void initRecycleview(View root) {
        recyclerView = root.findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Recycleview_ListUsers(getContext(), users_list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void executeQuery() {
        isLoading = true;
        MainActivity.mDatabase.child("user_public").child(user_public_key).child(section).orderByKey().limitToFirst(limit).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                process(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });
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

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                System.out.println(isLoading + "," + hasMore);
                if (!isLoading) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() == users_list.size() - 1
                                && hasMore) {
                        executeSecondQuery();
                    }
                }
            }
        });
    }

    private void executeSecondQuery() {
        MainActivity.mDatabase.child("user_public").child(user_public_key).child(section).orderByKey().startAt(upNext).limitToFirst(limit).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                process(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void process(DataSnapshot dataSnapshot) {
        int counter = (int) dataSnapshot.getChildrenCount();

        if (dataSnapshot.exists()) {
            isLoading = false;
            int i = 0;

            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
            DataSnapshot tmp;

            while (iterator.hasNext()) {
                i++;
                tmp = iterator.next();
                if (i == limit){
                    upNext = tmp.getKey();
                }
                else {
                    getNickname(tmp.getKey());
                }
            }

            if (counter < limit) {
                hasMore = false;
            }

            txt_empty_list.setVisibility(View.INVISIBLE);
         }
         else {
            recyclerView.setVisibility(View.INVISIBLE);
            hasMore = false;
        }
    }

    private void getNickname(final String publicKey) {

        MainActivity.mDatabase.child("user_public").child(publicKey).child("profile").child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String nickname = String.valueOf(dataSnapshot.getValue());
                    users_list.add(new UserItemObject(publicKey, nickname));
                    System.out.println(nickname + " is added");
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onItemClick(View view, UserItemObject user) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("user_key", user.getPublicKey());
        startActivity(intent);
    }
}
