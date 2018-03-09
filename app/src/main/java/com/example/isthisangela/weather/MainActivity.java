package com.example.isthisangela.weather;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    //api
    final String API_URL = "https://api.darksky.net/forecast/c3c9e7f71975c016b8aee5d0a6fd889d/";

    //location
    LocationManager locationManager;
    Location location;
    double latitude;
    double longitude;

    //frequency, distance of location updates: 1hr, 1km
    final int FREQUENCY = 3600000;
    final int DISTANCE = 1000;

    //formats for the month n day views
    final SimpleDateFormat monthDF = new SimpleDateFormat("MMM");
    final SimpleDateFormat dayDF = new SimpleDateFormat("dd");

    //today
    String city, summary, icon, rain;
    int tempNow, tempHigh, tempLow;

    //week
    ArrayList<String> months, dates, summaries, icons;
    ArrayList<Integer> tempHighs, tempLows;

    //layout on homepage
    TextView cityView, tempNowView, tempHighView, tempLowView, summaryView, rainView;
    ImageView iconView;

    //layout on weather page


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityView = findViewById(R.id.city);
        tempNowView = findViewById(R.id.tempNow);
        tempHighView = findViewById(R.id.tempHigh);
        tempLowView = findViewById(R.id.tempLow);
        summaryView = findViewById(R.id.textView6);
        rainView = findViewById(R.id.rain);
        iconView = findViewById(R.id.icon);

        getLocation();
        setInfo();
        setView();
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FREQUENCY, DISTANCE, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    private String getCity() {
        String city = "Nowhere";
        try {
            List<Address> addresses = new Geocoder(getBaseContext(), Locale.getDefault()).getFromLocation(location.getLatitude(),location.getLongitude(),1);
            city = addresses.get(0).getLocality();
        } catch (Exception e) {
            Log.e("getCity", "error");
            Toast.makeText(MainActivity.this, "Error retrieving current location", Toast.LENGTH_SHORT).show();
        }
        return city;
    }

    private void setInfo() {
        try {

            //get json
            URL url = new URL(API_URL + latitude + "," + longitude);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            urlConnection.disconnect();

            //make into json objects
            String jsonString = stringBuilder.toString();
            JSONObject weather = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONObject currently = weather.getJSONObject("currently");
            //week's json, plus filling in array of months & dates
            ArrayList<JSONObject> week = new ArrayList<JSONObject>();
            Calendar temp = Calendar.getInstance();
            for (int i = 0; i <= 7; i++) {
                week.add((JSONObject) weather.getJSONObject("daily").getJSONArray("data").get(i));
                temp.add(Calendar.DATE, 1);
                Date nextDate = temp.getTime();
                months.add(monthDF.format(nextDate));
                dates.add(dayDF.format(nextDate));
            }
            JSONObject today = week.get(0);

            //info rn
            city = getCity();
            icon = currently.getString("icon").replace("-", "_");
            summary = currently.getString("summary");
            tempNow = (int) currently.getDouble("temperature");
            tempHigh = (int) today.getDouble("temperatureHigh");
            tempLow = (int) today.getDouble("temperatureLow");
            rain = today.getString("summary");

            //info for week
            for (JSONObject day : week) {
                icons.add(day.getString("icon").replace("-", "_"));
                summaries.add(day.getString("summary"));
                tempHighs.add((int) day.getDouble("temperatureHigh"));
                tempLows.add((int) day.getDouble("temperatureLow"));
            }

        } catch (Exception e) {
            Log.e("setToday", "error");
            Toast.makeText(MainActivity.this, "Error getting today's weather :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void setView() {
        cityView.setText(city);
        tempNowView.setText(tempNow);
        tempHighView.setText(tempHigh);
        tempLowView.setText(tempLow);
        summaryView.setText(summary);
        rainView.setText(rain);
        iconView.setImageURI(Uri.fromFile(new File("/res/drawable/" + icon + ".png")));
    }

    @Override
    public void onLocationChanged(Location loc) {
        location = loc;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e("onProviderDisabled", "error");
        Toast.makeText(MainActivity.this, "Please enable GPS and internet", Toast.LENGTH_SHORT).show();
    }
}
