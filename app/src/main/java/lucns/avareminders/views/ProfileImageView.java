package lucns.avareminders.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;

import lucns.avareminders.R;
import lucns.avareminders.rest_api.internal.RestApiBase;
import lucns.avareminders.utils.Utils;

public class ProfileImageView extends View {

    private final float STROKE = 4f;

    private Paint paint, paintText, paintBackground;
    private String initials;
    private int textHeight;
    private Bitmap bitmap;
    private File file;

    public ProfileImageView(Context context) {
        super(context);
        init();
    }

    public ProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfileImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintText = new Paint();
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(Utils.dpToPx(20));
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBackground = new Paint();
        paintBackground.setColor(getContext().getColor(R.color.accent));

        paint = new Paint();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE);
        paint.setAntiAlias(true);

        file = new File(getContext().getExternalFilesDir(null).getPath(), "user/profile_picture.jpg");
        if (file.exists() && file.isFile()) {
            post(new Runnable() {
                @Override
                public void run() {
                    bitmap = BitmapFactory.decodeFile(file.getPath());
                    bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getWidth(), true);
                    invalidate();
                }
            });
        }
    }

    public void setImageUrl(String url) {
        if (url == null) return;
        if (bitmap != null) return;
        post(new Runnable() {
            @Override
            public void run() {
                download(url);
            }
        });
    }

    private void download(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RestApiBase(null).downloadFile(file, url);
                if (!file.exists()) return;
                bitmap = BitmapFactory.decodeFile(file.getPath());
                bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getWidth(), true);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        }).start();
    }

    public void setNameInitials(String initials) {
        //Log.d("Lucas", "setNameInitials->" + initials);
        this.initials = initials;
        Rect bounds = new Rect();
        paintText.getTextBounds(initials, 0, initials.length(), bounds);
        textHeight = bounds.height();
        if (bitmap == null) invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2f, getWidth() / 2f, getWidth() / 2f - STROKE / 2, paintBackground);
        if(bitmap != null || initials != null) {
            if (bitmap == null) {
                float centerX = canvas.getWidth() / 2f;
                float centerY = canvas.getHeight() / 2f;
                float baselineOffsetY = -((paintText.descent() + paintText.ascent()) / 2);
                canvas.drawText(initials, centerX, centerY + baselineOffsetY, paintText);
            } else {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setShader(shader);
                RectF rect = new RectF(0, 0, getWidth(), getWidth());
                canvas.drawRoundRect(rect, getWidth() / 2f, getWidth() / 2f, paint);
            }
        }
        canvas.drawCircle(getWidth() / 2f, getWidth() / 2f, getWidth() / 2f - STROKE / 2, paint);
    }
}
