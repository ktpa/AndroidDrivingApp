package group01.smartcar.client.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import group01.smartcar.client.R;

import static java.util.Objects.requireNonNull;

public class ProximityIndicator extends SurfaceView implements SurfaceHolder.Callback {

    private static final Paint DANGER_INDICATOR_LOW = new Paint();
    private static final Paint DANGER_INDICATOR_MEDIUM = new Paint();
    private static final Paint DANGER_INDICATOR_HIGH = new Paint();

    private IndicatorLevel frontIndicatorLevel = IndicatorLevel.NO_DETECTION;
    private IndicatorLevel backIndicatorLevel = IndicatorLevel.NO_DETECTION;

    static {
        DANGER_INDICATOR_LOW.setARGB(255, 93, 154, 96);
        DANGER_INDICATOR_LOW.setStyle(Paint.Style.STROKE);
        DANGER_INDICATOR_LOW.setStrokeCap(Paint.Cap.SQUARE);
        DANGER_INDICATOR_LOW.setStrokeWidth(10);
        DANGER_INDICATOR_LOW.setAntiAlias(true);

        DANGER_INDICATOR_MEDIUM.setARGB(255, 254, 154, 61);
        DANGER_INDICATOR_MEDIUM.setStyle(Paint.Style.STROKE);
        DANGER_INDICATOR_MEDIUM.setStrokeCap(Paint.Cap.SQUARE);
        DANGER_INDICATOR_MEDIUM.setStrokeWidth(10);
        DANGER_INDICATOR_MEDIUM.setAntiAlias(true);

        DANGER_INDICATOR_HIGH.setARGB(255, 147, 16, 32);
        DANGER_INDICATOR_HIGH.setStyle(Paint.Style.STROKE);
        DANGER_INDICATOR_HIGH.setStrokeCap(Paint.Cap.SQUARE);
        DANGER_INDICATOR_HIGH.setStrokeWidth(10);
        DANGER_INDICATOR_HIGH.setAntiAlias(true);
    }

    public ProximityIndicator(Context context) {
        super(context);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    public ProximityIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    public ProximityIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void setFrontIndicatorLevel(IndicatorLevel frontIndicatorLevel) {
        this.frontIndicatorLevel = frontIndicatorLevel;
    }

    public void setBackIndicatorLevel(IndicatorLevel backIndicatorLevel) {
        this.backIndicatorLevel = backIndicatorLevel;
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

        final int yOffset = 30;

        final int wireframeStartX = getWidth() / 3;
        final int wireframeEndX = proximityWireframe.getIntrinsicWidth() + wireframeStartX;

        final int wireframeStartY = getHeight() / 4 - yOffset;
        final int wireframeEndY = proximityWireframe.getIntrinsicHeight() + wireframeStartY;

        proximityWireframe.setBounds(wireframeStartX, wireframeStartY, wireframeEndX, wireframeEndY);
        proximityWireframe.draw(canvas);

        final Rect wireframeBounds = proximityWireframe.copyBounds();

        final RectF lowDangerBounds = new RectF(
            wireframeBounds.left - 80,
            wireframeBounds.left - 80,
            wireframeBounds.right + 80,
            wireframeBounds.right + 80
        );

        final RectF mediumDangerBounds = new RectF(
            wireframeBounds.left - 60,
            wireframeBounds.left - 60,
            wireframeBounds.right + 60,
            wireframeBounds.right + 60
        );

        final RectF highDangerBounds = new RectF(
            wireframeBounds.left - 40,
            wireframeBounds.left - 40,
            wireframeBounds.right + 40,
            wireframeBounds.right + 40
        );

        final RectF lowDangerBoundsReverse = new RectF(lowDangerBounds);
        lowDangerBoundsReverse.offset(0, wireframeBounds.bottom - wireframeBounds.right - 10);

        final RectF mediumDangerBoundsReverse = new RectF(mediumDangerBounds);
        mediumDangerBoundsReverse.offset(0, wireframeBounds.bottom - wireframeBounds.right - 10);

        final RectF highDangerBoundsReverse = new RectF(highDangerBounds);
        highDangerBoundsReverse.offset(0, wireframeBounds.bottom - wireframeBounds.right - 10);

        if (frontIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.HIGH)) {
            canvas.drawArc(highDangerBounds, 225, 90, false, DANGER_INDICATOR_HIGH);
        }

        if (frontIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.MEDIUM)) {
            canvas.drawArc(mediumDangerBounds, 225, 90, false, DANGER_INDICATOR_MEDIUM);
        }

        if (frontIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.LOW)) {
            canvas.drawArc(lowDangerBounds, 225, 90, false, DANGER_INDICATOR_LOW);
        }

        if (backIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.HIGH)) {
            canvas.drawArc(highDangerBoundsReverse, 45, 90, false, DANGER_INDICATOR_HIGH);
        }

        if (backIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.MEDIUM)) {
            canvas.drawArc(mediumDangerBoundsReverse, 45, 90, false, DANGER_INDICATOR_MEDIUM);
        }

        if (backIndicatorLevel.isHigherOrEqualThan(IndicatorLevel.LOW)) {
            canvas.drawArc(lowDangerBoundsReverse, 45, 90, false, DANGER_INDICATOR_LOW);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    public enum IndicatorLevel {
        NO_DETECTION(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3);

        private final int level;

        IndicatorLevel(int level) {
            this.level = level;
        }

        public boolean isHigherOrEqualThan(IndicatorLevel indicatorLevel) {
            return this.level >= indicatorLevel.level;
        }
    }
}
