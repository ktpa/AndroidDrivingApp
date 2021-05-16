package group01.smartcar.client.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import group01.smartcar.client.R;

import static java.util.Objects.requireNonNull;

public class ProximitySensor extends SurfaceView implements SurfaceHolder.Callback {

    public ProximitySensor(Context context) {
        super(context);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    public ProximitySensor(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    public ProximitySensor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        render();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void update() {
        render();
    }

    private void render() {
        if (!getHolder().getSurface().isValid()) {
            return;
        }

        final Canvas canvas = getHolder().lockCanvas();

        canvas.drawColor(getResources().getColor(R.color.driveBG, getResources().newTheme()));

        final Drawable proximityWireframe = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_proximity_car, null);
        requireNonNull(proximityWireframe);

        final int wireframeStartX = getWidth() / 3;
        final int wireframeEndX = proximityWireframe.getIntrinsicWidth() + wireframeStartX;

        final int wireframeStartY = getHeight() / 4;
        final int wireframeEndY = proximityWireframe.getIntrinsicHeight() + wireframeStartY;

        proximityWireframe.setBounds(wireframeStartX, wireframeStartY, wireframeEndX, wireframeEndY);
        proximityWireframe.draw(canvas);

        getHolder().unlockCanvasAndPost(canvas);
    }
}
