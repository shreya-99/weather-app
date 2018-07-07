package com.londonappbrewery.climapm;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.mime.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE= 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e72ksfhjdad9dwejdwsjls13";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER; //requests location from gps devices not from cell towers


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager; // start or stop requsting location update
    LocationListener mLocationListener;//that will notify if the location has actually changed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, changeCityController.class);
                startActivity(myIntent);

            }
        });

    }


    // TODO: Add onResume() here:
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();/*it is the android lifecycle method .
         gets executed just after the onCreate() and just before the user interacts with the activity.
         */
        Log.d("Clima", "onResume() called.");

        Intent myIntent =  getIntent();
        String city = myIntent.getStringExtra("City");

        if(city!=null){
              getWeatherForNewCity(city);
        }

        else {
            Log.d("Clima", "Getting request for weather data.");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city){

        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);

    }


    // TODO: Add getWeatherForCurrentLocation() here:

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getWeatherForCurrentLocation(){
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("Clima", "onLocationChanged() call back received");
               String longitude =  String.valueOf(location.getLongitude());
               String latitude = String.valueOf(location.getLatitude());

               Log.d("Clima", "Longitutde is: "+longitude);
               Log.d("clima", "Latitude is: "+latitude);

               RequestParams Params = new RequestParams();
               Params.put("lat", latitude);
               Params.put("long", longitude);
               Params.put("appId", APP_ID);

                letsDoSomeNetworking( Params);



            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() call back received");
            }
        };
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);


            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, 5000, 1000, mLocationListener);

     }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermissionsResult() : PERMISSION GRANTED");
                getWeatherForCurrentLocation();

            } else {
                Log.d("Clima", "PERMISSION DENIED");
            }
        }
    }
    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private  void letsDoSomeNetworking(RequestParams Params){

        AsyncHttpClient client= new AsyncHttpClient();

        client.get(WEATHER_URL, Params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers , JSONObject response){
                //when the get weather request gets successful this method gets triggered
                Log.d("Clima", "Success! JSON: "+response.toString());
                WeatherDataModel weatherData= WeatherDataModel.fromJSON(response);

                updateUI(weatherData);

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers , Throwable e, JSONObject response ){
                //when the get weather request gets unsuccessful this method gets triggered
                Log.e("Clima", "failure "+e.toString());
                Log.d("Clima", "status code "+statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather){
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName() );
        mWeatherImage.setImageResource(resourceID);

    }


    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null){mLocationManager.removeUpdates(mLocationListener);}
    }
}
