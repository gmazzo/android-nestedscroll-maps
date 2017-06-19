package gs.utils.maps.nestedscroll;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class NestedScrollMapView extends FrameLayout implements OnMapReadyCallback, NestedScrollingChild, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {
    private static final int INVALID_POINTER = -1;
    private final NestedScrollingChildHelper helper = new NestedScrollingChildHelper(this);
    private final int scrollOffset[] = {0, 0}, touchSlop, minimumVelocity, maximumVelocity;
    private GoogleMap googleMap;
    private VelocityTracker velocityTracker;
    private int activePointerId = INVALID_POINTER;
    private int lastMotionX, lastMotionY;
    private boolean originatedHere;

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

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        touchSlop = configuration.getScaledTouchSlop() - 1;
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private void requestParentDisallowInterceptTouchEvent() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                originatedHere = ViewTouchHelper.isTopMostView(this, ev);

                if (originatedHere) {
                    activePointerId = ev.getPointerId(0);
                    lastMotionX = (int) ev.getX();
                    lastMotionY = (int) ev.getY();

                    initOrResetVelocityTracker();
                    velocityTracker.addMovement(ev);

                    requestParentDisallowInterceptTouchEvent();

                } else {
                    stopNestedScroll();
                }
            }

            case MotionEvent.ACTION_MOVE: {
                if (hasNestedScrollingParent()) {
                    return true;
                }
                if (activePointerId == INVALID_POINTER || ev.findPointerIndex(activePointerId) == INVALID_POINTER) {
                    break;
                }
                int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                int x = (int) ev.getX(pointerIndex);
                int y = (int) ev.getY(pointerIndex);
                if (originatedHere && Math.abs(x - lastMotionX) > touchSlop || Math.abs(y - lastMotionY) > touchSlop) {
                    lastMotionX = x;
                    lastMotionY = y;

                    initVelocityTrackerIfNotExists();
                    velocityTracker.addMovement(ev);

                    requestParentDisallowInterceptTouchEvent();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopNestedScroll();
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return originatedHere;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                requestParentDisallowInterceptTouchEvent();

                activePointerId = ev.getPointerId(0);
                lastMotionX = (int) ev.getX();
                lastMotionY = (int) ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                int x = (int) ev.getX(activePointerIndex);
                int y = (int) ev.getY(activePointerIndex);
                if (hasNestedScrollingParent()) {
                    int deltaX = lastMotionX - y;
                    int deltaY = lastMotionY - y;
                    dispatchNestedPreScroll(deltaX, deltaY, null, scrollOffset);

                    // Google Maps infite scroll consumes any unconsumed offset
                    dispatchNestedScroll(deltaX, deltaY, 0, 0, null);
                }

                lastMotionX = x - scrollOffset[0];
                lastMotionY = y - scrollOffset[1];
                break;

            case MotionEvent.ACTION_UP:
                if (hasNestedScrollingParent()) {
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                    float velocityX = -velocityTracker.getXVelocity(activePointerId);
                    float velocityY = -velocityTracker.getYVelocity(activePointerId);
                    recycleVelocityTracker();

                    if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                        if (!dispatchNestedPreFling(velocityX, velocityY)) {
                            dispatchNestedFling(velocityX, velocityY, false);
                        }
                    }
                }

                stopNestedScroll();
                break;

            case MotionEvent.ACTION_CANCEL:
                stopNestedScroll();
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = ev.getPointerId(index);
                lastMotionX = (int) ev.getX(index);
                lastMotionY = (int) ev.getY(index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                int index = ev.findPointerIndex(activePointerId);
                lastMotionX = (int) ev.getX(index);
                lastMotionY = (int) ev.getY(index);
                break;
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }
        return getChildAt(0).dispatchTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int index = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK)
                >> MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;

        if (ev.getPointerId(index) == activePointerId) {
            int newIndex = index == 0 ? 1 : 0;
            activePointerId = ev.getPointerId(newIndex);
            lastMotionX = (int) ev.getX(newIndex);
            lastMotionY = (int) ev.getY(newIndex);

            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
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
        originatedHere = false;
        activePointerId = INVALID_POINTER;
        recycleVelocityTracker();
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

class ViewTouchHelper {

    public static boolean isTopMostView(View target, MotionEvent ev) {
        View root = target.getRootView();
        return isTopMostView(target, root, (int) ev.getRawX(), (int) ev.getRawY());
    }

    private static boolean isTopMostView(View target, View current, int x, int y) {
        if (current instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) current;
            final int scrollX = group.getScrollX();
            final int scrollY = group.getScrollY();

            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                final int left = child.getLeft() - scrollX;
                final int top = child.getTop() - scrollY;

                if (child.getVisibility() == View.VISIBLE &&
                        !(y < top || y >= child.getBottom() - scrollY
                                || x < left || x >= child.getRight() - scrollX)) {
                    // this is the top-most view
                    return child == target ||
                            isTopMostView(target, child, x - left, y - top);
                }
            }
        }
        return false;
    }

}
