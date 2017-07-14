package com.example.anand.mynotesharingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.ui.InstantSearchHelper;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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


        isStoragePermissionGranted();


        mAuth=FirebaseAuth.getInstance();

        TextView tv1,tv2;
        TextView uname,welcome;
        ImageView iv;
        SearchView sv;
        tv1=(TextView)findViewById(R.id.templatetv);
        tv2=(TextView)findViewById(R.id.templatetv2);
        sv=(SearchView)findViewById(R.id.simpleSearchView);


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


                    if((upload.getType().equals("pdf"))||(upload.getType().equals("docx"))||(upload.getType().equals("pptx"))||(upload.getType().equals("xlsx")))
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



        //Code Start for Algolia

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String SearchQueryInput) {
                Client client = new Client("Y1HVY75FHB", "8899aad8d8168c5d6d78147d56912cb3");
                Index index = client.getIndex("userinfo");
                Query query = new Query(SearchQueryInput)
                        .setAttributesToRetrieve("Subject", "Tags","Title")
                        .setHitsPerPage(50);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        // [..]
                        Log.d("algolia",content.toString());

                        final JSONObject innerContent=content;

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                //dismissing the progress dialog
                                s++;
                                uploads.clear();

                                //iterating through all the values in database
                                //iterating through all the values in database
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    FileDetails upload = postSnapshot.getValue(FileDetails.class);


                                    if (upload.getType().equals(img)) {

                                        try {
                                            JSONArray arr=innerContent.getJSONArray("hits");

                                            Log.d("JSONARRAY",arr.toString());
                                            for(int i=0;i<arr.length();i++)
                                            {


                                                JSONObject innerjson=arr.getJSONObject(i);

                                                if(innerjson.getJSONObject("_highlightResult").getJSONObject("ImageURI").get("value").toString().equals(upload.getImageURI()))
                                                {

                                                    uploads.add(upload);
                                                }

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }




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
                                uploads1.clear();
                                //iterating through all the values in database
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    FileDetails upload = postSnapshot.getValue(FileDetails.class);


                                    if((upload.getType().equals("pdf"))||(upload.getType().equals("docx"))||(upload.getType().equals("pptx"))||(upload.getType().equals("xlsx")))
                                    {
                                        try {
                                            JSONArray arr=innerContent.getJSONArray("hits");

                                            Log.d("JSONARRAY",arr.toString());
                                            for(int i=0;i<arr.length();i++)
                                            {


                                                JSONObject innerjson=arr.getJSONObject(i);
                                                if(innerjson.getJSONObject("_highlightResult").getJSONObject("ImageURI").get("value").toString().equals(upload.getImageURI()))
                                                {

                                                    uploads1.add(upload);
                                                }

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }


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


                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        sv.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                uploads.clear();
                uploads1.clear();
                //adding an event listener to fetch values
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //dismissing the progress dialog


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

                        //iterating through all the values in database
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            FileDetails upload = postSnapshot.getValue(FileDetails.class);


                            if((upload.getType().equals("pdf"))||(upload.getType().equals("docx"))||(upload.getType().equals("pptx"))||(upload.getType().equals("xlsx")))
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



                return false;
            }
        });


        //Code End for Algolia


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

    String TAG="OK";
    public boolean isStoragePermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
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
