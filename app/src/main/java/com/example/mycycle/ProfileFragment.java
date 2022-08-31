package com.example.mycycle;

import static com.example.mycycle.MainActivity.currentUser;
import static com.example.mycycle.Utils.isUserLogin;
import static com.example.mycycle.Utils.showDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.mycycle.adapter.QuestionAdapter;
import com.example.mycycle.adapter.ReplyAdapter;
import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.RemindersInterface;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.DAOPost;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.example.mycycle.viewModel.RepositoryViewModel;
import com.example.mycycle.worker.AlarmReceiver;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"SameParameterValue", "StatementWithEmptyBody"})
public class ProfileFragment extends Fragment implements QuestionAdapter.OnItemListener {
    FirebaseDAOUser daoUser;
    private DAOPost dao;
    private RepositoryViewModel viewModel;

    RemindersInterface notificationService;
    SwitchMaterial medicineReminderSwitch;
    SwitchMaterial menstruationReminderSwitch;

    private TextView nickname, menstruationDuration, periodDuration;
    private EditText dialogNickname, dialogDurationMenstruation, dialogDurationPeriod;
    private ImageView profilePicture, dialogProfilePicture;
    private SwipeRefreshLayout swipeRefreshLayout;
    private QuestionAdapter questionAdapter;
    private ReplyAdapter replyAdapter;

