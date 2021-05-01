package group01.smartcar.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

// Adapted from: https://www.instructables.com/A-Simple-Android-UI-Joystick/

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoystickListener joystickCallback;

    private void setupDimensions(){
        centerX = getWidth()/2;
        centerY = findViewById(R.id.joystickView2).getHeight()/(float)(2);
        baseRadius = Math.min(getWidth(), getHeight()) / 3;
        hatRadius = Math.min(getWidth(), getHeight()) / 5;

    }

    public JoystickView(Context context){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }


    public JoystickView(Context context, AttributeSet attributes, int style){
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }
    public JoystickView(Context context, AttributeSet attributes){
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    private void drawJoystick(float newX, float newY) {
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.parseColor("#6C6C6C")); // Clear the BG

            colors.setARGB(255, 8, 29, 61); //base
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            colors.setARGB(255, 13, 17, 23); //base
            myCanvas.drawCircle(centerX, centerY, (float)(baseRadius*0.95), colors);

            colors.setARGB(150,35,74,132); //hat
            myCanvas.drawCircle(newX, newY, hatRadius, colors);

            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public interface JoystickListener
    {
        void onJoystickMoved(float xPercent, float yPercent, int id);
    }

    public boolean onTouch(View v, MotionEvent e){
        if(v.equals(this)){
            if(e.getAction() != e.ACTION_UP) {
                float displacement = (float) Math.sqrt((Math.pow(e.getX()-centerX,2)) + Math.pow(e.getY()-centerY,2));
                if(displacement < baseRadius) {
                    drawJoystick(e.getX(), e.getY());
                    joystickCallback.onJoystickMoved((e.getX()-centerX)/baseRadius, (e.getY()-centerY)/baseRadius, getId());
                }
                else{
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX() - centerX) * ratio;
                    float constrainedY = centerY + (e.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                    joystickCallback.onJoystickMoved((constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius, getId());
                }
            }
            else {
                drawJoystick(centerX, centerY);
                joystickCallback.onJoystickMoved(0,0, getId());

            }
        }
        return true;
    }



}