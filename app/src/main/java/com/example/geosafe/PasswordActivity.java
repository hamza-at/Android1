package com.example.geosafe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.Executor;

public class PasswordActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button btn_change_password;
    EditText et_current_password, et_new_password, et_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepass);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Update password");
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));
        et_current_password = (EditText) findViewById(R.id.et_current_password);
        et_new_password = (EditText) findViewById(R.id.et_new_password);
        et_confirm_password = (EditText) findViewById(R.id.et_confirm_password);
        btn_change_password=(Button)findViewById(R.id.btn_change_password);
        auth = FirebaseAuth.getInstance();

        btn_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent aboutIntent = new Intent(PasswordActivity.this,Profile.class);
                startActivity(aboutIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword() {
        if (!et_current_password.getText().toString().isEmpty() &&
                !et_new_password.getText().toString().isEmpty() &&
                !et_confirm_password.getText().toString().isEmpty()
        ) {
            if (et_new_password.getText().toString().equals(et_confirm_password.getText().toString())) {

                FirebaseUser user = auth.getCurrentUser();
                Log.wtf("user",""+user);
                if (user != null && user.getEmail() != null) {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), et_current_password.getText().toString());
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(et_new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(PasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                                auth.signOut();
                                                startActivity(new Intent(PasswordActivity.this, MainActivity.class));
                                                finish();
                                            }

                                        });
                                    }
                                }
                            });

                }
            }
        }

    }

}
