package parking.group6.csc413.projectmap.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import parking.group6.csc413.projectmap.Parking;
import parking.group6.csc413.projectmap.R;

/**
 * This class is a custom adaptor class , * that inflates the parking List to the item views.
 * It therefor creates a customized ListView for favorites
 * @author  Csc 413 Group 6
 * @version 1
 */
public class DialogueListAdapter extends BaseAdapter {

    private ArrayList<Parking> ParkingListData;
    private LayoutInflater layoutInflater;

    /**
     * Constructor Initializes the ParkigListData and gets the context of the layout inflator
     * @param context
     * @param listData
     */
    public DialogueListAdapter(Context context, ArrayList<Parking> listData) {
        this.ParkingListData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * @return int count of the ParkingList
     */
    @Override
    public int getCount() {
        return ParkingListData.size();
    }

    /**
     * Returns the parking object when a list is clicked.
     * @param position
     * @return Parking Object at given position
     */
    @Override
    public Parking getItem(int position) {
        return ParkingListData.get(position);
    }

    /**
     * Returns the id of the item at the given position
     * @param position
     * @return long id of the view
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Inflates the view and populates them with data. Recycles the view if it already exists
     * @param position
     * @param convertView
     * @param parent
     * @return view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list, null);
            holder = new ViewHolder();
            holder.addressView = (TextView) convertView.findViewById(R.id.parking_address);
            holder.timeView = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.addressView.setText(ParkingListData.get(position).getAddress());
        String s = ParkingListData.get(position).getTimesAsString();
        holder.timeView.setText(s);

        return convertView;
    }

    /**
     * static class that encapsulates the viewHolder
     */
    static class ViewHolder {
        TextView addressView;
        TextView timeView;
    }
}
