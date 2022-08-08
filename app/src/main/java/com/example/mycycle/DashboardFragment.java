package com.example.mycycle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        view.findViewById(R.id.primaryCard).setOnClickListener(v -> ((MainActivity) getActivity()).replaceFragment(new CalendarFragment()));
        view.findViewById(R.id.secondaryCard).setOnClickListener(v -> ((MainActivity) getActivity()).replaceFragment(new CalendarFragment()));

        return view;
    }
}