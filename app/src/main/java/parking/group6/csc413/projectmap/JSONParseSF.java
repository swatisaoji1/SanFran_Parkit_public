package parking.group6.csc413.projectmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class will parse the JSON string to parking object
 */
public class JSONParseSF {
    JSONObject json;

    public static Parking[] parseJsonFromSF(JSONObject jobj) throws JSONException {
        JSONArray allParking = new JSONArray();
        int no_parking = Integer.parseInt(jobj.getString("NUM_RECORDS"));
        if (no_parking != 0){
            // get all available parking JSONobjects
            allParking = jobj.getJSONArray("AVL");
            Parking parkArray[] = new Parking[no_parking];
            for(int i = 0; i < allParking.length(); i++){
                JSONObject jo = allParking.getJSONObject(i);
                parkArray[i] = new Parking();
                parkArray[i].setAddress(jo.getString("NAME")); // get the address and set it
                parkArray[i].setLatLong(jo.getString("LOC")); // set lattitude and longitude
                // will get time options in future release
            }
            return parkArray;
        }

        return new Parking[0];
    }


}
