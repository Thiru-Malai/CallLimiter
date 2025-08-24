package com.thirumalai.calllimiter.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thirumalai.calllimiter.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Intro #newInstance} factory method to
 * create an instance of this fragment.
 */
public class Intro extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false);
    }
}