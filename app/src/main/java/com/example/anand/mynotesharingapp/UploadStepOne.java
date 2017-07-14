package com.example.anand.mynotesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anand on 07-06-2017.
 */

public class UploadStepOne extends AppCompatActivity implements View.OnClickListener {

    EditText title,subject,tag;
    String Title,Subject,Tag;
    Button b1,b2;
    List<String>tagList;
    TextView tv;
    String stringfortv="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.uploadsteptwo);
        title=(EditText)findViewById(R.id.title);
        subject=(EditText)findViewById(R.id.subject);
        tag=(EditText)findViewById(R.id.tags);
        b1=(Button)findViewById(R.id.sendto1);
        b2=(Button)findViewById(R.id.tagbutton);

        tv=(TextView)findViewById(R.id.allTags);

        tagList=new ArrayList<String>();

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v==b1)
        {
            Title=title.getText().toString().trim();
            Subject=subject.getText().toString().trim();


            if(Title.isEmpty())
            {
                Toast.makeText(UploadStepOne.this,"The Title cannot be empty!", Toast.LENGTH_SHORT).show();

            }
            else if (Subject.isEmpty())
            {
                Toast.makeText(UploadStepOne.this,"The Subject cannot be empty!",Toast.LENGTH_SHORT).show();
            }

            else {

                Bundle b = new Bundle();
                b.putString("Title", Title);
                b.putString("Subject", Subject);
                b.putString("Tag", stringfortv);


                Intent i = new Intent(this, UploadOptionsFile.class);
                i.putExtras(b);
                startActivity(i);
            }
        }

        if(v==b2)
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
}
