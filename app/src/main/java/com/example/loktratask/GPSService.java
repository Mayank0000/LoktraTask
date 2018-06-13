package com.example.loktratask;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Shubham Shukla on 15-11-2017.
 */

public class GPSService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOGSERVICE = "#######";
    private final IBinder binder = new LocalBinder();

    LocationPointsInterface locationPointsInterface;




    public class LocalBinder extends Binder {
        GPSService getService() {
            // Return this instance of MyService so clients can call public methods
            return GPSService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.i(LOGSERVICE, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGSERVICE, "onStartCommand");
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOGSERVICE, "onConnected" + bundle);

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
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (l != null) {
            try{
                //Log.d("lat", String.valueOf(l.getLatitude()));
                //params.put("lat",l.getLatitude());
                //params.put("lon",l.getLongitude());
                Log.d("lat onconnected", String.valueOf(l.getLatitude()));
                Log.d("long onconnected", String.valueOf(l.getLongitude()));


            }
            catch(Exception e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if(isOnline(getApplicationContext())){
            //new fetchApi().execute();
        }
        else{
            Toast.makeText(getApplicationContext(),"Please enable your internet connection !",Toast.LENGTH_SHORT).show();
        }

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOGSERVICE, "onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOGSERVICE+"changed", "lat " + location.getLatitude());
        Log.i(LOGSERVICE+"changed", "lng " + location.getLongitude());
        try{
            //Log.d("lat", String.valueOf(location.getLatitude()));
            //Log.d("long", String.valueOf(location.getLongitude()));
            locationPointsInterface.Locations(location);

        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGSERVICE, "onDestroy");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOGSERVICE, "onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(7000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();

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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    public boolean isOnline(Context mContext) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    public void setLocationPointsInterface(LocationPointsInterface locationPointsInterface) {
        this.locationPointsInterface=locationPointsInterface;
    }
}
