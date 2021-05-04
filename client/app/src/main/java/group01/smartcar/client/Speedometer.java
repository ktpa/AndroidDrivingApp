package group01.smartcar.client;

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
import android.view.View;

import androidx.annotation.NonNull;

import java.math.BigDecimal;

public class Speedometer extends SurfaceView implements SurfaceHolder.Callback {
    private double currentSpeedMS = 0;
    private double motorPowerPercentage = 0;

    private float centerX;
    private float centerY;
    private float baseRadius;

    private void setupDimensions(){
        centerX = getWidth()/2;
        centerY = getWidth()/2;
        baseRadius = getWidth()/(float)(2.5);
    }

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
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();

            Paint backgroundColor = new Paint();
            backgroundColor.setARGB(255,0,0,0);
            Paint speedIndicatorColor = new Paint();
            speedIndicatorColor.setARGB(255,246,213,92);
            Paint motorPowerIndicatorColor = new Paint();
            motorPowerIndicatorColor.setARGB(255,237,85,59);
            Paint textColor = new Paint();
            textColor.setARGB(255,255,255,255);

            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear the BG

            //Print the svg background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Drawable gauge = getResources().getDrawable(R.drawable.ic_gauge, null);
                gauge.setBounds(0, 0, getWidth(), getHeight());
                gauge.draw(myCanvas);
            }

            Float indicatorSizeFactor = 0.95F;
            Float indicatorThickness = 0.12F;

            RectF rect = new RectF(0+(getWidth()*(1-indicatorSizeFactor)), 0+(getHeight()*(1-indicatorSizeFactor)), getWidth()*indicatorSizeFactor, getHeight()*indicatorSizeFactor);

            //Draw speed indicator
            myCanvas.drawArc(rect, 135F, getSpeedIndicatorAngle(), true, speedIndicatorColor );
            //Draw motor power percentage
            myCanvas.drawArc(rect, 120F, getMotorPowerIndicatorAngle(), true, motorPowerIndicatorColor );

            //hide the middle part of the indicators
            myCanvas.drawCircle(centerX, centerY, (getWidth()*(indicatorSizeFactor-indicatorThickness)*0.5F), backgroundColor);

            //print digital speed
            textColor.setTextSize(getWidth()/5);
            textColor.setTextAlign(Paint.Align.CENTER);
            Typeface currentTypeFace =   textColor.getTypeface();
            Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);
            textColor.setTypeface(bold);

            int textXPos = (getWidth() / 2);
            int textYPos = (int) ((getHeight() / 2) - ((textColor.descent() + textColor.ascent()) / 2)) ;
            myCanvas.drawText(getCurrentSpeedKMHString(), textXPos, textYPos, textColor);

            getHolder().unlockCanvasAndPost(myCanvas);

        }
    }



    public void update() {

       drawSpeedometer();
       //invalidate();

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        setupDimensions();
        drawSpeedometer();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public double getCurrentSpeedKMH() {
        return currentSpeedMS*(3.6);
    }

    public double getCurrentSpeedMS() {
        return currentSpeedMS;
    }

    public void setCurrentSpeedMS(double currentSpeedMS) {
        this.currentSpeedMS = currentSpeedMS;
    }

    public void setMotorPowerPercentage(double percentage) {
        this.motorPowerPercentage= percentage;
    }

    private float getSpeedIndicatorAngle(){
        if(getCurrentSpeedKMH() > 7 ){
            return 270F;
        }

        return (float)getCurrentSpeedKMH()*270/7;
    }

    private float getMotorPowerIndicatorAngle(){
        if(Math.abs(motorPowerPercentage) > 100 ){
            return 60;
        }
        return (float)Math.abs(motorPowerPercentage)*60/100;
    }

    private String getCurrentSpeedKMHString(){
       String twoDigit = Double.toString(getCurrentSpeedKMH());
       return twoDigit.substring(0, Math.min(twoDigit.length(), 3));
    }



}
