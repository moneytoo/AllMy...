package com.smartmadsoft.xposed.aio;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

// from http://stackoverflow.com/questions/9220039/android-preferencescreen-title-in-two-lines

public class TwoLinePreference extends Preference {

    public TwoLinePreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public TwoLinePreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public TwoLinePreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView textView = (TextView) view.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setSingleLine(false);
        }
    }
}