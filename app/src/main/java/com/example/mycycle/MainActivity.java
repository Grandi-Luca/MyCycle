package com.example.mycycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.mycycle.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new DashboardFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.homeBtn:
                    replaceFragment(new DashboardFragment());
                    break;
                case R.id.calendarBtn:
                    replaceFragment(new CalendarFragment());
                    break;
                case R.id.forumBtn:
                    replaceFragment(new ForumFragment());
                    break;
                case R.id.userBtn:
                    replaceFragment(new ProfileFragment());
                    break;

            }
            return true;
        });

    }

    public void replaceFragment(Fragment fragment){
//        replace the current fragment with a specified one on input
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();

//        uncheck all items on bottom navigation menu
        var mBottomNavigationView=(BottomNavigationView)findViewById(R.id.bottomNavigationView);
        mBottomNavigationView.getMenu().setGroupCheckable(0, false, true);
        for (int i = 0; i < mBottomNavigationView.getMenu().size(); i++){
            mBottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        mBottomNavigationView.getMenu().setGroupCheckable(0, true, true);

//        check the correct item on bottom navigation menu based on current fragment
        if(fragment.getClass().getName() == DashboardFragment.class.getName()){
            mBottomNavigationView.getMenu().findItem(R.id.homeBtn).setChecked(true);
        } else if(fragment.getClass().getName() == CalendarFragment.class.getName()){
            mBottomNavigationView.getMenu().findItem(R.id.calendarBtn).setChecked(true);
        } else if(fragment.getClass().getName() == ForumFragment.class.getName()){
            mBottomNavigationView.getMenu().findItem(R.id.forumBtn).setChecked(true);
        } else if(fragment.getClass().getName() == ProfileFragment.class.getName()){
            mBottomNavigationView.getMenu().findItem(R.id.userBtn).setChecked(true);
        }
    }
}