package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lucns.avareminders.ava.AvaUtils;
import lucns.avareminders.ava.models.Course;
import lucns.avareminders.ava.models.Session;
import lucns.avareminders.ava.models.Task;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;
import lucns.avareminders.utils.Utils;

public class SessionTasksRestApi extends RestApiBase {

    private String urlDatesObtain = "https://ava.ufca.edu.br/lib/ajax/service.php?sesskey=%s&info=core_get_fragment";

    public SessionTasksRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        Annotator annotator = new Annotator("user", "Authentication.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            setCookie(jsonObject.getString("cookie"));
            urlDatesObtain = String.format(Locale.getDefault(), urlDatesObtain, jsonObject.getString("session_key"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentType("application/json");
    }

    public void request(Course course) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String json;
                for (int i = 0; i < course.sessions.length; i++) {
                    try {
                        JSONObject jsonObjectArgs = new JSONObject();
                        jsonObjectArgs.put("name", "sectionid");
                        jsonObjectArgs.put("value", course.sessions[i].id);
                        JSONArray args2 = new JSONArray();
                        args2.put(jsonObjectArgs);
                        JSONObject args = new JSONObject();
                        args.put("component", "format_tiles");
                        args.put("callback", "get_cm_list");
                        args.put("contextid", course.contextId);
                        args.put("args", args2);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("index", 0);
                        jsonObject.put("methodname", "core_get_fragment");
                        jsonObject.put("args", args);
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(jsonObject);
                        json = jsonArray.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    json = requestPost(urlDatesObtain, json);
                    if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                responseCallback.onError(responseCode);
                            }
                        });
                        return;
                    }
                    try {
                        JSONObject jsonData = new JSONArray(json).getJSONObject(0);
                        if (!jsonData.has("data")) {
                            course.sessions[i].tasks = new Task[0];
                            continue;
                        }
                        String html = jsonData.getJSONObject("data").getString("html");
                        retrieveTasks(course.sessions[i], html);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Task[] tasks = course.getTasks();
                AvaUtils.setTasks(course.id, tasks);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        responseCallback.onFinish(tasks);
                    }
                });
            }
        });
        thread.start();
    }

    private void retrieveTasks(Session session, String html) {
        List<Task> taskList = new LinkedList<>();
        String[] lines = html.split("\n");
        String title;
        boolean waitingDates = false;
        boolean waitingConcludedButton = false;
        boolean inButton = false;
        Task task = null;
        String strong = "</strong>";

        for (String line : lines) {
            if (line.contains("data-modtype")) {
                task = Task.validateType(line.split("\"")[1]);
                continue;
            }
            if (task != null && line.contains("cm-link")) {
                task.url = line.split("\"")[3];
                // continue;
            }
            if (line.contains("instancename") && task != null) {
                title = Utils.removeEmojis(line.split("\"")[8]);
                task.title = title.substring(1, title.indexOf("<")).trim();
                if (checkIsInvalid(task.title)) task.invalid = true;
                if (task.title.toLowerCase().contains("avaliação")) task.type = Task.FINAL_TEST;
                continue;
            }
            if (line.contains("activity-dates")) {
                waitingDates = true;
                continue;
            }
            if (waitingDates) {
                if (line.contains(strong)) {
                    if (line.contains("Abre") || line.contains("Aberto")) {
                        task.openedDate = line.substring(line.indexOf(strong) + strong.length() + 1);
                        task.openedDate = task.openedDate.replace("</strong>", "");
                    } else if (line.contains("Fecha") || line.contains("Vencimento")) {
                        waitingDates = false;
                        task.overdueDate = line.substring(line.indexOf(strong) + strong.length() + 1);
                        task.overdueDate = task.overdueDate.replace("</strong>", "");
                    }
                }
                continue;
            }
            if (line.contains("</li>")) {
                waitingConcludedButton = false;
                inButton = false;
                if (task != null && !task.invalid) taskList.add(task);
                task = null;
                continue;
            } else if (line.contains("activity-completion")) {
                waitingConcludedButton = true;
                continue;
            }
            if (waitingConcludedButton) {
                if (line.contains("<button")) {
                    inButton = true;
                    continue;
                }
                if (inButton) {
                    if (line.contains("</button>")) {
                        inButton = false;
                        waitingConcludedButton = false;
                    } else if (line.contains("Concluído") && task != null) {
                        task.concluded = true;
                    }
                }
            }
        }
        if (!taskList.isEmpty()) session.tasks = taskList.toArray(new Task[0]);
    }

    private boolean checkIsInvalid(String title) {
        String[] blackList = new String[] {
                "Central de Avisos",
                "Fórum de Discussões",
                "Tira Dúvidas"
        };
        for (String black : blackList) {
            if (title.toLowerCase().contains(black.toLowerCase())) return true;
        }
        return false;
    }
}
