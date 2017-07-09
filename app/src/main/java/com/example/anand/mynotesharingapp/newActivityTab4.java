package com.example.anand.mynotesharingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 12-06-2017.
 */

public class newActivityTab4 extends AppCompatActivity implements View.OnClickListener {


    TextView savebutton,logoutbutton,info;
    EditText name,collegename;
    ImageButton imageButton;
    private Uri mCropImageUri;
    Uri finalimgUri;
    String UserID;
    String Name;
    String CollegeName;

    String filePath;
    UserDetails userinfo;

    DatabaseReference mDatabase;
    StorageReference mStorageRef;
    FirebaseAuth firebaseAuth,mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourthtab);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        final String curruser=user.getDisplayName();
        Uri currphotouri=user.getPhotoUrl();
        final String curruid=user.getUid();

        mStorageRef=FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference("profilepicture");


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();



        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        savebutton=(TextView)findViewById(R.id.savebutton);
        name=(EditText)findViewById(R.id.displayname);
        imageButton=(ImageButton) findViewById(R.id.imageView1);
        collegename=(EditText) findViewById(R.id.collegename);
        info=(TextView)findViewById(R.id.txtinfo4);
        logoutbutton=(TextView)findViewById(R.id.logout);

        Glide.with(getApplicationContext()).load(currphotouri).into(imageButton);
        name.setText(curruser);



        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {



                    UserDetails upload = postSnapshot.getValue(UserDetails.class);

                    if(upload.getUserID().equals(curruid))
                    {
                        CollegeName=upload.getCollegeName();
                        collegename.setText(CollegeName);
                        Log.d("College Name",CollegeName+" is the college name");

                    }


                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Typeface typeFace=Typeface.createFromAsset(getAssets(),"fonts/robotoslabreg.ttf");
        name.setTypeface(typeFace);
        collegename.setTypeface(typeFace);
        info.setTypeface(typeFace);
        savebutton.setTypeface(typeFace);
        logoutbutton.setTypeface(typeFace);



        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_profile);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId==R.id.tab_home)
                {
                    startActivity(new Intent(newActivityTab4.this,newActivityTab1.class));
                }
                if(tabId==R.id.tab_search)
                {
                    startActivity(new Intent(newActivityTab4.this,newActivityTab3.class));
                }
                if(tabId==R.id.tab_addfile)
                {
                    startActivity(new Intent(newActivityTab4.this,newActivityTab2.class));
                }
            }
        });


        savebutton.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        logoutbutton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v==imageButton)
        {
            onSelectImageClick(v);
        }

        if(v==savebutton)
        {
            updateProfile();
            Toast.makeText(newActivityTab4.this, "Changes Saved!", Toast.LENGTH_SHORT).show();
        }
        if(v==logoutbutton)
        {
            signout();
        }
    }


    public void signout()
    {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        startActivity(new Intent(this,MainActivity.class));
    }

    public void updateProfile(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserID=user.getUid();

        CollegeName=collegename.getText().toString();
        Name=name.getText().toString();

        Log.d("CollegeNameis",CollegeName+" is the coll. name");

        uploadJPEG();


        mDatabase= FirebaseDatabase.getInstance().getReference("profilepicture");









    }

    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }


    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{"Manifest.permission.READ_EXTERNAL_STORAGE"}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ((ImageButton) findViewById(R.id.imageView1)).setImageURI(result.getUri());
                finalimgUri=result.getUri();
                Toast.makeText(newActivityTab4.this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(newActivityTab4.this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(newActivityTab4.this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start crop image activity for the given image.
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.CENTER_INSIDE)
                .setFixAspectRatio(true)
                .start(this);
    }


    public void writeNewPost(String uri)
    {

        String Key=mDatabase.child(UserID).push().getKey();
        userinfo=new UserDetails(uri,Name,UserID,CollegeName);
        Map<String,Object> postValues=userinfo.toMap();

        Map<String,Object> childUpdates=new HashMap<>();
        childUpdates.put("/"+UserID,postValues);

        mDatabase.updateChildren(childUpdates);


    }

    private void uploadJPEG() {
        if (finalimgUri != null) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();


            //getting the storage reference
            StorageReference sRef = mStorageRef.child("profilepictures/"+UserID+"." + "jpeg");
            sRef.putFile(finalimgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
                            progressDialog.dismiss();

                            //displaying success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                            //creating the upload object to store uploaded image details
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();



                            if(String.valueOf(downloadUrl)!=null) {


                                writeNewPost(String.valueOf(downloadUrl));

                                filePath=String.valueOf(downloadUrl);

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserID=user.getUid();


                                if (user != null) {
                                    // Name, email address, and profile photo Url
                                    String DN = user.getDisplayName();
                                    Uri photoUrl=null;
                                    photoUrl = user.getPhotoUrl();

                                    if(photoUrl!=null && Name!=null) {
                                        writeNewPost(String.valueOf(downloadUrl));
                                        Log.d("photoUri:", photoUrl.toString());
                                        Log.d("downloadURI",downloadUrl.toString());
                                    }


                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(Name)
                                                .setPhotoUri(Uri.parse(String.valueOf(downloadUrl)))
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("UpdateDone", "User profile updated.");
                                                        }
                                                    }
                                                })

                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("Fail","Failed to update Profile!");
                                                    }
                                                });

                                    }
                                }




                            else Toast.makeText(newActivityTab4.this, "File Path Error!", Toast.LENGTH_LONG).show();


//ok
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });

        }
        else{
            //If No DP IS SELECTED ONLY CHANGE DISPLAYNAME AND COLLEGE NAME
            FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
            String DN = user.getDisplayName();
            Uri photoUrl=null;
            photoUrl = user.getPhotoUrl();

            writeNewPost(String.valueOf(photoUrl));

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(Name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("UpdateDone", "User profile name updated.");
                            }
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail","Failed to update Profile!");
                        }
                    });



        }


    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
