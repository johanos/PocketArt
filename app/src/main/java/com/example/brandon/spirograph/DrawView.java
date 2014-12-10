package com.example.brandon.spirograph;

/**
 * Created by Brandon on 12/6/2014.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View
{
    private final int GEAR_THICKNESS = 10;

    public pPoint origin;
    public int outsideRadius;
    public boolean showGears = true;
    public boolean nextPointsCalculated = false;
    public Gear insideGear = new Gear();
    public Paint drawPaint = new Paint();
    private Paint gearPaint = new Paint();

    public void setInsideGearRadius(int value)
    {
        insideGear.setRadius(value);
        insideGear.setCenter(new pPoint((int)(origin.getX() + outsideRadius - insideGear.getRadius()), (int)(origin.getY())));
    }

    public DrawView(Context context)
    {
        super(context);

        gearPaint.setColor(Color.BLACK);
        gearPaint.setStyle(Paint.Style.STROKE);
        gearPaint.setStrokeWidth(GEAR_THICKNESS);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        //draw gears
        if(showGears)
        {
            canvas.drawCircle((float)origin.getX(), (float)origin.getY(), outsideRadius, gearPaint);
            canvas.drawCircle((float)insideGear.getCenter().getX(), (float)insideGear.getCenter().getY(), (float)insideGear.getRadius(), gearPaint);
        }

        //draw initial point
        if(insideGear.drawPoints.size() > 0)
        {
            pPoint drawPoint = insideGear.drawPoints.get(0);
            canvas.drawCircle((float)drawPoint.getX(), (float)drawPoint.getY(), drawPaint.getStrokeWidth(), drawPaint);
        }

        //draw figure with lines
        for (int i = 0; i < insideGear.drawPoints.size(); i++)
        {
            pPoint drawPoint = insideGear.drawPoints.get(i);

            if(nextPointsCalculated) {
                if(i == insideGear.drawPoints.size() - 1) break;

                pPoint nextPoint = insideGear.drawPoints.get(i + 1);
                canvas.drawLine((float) drawPoint.getX(), (float) drawPoint.getY(), (float) nextPoint.getX(), (float) nextPoint.getY(), drawPaint);
            }
        }
    }
}

