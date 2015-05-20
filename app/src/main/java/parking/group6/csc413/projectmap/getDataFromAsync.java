package parking.group6.csc413.projectmap;


import org.json.JSONObject;

/**
 * Interface for any class which needs to communicate with an Async Task
 * Includes a single method onTaskCompleted(Object obj)
 * @author csc 413 group 6
 * @version 1
 */
public interface getDataFromAsync {
    void onTaskCompleted(JSONObject jobj);
}
