package com.example.geosafe;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.Interface.IFirebaseLoadDone;
import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {


   FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_Circle;
    MaterialSearchBar searchBar;
    List<String> suggestList = new ArrayList<>();
    IFirebaseLoadDone firebaseLoadDone;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    LocationRequest requestlocalisation;
    FusedLocationProviderClient fusedlocationprovider;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle extras = getIntent().getExtras();
        String value="";
        if (extras != null) {
            value = extras.getString("email");
            Log.wtf("tag",value);
        }else{
            value= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        }
        this.configureToolBar();

        this.configureDrawerLayout();

        this.configureNavigationView(value);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Toast.makeText(HomeActivity.this, "Displaying your chat history", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(HomeActivity.this,ChatActivity.class);
            startActivity(i );

        });

        FloatingActionButton maps = (FloatingActionButton) findViewById(R.id.map);
        maps.setOnClickListener(view -> {
            Toast.makeText(HomeActivity.this, "Displaying the maps with your location", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(HomeActivity.this, MapsActivity.class));

        });

        FloatingActionButton don = (FloatingActionButton) findViewById(R.id.donate);
        don.setOnClickListener(view -> Toast.makeText(HomeActivity.this, "I support ur safety, Donate to support me back! (Coming soon)", Toast.LENGTH_SHORT).show());

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
                        recycler_Circle.setAdapter(adapter);
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
        recycler_Circle = (RecyclerView) findViewById(R.id.recycler_Circle);
        recycler_Circle.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_Circle.setLayoutManager(layoutManager);
        recycler_Circle.setItemAnimator(null);
        recycler_Circle.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        
        //mettre à jour la localisation
        UpdateLocalisation();
        firebaseLoadDone=this;
        if(Tools.loggedUser==null){ user= FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            Tools.loggedUser=new User(user.getUid(), user.getEmail());}

        loadCercleList();

        loadSearch();


    }

    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        if (searchAdapter != null) {
            searchAdapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.getItemCount() == 0) {
            Log.wtf("itemcount ",""+adapter.getItemCount());
            Crouton.makeText(HomeActivity.this, "If your circle is empty, You may need to add some users!", new Style.Builder()
                    .setBackgroundColorValue(0xffff4444)
                    .build(),(ViewGroup) findViewById(R.id.view)).show();


        }
        if (adapter != null) {
            adapter.startListening();
        }
        if (searchAdapter != null) {
            searchAdapter.startListening();
        }
    }

    private void loadSearch() {
        List<String> lstUserEmail = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    assert user != null;
                    if(user.getEmail() !=null) {
                        lstUserEmail.add(user.getEmail());
                    }
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                firebaseLoadDone.onFirebaseLoadFailed(error.getMessage());
            }
        });
        if (adapter.getItemCount() == 0) {
            Log.wtf("itemcount ",""+adapter.getItemCount());
            Crouton.makeText(HomeActivity.this, " If your circle is empty, You may need to add some users!",new Style.Builder()
                    .setBackgroundColorValue(0xffff4444)
                    .build(),(ViewGroup) findViewById(R.id.view)).show();


        }
    }

    private void loadCercleList() {
            Query query = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                    .setQuery(query, User.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                    int index = model.getEmail().indexOf('@');
                    String name = model.getEmail().substring(0,index);
                      holder.txt_user_name.setText(new StringBuilder(name));
                      holder.txt_user_email.setText(new StringBuilder(model.getEmail()));

                    holder.setiRecycleItemClickListener((view, position1) -> {
                        Tools.UserTraced=model;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                        alertDialog.setTitle("Choissisez une option");
                        alertDialog.setMessage("Do you want to Trace or Chat with this user " + model.getEmail());
                        alertDialog.setIcon(R.drawable.ic_account);
                        alertDialog.setNegativeButton("Trace", (dialog, which) -> startActivity(new Intent(HomeActivity.this,MapsActivity.class)));
                        alertDialog.setPositiveButton("Chat", (dialog, which) -> {
                            Intent i = new Intent(HomeActivity.this,MessageActivity.class);
                            i.putExtra("userid",model.getUid());
                            startActivity(i);
                        });
                        alertDialog.show();
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
            adapter.startListening();
            recycler_Circle.setAdapter(adapter);
    }

    private void UpdateLocalisation() {
    Log.wtf("Updating localisation realtime","In progress");
        requestlocalisation= com.google.android.gms.location.LocationRequest.create();
        requestlocalisation.setSmallestDisplacement(10f);
        requestlocalisation.setFastestInterval(3000);
        requestlocalisation.setInterval(5000);
        requestlocalisation.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedlocationprovider = LocationServices.getFusedLocationProviderClient(this);
        //verify permissions and ask user to allow them if necessary
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedlocationprovider.requestLocationUpdates(requestlocalisation, PendingIntent());

    }
    //broadcast receiver to update location from background
    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent PendingIntent() {
        Intent intent=new Intent(HomeActivity.this, com.example.geosafe.IFCService.LocalisationReceiver.class);
        intent.setAction(com.example.geosafe.IFCService.LocalisationReceiver.ACTION);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void Search(String s) {
        Query query=FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION)
                .child(Tools.loggedUser.getUid())
                .child(Tools.ACCEPTLIST)
                .orderByChild("email")
                .startAt(s);
        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull final User model) {
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));

                holder.setiRecycleItemClickListener((view, position1) -> {
                    //displaying realtime location of user model
                    Tools.UserTraced=model;
                    startActivity(new Intent(HomeActivity.this,MapsActivity.class));
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

        searchAdapter.startListening();
        recycler_Circle.setAdapter(adapter);

                }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.add:
                startActivity(new Intent(HomeActivity.this,CercleRequestActivity.class));
                break;
            case R.id.find:
                startActivity(new Intent(HomeActivity.this,AllUsersActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(HomeActivity.this,Profile.class));
                 finish();
                break;
            case R.id.donate:
                Toast.makeText(HomeActivity.this, "I support ur safety, Donate to support me back!", Toast.LENGTH_SHORT).show();
                break;
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }



    private void configureToolBar(){
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My circle");
        setSupportActionBar(toolbar);
    }

    private void configureDrawerLayout(){
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(String string){
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUseremail = (TextView) headerView.findViewById(R.id.text2);
        navUseremail.setText(string);
        int index = string.indexOf('@');
        String name = string.substring(0,index);
        TextView navUsername = (TextView) headerView.findViewById(R.id.text1);
        navUsername.setText(name);

        navigationView.setNavigationItemSelectedListener(this);
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