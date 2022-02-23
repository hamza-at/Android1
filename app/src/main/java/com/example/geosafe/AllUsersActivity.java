package com.example.geosafe;

import static android.graphics.Color.RED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.geosafe.Interface.IFirebaseLoadDone;
import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.model.MyResponse;
import com.example.geosafe.model.Request;
import com.example.geosafe.model.User;
import com.example.geosafe.request.IFCMService;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AllUsersActivity extends AppCompatActivity implements IFirebaseLoadDone {
    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_all_user;
    IFirebaseLoadDone firebaseLoadDone;
    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MaterialSearchBar searchBar;
    List<String> suggestList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Send request");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));

        //initialiser API
        ifcmService = Tools.getFCMService();
        //Initilialser the view
        searchBar = (MaterialSearchBar) findViewById(R.id.material_search_bar);
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
        recycler_all_user = (RecyclerView) findViewById(R.id.recycler_all_users);
        recycler_all_user.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        firebaseLoadDone = this;
        loadUserList();
        loadSearchData();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent aboutIntent = new Intent(AllUsersActivity.this,HomeActivity.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
// kat3mr suggestion list bles users
    private void loadSearchData() {
        List<String> lstUserEmail = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    lstUserEmail.add(user.getEmail());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                firebaseLoadDone.onFirebaseLoadFailed(error.getMessage());
            }
        });
    }

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Tools.USER_INFORMATION);
       // It is a class provide by the FirebaseUI to make query in the database to fetch appropriate data
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        //passer l'objet au adapter
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0,index);
                if (model.getEmail().equals(Tools.loggedUser.getEmail())) {
                    holder.txt_user_name.setText(new StringBuilder(name));


                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append("(me)"));
                    holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                } else {
                    holder.txt_user_name.setText(new StringBuilder(name));

                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                }
                //Event
                holder.setiRecycleItemClickListener(new IRecycleItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        if(Tools.loggedUser.getEmail().equals(model.getEmail())){
                            Snackbar.make(view, "     That's you!!", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(RED)
                                    .setAction("Alert", null).show();
                        }else{
                            showDialogRequest(model);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        // this ligne is important to avoid blank in load user
        adapter.startListening();
        recycler_all_user.setAdapter(adapter);


    }

    private void showDialogRequest(User model) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Request Friend");
        alertDialog.setMessage("Do you want to sent request friend to " + model.getEmail());
        alertDialog.setIcon(R.drawable.ic_account);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on lajoute a la liste des acceptes
               /* DatabaseReference acceptlist = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION)
                        .child(Tools.loggedUsed.getUid())
                        .child(Tools.ACCEPTLIST);
                acceptlist.orderByKey().equalTo(model.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() == null)
                                    sendCircleRequest(model);
                                else
                                    Toast.makeText(AllUsersActivity.this, model.getEmail() + " is on your circleSafe", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });*/
                sendCircleRequest(model);
            }
        });
        alertDialog.show();
    }

    private void sendCircleRequest(User model) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Tools.TOKENS);

        tokens.orderByKey().equalTo(model.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null)
                            Toast.makeText(AllUsersActivity.this, "Token error", Toast.LENGTH_LONG).show();
                        else {
                            Request request = new Request();
                            request.setTo(snapshot.child(model.getUid()).getValue(String.class));
                            Map<String, String> data = new HashMap<>();
                            data.put(Tools.Transmitter_NAME, Tools.loggedUser.getEmail());
                            data.put(Tools.Transmitter_UID,Tools.loggedUser.getUid());
                            data.put(Tools.RECEIVER_NAME, model.getEmail());
                            data.put(Tools.RECEIVER_UID, model.getUid());

                            request.setData(data);
                            Log.wtf("request---------------------------------", "" + request.toString());
                            //envoyer
                           compositeDisposable.add(ifcmService.sendFriendRequestToUser(request).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MyResponse>() {
                                        @Override
                                        public void accept(MyResponse myResponse) throws Exception {
                                            Log.d("request_response",myResponse.toString());
                                            if (myResponse.success == 1)
                                                Toast.makeText(AllUsersActivity.this, "Request sent", Toast.LENGTH_SHORT).show();

                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(AllUsersActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }));


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        if (searchAdapter != null) {
            searchAdapter.stopListening();
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
        if (searchAdapter != null) {
            searchAdapter.startListening();
        }
    }

    private void startSearch(String text_search) {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION).orderByChild("email")
                .startAt(text_search);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                int index = model.getEmail().indexOf('@');
                String name = model.getEmail().substring(0,index);
                if (model.getEmail().equals(Tools.loggedUser.getEmail())) {
                    holder.txt_user_name.setText(new StringBuilder(name));
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append("(me)"));
                    holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                } else {
                    holder.txt_user_name.setText(new StringBuilder(name));
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                }
                //Event
                holder.setiRecycleItemClickListener(new IRecycleItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        if(Tools.loggedUser.getEmail().equals(model.getEmail())){
                            Snackbar.make(view, "     That's you!!", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(RED)
                                    .setAction("Alert", null).show();
                        }else{
                            showDialogRequest(model);
                        }
                    }

                });
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
        // this ligne is important to avoid blank in load user
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
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