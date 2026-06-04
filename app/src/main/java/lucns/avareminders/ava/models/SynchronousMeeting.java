package lucns.avareminders.ava.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SynchronousMeeting {

    public String title, date, duration, url;

    public String getStartDateTime() {
        DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter saida =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd 'de' MMM yyyy, HH:mm",
                        Locale.of("pt", "BR")
                );
        LocalDateTime dateTime = LocalDateTime.parse(date, entrada);
        return dateTime.format(saida);
    }

    public String getEndDateTime() {
        DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "HH:mm",
                        Locale.of("pt", "BR")
                );
        LocalDateTime dateTime = LocalDateTime.parse(date, entrada);
        LocalDateTime updatedDateTime = dateTime.plusHours(1);
        return updatedDateTime.format(formatter);
    }

    public long getRemainingMinutes(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime older = LocalDateTime.parse(dateTime, formatter);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updatedDateTime = older.plusHours(1);
        return Duration.between(now, updatedDateTime).toMinutes();
    }
}
