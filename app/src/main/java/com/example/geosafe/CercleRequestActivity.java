package com.example.geosafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.geosafe.Interface.IFirebaseLoadDone;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.RequestViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class CercleRequestActivity extends AppCompatActivity implements IFirebaseLoadDone {
    FirebaseRecyclerAdapter<User, RequestViewHolder> adapter, searchAdapter;
    RecyclerView recycler_all_user;
    IFirebaseLoadDone firebaseLoadDone;
    MaterialSearchBar searchBar;
    List<String> suggestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cercle_request);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Circle invitation requests");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));
        searchBar = (MaterialSearchBar) findViewById(R.id.material_search_bar);
        searchBar.setHint("Type a specific email");
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
                        recycler_all_user.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                Search(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {


            }
        });
        recycler_all_user = (RecyclerView) findViewById(R.id.recycler_all_users);
        recycler_all_user.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));


        firebaseLoadDone = this;
        loadRequestList();
        if (searchAdapter.getItemCount() == 0) {
            Log.wtf("itemcount ", "" + searchAdapter.getItemCount());
            Crouton.makeText(CercleRequestActivity.this, "At the moment there is no request for you!", new Style.Builder()
                    .setBackgroundColorValue(0xffff4444)
                    .build(), (ViewGroup) findViewById(R.id.view)).show();
        }

            loadSearchData();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent aboutIntent = new Intent(CercleRequestActivity.this,HomeActivity.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Search(String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child(Tools.USER_INFORMATION)
                .child(Tools.loggedUser.getUid())
                .child(Tools.CERCLE_REQUEST)
                .orderByChild("email")
                .startAt(s);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView= LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_cercle_request,viewGroup,false);
                return new RequestViewHolder(itemView);
            }


        @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull final User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0, index);
                holder.txt_user_name.setText(new StringBuilder(name));
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.accept_bt.setOnClickListener(v -> {
                    //false means user accepted the request and then displaying a Toast accordingly
                    deleteRequest(model, false);
                    AcceptList(model);

                });
                holder.decline_bt.setOnClickListener(v -> deleteRequest(model, true));


            }
        };

        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
    }

    // add users in suggestion bar
    private void loadSearchData() {
        final List<String> lstUserEmail=new ArrayList<>();
        DatabaseReference userList=FirebaseDatabase.getInstance().getReference().child(Tools.USER_INFORMATION)
                .child(Tools.loggedUser.getUid())
                .child(Tools.CERCLE_REQUEST);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot:dataSnapshot.getChildren())
                {
                    User user=userSnapShot.getValue(User.class);
                    assert user != null;
                    if(!user.equals(Tools.loggedUser))
                    lstUserEmail.add(user.getEmail());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());

            }
        });

    }

    private void loadRequestList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Tools.USER_INFORMATION)
                .child(Tools.loggedUser.getUid())
                .child(Tools.CERCLE_REQUEST);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0,index);
                holder.txt_user_name.setText(new StringBuilder(name));
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.accept_bt.setOnClickListener(v -> {
                    deleteRequest(model, false);
                    AcceptList(model);
                });
                holder.decline_bt.setOnClickListener(v -> deleteRequest(model, true));
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
              View itemView= LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.layout_cercle_request,parent,false);
                return new RequestViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);


            }


    private void AcceptList(User model) {
        DatabaseReference list=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);
        list.child(model.getUid()).setValue(model);

    }

    private void deleteRequest(final User model,final boolean b) {
        DatabaseReference request= FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.CERCLE_REQUEST);

        request.child(model.getUid()).removeValue().addOnSuccessListener(unused -> {
            if (b) {
                Toast.makeText(CercleRequestActivity.this, "Remove user from requests definitely", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CercleRequestActivity.this, "Remove user from requests and add h(er)im to your circle", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null)
            searchAdapter.stopListening();
        super.onStop();
    }
    @Override
    protected void onResume() {
        if (searchAdapter.getItemCount() == 0) {
            Log.wtf("itemcount ", "" + searchAdapter.getItemCount());
            Crouton.makeText(CercleRequestActivity.this, "At the moment there is no request for you!", new Style.Builder()
                    .setBackgroundColorValue(0xffff4444)
                    .build(), (ViewGroup) findViewById(R.id.view)).show();
        }
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null)
            searchAdapter.stopListening();
        super.onResume();
    }


    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {
        searchBar.setLastSuggestions(lstEmail);

    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
