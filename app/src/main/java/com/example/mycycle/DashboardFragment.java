package com.example.mycycle;

import static com.example.mycycle.MainActivity.currentUser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mycycle.model.Menstruation;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
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

        if(currentUser != null) {
            var last = vModel.getLastMenstruationSaved();
            if(last == null) {
                last = new Menstruation().setStartDay(LocalDate
                        .parse(currentUser.getFirstDay(),
                                DateTimeFormatter.ofPattern("d/M/yyyy")).toString());
            }

            var difference = (LocalDate.now()
                    .until(LocalDate.parse(last.getStartDay()), ChronoUnit.DAYS));


            var nextMenstruationInfo = vModel.getNextMenstruationInfo(currentUser);
            TextView subTitle = view.findViewById(R.id.subTitlePrimaryCard);

            if (nextMenstruationInfo != null) {
                String primaryCardSubTitle = LocalDate.parse(nextMenstruationInfo.getStartDay()).getDayOfMonth() +
                        " " + LocalDate.parse(nextMenstruationInfo.getStartDay()).getMonth().toString() + " - Prossime mestruazioni";
                subTitle.setText(primaryCardSubTitle);

                MaterialTextView title = view.findViewById(R.id.titlePrimaryCard);
                String primaryCardTitle;

                if (Math.abs(difference) > currentUser.getDurationMenstruation()
                        || last.getStartDay().equals(nextMenstruationInfo.getStartDay())) {
                    difference = LocalDate.now()
                            .until(LocalDate
                                    .parse(nextMenstruationInfo.getStartDay()), ChronoUnit.DAYS);
                    primaryCardTitle = Math.abs(difference) + (Math.abs(difference) == 1 ?
                            " gg rimasto" :
                            (Math.abs(difference) == 0 ? "1° giorno" : "gg rimasti"));
                } else {
                    primaryCardTitle = (Math.abs(difference) + 1) + "° giorno";
                }
                title.setText(primaryCardTitle);
            }
        }

        if(activity != null && currentUser != null) {
            var notes = vModel.getImportantNote()
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