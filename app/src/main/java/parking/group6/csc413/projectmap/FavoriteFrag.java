package parking.group6.csc413.projectmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Swati on 5/5/2015.
 */
public class FavoriteFrag extends Fragment {

    public FavoriteFrag(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.favorite, container, false);
        return rootView;
    }
}