package lucns.avareminders.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.VibratorManager;

public class Utils {

    private static VibratorManager vibrator;

    static {
        init();
    }

    private static void init() {
        Context context = App.getContext();
        vibrator = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
    }

    public static void vibrate(int duration) {
        if (duration > 0) {
            vibrator.cancel();
            vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)));
        }
    }

    public static void vibrate() {
        vibrator.cancel();
        vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)));
    }

    public static boolean hasInternetConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivity.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivity.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static int dpToPx(int dp) {
        float density = App.getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public static int pxToDp(int px) {
        float density =  App.getContext().getResources().getDisplayMetrics().density;
        return (int) (px / density);
    }

    public static String removeEmojis(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int codePoint = input.codePointAt(i);
            // Skip characters that fall into the "Other Symbol" category (often emojis)
            if (Character.getType(codePoint) != Character.OTHER_SYMBOL) {
                sb.appendCodePoint(codePoint);
            }
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++; // Increment again for surrogate pairs
            }
        }
        return sb.toString();
    }
}
