package it.sephiroth.android.library.imagezoom.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast bitmap drawable. Does not support states. it only
 * support alpha and colormatrix
 *
 * Also, the bitmap is stratified into stripes having height 4096 to
 * avoid breaking the OpenGL max Bitmap size (4096x4096)
 * (OK we should theoretically cut vertical stripes too, but for now we
 *  have this problem in only one direction ^^)
 *
 * @author alessandro
 */
public class FastBitmapDrawable extends Drawable implements IBitmapDrawable {
    final static int       CHUNK_HEIGHT = 4096;

    protected Bitmap       mOriginalBitmap;
    protected List<Bitmap> mChunks;
    protected Paint        mPaint;
    protected int          mIntrinsicWidth, mIntrinsicHeight;

    public FastBitmapDrawable(Bitmap b) {
        setBitmap(b);

        if (null != b) {
            mIntrinsicWidth = b.getWidth();
            mIntrinsicHeight = b.getHeight();
        } else {
            mIntrinsicWidth = 0;
            mIntrinsicHeight = 0;
        }
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
    }

    public void setBitmap(Bitmap b) {
        mOriginalBitmap = b;
        int height = b.getHeight();
        int chunkCount = (height / CHUNK_HEIGHT);
        if ((height % CHUNK_HEIGHT) > 0) {
            chunkCount++;
        }
        mChunks = new ArrayList<Bitmap>();
        for (int i = 0; i < chunkCount; ++i) {
            int chunkHeight = (i < (height / CHUNK_HEIGHT)) ? CHUNK_HEIGHT : (height % CHUNK_HEIGHT);
            Bitmap chunk = Bitmap.createBitmap(b, 0, i * CHUNK_HEIGHT, b.getWidth(), chunkHeight);
            mChunks.add(chunk);
        }
    }

    public FastBitmapDrawable(Resources res, InputStream is) {
        this(BitmapFactory.decodeStream(is));
    }

    @Override
    public void draw(Canvas canvas) {
        if (mOriginalBitmap == null) {
            return;
        }
        for (Bitmap chunk : mChunks) {
            if (chunk.isRecycled()) {
                return;
            }
        }

        final Rect bounds = getBounds();
        if (!bounds.isEmpty()) {
            float factor = (float)bounds.height() / (float)mOriginalBitmap.getHeight();
            for (int i = 0; i < mChunks.size(); ++i) {
                Bitmap chunk = mChunks.get(i);
                int destTop = bounds.top + (int)((float)i * (float)CHUNK_HEIGHT * factor);
                int destHeight = (int)((float)chunk.getHeight() * factor);
                Rect destRect = new Rect(bounds.left, destTop, bounds.right, destTop + destHeight);
                canvas.drawBitmap(chunk, null, destRect, mPaint);
            }
        } else {
            for (int i = 0; i < mChunks.size(); ++i) {
                canvas.drawBitmap(mChunks.get(i), 0f, i * CHUNK_HEIGHT, mPaint);
            }
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getMinimumHeight() {
        return mIntrinsicHeight;
    }

    public void setAntiAlias(boolean value) {
        mPaint.setAntiAlias(value);
        invalidateSelf();
    }

    @Override
    public Bitmap getBitmap() {
        return mOriginalBitmap;
    }

    public Paint getPaint() {
        return mPaint;
    }
}
