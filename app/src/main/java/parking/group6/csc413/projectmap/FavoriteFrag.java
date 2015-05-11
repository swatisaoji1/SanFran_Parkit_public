package parking.group6.csc413.projectmap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import parking.group6.csc413.projectmap.Adapters.DialogueListAdapter;

/**
 * Created by Swati on 5/5/2015.
 */
public class FavoriteFrag extends Fragment {
    ConnectDB db;
    ArrayList<Parking> parking;
    public FavoriteFrag(){}
    DialogueListAdapter myAdapter;
    View rootView;
    ListView lv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.favorite, container, false);
        lv = (ListView)rootView.findViewById(R.id.favorite_list);

       /* String[] testAr = { "Favourites", "Broadcast", "Settings", "Contact Us", "Rate the app" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                testAr

        );*/


        //get the parking list
        db = new ConnectDB(getActivity());
        parking = db.getParkingList();

        myAdapter = new DialogueListAdapter(getActivity(), parking);

        lv.setAdapter(myAdapter);

        registerForContextMenu(lv);

        return rootView;
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle("Favorite");
        menu.add(Menu.NONE, 0, 0, "Navigate to");
        menu.add(Menu.NONE, 1, 1, "Remove from favorites");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        // If user selects "Navigate to"
        if (item.getItemId() == 0) {
            double latitude = parking.get(info.position).getLatitude();
            double longitude = parking.get(info.position).getLongitude();
            String coordinates = latitude + "," + longitude;
            String uri ="google.navigation:q=" + coordinates;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            lv.getContext().startActivity(i);
        // Else if user selects "Remove from favorites"
        } else if (item.getItemId() == 1) {
            db.deleteParking(parking.get(info.position));
            parking.remove(info.position);
            myAdapter.notifyDataSetChanged();
        }

        return true;
    }

}