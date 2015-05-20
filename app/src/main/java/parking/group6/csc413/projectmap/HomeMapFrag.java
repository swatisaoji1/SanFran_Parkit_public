package parking.group6.csc413.projectmap;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
/**
 * HomeMap Fragment Hosts the main Google Map implements the getDataFromAsync interface
 * @author  csc 413 Group 6
 * @version 1
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
    TextView  timeText;
    Button take_to_car_btn;
    Button unPark;
    Button timer;

    // alarm
    public static String TAG = "HOME_FRAG";
    public static String ALARM = "ALARM";
    Intent intentAlarm;
    AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    Intent service_intent;


    /**
     * Default Constructor
     */
    public HomeMapFrag(){
    }


    /**
     * Overriden method calls super.Oncreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    /**
     * Inflates the layout , which in this case is a google map
     * Gets the Parking info from the Preferencs and sets the view visibility accordingly
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
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
        timer = (Button)rootView.findViewById(R.id.timer_btn);
        timeText=(TextView)rootView.findViewById(R.id.time_tick);
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
                //getActivity().stopService(service_intent);


            }
        });
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timeText.setVisibility(View.VISIBLE);
                        timeText.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timeText.setText("done!");
                        Calendar cal1 = Calendar.getInstance(); // Now
                        cal1.add(Calendar.SECOND, 5); // Change to five seconds from now
                        alarmMgr = (AlarmManager)myContext.getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(myContext, AlarmReceiver.class);
                        alarmIntent = PendingIntent.getBroadcast(myContext, 0, intent, 0);
                        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                                SystemClock.elapsedRealtime(),
                                alarmIntent);
                    }
                }.start();

            }
        });

        return rootView;

    }

    /**
     * Overriden Method OnResume
     * Re-registers the Broadcast receiver on activity resume
     *
     */
    @Override
    public void onResume() {
        getActivity().registerReceiver(receiver, new IntentFilter());
        super.onResume();
    }

    /**
     * unregisters the Broadcast receivr onStop
     */
    @Override
    public void onStop() {
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
        }
        super.onStop();
    }

    /**
     * Removes the fragment on view Destroy
     */
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

    /**
     * Checks if Map is null, calls setUpMap if needed
     */
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            FragmentManager myFM = getChildFragmentManager();
            mapFragment = (SupportMapFragment)myFM.findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * If the map is null this method is called to set up.
     * Enables the map control , zooms to current location.
     * Sets on click listeners.
     */
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

    /**
     * Inflates the Option Menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

    }

    /**
     * Set listener on Option selected
     * @param item
     * @return
     */
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

    /**
     * Uses system location services to get the current location fo the user.
     * @return Location
     */
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

    /**
     * Calls the async Task (GetParking) to connect to the SFPark and get the parking information.
     * @param lat
     * @param lon
     */
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


    /**
     * Connects to the Databse and adds parking to the favorites table
     * @param parking
     */
    public void addParkingtoDB(Parking parking){
        db = new ConnectDB(myContext);
        db.addParking(parking);
    }


    /**
     * Returns the last ( most recent )favorite drom the databse
     * @return String
     */
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

    /**
     * Overriden method
     * Unregisters the broadcast receiver
     */
    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    /**
     * called from the AsyncTask and returns the JsonObject
     * @param jobj
     */
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

                putMarkers(parkingList);
                //TODO check this code

            }else{
                Toast.makeText(myContext, "SORRY !! NO PARKING DATA !", Toast.LENGTH_LONG).show();
            }

        }
    }


    /**
     * Takes the ArrayList of Parking objects and displays the markets on the map.
     * @param parking
     */
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

    /**
     * Shows the custom dialogue box , with info about the selected parking location.
     * On click listeners to add the parking to favorite
     * Or to navigate to the Parking
     * @param marker
     */
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


    /**
     * Helper method that adjusts the size of the bitmap image based on screen size.
     * @param image
     * @return
     */
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

    /**
     * Parks the user to the current location, Adds the user info to the shared preference.
     * Displays the additional controls ( when user is parked)
     */
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
                    //startParkingTimer();
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


    /**
     * Uses the Geocoder to get complete postal address from latitude and longitude.
     * @param LATITUDE
     * @param LONGITUDE
     * @return String address
     */
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    /**
     * Removes the parking info from the Preferences.
     */
    public void removeParkingInfoSP(){
        //SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("latitude");
        editor.remove("longitude");
        editor.putInt("parked", 0);
        editor.commit();

    }

    /**
     * Stores the Parking info in Shared Preferences
     * @param lat
     * @param lon
     * @param mili
     */
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

    /**
     * Gets the Parking info from the Shared Preferences
     * @return LatLng
     */
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

    /**
     * Starts the ACTION_VIEW intent by calling the google navigation
     * @param lat_lon
     */
    public void navigateTo(LatLng lat_lon){
        String add = getCompleteAddressString(lat_lon.latitude, lat_lon.longitude);
        String uri ="google.navigation:q=" + add;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        myContext.startActivity(i);
    }

    /**
     * Broadcast receiver to receive the message when coundowntimer is completed.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString("mess");
                Toast.makeText(getActivity(), "fromTime:"+ message, Toast.LENGTH_LONG).show();
                timeText.setText("YOUR PARKING TIME IS UP");

            }
        }
    };
}
