package com.example.geosafe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class RemoveFromCircle extends AppCompatActivity {
    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_all_user;
    MaterialSearchBar searchBar;
    List<String> suggestList = new ArrayList<>();
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_from_circle);
        if(Tools.loggedUser==null){
            user= FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            Tools.loggedUser=new User(user.getUid(), user.getEmail());}

        searchBar = (MaterialSearchBar) findViewById(R.id.material_search);
        searchBar.setCardViewElevation(10);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<>();
                for (String search : suggestList) {
                    if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    if (adapter != null) {
                        //if close search restore default
                        recycler_all_user.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {


            }
        });
        recycler_all_user = (RecyclerView) findViewById(R.id.recycler_circle_remove);
        recycler_all_user.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        loadUserList();

    }

    private void startSearch(String text_search) {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST).orderByChild("email")
                .startAt(text_search);



        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0, index);
                holder.txt_user_name.setText(new StringBuilder(name));
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));


                holder.setiRecycleItemClickListener((view, position1) -> showDialogRequest(model));
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                //pour passer declarer a la classe le layout ou les donnees will be displayed
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
    }

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0, index);
                holder.txt_user_name.setText(new StringBuilder(name));
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));

                holder.setiRecycleItemClickListener((view, position1) -> showDialogRequest(model));
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_all_user.setAdapter(adapter);


    }
    private void showDialogRequest(User model) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Remove Friend");
        alertDialog.setMessage("Do you want to remove this user from your circle " + model.getEmail());
        alertDialog.setIcon(R.drawable.ic_account);
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        alertDialog.setPositiveButton("Confirm", (dialog, which) -> RemoveCircle(model));
        alertDialog.show();
    }

    private void RemoveCircle(User model) {
        DatabaseReference request = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);

        request.child(model.getUid()).removeValue().addOnSuccessListener(unused -> Toast.makeText(RemoveFromCircle.this, "Remove", Toast.LENGTH_SHORT).show());
    }
}

