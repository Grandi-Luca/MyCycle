package com.example.mycycle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.User;
import com.example.mycycle.worker.AlarmReceiver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

@SuppressWarnings("StatementWithEmptyBody")
@RequiresApi(api = Build.VERSION_CODES.N)
public class RegisterUser extends AppCompatActivity {

    private DatePickerDialog picker;
    private Uri uriProfileImage;
    private EditText eLastTime, editPassword, editConfirmPassword, editEmail,
            editNickName, editDurationPeriod, editDurationMenstruation;
    private TextView textClock;
    private TextClock clock;
    private CheckBox pill;

    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    ImageView imageView = findViewById(R.id.profileImage);
                    imageView.setImageURI(result.getData().getData());

                    uriProfileImage = result.getData().getData();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");

                    uriProfileImage = getImageUri(RegisterUser.this, imageBitmap);
                    ImageView imageView = findViewById(R.id.profileImage);
                    imageView.setImageURI(uriProfileImage);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(takePictureIntent);
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(galleryIntent);
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    setNotifications();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        initActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"DefaultLocale"})
    private void initActivity(){

        this.editNickName = findViewById(R.id.nickname);
        this.editEmail = findViewById(R.id.email);
        this.editPassword = findViewById(R.id.password);
        this.editConfirmPassword = findViewById(R.id.confirm_password);
        this.editDurationPeriod = findViewById(R.id.durationPeriod);
        this.editDurationMenstruation = findViewById(R.id.menstruationDuration);
        this.textClock = findViewById(R.id.textClock);
        this.pill = findViewById(R.id.pill);

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
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // permission storage granted
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(galleryIntent);

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Additional rationale should be displayed
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                // Permission has not been asked yet
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });


        findViewById(R.id.buttonTakePicture).setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // permission camera granted
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(takePictureIntent);

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA)) {
                // Additional rationale should be displayed
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                // Permission has not been asked yet
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        findViewById(R.id.register).setOnClickListener(view -> registerUser());

        pill.setOnClickListener(view ->
                findViewById(R.id.textClock)
                        .setVisibility(pill.isChecked()
                                ? View.VISIBLE
                                : View.GONE));

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

    @RequiresApi(api = Build.VERSION_CODES.S)
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

        if(uriProfileImage == null) {
            this.uriProfileImage =
                    Uri.parse("android.resource://com.example.mycycle/"
                            + R.drawable.default_profile_image);
        } else if(uriProfileImage.toString().trim().isEmpty()){
            this.uriProfileImage =
                    Uri.parse("android.resource://com.example.mycycle/"
                            + R.drawable.default_profile_image);
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
                                .setEmail(email)
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

                                        if(ContextCompat.checkSelfPermission(this,
                                                Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                                            // permission notification granted
                                            setNotifications();

                                        } else if(ActivityCompat.shouldShowRequestPermissionRationale(
                                                this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                                            // Additional rationale should be displayed
                                            requestNotificationPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM);
                                        } else {
                                            // Permission has not been asked yet
                                            requestNotificationPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM);
                                        }
                                        setNotifications();

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

    @SuppressLint("SimpleDateFormat")
    private void setNotifications() {
        var notificationService = new NotificationService(this);
        Calendar calendar = Calendar.getInstance();
        // add Alarm manager to create a daily remainder
        if(pill.isChecked()) {
            CharSequence time;

            // get the time as a string using a specified format
            if (!clock.is24HourModeEnabled()) {
                time = CalendarUtils.formattedTime(textClock.getText().toString(),
                        new SimpleDateFormat("h:m aa"),
                        new SimpleDateFormat("H:m"));
            } else {
                time = textClock.getText().toString();
            }
            var date = LocalTime.parse(time);

            calendar.set(Calendar.HOUR_OF_DAY, date.getHour());
            calendar.set(Calendar.MINUTE, date.getMinute());
            calendar.set(Calendar.SECOND, 0);

            notificationService.setMedicineDailyNotification(calendar);
        }

        calendar = Calendar.getInstance();
        var date = LocalDate.parse(eLastTime.getText().toString(),
                DateTimeFormatter.ofPattern("d/M/yyyy"));
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.add(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        notificationService.setMenstruationNotification(calendar);
    }

    private boolean isEmpty(@NonNull String str, EditText editText, String nameField){
        if(str.isEmpty()) {
            editText.setError(nameField + "is required");
            editText.requestFocus();
            return true;
        }
        return false;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.
                insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}