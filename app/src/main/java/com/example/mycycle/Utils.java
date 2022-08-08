package com.example.mycycle;

import com.google.firebase.auth.FirebaseAuth;

public class Utils {

    public static boolean isUserLogin(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}
