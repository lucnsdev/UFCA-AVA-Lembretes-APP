package lucns.avareminders.rest_api.internal;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import lucns.avareminders.rest_api.ava.ResponseCallback;

public class RestApiBase {

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36";
    public static final int OPERATION_INTERVAL_REQUESTERS = 2500;
    public static final int OPERATION_INTERVAL_MAJOR = 1000;
    public static final int OPERATION_INTERVAL_MINOR = 250;
    public static final int OPERATION_INTERVAL_FAST = 100;

    public static final int READ_TIMEOUT = 60000;
    public static final int CONNECT_TIMEOUT = 30000;

    public static final int ERROR_APP_INTERNAL = -1;
    public static final int ERROR_TIMEOUT = -10;
    public static final int ERROR_UNKNOWN_HOST = -11;
    public static final int ERROR_BAD_CONNECTION = -12;
    public static final int ERROR_NO_STREAM_DATA = -13;

    protected ResponseCallback responseCallback;
    protected Thread thread;
    protected int responseCode;
    protected String responseMessage;
    protected HttpURLConnection connection;
    protected String request;
    private String token, cookie, contentType;
    protected Map<String, List<String>> headers;

    public RestApiBase(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    public String getRawRequest() {
        return request;
    }

    public void cancel() {
        if (isThreadRunning()) thread.interrupt();
        if (connection != null) connection.disconnect();
    }

    public boolean isThreadRunning() {
        return thread != null && !thread.isInterrupted() && thread.getState() != Thread.State.TERMINATED;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    protected String requestGet(String url) {
        return request(url, "GET", null);
    }

    protected String requestPost(String url, String data) {
        return request(url, "POST", data);
    }

    protected String requestPut(String url, String data) {
        return request(url, "PUT", data);
    }

    protected String requestDelete(String url, String data) {
        return request(url, "DELETE", data);
    }

    private String request(String url, String method, String data) {
        responseCode = 0;
        InputStreamReader inputStreamReader = null;
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(false);
            if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
            if (cookie != null) connection.setRequestProperty("Cookie", cookie);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            if (data != null) {
                connection.setRequestProperty("Content-Type", contentType);
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.write(data.getBytes(StandardCharsets.UTF_8));
                dos.flush();
                dos.close();
            }

            connection.connect();
            responseCode = connection.getResponseCode();
            if (responseCode == 204) return "";
            headers = connection.getHeaderFields();
            inputStreamReader = new InputStreamReader(connection.getInputStream());
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            if (responseCode == 0) {
                if (e instanceof SocketTimeoutException) responseCode = ERROR_TIMEOUT;
                else if (e instanceof UnknownHostException) responseCode = ERROR_UNKNOWN_HOST;
                else if (e instanceof ConnectException) responseCode = ERROR_BAD_CONNECTION;
                else if (e instanceof NullPointerException) responseCode = ERROR_BAD_CONNECTION; // Channel is unrecoverably broken and will be disposed!
                else if (e instanceof FileNotFoundException) responseCode = HttpStatus.NOT_FOUND;
                else responseCode = HttpStatus.NOT_FOUND;
            }
        }

        if (inputStreamReader == null) {
            if (connection.getErrorStream() != null) {
                inputStreamReader = new InputStreamReader(connection.getErrorStream());
            } else {
                return "";
            }
        }

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (builder.length() > 0) builder.append("\n");
                builder.append(line);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public boolean downloadFile(File file, String url) {
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("connection", "keep-alive");
            connection.setRequestProperty("user-agent", USER_AGENT);

            responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode > 299) return false;

            InputStream inputStream = connection.getInputStream();
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists() || parent.isFile()) parent.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            if (responseCode == 0) {
                if (e instanceof SocketTimeoutException) responseCode = ERROR_TIMEOUT;
                else if (e instanceof UnknownHostException) responseCode = ERROR_UNKNOWN_HOST;
                else if (e instanceof ConnectException) responseCode = ERROR_BAD_CONNECTION;
                else if (e instanceof NullPointerException) responseCode = ERROR_BAD_CONNECTION; // Channel is unrecoverably broken and will be disposed!
                else if (e instanceof FileNotFoundException) responseCode = HttpStatus.NOT_FOUND;
                else responseCode = HttpStatus.NOT_FOUND;
            }
            file.delete();
            return false;
        }
        return true;
    }

    protected void sendFinish() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                responseCallback.onFinish();
            }
        });
    }

    protected void sendError(int error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                responseCallback.onError(error);
            }
        });
    }
}
