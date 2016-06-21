package com.gulzar.locationtracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="gulzarxxxy";
    //UI COMPONENTS
    private Button mServiceButton;
    private EditText mUID;
    private EditText mPswd;
    Toolbar mToolbar;


    //FireBase Auth
    // [START declare_auth]
    public FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       mAuth=FirebaseAuth.getInstance();

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        Log.d(TAG,user.getDisplayName());

        if(!isMyServiceRunning(BackgroundLocationService.class))
        {
        //Intializing FireBase once
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }


        //Intializing UI
        mServiceButton=(Button)findViewById(R.id.startservice);
        mUID=(EditText)findViewById(R.id.input_id);
        mToolbar=(Toolbar)findViewById(R.id.tool_bar);
        mPswd=(EditText)findViewById(R.id.input_pswd);




        //If service is running before disables UI and displays a DialogBox
        if(isMyServiceRunning(BackgroundLocationService.class)){
            EnableDisableUI(false);
            showDialog(getBaseContext());

        }


        //Adding toolbar
        setSupportActionBar(mToolbar);


    }
    /**
     * This method is called when the user clicks the submit button!!
     */
    public boolean ValidateForm()
    {
      boolean valid=true;
        if(!checkInternetConnectivity(getBaseContext()))
        {
            displaySnackBarNoConnectivity();
            valid=false;
        }
        if(!checkGPSConnectivity())
        {
            showGPSDisabledAlertToUser();
            valid=false;
        }

        return valid;


    }
    public void SubmitClicked(View view)
    {


        if(ValidateForm()) {
            //BackgroudLocationService is started
            startService(new Intent(getBaseContext(), BackgroundLocationService.class));
            //Toast to display the start service
            Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Display the GPS enable DialogBox to user
     */
    private void showGPSDisabledAlertToUser() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    /**
     * Method use to check the connectivity of GPS
     * @return false when the GPS is Disabled
     */
    private boolean checkGPSConnectivity() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.i(TAG,"GPS ENABLED");
            //Toast.makeText(this, "GPS ENABLED", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            return false;

        }

    }

    /**
     * Displays snack message when internet connectivity is absent
     */
    private void displaySnackBarNoConnectivity() {

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.layout_rel), "No internet connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                    }
                });

// Changing message text color
        snackbar.setActionTextColor(Color.RED);

// Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }



    /**
     * To check whether the application is connected to the net
     * @param context
     * @return boolean
     */
    private boolean checkInternetConnectivity(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }


    /**
     * Service is running or not checker
     * Return true if the service is presently running.
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enables or Disables the UI component
     * @param false to disable
     */
    private void EnableDisableUI(boolean flag)
    {
        if(!flag)
        {
            mServiceButton.setEnabled(false);
            //mUID.setText(ReadFromSharedPreference());
            mUID.setEnabled(false);
            mPswd.setEnabled(false);
        }
        else
        {
            mServiceButton.setEnabled(true);
         //   mUID.setEnabled(true);
            mUID.setText("");
            mPswd.setEnabled(true);
        }
    }

    /**
     * Shows dialog to stop the current Service
     * @param context
     */
    private void showDialog(final Context context)
    {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                MainActivity.this);
        alertDialog2.setCancelable(false);
       // Setting Dialog Title
        alertDialog2.setTitle("Location is Monitored");

       // Setting Dialog Message
        alertDialog2.setMessage("Stop monitoring?");

      // Setting Icon to Dialog
        alertDialog2.setIcon(android.R.drawable.ic_dialog_alert);

     // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog

                        Toast.makeText(getApplicationContext(),
                                "Location Monitoring Stopped", Toast.LENGTH_SHORT)
                                .show();
                        stopService(new Intent(context,BackgroundLocationService.class));
                        EnableDisableUI(true);
                        mAuth.signOut();
                        startActivity(new Intent(context,EmailPasswordActivity.class));
                        finish();
                    }
                });

     // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(getApplicationContext(),
                                "You are still being monitored", Toast.LENGTH_SHORT)
                                .show();
                        dialog.cancel();
                    }
                });

// Showing Alert Dialog

        alertDialog2.show();

    }

}
