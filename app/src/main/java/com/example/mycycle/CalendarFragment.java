package com.example.mycycle;

import static com.example.mycycle.CalendarUtils.daysInMonthArray;
import static com.example.mycycle.CalendarUtils.monthYearFromDate;
import static com.example.mycycle.CalendarUtils.selectedDate;
import static com.example.mycycle.MainActivity.currentUser;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.adapter.CalendarAdapter;
import com.example.mycycle.adapter.CalendarAdapter.OnItemListener;
import com.example.mycycle.adapter.NoteAdapter;
import com.example.mycycle.model.Menstruation;
import com.example.mycycle.model.Note;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.example.mycycle.repo.NoteRepository;
import com.example.mycycle.viewModel.RepositoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalendarFragment extends Fragment implements OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private boolean isAllFABVisible, importance;

    private NoteAdapter adapter;

    private FloatingActionButton noteFAB;
    private FloatingActionButton infoFAB;

    private NoteRepository noteRepository;

    private RepositoryViewModel mViewModel;
    private FirebaseDAOUser uDAO;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        noteRepository = new NoteRepository(getActivity().getApplication());
        mViewModel = MainActivity.mViewModel;
        uDAO = new FirebaseDAOUser();

        adapter = new NoteAdapter();

        initWidgets(view);

            Menstruation menstruation = mViewModel
                    .getLastMenstruationSaved();
            LocalDate date;
            if (menstruation == null) {
                if(currentUser != null) {
                    date = LocalDate.parse(currentUser.getFirstDay(), DateTimeFormatter.ofPattern("d/M/yyyy"));
                    CalendarUtils.lastMenstruation = date;
                }
            } else {
                date = LocalDate.parse(menstruation.getStartDay());
                CalendarUtils.lastMenstruation = date;
            }

        CalendarUtils.selectedDate = Optional.ofNullable(LocalDate.now());

        updateMonthView(LocalDate.now());

        return view;
    }

    private void initWidgets(View v) {
        calendarRecyclerView = v.findViewById(R.id.calendarRecyclerView);
        monthYearText = v.findViewById(R.id.monthYearTV);

        infoFAB = v.findViewById(R.id.infoFab);
        noteFAB = v.findViewById(R.id.noteFab);

        hideAll();
        isAllFABVisible = false;

        v.findViewById(R.id.extendedFab).setOnClickListener(view -> {
            if(!isAllFABVisible){
                showAll();
            } else {
                hideAll();
            }
            isAllFABVisible = !isAllFABVisible;
        });

        v.findViewById(R.id.prevMonth).setOnClickListener(view -> {
            if(!isAllFABVisible){
                this.previousMonthAction();
            }
        });

        v.findViewById(R.id.nextMonth).setOnClickListener(view -> {
            if(!isAllFABVisible){
                this.nextMonthAction();
            }
        });

        noteFAB.setOnClickListener(view -> {
            Dialog dialog = new Dialog(getActivity());
            importance = false;
            Utils.showDialog(dialog,
                    R.layout.add_new_note_dialog,
                    R.style.dialog_down_top_swipe_animation);

            ImageButton importantSwitch = dialog.findViewById(R.id.importance);
            importantSwitch.setOnClickListener(d -> {
                importance = !importance;
                importantSwitch.setImageResource(importance ? R.drawable.ic_round_star_24 :
                        R.drawable.ic_round_star_outline_24);
            });

            dialog.findViewById(R.id.addButton).setOnClickListener(v1 -> {
                TextView text = dialog.findViewById(R.id.description);
                if(text.getText().toString().isEmpty()) {
                    return;
                }
                var note = new Note()
                        .setUserID(mViewModel.getUserID())
                        .setNote(text.getText().toString())
                        .setDate(selectedDate.get().toString());
                if(importance) {
                    note.setImportance(1);
                }
                mViewModel.insertNote(note);
                dialog.dismiss();
                updateMonthView(selectedDate.get());
            });
            hideAll();
            isAllFABVisible = false;
        });

        infoFAB.setOnClickListener(view -> {

            Dialog dialog = new Dialog(getActivity());
            Utils.showDialog(dialog, R.layout.list_dialog, R.style.dialog_down_top_swipe_animation);

            RecyclerView recyclerView = dialog.findViewById(R.id.recyclerList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            var noElementText = (TextView) dialog.findViewById(R.id.listEmptyMsg);
            noElementText.setText(R.string.no_note);
            boolean isLoadSuccess = loadNotes();
            dialog.findViewById(R.id.listEmptyMsg).setVisibility(!isLoadSuccess ? View.VISIBLE : View.GONE);

            hideAll();
            isAllFABVisible = false;
        });
    }

    private void updateMonthView(LocalDate date) {
        monthYearText.setText(monthYearFromDate(date));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(date);

        var listActual =
                mViewModel.getMonthlyMenstruationEvent(uDAO.getCurrentUid(),
                        date);

        List<Menstruation> listPredicted = new ArrayList<>();
        if(currentUser != null) {
            listPredicted = mViewModel.getPredictedMenstruation(currentUser, date);
        }
        var listNote = mViewModel.getNotes();

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth,
                this, listActual, listPredicted, listNote);
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(requireContext().getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction() {
        LocalDate date = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

        CalendarUtils.selectedDate = Optional.ofNullable(date.minusMonths(1));
        if(currentUser != null) {
            var menstruation = mViewModel
                    .getLastMenstruationSaved();
            if(menstruation != null) {
                CalendarUtils.lastMenstruation =
                        LocalDate.parse(menstruation.getStartDay());
            }
        }
        updateMonthView(date.minusMonths(1));
    }

    public void nextMonthAction() {
        LocalDate date = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

        CalendarUtils.selectedDate = Optional.ofNullable(date.plusMonths(1));
        updateMonthView(date.plusMonths(1));
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null && !isAllFABVisible) {
            CalendarUtils.selectedDate = Optional.of(date);
            updateMonthView(date);
        }
    }

    private boolean loadNotes() {
        if(selectedDate.isPresent()) {
            var notes = mViewModel.getNotes()
                    .stream()
                    .filter(e -> LocalDate.parse(e.getDate()).isEqual(selectedDate.get()))
                    .collect(Collectors.toList());
            adapter.setNotes(notes);
            return notes.size() > 0;
        }
        return false;
    }

    private void hideAll() {
        infoFAB.hide();
        noteFAB.hide();
    }

    private void showAll() {
        infoFAB.show();
        noteFAB.show();
    }

}