package com.example.anand.mynotesharingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 12-06-2017.
 */

public class ProfileSetup extends AppCompatActivity implements View.OnClickListener {


    Button B1;
    EditText name;
    ImageButton imageButton;
    private Uri mCropImageUri;
    Uri finalimgUri;
    String UserID;
    String Name;
    String CollegeName="";

   String filePath;
    UserDetails userinfo;

    DatabaseReference mDatabase;
    StorageReference mStorageRef;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);


        mStorageRef=FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference("profilepicture");

        B1=(Button)findViewById(R.id.profilebutton);
        name=(EditText)findViewById(R.id.name);
        imageButton=(ImageButton) findViewById(R.id.imageView1);

        B1.setOnClickListener(this);
        imageButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v==imageButton)
        {
            onSelectImageClick(v);
        }

        if(v==B1)
        {
            updateProfile();
        }
    }

    public void updateProfile(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserID=user.getUid();


        Name=name.getText().toString();


        uploadJPEG();





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
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
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

                                Intent i = new Intent(ProfileSetup.this, DisplayOptionsFile.class);
                                i.putExtra("DispName", Name);
                                i.putExtra("PhotoURI", filePath);
                                startActivity(i);
                            }
                            else Toast.makeText(ProfileSetup.this, "File Path Error!", Toast.LENGTH_LONG).show();


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
            //display an error if no file is selected
        }


    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onStart() {
        super.onStart();
         //firebaseAuth is of class FirebaseAuth
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
