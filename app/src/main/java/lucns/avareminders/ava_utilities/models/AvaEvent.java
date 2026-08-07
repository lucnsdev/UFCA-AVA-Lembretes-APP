package lucns.avareminders.ava_utilities.models;

public abstract class AvaEvent {

    public static final int DELIVERY = 0;
    public static final int FORUM = 1;
    public static final int QUIZ = 2;
    public static final int FINAL_TEST = 3;
    public static final int SYNCHRONOUS_MEETING = 4;
    public boolean concluded, expired, invalid;
    public int type;
    public String title, courseName;

    public abstract String getStartDateTime();

    public abstract String getEndDateTime();

    public abstract long getRemainingMinutes();

    public abstract long getRemainingMinutes(String dateTime);
}
