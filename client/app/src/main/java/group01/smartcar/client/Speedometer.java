package group01.smartcar.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

public class Speedometer extends SurfaceView implements SurfaceHolder.Callback {
    private double currentSpeedMS;

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
        currentSpeedMS=0;
    }

    public Speedometer(Context context, AttributeSet attributes, int style){
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        currentSpeedMS=0;
    }
    public Speedometer(Context context, AttributeSet attributes){
        super(context);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        currentSpeedMS=0;

    }

    private void drawSpeedometer(float centerX, float centerY) {
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear the BG

            colors.setARGB(255, 255, 255, 255); //base
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        setupDimensions();
        drawSpeedometer(centerX, centerY);
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
}
