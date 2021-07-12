package com.oicen.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String newdate;
    EditText searchField;
    Button getWeather;
    TextView date, weather, temperature, wind, humidity, visibility, city, weather_desc;
    String api = "c74205ed5059d2ce6ee4290f431c3429";
    private GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField = findViewById(R.id.searchField);
        getWeather = findViewById(R.id.getWeather);
        date = findViewById(R.id.date);
        weather = findViewById(R.id.weather);
        temperature = findViewById(R.id.temperature);
        wind = findViewById(R.id.wind);
        humidity = findViewById(R.id.humidity);
        visibility = findViewById(R.id.visibility);
        city = findViewById(R.id.city);
        weather_desc = findViewById(R.id.weather_desc);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEE, MMM d");
        newdate = dateFormat.format(calendar.getTime());
        date.setText(newdate);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            getCity(latitude, longitude);
        }else{
            gpsTracker.showSettingsAlert();
        }


        getWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentWeather(searchField.getText().toString());
            }
        });
    }

    private void getCity(double d2, double d3) {
        List list;
        try {
            list = new Geocoder(getApplicationContext(),
                    Locale.getDefault()).getFromLocation(d2, d3, 1);
            if (list != null && (!list.isEmpty())) {
                String locality = ((Address) list.get(0)).getLocality();
                getCurrentWeather(locality);

            }
        } catch (NullPointerException e2) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void getCurrentWeather(String locality){
        String url = "http://api.openweathermap.org/data/2.5/weather?q=city&appid=apikey";
        url = url.replaceAll("apikey", api);
        url = url.replaceAll("city", locality);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray resWeather = response.getJSONArray("weather");
                            JSONObject resTemp = response.getJSONObject("main");
                            JSONObject sys = response.getJSONObject("sys");
                            JSONObject resWind = response.getJSONObject("wind");
                            JSONObject weatherRes = resWeather.getJSONObject(0);

                            String currWeather = weatherRes.getString("main");
                            String desc = weatherRes.getString("description");
                            double currTemp = resTemp.getDouble("temp");
                            currTemp -= 273.15;
                            String currHumid = resTemp.getString("humidity");
                            String currWind = resWind.getString("speed");
                            String currVis = response.getString("visibility");
                            String currCity = response.getString("name");
                            String currCountry = sys.getString("country");

                            weather.setText(currWeather);
                            temperature.setText(String.format("%.2f", currTemp) + " °C");
                            humidity.setText(currHumid);
                            wind.setText(currWind);
                            visibility.setText(currVis);
                            city.setText(currCountry + "/" +currCity);
                            weather_desc.setText(desc);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }
}