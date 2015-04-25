package parking.group6.csc413.projectmap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends ActionBarActivity {
    Context myContext = this;
    LocationManager mLocationManager;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng center;
    TextView markerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markerText = (TextView) findViewById(R.id.locationMarkertext);

        setUpMapIfNeeded();

        mMap.clear();
        markerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                center = mMap.getCameraPosition().target;

                PopupMenu popup = new PopupMenu(MapsActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.pop_up, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.park_me:
                                Toast.makeText(MapsActivity.this, "Park me", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.search_parking:
                                getMessageFromSFpark(center.latitude, center.longitude);
                                return true;
                            case R.id.mark_fav:
                                Toast.makeText(MapsActivity.this, "Mark Fav", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                // Handle dismissal with: popup.setOnDismissListener(...);
                // Show the menu
                popup.show();



            }
        });

        /*mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // get the latitude and longitude of the center
                center = mMap.getCameraPosition().target;
                getMessageFromSFpark(center.latitude, center.longitude);
            }
        });*/

       /* mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // get longitude and latitude
                getMessageFromSFpark(latLng.latitude, latLng.longitude);

            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        LatLng myCurrentLocation;
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.getUiSettings(). setZoomControlsEnabled(true);
        mMap.getUiSettings(). setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);

        Location myLocation = getLastKnownLocation();
        if(myLocation!= null){
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myCurrentLocation).title("You are here!"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
        }else{
            Toast.makeText(getApplicationContext(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void getMessageFromSFpark(double lat, double lon){
        String myurl = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat="
                +lat
                +"&long="
                +lon
                +"&radius=0.25&uom=mile&response=json";

        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String messages = (String) msg.getData().getSerializable("data");
                Toast.makeText(myContext, messages, Toast.LENGTH_LONG).show();

            }
        };
        GetParking gp = new GetParking(myContext,handler, myurl );
        gp.execute(myurl);
       // GetParking gp = new GetParking();
       //gp.execute(myurl);
    }

    public void showMsg(String msg){
        Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
