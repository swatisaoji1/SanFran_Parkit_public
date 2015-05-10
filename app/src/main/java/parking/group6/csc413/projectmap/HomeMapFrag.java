package parking.group6.csc413.projectmap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    //controls after parkes
    LatLng parked;
    View controlv;
    TextView parker_info;
    Button take_to_car_btn;
    Button unPark;



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

        setUpMapIfNeeded();
        controlv = rootView.findViewById(R.id.controls_parked);
        parker_info = (TextView)rootView.findViewById(R.id.parked_info);
        take_to_car_btn= (Button)rootView.findViewById(R.id.walk_to_Car);
        unPark = (Button)rootView.findViewById(R.id.un_park);

        myContext = getActivity();
        mDpi = getActivity().getResources().getDisplayMetrics().densityDpi;

        //SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long time;
        String add ="";
        if(pref.contains("parked")){
            if(pref.getInt("parked", 0)==1){
                controlv.setVisibility(View.VISIBLE);
                parked = getParkingSharedPref();
                if(parked != null){
                    add = "You are parked at :\n"
                            + getCompleteAddressString(parked.latitude, parked.longitude);
                    if(pref.contains("time")){
                        time = pref.getLong("time", 0);
                        Date date = new Date(time);
                        String result = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                        add = add + "\nSince: " + result;
                    }
                    parker_info.setText(add);
                    Marker mark = mMap.addMarker(new MarkerOptions().position(parked).title("You are parked here"));
                    mark.showInfoWindow();
                    mainPin.setVisibility(View.INVISIBLE);
                }

            }
        }
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
                popup.show();

            }
        });

        take_to_car_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng parkLocation;
                parkLocation = getParkingSharedPref();
                if(parkLocation != null){
                    navigateTo(parkLocation);
                }
            }
        });

        unPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeParkingInfoSP();
                Toast.makeText(myContext, "Parking Removed !!", Toast.LENGTH_LONG).show();
                mMap.clear();
                if(controlv.getVisibility() == View.VISIBLE){
                    controlv.setVisibility(View.INVISIBLE);
                }
                mainPin.setVisibility(View.VISIBLE);


            }
        });

        return rootView;

    }

    @Override
    public void onResume() {
       //setUpMapIfNeeded();
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        if (mMap != null) {
            mMap = null;
            try{
                Fragment f = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                if(f != null){
                    getChildFragmentManager().beginTransaction()
                            .remove(f).commit();
                }
            }catch(Exception e){
            }
        }
        super.onDestroyView();
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

        mMap.clear();
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.getUiSettings(). setZoomControlsEnabled(true);
        mMap.getUiSettings(). setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);


        Location myLocation = getLastKnownLocation();
        if(myLocation!= null){
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
        }else{
            Toast.makeText(getActivity(), "cant get loc, GPS may be OFF !!", Toast.LENGTH_LONG).show();
        }


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

  /*  public void showListDialogue(){
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

    }*/

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
        dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar){
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                this.dismiss();
                return true;
            }
        };
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
        final String address = parking.getAddress();
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

            }
        });

        addNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uri ="google.navigation:q=" + address;
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                myContext.startActivity(i);

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


   public void park_me1() {
        Location myLocation = getLastKnownLocation();
        if (myLocation != null) {
            myCurrentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 13));
            dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar){
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    this.dismiss();
                    return true;
                }
            };
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
            window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
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
            yes_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add parking markers.
                    Date date = new Date(System.currentTimeMillis());

                    String result = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                    String add = "You are parked at :\n"
                            + getCompleteAddressString(myCurrentLocation.latitude, myCurrentLocation.longitude)
                            + "\nSince: "
                            + result;

                    Marker mark = mMap.addMarker(new MarkerOptions().position(myCurrentLocation).title("You are parked here"));
                    mark.showInfoWindow();

                    //add to shared pref
                    storeParkingSharedPref(myCurrentLocation.latitude, myCurrentLocation.longitude,System.currentTimeMillis());
                    controlv.setVisibility(View.VISIBLE);
                    mainPin.setVisibility(View.INVISIBLE);
                    parker_info.setText(add);
                    mMap.setPadding(0,0,0,300);
                    startParkingTimer();
                    //Toast.makeText(getActivity(), "This works", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
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

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(myContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return strAdd;
    }

    public void removeParkingInfoSP(){
        //SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("latitude");
        editor.remove("longitude");
        editor.putInt("parked", 0);
        editor.commit();

    }
    public void storeParkingSharedPref(double lat, double lon, long mili){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        long d1 = Double.doubleToRawLongBits(lat);
        long d2 = Double.doubleToRawLongBits(lon);
        editor.putLong("latitude", Double.doubleToRawLongBits(lat));
        editor.putLong("longitude", Double.doubleToRawLongBits(lon));
        editor.putLong("time", mili);
        editor.putInt("parked",1 );
        editor.commit();

    }

    public LatLng getParkingSharedPref(){
        LatLng current = null;
        double myLat=-1;
        double myLon=-1;
        //SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ( pref.contains("latitude")){
            long l1 = pref.getLong("latitude", 0);
            myLat = Double.longBitsToDouble(l1);
        }
        if ( pref.contains("longitude")){
            long l = pref.getLong("longitude", 0);
            myLon = Double.longBitsToDouble(l);
        }
        if((myLat != -1)&& (myLon !=-1)){
            current = new LatLng(myLat, myLon);
        }

       return current;
    }

    public void navigateTo(LatLng lat_lon){
        String add = getCompleteAddressString(lat_lon.latitude, lat_lon.longitude);
        String uri ="google.navigation:q=" + add;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        myContext.startActivity(i);
    }
}
