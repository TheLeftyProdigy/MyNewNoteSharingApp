package com.example.anand.mynotesharingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

/**
 * Created by Anand on 26-06-2017.
 */

public class newActivityTab3 extends AppCompatActivity {

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
        setContentView(R.layout.thirdtab);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }




        mAuth=FirebaseAuth.getInstance();

        TextView tv1,tv2;
        TextView uname,welcome;
        ImageView iv;
        tv1=(TextView)findViewById(R.id.templatetv);
        tv2=(TextView)findViewById(R.id.templatetv2);

        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_search);

        Typeface typeFace=Typeface.createFromAsset(getAssets(),"fonts/robotoslabreg.ttf");

        tv1.setTypeface(typeFace);
        tv2.setTypeface(typeFace);



        recyclerView = (RecyclerView) findViewById(R.id.recyclerView2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView1 = (RecyclerView) findViewById(R.id.recyclerView3);
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
                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    FileDetails upload = postSnapshot.getValue(FileDetails.class);



                    if(upload.getType().equals(img))
                        uploads.add(upload);

                }
                //creating adapter
                adapter = new AdapterForExploreJPEG(newActivityTab3.this, uploads);

                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
                recyclerView.setNestedScrollingEnabled(false);
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


                    if(upload.getType().equals(pdf))
                        uploads1.add(upload);

                }
                //creating adapter
                adapter1 = new AdapterForExplorePDF(newActivityTab3.this, uploads1);

                //adding adapter to recyclerview

                recyclerView1.setNestedScrollingEnabled(false);
                recyclerView1.setAdapter(adapter1);
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
                    startActivity(new Intent(newActivityTab3.this,newActivityTab2.class));
                }
                if(tabId==R.id.tab_home)
                {
                    startActivity(new Intent(newActivityTab3.this,newActivityTab1.class));
                }
                if(tabId==R.id.tab_profile)
                {
                    startActivity(new Intent(newActivityTab3.this,newActivityTab4.class));
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
