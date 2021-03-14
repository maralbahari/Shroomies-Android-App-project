package com.example.shroomies;

import android.content.Context;
import android.util.AttributeSet;


public class FloatSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    private float max = 1.0f;
    private float min = 0.50f;
    private float seekValueFloat = 0f;

    public FloatSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FloatSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatSeekBar(Context context) {
        super(context);
    }

    public void setSeekValueFloat(float value) {
        this.seekValueFloat = value;
    }

    public float getSeekValueFloat() {
        return this.seekValueFloat;
    }

    public void setPlus(float value) {
        setValue(value + min);
        seekValueFloat = value + min;
    }

    public void setMinus(float value) {
        setValue(value - min);
        seekValueFloat = value - min;
    }

    public float getMinFloat() {
        return this.min;
    }

    public float getMaxFloat() {
        return this.max;
    }

    public void setMaxFloat(float value) {
        this.max = value;
        setMax(Math.round(value));
    }

    public void setMinFloat(float value) {
        this.min = value;
    }

    public float getValue() {
        return Math.round((max - min) * ((float) getProgress() / (float) getMax()) + min);
    }

    public void setValue(float value) {
        setProgress(Math.round(value));
    }
}