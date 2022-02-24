package com.example.geosafe;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geosafe.model.User;
import com.example.geosafe.utils.Tools;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "";
    public DatabaseReference userInfo;
    List<AuthUI.IdpConfig> providers;
    TextView txt;
    FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.wtf("loggeduser",""+auth.getCurrentUser());
            String value = auth.getCurrentUser().getEmail();
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            i.putExtra("email",value);
            startActivity(i);
            finish();
        }
        setContentView(R.layout.activity_main);


        Paper.init(this);
        //initialiser Firebase
        userInfo = FirebaseDatabase.getInstance()
                .getReference(Tools.USER_INFORMATION);

        //initialiser providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
//dexter// request permission de localisation
/*     Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                       //showSignInOptions();
                        Intent signInIntent = AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build();
                        signInLauncher.launch(signInIntent);
                        // [END auth_fui_create_intent]

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this,"you must accept permission",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();*/
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Intent signInIntent = AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(providers)
                                        .build();
                                signInLauncher.launch(signInIntent);
                            } else {
                                // No location access granted.
                                Toast.makeText(MainActivity.this, "you must accept permission", Toast.LENGTH_SHORT).show();

                            }
                        }

                );

// Before the actual permission request, we need to check whether the app
// already has the permissions, and whether it needs to show a permission

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

    }


    private void LaunchAct(FirebaseUser firebaseUser) {
        String value = firebaseUser.getEmail();
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        i.putExtra("email",value);
        startActivity(i);
        finish();

    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            txt=(TextView)findViewById(R.id.txt);
            txt.setText(R.string.Bienvenu);
            txt.setText(R.string.Redirection);

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Log.wtf("firebaseuser",""+firebaseUser);
            Log.wtf("userinfofromrealtime",""+userInfo);
            //user exists?
            assert firebaseUser != null;
            userInfo.orderByKey().equalTo(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.wtf("snapshot", "" + snapshot);

                            if (snapshot.getValue() == null)
                            {  if (!snapshot.child(firebaseUser.getUid()).exists())
                              {
                                Log.wtf("onDataChange", "key uid doesn't exist+add to BD");
                                Tools.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                                userInfo.child(Tools.loggedUser.getUid()).setValue(Tools.loggedUser);
                              }
                            } else {
                                Log.wtf("onDataChange", "user available");
                                Tools.loggedUser = snapshot.child(firebaseUser.getUid()).getValue(User.class);
                            }


                    Paper.book().write(Tools.SAVED_USER_UID,Tools.loggedUser.getUid());

                       updateToken(firebaseUser);
                          Log.d(TAG, "pass user uid to the next activity+launch the activity");
                            LaunchAct(firebaseUser);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.wtf("Error---------------------", "" + error);
                        }
                    });
        }
    }


    private void updateToken(FirebaseUser firebaseUser) {
        Log.d(TAG, "updating Token in progress");
        final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Tools.TOKENS);

        //Get Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> tokens.child(firebaseUser.getUid())
                        .setValue(task.getResult())).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}


