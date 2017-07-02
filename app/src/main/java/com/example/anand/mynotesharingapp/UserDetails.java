package com.example.anand.mynotesharingapp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand on 27-06-2017.
 */

public class UserDetails {

    private String ImageURI;
    private String Name;
    private String UserID;

    public UserDetails()
    {

    }

    public UserDetails(String ImageURI,String Name,String UserID)
    {
        this.ImageURI=ImageURI;
        this.Name=Name;
        this.UserID=UserID;

    }

    public String getName(){return Name;}

    public String getImageURI(){return ImageURI;}

    @Exclude
    public Map<String,Object>toMap()
    {
        HashMap<String,Object>result=new HashMap<>();
        result.put("Name",Name);
        result.put("ImageURI",ImageURI);
        result.put("UserID",UserID);
        return result;
    }

}
