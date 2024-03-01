package com.example.testapplication2.Model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }
    public static DatabaseReference currentUserInfo(){
        return FirebaseDatabase.getInstance().getReference("Users").child(currentUserId());
    }
    public static DatabaseReference allUser(){
        return FirebaseDatabase.getInstance().getReference().child("Users");
    }
}
