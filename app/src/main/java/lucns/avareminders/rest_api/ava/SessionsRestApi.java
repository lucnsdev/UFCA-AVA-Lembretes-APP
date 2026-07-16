package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import lucns.avareminders.ava.AvaUtils;
import lucns.avareminders.ava.models.Course;
import lucns.avareminders.ava.models.Session;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;

public class SessionsRestApi extends RestApiBase {

    private String urlSessionId = "https://ava.ufca.edu.br/lib/ajax/service.php?sesskey=%s&info=core_courseformat_get_state";

    public SessionsRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        Annotator annotator = new Annotator("user", "Authentication.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            setCookie(jsonObject.getString("cookie"));
            urlSessionId = String.format(Locale.getDefault(), urlSessionId, jsonObject.getString("session_key"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentType("application/json");
    }

    public void request(Course course) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String html = requestGet(course.url);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                if (!html.contains("courseContextId")) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                course.contextId = getCourseContextId(html);
                if (course.contextId == 0) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }

                String json;
                try {
                    JSONObject args = new JSONObject();
                    args.put("courseid", course.id);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("index", 0);
                    jsonObject.put("methodname", "core_courseformat_get_state");
                    jsonObject.put("args", args);
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    json = jsonArray.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                json = requestPost(urlSessionId, json);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                retrieveCourseSynchronousMeetingId(course, json);
                retrieveCourseSessionIds(course, json);
                AvaUtils.setSessions(course.id, course.sessions);
                sendFinish();
            }
        });
        thread.start();
    }

    private void retrieveCourseSynchronousMeetingId(Course course, String json) {
        try {
            JSONObject jsonObject = new JSONObject(new JSONArray(json).getJSONObject(0).getString("data"));
            JSONArray jsonArray = jsonObject.getJSONArray("cm");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSession = jsonArray.getJSONObject(i);
                String title = jsonSession.getString("name");
                if (title.contains("Sala de Aula Virtual") && jsonSession.getString("module").equals("lti")) {
                    course.synchronousMeetingPartitionId = Integer.parseInt(jsonSession.getString("id"));
                }
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void retrieveCourseSessionIds(Course course, String json) {
        try {
            JSONObject jsonObject = new JSONObject(new JSONArray(json).getJSONObject(0).getString("data"));
            JSONArray jsonArray = jsonObject.getJSONArray("section");
            course.sessions = new Session[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSession = jsonArray.getJSONObject(i);
                course.sessions[i] = new Session(Integer.parseInt(jsonSession.getString("id")), jsonSession.getString("title"));
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private int getCourseContextId(String html) {
        String[] lines = html.split("\n");
        for (String line : lines) {
            try {
                if (line.contains("courseContextId")) {
                    String[] segments = line.split("\"");
                    return Integer.parseInt(segments[58].substring(1, segments[58].length() - 1));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
