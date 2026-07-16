package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import lucns.avareminders.ava.models.Task;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;

public class TaskOverDueDateRestApi  extends RestApiBase {

    public TaskOverDueDateRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        Annotator annotator = new Annotator("user", "Authentication.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            setCookie(jsonObject.getString("cookie"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentType("application/json");
    }

    public void request(Task task) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String html = requestGet(task.url);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                extractDatesFromTaskHtml(task, html);
                sendFinish();
            }
        });
        thread.start();
    }

    private void extractDatesFromTaskHtml(Task task, String html) {
        String[] lines = html.split("\n");
        String strong = "</strong>";
        for (String line : lines) {
            if (task.openedDate == null && line.contains("Aberto:")) {
                task.openedDate = line.substring(line.indexOf(strong) + strong.length() + 1);
                continue;
            }
            if (task.overdueDate == null && (line.contains("Vencimento:") || line.contains("Fecha:") || line.contains("Fechado:"))) {
                task.overdueDate = line.substring(line.indexOf(strong) + strong.length() + 1);
            }
            if (task.openedDate != null && task.overdueDate != null) break;
        }
    }
}
