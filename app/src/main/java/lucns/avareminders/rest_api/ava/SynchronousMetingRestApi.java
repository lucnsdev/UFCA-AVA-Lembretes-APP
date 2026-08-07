package lucns.avareminders.rest_api.ava;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lucns.avareminders.ava_utilities.AvaUtils;
import lucns.avareminders.ava_utilities.models.Course;
import lucns.avareminders.ava_utilities.models.SynchronousMeeting;
import lucns.avareminders.rest_api.internal.HttpStatus;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Annotator;
import lucns.avareminders.utils.Utils;

public class SynchronousMetingRestApi extends RestApiBase {

    private final String urlTrigger = "https://ava.ufca.edu.br/mod/lti/launch.php?id=%d&triggerview=0";
    private Course course;

    public SynchronousMetingRestApi(ResponseCallback responseCallback) {
        super(responseCallback);
        Annotator annotator = new Annotator("user", "Authentication.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            setCookie(jsonObject.getString("cookie"));
            //urlSessionId = String.format(Locale.getDefault(), urlSessionId, jsonObject.getString("session_key"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentType("application/x-www-form-urlencoded");
    }

    public void request(Course course) {
        this.course = course;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // https://ava.ufca.edu.br/mod/lti/launch.php?id=24584&triggerview=0
                String html = requestGet(String.format(Locale.getDefault(), urlTrigger, course.synchronousMeetingPartitionId));
                if (responseCode < HttpStatus.OK || responseCode > HttpStatus.IM_USED) {
                    sendError(responseCode);
                    return;
                }
                String url = html.split("\"")[1];
                String data = retrieveDataFromHtmlForm(html);
                // https://lti.mconf.rnp.br/lti/rooms/messages/blti
                html = requestPost(url, data);
                if (responseCode != HttpStatus.OK) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                String _bbb_lti_broker_session = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("_bbb_lti_broker_session")) {
                        _bbb_lti_broker_session = value.substring(0, value.indexOf(";"));
                        break;
                    }
                }
                if (_bbb_lti_broker_session == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(_bbb_lti_broker_session);
                String[] segments = html.split("'");
                url = "https://lti.mconf.rnp.br" + segments[3];
                data = segments[9] + "=" + segments[11];
                // https://lti.mconf.rnp.br/rooms/launch?launch_nonce=70571b182fd6c61b2596aa5660d16a59
                html = requestPost(url, data);
                if (responseCode != HttpStatus.OK) {
                    sendError(responseCode);
                    return;
                }
                segments = html.split("'");
                url = segments[3];
                data = segments[9] + "=" + segments[11];
                // https://lti.mconf.rnp.br/lti/rooms/launch?action=basic_lti_launch_request&app=...
                html = requestPost(url, data);
                if (headers == null || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                String _app_rooms_session = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("_app_rooms_session")) {
                        _app_rooms_session = value.substring(0, value.indexOf(";"));
                        break;
                    }
                }
                if (_app_rooms_session == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(_bbb_lti_broker_session + "; " + _app_rooms_session);

                segments = html.split("\"");
                url = "https://lti.mconf.rnp.br" + segments[3];
                data = segments[9] + "=" + segments[11];
                // https://lti.mconf.rnp.br/rooms/launch?launch_nonce=70571b182fd6c61b2596aa5660d16a59
                requestPost(url, data);
                if (responseCode != HttpStatus.FOUND) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Location") || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                String location = headers.get("Location").get(0);
                if (location == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                _app_rooms_session = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("_app_rooms_session")) {
                        _app_rooms_session = value.substring(0, value.indexOf(";"));
                        break;
                    }
                }
                if (_app_rooms_session == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(_bbb_lti_broker_session + "; " + _app_rooms_session);
                // https://lti.mconf.rnp.br/lti/oauth/authorize?client_id=rnp-app-rooms&launch_nonce=...
                requestGet(location);
                if (responseCode != HttpStatus.FOUND) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Location")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                location = headers.get("Location").get(0);
                if (location == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                // https://lti.mconf.rnp.br/rooms/auth/bbbltibroker/callback?launch_nonce=...
                requestGet(location);
                if (responseCode != HttpStatus.FOUND) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Location") || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                location = headers.get("Location").get(0);
                if (location == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                _app_rooms_session = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("_app_rooms_session")) {
                        _app_rooms_session = value.substring(0, value.indexOf(";"));
                        break;
                    }
                }
                if (_app_rooms_session == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(_bbb_lti_broker_session + "; " + _app_rooms_session);
                // https://lti.mconf.rnp.br/rooms/launch?launch_nonce=...
                requestGet(location);
                if (responseCode != HttpStatus.FOUND) {
                    sendError(responseCode);
                    return;
                }
                if (headers == null || !headers.containsKey("Location") || !headers.containsKey("Set-Cookie")) {
                    sendError(ERROR_BAD_CONNECTION);
                    return;
                }
                location = headers.get("Location").get(0);
                if (location == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                _app_rooms_session = null;
                for (String value : headers.get("Set-Cookie")) {
                    if (value.startsWith("_app_rooms_session")) {
                        _app_rooms_session = value.substring(0, value.indexOf(";"));
                        break;
                    }
                }
                if (_app_rooms_session == null) {
                    sendError(ERROR_APP_INTERNAL);
                    return;
                }
                setCookie(_bbb_lti_broker_session + "; " + _app_rooms_session);
                // https://lti.mconf.rnp.br/rooms/449608b0...
                html = requestGet(location);
                if (responseCode != HttpStatus.OK) {
                    sendError(responseCode);
                    return;
                }

                SynchronousMeeting[] meetings = retrieveMeetingsFromHtml(html);
                AvaUtils.setMeetings(course.id, meetings);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        responseCallback.onFinish(meetings);
                    }
                });
            }
        });
        thread.start();
    }

    private SynchronousMeeting[] retrieveMeetingsFromHtml(String html) {
        List<SynchronousMeeting> list = new LinkedList<>();
        String[] lines = html.split("\n");
        SynchronousMeeting meeting = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.contains("</tbody>") || (line.contains("Sessões agendadas") && lines[i + 1].contains("empty-list"))) break;
            if (line.contains("<tr class=\"d-flex row tr-row\">")) {
                meeting = new SynchronousMeeting();
                meeting.courseName = course.name;
                continue;
            }
            if (line.contains("item-title")) {
                meeting.title = Utils.removeEmojis(line.substring(line.indexOf(">") + 1, line.indexOf("</div>")));
                continue;
            }
            if (line.contains("col-md-3")) {
                meeting.date = lines[i + 1].trim();
                i += 2;
                continue;
            }
            if (line.contains("col-md-1") && meeting.duration == null) {
                meeting.duration = lines[i + 1].trim();
                i += 2;
                continue;
            }
            if (line.contains("join-room-btn")) {
                meeting.url = line.split("\"")[15];
                continue;
            }
            if (line.contains("</tr>")) {
                list.add(meeting);
            }
        }
        return list.toArray(new SynchronousMeeting[0]);
    }

    private String retrieveDataFromHtmlForm(String html) {
        StringBuilder builder = new StringBuilder();
        String[] lines = html.split("\n");
        for (String line : lines) {
            if (!line.contains("input")) continue;
            String[] segments = line.split("\"");
            if (builder.length() > 0) builder.append("&");
            builder.append(segments[3]);
            builder.append("=");
            builder.append(URLEncoder.encode(segments[5], StandardCharsets.UTF_8).replaceAll("%26quot%3B", "%22").replaceAll("amp%3B", ""));
        }
        return builder.toString();
    }

    private String getUrlFromHtmlForm(String html) {
        return html.split("\"")[1];
    }

    private String getNonceFromHtml(String html) {
        String[] lines = html.split("\n");
        for (String line : lines) {
            String[] segments = line.split("\"");
            if (segments.length <= 3) continue;
            if (line.contains("oauth_nonce")) return segments[5];
        }
        return null;
    }
}
