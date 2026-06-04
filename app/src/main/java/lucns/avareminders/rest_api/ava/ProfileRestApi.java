package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucns.avareminders.ava.models.Student;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;

public class ProfileRestApi extends RestApiBase {

    private final String url = "https://ava.ufca.edu.br/user/profile.php";

    public ProfileRestApi(ResponseCallback responseCallback) {
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

    public void request() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String html = requestGet(url);
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onError(responseCode);
                        }
                    });
                    return;
                }
                Student student = retrieveHtml(html);
                if (student == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        responseCallback.onFinish(student);
                    }
                });
            }
        });
        thread.start();
    }

    private Student retrieveHtml(String html) {
        Student student = null;
        String[] lines = html.split("\n");
        String email = "Endereço de e-mail";
        String tag = "<dd>";
        for (String line : lines) {
            if (line.contains("page-context-header")) {
                String[] segments = line.split("\"");
                student = new Student();
                if (segments[5].startsWith("http")) {
                    student.urlPicture = segments[5];
                    student.name = segments[13];
                } else {
                    student.name = segments[7];
                }
            } else if (line.contains(email)) {
                line = line.substring(line.indexOf(email) + email.length());
                line = line.substring(line.indexOf("\"") + 1);
                student.email = unescapeHtmlCodes(line.substring(line.indexOf(">") + 1, line.indexOf("<")));
                line = line.substring(line.indexOf("CPF"));
                int indexOf  = line.indexOf(tag) + tag.length();
                student.cpf = line.substring(indexOf, indexOf + 11);
                String registration = "Matrícula";
                line = line.substring(line.indexOf(registration) + registration.length());
                tag = "<dd>";
                indexOf = line.indexOf(tag) + tag.length();
                student.registration = line.substring(indexOf, indexOf + 10);
                return student;
            }
        }
        return student;
    }

    private String unescapeHtmlCodes(String input) {
        Pattern pattern = Pattern.compile("&#(\\d+);");
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            int codePoint = Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(sb, Character.toString(codePoint));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
