package com.example.financialmanagement.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class AvatarHelper {

    private static final int[] PALETTE = {
            0xFFE57373, 0xFF4DB6AC, 0xFF81C784, 0xFFFFB74D,
            0xFF9575CD, 0xFF64B5F6, 0xFFF06292, 0xFFA1887F
    };

    public static Bitmap generateTextAvatar(String name, int sizePx, Context context) {
        String text = "";
        if (name != null && !name.isEmpty()) {
            text = name.substring(0, 1);
        }

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int bgColor = PALETTE[Math.abs(name.hashCode()) % PALETTE.length];

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(bgColor);
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(sizePx * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float x = sizePx / 2f;
        float y = sizePx / 2f - textBounds.exactCenterY();
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static void loadAvatar(ImageView imageView, String name, String avatarBase64, int sizeDp, Context context) {
        int sizePx = dpToPx(sizeDp, context);
        Bitmap bitmap = base64ToBitmap(avatarBase64);
        if (bitmap == null) {
            bitmap = generateTextAvatar(name, sizePx, context);
        }
        imageView.setImageBitmap(bitmap);
    }

    public static Bitmap scaleBitmap(Bitmap source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= maxSize && height <= maxSize) {
            return source;
        }
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    private static int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
