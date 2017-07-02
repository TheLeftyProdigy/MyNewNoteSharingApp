package com.example.anand.mynotesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Anand on 07-06-2017.
 */

public class UploadStepOne extends AppCompatActivity implements View.OnClickListener {

    EditText title,subject,tag;
    String Title,Subject,Tag;
    Button b1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.uploadsteptwo);
        title=(EditText)findViewById(R.id.title);
        subject=(EditText)findViewById(R.id.subject);
        tag=(EditText)findViewById(R.id.tags);
        b1=(Button)findViewById(R.id.sendto1);

        b1.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v==b1)
        {
            Title=title.getText().toString().trim();
            Subject=subject.getText().toString().trim();
            Tag=tag.getText().toString().trim();

            Bundle b=new Bundle();
            b.putString("Title",Title);
            b.putString("Subject",Subject);
            b.putString("Tag",Tag);





            Intent i=new Intent(this,UploadOptionsFile.class);
            i.putExtras(b);
            startActivity(i);

        }

    }
}
