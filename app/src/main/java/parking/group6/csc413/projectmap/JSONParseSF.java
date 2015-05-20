package parking.group6.csc413.projectmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * This class will parse the JSON string to parking object
 * @author csc 413 group 6
 * @ version 1
 */
public class JSONParseSF {
    JSONObject json;

    /**
     * Parses the JSONObject and returns the parking array
     * @param jobj
     * @return Parking[]
     * @throws JSONException
     */
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
                parkArray[i].setLatLong(jo.getString("LOC"));  // set latitude and longitude
                if(jo.has("RATES"))
                    parkArray[i].setTimes(parseRates(jo)); // set times array
                else {
                    String[] noInfo = new String[1];
                    noInfo[0] = "No info";
                    parkArray[i].setTimes(noInfo);
                }

            }
            return parkArray;
        }

        return new Parking[0];
    }

    /**
     * Returns the rates of the parking from json object
     * @param jobj
     * @return String
     * @throws JSONException
     */
    private static String[] parseRates(JSONObject jobj) throws JSONException {
        JSONObject ratesObject = jobj.getJSONObject("RATES");
        Iterator keys = ratesObject.keys();
        JSONArray jsonArray = new JSONArray();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            jsonArray.put(ratesObject.get(key));
        }

        // Goes through our jsonArray to parse each element
        String[] parsedStrings = new String[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++)
            parsedStrings[i] = parseRate(jsonArray.getJSONObject(i));

        return parsedStrings;
    }

    /**
     * Returns the rate from the JSON object
     * @param rateObject
     * @return string
     * @throws JSONException
     */
    private static String parseRate(JSONObject rateObject) throws JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(rateObject.getString("BEG"));
        sb.append("-");
        sb.append(rateObject.getString("END"));
        sb.append(" Cost per hour:");
        sb.append(rateObject.getString("RATE"));
        return sb.toString();
    }
}
