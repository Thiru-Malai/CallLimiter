package com.thirumalai.calllimiter.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        View view =  inflater.inflate(R.layout.fragment_intro, container, false);

        TextView termsText = view.findViewById(R.id.terms_conditions);

        String text = "By continuing, you agree to our <a href='https://github.com/Thiru-Malai/CallLimiter/blob/master/TermsAndConditions.md'>Terms & Conditions</a>.";
        termsText.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        termsText.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}