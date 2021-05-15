package group01.smartcar.client.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import group01.smartcar.client.R;

public class Speedometer extends SurfaceView implements SurfaceHolder.Callback {
    private double currentSpeedMS = 0;
    private int motorPowerPercentage = 0;

    public Speedometer(Context context){
        super(context);

        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public Speedometer(Context context, AttributeSet attributes, int style){
        super(context, attributes, style);

        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }
    public Speedometer(Context context, AttributeSet attributes){
        super(context, attributes);

        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

    }

    private void drawSpeedometer() {
        if (!getHolder().getSurface().isValid()) {
            return;
        }

        final int gaugeSize = getHeight();

        final Canvas canvas = getHolder().lockCanvas();

        final Paint backgroundColor = new Paint();
        backgroundColor.setARGB(255,6,11,23);

        final Paint textColor = new Paint();
        textColor.setARGB(255,255,255,255);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear the BG

        final RectF leftIndicatorSpace = new RectF(gaugeSize/9, gaugeSize/9, gaugeSize-(gaugeSize/9), gaugeSize-(gaugeSize/9));
        final RectF rightIndicatorSpace = new RectF(getWidth() - gaugeSize + (gaugeSize/9), gaugeSize/9, getWidth() - gaugeSize/9, gaugeSize-(gaugeSize/9));

        //Print the svg background
        Drawable leftGauge = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge_left, null);
        leftGauge.setBounds(0, 0, gaugeSize, gaugeSize);
        leftGauge.draw(canvas);

        Drawable rightGauge = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge_right, null);
        rightGauge.setBounds(getWidth()-gaugeSize, 0, getWidth(), gaugeSize);
        rightGauge.draw(canvas);

        Drawable leftIndicator = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge_left_indicator, null);
        leftIndicator.setBounds(gaugeSize/8, gaugeSize/8, gaugeSize-(gaugeSize/8), gaugeSize-(gaugeSize/8));
        leftIndicator.draw(canvas);

        canvas.drawArc(leftIndicatorSpace, 135F, getSpeedIndicatorAngle(), true, backgroundColor );

        Drawable rightIndicator = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge_right_indicator, null);
        rightIndicator.setBounds(getWidth() - gaugeSize + (gaugeSize/8), gaugeSize/8, getWidth() - gaugeSize/8, gaugeSize-(gaugeSize/8));
        rightIndicator.draw(canvas);

        canvas.drawArc(rightIndicatorSpace, 45F, getMotorPowerIndicatorAngle(), true, backgroundColor );

        Drawable gradient = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge_gradient, null);
        gradient.setBounds(gaugeSize/5, gaugeSize/5, gaugeSize-(gaugeSize/5), gaugeSize-(gaugeSize/5));
        gradient.draw(canvas);

        gradient.setBounds(getWidth() - gaugeSize + (gaugeSize/5), gaugeSize/5, getWidth() - gaugeSize/5, gaugeSize-(gaugeSize/5));
        gradient.draw(canvas);

        //print digital speed
        textColor.setTextSize(gaugeSize / 5f);
        textColor.setTextAlign(Paint.Align.CENTER);

        final Typeface currentTypeFace = textColor.getTypeface();
        final Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);

        textColor.setTypeface(bold);

        final int leftGaugeTextXPos = (gaugeSize / 2);
        final int rightGaugeTextXPos = getWidth() - (gaugeSize / 2);
        final int primaryTextYPos = (int) ((getHeight() / 2.2F) - ((textColor.descent() + textColor.ascent()) / 2.2F)) ;
        final int secondaryTextYPos = (int) ((getHeight() / 1.7F) - ((textColor.descent() + textColor.ascent()) / 1.7F)) ;

        canvas.drawText(getCurrentSpeedKMHString(), leftGaugeTextXPos, primaryTextYPos, textColor);
        canvas.drawText(Integer.toString(motorPowerPercentage), rightGaugeTextXPos, primaryTextYPos, textColor);

        textColor.setTextSize(gaugeSize / 12f);

        canvas.drawText("KM/H", leftGaugeTextXPos, secondaryTextYPos, textColor);
        canvas.drawText("POWER", rightGaugeTextXPos, secondaryTextYPos, textColor);

        getHolder().unlockCanvasAndPost(canvas);
    }



    public void update() {
       drawSpeedometer();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        drawSpeedometer();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public double getCurrentSpeedKMH() {
        return currentSpeedMS * 3.6;
    }

    public void setCurrentSpeedMS(double currentSpeedMS) {
        this.currentSpeedMS = currentSpeedMS;
    }


    private float getSpeedIndicatorAngle() {
        if (getCurrentSpeedKMH() > 6.5) {
            return -90F;
        }
        System.out.println((float) (getCurrentSpeedKMH() * 270 / 6.5F) - 360);
        return  (float) (getCurrentSpeedKMH() * 270 / 6.5F) - 360;
    }

    private String getCurrentSpeedKMHString(){
       final String twoDigit = Double.toString(getCurrentSpeedKMH());
       return twoDigit.substring(0, Math.min(twoDigit.length(), 3));
    }

    public void setMotorPowerPercentage(int motorPowerPercentage) {
        this.motorPowerPercentage = Math.abs(motorPowerPercentage*100/130);
    }

    private float getMotorPowerIndicatorAngle(){
        if (motorPowerPercentage >= 100) {
            return 90F;
        }

        return  360 - (motorPowerPercentage * 270 / 100);
    }

}
