package com.example.mycycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.userBtn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserSectionActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.calendarBtn).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }
}