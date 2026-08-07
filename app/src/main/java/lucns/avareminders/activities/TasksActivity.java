package lucns.avareminders.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lucns.avareminders.R;
import lucns.avareminders.ava_utilities.AvaUtils;
import lucns.avareminders.ava_utilities.models.AvaEvent;
import lucns.avareminders.ava_utilities.models.Course;
import lucns.avareminders.ava_utilities.models.SynchronousMeeting;
import lucns.avareminders.ava_utilities.models.Task;
import lucns.avareminders.ui_controller.UIController;

public class TasksActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_and_meetings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Course[] courses = AvaUtils.getCourses();
        if (courses == null) {
            findViewById(R.id.item_error).setVisibility(View.VISIBLE);
            return;
        }
        List<AvaEvent> list = new ArrayList<>();
        for (Course course : courses) {
            Task[] tasks = AvaUtils.getTasks(course.id);
            if (tasks != null) {
                for (Task t : tasks) {
                    if (t.expired || t.getRemainingMinutes(t.getEndDateTime()) <= 1) continue;
                    list.add(t);
                }
            }
            SynchronousMeeting[] meetings = AvaUtils.getMeetings(course.id);
            if (meetings != null) list.addAll(Arrays.asList(meetings));
        }
        list.sort(new Comparator<AvaEvent>() {
            @Override
            public int compare(AvaEvent o1, AvaEvent o2) {
                return Long.compare(o1.getRemainingMinutes(), o2.getRemainingMinutes());
            }
        });

        UIController uiController = UIController.getInstance(this);
        ListView listView = findViewById(R.id.listView);
        LayoutInflater inflater = LayoutInflater.from(this);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.item_task_course) {

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = inflater.inflate(R.layout.item_task_course, null, false);
                AvaEvent event = list.get(position);

                View line = view.findViewById(R.id.line);
                ImageView iconTitle = view.findViewById(R.id.iconTitle);
                TextView textTopStart = view.findViewById(R.id.textTopStart);
                TextView textCourseName = view.findViewById(R.id.textCourseName);
                TextView textCenterStart = view.findViewById(R.id.textCenterStart);
                TextView textCenterEnd = view.findViewById(R.id.textCenterEnd);
                TextView textBottomStart = view.findViewById(R.id.textBottomStart);
                TextView textBottomEnd = view.findViewById(R.id.textBottomEnd);
                textCenterStart.setText(R.string.open_in);
                textCenterEnd.setText(R.string.close_in);
                ImageView iconCenterStart = view.findViewById(R.id.iconCenterStart);
                ImageView iconCenterEnd = view.findViewById(R.id.iconCenterEnd);
                textCourseName.setText(event.courseName);

                int red = getContext().getColor(R.color.text_red);
                if (event.type == AvaEvent.SYNCHRONOUS_MEETING) {
                    SynchronousMeeting meeting = (SynchronousMeeting) event;
                    textTopStart.setText(meeting.title);
                    textBottomStart.setText(meeting.getStartDateTime());
                    long remaining = event.getRemainingMinutes(meeting.date);
                    if (remaining <= 1) {
                        view.setAlpha(0.3f);
                        iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_camera, Color.WHITE));
                    } else if (remaining <= 24 * 60) {
                        view.setBackgroundResource(R.drawable.sub_item_red);
                        iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_camera, red));
                        textCenterStart.setTextColor(red);
                        textCenterEnd.setTextColor(red);
                        textBottomStart.setTextColor(red);
                        textBottomEnd.setTextColor(red);
                        line.setBackgroundColor(red);
                        iconCenterStart.setImageDrawable(uiController.tint(R.drawable.icon_clock, red));
                        iconCenterEnd.setImageDrawable(uiController.tint(R.drawable.icon_clock, red));
                    } else {
                        iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_camera, Color.WHITE));
                    }
                } else {
                    long passedTime = event.getRemainingMinutes(event.getEndDateTime());
                    if (passedTime <= 60 * 24) {
                        if (event.type == AvaEvent.QUIZ) {
                            iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_list, red));
                        } else if (event.type == AvaEvent.FINAL_TEST) {
                            iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_school_test, red));
                        } else if (event.type == AvaEvent.FORUM) {
                            iconTitle.setImageResource(R.drawable.icon_message);
                            iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_message, red));
                        } else { // AvaEvent.DELIVERY
                            iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_pdf, red));
                        }
                    } else {
                        if (event.type == AvaEvent.QUIZ) {
                            iconTitle.setImageResource(R.drawable.icon_list);
                        } else if (event.type == AvaEvent.FINAL_TEST) {
                            iconTitle.setImageResource(R.drawable.icon_school_test);
                        } else if (event.type == AvaEvent.FORUM) {
                            iconTitle.setImageResource(R.drawable.icon_message);
                        } else { // AvaEvent.DELIVERY
                            iconTitle.setImageResource(R.drawable.icon_pdf);
                        }
                    }
                    if (event.expired || passedTime <= 1) {
                        view.setAlpha(0.3f);
                        textTopStart.setTextColor(Color.WHITE);
                    } else if (event.concluded) {
                        line.setBackgroundColor(getContext().getColor(R.color.sub_item_green_border));
                        view.setBackgroundResource(R.drawable.sub_item_green);
                    } else if (passedTime <= 60 * 24) {
                        iconTitle.setImageDrawable(uiController.tintDrawable(iconTitle.getDrawable(), red));
                        textCenterStart.setTextColor(red);
                        textCenterEnd.setTextColor(red);
                        textBottomStart.setTextColor(red);
                        textBottomEnd.setTextColor(red);
                        line.setBackgroundColor(red);
                        line.setBackgroundColor(red);
                        iconCenterStart.setImageDrawable(uiController.tint(R.drawable.icon_clock, red));
                        iconCenterEnd.setImageDrawable(uiController.tint(R.drawable.icon_clock, red));
                        view.setBackgroundResource(R.drawable.sub_item_red);
                    }
                    textTopStart.setText(event.title + (event.concluded ? " " + getContext().getString(R.string.concluded) : ""));
                    if (event.getStartDateTime() == null) textBottomStart.setText(R.string.not_specified);
                    else textBottomStart.setText(event.getStartDateTime());
                }
                if (event.getEndDateTime() == null) textBottomEnd.setText(R.string.not_specified);
                else textBottomEnd.setText(event.getEndDateTime());
                return view;
            }
        });
    }
}
