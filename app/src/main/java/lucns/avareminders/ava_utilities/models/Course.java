package lucns.avareminders.ava_utilities.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Course {

    public int id, contextId, synchronousMeetingPartitionId;
    public String name, url;
    public Session[] sessions;
    public boolean expired;

    public Course() {}

    public Course(int id, String name, String url, long startDate) {
        this.id = id;
        this.name = name.replaceAll("\\|", "").trim();
        this.url = url;
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);;
        calendar.setTimeInMillis(startDate * 1000L);
        expired = currentYear > calendar.get(Calendar.YEAR);
    }

    public Task[] getTasks() {
        if (sessions == null) return null;
        List<Task> list = new ArrayList<>();
        for (Session session : sessions) {
            if (session.tasks == null) continue;
            Collections.addAll(list, session.tasks);
        }
        Task[] tasks = list.toArray(new Task[0]);

        boolean expiredTask = false;
        for (int i = tasks.length; i > 0; i--) {
            if (!expiredTask && tasks[i - 1].overdueDate != null && tasks[i - 1].getRemainingMinutes(tasks[i - 1].overdueDate) <= 1) {
                expiredTask = true;
                continue;
            }
            if (expiredTask && tasks[i - 1].overdueDate == null || expired) tasks[i - 1].expired = true;
        }
        return tasks;
    }
}
