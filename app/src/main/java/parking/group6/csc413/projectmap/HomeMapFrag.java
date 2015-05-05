package parking.group6.csc413.projectmap;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import parking.group6.csc413.projectmap.Adapters.DialogueListAdapter;

/**
 * Created by Swati on 5/5/2015.
 */
public class HomeMapFrag extends Fragment implements getDataFromAsync{
    private SupportMapFragment mapFragment;
    ConnectDB db;
    Context myContext = null;
    LocationManager mLocationManager;
    MapView mapView ;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng center;
    TextView markerText;
    Parking[] parkings = null;
    ArrayList<Parking> parkingList = new ArrayList<Parking>();


    public HomeMapFrag(){

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.home_map, container, false);
        markerText = (TextView) rootView.findViewById(R.id.locationMarkertext);
        //mMap.clear();
        myContext = getActivity();


        markerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                center = mMap.getCameraPosition().target;

                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.pop_up, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.park_me:
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(center).title("Parked"));
                                Toast.makeText(getActivity(), "Park me", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.search_parking:
                                getMessageFromSFpark(center.latitude, center.longitude);

                                return true;
                            case R.id.mark_fav:
                                Toast.makeText(getActivity(), "Mark Fav", Toast.LENGTH_SHORT).show();
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

        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
       setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            getActivity().getFragmentManager().beginTransaction()
                    .remove(getActivity().getFragmentManager().findFragmentById(R.id.map)).commit();
            mMap = null;
        }
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            FragmentManager myFM = getChildFragmentManager();
            mapFragment = (SupportMapFragment)myFM.findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
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
            Toast.makeText(getActivity(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }

    }
    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getActivity().getSystemService(myContext.LOCATION_SERVICE);
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
        /*
        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                String messages = (String) msg.getData().getSerializable("data");
                Toast.makeText(myContext, messages, Toast.LENGTH_LONG).show();

            }
        };
        */
        //GetParking gp = new GetParking(myContext,handler, myurl );
        GetParking gp = new GetParking(myContext, this);
        gp.execute(myurl);
        // GetParking gp = new GetParking();
        //gp.execute(myurl);
    }

    public void showListDialogue(){
        final Dialog dialog = new Dialog(myContext);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialogue_list, null);
        ListView lv = (ListView) view.findViewById(R.id.parking_list);
        DialogueListAdapter myPark = new DialogueListAdapter(myContext, parkingList);
        // on click listener here:
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // show action items
                Parking newOne = (Parking)parent.getAdapter().getItem(position);
                // now add parking to database.
                addParkingtoDB(newOne);
                Toast.makeText(myContext, "added to db", Toast.LENGTH_LONG).show();
                dialog.dismiss();



            }
        });

        lv.setAdapter(myPark);
        dialog.setTitle("Parking Nearby :");
        dialog.setContentView(view);
        dialog.show();

    }

    public void addParkingtoDB(Parking parking){
        db = new ConnectDB(myContext);
        db.addParking(parking);
    }

    public String checkParkingDB(){
        db = new ConnectDB(myContext);
        ArrayList<Parking> parkList = db.getParkingList();
        Parking parkFav = parkList.get(parkList.size() - 1);
        String s = "FROM DB :Total items="
                +  parkList.size()
                + "\nLast Address = "
                + parkFav.getAddress();
        return s;

    }
    public void showMsg(String msg){
        Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onTaskCompleted(JSONObject jobj) {
        // method of the interface getDataFromAsync


        try {
            parkings = JSONParseSF.parseJsonFromSF(jobj);

        } catch (JSONException e) {
            Log.e("Error", "Exception from  JSONParseSF.parseJsonFromSF ");
            e.printStackTrace();
        }
        if(parkings != null){
            parkingList = new ArrayList<Parking>(Arrays.asList(parkings));// for listview
            //String address = parkings[0].getAddress();
            //Toast.makeText(myContext, address, Toast.LENGTH_LONG).show();
            if(parkingList.size()>0){
                showListDialogue();
            }else{
                Toast.makeText(myContext, "SORRY !! NO PARKING DATA !", Toast.LENGTH_LONG).show();
            }

        }
    }
}
