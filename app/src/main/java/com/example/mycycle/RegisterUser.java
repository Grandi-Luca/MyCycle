package com.example.mycycle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

public class RegisterUser extends AppCompatActivity {

    private DatePickerDialog picker;
    private Uri uriProfileImage;
    private EditText eText, editPassword, editConfirmPassword, editEmail, editNickName, editDurationPeriod, editDurationMenstruation;
    private TextClock textClock;

    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    ImageView imageView = findViewById(R.id.profileImage);
                    imageView.setImageURI(result.getData().getData());

                    uriProfileImage = result.getData().getData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        initActivity();

    }

    @SuppressLint("DefaultLocale")
    private void initActivity(){

        this.editNickName = findViewById(R.id.nickname);
        this.editEmail = findViewById(R.id.email);
        this.editPassword = findViewById(R.id.password);
        this.editConfirmPassword = findViewById(R.id.confirm_password);
        this.editDurationPeriod = findViewById(R.id.duration_period);
        this.editDurationMenstruation = findViewById(R.id.duration_menstruation);
        this.textClock = findViewById(R.id.textClock);

        this.uriProfileImage = Uri.parse("android.resource://com.example.mycycle/" + R.drawable.default_profile_image);

        eText = findViewById(R.id.editText1);
        eText.setInputType(InputType.TYPE_NULL);
        eText.setOnClickListener((View v)-> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(RegisterUser.this,
                    (view, year1, monthOfYear, dayOfMonth) -> eText.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1)), year, month, day);
            picker.show();
        });

        findViewById(R.id.register).setOnClickListener((View v) -> registerUser());

        findViewById(R.id.buttonLoadPicture).setOnClickListener((View v)->{

            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            launcher.launch(galleryIntent);
        });

        findViewById(R.id.pill).setOnClickListener(v->{
            CheckBox checkBox = findViewById(R.id.pill);
            if(checkBox.isChecked()){
                findViewById(R.id.setAlarmContainer).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.setAlarmContainer).setVisibility(View.INVISIBLE);
            }
        });

        textClock.setOnClickListener(v -> {
            Calendar mCurrentTime = Calendar.getInstance();
            int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mCurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(RegisterUser.this, (timePicker, selectedHour, selectedMinute) ->
                    textClock.setText(getHHmmInSystemFormat(selectedHour, selectedMinute)),
                    hour, minute, textClock.is24HourModeEnabled());
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

    }

    private void registerUser() {
        String nickname = this.editNickName.getText().toString();
        String email = this.editEmail.getText().toString().trim();
        String password = this.editPassword.getText().toString().trim();
        String confirmPassword = this.editConfirmPassword.getText().toString().trim();
        String durationPeriod = this.editDurationPeriod.getText().toString().trim();
        String durationMenstruation = this.editDurationMenstruation.getText().toString().trim();
        String firstDay = this.eText.getText().toString().trim();

        if(this.isEmpty(nickname.trim(), this.editNickName, "nickname")
                || this.isEmpty(email, editEmail, "email")
                || this.isEmpty(password, this.editPassword, "password")
                || this.isEmpty(confirmPassword, this.editConfirmPassword, "confirm password")
                || this.isEmpty(durationPeriod, this.editDurationPeriod, "duration period")
                || this.isEmpty(durationMenstruation, this.editDurationMenstruation,"duration menstruation")
                || this.isEmpty(firstDay, this.eText, "first day of the last period")){
            return;
        }

//        check if the patterns matches with the fields
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            this.editEmail.setError("Please provide valid email");
            this.editEmail.requestFocus();
            return;
        }

        if(password.length() < 6){
            this.editPassword.setError("Min password length should be 6 characters");
            this.editPassword.requestFocus();
            return;
        }

        if(!confirmPassword.equals(password)){
            this.editConfirmPassword.setError("Confirm password must bu equal to password");
            this.editConfirmPassword.requestFocus();
            return;
        }

        if(uriProfileImage.toString().trim().isEmpty()){
            Toast.makeText(RegisterUser.this, "Choose a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

//        create new user and save in firebase realtime database
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(registerAuth -> {
                    if(registerAuth.isSuccessful()){
                        User user = new User()
                                .setNickname(nickname)
                                .setFirstDay(firstDay)
                                .setDurationPeriod(durationPeriod)
                                .setDurationMenstruation(durationMenstruation);

                        FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("users")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                .setValue(user)
                                .addOnCompleteListener(register -> {
                                    if(register.isSuccessful()){
                                        Toast.makeText(RegisterUser.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterUser.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                        FirebaseStorage.getInstance("gs://auth-89f75.appspot.com")
                                                .getReference()
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .putFile(uriProfileImage);

//                                        TODO: add Alarm manager to create a daily remainder

                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register! Try again", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterUser.this, "Failed to register! Try again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isEmpty(@NonNull String str, EditText editText, String nameField){
        if(str.isEmpty()) {
            editText.setError(nameField + "is required");
            editText.requestFocus();
            return true;
        }
        return false;
    }

    @NonNull
    private String getHHmmInSystemFormat(int hour, int minute){
        String formattedTime;

        if(!textClock.is24HourModeEnabled()) {
            if(hour == 0) {
                formattedTime = (minute < 10) ?
                        12 + ":" + 0 + minute + " am" : 12 + ":" + minute + " am";
            } else if(hour > 12) {
                formattedTime = (minute < 10) ?
                        (hour - 12) + ":" + 0 + minute + " pm" : (hour - 12) + ":" + minute + " pm";
            } else if(hour == 12) {
                formattedTime = (minute < 10) ?
                        hour + ":" + 0 + minute + " pm" : hour + ":" + minute + " pm";
            } else {
                formattedTime = (minute < 10) ?
                        hour + ":" + 0 + minute + " am" : hour + ":" + minute + " am";
            }
        } else {
            formattedTime = hour + ":" + minute;
        }

        return formattedTime;
    }
}