package gs.utils.maps.nestedscroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

public class SupportNestedScrollMapFragment extends SupportMapFragment {

    public static SupportNestedScrollMapFragment newInstance() {
        return new SupportNestedScrollMapFragment();
    }

    public static SupportNestedScrollMapFragment newInstance(GoogleMapOptions options) {
        SupportNestedScrollMapFragment fragment = new SupportNestedScrollMapFragment();

        Bundle args = new Bundle();
        args.putParcelable("MapOptions", options);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);

        NestedScrollMapView scrollingView = new NestedScrollMapView(getContext());
        scrollingView.addView(view);
        getMapAsync(scrollingView);
        return scrollingView;
    }

}
