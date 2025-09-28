package com.example.eduvia;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

public class Loader {
    private Dialog dialog;

    public Loader(Context context) {
        dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loader, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
