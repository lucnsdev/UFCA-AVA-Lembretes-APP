package lucns.avareminders.rest_api.ava;

import lucns.avareminders.ava_utilities.models.Course;
import lucns.avareminders.ava_utilities.models.Student;
import lucns.avareminders.ava_utilities.models.SynchronousMeeting;
import lucns.avareminders.ava_utilities.models.Task;

public abstract class ResponseCallback {

    public abstract void onUnauthenticated();

    public abstract void onError(int responseCode);

    public void onFinish(Course[] courses) {}

    public void onFinish(SynchronousMeeting[] course) {}

    public void onFinish(Task[] tasks) {}

    public void onFinish(Student student) {}

    public void onFinish() {}
}
