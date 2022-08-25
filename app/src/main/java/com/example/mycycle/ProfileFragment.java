package com.example.mycycle;

import static com.example.mycycle.Utils.isUserLogin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.mycycle.adapter.QuestionAdapter;
import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.RemindersInterface;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.DAOPost;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    FirebaseDAOUser daoUser;
    private DAOPost dao;
    private QuestionAdapter adapter;

    RemindersInterface notificationService;
    SwitchMaterial medicineReminderSwitch;

    TextView nickname, menstruationDuration, periodDuration;
    ImageView profilePicture;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static User currentUser;

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

        daoUser = new FirebaseDAOUser();
        this.dao = new DAOPost();
        this.adapter = new QuestionAdapter(getActivity(), R.layout.personal_question_item);

        notificationService = new NotificationService(requireContext());

        profilePicture = view.findViewById(R.id.profileImage);
        nickname = view.findViewById(R.id.nickname);
        menstruationDuration = view.findViewById(R.id.menstruationDuration);
        periodDuration = view.findViewById(R.id.duration_period);

        loadUserData();

        if(!isUserLogin()){
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            currentUser = null;

            return null;
        }

        medicineReminderSwitch = view.findViewById(R.id.medicineReminderSwitch);
        medicineReminderSwitch.setChecked(notificationService.isMedicineReminderActive());
        medicineReminderSwitch.setOnClickListener(v -> {
            if(medicineReminderSwitch.isChecked()){
                showTimePickerDialog();
            } else {
                notificationService.cancelMedicineDailyNotification();
            }
        });

        SwitchMaterial menstruationReminderSwitch = view.findViewById(R.id.menstruationReminderSwitch);
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

        return view;
    }

    private void showTimePickerDialog(){
        var dialog = new Dialog(getActivity());
        showDialog(dialog, R.layout.time_picker_dialog, R.style.dialog_vertical_swipe_animation);

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

    private void showDialog(Dialog dialog, int layout, int animation) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(layout);
        dialog.getWindow().getAttributes().windowAnimations = animation;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        var btn_cancel = dialog.findViewById(R.id.closeButton);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPersonalPosts() {
        var dialog = new Dialog(getActivity());
        showDialog(dialog, R.layout.personal_posts_dialog, R.style.dialog_horizontal_swipe_animation);

        this.swipeRefreshLayout = dialog.findViewById(R.id.swipeLayout);
        loadData();

        RecyclerView questionRecyclerView = dialog.findViewById(R.id.questions);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(adapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadUserData() {
        if(currentUser != null) {
            var activity = getActivity();
            if(activity != null) {
                Glide.with(activity)
                        .load(currentUser.getProfilePicture())
                        .into(profilePicture);
            }
            nickname.setText(currentUser.getNickname());
            menstruationDuration.setText(currentUser.getDurationMenstruation() != 1
                    ? currentUser.getDurationMenstruation() + " giorni"
                    : currentUser.getDurationMenstruation() + " giorno");
            periodDuration.setText(currentUser.getDurationPeriod() != 1
                    ? currentUser.getDurationPeriod() + " giorni"
                    : currentUser.getDurationPeriod() + " giorno");
        } else {
            daoUser.getUserInfo().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    currentUser = user;
                    if (!Objects.requireNonNull(user).getProfilePicture().isEmpty()) {
                        // load profile picture
                        var activity = getActivity();
                        if (activity != null) {
                            Glide.with(activity)
                                    .load(user.getProfilePicture())
                                    .into(profilePicture);
                        }
                    }
                    nickname.setText(user.getNickname());
                    menstruationDuration.setText(user.getDurationMenstruation() != 1
                            ? user.getDurationMenstruation() + " giorni"
                            : user.getDurationMenstruation() + " giorno");
                    periodDuration.setText(user.getDurationPeriod() != 1
                            ? user.getDurationPeriod() + " giorni"
                            : user.getDurationPeriod() + " giorno");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void loadData() {
        this.dao.get(null).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(true);
                if(snapshot.exists()) {
                    adapter.clearAll();
                    List<QuestionItem> questionItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        Objects.requireNonNull(item).setPostID(data.getKey());
                        questionItemList.add(item);


                        // get replies
                        dao.getReplies(item.getPostID()).addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                var replies = new ArrayList<ReplyItem>();
                                if (snapshot.exists()) {
                                    replies.add(snapshot.getValue(ReplyItem.class));
                                }
                                item.setQuestionReplies(replies);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    adapter.setQuestions(questionItemList
                            .stream()
                            .filter(e ->
                                    e.getUserID().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                            .collect(Collectors.toList()));
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}