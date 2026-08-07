package lucns.avareminders.ava_utilities.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SynchronousMeeting extends AvaEvent {

    public String date, duration, url;

    public SynchronousMeeting() {
        type = SYNCHRONOUS_MEETING;
    }

    @Override
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

    @Override
    public String getEndDateTime() {
        DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter saida =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd 'de' MMM yyyy, HH:mm",
                        Locale.of("pt", "BR")
                );
        LocalDateTime dateTime = LocalDateTime.parse(date, entrada);
        LocalDateTime updatedDateTime = dateTime.plusHours(1);
        return updatedDateTime.format(saida);
    }

    @Override
    public long getRemainingMinutes() {
        return getRemainingMinutes(date);
    }

    @Override
    public long getRemainingMinutes(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime older = LocalDateTime.parse(dateTime, formatter);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updatedDateTime = older.plusHours(1);
        return Duration.between(now, updatedDateTime).toMinutes();
    }
}
