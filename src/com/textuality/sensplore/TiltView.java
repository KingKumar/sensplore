package com.textuality.sensplore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TiltView extends View implements TiltListener {

    private final Paint mCirclePaint = new Paint();
    private final Paint mLinePaint = new Paint();
    private final Path mCircle = new Path();
    private final Path mLine = new Path();
    private double mRotation = 0f;

    public TiltView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
    public TiltView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    public TiltView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setStyle(Paint.Style.STROKE);

        // circle with horizontal bar
        mCircle.addCircle(0, 0, 75, Path.Direction.CCW);
        mLine.addRect(-150, 0, 150, 1, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        canvas.translate(cx, cy);
        canvas.rotate((float) Math.toDegrees(mRotation));
        canvas.drawPath(mCircle, mCirclePaint);
        canvas.drawPath(mLine, mLinePaint);
    }

    @Override
    public void setTilt(double rotation) {
        mRotation = rotation + (Math.PI/2);
        invalidate();
    }

}
