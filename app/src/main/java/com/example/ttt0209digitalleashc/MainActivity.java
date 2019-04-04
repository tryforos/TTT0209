package com.example.ttt0209digitalleashc;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private EditText editParentUsername;
    private EditText editChildUsername;
    private TextView textError;
    private String s;

    ////////
    //HOLLER GPS
    //
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location locLocation;
    //
    //
    ////////

    ////////
    //HOLLER DATA
    //
    private final String SHARED_KEY = "TTT0209";
    private SharedPreferences sharedPreferences;
    //
    //
    ////////


    ////////
    ////////
    ////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editParentUsername  = findViewById(R.id.editParentUsername);
        editChildUsername  = findViewById(R.id.editChildUsername);
        textError  = findViewById(R.id.textError);

        ////////
        //HOLLER DATA
        //
        //set in onCreate
        sharedPreferences = getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE);
        //
        //
        ////////

        ////////
        //HOLLER GPS
        //
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //set accessible variable to new location
                locLocation = location;

                //STOP LOCATIONMANAGER FROM RECORDING, I.E. STOPS EVENTS
                //locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Check if location permission exists.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                // If permission does not exists, request for it.
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        else {

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
        //
        //
        ////////


    }


    //when button is clicked
    public void buttonClickedReportLocation(View view) {

        writeJSON(view);

        //String strURL = "https://turntotech.firebaseio.com/digitalleash/users/" + editParentUsername.getText() + "/" + editChildUsername.getText() + ".json";
        //Toast.makeText(MainActivity.this, strURL, Toast.LENGTH_LONG).show();
        //Toast.makeText(MainActivity.this, "Lat: " + locLocation.getLatitude() + " || Lon: " + locLocation.getLongitude(), Toast.LENGTH_LONG).show();

    }


    public void writeJSON(View view){

        JSONObject jsonObject = new JSONObject();

        try{
            //needs timestamp, else gets rejected
            jsonObject.put("latitude",locLocation.getLatitude());
            jsonObject.put("longitude",locLocation.getLongitude());
            jsonObject.put("timestamp", new Date().getTime());

            String strURL = "https://turntotech.firebaseio.com/digitalleash/users/" + editParentUsername.getText() + "/" + editChildUsername.getText() + ".json";

            new HttpPostAsyncTask(jsonObject).execute(strURL);

            //Toast.makeText(MainActivity.this, "Location successfully sent!", Toast.LENGTH_LONG).show();
        }
        catch (JSONException e){
            Log.e("JSON_ERROR",e.getMessage());

            Toast.makeText(MainActivity.this, "ERROR: Location not sent successfully!!! :(", Toast.LENGTH_LONG).show();
        }

        Log.d("JSON_DATA",jsonObject.toString());

    }



    private Integer uploadData(String urlString, JSONObject postData) {

        final String TAG = "hollerrr:";

        try {
            // This is getting the url from the string we passed in
            URL url = new URL(urlString);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("PUT");

            // Send the post body
            if (postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }

            int intStatusCode = urlConnection.getResponseCode();

            if (intStatusCode ==  200) {
                // Status code 200 = everything fine

                ////////
                //record output
                //
                InputStream is = urlConnection.getInputStream(); //input stream
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead, totalBytesRead = 0;

                byte[] data = new byte[2048];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                   buffer.write(data, 0, nRead);
                   totalBytesRead += nRead;
                }

                byte[] bytes = buffer.toByteArray();
                //
                //
                ////////

                Log.d(TAG, new String(bytes));


            } else {
                // Status code is not 200

                ////////
                //record output
                //
                InputStream is = urlConnection.getErrorStream(); //error stream
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead, totalBytesRead = 0;

                byte[] data = new byte[2048];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                   buffer.write(data, 0, nRead);
                   totalBytesRead += nRead;
                }

                byte[] bytes = buffer.toByteArray();
                //
                //
                ////////

                Log.d(TAG, new String(bytes));
            }

            return intStatusCode;

        }
        catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return -1; // error
        }
    }

    private class HttpPostAsyncTask extends AsyncTask<String, Void, Integer> {

        // This is the JSON body of the post
        JSONObject jsonObjPostData;


        // This is a constructor that allows you to pass in the JSON body
        public HttpPostAsyncTask(JSONObject jsonObjPostData) {
            if (jsonObjPostData != null) {
                this.jsonObjPostData = jsonObjPostData;
            }
        }

        @Override
        protected Integer doInBackground(String... params) {

            int result = uploadData(params[0], this.jsonObjPostData);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);

            if(result != null && result == 200) {
                // code 200 = everything worked
                textError.setText(null);
                Toast.makeText(MainActivity.this, "Saved! Whoop!", Toast.LENGTH_LONG).show();
            }
            else {
                textError.setText("Error in saving!!! :(\nResponse Code: " + result);
                //Toast.makeText(MainActivity.this, "Error in saving!!! :(", Toast.LENGTH_LONG).show();
            }
        }
    }

    ////////
    //HOLLER GPS
    //
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Location Permission Not Granted, Punk!", Toast.LENGTH_LONG).show();
                }
                else {
                    //Toast.makeText(MainActivity.this, "Blah Blah Toast", Toast.LENGTH_LONG).show(); //holler
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                }
        }
    }
    //
    //
    ////////


}
