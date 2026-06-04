package lucns.avareminders.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;

import lucns.avareminders.R;

public class CustomListView extends ScrollView {

    private LinearLayout linearLayout;
    private float spaceBetween;

    public CustomListView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addView(linearLayout);
        if (attrs == null) return;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.customListView, 0, 0);
        try {
            spaceBetween = a.getDimension(R.styleable.customListView_spaceBetween, 0);
        } finally {
            a.recycle();
        }
    }

    public void setAdapter(ArrayAdapter adapter) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (linearLayout.getChildCount() > 0 && spaceBetween > 0) {
                Space space = new Space(getContext());
                space.setLayoutParams(new LinearLayout.LayoutParams(1, (int) spaceBetween));
                linearLayout.addView(space);
            }
            linearLayout.addView(adapter.getView(i, null, null));
        }
    }
}
