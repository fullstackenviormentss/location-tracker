package com.gulzar.locationtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button mServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //startService(new Intent(getBaseContext(),BackgroundLocationService.class));
        mServiceButton=(Button)findViewById(R.id.startservice);

    }
    public void clicked(View view)
    {

            startService(new Intent(getBaseContext(),BackgroundLocationService.class));
            Toast.makeText(getBaseContext(),"Service started",Toast.LENGTH_SHORT).show();
    }
}
