package lucns.avareminders.ui_controller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import lucns.avareminders.R;

public class UIController {

    private static UIController instance;
    private final Context context;
    private int indexColors, indexIcons;
    private final int[] colors;
    private final Drawable[] icons;

    public static UIController getInstance(Context context) {
        if (instance == null) {
            synchronized (UIController.class) {
                instance = new UIController(context);
            }
        }
        return instance;
    }

    protected UIController(Context context) {
        this.context = context;
        colors = context.getResources().getIntArray(R.array.colors);
        TypedArray drawableArray = context.getResources().obtainTypedArray(R.array.icons);
        icons = new Drawable[drawableArray.length()];
        for (int i = 0; i < drawableArray.length(); i++) {
            icons[i] = drawableArray.getDrawable(i);
        }
    }

    public void next() {
        if (indexColors + 1 == colors.length) indexColors = 0;
        else indexColors++;
        if (indexIcons + 1 == icons.length) indexIcons = 0;
        else indexIcons++;
    }

    public int getColor() {
        return colors[indexColors];
    }

    public Drawable getIcon() {
        return tintDrawable(icons[indexIcons], colors[indexColors]);
    }

    public int getColor(int i) {
        int index;
        if (i > colors.length - 1) index = i % colors.length;
        else index = i;
        return colors[index];
    }

    public Drawable getIcon(int i) {
        int index;
        if (i > icons.length - 1) index = i % icons.length;
        else index = i;
        return tintDrawable(icons[index], getColor(i));
    }

    public Drawable tint(int resId, int color) {
        return tintDrawable(context.getDrawable(resId), color);
    }

    private Drawable tintDrawable(Drawable drawable, int color) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new BitmapDrawable(context.getResources(), tintBitmap(bitmap, color));
    }

    private Bitmap tintBitmap(Bitmap bitmap, int color) {
        Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixelColor = bitmap.getPixel(x, y);
                int alpha = Color.alpha(pixelColor);
                bitmap2.setPixel(x, y, Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
            }
        }
        bitmap.recycle();
        return bitmap2;
    }
}
