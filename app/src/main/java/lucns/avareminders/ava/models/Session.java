package lucns.avareminders.ava.models;

public class Session {

    public String title;
    public int id;

    public Task[] tasks;

    public Session() {}

    public Session(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public boolean expired() {
        return false;
    }
}
