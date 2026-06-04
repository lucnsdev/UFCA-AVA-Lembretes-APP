package lucns.avareminders.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Locale;

import lucns.avareminders.R;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;

public class Notify {

    private static Toast toast;
    private static Handler main;

    static {
        init();
    }

    private static void init() {
        main = new Handler(Looper.getMainLooper());
    }

    public static void showToast(int resId) {
        showToast(App.getContext().getString(resId), Toast.LENGTH_SHORT);
    }

    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(int resId) {
        showToast(App.getContext().getString(resId), Toast.LENGTH_LONG);
    }

    public static void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    private static void showToast(String message, int type) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    //cancel();
                    showToast(message, type);
                }
            });
            return;
        }
        if (toast != null) toast.cancel();
        toast = Toast.makeText(App.getContext(), message, type);
        toast.show();
    }

    public static void showErrorToast(int code) {
        String message;
        switch (code) {
            case RestApiBase.CONNECT_TIMEOUT:
                message = App.getContext().getString(R.string.error_time_out);
                break;
            case RestApiBase.ERROR_APP_INTERNAL:
                message = App.getContext().getString(R.string.error_app_internal);
                break;
            case RestApiBase.ERROR_NO_STREAM_DATA:
                message = App.getContext().getString(R.string.error_no_data);
                break;
            case HttpStatus.BAD_REQUEST:
                message = App.getContext().getString(R.string.error_bad_request);
                break;
            case HttpStatus.NOT_FOUND:
                message = App.getContext().getString(R.string.error_not_found);
                break;
            case RestApiBase.ERROR_UNKNOWN_HOST:
                message = App.getContext().getString(R.string.error_no_internet);
                break;
            case RestApiBase.ERROR_TIMEOUT:
                message = App.getContext().getString(R.string.error_time_out);
                break;
            default:
                message = String.format(Locale.getDefault(), App.getContext().getString(R.string.format_error_request), code);
        }
        Notify.showLongToast(message);
    }

    public static void cancel() {
        if (toast != null) toast.cancel();
    }
}
