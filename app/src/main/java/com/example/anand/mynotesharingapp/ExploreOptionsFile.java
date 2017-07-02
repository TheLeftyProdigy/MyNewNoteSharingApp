package com.example.anand.mynotesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Anand on 27-06-2017.
 */

public class ExploreOptionsFile extends AppCompatActivity implements View.OnClickListener {


    Button b1,b2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exploreoptions);

        b1=(Button)findViewById(R.id.explorepdf);
        b2=(Button)findViewById(R.id.explorejpeg);


        b1.setOnClickListener(this);
        b2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(v==b1)
        {
            //Explore PDF
            Intent i=new Intent(this,ExplorePDFActivity.class);
            startActivity(i);


        }

        if(v==b2)
        {
            //Explore JPEG
            Intent i=new Intent(this,ExploreJPEGActivity.class);
            startActivity(i);
        }

    }
}
