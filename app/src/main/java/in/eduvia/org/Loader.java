package in.eduvia.org;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Loader {

    private Dialog dialog;
    private AppCompatActivity activity;

    public Loader(AppCompatActivity activity) {
        this.activity = activity;

        dialog = new Dialog(activity);
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_loader, null);

        dialog.setContentView(view);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void show() {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void release() {
        dismiss();
        dialog = null;
        activity = null;
    }
}
