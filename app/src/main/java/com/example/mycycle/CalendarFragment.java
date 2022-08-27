package com.example.mycycle;

import static com.example.mycycle.CalendarUtils.daysInMonthArray;
import static com.example.mycycle.CalendarUtils.monthYearFromDate;
import static com.example.mycycle.CalendarUtils.selectedDate;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.adapter.CalendarAdapter;
import com.example.mycycle.adapter.NoteAdapter;
import com.example.mycycle.adapter.CalendarAdapter.OnItemListener;
import com.example.mycycle.model.Note;
import com.example.mycycle.repo.NoteRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class CalendarFragment extends Fragment implements OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private boolean isAllFABVisible;

    private NoteAdapter adapter;

    private FloatingActionButton noteFAB;
    private FloatingActionButton infoFAB;
    private FloatingActionButton editFAB;

    NoteRepository repository;

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

        repository = new NoteRepository(getActivity().getApplication());
        adapter = new NoteAdapter();

        initWidgets(view);

        CalendarUtils.selectedDate = Optional.ofNullable(LocalDate.now());
        updateMonthView(LocalDate.now());

        return view;
    }

    private void initWidgets(View v) {
        calendarRecyclerView = v.findViewById(R.id.calendarRecyclerView);
        monthYearText = v.findViewById(R.id.monthYearTV);

        editFAB = v.findViewById(R.id.editFab);
        infoFAB = v.findViewById(R.id.infoFab);
        noteFAB = v.findViewById(R.id.noteFab);

        infoFAB.setVisibility(View.GONE);
        noteFAB.setVisibility(View.GONE);
        editFAB.setVisibility(View.GONE);
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
            Utils.showDialog(dialog,
                    R.layout.add_new_note_dialog,
                    R.style.dialog_vertical_swipe_animation);

            dialog.findViewById(R.id.addButton).setOnClickListener(v1 -> {
                TextView text = dialog.findViewById(R.id.description);
                repository.insertNote(new Note()
                        .setNote(text.getText().toString())
                        .setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                dialog.dismiss();
            });
            hideAll();
            isAllFABVisible = false;
        });

        infoFAB.setOnClickListener(view -> {

            Dialog dialog = new Dialog(getActivity());
            Utils.showDialog(dialog, R.layout.list_dialog, R.style.dialog_vertical_swipe_animation);

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

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext().getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction() {
        LocalDate date = CalendarUtils.selectedDate.orElseGet(LocalDate::now);

        CalendarUtils.selectedDate = Optional.ofNullable(date.minusMonths(1));
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
            var notes = repository.getNotes(selectedDate.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            adapter.setNotes(notes);
            return notes.size() > 0;
        }
        return false;
    }

    private void hideAll() {
        infoFAB.hide();
        noteFAB.hide();
        editFAB.hide();
    }

    private void showAll() {
        infoFAB.show();
        noteFAB.show();
        editFAB.show();
    }

}