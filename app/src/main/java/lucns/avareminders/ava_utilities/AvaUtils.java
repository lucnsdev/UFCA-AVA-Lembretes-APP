package lucns.avareminders.ava_utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lucns.avareminders.ava_utilities.models.Course;
import lucns.avareminders.ava_utilities.models.Session;
import lucns.avareminders.ava_utilities.models.SynchronousMeeting;
import lucns.avareminders.ava_utilities.models.Task;
import lucns.avareminders.utils.Annotator;

public class AvaUtils {

    public static Course[] getCourses() {
        Annotator annotator = new Annotator("courses", "Courses.json");
        if (!annotator.exists()) return null;
        try {
            JSONArray jsonArray = new JSONArray(annotator.getContent());
            Course[] courses = new Course[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                courses[i] = new Course();
                courses[i].id = jsonObject.getInt("id");
                courses[i].contextId = jsonObject.getInt("contextId");
                courses[i].synchronousMeetingPartitionId = jsonObject.getInt("synchronousMeetingPartitionId");
                courses[i].name = jsonObject.getString("name");
                courses[i].url = jsonObject.getString("url");
                courses[i].expired = jsonObject.getBoolean("expired");
            }
            return courses;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setCourses(Course[] courses) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Course course : courses) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", course.id);
                jsonObject.put("contextId", course.contextId);
                jsonObject.put("synchronousMeetingPartitionId", course.synchronousMeetingPartitionId);
                jsonObject.put("name", course.name);
                jsonObject.put("url", course.url);
                jsonObject.put("expired", course.expired);
                jsonArray.put(jsonObject);
            }
            new Annotator("courses", "Courses.json").setContent(jsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Session[] getSessions(int courseId) {
        Annotator annotator = new Annotator("courses/" + courseId, "Sessions.json");
        if (!annotator.exists()) return null;
        try {
            JSONArray jsonArray = new JSONArray(annotator.getContent());
            Session[] sessions = new Session[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sessions[i] = new Session();
                sessions[i].id = jsonObject.getInt("id");
                sessions[i].title = jsonObject.getString("title");
            }
            return sessions;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setSessions(int courseId, Session[] sessions) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Session session : sessions) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", session.id);
                jsonObject.put("title", session.title);
                jsonArray.put(jsonObject);
            }
            new Annotator("courses/" + courseId, "Sessions.json").setContent(jsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Task[] getTasks(int courseId) {
        Annotator annotator = new Annotator("courses/" + courseId, "Tasks.json");
        if (!annotator.exists()) return null;
        try {
            JSONArray jsonArray = new JSONArray(annotator.getContent());
            Task[] tasks = new Task[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                tasks[i] = new Task();
                tasks[i].courseName = jsonObject.getString("courseName");
                tasks[i].title = jsonObject.getString("title");
                tasks[i].overdueDate = jsonObject.optString("overdueDate", null);
                tasks[i].openedDate = jsonObject.optString("openedDate", null);
                tasks[i].expired = jsonObject.getBoolean("expired");
                tasks[i].concluded = jsonObject.getBoolean("concluded");
                tasks[i].type = jsonObject.getInt("type");
            }
            return tasks;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setTasks(int courseId, Task[] tasks) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Task task : tasks) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("courseName", task.courseName);
                jsonObject.put("title", task.title);
                jsonObject.put("overdueDate", task.overdueDate);
                jsonObject.put("openedDate", task.openedDate);
                jsonObject.put("expired", task.expired);
                jsonObject.put("concluded", task.concluded);
                jsonObject.put("type", task.type);
                jsonArray.put(jsonObject);
            }
            new Annotator("courses/" + courseId, "Tasks.json").setContent(jsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SynchronousMeeting[] getMeetings(int courseId) {
        Annotator annotator = new Annotator("courses/" + courseId, "SynchronousMeetings.json");
        if (!annotator.exists()) return null;
        try {
            JSONArray jsonArray = new JSONArray(annotator.getContent());
            SynchronousMeeting[] meetings = new SynchronousMeeting[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                meetings[i] = new SynchronousMeeting();
                meetings[i].courseName = jsonObject.getString("courseName");
                meetings[i].title = jsonObject.getString("title");
                meetings[i].date = jsonObject.getString("date");
                meetings[i].duration = jsonObject.getString("duration");
                meetings[i].url = jsonObject.getString("url");
            }
            return meetings;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setMeetings(int courseId, SynchronousMeeting[] meetings) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (SynchronousMeeting meeting : meetings) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("courseName", meeting.courseName);
                jsonObject.put("title", meeting.title);
                jsonObject.put("date", meeting.date);
                jsonObject.put("duration", meeting.duration);
                jsonObject.put("url", meeting.url);
                jsonArray.put(jsonObject);
            }
            new Annotator("courses/" + courseId, "SynchronousMeetings.json").setContent(jsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
