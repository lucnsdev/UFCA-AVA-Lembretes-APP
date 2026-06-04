package lucns.avareminders.rest_api.ava;

import lucns.avareminders.ava.models.Course;
import lucns.avareminders.ava.models.Student;
import lucns.avareminders.ava.models.SynchronousMeeting;
import lucns.avareminders.ava.models.Task;

public abstract class ResponseCallback {

    public abstract void onUnauthenticated();

    public abstract void onError(int responseCode);

    public void onFinish(Course[] courses) {}

    public void onFinish(SynchronousMeeting[] course) {}

    public void onFinish(Task[] tasks) {}

    public void onFinish(Student student) {}

    public void onFinish() {}
}
