package com.example.eduvia;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleWatcher implements TextWatcher {
    private final Runnable callback;
    public SimpleWatcher(Runnable callback) {
        this.callback = callback;
    }
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        callback.run();
    }
    @Override public void afterTextChanged(Editable s) {}
}
