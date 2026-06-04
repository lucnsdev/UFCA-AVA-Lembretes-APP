package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;

public class LoginRestApi extends RestApiBase {

    private final String urlLogin = "https://ava.ufca.edu.br/login/index.php";
    private final String urlSessionKey = "https://ava.ufca.edu.br/my/courses.php";

    public LoginRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        setContentType("application/x-www-form-urlencoded");
    }

    public void request(String user, String password) {
        user = user.replaceAll("\\.", "").replace("-", "");
        if (user.startsWith("0")) user = user.substring(1);
        final String username = user;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String html = requestGet(urlLogin);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    sendError(responseCode);
                    return;
                }

                if (headers == null || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                String cookie = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("MoodleSession")) cookie = value.substring(0, value.indexOf(";"));
                }
                if (cookie == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(cookie);
                String loginToken = getLoginTokenFromHtml(html);
                if (loginToken == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }

                String data = "logintoken=" + URLEncoder.encode(loginToken, StandardCharsets.UTF_8) + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
                requestPost(urlLogin, data);
                if (responseCode != HttpStatus.OK && responseCode != HttpStatus.SEE_OTHER) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Set-Cookie")) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onUnauthenticated();
                        }
                    });
                    return;
                }
                if (responseCode != HttpStatus.SEE_OTHER) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                cookie = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("MoodleSession")) {
                        cookie = value.substring(0, value.indexOf(";") + 1);
                    } else if (value.startsWith("MOODLEID1_")) {
                        String[] segments = value.split("=");
                        if (segments[1].startsWith("sodium")) cookie += "; " + value.substring(0, value.indexOf(";"));
                    }
                }
                if (cookie == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(cookie);

                html = requestGet(urlSessionKey);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                String sessionKey = getSessionFromHtml(html);
                if (sessionKey == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("timestamp", System.currentTimeMillis());
                    jsonObject.put("cookie", cookie);
                    jsonObject.put("session_key", sessionKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Annotator("user", "Authentication.json").setContent(jsonObject.toString());
                sendFinish();
            }
        });
        thread.start();
    }

    private String getSessionFromHtml(String html) {
        String[] lines = html.split("\n");
        for (String line : lines) {
            if (line.contains("sesskey")) {
                String[] segments = line.split("\"");
                return segments[13];
            }
        }
        return null;
    }

    private String getLoginTokenFromHtml(String html) {
        String[] lines = html.split("\n");
        for (String line : lines) {
            if (line.contains("logintoken")) {
                String[] segments = line.split("\"");
                return segments[5];
            }
        }
        return null;
    }
}
