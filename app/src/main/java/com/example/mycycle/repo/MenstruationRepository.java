package com.example.mycycle.repo;

import android.app.Application;
import android.content.Context;

import com.example.mycycle.database.MyDatabase;
import com.example.mycycle.model.Menstruation;

import java.util.List;

public class MenstruationRepository {

    private final MenstruationDAO menstruationDAO;

    public MenstruationRepository(Application application) {
        MyDatabase database = MyDatabase.getDatabase(application);
        menstruationDAO = database.menstruationDAO();
    }

    public void insert(Menstruation menstruation) {
        MyDatabase.executor.execute(() ->
                menstruationDAO.insert(menstruation));
    }

    public Menstruation getLastMenstruation(String userID) {
        return menstruationDAO.getLastMenstruation(userID);
    }

    public List<Menstruation> getMonthlyMenstruation(String userID) {
        return menstruationDAO.getMonthlyMenstruation(userID);
    }

}
