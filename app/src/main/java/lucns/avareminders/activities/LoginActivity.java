package lucns.avareminders.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import lucns.avareminders.R;
import lucns.avareminders.rest_api.ava.LoginRestApi;
import lucns.avareminders.rest_api.ava.ResponseCallback;
import lucns.avareminders.utils.Annotator;
import lucns.avareminders.utils.AppPreferences;
import lucns.avareminders.utils.Notify;
import lucns.avareminders.utils.Utils;

public class LoginActivity extends Activity {

    private PopupMenu popupMenu;
    private EditText editTextUser, editTextPassword;
    private LoginRestApi loginRestApi;
    private CustomDialog dialog;
    private boolean forcingLogin, passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dialog = new CustomDialog(this);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.textVersion)).setText(String.format(Locale.getDefault(), getString(R.string.format_version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        RelativeLayout rootLogging = findViewById(R.id.buttonLoading);
        Button button = findViewById(R.id.button);
        ImageButton buttonVisibility = findViewById(R.id.buttonVisibility);
        TextView textError = findViewById(R.id.textError);
        editTextUser = findViewById(R.id.editTextUser);
        editTextPassword = findViewById(R.id.editTextPassword);
        CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setChecked(AppPreferences.getBoolean("not_request_again"));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPreferences.setBoolean("not_request_again", isChecked);
            }
        });

        loginRestApi = new LoginRestApi(new ResponseCallback() {

            @Override
            public void onError(int responseCode) {
                Utils.vibrate();
                buttonVisibility.setVisibility(View.VISIBLE);
                rootLogging.setVisibility(View.INVISIBLE);
                button.setVisibility(View.VISIBLE);
                checkBox.setEnabled(true);
                editTextUser.setEnabled(true);
                editTextPassword.setEnabled(true);
                //Notify.showErrorToast(responseCode);
                dialog.showDialogConnectionFailure();
                if (forcingLogin) {
                    findViewById(R.id.rootLogging).setVisibility(View.INVISIBLE);
                    findViewById(R.id.rootForm).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onUnauthenticated() {
                Utils.vibrate();
                buttonVisibility.setVisibility(View.VISIBLE);
                textError.setVisibility(View.VISIBLE);
                rootLogging.setVisibility(View.INVISIBLE);
                button.setVisibility(View.VISIBLE);
                checkBox.setEnabled(true);
                editTextUser.setEnabled(true);
                editTextPassword.setEnabled(true);
            }

            @Override
            public void onFinish() {
                Utils.vibrate();
                if (!forcingLogin) {
                    try {
                        JSONObject jsonLogin = new JSONObject();
                        jsonLogin.put("username", editTextUser.getText().toString());
                        if (checkBox.isChecked()) jsonLogin.put("password", editTextPassword.getText().toString());
                        new Annotator("user", "User.json").setContent(jsonLogin.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
        Annotator annotator = new Annotator("user", "User.json");
        if (annotator.exists()) {
            boolean hasPassword = false;
            try {
                JSONObject jsonObject = new JSONObject(annotator.getContent());
                editTextUser.setText(jsonObject.getString("username"));
                if (jsonObject.has("password")) {
                    hasPassword = true;
                    editTextPassword.setText(jsonObject.getString("password"));
                    button.setEnabled(true);
                    buttonVisibility.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = getIntent();
            if (hasPassword && intent.hasExtra("force_login")) {
                forcingLogin = true;
                findViewById(R.id.rootLogging).setVisibility(View.VISIBLE);
                login();
            } else {
                findViewById(R.id.rootForm).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.rootForm).setVisibility(View.VISIBLE);
        }

        ImageButton buttonMenu = findViewById(R.id.buttonMenu);
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.button) {
                    if (!Utils.hasInternetConnection()) {
                        Notify.showToast(R.string.error_no_internet);
                        return;
                    }
                    button.setVisibility(View.INVISIBLE);
                    rootLogging.setVisibility(View.VISIBLE);
                    buttonVisibility.setVisibility(View.INVISIBLE);
                    checkBox.setEnabled(false);
                    editTextUser.setEnabled(false);
                    editTextPassword.setEnabled(false);
                    login();
                } else if (view.getId() == R.id.buttonMenu) {
                    popupMenu.show();
                } else if (view.getId() == R.id.buttonVisibility) {
                    if (passwordVisible) {
                        passwordVisible = false;
                        editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        buttonVisibility.setImageResource(R.drawable.icon_visibility_off);
                    } else {
                        passwordVisible = true;
                        editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        buttonVisibility.setImageResource(R.drawable.icon_visibility);
                    }
                }
            }
        };
        buttonVisibility.setOnClickListener(onClick);
        button.setOnClickListener(onClick);
        buttonMenu.setOnClickListener(onClick);
        popupMenu = new PopupMenu(this, buttonMenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_about) {
                    dialog.showDialogAbout();
                } else if (itemId == R.id.menu_contact) {
                    dialog.showContactDialog();
                }
                return true;
            }
        });
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_login, popupMenu.getMenu());

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //buttonVisibility.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                textError.setVisibility(View.INVISIBLE);
                button.setEnabled(s.toString().length() > 7 && editTextUser.getText().toString().length() > 12);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        button.setOnClickListener(onClick);

        editTextUser.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;
            String old = "";
            final String FORMAT_CPF = "###.###.###-##";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textError.setVisibility(View.INVISIBLE);
                boolean isOk = s.toString().length() == 14;
                button.setEnabled(isOk && editTextPassword.getText().toString().length() > 7);
                String str = unmask(s.toString());
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    if (isOk) {
                        loginRestApi.cancel();
                    }
                    return;
                }
                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : FORMAT_CPF.toCharArray()) {
                    if ((m != '#' && str.length() > old.length()) || (m != '#' && str.length() < old.length() && str.length() != i)) {
                        mascara.append(m);
                        continue;
                    }

                    try {
                        mascara.append(str.charAt(i));
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                editTextUser.setText(mascara.toString());
                editTextUser.setSelection(mascara.length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            public String unmask(String s) {
                return s.replaceAll("[^0-9]*", "");
            }
        });
    }

    private void login() {
        String user = editTextUser.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        loginRestApi.request(user, password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginRestApi.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
        popupMenu.dismiss();
    }
}