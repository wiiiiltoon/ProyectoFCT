package com.example.aplicaciongestindealumnos;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class HorizontalRadioGroup extends RadioGroup {

    public HorizontalRadioGroup(Context context) {
        super(context);
        init();
    }

    public HorizontalRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                View view = group.getChildAt(i);
                if (view instanceof RadioButton) {
                    RadioButton radioButton = (RadioButton) view;
                    radioButton.setChecked(radioButton.getId() == checkedId);
                }
            }
        });
    }
}