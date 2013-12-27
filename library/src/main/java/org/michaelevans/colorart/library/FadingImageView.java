package org.michaelevans.colorart.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Michael Evans on 11/25/13.
 */
public class FadingImageView extends ImageView {

    public enum FadeSide {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private Shader mShader;
    private final Paint mPaint = new Paint();
    private int mBackgroundColor;
    private boolean mFadeEnabled = true;
    private FadeSide mFadeSide = FadeSide.LEFT;

    public FadingImageView(Context context) {
        super(context);
    }

    public FadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FadingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBackgroundColor(int color, FadeSide orientation) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        mBackgroundColor = Color.argb(255, r, g, b);
        mFadeSide = orientation;
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        setBackgroundDrawable(drawable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mFadeSide == FadeSide.LEFT) {
            mShader = new LinearGradient(0, h, w / 3, h, mBackgroundColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        } else if (mFadeSide == FadeSide.RIGHT) {
            mShader = new LinearGradient(2 * w / 3, h, w, h, Color.TRANSPARENT, mBackgroundColor, Shader.TileMode.CLAMP);
        } else if (mFadeSide == FadeSide.TOP) {
            mShader = new LinearGradient(w, 0, w, h / 3, mBackgroundColor, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        } else {
            mShader = new LinearGradient(w, 2 * h / 3, w, h, Color.TRANSPARENT, mBackgroundColor, Shader.TileMode.CLAMP);
        }
    }

    public void setFadeEnabled(boolean enabled) {
        mFadeEnabled = enabled;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShader != null && mFadeEnabled) {
            mPaint.setShader(mShader);
            canvas.drawPaint(mPaint);
        }
    }
}