    private Uri uriProfileImage;


    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
            result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        uriProfileImage = result.getData().getData();
                        if(dialogProfilePicture != null) {
                            dialogProfilePicture.setImageURI(uriProfileImage);
                        }
                    }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");

                        uriProfileImage = getImageUri(getContext(), imageBitmap);
                        dialogProfilePicture.setImageURI(uriProfileImage);
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

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        viewModel = MainActivity.mViewModel;
        daoUser = new FirebaseDAOUser();
        this.questionAdapter = new QuestionAdapter(getActivity(), this);
        this.replyAdapter = new ReplyAdapter(getActivity());
        this.dao = new DAOPost();

        notificationService = new NotificationService(getActivity());

        profilePicture = view.findViewById(R.id.profileImage);
        nickname = view.findViewById(R.id.nickname);
        menstruationDuration = view.findViewById(R.id.menstruationDuration);
        periodDuration = view.findViewById(R.id.durationPeriod);

        if(!isUserLogin()) {
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            currentUser = null;

            return null;
        }

        loadUserData();

        medicineReminderSwitch = view.findViewById(R.id.medicineReminderSwitch);
        medicineReminderSwitch.setChecked(notificationService.isMedicineReminderActive());
        medicineReminderSwitch.setOnClickListener(v -> {
            if(medicineReminderSwitch.isChecked()){
                showTimePickerDialog();
            } else {
                notificationService.cancelMedicineDailyNotification();
            }
        });

        menstruationReminderSwitch = view.findViewById(R.id.menstruationReminderSwitch);
        menstruationReminderSwitch.setChecked(notificationService.isMenstruationReminderActive());
        menstruationReminderSwitch.setOnClickListener(v -> {
            if(menstruationReminderSwitch.isChecked()){
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                notificationService.setMenstruationNotification(calendar);
            } else {
                notificationService.cancelMenstruationNotification();
            }
        });

        view.findViewById(R.id.personalPosts).setOnClickListener(v -> showPersonalPosts());

        view.findViewById(R.id.medicineReminder).setOnClickListener(v ->
                showTimePickerDialog());

        view.findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            currentUser = null;
        });

        MaterialCardView card = view.findViewById(R.id.accountInfo);
        if (card != null) {
            card.setOnClickListener(v -> {
                if (currentUser != null) {
                    showModifyInfoUserDialog();
                }
            });
        }
        return view;
    }

    private void loadUserData() {
        daoUser.getCurrentUserInfo().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                var user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUser = user.setUserID(snapshot.getKey());

                    String mDuration = currentUser.getDurationMenstruation() +
                            (currentUser.getDurationMenstruation() != 1 ?
                             " giorni"
                            : " giorno");

                    String pDuration = currentUser.getDurationPeriod() +
                            (currentUser.getDurationPeriod() != 1 ?
                                    " giorni"
                                    : " giorno");
                    // set profile filed
                    nickname.setText(currentUser.getNickname());
                    menstruationDuration.setText(mDuration);
                    periodDuration.setText(pDuration);

                    var activity = getActivity();
                    if (activity != null) {
                        Glide.with(activity)
                                .load(currentUser.getProfilePicture())
                                .into(profilePicture);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showModifyInfoUserDialog() {
        var dialog = new Dialog(getActivity());
        showDialog(dialog, R.layout.modify_info_user_dialog, R.style.dialog_top_down_swipe_animation);

        dialogProfilePicture = dialog.findViewById(R.id.profileImage);
        dialogNickname = dialog.findViewById(R.id.nickname);
        dialogDurationMenstruation = dialog.findViewById(R.id.menstruationDuration);
        dialogDurationPeriod = dialog.findViewById(R.id.durationPeriod);

        dialogNickname.setText(currentUser.getNickname());
        dialogDurationMenstruation.setText(String.valueOf(currentUser.getDurationMenstruation()));
        dialogDurationPeriod.setText(String.valueOf(currentUser.getDurationPeriod()));

        var activity = getActivity();
        if(activity != null) {
            if(currentUser.getProfilePicture().isEmpty()) {
                dialogProfilePicture.setImageURI(Uri
                        .parse("android.resource://com.example.mycycle/"
                                + R.drawable.default_profile_image));
            } else {
                Glide.with(getActivity())
                        .load(currentUser.getProfilePicture())
                        .into(dialogProfilePicture);
            }
        }

        // load image from gallery
        dialog.findViewById(R.id.buttonLoadPicture).setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // permission storage granted
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(galleryIntent);

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Additional rationale should be displayed
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                // Permission has not been asked yet
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        dialog.findViewById(R.id.buttonTakePicture).setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // permission camera granted
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(takePictureIntent);

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(), Manifest.permission.CAMERA)) {
                // Additional rationale should be displayed
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                // Permission has not been asked yet
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        dialog.findViewById(R.id.save).setOnClickListener(view -> {
            updateUserInfo();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.closeButton).setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    private void showTimePickerDialog(){
        var dialog = new Dialog(getActivity());
        showDialog(dialog, R.layout.time_picker_dialog, R.style.dialog_down_top_swipe_animation);

        dialog.findViewById(R.id.setAlarm).setOnClickListener(v -> {

            TimePicker timePicker = dialog.findViewById(R.id.material_timepicker_view);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);

            notificationService.cancelMedicineDailyNotification();
            notificationService.setMedicineDailyNotification(calendar);
            medicineReminderSwitch.setChecked(true);

            dialog.dismiss();
        });

        dialog.findViewById(R.id.closeButton).setOnClickListener(v -> {
            dialog.dismiss();
            if(!notificationService.isMedicineReminderActive()) {
                medicineReminderSwitch.setChecked(false);
            }
        });
    }

    private void showPersonalPosts() {
        var dialog = new Dialog(getActivity());
        showDialog(dialog, R.layout.list_dialog, R.style.dialog_horizontal_swipe_animation);

        this.swipeRefreshLayout = dialog.findViewById(R.id.swipeLayout);
        reloadQuestions("");

        RecyclerView questionRecyclerView = dialog.findViewById(R.id.recyclerList);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(questionAdapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadQuestions("");
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void updateUserInfo() {
        String nickname = this.dialogNickname.getText().toString();
        String durationPeriod = this.dialogDurationPeriod.getText().toString().trim();
        String durationMenstruation = this.dialogDurationMenstruation.getText().toString().trim();

        if(this.isEmpty(nickname.trim(), this.dialogNickname, "nickname")
                || this.isEmpty(durationPeriod, this.dialogDurationPeriod, "duration period")
                || this.isEmpty(durationMenstruation, this.dialogDurationMenstruation,"duration menstruation")) {
            return;
        }

        var user = new User()
                .setUserID(currentUser.getUserID())
                .setNickname(dialogNickname.getText().toString())
                .setDurationPeriod(Integer.parseInt(dialogDurationPeriod.getText().toString()))
                .setDurationMenstruation(Integer.parseInt(dialogDurationMenstruation
                        .getText().toString()))
                .setFirstDay(currentUser.getFirstDay())
                .setProfilePicture(currentUser.getProfilePicture());

        daoUser.updateUserInfo(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(),
                        "Info has been updated",
                        Toast.LENGTH_SHORT).show();

                if(uriProfileImage != null) {
                    daoUser.updateProfileImage(uriProfileImage)
                            .continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) uploadOnStorage -> {
                                if (!uploadOnStorage.isSuccessful()) {
                                    throw Objects.requireNonNull(uploadOnStorage.getException());
                                }
                                return daoUser.getStorageReference().getDownloadUrl();})
                            .addOnCompleteListener(uriTask -> {
                                if (uriTask.isSuccessful()) {
                                    Uri downloadUri = uriTask.getResult();
                                    daoUser.getCurrentUserInfo().child("profilePicture")
                                            .setValue(downloadUri.toString());

                                    AlarmReceiver.menstruationPeriod = Integer.parseInt(
                                            dialogDurationPeriod.getText().toString());
                                    AlarmReceiver.menstruationDuration = Integer.parseInt(
                                            dialogDurationMenstruation.getText().toString());

                                    updateMenstruationNotification();

                                }
                            });
                } else {
                    updateMenstruationNotification();
                }

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

    @Override
    public void onItemClick(QuestionItem item) {
        var dialog = new Dialog(getActivity());
        Utils.showDialog(dialog, R.layout.list_dialog, R.style.dialog_horizontal_swipe_animation);

        RecyclerView questionRecyclerView = dialog.findViewById(R.id.recyclerList);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(replyAdapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SwipeRefreshLayout swipeRefreshLayout = dialog.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
        });

        loadReplies(item.getPostID());

    }

    private void reloadQuestions(String query) {
        this.dao.get().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(true);
                if(snapshot.exists()) {
                    questionAdapter.clearAll();
                    List<QuestionItem> questionItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        if(item != null) {
                            item.setPostID(data.getKey());
                            daoUser.getUserInfo(item.getUserID())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            var user = snapshot.getValue(User.class);
                                            if (user != null) {
                                                item.setNickname(user.getNickname())
                                                        .setUri(user.getProfilePicture());

                                                questionAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }
                        questionItemList.add(item);
                    }
                    if(!query.trim().isEmpty()) {
                        questionAdapter.setQuestions(questionItemList
                                .stream()
                                .filter(e ->
                                        e.getQuestionTitle().contains(query)
                                                || e.getQuestionDescription().contains(query))
                                .collect(Collectors.toList()));

                    } else {
                        questionAdapter.setQuestions(questionItemList);
                    }
                    questionAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadReplies(String key) {
        replyAdapter.clearAll();
        dao.getReplies(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var reply = data.getValue(ReplyItem.class);

                        if (reply != null) {
                            daoUser.getUserInfo(reply.getUserID())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            var user = snapshot.getValue(User.class);
                                            if (user != null) {
                                                reply.setNickname(user.getNickname())
                                                        .setUri(user.getProfilePicture());

                                                replyAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                        replyItemList.add(reply);
                    }
                    replyAdapter.setReplies(replyItemList);
                    replyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.
                insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void updateMenstruationNotification() {
        if(menstruationReminderSwitch.isChecked()) {
            Calendar calendar = Calendar.getInstance();
            var date = LocalDate
                    .parse(viewModel
                            .getLastMenstruationSaved(daoUser.getCurrentUid())
                            .getStartDay());
            calendar.set(Calendar.YEAR, date.getYear());
            calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
            calendar.add(Calendar.MINUTE, 5);
            calendar.set(Calendar.SECOND, 0);
            notificationService.updateMenstruationNotification(calendar);
        }
    }

}