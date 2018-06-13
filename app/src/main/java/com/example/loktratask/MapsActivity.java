package com.example.loktratask;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,GoogleMap.OnPolylineClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationPointsInterface {

    private GoogleMap mMap;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private Polyline polyline1;
    private int REQUEST_CODE=100;
    private GoogleApiClient mGoogleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean gpsStatus;
    private Button shift;
    private SupportMapFragment mapFragment;
    boolean checker;
    private List<Location> locationList;
    private List<LatLng> latlngList;
    private Intent serviceintent;
    private GPSService gpsService;
    private boolean bound=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        locationList=new ArrayList<>();
        latlngList=new ArrayList<>();
        shift=(Button)findViewById(R.id.start_shift);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shift.getText().toString().equals("START SHIFT")){
                    latlngList.clear();
                    locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
                    gpsStatus=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if(!gpsStatus){
                        makeRequest();
                    }
                    else{
                        if(!isOnline(getApplicationContext())){
                            Toast.makeText(getApplicationContext(),"No Internet",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            checker=true;
                            startIntentService();

                        }
                    }
                }
                else{
                    checker=false;
                    loadMap();
                }

            }
        });
    }


    private void loadMap(){
        mapFragment.getMapAsync(this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(shift.getText().toString().equals("START SHIFT")){
            shift.setText("END SHIFT");
            LatLng initialloc = latlngList.get(0);
            mMap.addMarker(new MarkerOptions().position(initialloc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initialloc));
        }
        else if(shift.getText().toString().equals("END SHIFT")){
            shift.setText("START SHIFT");
            stopService(serviceintent);
            unbindService(serviceConnection);
            Log.d("size", String.valueOf(latlngList.size()));
            polyline1 = googleMap.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .addAll(latlngList)
                    .width(POLYLINE_STROKE_WIDTH_PX).color(getResources().getColor(R.color.blue)).jointType(JointType.ROUND).geodesic(true));

            LatLng initialloc = latlngList.get(0);
            mMap.addMarker(new MarkerOptions().position(initialloc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initialloc));
            LatLng finalloc = latlngList.get(latlngList.size()-1);
            mMap.addMarker(new MarkerOptions().position(finalloc));
            googleMap.setOnPolylineClickListener(this);

        }


    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CHECK_SETTINGS:
                switch(resultCode){
                    case MapsActivity.RESULT_OK:
                        startIntentService();
                        break;
                    case MapsActivity.RESULT_CANCELED:
                        break;
                }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {

        if(requestCode == REQUEST_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                makeRequest();
            }
            else{
                Toast.makeText(MapsActivity.this, "To Continue Please provide permission", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            }
        }
    }
    private void makeRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startIntentService();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public boolean isOnline(Context mContext) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void Locations(Location l) {
        locationList.add(l);
        latlngList.add(new LatLng(l.getLatitude(),l.getLongitude()));
        if(latlngList.size()==1){
            loadMap();
        }
    }
    private void startIntentService(){

        serviceintent=new Intent(MapsActivity.this,GPSService.class);
        startService(serviceintent);
        bindService(serviceintent,serviceConnection,Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
            gpsService = binder.getService();
            bound = true;
            gpsService.setLocationPointsInterface(MapsActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    @Override
    protected void onStop() {
        super.onStop();

    }

}
