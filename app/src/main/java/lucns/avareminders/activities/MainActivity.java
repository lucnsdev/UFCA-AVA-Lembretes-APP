package lucns.avareminders.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lucns.avareminders.R;
import lucns.avareminders.ava.AvaUtils;
import lucns.avareminders.ava.models.Course;
import lucns.avareminders.ava.models.Student;
import lucns.avareminders.rest_api.ava.CoursesRestApi;
import lucns.avareminders.rest_api.ava.ProfileRestApi;
import lucns.avareminders.rest_api.ava.ResponseCallback;
import lucns.avareminders.ui_controller.CourseRetrieveView;
import lucns.avareminders.ui_controller.UIController;
import lucns.avareminders.utils.Annotator;
import lucns.avareminders.utils.Notify;
import lucns.avareminders.utils.TimeRegister;
import lucns.avareminders.utils.Utils;
import lucns.avareminders.views.CustomListView;
import lucns.avareminders.views.FlexibleLayout;
import lucns.avareminders.views.ProfileImageView;

public class MainActivity extends Activity {

    private PopupMenu popupMenu;
    private CustomDialog customDialog;
    private CoursesRestApi coursesRestApi;
    private CustomListView listView;
    private ProgressBar progressBar;
    private LinearLayout rootNoConnection;
    private UIController uiController;
    private ProfileRestApi profileRestApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        uiController = UIController.getInstance(this);
        /*
        int blurRadius = 500;
        SemiCircleView circleView = findViewById(R.id.circleView);
        RenderEffect blurEffect = RenderEffect.createBlurEffect(
                blurRadius,
                blurRadius,
                Shader.TileMode.CLAMP
        );
        circleView.setRenderEffect(blurEffect);
         */
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.textVersion)).setText(String.format(Locale.getDefault(), getString(R.string.format_version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        customDialog = new CustomDialog(this);
        rootNoConnection = findViewById(R.id.rootNoConnection);
        LinearLayout buttonRefresh = findViewById(R.id.buttonRefresh);
        TextView textName = findViewById(R.id.textName);
        ProfileImageView profileImage = findViewById(R.id.profileImage);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listView);
        coursesRestApi = new CoursesRestApi(new ResponseCallback() {
            @Override
            public void onUnauthenticated() {
                showUnauthenticatedDialog();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(int responseCode) {
                Utils.vibrate();
                Notify.showErrorToast(responseCode);
                progressBar.setVisibility(View.INVISIBLE);
                buttonRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish(Course[] cs) {
                new TimeRegister("courses").setLastUpdate();
                updateCourses(cs);
            }
        });

        Annotator annotator = new Annotator("user", "User.json");
        profileRestApi = new ProfileRestApi(new ResponseCallback() {
            @Override
            public void onUnauthenticated() {
            }

            @Override
            public void onError(int responseCode) {
            }

            @Override
            public void onFinish(Student student) {
                textName.setText(student.name);
                profileImage.setImageUrl(student.urlPicture);
                String[] segments = student.name.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String segment : segments) {
                    builder.append(segment.charAt(0));
                    if (builder.length() == 2) break;
                }
                profileImage.setNameInitials(builder.toString());
                try {
                    JSONObject jsonObject = new JSONObject(annotator.getContent());
                    jsonObject.put("name", student.name);
                    jsonObject.put("cpf", student.cpf);
                    jsonObject.put("email", student.email);
                    if (student.urlPicture != null) jsonObject.put("urlPicture", student.urlPicture);
                    annotator.setContent(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            if (!jsonObject.has("name")) {
                profileRestApi.request();
            } else {
                String name = jsonObject.getString("name");
                textName.setText(name);
                String[] segments = name.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String segment : segments) {
                    builder.append(segment.charAt(0));
                    if (builder.length() == 2) break;
                }
                profileImage.setNameInitials(builder.toString());
                if (jsonObject.has("urlPicture")) profileImage.setImageUrl(jsonObject.getString("urlPicture"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.buttonRefresh) {
                    if (!Utils.hasInternetConnection()) {
                        Notify.showToast(R.string.error_no_internet);
                        return;
                    }
                    buttonRefresh.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    coursesRestApi.request();
                } else if (v.getId() == R.id.buttonMenu) {
                    popupMenu.show();
                }
            }
        };
        buttonRefresh.setOnClickListener(onClickListener);
        ImageButton buttonMenu = findViewById(R.id.buttonMenu);
        buttonMenu.setOnClickListener(onClickListener);
        popupMenu = new PopupMenu(this, buttonMenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_about) {
                    customDialog.showDialogAbout();
                } else if (itemId == R.id.menu_contact) {
                    new CustomDialog(MainActivity.this).showContactDialog();
                } else if (itemId == R.id.menu_settings) {
                    Notify.showToast(R.string.not_implemented);
                } else if (itemId == R.id.menu_logout) {
                    logout();
                }
                return true;
            }
        });
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popupMenu.getMenu());

        Course[] courses = AvaUtils.getCourses();
        if (Utils.hasInternetConnection()) {
            if (new TimeRegister("courses").isOverTime(60)) {
                progressBar.setVisibility(View.VISIBLE);
                coursesRestApi.request();
                return;
            }
        } else if (courses == null) {
            rootNoConnection.setVisibility(View.INVISIBLE);
            return;
        }
        updateCourses(courses);
    }

    private void logout() {
        new Annotator("user", "Authentication.json").delete();
        Annotator annotator = new Annotator("user", "User.json");
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            jsonObject.remove("name");
            jsonObject.remove("cpf");
            jsonObject.remove("email");
            jsonObject.remove("urlPicture");
            annotator.setContent(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void updateCourses(Course[] cs) {
        List<Course> sortedCourses = new LinkedList<>();
        List<Course> expiredCourses = new LinkedList<>();
        List<Course> notExpiredCourses = new LinkedList<>();
        for (Course c : cs) {
            if (c.expired) expiredCourses.add(c);
            else notExpiredCourses.add(c);
        }
        sortedCourses.addAll(notExpiredCourses);
        sortedCourses.addAll(expiredCourses);
        Course[] courses = sortedCourses.toArray(new Course[0]);
        //Course[] courses = new Course[]{cs[3]}; // for debug
        //Log.d("Lucas", "Course name: " + courses[0].name);

        progressBar.setVisibility(View.INVISIBLE);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        CourseRetrieveView[] controllers = new CourseRetrieveView[courses.length];
        View[] views = new View[courses.length];
        listView.setAdapter(new ArrayAdapter<Course>(MainActivity.this, 0, courses) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    if (position == 0) {
                        View content = inflater.inflate(R.layout.item_course, null, false);
                        Space space = new Space(MainActivity.this);
                        space.setLayoutParams(new LinearLayout.LayoutParams(1, (int) getResources().getDimension(R.dimen.listview_padding_top)));
                        LinearLayout layout = new LinearLayout(MainActivity.this);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.addView(space);
                        layout.addView(content);
                        views[position] = layout;
                    } else if (position == courses.length - 1) {
                        View content = inflater.inflate(R.layout.item_course, null, false);
                        Space space = new Space(MainActivity.this);
                        space.setLayoutParams(new LinearLayout.LayoutParams(1, (int) getResources().getDimension(R.dimen.listview_padding_bottom)));
                        LinearLayout layout = new LinearLayout(MainActivity.this);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.addView(content);
                        layout.addView(space);
                        views[position] = layout;
                    } else {
                        views[position] = inflater.inflate(R.layout.item_course, null, false);
                    }
                }
                TextView textTitle = views[position].findViewById(R.id.textTitle);
                textTitle.setText(courses[position].name);
                textTitle.setTextColor(uiController.getColor(position));
                ImageButton buttonIcon = views[position].findViewById(R.id.buttonIcon);
                buttonIcon.setImageDrawable(uiController.getIcon(position));

                FlexibleLayout flexibleLayout = views[position].findViewById(R.id.flexibleLayout);
                ImageButton buttonExpand = views[position].findViewById(R.id.buttonExpand);
                buttonExpand.setEnabled(!courses[position].expired);
                buttonExpand.setAlpha(courses[position].expired ? 0.5f : 1f);
                buttonExpand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (flexibleLayout.isFlexing()) return;
                        if (flexibleLayout.isExpanded()) {
                            flexibleLayout.constrict();
                            ObjectAnimator animation = ObjectAnimator.ofFloat(buttonExpand, "rotation", 0f, 180f);
                            animation.setDuration(300);
                            animation.setInterpolator(new DecelerateInterpolator());
                            animation.start();
                        } else {
                            flexibleLayout.expand();
                            ObjectAnimator animation = ObjectAnimator.ofFloat(buttonExpand, "rotation", 180f, 0f);
                            animation.setDuration(300);
                            animation.setInterpolator(new DecelerateInterpolator());
                            animation.start();
                        }
                    }
                });

                if (courses[position].expired) {
                    flexibleLayout.overlap(R.layout.item_empty);
                } else {
                    if (controllers[position] == null) {
                        controllers[position] = new CourseRetrieveView(courses[position], flexibleLayout, new ResponseCallback() {
                            @Override
                            public void onUnauthenticated() {
                                if (customDialog.isShowing()) return;
                                Utils.vibrate();
                                showUnauthenticatedDialog();
                            }

                            @Override
                            public void onError(int responseCode) {
                                Notify.showErrorToast(responseCode);
                            }
                        });
                        controllers[position].retrieve();
                    }
                }
                return views[position];
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        coursesRestApi.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        customDialog.dismiss();
        popupMenu.dismiss();
    }

    private void showUnauthenticatedDialog() {
        Utils.vibrate();
        Dialog dialog = customDialog.generateDialog(R.layout.dialog_info, false);
        TextView textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText(R.string.error_unauthenticated);
        dialog.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                logout();
            }
        });
        dialog.show();
    }
}