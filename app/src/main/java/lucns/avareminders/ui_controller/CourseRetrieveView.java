package lucns.avareminders.ui_controller;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import lucns.avareminders.R;
import lucns.avareminders.ava.AvaUtils;
import lucns.avareminders.ava.models.Course;
import lucns.avareminders.ava.models.SynchronousMeeting;
import lucns.avareminders.ava.models.Task;
import lucns.avareminders.rest_api.ava.ResponseCallback;
import lucns.avareminders.rest_api.ava.SessionTasksRestApi;
import lucns.avareminders.rest_api.ava.SessionsRestApi;
import lucns.avareminders.rest_api.ava.SynchronousMetingRestApi;
import lucns.avareminders.rest_api.ava.TaskOverDueDateRestApi;
import lucns.avareminders.utils.TimeRegister;
import lucns.avareminders.utils.Utils;
import lucns.avareminders.views.FlexibleLayout;

public class CourseRetrieveView {

    private final FlexibleLayout flexibleLayout;
    private final SessionsRestApi sessionsRestApi;
    private final SessionTasksRestApi sessionTasksRestApi;
    private final SynchronousMetingRestApi synchronousMetingRestApi;
    private final Course course;
    private final LayoutInflater inflater;
    private View viewActivities, viewMeetings;
    private final LinearLayout rootActivities, rootMeting;
    private boolean running;

    public CourseRetrieveView(Course course, FlexibleLayout flexibleLayout, ResponseCallback responseCallback) {
        this.course = course;
        this.flexibleLayout = flexibleLayout;
        this.inflater = LayoutInflater.from(flexibleLayout.getContext());

        rootMeting = generateLinearLayout();
        rootActivities = generateLinearLayout();

        LinearLayout root = generateLinearLayout();
        root.addView(generateTextView(R.string.meetings));
        root.addView(generateSpace(1, Utils.dpToPx(8)));
        root.addView(rootMeting);
        root.addView(generateSpace(1, Utils.dpToPx(16)));
        root.addView(generateTextView(R.string.activities));
        root.addView(generateSpace(1, Utils.dpToPx(8)));
        root.addView(rootActivities);
        flexibleLayout.overlap(root);

        sessionsRestApi = new SessionsRestApi(new ResponseCallback() {

            @Override
            public void onUnauthenticated() {
                responseCallback.onUnauthenticated();
                putErrorInActivitiesRoot();
            }

            @Override
            public void onError(int responseCode) {
                responseCallback.onError(responseCode);
                putTryAgainInActivitiesRoot();
            }

            @Override
            public void onFinish() {
                new TimeRegister(course.id + "_sessions").setLastUpdate();
                if (course.sessions == null) {
                    putEmptyInActivitiesRoot();
                    return;
                }
                updateSessions();
            }
        });
        sessionTasksRestApi = new SessionTasksRestApi(new ResponseCallback() {
            @Override
            public void onUnauthenticated() {
                responseCallback.onUnauthenticated();
                putErrorInActivitiesRoot();
            }

            @Override
            public void onError(int responseCode) {
                responseCallback.onError(responseCode);
                putTryAgainInActivitiesRoot();
            }

            @Override
            public void onFinish(Task[] tasks) {
                new TimeRegister(course.id + "_tasks").setLastUpdate();
                if (tasks == null || tasks.length == 0) {
                    putEmptyInActivitiesRoot();
                    return;
                }
                updateTasks(tasks);
            }
        });

        synchronousMetingRestApi = new SynchronousMetingRestApi(new ResponseCallback() {
            @Override
            public void onUnauthenticated() {
                responseCallback.onUnauthenticated();
                putErrorInMeetingRoot();
            }

            @Override
            public void onError(int responseCode) {
                responseCallback.onError(responseCode);
                putTryAgainInMeetingRoot();
            }

            @Override
            public void onFinish(SynchronousMeeting[] meetings) {
                new TimeRegister(course.id + "_meetings").setLastUpdate();
                if (meetings == null || meetings.length == 0) {
                    putEmptyInMeetingRoot();
                    return;
                }
                updateMeetings(meetings);
            }
        });
    }

    private void updateSessions() {
        if (new TimeRegister(course.id + "_tasks").isOverTime(60)) {
            putLoadingInActivitiesRoot();
            sessionTasksRestApi.request(course);
        } else {
            Task[] tasks = AvaUtils.getTasks(course.id);
            updateTasks(tasks);
        }
        if (new TimeRegister(course.id + "_meetings").isOverTime(60)) {
            if (course.synchronousMeetingPartitionId > 0) {
                putLoadingInMetingRoot();
                synchronousMetingRestApi.request(course);
            } else {
                putErrorInMeetingRoot();
            }
        } else {
            SynchronousMeeting[] meetings = AvaUtils.getMeetings(course.id);
            updateMeetings(meetings);
        }
    }

