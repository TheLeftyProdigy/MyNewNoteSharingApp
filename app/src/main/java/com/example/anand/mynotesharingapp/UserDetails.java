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
    private String CollegeName;

    public UserDetails()
    {

    }

    public UserDetails(String ImageURI,String Name,String UserID,String CollegeName)
    {
        this.ImageURI=ImageURI;
        this.Name=Name;
        this.UserID=UserID;
        this.CollegeName=CollegeName;

    }

    public String getName(){return Name;}

    public String getImageURI(){return ImageURI;}

    public String getCollegeName(){return CollegeName;}

    public String getUserID(){return UserID;}

    @Exclude
    public Map<String,Object>toMap()
    {
        HashMap<String,Object>result=new HashMap<>();
        result.put("Name",Name);
        result.put("ImageURI",ImageURI);
        result.put("UserID",UserID);
        result.put("CollegeName",CollegeName);
        return result;
    }

}
