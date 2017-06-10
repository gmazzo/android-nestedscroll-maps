package gs.utils.maps.nestedscroll.demo;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import gs.utils.maps.nestedscroll.SupportNestedScrollMapFragment;

public class DemoActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private final GoogleMapOptions mapOptions = new GoogleMapOptions()
            .camera(CameraPosition.fromLatLngZoom(new LatLng(-38.006004, -57.543122), 14));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Click!", Toast.LENGTH_SHORT).show();
            }

        });

        RadioGroup groupMapType = (RadioGroup) findViewById(R.id.groupMapType);
        groupMapType.setOnCheckedChangeListener(this);
        groupMapType.check(R.id.showNestedMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        Fragment fragment = null;

        switch (checkedId) {
            case R.id.showNestedMap:
                fragment = SupportNestedScrollMapFragment.newInstance(mapOptions);
                break;

            case R.id.showRegularMap:
                fragment = SupportMapFragment.newInstance(mapOptions);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFragment, fragment, null)
                .commit();
    }

}
