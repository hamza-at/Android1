package com.example.geosafe;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.geosafe.model.User;
import com.example.geosafe.utils.Commun;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
  //////

///
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "";
    DatabaseReference userInfo;
     private static final int Requete = 1337;
    List<AuthUI.IdpConfig> providers;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                    setupUI();
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);
        //initialiser Firebase
        userInfo = FirebaseDatabase.getInstance("https://www.geosafe-7ae63.firebase.io")
                .getReference(Commun.USER_INFORMATION);

        //initialiser providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
//dexter// request permission de localisation
/*     Dexter.withContext(this)
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
                                        .setAvailableProviders(providers)
                                        .build();
                                signInLauncher.launch(signInIntent);
                            } else {
                                // No location access granted.
                                Toast.makeText(MainActivity.this,"you must accept permission",Toast.LENGTH_SHORT).show();

                            }
                        }

                );
// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

    }


    private void setupUI() {
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
        Log.d(TAG,"fucking shitt");
    }
    // [START auth_fui_result]
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        Log.d(TAG,"fucking shitt222222");
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            //check if user exists
            userInfo.orderByKey()
                    .equalTo(Objects.requireNonNull(firebaseUser).getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() == null) //user doesnt exist
                            {
                                Commun.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                                //add to BD
                                userInfo.child(Commun.loggedUser.getUid()).setValue(Commun.loggedUser);

                            } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
                                //user available
                                Commun.loggedUser = snapshot.child(firebaseUser.getUid()).getValue(User.class);
                            }
                            //save uid to storage to update location from background
                            Paper.book().write(Commun.USER_UID_SAVE_KEY,Commun.loggedUser.getUid());
                            updateToken(firebaseUser);
                            setupUI();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }




   /* private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder().setAvailableProviders(providers)
                        .build(),Requete);
    }
    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Requete)
        {
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK)
            {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                //check if user exists
                userInfo.orderByKey()
                        .equalTo(Objects.requireNonNull(firebaseUser).getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() == null) //user doesnt exist
                                {
                                    Commun.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                                    //add to BD
                                    userInfo.child(Commun.loggedUser.getUid()).setValue(Commun.loggedUser);
                                } else {
                                    //user available
                                    Commun.loggedUser = snapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }
                                //save uid to storage to update location from background
                                Paper.book().write(Commun.USER_UID_SAVE_KEY,Commun.loggedUser.getUid());
                                updateToken(firebaseUser);
                                setupUI();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }*/



    private void updateToken(FirebaseUser firebaseUser) {
        Log.d(TAG,"fucking shitt222222");

        DatabaseReference tokens = FirebaseDatabase.getInstance("https://www.geosafe-7ae63.firebase.io").getReference(Commun.TOKENS);
        //getting token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this,"Fetching FCM registration token failed",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    String msg = getString(R.string.msg_token_fmt, token);
                    Log.d(TAG, msg);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });
                }

}

