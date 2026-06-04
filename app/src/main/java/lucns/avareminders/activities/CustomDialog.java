package lucns.avareminders.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import lucns.avareminders.R;

public class CustomDialog {

    private Activity activity;

    public CustomDialog(Activity activity) {
        this.activity = activity;
    }

    private Dialog dialog;

    public Dialog generateDialog(int layoutId, boolean isCancelable) {
        dismiss();
        dialog = new Dialog(activity, R.style.DialogTheme);
        dialog.setCancelable(isCancelable);
        dialog.setContentView(layoutId);
        return dialog;
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }


    public void showDialogConnectionFailure() {
        showConsentDialog(R.drawable.icon_filled_cancel, R.string.connection_failure, R.string.connection_failure_description);
    }

    public void showConsentDialog(int icon, int title, int description) {
        Dialog d = generateDialog(R.layout.dialog_failure, false);
        ImageView imageIcon = d.findViewById(R.id.imageIcon);
        TextView textTitle = d.findViewById(R.id.textTitle);
        TextView textDescription = d.findViewById(R.id.textDescription);
        imageIcon.setImageResource(icon);
        textTitle.setText(title);
        textDescription.setText(description);
        d.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    public void showDialogAbout() {
        Dialog d = generateDialog(R.layout.dialog_about, false);
        d.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    public void showContactDialog() {
        Dialog d = generateDialog(R.layout.dialog_contact, true);
        int size = 72;
        ImageView imageInstagram = d.findViewById(R.id.imageInstagram);
        ImageView imageGmail = d.findViewById(R.id.imageGmail);
        ImageView imageLinkedin = d.findViewById(R.id.imageLinkedin);
        ImageView imageGithub = d.findViewById(R.id.imageGithub);
        imageInstagram.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.instagram), size, size, false));
        imageGmail.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.gmail), size, size, false));
        imageLinkedin.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.linkedin), size, size, false));
        imageGithub.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.github), size, size, false));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                if (view.getId() == R.id.button) {
                    d.dismiss();
                    return;
                } else if (view.getId() == R.id.imageInstagram) {
                    i.setData(Uri.parse("https://www.instagram.com/lucns/"));
                } else if (view.getId() == R.id.imageGmail) {
                    i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:"));
                    i.putExtra(Intent.EXTRA_EMAIL, "lucns2906@gmail.com");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Ava Reminders. Criticas & Sugestões.");
                } else if (view.getId() == R.id.imageLinkedin) {
                    i.setData(Uri.parse("https://www.linkedin.com/in/lucns/"));
                } else if (view.getId() == R.id.imageGithub) {
                    i.setData(Uri.parse("https://github.com/lucnsdev/Sobre-o-Lucas"));
                }
                d.dismiss();
                activity.startActivity(i);
            }
        };
        imageInstagram.setOnClickListener(onClickListener);
        imageGmail.setOnClickListener(onClickListener);
        imageLinkedin.setOnClickListener(onClickListener);
        imageGithub.setOnClickListener(onClickListener);
        d.findViewById(R.id.button).setOnClickListener(onClickListener);
        d.show();
    }
}
