package com.example.anand.mynotesharingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 06-06-2017.
 */

public class DisplayOptionsFile extends AppCompatActivity implements View.OnClickListener{


    TextView tv1,tv2,tv3,tv4;
    FirebaseAuth mAuth;

    String b_name,b_photoUri;
    String name,email;
    DatabaseReference mDatabase;
    UserDetails userinfo;
    String UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispoptions);

        mAuth=FirebaseAuth.getInstance();

        mDatabase= FirebaseDatabase.getInstance().getReference("profilepicture");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserID=user.getUid();
        if (user != null) {
            // Name, email address, and profile photo Url
            name = user.getDisplayName();
            email = user.getEmail();
            Uri photoUrl=null;
            photoUrl = user.getPhotoUrl();

            if(photoUrl!=null && name!=null) {

                writeNewPost(name,String.valueOf(photoUrl));
                Log.d("photoUri:", photoUrl.toString());
            }// Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();


            if(mAuth.getCurrentUser()!=null && photoUrl==null)
            {

                Bundle b=getIntent().getExtras();
                b_name=(String)b.get("DispName");
                b_photoUri=(String)b.get("PhotoURI");
                Log.d("Ok","PhotoURL IS NULL,, Starting Profile Setup!");
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(b_name)
                        .setPhotoUri(Uri.parse(b_photoUri))
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

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
        }




        tv1=(TextView)findViewById(R.id.createButton);
        tv2=(TextView)findViewById(R.id.exploreButton);
        tv3=(TextView)findViewById(R.id.myCreationsButton);
        tv4=(TextView)findViewById(R.id.logout) ;


        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        tv4.setOnClickListener(this);

    }


    public void writeNewPost(String name,String uri)
    {

        String Key=mDatabase.child(UserID).push().getKey();
        userinfo=new UserDetails(uri,name,UserID);
        Map<String,Object> postValues=userinfo.toMap();

        Map<String,Object> childUpdates=new HashMap<>();
        childUpdates.put("/"+UserID,postValues);

        mDatabase.updateChildren(childUpdates);


    }

    @Override
    public void onClick(View v) {
        if(v==tv1)
        {
            Intent i=new Intent(this,UploadStepOne.class);
            startActivity(i);
            //Create.
        }

        if(v==tv2)
        {
            //Explore.
            Intent i=new Intent(this,ExploreOptionsFile.class);
            startActivity(i);
        }

        if(v==tv3)
        {
            Intent i=new Intent(this,OwnOptionsFile.class);
            startActivity(i);
            //Own Uploads.
        }
        if(v==tv4)
        {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            startActivity(new Intent(this,MainActivity.class));
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
