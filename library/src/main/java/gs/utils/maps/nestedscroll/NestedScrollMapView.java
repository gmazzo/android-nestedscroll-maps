package gs.utils.maps.nestedscroll;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class NestedScrollMapView extends NestedScrollView implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private float diffX;

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
        setFillViewport(true);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (googleMap != null) {
            // NestedScrollView does not track horizontal scrolls
            dxUnconsumed -= diffX;
            diffX = 0;

            // map has an "infinite" scroll, it will consume any unconsumed offset
            googleMap.moveCamera(CameraUpdateFactory.scrollBy(dxUnconsumed, dyUnconsumed));
            return super.dispatchNestedScroll(dxConsumed + dxUnconsumed, dyConsumed + dyUnconsumed, 0, 0, offsetInWindow);
        }
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (MotionEventCompat.getActionMasked(ev) == MotionEvent.ACTION_MOVE && hasNestedScrollingParent() && ev.getHistorySize() > 0) {
            diffX += ev.getX() - ev.getHistoricalX(0);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void stopNestedScroll() {
        super.stopNestedScroll();
        diffX = 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        googleMap = null;
    }

}

