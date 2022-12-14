package com.example.mycycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.mycycle.databinding.ActivityMainBinding;
import com.example.mycycle.model.Menstruation;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.example.mycycle.viewModel.RepositoryViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static User currentUser;
    public static RepositoryViewModel mViewModel;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        mViewModel = new RepositoryViewModel(this.getApplication());

        FirebaseDAOUser daoUser = new FirebaseDAOUser();
        daoUser.getCurrentUserInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                var user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUser = user.setUserID(snapshot.getKey());
                    calendarInit();
                    replaceFragment(new DashboardFragment());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        setContentView(binding.getRoot());

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
        if(fragment.getClass().getName().equals(DashboardFragment.class.getName())){
            mBottomNavigationView.getMenu().findItem(R.id.homeBtn).setChecked(true);
        } else if(fragment.getClass().getName().equals(CalendarFragment.class.getName())){
            mBottomNavigationView.getMenu().findItem(R.id.calendarBtn).setChecked(true);
        } else if(fragment.getClass().getName().equals(ForumFragment.class.getName())){
            mBottomNavigationView.getMenu().findItem(R.id.forumBtn).setChecked(true);
        } else if(fragment.getClass().getName().equals(ProfileFragment.class.getName())){
            mBottomNavigationView.getMenu().findItem(R.id.userBtn).setChecked(true);
        }
    }

    private void calendarInit() {

        if(currentUser != null) {
            Menstruation menstruation = mViewModel
                    .getLastMenstruationSaved();

            LocalDate date;
            if (menstruation == null) {
                date = LocalDate.parse(currentUser.getFirstDay(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            } else {
                date = LocalDate.parse(menstruation.getStartDay());
            }

            while (!date.isAfter(LocalDate.now())) {
                var startDay = date.toString();
                var finishDay = date
                        .plusDays(currentUser.getDurationMenstruation() - 1)
                        .toString();

                mViewModel.insertNewMenstruation(new Menstruation()
                        .setUserID(currentUser.getUserID())
                        .setStartDay(startDay)
                        .setLastDay(finishDay));

                CalendarUtils.lastMenstruation = date;

                date = date.plusDays(currentUser.getDurationPeriod());

            }

            mViewModel.clearPrediction();

            var c = mViewModel.getPredictedMenstruation(currentUser, LocalDate.now());
        }
    }
}