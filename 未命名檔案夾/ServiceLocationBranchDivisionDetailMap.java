package bok.mitake.com.bokmbank.ServiceLocation;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import bok.mitake.com.bokmbank.R;
import bok.mitake.com.bokmbank.activity.BaseFragmentActivity;
import bok.mitake.com.bokmbank.adapter.MapInfoWindowAdapter;
import mma.security.component.diagnostics.Debuk;

import static bok.mitake.com.bokmbank.util.UtilHelper.getNowLatLng;

/**
 * 地圖檢視
 * Created by Ann on 2018/1/30.
 */

public class ServiceLocationBranchDivisionDetailMap extends BaseFragmentActivity implements OnMapReadyCallback {

    protected String TAG = getClass().getSimpleName();
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_location_branch_division_detail_map);
        findView();
        init();
        initNavbar(getIntent().getExtras().getString("PageTitle"),
                true,
                onClick,
                false,
                null,
                false,
                null);
    }

    private void findView() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_show_map);
    }

    private void init() {
        mapFragment.getMapAsync(this);
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_navbar_previous:
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng myPoint = getNowLatLng();//new LatLng(UtilHelper.getNow_Latitude(), UtilHelper.getNow_Longitude());
        mMap.addMarker(new MarkerOptions().position(myPoint).title("我的位置").icon(BitmapDescriptorFactory.fromResource(R.drawable.map_my_location)));

        LatLng point = new LatLng(Double.parseDouble(getIntent().getExtras().getString("lat")),
                Double.parseDouble(getIntent().getExtras().getString("lng")));
        mMap.addMarker(new MarkerOptions().position(point).title(getIntent().getExtras().getString("PageTitle"))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPoint, 15.0f));

        View infoWindow = getLayoutInflater().inflate(R.layout.map_bubble_layout, null);
        mMap.setInfoWindowAdapter(new MapInfoWindowAdapter(ServiceLocationBranchDivisionDetailMap.this, infoWindow));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //帶我去 Google Maps Directions API
//         Catch click on Navigate on the MapToolbar in a Google Maps FragmentActivity?
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                Debuk.WriteLine("arg0", arg0.latitude + "-" + arg0.longitude);
            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f));
    }
}
