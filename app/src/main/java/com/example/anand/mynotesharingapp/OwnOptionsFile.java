package com.example.anand.mynotesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Anand on 27-06-2017.
 */

public class OwnOptionsFile extends AppCompatActivity implements View.OnClickListener {


    Button b1,b2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owncreationoptions);

        b1=(Button)findViewById(R.id.ownpdf);
        b2=(Button)findViewById(R.id.ownjpeg);


        b1.setOnClickListener(this);
        b2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(v==b1)
        {
            //Own PDF
            Intent i=new Intent(this,ShowOwnPDFUploads.class);
            startActivity(i);


        }

        if(v==b2)
        {
            //Own JPEG
            Intent i=new Intent(this,ShowOwnJPEGUploads.class);
            startActivity(i);
        }

    }
}
