package com.example.weathercompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final TextView forecastTitle=(TextView)findViewById(R.id.forecastMain);
        final TextView forecastDetails=(TextView)findViewById(R.id.forecastDetail);
        Bundle forecastData=getIntent().getExtras();
        if(forecastData==null)
        {
            return;
        }
        final String daySelect=forecastData.getString("day");
        final String[] details=forecastData.getStringArray("details");
        final String[] allDays=forecastData.getStringArray("days");

        forecastTitle.setText(daySelect);
        for (int i=0;i< allDays.length ;i++){
           
            if(daySelect.equals(allDays[i])){
                forecastDetails.setText(details[i]);
            }
        }
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}



