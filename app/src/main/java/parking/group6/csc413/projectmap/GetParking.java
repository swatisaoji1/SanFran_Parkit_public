package parking.group6.csc413.projectmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class extends AsyncTask and establishes a connection with SFPark to get the parking information.
 * @author csc 413 group 6
 * @version 1
 */
public class GetParking extends AsyncTask<String, String, JSONObject> {

    private Context context;
    getDataFromAsync dataListener;
    private ProgressDialog dialog;

    JSONObject result = null;
    String message = "No Message";
    String json = null;


    /**
     * Constructor
     * @param context
     * @param dataList
     */
    public GetParking(Context context,getDataFromAsync dataList){
        this.context = context;
        this.dataListener = dataList;
        dialog = new ProgressDialog(context);

    }

    /**
     * This method is executed before the doInBackgroud .
     * It starts a dialogue on the UI thread, while the data is being fetched by doInBackground
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Getting Parking Location...");
        this.dialog.show();
     }

    /**
     * Connects to SFPark by calling method getJSONFromUrl
     * @param params received from the calling activity
     * @return result which is then passed to onPostExecute
     */
    @Override
    protected JSONObject doInBackground(String... params) {
        result = getJSONFromUrl(params[0]);
        try{
            if(result != null){
                message = result.getString("MESSAGE");
            }
        }catch(Exception e){
            Log.e("Error", "Do in Background ");
        }
        return result;
    }

    /**
     * This method receives the result from doInBackground .
     * calls onTaskCompleted method of the interface dataListener ( which is implemented by calling class)
     * Dismissed the dialogue indicating receipt of the result
     * @param result
     */
    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        dataListener.onTaskCompleted(result);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    /**
     * Establishes connection with SFstate, receives the string responce ,
     * which is converted to JSONonject and returned
     * @param  myurl  an string query URL set to the sfpark
     * @return JSONObject
     * @see JSONObject
     */
    public JSONObject getJSONFromUrl(String myurl){
        BufferedReader reader = null;
        JSONObject jObj = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            //is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON object
        return jObj;
    }

    public String getMessage(){
        return message;
    }
}

