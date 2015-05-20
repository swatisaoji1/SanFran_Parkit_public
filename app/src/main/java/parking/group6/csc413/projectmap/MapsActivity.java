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

import parking.group6.csc413.projectmap.Adapters.AppRating;


/**
 * Mainactivity class that hols the navigation drawer and
 * swaps Fragments including the map fragment and the favorite fragment based on the item selected in the navigation drawer.
 * @author csc 413 group 6
 * @version 1
 */
public class MapsActivity extends ActionBarActivity implements getDataFromAsync{

    public static FragmentManager fragmentManager;
    Fragment fragment = null;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;


    /**
     * Overridden Oncreate method of the activity, inflates the layout and
     * sets the view and sets up the navigation drawer
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fragmentManager = getSupportFragmentManager();


        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        if (savedInstanceState == null) {
            selectItem(0);
        }

    }// end oncreate

    /**
     * calls super OnResume
     */
    @Override
    protected void onResume() {
        super.onResume();

    }


    /**
     * calls the super.onPostCreate
     * Syncs the state of the navigation drawer
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Overriden method, Toggles the navigation drawer on change of configuration.
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Inflates the option menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * Sets listener on option selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.cancel_action:

        }
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(JSONObject jobj) {

    }


    /**
     * Adds the options to the navigation drawer.
     */
    private void addDrawerItems() {
        String[] parkArray = { "Map", "Favorites", "Park Me", "Contact Us", "Rate the app" };
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

    /**
     * Sets up the navigation drawer.
     */
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

    /**
     * Swaps fragments in the main content view
     * @param position and integer indicating position selected
     */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        // Insert the fragment by replacing any existing fragment
        switch (position) {
            case 0:
                if (!(fragment instanceof HomeMapFrag)){
                    fragment = new HomeMapFrag();
                }
                break;
            case 1:
                fragment = new FavoriteFrag();
                break;
            case 2:
                // park me button

                break;
            case 3:

                Toast.makeText(MapsActivity.this, "Yet to be implemented!", Toast.LENGTH_SHORT).show();
                break;
            default:

                AppRating.app_launched(this);
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
