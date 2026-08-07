package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import lucns.avareminders.ava_utilities.AvaUtils;
import lucns.avareminders.ava_utilities.models.Course;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;

public class CoursesRestApi extends RestApiBase {

    private String url = "https://ava.ufca.edu.br/lib/ajax/service.php?sesskey=%s&info=core_course_get_enrolled_courses_by_timeline_classification";

    public CoursesRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        Annotator annotator = new Annotator("user", "Authentication.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            setCookie(jsonObject.getString("cookie"));
            url = String.format(Locale.getDefault(), url, jsonObject.getString("session_key"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentType("application/json");
    }

    public void request() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray arrayBody = new JSONArray();
                try {
                    JSONObject jsonObject = new JSONObject();
                    arrayBody.put(jsonObject);

                    JSONArray arrayData = new JSONArray();
                    arrayData.put("id");
                    arrayData.put("fullname");
                    arrayData.put("shortname");
                    arrayData.put("showcoursecategory");
                    arrayData.put("showshortname");
                    arrayData.put("visible");
                    arrayData.put("enddate");

                    JSONObject args = new JSONObject();
                    args.put("offset", 0);
                    args.put("limit", 0);
                    args.put("classification", "all");
                    args.put("sort", "fullname");
                    args.put("customfieldname", "");
                    args.put("customfieldvalue", "");
                    args.put("requiredfields", arrayData);

                    jsonObject.put("index", 0);
                    jsonObject.put("methodname", "core_course_get_enrolled_courses_by_timeline_classification");
                    jsonObject.put("args", args);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String json = requestPost(url, arrayBody.toString());
                if (json.contains("expirou")) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onUnauthenticated();
                        }
                    });
                    return;
                }
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                List<Course> list = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(json).getJSONObject(0).getJSONObject("data").getJSONArray("courses");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //if (jsonObject.getLong("enddate") > 0) continue;
                        list.add(new Course(jsonObject.getInt("id"), jsonObject.getString("fullname").toUpperCase(), jsonObject.getString("viewurl"), jsonObject.getLong("startdate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                list.sort(new Comparator<Course>() {
                    @Override
                    public int compare(Course o1, Course o2) {
                        return o2.name.substring(0, o2.name.indexOf(" ")).compareTo(o1.name.substring(0, o1.name.indexOf(" ")));
                    }
                });
                Course[] courses = list.toArray(new Course[0]);
                AvaUtils.setCourses(courses);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        responseCallback.onFinish(courses);
                    }
                });
            }
        });
        thread.start();
    }
}
