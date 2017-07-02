package com.example.anand.mynotesharingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 07-06-2017.
 */

public class UploadOptionsFile extends AppCompatActivity implements View.OnClickListener {



    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;
    Button b1,b2;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    String type="img";

    FileDetails fileinfo;

    String Title,Tag,Subject,UserID,ImageURI;

    ProgressDialog progressDialog;

    Bundle b;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.uploadoptions);

        b=getIntent().getExtras();

        Title=b.getString("Title");
        Tag=b.getString("Tag");
        Subject=b.getString("Subject");

        Log.d("bTitle","Title is "+Title);
        Log.d("bSubject","Subject is "+Subject);
        Log.d("b.Tags","Tag is "+Tag);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        UserID=user.getUid();

        Log.d("UserID","The User ID is"+UserID);

        mStorageRef=FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference("userinfo");

        b1=(Button)findViewById(R.id.uploadPDF);
        b2=(Button)findViewById(R.id.uploadjpeg);

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v==b1)
        {
            type="pdf";
            showFileChooserForPDF();

        }

        if(v==b2)
        {
            showFileChooserForImage();
        }
    }


    /**private void showFileChooserForImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }**/


    private void showFileChooserForImage() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    private void showFileChooserForPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_IMAGE_REQUEST);

    }



    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        uploadJPEG();
    }




    public void writeNewPost(String title,String tag, String uri, String subject,String type)
    {
        String Key=mDatabase.child("users").push().getKey();
        fileinfo=new FileDetails(title,tag,uri,subject,UserID,type);
        Map<String,Object> postValues=fileinfo.toMap();

        Map<String,Object> childUpdates=new HashMap<>();
        childUpdates.put("/"+Key,postValues);

        mDatabase.updateChildren(childUpdates);


    }

    private void uploadJPEG() {
        if (filePath != null) {
            //displaying progress dialog while image is uploading

            final ProgressDialog progressDialog = new ProgressDialog(this);

            if ((getFileExtension(filePath) == "jpeg") || (getFileExtension(filePath) == "png") || (getFileExtension(filePath) == "gif") || (getFileExtension(filePath) == "jpg")) {
                type = "img";
                progressDialog.setTitle("Uploading");
                progressDialog.show();
            } else if ((getFileExtension(filePath) == "pdf") || (getFileExtension(filePath) == "pptx") || (getFileExtension(filePath) == "docx") || (getFileExtension(filePath) == "xlsx")) {
                type = "pdf";
                progressDialog.setTitle("Uploading");
                progressDialog.show();
            } else {
                Toast.makeText(getApplicationContext(), "Wrong File Option! "+getFileExtension(filePath)+" format is not supported!", Toast.LENGTH_LONG).show();
                filePath=null;
                killActivity();
            }


            if(filePath!=null)
            {//getting the storage reference
            StorageReference sRef = mStorageRef.child("users/" + UserID + "/" + type + "/" + Subject + "/" + Title + "_" + System.currentTimeMillis() + "." + getFileExtension(filePath));
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
                            progressDialog.dismiss();


                            //displaying success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                            //creating the upload object to store uploaded image details
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            writeNewPost(Title, Tag, String.valueOf(downloadUrl), Subject, type);


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

        }
        else{
            //display an error if no file is selectedprotected void onPostExecute(MyResult result) {
            try {
                if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
                    this.progressDialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                this.progressDialog = null;
            }
        }


        Intent i=new Intent(this,DisplayOptionsFile.class);



        startActivity(i);
    }





    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DisplayOptionsFile.class));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        startActivity(new Intent(this, DisplayOptionsFile.class));
    }


    private void killActivity() {
        UploadOptionsFile.this.finish();
    }

}
