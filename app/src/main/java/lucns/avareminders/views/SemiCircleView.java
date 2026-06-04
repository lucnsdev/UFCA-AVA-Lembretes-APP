package lucns.avareminders.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class SemiCircleView extends View {

    private Paint paint;
    private float initialY, finalY, lastY, targetY;
    private ObjectAnimator animator;

    public SemiCircleView(Context context) {
        super(context);
        init();
    }

    public SemiCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#00e5ff"));
        setAlpha(0.1f);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //startAnimation();
            }
        });
    }

    private void startAnimation() {
        initialY = getWidth() / -2f;
        finalY = Resources.getSystem().getDisplayMetrics().heightPixels - (getWidth() / 2f);
        setY(initialY);
        lastY = initialY;
        targetY = finalY;
        runAnimation();
    }

    private void runAnimation() {
        animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, lastY, targetY);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(30000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                float temp = targetY;
                targetY = lastY;
                lastY = temp;
                runAnimation();
            }
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(0, getWidth() / 2f, getWidth() / 2f, paint);
    }
}