    private void updateTasks(Task[] tasks) {
        rootActivities.removeAllViews();
        if (tasks.length == 0) {
            putEmptyInActivitiesRoot();
        }
        UIController uiController = UIController.getInstance(flexibleLayout.getContext());
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            View view = inflater.inflate(R.layout.item_task, null, false);
            View line = view.findViewById(R.id.line);
            ImageView iconTitle = view.findViewById(R.id.iconTitle);
            TextView textTopStart = view.findViewById(R.id.textTopStart);
            TextView textCenterStart = view.findViewById(R.id.textCenterStart);
            TextView textCenterEnd = view.findViewById(R.id.textCenterEnd);
            TextView textBottomStart = view.findViewById(R.id.textBottomStart);
            TextView textBottomEnd = view.findViewById(R.id.textBottomEnd);
            textCenterStart.setText(R.string.open_in);
            textCenterEnd.setText(R.string.close_in);
            ImageView iconCenterStart = view.findViewById(R.id.iconCenterStart);
            ImageView iconCenterEnd = view.findViewById(R.id.iconCenterEnd);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);

            long passedTime = task.getRemainingMinutes(task.overdueDate);
            int red = flexibleLayout.getContext().getColor(R.color.text_red);
            if (passedTime <= 60 * 24) {
                if (task.type == Task.QUIZ) {
                    iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_list, red));
                } else if (task.type == Task.FINAL_TEST) {
                    iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_school_test, red));
                } else if (task.type == Task.FORUM) {
                    iconTitle.setImageResource(R.drawable.icon_message);
                    iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_message, red));
                } else { // Task.DELIVERY
                    iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_pdf, red));
                }
            } else {
                if (task.type == Task.QUIZ) {
                    iconTitle.setImageResource(R.drawable.icon_list);
                } else if (task.type == Task.FINAL_TEST) {
                    iconTitle.setImageResource(R.drawable.icon_school_test);
                } else if (task.type == Task.FORUM) {
                    iconTitle.setImageResource(R.drawable.icon_message);
                } else { // Task.DELIVERY
                    iconTitle.setImageResource(R.drawable.icon_pdf);
                }
            }
            if (task.expired || passedTime <= 1) {
                view.setAlpha(0.3f);
                textTopStart.setTextColor(Color.WHITE);
            } else if (task.concluded) {
                line.setBackgroundColor(flexibleLayout.getContext().getColor(R.color.sub_item_green_border));
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
            textTopStart.setText(task.title + (task.concluded ? " " + flexibleLayout.getContext().getString(R.string.concluded) : ""));
            if (task.openedDate == null) textBottomStart.setText(R.string.not_specified);
            else textBottomStart.setText(task.openedDate);
            if (task.overdueDate == null) {
                textBottomEnd.setText(R.string.not_specified);
                if (Utils.hasInternetConnection() && task.url != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    new TaskOverDueDateRestApi(new ResponseCallback() {
                        @Override
                        public void onUnauthenticated() {
                            progressBar.setVisibility(View.INVISIBLE);
                            textBottomEnd.setTextColor(flexibleLayout.getContext().getColor(R.color.orange));
                            textBottomEnd.setText(R.string.error);
                        }

                        @Override
                        public void onError(int responseCode) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textBottomEnd.setTextColor(flexibleLayout.getContext().getColor(R.color.orange));
                            textBottomEnd.setText(R.string.error);
                        }

                        @Override
                        public void onFinish() {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (task.openedDate == null) {
                                textBottomStart.setText(R.string.not_specified);
                            } else {
                                textBottomStart.setText(task.openedDate);
                            }
                            if (task.overdueDate == null) {
                                textBottomEnd.setText(R.string.not_specified);
                            } else {
                                textBottomEnd.setText(task.overdueDate);
                                long passedTime = task.getRemainingMinutes(task.overdueDate);
                                if (passedTime <= 1) {
                                    view.setAlpha(0.3f);
                                    return;
                                }
                                if (task.concluded) return;
                                if (passedTime <= 60 * 24) {
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
                            }
                        }
                    }).request(task);
                }
            } else {
                textBottomEnd.setText(task.overdueDate);
            }
            if (rootActivities.getChildCount() > 0) {
                Space space = new Space(flexibleLayout.getContext());
                space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(16)));
                rootActivities.addView(space);
            }
            rootActivities.addView(view);
        }
        flexibleLayout.computeSizes();
    }

    private void updateMeetings(SynchronousMeeting[] meetings) {
        rootMeting.removeAllViews();
        if (meetings.length == 0) {
            putEmptyInMeetingRoot();
        }
        UIController uiController = UIController.getInstance(flexibleLayout.getContext());
        for (int i = 0; i < meetings.length; i++) {
            SynchronousMeeting meeting = meetings[i];
            View view = inflater.inflate(R.layout.item_task, null, false);
            View line = view.findViewById(R.id.line);
            ImageView iconTitle = view.findViewById(R.id.iconTitle);
            ImageView iconCenterStart = view.findViewById(R.id.iconCenterStart);
            ImageView iconCenterEnd = view.findViewById(R.id.iconCenterEnd);
            TextView textTopStart = view.findViewById(R.id.textTopStart);
            TextView textCenterStart = view.findViewById(R.id.textCenterStart);
            TextView textCenterEnd = view.findViewById(R.id.textCenterEnd);
            TextView textBottomStart = view.findViewById(R.id.textBottomStart);
            TextView textBottomEnd = view.findViewById(R.id.textBottomEnd);
            textCenterStart.setText(R.string.starts_at);
            textCenterEnd.setText(R.string.ends_at);
            textTopStart.setText(meeting.title);
            textBottomStart.setText(meeting.getStartDateTime());
            textBottomEnd.setText(meeting.getEndDateTime());

            long remaining = meeting.getRemainingMinutes(meeting.date);
            if (remaining <= 1) {
                view.setAlpha(0.3f);
                iconTitle.setImageDrawable(uiController.tint(R.drawable.icon_camera, Color.WHITE));
            } else if (remaining <= 24 * 60) {
                view.setBackgroundResource(R.drawable.sub_item_red);
                int red = flexibleLayout.getContext().getColor(R.color.text_red);
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
            if (rootMeting.getChildCount() > 0) {
                Space space = new Space(flexibleLayout.getContext());
                space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(16)));
                rootMeting.addView(space);
            }
            rootMeting.addView(view);
        }
        flexibleLayout.computeSizes();
    }

    private void putTryAgainInActivitiesRoot() {
        viewActivities = inflater.inflate(R.layout.item_try_again, rootActivities, false);
        LinearLayout buttonRefresh = viewActivities.findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSessions();
            }
        });
        if (viewActivities != null) rootActivities.removeView(viewActivities);
        rootActivities.addView(viewActivities);
    }

    private void putTryAgainInMeetingRoot() {
        if (rootMeting == null) return;
        if (viewMeetings != null) rootMeting.removeView(viewMeetings);
        viewMeetings = inflater.inflate(R.layout.item_try_again, rootMeting, false);
        LinearLayout buttonRefresh = viewMeetings.findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSessions();
            }
        });
        rootMeting.addView(viewMeetings);
    }

    private void putErrorInActivitiesRoot() {
        if (rootActivities == null) return;
        if (viewActivities != null) rootActivities.removeView(viewActivities);
        viewActivities = inflater.inflate(R.layout.item_error, rootActivities, false);
        rootActivities.addView(viewActivities);
    }

    private void putErrorInMeetingRoot() {
        if (rootMeting == null) return;
        if (viewMeetings != null) rootActivities.removeView(viewMeetings);
        viewMeetings = inflater.inflate(R.layout.item_error, rootMeting, false);
        rootMeting.addView(viewMeetings);
    }

    private void putEmptyInActivitiesRoot() {
        if (rootActivities == null) return;
        if (viewActivities != null) rootActivities.removeView(viewActivities);
        viewActivities = inflater.inflate(R.layout.item_empty, rootMeting, false);
        rootActivities.addView(viewActivities);
    }

    private void putEmptyInMeetingRoot() {
        if (rootMeting == null) return;
        if (viewMeetings != null) rootMeting.removeView(viewMeetings);
        viewMeetings = inflater.inflate(R.layout.item_empty, rootMeting, false);
        rootMeting.addView(viewMeetings);
    }

    private void putLoadingInActivitiesRoot() {
        if (rootActivities == null) return;
        if (viewActivities != null) rootActivities.removeView(viewActivities);
        viewActivities = inflater.inflate(R.layout.item_loading, rootActivities, false);
        rootActivities.addView(viewActivities);
    }

    private void putLoadingInMetingRoot() {
        if (rootMeting == null) return;
        if (viewMeetings != null) rootMeting.removeView(viewMeetings);
        viewMeetings = inflater.inflate(R.layout.item_loading, rootMeting, false);
        rootMeting.addView(viewMeetings);
    }

    private View generateTextView(int resString) {
        View view = inflater.inflate(R.layout.item_text_divider, rootMeting, false);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(resString);
        return view;
        /*
        TextView textView = new TextView(flexibleLayout.getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(16);
        textView.setTextColor(flexibleLayout.getContext().getColor(R.color.gray));
        textView.setText(resString);
        textView.setPadding(16, 0, 0, 0);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
         */
    }

    private Space generateSpace(int width, int height) {
        Space space = new Space(flexibleLayout.getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return space;
    }

    private LinearLayout generateLinearLayout() {
        LinearLayout root = new LinearLayout(flexibleLayout.getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return root;
    }

    public boolean isRunning() {
        return running;
    }

    public void retrieve() {
        running = true;
        course.sessions = AvaUtils.getSessions(course.id);
        if (Utils.hasInternetConnection()) {
            if (new TimeRegister(course.id + "_sessions").isOverTime(60)) {
                putLoadingInActivitiesRoot();
                putLoadingInMetingRoot();
                sessionsRestApi.request(course);
                return;
            }
        } else if (course.sessions == null) {
            putErrorInMeetingRoot();
            putErrorInActivitiesRoot();
            return;
        }
        updateSessions();
    }
}
