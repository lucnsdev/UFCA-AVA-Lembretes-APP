package lucns.avareminders.ava_utilities.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task extends AvaEvent {

    public String overdueDate, openedDate, url;

    public Task() {}

    private Task(int type) {
        this.type = type;
    }

    public static Task validateType(String modType) {
        switch (modType) {
            case "assign":
                return new Task(DELIVERY);
            case "forum":
                return new Task(FORUM);
            case "quiz":
                return new Task(QUIZ);
            default:
                return null;
        }
    }

    @Override
    public String getStartDateTime() {
        return openedDate;
    }

    @Override
    public String getEndDateTime() {
        return overdueDate;
    }

    @Override
    public long getRemainingMinutes() {
        return getRemainingMinutes(overdueDate);
    }

    @Override
    public long getRemainingMinutes(String dateTime) {
        if (dateTime == null) return (60 * 24) + 1;
        else if (dateTime.equals("expired")) return 0;
        String[] segments = dateTime.split(" "); // "quarta-feira, 27 mai. 2026, 23:59"
        String[] months = new String[]{"jan.", "fev.", "mar.", "abr.", "mai.", "jun.", "jul.", "ago.", "set.", "out.", "nov.", "dez."};
        int month = 0;
        for (int i = 0; i < months.length; i++) {
            if (segments[2].equals(months[i])) month = i + 1;
        }
        String m = month < 10 ? "0" + month : String.valueOf(month);
        String day = segments[1].length() == 1 ? "0" + segments[1] : segments[1];
        String formatedDatetime = segments[3].substring(0, segments[3].length() - 1) + "-" + m + "-" + day + " " + segments[4];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime target = LocalDateTime.parse(formatedDatetime, formatter);
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, target).toMinutes();
    }
}
