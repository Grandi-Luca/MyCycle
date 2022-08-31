package com.example.mycycle;

import static com.example.mycycle.MainActivity.currentUser;
import static com.example.mycycle.MainActivity.mViewModel;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mycycle.model.Menstruation;
import com.example.mycycle.model.Note;
import com.example.mycycle.repo.NoteRepository;
import com.example.mycycle.viewModel.RepositoryViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        var vModel = MainActivity.mViewModel;

        var activity = getActivity();
        MaterialCardView cardView = view.findViewById(R.id.primaryCard);
        cardView.setOnClickListener(v ->{
            if(activity != null) {
                ((MainActivity) activity).replaceFragment(new CalendarFragment());
            }
        });

        view.findViewById(R.id.secondaryCard).setOnClickListener(v ->{
            if(activity != null) {
                ((MainActivity) activity).replaceFragment(new CalendarFragment());
            }
        });

        var next = vModel.getNextMenstruationInfo(currentUser);

        if(next == null) {
            vModel.getPredictedMenstruation(currentUser, LocalDate.now().plusMonths(1));
            next = vModel.getNextMenstruationInfo(currentUser);
        }

        var last = vModel.getLastMenstruationSaved(currentUser.getUserID());
        if(last == null) {
            last = new Menstruation().setStartDay(currentUser.getFirstDay());
        }

        var difference = LocalDate.now()
                .compareTo(LocalDate.parse(last.getStartDay()));

        MaterialTextView title = view.findViewById(R.id.titlePrimaryCard);
        String primaryCardTitle = (difference + 1) + "° giorno";
        title.setText(primaryCardTitle);

        TextView subTitle = view.findViewById(R.id.subTitlePrimaryCard);
        String primaryCardSubTitle = LocalDate.parse(next.getStartDay()).getDayOfMonth() +
                " " + LocalDate.parse(next.getStartDay()).getMonth().toString() + " - Prossime mestruazioni";
        subTitle.setText(primaryCardSubTitle);

        if(activity != null) {
            NoteRepository noteRepository = new NoteRepository(activity.getApplication());
            var notes =noteRepository.getImportantNote()
                    .stream()
                    .filter(n -> LocalDate.parse(n.getDate()).isAfter(LocalDate.now()))
                    .collect(Collectors.toList());
            if(notes.size() > 0) {
                var nextNote = notes.get(0);
                TextView event = view.findViewById(R.id.event);
                TextView secTitle = view.findViewById(R.id.secondaryTitle);
                String e = LocalDate.parse(nextNote.getDate()).getDayOfMonth() +
                        " " + LocalDate.parse(nextNote.getDate()).getMonth().toString();
                secTitle.setText(e);
                event.setText("Prossimo evento");
            }
        }

        return view;
    }
}