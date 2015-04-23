package parking.group6.csc413.projectmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Swati on 4/17/2015.
 */
public class GetParking extends AsyncTask<String, String, String> {

    private Context context;
    private Handler handler;
    private ProgressDialog dialog;
    String data="";

    JSONObject result = null;
    String message = "No Message";
    String json = null;

    //constructor
    public GetParking(Context context,Handler handler,String data){
        this.context = context;
        this.handler = handler;
        this.data=data;
        dialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Getting Parking Location...");
        this.dialog.show();
     }

    @Override
    protected String doInBackground(String... params) {
        result = getJSONFromUrl(params[0]);
        try{
            if(result != null){
                message = result.getString("MESSAGE");
            }
        }catch(Exception e){
            Log.e("Error", "Do in Background ");
        }

        return message;
    }

    @Override
    protected void onPostExecute(String message) {
        super.onPostExecute(message);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        // bundle the result and send through handler
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("data", this.message);
        msg.setData(b);
        handler.sendMessage(msg);

    //MapsActivity mp = new MapsActivity();
    //mp.showMsg(message);

    }

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

