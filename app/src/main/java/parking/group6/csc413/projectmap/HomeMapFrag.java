package parking.group6.csc413.projectmap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    static private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng center;
    TextView markerText;
    Parking[] parkings = null;
    ArrayList<Parking> parkingList = new ArrayList<Parking>();
    LatLng searchPoint;
    View mainPin;
    int parkMe = 0;
    protected int mDpi = 0;
    private HashMap<Marker, Parking> mark_park = new HashMap<Marker, Parking>();
    Dialog dialog;
    static LatLng myCurrentLocation;


    public HomeMapFrag(){
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.home_map, container, false);
        markerText = (TextView) rootView.findViewById(R.id.locationMarkertext);
        mainPin = rootView.findViewById(R.id.locationMarker);
        //mMap.clear();
        myContext = getActivity();
        mDpi = getActivity().getResources().getDisplayMetrics().densityDpi;

        markerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                center = mMap.getCameraPosition().target;

                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.pop_up, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                                getMessageFromSFpark(center.latitude, center.longitude);
                                return false;

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
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /*
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            getActivity().getFragmentManager().beginTransaction()
                    .remove(getActivity().getFragmentManager().findFragmentById(R.id.map)).commit();
            mMap = null;
        }
    }
    */
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

        mMap.clear();
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.getUiSettings(). setZoomControlsEnabled(true);
        mMap.getUiSettings(). setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);

        Location myLocation = getLastKnownLocation();
        if(myLocation!= null){
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(myCurrentLocation).title("You are here!"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
        }else{
            Toast.makeText(getActivity(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }
    /*    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                displayPopUp(marker);

            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getActivity().getLayoutInflater().inflate(R.layout.detailedview, null);




                // Returning the view containing InfoWindow contents
                return v;

            }
        });*/

        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    boolean doNotMoveCameraToCenterMarker = true;
                    public boolean onMarkerClick(Marker marker) {
                        //marker.showInfoWindow();
                        if((dialog != null) && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        if(mark_park.containsKey(marker)){
                            displayPopUp(marker);
                        }else{
                            marker.showInfoWindow();
                        }

                        return doNotMoveCameraToCenterMarker;
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.park_me:
               park_me1();
                //Toast.makeText(getActivity(), "Parked dummy", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;


        }
        return super.onOptionsItemSelected(item);
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
        searchPoint = new LatLng(lat, lon);
        String myurl = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat="
                +lat
                +"&long="
                +lon
                +"&radius=0.25&uom=mile&response=json";

        GetParking gp = new GetParking(myContext, this);
        gp.execute(myurl);

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
                //showListDialogue();

                putMarkers(parkingList);
                //TODO check this code

            }else{
                Toast.makeText(myContext, "SORRY !! NO PARKING DATA !", Toast.LENGTH_LONG).show();
            }

        }
    }


    public void putMarkers(ArrayList<Parking> parking){
        mMap.clear();
        // add search point
        Bitmap sbMap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.red_pin);
        Bitmap sadjustedImage = Bitmap.createScaledBitmap(sbMap, 100, 100, true);
        Bitmap snewImage = adjustImage(sadjustedImage);
        BitmapDescriptor sicon = BitmapDescriptorFactory.fromBitmap(snewImage);
        mMap.addMarker(new MarkerOptions().position(searchPoint).title("Search Point").icon(sicon));

        //add parking markers.
        Bitmap bMap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.park_avail);
        Bitmap adjustedImage = Bitmap.createScaledBitmap(bMap, 100, 100, true);
        Bitmap newImage = adjustImage(adjustedImage);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(newImage);
        if(parkingList.size()>0){
            for(int i=0; i< parkingList.size(); i++){
                Parking thisParking = parkingList.get(i);
                final LatLng ll = new LatLng(thisParking.getLongitude(),thisParking.getLatitude());
                //final LatLng ll = new LatLng(37.76425207, -122.4207729);
                if (mMap !=null){
                    //mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).title("Parked"));
                     Marker mark = mMap.addMarker(new MarkerOptions().position(ll).title(thisParking.getAddress()).icon(icon));
                     mark_park.put(mark,thisParking );
                 }

            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchPoint, 16));
        }
    }

    protected void displayPopUp(Marker marker){
        mainPin.setVisibility(View.INVISIBLE);
        marker.setTitle("This one");
        dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.detailedview);
        dialog.setTitle(null);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mainPin.setVisibility(View.VISIBLE);
            }
        });
        // Setting dialogview
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();

        params.gravity= Gravity.BOTTOM;
        window.setAttributes(params);
        // Getting the parking from the marker
        final Parking  parking = mark_park.get(marker);

        // components
        // Getting reference to the TextView to set latitude
        TextView street = (TextView) dialog.findViewById(R.id.str_add);
        TextView time = (TextView) dialog.findViewById(R.id.time_tv);
        Button addFav = (Button)dialog.findViewById(R.id.fav_btn);
        Button addNav = (Button)dialog.findViewById(R.id.nav_btn);
        ImageView cross = (ImageView)dialog.findViewById(R.id.cross_btn);
        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addParkingtoDB(parking);
                Toast.makeText(myContext, "added to db", Toast.LENGTH_LONG).show();

                dialog.dismiss();
            }
        });
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "This works", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
        // Setting the Text
        street.setText(parking.getAddress());
        time.setText(parking.getTimesAsString());

        dialog.show();

    }



    protected Bitmap adjustImage(Bitmap image) {
        int dpi = image.getDensity();
        if (dpi == mDpi)
            return image;
        else {
            int width = (image.getWidth() * mDpi + dpi / 2) / dpi;
            int height = (image.getHeight() * mDpi + dpi / 2) / dpi;
            Bitmap adjustedImage = Bitmap.createScaledBitmap(image, width, height, true);
            adjustedImage.setDensity(mDpi);
            return adjustedImage;
        }
    }


    public void park_me(){
        Location myLocation = getLastKnownLocation();
        if(myLocation!= null){
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 4);
            builder.setMessage(" Wanna Park ? ")
                    .setTitle("Found a spot? GREAT !! ")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(Global_var.getState()==0){

                            }
                            int x =  Global_var.getState();
                            String parkingState = "parked stats = " +  x;
                            Toast.makeText(myContext,parkingState, Toast.LENGTH_SHORT ).show();
                            Global_var.setState(1);
                            x = Global_var.getState();
                            parkingState = "parked stats changed= " + Global_var.getState() ;
                            Toast.makeText(myContext,parkingState, Toast.LENGTH_SHORT ).show();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            Window win = dialog.getWindow();
            win.setGravity(Gravity.BOTTOM);
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }
    }


    public void park_me1() {
        Location myLocation = getLastKnownLocation();
        if (myLocation != null) {
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
            dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.park_me_dialog);
            dialog.setTitle(null);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //do something on dismiss
                }
            });
            // Setting dialogview
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams params = window.getAttributes();

            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);


            // components
            // Getting reference to the TextView to set latitude
            TextView tv1 = (TextView) dialog.findViewById(R.id.park_me_head);
            TextView tv2 = (TextView) dialog.findViewById(R.id.park_me);
            Button cancel = (Button) dialog.findViewById(R.id.cancel);
            Button yes_btn = (Button) dialog.findViewById(R.id.park_me_yes);
            ImageView cross = (ImageView) dialog.findViewById(R.id.cross_btn_1);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add parking markers.
                    Bitmap bMap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.flag);
                    Bitmap adjustedImage = Bitmap.createScaledBitmap(bMap, 100, 100, true);
                    Bitmap newImage = adjustImage(adjustedImage);
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(newImage);

                    Marker mark = mMap.addMarker(new MarkerOptions().position(myCurrentLocation).title("Parked").icon(icon));
                    startParkingTimer();
                    Toast.makeText(getActivity(), "This works", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            cross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();

                }
            });
            // Setting the Text
            //street.setText(parking.getAddress());
            //time.setText(parking.getTimesAsString());

            dialog.show();


        }else{
            Toast.makeText(getActivity(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }

    }


    public void  startParkingTimer(){
        showTimeOptions();

    }

    public void showTimeOptions(){

    }
}
