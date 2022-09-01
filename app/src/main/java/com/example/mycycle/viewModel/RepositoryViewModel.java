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
import java.util.ArrayList;
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

    public void insertNewEvent(Menstruation menstruation) {
        menstruationRepository.insert(menstruation);
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

        var nextDate = CalendarUtils.lastMenstruation.plusDays(user.getDurationPeriod());

        while (nextDate.getMonthValue() <= date.getMonthValue() &&
                nextDate.getYear() == date.getYear() && nextDate.isAfter(LocalDate.now())) {

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
        for (var menstruation : mPredict) {
            if (LocalDate.parse(getLastMenstruationSaved(user.getUserID())
                            .getStartDay()).plusDays(user.getDurationPeriod())
                    .isEqual(LocalDate.parse(menstruation.getStartDay()))) {
                return menstruation;
            }
        }
        return null;
    }

    public int getPredictionSetSize() {
        return mPredict == null ? 0 : mPredict.size();
    }

    public String getUserID() {
        return uDAO.getCurrentUid();
    }

    public Menstruation getLastMenstruationSaved(String userID) {
        return menstruationRepository.getLastMenstruation(userID);
    }

    public List<Note> getNotes() {
        return noteRepository.getNotes();
    }
}
