package parking.group6.csc413.projectmap;


import org.json.JSONObject;

/**
 * Created by Swati on 4/28/2015.
 */
public interface getDataFromAsync {
    void onTaskCompleted(JSONObject jobj);
    void onTimeup();
}
