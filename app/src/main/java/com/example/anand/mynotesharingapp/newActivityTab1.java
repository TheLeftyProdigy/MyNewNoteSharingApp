package com.example.anand.mynotesharingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.anand.mynotesharingapp.R.id.bottomBar;
import static com.example.anand.mynotesharingapp.R.id.start;
import static com.example.anand.mynotesharingapp.R.id.templatetv;

/**
 * Created by Anand on 26-06-2017.
 */

public class newActivityTab1 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView recyclerView1;

    //adapter object
    private RecyclerView.Adapter adapter;
    private RecyclerView.Adapter adapter1;

    //database reference
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase1;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<FileDetails> uploads;
    private List<FileDetails> uploads1;

    FirebaseAuth mAuth;

    int s=0;

    String b_name,b_photoUri;
    String name,email;
    UserDetails userinfo;
    String UserID;
    String CollegeName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firsttab);

        //Checking if Profile Picture and other various Information is empty or not.
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//        mDatabase= FirebaseDatabase.getInstance().getReference("profilepicture");
//
//        UserID=user.getUid();
//        if (user != null) {
//            // Name, email address, and profile photo Url
//            name = user.getDisplayName();
//            email = user.getEmail();
//            Uri photoUrl=null;
//            photoUrl = user.getPhotoUrl();
//
//            if(photoUrl!=null && name!=null) {
//                writeNewPost(name,String.valueOf(photoUrl));
//                Log.d("photoUri:", photoUrl.toString());
//            }// Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();
//
//
//            if(mAuth.getCurrentUser()!=null && photoUrl==null)
//            {
//
//                Bundle b=getIntent().getExtras();
//                b_name=(String)b.get("DispName");
//                b_photoUri=(String)b.get("PhotoURI");
//                Log.d("Ok","PhotoURL IS NULL,, Starting Profile Setup!");
//                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                        .setDisplayName(b_name)
//                        .setPhotoUri(Uri.parse(b_photoUri))
//                        .build();
//
//                user.updateProfile(profileUpdates)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Log.d("UpdateDone", "User profile updated.");
//                                }
//                            }
//                        })
//
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.d("Fail","Failed to update Profile!");
//                            }
//                        });
//
//            }
//            // The user's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getToken() instead.
//
//        }







        mAuth=FirebaseAuth.getInstance();

        Uri ImageURI=user.getPhotoUrl();
        String username=user.getDisplayName();

        final TextView tv1,tv2;
        TextView uname,welcome;
        ImageView iv;
        tv1=(TextView)findViewById(R.id.templatetv);
        tv2=(TextView)findViewById(R.id.templatetv2);
        welcome=(TextView)findViewById(R.id.welcome);
        iv=(ImageView)findViewById(R.id.imageView1);
        uname=(TextView)findViewById(R.id.username);

        uname.setText(username);

        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_home);

        Typeface typeFace=Typeface.createFromAsset(getAssets(),"fonts/robotoslabreg.ttf");

        tv1.setTypeface(typeFace);
        tv2.setTypeface(typeFace);
        uname.setTypeface(typeFace);
        welcome.setTypeface(typeFace);

        Glide.with(getApplicationContext()).load(ImageURI).into(iv);



        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView1 = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView1.setHasFixedSize(true);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));


        progressDialog = new ProgressDialog(this);

        uploads = new ArrayList<>();
        uploads1 = new ArrayList<>();

        //displaying progress dialog while fetching images
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference("userinfo");
        mDatabase1 = FirebaseDatabase.getInstance().getReference("userinfo");

        final String UserID;
        FirebaseUser us= FirebaseAuth.getInstance().getCurrentUser();
        UserID=us.getUid();


        final String img="img";
        final String pdf="pdf";



        //adding an event listener to fetch values
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                s++;


                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {


                    FileDetails upload = postSnapshot.getValue(FileDetails.class);

                    if(upload.getUserID().equals(UserID)&&upload.getType().contentEquals(img)) {
                        uploads.add(upload);
                    }


                }
                //creating adapter
                adapter = new AdapterForOwnJPEG(newActivityTab1.this, uploads);

                //adding adapter to recyclerview
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setAdapter(adapter);


               if(adapter.getItemCount()==0){
                   tv1.setText("No Image Files uploaded yet! Click on the Plus icon to upload one!");
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        mDatabase1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                progressDialog.dismiss();

                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {


                    FileDetails upload = postSnapshot.getValue(FileDetails.class);

                    if(upload.getUserID().equals(UserID)&&((upload.getType().equals("pdf"))||(upload.getType().equals("docx"))||(upload.getType().equals("pptx"))||(upload.getType().equals("xlsx")))) {
                        uploads1.add(upload);
                    }


                }
                //creating adapter
                adapter1 = new AdapterForOwnPDF(newActivityTab1.this, uploads1);

                //adding adapter to recyclerview
                recyclerView1.setNestedScrollingEnabled(false);
                recyclerView1.setAdapter(adapter1);

                if(adapter1.getItemCount()==0){
                    tv2.setText("No PDF Files uploaded yet! Click on the Plus icon to upload one!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });




        //BottomBarNavigation

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId==R.id.tab_addfile)
                {
                    startActivity(new Intent(newActivityTab1.this,newActivityTab2.class));
                }
                if(tabId==R.id.tab_search)
                {
                    startActivity(new Intent(newActivityTab1.this,newActivityTab3.class));
                }
                if(tabId==R.id.tab_profile)
                {
                    startActivity(new Intent(newActivityTab1.this,newActivityTab4.class));
                }
            }
        });




    }

    public void writeNewPost(String name,String uri)
    {

        String Key=mDatabase.child(UserID).push().getKey();
        userinfo=new UserDetails(uri,name,UserID,CollegeName);
        Map<String,Object> postValues=userinfo.toMap();

        Map<String,Object> childUpdates=new HashMap<>();
        childUpdates.put("/"+UserID,postValues);

        mDatabase.updateChildren(childUpdates);


    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
