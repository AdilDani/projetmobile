package com.fleettracking.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Helpers to move images between Bitmaps, base64 strings (what we store in the
 * database) and ImageViews. Images are downscaled and JPEG-compressed before
 * encoding so the base64 payload stays small enough to send over the API.
 */
public final class ImageUtils {

    /** Longest edge (px) an image is scaled down to before encoding. */
    private static final int MAX_DIMENSION = 1024;
    /** JPEG quality used when compressing before base64 encoding. */
    private static final int JPEG_QUALITY = 70;

    private ImageUtils() {}

    /** Compress + downscale a Bitmap and return a base64 string (no wrapping). */
    @Nullable
    public static String encode(@Nullable Bitmap bitmap) {
        if (bitmap == null) return null;
        Bitmap scaled = scaleDown(bitmap, MAX_DIMENSION);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    /** Decode a base64 string back into a Bitmap, or null if it can't be read. */
    @Nullable
    public static Bitmap decode(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Read an image picked from the gallery (content Uri) as a downscaled base64 string. */
    @Nullable
    public static String encodeFromUri(Context ctx, @Nullable Uri uri) {
        if (uri == null) return null;
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(in);
            return encode(bmp);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Show a base64 image in the ImageView. When the string is empty/invalid the
     * placeholder drawable is shown instead and the (tint) color filter is kept.
     * On a real image the color filter is cleared so it renders naturally.
     */
    public static void bind(ImageView view, @Nullable String base64, @DrawableRes int placeholder) {
        Bitmap bmp = decode(base64);
        if (bmp != null) {
            view.setImageBitmap(bmp);
            view.clearColorFilter();
        } else {
            view.setImageResource(placeholder);
        }
    }

    private static Bitmap scaleDown(Bitmap src, int maxDimension) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxDimension && h <= maxDimension) return src;
        float ratio = Math.min((float) maxDimension / w, (float) maxDimension / h);
        int nw = Math.round(w * ratio);
        int nh = Math.round(h * ratio);
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }
}
