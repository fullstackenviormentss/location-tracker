package com.gulzar.locationtracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gulzar on 24-05-2016.
 */

public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private String TAG="gulxxx";

    private LocationRequest mLocationRequest;

    private String mUID;

    //Getting Firebase Reference intial i.e https://employee-tracker123.firebaseio.com/EmployeeID
    DatabaseReference mRef;
    //Getting FireBase Reference i.e https://employee-tracker123.firebaseio.com/EmployeeID/<User Id>
    DatabaseReference EmployeeIDRef;


    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();

      buildGoogleApiClient();
      //Get UID from SharedPreference !!
      mUID= ReadFromSharedPreference();
      Log.i(TAG,"UID"+mUID);


      intializefirebase();

        Log.i(TAG, "onCreate");

        mInProgress = false;

        servicesAvailable = servicesConnected();
    }

    private String ReadFromSharedPreference() {
        final String MyPREFERENCES = "MySavedUID" ;
        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES,
                MODE_PRIVATE);
        String string = prefs.getString("UID","6666666");//Default Value
        return string;
    }

    private void intializefirebase() {

        mRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://employee-tracker123.firebaseio.com/EmployeeID");
        EmployeeIDRef=mRef.child(mUID);


    }


    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean servicesConnected() {//

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
        } else {

            return false;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStart");
        Foreground();
        if(!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;
        buildGoogleApiClient();
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
        {
            mInProgress = true;
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    private void Foreground() {


        //Pending intent responds on click on notification takes back to Main Activity
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Anu Solar")
                .setContentText("You are being tracked")
                .setSmallIcon(R.drawable.ic_mood_black_24dp)
                .setContentIntent(resultPendingIntent)
                .build();

        startForeground(133, notification);
    }


    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        updateFirebase(location.getLatitude(),location.getLongitude());
        Log.d("debug", msg);
    }

    private void updateFirebase(Double lat,Double lang) {

        //Getting current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        EmployeeIDRef.child("id").setValue(mUID);
        EmployeeIDRef.child("lat").setValue(lat);
        EmployeeIDRef.child("lang").setValue(lang);
        EmployeeIDRef.child("time").setValue(currentDateandTime);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onDestroy(){
        mInProgress = false;
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every 10 econd


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
       Log.i(TAG, "GoogleApiClient connection has failed");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }


}
