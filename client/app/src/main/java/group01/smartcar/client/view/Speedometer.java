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
    private double motorPowerPercentage = 0;

    private float centerX;
    private float centerY;

    private void setupDimensions(){
        centerX = getWidth() / 2f;
        centerY = getWidth() / 2f;
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
        if (!getHolder().getSurface().isValid()) {
            return;
        }

        final Canvas canvas = getHolder().lockCanvas();

        final Paint backgroundColor = new Paint();
        backgroundColor.setARGB(255,0,0,0);

        final Paint speedIndicatorColor = new Paint();
        speedIndicatorColor.setARGB(255,246,213,92);

        final Paint motorPowerIndicatorColor = new Paint();
        motorPowerIndicatorColor.setARGB(255,237,85,59);

        final Paint textColor = new Paint();
        textColor.setARGB(255,255,255,255);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear the BG

        //Print the svg background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Drawable gauge = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gauge, null);
            gauge.setBounds(0, 0, getWidth(), getHeight());
            gauge.draw(canvas);
        }

        final float indicatorSizeFactor = 0.95F;
        final float indicatorThickness = 0.12F;

        final RectF rect = new RectF(
            0 + (getWidth() * (1 - indicatorSizeFactor)),
            0 + (getHeight() * (1 - indicatorSizeFactor)),
            getWidth() * indicatorSizeFactor,
            getHeight() * indicatorSizeFactor
        );

        //Draw speed indicator
        canvas.drawArc(rect, 135F, getSpeedIndicatorAngle(), true, speedIndicatorColor );
        //Draw motor power percentage
        canvas.drawArc(rect, 120F, getMotorPowerIndicatorAngle(), true, motorPowerIndicatorColor );

        //hide the middle part of the indicators
        canvas.drawCircle(centerX, centerY, (getWidth()*(indicatorSizeFactor-indicatorThickness)*0.5F), backgroundColor);

        //print digital speed
        textColor.setTextSize(getWidth() / 5f);
        textColor.setTextAlign(Paint.Align.CENTER);

        final Typeface currentTypeFace = textColor.getTypeface();
        final Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);

        textColor.setTypeface(bold);

        final int textXPos = (getWidth() / 2);
        final int textYPos = (int) ((getHeight() / 2) - ((textColor.descent() + textColor.ascent()) / 2)) ;
        canvas.drawText(getCurrentSpeedKMHString(), textXPos, textYPos, textColor);

        getHolder().unlockCanvasAndPost(canvas);
    }



    public void update() {
       drawSpeedometer();
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
        return currentSpeedMS * 3.6;
    }

    public void setCurrentSpeedMS(double currentSpeedMS) {
        this.currentSpeedMS = currentSpeedMS;
    }

    public void setMotorPowerPercentage(double motorPowerPercentage) {
        this.motorPowerPercentage = motorPowerPercentage;
    }

    private float getSpeedIndicatorAngle() {
        if (getCurrentSpeedKMH() > 7) {
            return 270F;
        }

        return (float) getCurrentSpeedKMH() * 270 / 7;
    }

    private float getMotorPowerIndicatorAngle(){
        if (Math.abs(motorPowerPercentage) > 100 ){
            return -60;
        }

        return (float) Math.abs(motorPowerPercentage) * 60 / 100 * -1;
    }

    private String getCurrentSpeedKMHString(){
       final String twoDigit = Double.toString(getCurrentSpeedKMH());
       return twoDigit.substring(0, Math.min(twoDigit.length(), 3));
    }

}
