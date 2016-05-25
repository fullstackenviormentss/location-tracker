package com.gulzar.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    //UI COMPONENTS
    private Button mServiceButton;
    private EditText mUID;

    //SHARED PREFERENCE COMPONENT
    public static final String MyPREFERENCES = "MySavedUID" ;
    SharedPreferences msharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intializing FireBase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Intializing UI
        mServiceButton=(Button)findViewById(R.id.startservice);
        mUID=(EditText)findViewById(R.id.input_id);


    }

    /**
     * This method is called when the user clicks the submit button!!
     */
    public void clicked(View view)
    {
            if(mUID.getText()==null||mUID.getText().length()!=7)
               Toast.makeText(getBaseContext(),"Enter correct ID",Toast.LENGTH_SHORT).show();
                else{
            SaveToSharedPreference(mUID.getText().toString());
            startService(new Intent(getBaseContext(),BackgroundLocationService.class));
            Toast.makeText(getBaseContext(),"Service started",Toast.LENGTH_SHORT).show();
            }
    }


    /**
     * SAVES UID TO SHARED PREFERENCE WHICH CAN BE ACCESS LATER BY BackgroudLocationService Service
     * Context notPrivate*/
    private void SaveToSharedPreference(String UID) {
        msharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=msharedpreferences.edit();
        editor.putString("UID",UID);
        editor.commit();

    }
}
