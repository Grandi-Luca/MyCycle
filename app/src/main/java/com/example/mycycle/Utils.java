package com.example.mycycle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean isUserLogin(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static final int MAX_NUMBER_OF_LOAD_CARD = 8;

    public static <T> List<T> removeDuplicates(List<T> list) {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
}
