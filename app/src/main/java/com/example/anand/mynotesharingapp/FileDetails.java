package com.example.anand.mynotesharingapp;

import android.media.Image;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 09-06-2017.
 */
@IgnoreExtraProperties
public class FileDetails {

    private String Title;
    private String ImageURI;
    private String Subject;
    private String UserID;
    private String Type;
    private String Tags;

    public FileDetails()
    {

    }

    public FileDetails(String Title,String Tags,String ImageURI,String Subject,String UserID,String Type)
    {
        this.Title=Title;
        this.Tags=Tags;
        this.ImageURI= ImageURI;
        this.Subject=Subject;
        this.UserID=UserID;
        this.Type=Type;
    }

    public String getTitle()
    {
        return Title;
    }

    public String getTags()
    {
        return Tags;
    }

    public String getImageURI()
    {
        return ImageURI;
    }

    public String getSubject()
    {
        return Subject;
    }

    public String getType()
    {
        return Type;
    }

    public String getUserID(){return UserID;}


    @Exclude
    public Map<String, Object> toMap()
    {
        HashMap<String,Object>result=new HashMap<>();
        result.put("Subject",Subject);
        result.put("Tags",Tags);
        result.put("Title",Title);
        result.put("ImageURI",ImageURI);
        result.put("UserID",UserID);
        result.put("Type",Type);
        return result;

    }
}

