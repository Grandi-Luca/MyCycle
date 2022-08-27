package com.example.mycycle;

import static com.example.mycycle.Utils.isUserLogin;
import static com.example.mycycle.Utils.showDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment implements QuestionAdapter.OnItemListener {
    FirebaseDAOUser daoUser;
    private DAOPost dao;

    RemindersInterface notificationService;
    SwitchMaterial medicineReminderSwitch;

    private TextView nickname, menstruationDuration, periodDuration;
    private ImageView profilePicture;
    private SwipeRefreshLayout swipeRefreshLayout;
    private QuestionAdapter questionAdapter;
    private ReplyAdapter replyAdapter;

    public static User currentUser;

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
        this.questionAdapter = new QuestionAdapter(getActivity(), this);
        this.replyAdapter = new ReplyAdapter(getActivity());
        this.dao = new DAOPost();

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
                        Objects.requireNonNull(item).setPostID(data.getKey());
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

        SwipeRefreshLayout swipeRefreshLayout  = dialog.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadReplies(item.getPostID());
            swipeRefreshLayout.setRefreshing(false);
        });

        loadReplies(item.getPostID());

    }

    private void reloadReplies(String key) {
        replyAdapter.clearAll();

        dao.getReplies(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        replyItemList.add(data.getValue(ReplyItem.class));
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

    private void loadReplies(String key) {
        replyAdapter.clearAll();
        dao.getReplies(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        replyItemList.add(data.getValue(ReplyItem.class));
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
}