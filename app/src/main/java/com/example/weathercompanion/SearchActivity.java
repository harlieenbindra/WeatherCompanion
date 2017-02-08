package com.example.weathercompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity {

    Button btnAddCity,btnLocate;
    AppLocationService appLocationService;
    public static String location_address;
    Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_search);
        super.onCreate(savedInstanceState);
        btnAddCity =(Button)findViewById(R.id.buttonadd);
        btnLocate=(Button)findViewById(R.id.locatebutton);
        String[] arr= null;
        List<String> items= new ArrayList<String>();
        tb=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent startIntent = new Intent(SearchActivity.this, MainActivity.class);
                finish();
                SearchActivity.this.startActivity(startIntent);
            }
        });

        try
        {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("city.txt")));
            String str_line;

            while ((str_line = reader.readLine()) != null)
            {
                str_line = str_line.trim();
                if ((str_line.length()!=0))
                {
                    items.add(str_line);
                }
            }

            arr = (String[])items.toArray(new String[items.size()]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.select_dialog_item,arr);
        final AutoCompleteTextView actv= (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(2);
        actv.setAdapter(adapter);
        btnAddCity.setOnClickListener(new View.OnClickListener()
        {

            String city;
            @Override
            public void onClick(View v)
            {

                city=actv.getText().toString();
                SharedPreferences prefs = getSharedPreferences("cities", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("name",city);
                editor.apply();
                Intent startIntent = new Intent(SearchActivity.this, MainActivity.class);
                finish();
                SearchActivity.this.startActivity(startIntent);
            }
        });

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                actv.setText(location_address);
            }
        });

        appLocationService = new AppLocationService(SearchActivity.this);

        Location gpsLocation = appLocationService.getLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation =  appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);
        if (gpsLocation != null)
        {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();

            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
        }

        else if(networkLocation !=null)
        {
            double latitude = networkLocation.getLatitude();
            double longitude = networkLocation.getLongitude();

            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
        }

        else
        {
            showSettingsAlert();
        }
    }

    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                SearchActivity.this.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler
    {
        @Override
        public void handleMessage(Message message)
        {

            switch (message.what)
            {
                case 1:
                    Bundle bundle = message.getData();
                    location_address = bundle.getString("address");
                    break;
                default:
                    location_address = null;
            }
        }

    }
    @Override
    public void onBackPressed()
    {
        android.support.v7.app.AlertDialog.Builder a=new android.support.v7.app.AlertDialog.Builder(SearchActivity.this);
        a.setMessage("Exit the app?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("NO",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

}
