package com.example.geosafe;


import static android.graphics.Color.RED;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.location.LocationRequest;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.example.geosafe.Interface.IFirebaseLoadDone;
import com.example.geosafe.Interface.IRecycleItemClickListener;
import com.example.geosafe.model.Request;
import com.example.geosafe.model.User;
import com.example.geosafe.request.IFCMService;
import com.example.geosafe.utils.Tools;
import com.example.geosafe.viewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geosafe.databinding.ActivityHomeBinding;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {

    private static final String TAG = "";

   /* @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Objects.requireNonNull(getSupportActionBar()).hide();//Ocultar ActivityBar anterior
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        binding.appBarHome.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open, R.string.nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Log.d(TAG,"bla bla éé");
      navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Log.d(TAG,"iddddddd "+id);
                if(id == R.id.add){
                    Log.d(TAG,"bla bla");
                }else if(id == R.id.find){
                    startActivity(new Intent(HomeActivity.this,AllUsersActivity.class));

                }else if(id == R.id.sign_out){

                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item selected
                int id= menuItem.getItemId();
                       if(id == R.id.add){
                           Log.d(TAG,"clicked-------------");

                       }
                return true;
            }});
    }

   @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.add){
Log.d(TAG,"bla bla");
        }else if(id == R.id.find){
            startActivity(new Intent(HomeActivity.this,AllUsersActivity.class));

        }else if(id == R.id.sign_out){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}*/
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
            value= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        this.configureToolBar();

        this.configureDrawerLayout();

        this.configureNavigationView(value);

        //pour le Chat
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(HomeActivity.this, "ChatActivity Comming soon", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this,ChatActivity.class);
                startActivity(i );

            }
        });
        //pour afficher le maps
        FloatingActionButton maps = (FloatingActionButton) findViewById(R.id.map);
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, MapsActivity.class));

            }
        });
        //pour afficher la donation
        FloatingActionButton don = (FloatingActionButton) findViewById(R.id.donate);
        don.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(HomeActivity.this, MapsActivity.class));
                Toast.makeText(HomeActivity.this, "I support ur safety, Donate to support me back!", Toast.LENGTH_SHORT).show();

            }
        });
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_Circle.setLayoutManager(layoutManager);
        recycler_Circle.setItemAnimator(null);
        recycler_Circle.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));
        
        //mettre à jour la localisation
        UpdateLocalisation();
        firebaseLoadDone=this;
        if(Tools.loggedUser==null){ user= FirebaseAuth.getInstance().getCurrentUser();
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
//les suggestions
    private void loadSearch() {
        List<String> lstUserEmail = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapShot : snapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
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
                      holder.txt_user_name.setText(new StringBuilder(name));
                      holder.txt_user_email.setText(new StringBuilder(model.getEmail()));

                    //Event
                    holder.setiRecycleItemClickListener(new IRecycleItemClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position) {
                            Tools.UserTraced=model;
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                            alertDialog.setTitle("Choissisez une option");
                            alertDialog.setMessage("Do you want to Trace or Chat with this user " + model.getEmail());
                            alertDialog.setIcon(R.drawable.ic_account);
                            alertDialog.setNegativeButton("Trace", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(HomeActivity.this,MapsActivity.class));

                                }
                            });
                            alertDialog.setPositiveButton("Chat", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(HomeActivity.this,MessageActivity.class);
                                    i.putExtra("userid",model.getUid());
                                    startActivity(i );
                                }
                            });
                            alertDialog.show();
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
            recycler_Circle.setAdapter(adapter);



    }

    private void UpdateLocalisation() {
        //constructeur RequestLocalisation
        requestlocalisation= com.google.android.gms.location.LocationRequest.create();
        requestlocalisation.setSmallestDisplacement(10f);
        requestlocalisation.setFastestInterval(3000);
        requestlocalisation.setInterval(5000);
        requestlocalisation.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedlocationprovider = LocationServices.getFusedLocationProviderClient(this);
       //verifier les permissions et demande ce qui est pas disponible dans le package manager de cette application celui qui gere tout les information de ce package. et puis appcompact.requestpermissions override automatiquement le onpermissionresult()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedlocationprovider.requestLocationUpdates(requestlocalisation, PendingIntent());

    }
// creer un broadcast receiver pour modifier la localisation depuis le background
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

                holder.setiRecycleItemClickListener(new IRecycleItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        //s tracking
                        Tools.UserTraced=model;
                        startActivity(new Intent(HomeActivity.this,MapsActivity.class));
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

        searchAdapter.startListening();
        recycler_Circle.setAdapter(adapter);

                }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
           // Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        Log.wtf("TAG","navigationlistener");

        switch (id){
            case R.id.add:
                Log.wtf("add",""+id);
                startActivity(new Intent(HomeActivity.this,CercleRequestActivity.class));
                break;
            case R.id.find:
                Log.wtf("find",""+id);

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
        //sets the toolbar as the app bar for the activity
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