package lucns.avareminders.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class FlexibleLayout extends ViewGroup {
    private int currentHeight;
    private int width, height;
    private ValueAnimator scale;

    public FlexibleLayout(Context context) {
        super(context);
        init();
    }

    public FlexibleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlexibleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //setBackgroundColor(Color.RED);
    }

    private void startAnimation(int target) {
        if (currentHeight == target) return;
        if (scale != null) scale.cancel();
        scale = new ValueAnimator();
        scale.setDuration(500);
        scale.setIntValues(currentHeight, target);
        scale.setInterpolator(new DecelerateInterpolator());
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentHeight = (Integer) animation.getAnimatedValue();
                setMutableHeight();
            }
        });
                /*
        scale.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (target == 0) {
                    if (getChildCount() > 0) getChildAt(0).removeOnLayoutChangeListener(onLayoutChangeListener);
                    removeAllViews();
                }
            }
        });
                 */
        scale.start();
    }

    private void setMutableHeight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = currentHeight;
        setLayoutParams(params);
        setMeasuredDimension(width, currentHeight);
    }

    public boolean isExpanded() {
        return currentHeight > 0;
    }

    public boolean isFlexing() {
        return scale != null && scale.isRunning();
    }

    public void expand() {
        startAnimation(height);
    }

    public void constrict() {
        startAnimation(0);
    }

    public View overlap(int resId) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(resId, null, false);
        overlap(view);
        return view;
    }

    public void overlap(View view) {
        if (getChildCount() > 0) getChildAt(0).removeOnLayoutChangeListener(onLayoutChangeListener);
        removeAllViews();
        addView(view);
        updateSize(view);
    }

    private final OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) updateSize(getChildAt(0));
        }
    };

    public void computeSizes() {
        View view = getChildAt(0);
        view.removeOnLayoutChangeListener(onLayoutChangeListener);
        updateSize(view);
    }

    private void updateSize(View view) {
        view.addOnLayoutChangeListener(onLayoutChangeListener);
        post(new Runnable() {
            @Override
            public void run() {
                int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.AT_MOST);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                view.measure(widthSpec, heightSpec);

                int measuredHeight = view.getMeasuredHeight();
                width = getWidth();
                height = measuredHeight;
                startAnimation(measuredHeight);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.layout(0, 0, width, height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getWidth() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        setMeasuredDimension(width, currentHeight);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, height);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }
}
