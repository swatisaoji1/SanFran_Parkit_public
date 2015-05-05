package parking.group6.csc413.projectmap;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

public class MapsActivity extends ActionBarActivity implements getDataFromAsync{

    public static FragmentManager fragmentManager;
    Fragment fragment = null;
    //--ANSHUL
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    //--END_ANSHUL



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fragmentManager = getSupportFragmentManager();

        //--ANSHUL
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        //--END_ANSHUL
        /*

        */

        //--ANSHUL
        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //--END_ANSHUL

        if (savedInstanceState == null) {
            selectItem(1);
        }

    }// end oncreate

    @Override
    protected void onResume() {
        super.onResume();

       // setUpMapIfNeeded();
    }

    //--ANSHUL
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    //--END_ANSHUL








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.cancel_action:
                //String check = checkParkingDB();
                //showMsg(check);
        }

        //--ANSHUL
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //--END_ANSHUL
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(JSONObject jobj) {


    }




    //--ANSHUL
    private void addDrawerItems() {
        String[] parkArray = { "Favourites", "Broadcast", "Settings", "Contact Us", "Rate the app" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, parkArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
                //Toast.makeText(MapsActivity.this, "Yet to be implemented!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }
    //--END_ANSHUL


    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position


        // Insert the fragment by replacing any existing fragment
        switch (position) {
            case 0:
                // uncomment the following and work on FavoriteFrag class
                //fragment = new FavoriteFrag();
                Toast.makeText(MapsActivity.this, "Yet to be implemented!", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                if (!(fragment instanceof HomeMapFrag)){
                    fragment = new HomeMapFrag();
                }


            default:



                break;
        }

        // replace the fragment
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            //setTitle(mPlanetTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }else{
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

}
