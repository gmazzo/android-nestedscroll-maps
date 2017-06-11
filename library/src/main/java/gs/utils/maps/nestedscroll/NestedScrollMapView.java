package gs.utils.maps.nestedscroll;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class NestedScrollMapView extends FrameLayout implements OnMapReadyCallback, NestedScrollingChild, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {
    private final NestedScrollingChildHelper helper = new NestedScrollingChildHelper(this);
    private final int scrollOffset[] = {0, 0};
    private GoogleMap googleMap;
    private VelocityTracker velocityTracker;
    private float lastX, lastY;

    public NestedScrollMapView(Context context) {
        super(context);
    }

    public NestedScrollMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setWillNotDraw(true);

        helper.setNestedScrollingEnabled(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }

                initOrResetVelocityTracker();
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                initVelocityTrackerIfNotExists();
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopNestedScroll();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (hasNestedScrollingParent()) {
            switch (MotionEventCompat.getActionMasked(ev)) {
                case MotionEvent.ACTION_MOVE:
                    if (Float.isNaN(lastX) && ev.getHistorySize() > 1) {
                        lastX = ev.getHistoricalX(0);
                        lastY = ev.getHistoricalY(0);
                    }
                    if (!Float.isNaN(lastX)) {
                        int deltaX = (int) (lastX - ev.getX());
                        int deltaY = (int) (lastY - ev.getY());

                        if (dispatchNestedPreScroll(deltaX, deltaY, null, scrollOffset)) {
                            ev.offsetLocation(-scrollOffset[0], -scrollOffset[1]);
                        }

                        dispatchNestedScroll(deltaX, deltaY, 0, 0, null);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    ViewConfiguration configuration = ViewConfiguration.get(getContext());
                    velocityTracker.computeCurrentVelocity(1000, configuration.getScaledMaximumFlingVelocity());
                    float velocityX = -velocityTracker.getXVelocity();
                    float velocityY = -velocityTracker.getYVelocity();
                    recycleVelocityTracker();

                    if (!dispatchNestedPreFling(velocityX, velocityY)) {
                        dispatchNestedFling(velocityX, velocityY, false);
                    }
                    break;
            }

            if (velocityTracker != null) {
                velocityTracker.addMovement(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return helper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return helper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return helper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        helper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return helper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return googleMap != null && helper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        helper.stopNestedScroll();
        recycleVelocityTracker();
        lastX = lastY = Float.NaN;
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return helper.hasNestedScrollingParent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setOnCameraMoveStartedListener(this);
        this.googleMap.setOnCameraIdleListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (googleMap != null) {
            googleMap.setOnCameraMoveStartedListener(null);
            googleMap.setOnCameraIdleListener(null);
            googleMap = null;
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL | ViewCompat.SCROLL_AXIS_HORIZONTAL);
        }
    }

    @Override
    public void onCameraIdle() {
        stopNestedScroll();
    }

    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

}

