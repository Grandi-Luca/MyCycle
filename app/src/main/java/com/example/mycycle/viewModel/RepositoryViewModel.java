package com.example.mycycle.viewModel;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.mycycle.CalendarUtils;
import com.example.mycycle.model.Menstruation;
import com.example.mycycle.model.Note;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.example.mycycle.repo.MenstruationRepository;
import com.example.mycycle.repo.NoteRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryViewModel extends AndroidViewModel {

    private final MenstruationRepository menstruationRepository;
    private final NoteRepository noteRepository;
    private final Set<Menstruation> mPredict;
    private final FirebaseDAOUser uDAO;

    public RepositoryViewModel(@NonNull Application application) {
        super(application);
        menstruationRepository = new MenstruationRepository(application);
        noteRepository = new NoteRepository(application);
        mPredict = new HashSet<>();
        uDAO = new FirebaseDAOUser();
    }

    public void insertNewMenstruation(Menstruation menstruation) {
        menstruationRepository.insert(menstruation);
    }

    public void deleteAllMenstruationEvent() {
        menstruationRepository.deleteAll(uDAO.getCurrentUid());
    }

    public List<Menstruation> getMonthlyMenstruationEvent(String userID, LocalDate date) {
        return menstruationRepository.getMonthlyMenstruation(userID)
                .stream().filter(e ->
                        // next month
                        (LocalDate.parse(e.getStartDay()).minusMonths(1).getMonthValue() == date.getMonthValue()
                                && LocalDate.parse(e.getStartDay()).minusMonths(1).getYear() == date.getYear()) ||
                                // previous month
                                (LocalDate.parse(e.getStartDay()).plusMonths(1).getMonthValue() == date.getMonthValue()
                                        && LocalDate.parse(e.getStartDay()).plusMonths(1).getYear() == date.getYear()) ||
                                // actual month
                                (LocalDate.parse(e.getStartDay()).getMonthValue() == date.getMonthValue()
                                        && LocalDate.parse(e.getStartDay()).getYear() == date.getYear()))
                .collect(Collectors.toList());
    }

    public List<Menstruation> getPredictedMenstruation(User user, LocalDate date) {

        if(CalendarUtils.lastMenstruation == null) {
            var menstruation = getLastMenstruationSaved();
            if(menstruation == null) {
                CalendarUtils.lastMenstruation = LocalDate
                        .parse(user.getFirstDay(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            } else {
                CalendarUtils.lastMenstruation =
                        LocalDate.parse(menstruation.getStartDay());
            }
        }
        var nextDate = CalendarUtils.lastMenstruation;
        if(!LocalDate.now().isBefore(CalendarUtils.lastMenstruation)) {
            nextDate = CalendarUtils.lastMenstruation.plusDays(user.getDurationPeriod());
        }

        while (((nextDate.minusMonths(1).getMonthValue() == date.getMonthValue()
                && nextDate.minusMonths(1).getYear() == date.getYear()) ||
                // previous month
                (nextDate.plusMonths(1).getMonthValue() == date.getMonthValue()
                        && nextDate.plusMonths(1).getYear() == date.getYear()) ||
                // actual month
                (nextDate.getMonthValue() == date.getMonthValue()
                        && nextDate.getYear() == date.getYear()))
                && !nextDate.isBefore(LocalDate.now())) {

            var m = new Menstruation()
                    .setStartDay(nextDate.toString())
                    .setLastDay(nextDate.plusDays(user.getDurationMenstruation() - 1).toString());

            mPredict.add(m);

            CalendarUtils.lastMenstruation = nextDate;
            nextDate = nextDate.plusDays(user.getDurationPeriod());
        }

        return new ArrayList<>(mPredict);
    }

    public void clearPrediction() {
        mPredict.clear();
    }

    public Menstruation getNextMenstruationInfo(User user) {
        var m = getLastMenstruationSaved();
        if(m != null) {
            var last = LocalDate.parse(m.getStartDay());
            for (var menstruation : mPredict) {

                if (last.plusDays(user.getDurationPeriod())
                        .isEqual(LocalDate.parse(menstruation.getStartDay()))) {
                    return menstruation;
                }
            }
        }
        return new Menstruation()
                .setStartDay(LocalDate
                        .parse(user.getFirstDay(), DateTimeFormatter.ofPattern("d/M/yyyy"))
                        .toString());
    }

    public int getPredictionSetSize() {
        return mPredict == null ? 0 : mPredict.size();
    }

    public String getUserID() {
        return uDAO.getCurrentUid();
    }

    public Menstruation getLastMenstruationSaved() {
        return menstruationRepository.getLastMenstruation(uDAO.getCurrentUid());
    }

    public List<Note> getNotes() {
        return noteRepository.getNotes(uDAO.getCurrentUid());
    }

    public List<Note> getImportantNote() {
        return noteRepository.getImportantNote(uDAO.getCurrentUid());
    }

    public void insertNote(Note note) {
        noteRepository.insertNote(note);
    }
}
