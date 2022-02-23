package com.example.geosafe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.geosafe.model.User;
import com.example.geosafe.utils.IProfile;
import com.example.geosafe.utils.ImageListFragment;
import com.example.geosafe.utils.Tools;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    String uid;
    RelativeLayout layout;
    ProgressBar progressBar;
    String model;
    TextView name, email;
    ImageView image_profile;
    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    FirebaseAuth mAuth;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    // doSomeOperations();
                    Intent data = result.getData();
                    imageUri = Objects.requireNonNull(data).getData();
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadImage();
                    }
                   /* InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BitmapFactory.decodeStream(imageStream);
                    image_profile.setImageURI(selectedImage);// To display selected image in image view
                    imageUri=*/
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor("#E02947")));
        name = findViewById(R.id.tv_name);
        email = findViewById(R.id.tv_address);
        image_profile = findViewById(R.id.profile_ig);
        layout = findViewById(R.id.root);

        model = "";
        if (Tools.loggedUser != null) {
            model = Tools.loggedUser.getEmail();
            uid=Tools.loggedUser.getUid();
        } else {
            model = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
            uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        //String model=Tools.loggedUser.getEmail();

        assert model != null;
        int index = model.indexOf('@');
        String nom = model.substring(0, index);
        getSupportActionBar().setTitle(nom);
        name.setText(new StringBuilder(nom));
        email.setText(new StringBuilder(model));
        FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION).child(uid
        ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                        Log.wtf("user",user.getUid());
                        Log.wtf("user",user.getAvatar());


                        if(user.getAvatar().equals("default")){
                            image_profile.setImageResource(R.drawable.ike);

                        }else{
                            Glide.with(getApplicationContext()).load(user.getAvatar()).into(image_profile);
                        }
                    }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference(Tools.PHOTOS);
        //mAvatarImage = findViewById(R.id.image_choose_avatar);
        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, SettingActivity.class));

            }
        });


        findViewById(R.id.pass).setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           startActivity(new Intent(Profile.this, PasswordActivity.class));

                                                       }
                                                   }
        );


        findViewById(R.id.traced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, RemoveFromCircle.class));

            }
        });
        findViewById(R.id.out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                startActivity(new Intent(Profile.this, MainActivity.class));
                finish();
            }
        });
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);


    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
           /* final ProgressDialog progressDialog=new ProgressDialog(getApplicationContext());
            progressDialog.setMessage("Uploading");
            progressDialog.show();*/
        progressBar = new ProgressBar(Profile.this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);


        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + "jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    // complete the rest of your code
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
                                    Log.wtf("model", model+""+uid);
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("avatar", downloadUrl);
                                    reference.child(uid).updateChildren(map);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
           ///
          /*  uploadTask = fileReference.putFile(imageUri);
            try {
                uploadTask.continueWith((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Log.wtf("taskuri", "" + task.getResult());
                            Uri downloadUri = task.getResult();
                            String mUri = downloadUri.toString();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Tools.USER_INFORMATION);
                            Log.wtf("model", model);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("avatar", mUri);
                            reference.child(model).setValue(map);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }catch (Exception e){}
            }else{
                Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }*/

        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            switch (item.getItemId()) {
                // Respond to the action bar's Up/Home button
                case android.R.id.home:
                    //NavUtils.navigateUpFromSameTask(this);
                    Intent aboutIntent = new Intent(Profile.this, HomeActivity.class);
                    startActivity(aboutIntent);
                    finish();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }



