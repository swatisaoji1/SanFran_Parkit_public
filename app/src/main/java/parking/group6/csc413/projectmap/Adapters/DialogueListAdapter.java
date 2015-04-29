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
 * Created by Swati on 4/28/2015.
 */
public class DialogueListAdapter extends BaseAdapter {

    private ArrayList<Parking> ParkingListData;
    private LayoutInflater layoutInflater;

    public DialogueListAdapter(Context context, ArrayList<Parking> listData) {
        this.ParkingListData = listData;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return ParkingListData.size();
    }

    @Override
    public Parking getItem(int position) {
        return ParkingListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
        holder.timeView.setText(ParkingListData.get(position).getTimes());

        return convertView;
    }

    static class ViewHolder {
        TextView addressView;
        TextView timeView;
    }
}
