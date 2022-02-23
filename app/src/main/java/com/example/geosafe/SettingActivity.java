package com.example.geosafe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geosafe.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "";
    Button Circle,Account;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));
        Circle=(Button)findViewById(R.id.Circle);
        Account=(Button)findViewById(R.id.Account);

        Circle.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
                                          ref.child(Tools.loggedUser.getUid()).child(Tools.ACCEPTLIST).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void unused) {
                                                  startActivity(new Intent(SettingActivity.this, HomeActivity.class));
                                                finish();
                                              }
                                          });
                                          //a voir
                                                  //startActivity(new Intent(SettingActivity.this, HomeActivity.class));

                                        //  );
                                      }
                                  }
        );
        Account.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          deleteAccount();

                                      }
                                  }
        );


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent aboutIntent = new Intent(SettingActivity.this,Profile.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void deleteAccount() {
        Log.d(TAG, " deleteAccount");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        DatabaseReference request=FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(Tools.loggedUser.getUid());

        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG,"OK! Works fine!");
                    request.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Toast.makeText(SettingActivity.this, "Removed user from RT", Toast.LENGTH_SHORT).show();
                        }
                    });

                    startActivity(new Intent(SettingActivity.this, MainActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"Ocurrio un error durante la eliminación del usuario", e);
            }
        });
    }
}
