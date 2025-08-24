package com.thirumalai.calllimiter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.thirumalai.calllimiter.Fragment.Intro;
import com.thirumalai.calllimiter.Fragment.Permissions;

public class OnboardingAdapter extends FragmentStateAdapter {
    public OnboardingAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new Permissions();
        }
        return new Intro();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}