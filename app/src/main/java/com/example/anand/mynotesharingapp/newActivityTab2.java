package com.example.anand.mynotesharingapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anand on 07-06-2017.
 */

public class newActivityTab2 extends AppCompatActivity implements View.OnClickListener {

    EditText title,subject,tag;
    String Title,Subject,Tag;
    Button b1;
    List<String>tagList;
    TextView tv,uploadtv;
    String stringfortv="";
    TextView i1,i2,i3;


    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    String type="img";

    FileDetails fileinfo;

    String UserID,ImageURI;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.secondtab);
        title=(EditText)findViewById(R.id.title);
        subject=(EditText)findViewById(R.id.subject);
        tag=(EditText)findViewById(R.id.tags);
        b1=(Button)findViewById(R.id.tagbutton);
        uploadtv=(TextView) findViewById(R.id.uploadbutton);

        tv=(TextView)findViewById(R.id.allTags);

        i1=(TextView)findViewById(R.id.txtinfo1);
        i2=(TextView)findViewById(R.id.txtinfo2);
        i3=(TextView)findViewById(R.id.txtinfo3);

        Typeface typeface=Typeface.createFromAsset(getAssets(),"fonts/robotoslabreg.ttf");

        title.setTypeface(typeface);
        subject.setTypeface(typeface);
        tag.setTypeface(typeface);
        uploadtv.setTypeface(typeface);
        tv.setTypeface(typeface);
        i1.setTypeface(typeface);
        i2.setTypeface(typeface);
        i3.setTypeface(typeface);


        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_addfile);

        tagList=new ArrayList<String>();

        b1.setOnClickListener(this);
        uploadtv.setOnClickListener(this);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId==R.id.tab_home)
                {
                    startActivity(new Intent(newActivityTab2.this,newActivityTab1.class));
                }
                if(tabId==R.id.tab_search)
                {
                    startActivity(new Intent(newActivityTab2.this,newActivityTab3.class));
                }
                if(tabId==R.id.tab_profile)
                {
                    startActivity(new Intent(newActivityTab2.this,newActivityTab4.class));
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v==uploadtv)
        {
            Title=title.getText().toString().trim();
            Subject=subject.getText().toString().trim();


            if(Title.isEmpty())
            {
                Toast.makeText(newActivityTab2.this,"The Title cannot be empty!", Toast.LENGTH_SHORT).show();

            }
            else if (Subject.isEmpty())
            {
                Toast.makeText(newActivityTab2.this,"The Subject cannot be empty!",Toast.LENGTH_SHORT).show();
            }

            else {

                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                UserID=user.getUid();

                Log.d("UserID","The User ID is"+UserID);

                mStorageRef= FirebaseStorage.getInstance().getReference();
                mDatabase= FirebaseDatabase.getInstance().getReference("userinfo");

                showFileChooserForImage();

                emptyAll();

            }
        }


        if(v==b1)
        {
            Tag=tag.getText().toString().trim();
            if(!Tag.isEmpty())
            {
                tagList.add(tag.getText().toString().trim());
            }

            stringfortv="";

            for(String s:tagList)
            {
                stringfortv=stringfortv+s+" ";
                Log.d("ok",stringfortv);
            }

            tv.setText(stringfortv);
            tag.setText("");

        }

    }

    private void emptyAll()
    {
        title.setText("Title");
        subject.setText("Subject");
        tag.setText("Keywords");
        tv.setText("");
    }

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


    }





    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startActivity(new Intent(this, newActivityTab1.class));
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void killActivity() {
        newActivityTab2.this.finish();
    }
}
