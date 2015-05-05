package parking.group6.csc413.projectmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.favorite, container, false);
       ListView lv = (ListView)rootView.findViewById(R.id.favorite_list);

       /* String[] testAr = { "Favourites", "Broadcast", "Settings", "Contact Us", "Rate the app" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                testAr

        );*/


        //get the parking list
       db = new ConnectDB(getActivity());
       parking = db.getParkingList();

       DialogueListAdapter myAdapter = new DialogueListAdapter(getActivity(), parking);

       lv.setAdapter(myAdapter);



        return rootView;
    }
}