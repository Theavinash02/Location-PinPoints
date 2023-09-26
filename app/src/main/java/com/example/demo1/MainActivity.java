package com.example.demo1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int default_update_interval = 30;
    public static final int fast_update_interval = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_address, tv_updates,tv_wayPointCounts ;
    Switch sw_locationupdates, sw_gps;

    Button btn_newWayPoint,btn_showWayPointList,btn_showMap;
    // variable to remember if we are tracking or not
    boolean updateOn = false;
    // CURRENT LOCATION
    Location currentlocation;
    // LIST OF SAVED LOCATION
    List<Location> savedLocations;

    //location request related to FusedLocationProviderCLient

    LocationRequest locationRequest;

    LocationCallback locationCallback;


    //google api for location
    FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // give each UI variable a value

        tv_lat = findViewById(R.id.tvlat);
        tv_lon = findViewById(R.id.tvlon);
        tv_altitude = findViewById(R.id.tvaltitude);
        tv_accuracy = findViewById(R.id.tvaccuracy);
        tv_speed = findViewById(R.id.tvspeed);
        tv_sensor = findViewById(R.id.tvsensor);
        tv_updates = findViewById(R.id.tvupdates);
        tv_address = findViewById(R.id.tvaddress);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);


        sw_gps = findViewById(R.id.swgps);
        sw_locationupdates = findViewById(R.id.swlocationsupdates);

        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        btn_showMap = findViewById(R.id.btn_showMap);

        //set all properties of LocationRequest

        locationRequest = new LocationRequest();

        //often the interval between the location check
        locationRequest.setInterval(1000 * default_update_interval);

        //how often the location check occur when set to the most frequuent update?
        locationRequest.setFastestInterval(5000 * fast_update_interval);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                Location location = locationResult.getLastLocation();
                updateUIvalues(location);
            }
        };

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GET THE GPS LOCATION

                //ADD THE LOCATION TO GLOBAL LIST
                myApplication myApplication = (myApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentlocation);
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(view -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Using the GPS");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Using the towers and wifi");
            }
        });
        sw_locationupdates.setOnClickListener(view -> {
            if (sw_locationupdates.isChecked()) {
                //turn on location tracking
                startLocationUpdates();
            } else {
                // turn off tracking
                stopLocationUpdates();
            }
        });



        updateGPS();
    }//end of on create method

    private void stopLocationUpdates() {
        tv_updates.setText("Location is Not being tracked");
        tv_lat.setText("NOT tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not Tracking location");
        tv_address.setText("Not Tracking location");
        tv_accuracy.setText("Not Tracking location");
        tv_altitude.setText("Not Tracking location");
        tv_sensor.setText("Not Tracking location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else{
                Toast.makeText(this,"this app requires permission to be granted in ordeer to work properly",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGPS(){
        //get permissions from the user to track GPS
        //get the current location from the fused client
        //update the UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided tge permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, (location) -> {
                updateUIvalues(location);
                currentlocation = location;
            });
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);

            }
        }



    }

    private void updateUIvalues(Location location) {
    tv_lat.setText(String.valueOf(location.getLatitude()));
    tv_lon.setText(String.valueOf(location.getLongitude()));
    tv_altitude.setText(String.valueOf(location.getAltitude()));
    tv_accuracy.setText(String.valueOf(location.getAccuracy()));

    if(location.hasAltitude()){
        tv_altitude.setText("NOT avaiable");
    }

    else{
        tv_altitude.setText("not avaible");
    }

    if(location.hasSpeed()){
        tv_speed.setText(String.valueOf(location.getAltitude()));
    }
    else{
        tv_speed.setText("not avaible");
    }

    Geocoder geocoder =  new Geocoder(MainActivity.this);
    try{
        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
        tv_address.setText(addresses.get(0).getAddressLine(0));
    }
    catch (Exception e){
        tv_address.setText("Unable to get Street address");
    }

    myApplication myApplication = (myApplication)getApplicationContext();
    savedLocations = myApplication.getMyLocations();
    // show the number of waypoint saved.
    tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
    };

}