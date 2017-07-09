package com.example.anand.mynotesharingapp;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anand on 09-06-2017.
 */

public class ExploreJPEGActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    //adapter object
    private RecyclerView.Adapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<FileDetails> uploads;

    final String img="img";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerviewtemplate);

        TextView tv=(TextView)findViewById(R.id.templatetv);

        Typeface typeFace=Typeface.createFromAsset(getAssets(),"fonts/robotoslabreg.ttf");
        tv.setTypeface(typeFace);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        progressDialog = new ProgressDialog(this);

        uploads = new ArrayList<>();

        //displaying progress dialog while fetching images
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference("userinfo");

        //adding an event listener to fetch values
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                progressDialog.dismiss();


                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    FileDetails upload = postSnapshot.getValue(FileDetails.class);



                    if(upload.getType().equals(img))
                    uploads.add(upload);

                }
                //creating adapter
                adapter = new AdapterForExploreJPEG(ExploreJPEGActivity.this, uploads);

                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
                recyclerView.setNestedScrollingEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }
}
