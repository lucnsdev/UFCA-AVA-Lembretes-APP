package lucns.avareminders.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lucns.avareminders.R;
import lucns.avareminders.utils.Annotator;
import lucns.avareminders.utils.Notify;
import lucns.avareminders.utils.Utils;

public class PermissionActivity extends Activity {

    private String[] PERMISSIONS_RUNTIME = new String[]{
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    private void requestPermissions() {
        String[] deniedPermissions = getDeniedPermissions();
        if (deniedPermissions.length > 0) {
            requestPermissions(deniedPermissions, 1234);
            return;
        }
        Annotator annotator = new Annotator("user", "Authentication.json");
        if (annotator.exists()) {
            long timestamp;
            try {
                JSONObject jsonObject = new JSONObject(annotator.getContent());
                timestamp = jsonObject.getLong("timestamp");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            if (Utils.hasInternetConnection() && System.currentTimeMillis() - timestamp > 59 * 60 * 1000) { // 59 minutes
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("force_login", true);
                startActivity(intent);
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    private String[] getDeniedPermissions() {
        List<String> permissions = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();
        for (String permission : PERMISSIONS_RUNTIME) {
            if (packageManager.checkPermission(permission, packageName) != PackageManager.PERMISSION_GRANTED)
                permissions.add(permission);
        }
        return permissions.toArray(new String[permissions.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Notify.showToast(permissions[i]);
                finish();
                break;
            }
        }
    }
}