package com.example.mycycle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.User;
import com.example.mycycle.worker.AlarmReceiver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

public class RegisterUser extends AppCompatActivity {

    private DatePickerDialog picker;
    private Uri uriProfileImage;
    private EditText eLastTime, editPassword, editConfirmPassword, editEmail, editNickName, editDurationPeriod, editDurationMenstruation;
    private TextView textClock;
    private TextClock clock;
    private CheckBox pill;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        initActivity();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"DefaultLocale", "CutPasteId"})
    private void initActivity(){

        this.editNickName = findViewById(R.id.nickname);
        this.editEmail = findViewById(R.id.email);
        this.editPassword = findViewById(R.id.password);
        this.editConfirmPassword = findViewById(R.id.confirm_password);
        this.editDurationPeriod = findViewById(R.id.durationPeriod);
        this.editDurationMenstruation = findViewById(R.id.menstruationDuration);
        this.textClock = findViewById(R.id.textClock);
        this.pill = findViewById(R.id.pill);

        this.uriProfileImage = Uri.parse("android.resource://com.example.mycycle/" + R.drawable.default_profile_image);

        eLastTime = findViewById(R.id.editText1);
        eLastTime.setInputType(InputType.TYPE_NULL);
        eLastTime.setOnClickListener((View v)-> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(RegisterUser.this,
                    (view, year1, monthOfYear, dayOfMonth) -> eLastTime.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1)), year, month, day);
            picker.show();
        });

        // load image from gallery
        findViewById(R.id.buttonLoadPicture).setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(galleryIntent);
        });

        findViewById(R.id.register).setOnClickListener(view -> registerUser());


        pill.setOnClickListener(v->{
            findViewById(R.id.setAlarmContainer)
                    .setVisibility(pill.isChecked()
                            ? View.VISIBLE
                            : View.GONE);
        });

        clock = new TextClock(this);
        textClock.setText(LocalTime.now().format(clock.is24HourModeEnabled() ?
                DateTimeFormatter.ofPattern("HH:mm") :
                DateTimeFormatter.ofPattern("hh:mm a")
        ));
        textClock.setOnClickListener(v -> {
            boolean is24HourFormat = clock.is24HourModeEnabled();

//          get the alarm time and parse it to a LocalTime for extract hour and minute
            var format = is24HourFormat
                    ? DateTimeFormatter.ofPattern("HH:mm")
                    : DateTimeFormatter.ofPattern("hh:mm a");
            var date = LocalTime.parse(textClock.getText(), format);
            int hour = date.getHour();
            int minute = date.getMinute();

//          select time for the daily remainder
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this,
                    (timePicker, selectedHour, selectedMinute) ->
                            textClock.setText(Utils.getHHmmInSystemFormat(selectedHour, selectedMinute, is24HourFormat)),
                    hour, minute, is24HourFormat);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    private void registerUser() {
        String nickname = this.editNickName.getText().toString();
        String email = this.editEmail.getText().toString().trim();
        String password = this.editPassword.getText().toString().trim();
        String confirmPassword = this.editConfirmPassword.getText().toString().trim();
        String durationPeriod = this.editDurationPeriod.getText().toString().trim();
        String durationMenstruation = this.editDurationMenstruation.getText().toString().trim();
        String firstDay = this.eLastTime.getText().toString().trim();

        if(this.isEmpty(nickname.trim(), this.editNickName, "nickname")
                || this.isEmpty(email, editEmail, "email")
                || this.isEmpty(password, this.editPassword, "password")
                || this.isEmpty(confirmPassword, this.editConfirmPassword, "confirm password")
                || this.isEmpty(durationPeriod, this.editDurationPeriod, "duration period")
                || this.isEmpty(durationMenstruation, this.editDurationMenstruation,"duration menstruation")
                || this.isEmpty(firstDay, this.eLastTime, "first day of the last period")) {
            return;
        }

        // check if the patterns matches with the fields
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.editEmail.setError("Please provide valid email");
            this.editEmail.requestFocus();
            return;
        }

        if(password.length() < 6) {
            this.editPassword.setError("Min password length should be 6 characters");
            this.editPassword.requestFocus();
            return;
        }

        if(!confirmPassword.equals(password)) {
            this.editConfirmPassword.setError("Confirm password must bu equal to password");
            this.editConfirmPassword.requestFocus();
            return;
        }

        if(uriProfileImage.toString().trim().isEmpty()){
            Toast.makeText(RegisterUser.this, "Choose a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // create new user and save in firebase realtime database
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(registerAuth -> {
                    if(registerAuth.isSuccessful()) {

                        // create user
                        User user = new User()
                                .setNickname(nickname)
                                .setFirstDay(firstDay)
                                .setDurationPeriod(Integer.parseInt(durationPeriod))
                                .setDurationMenstruation(Integer.parseInt(durationMenstruation))
                                .setProfilePicture(" ");

                        var uReference =  FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                        // insert user on database
                        uReference.setValue(user).addOnCompleteListener(register -> {
                            if(register.isSuccessful()){
                                Toast.makeText(RegisterUser.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterUser.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                AlarmReceiver.menstruationPeriod = user.getDurationPeriod();

                                var storageReference = FirebaseStorage.getInstance("gs://auth-89f75.appspot.com")
                                        .getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                // insert profile picture on database storage
                                storageReference.putFile(uriProfileImage).addOnSuccessListener(taskSnapshot -> {
                                    taskSnapshot.getMetadata();
                                    Task<Uri> downloadUrl = storageReference.getDownloadUrl();
                                    // link profile picture database storage - database
                                    downloadUrl.addOnSuccessListener(uri -> {
                                        uReference.child("profilePicture").setValue(uri.toString());

                                        var notificationService = new NotificationService(this);
                                        Calendar calendar = Calendar.getInstance();
                                        // add Alarm manager to create a daily remainder
                                        if(pill.isChecked()) {
                                            CharSequence time;

                                            // get the time as a string using a specified format
                                            if (!clock.is24HourModeEnabled()) {
                                                time = CalendarUtils.formattedTime(textClock.getText().toString(),
                                                        new SimpleDateFormat("hh:mm aa"),
                                                        new SimpleDateFormat("HH:mm"));
                                            } else {
                                                time = textClock.getText().toString();
                                            }
                                            var date = LocalTime.parse(time);

                                            calendar.set(Calendar.HOUR_OF_DAY, date.getHour());
                                            calendar.set(Calendar.MINUTE, date.getMinute());
                                            calendar.set(Calendar.SECOND, 0);

                                            notificationService.setMedicineDailyNotification(calendar);
                                        }

                                        var date = LocalDate.parse(eLastTime.getText().toString(),
                                                DateTimeFormatter.ofPattern("d/MM/yyyy"));
                                        calendar.set(Calendar.MONTH, date.getMonthValue());
                                        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
                                        calendar.set(Calendar.HOUR_OF_DAY, 9);
                                        calendar.set(Calendar.MINUTE, 0);
                                        calendar.set(Calendar.SECOND, 0);
                                        notificationService.setMenstruationNotification(calendar);
                                    });
                                });

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
}