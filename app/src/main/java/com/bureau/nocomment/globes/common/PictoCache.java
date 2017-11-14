package com.bureau.nocomment.globes.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import com.bureau.nocomment.globes.model.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PictoCache {

    private Map<Pair<Integer, Integer>, Bitmap> bitmaps = new HashMap<>();

    public void loadProjects(List<Project> projects, Context context) {
        for (Project project : projects) {
            int projectId = project.getId();
            int index = project.getIndex();
            String name = (index > 0) ?
                    String.format("ic_%02d_%d_rvb_picto", projectId, index) :
                    String.format("ic_%02d_rvb_picto", projectId, index);
            final int resourceId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());

            if (resourceId > 0) {
                Log.d("Cell", "Decoding project" + projectId + "," + index);
            } else {
                Log.e("Cell", "bizarre");
                continue;
            }

            Bitmap bitmap = getBitmapFromVectorDrawable(context, resourceId);

            Pair<Integer, Integer> key = new Pair<>(projectId, index);
            bitmaps.put(key, bitmap);
        }
    }

    public Bitmap bitmapForProject(Project project) {
        Pair<Integer, Integer> key = new Pair<>(project.getId(), project.getIndex());
        return bitmaps.get(key);
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        // Mind the ALPHA_8 type, to save memory
        // We also scale down the bitmaps, which are pretty large
        int scalingFactor = 3;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() / scalingFactor,
                drawable.getIntrinsicHeight() / scalingFactor, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
