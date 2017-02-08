package com.example.weathercompanion;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;


public class CurrentFragment extends Fragment {

    ProgressDialog pDialog;
    Button btnByCityName;
    TextView textViewResult;
    TextToSpeech t1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.current_layout, container, false);
        textViewResult = (TextView)rootView.findViewById(R.id.result);

        t1 = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status)
            {
                if (status != TextToSpeech.ERROR)
                {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        SharedPreferences prefs = getActivity().getSharedPreferences("cities",Context.MODE_PRIVATE);
        String city=prefs.getString("name","Default");
        new OpenWeatherMapTask(
                city,
                textViewResult).execute();
        return rootView;
    }



    public class OpenWeatherMapTask extends AsyncTask<Void, Void, String> {

        String cityName;
        TextView tvResult;

        String Appid = "67746b7c3b4542644cde56475ee0cda1";
        String queryWeather = "http://api.openweathermap.org/data/2.5/weather?q=";
        String queryDummyKey = "&appid=" + Appid;

        OpenWeatherMapTask(String cityName, TextView tvResult){
            this.cityName = cityName;
            this.tvResult = tvResult;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            String queryReturn;

            String query = null;
            try {
                query = queryWeather + URLEncoder.encode(cityName, "UTF-8") + queryDummyKey;
                queryReturn = sendQuery(query);
                result += ParseJSON(queryReturn);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                queryReturn = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                queryReturn = e.getMessage();
            }

            return result;
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Please Wait");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();
            tvResult.setText(s);
        }

        private String sendQuery(String query) throws IOException {
            String result = "";

            URL searchURL = new URL(query);

            HttpURLConnection httpURLConnection = (HttpURLConnection)searchURL.openConnection();
            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader,
                        8192);

                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    result += line;
                }

                bufferedReader.close();
            }

            return result;
        }

        private String ParseJSON(String json){
            String jsonResult = "";
            String haze="haze";
            String clouds="clouds";
            String rain="rain";
            String thunderstorm="thunderstorm";
            String drizzle="drizzle";
            String snow="snow";
            String clear="clear";


            try {
                JSONObject JsonObject = new JSONObject(json);
                String cod = jsonHelperGetString(JsonObject, "cod");

                if(cod != null){
                    if(cod.equals("200"))
                    {

                        jsonResult += jsonHelperGetString(JsonObject, "name") + "\n";
                        JSONObject sys = jsonHelperGetJSONObject(JsonObject, "sys");
                        if(sys != null){
                            jsonResult += jsonHelperGetString(sys, "country") + "\n";
                        }
                        jsonResult += "\n";

                        JSONObject coord = jsonHelperGetJSONObject(JsonObject, "coord");
                        if(coord != null){
                            String lon = jsonHelperGetString(coord, "lon");
                            String lat = jsonHelperGetString(coord, "lat");
                            jsonResult += "lon: " + lon + "\n";
                            jsonResult += "lat: " + lat + "\n";
                        }
                        jsonResult += "\n";

                        JSONArray weather = jsonHelperGetJSONArray(JsonObject, "weather");
                        if(weather != null){
                            for(int i=0; i<weather.length(); i++){
                                JSONObject thisWeather = weather.getJSONObject(i);
                                jsonResult += "weather " + i + ":\n";
                                jsonResult += "id: " + jsonHelperGetString(thisWeather, "id") + "\n";
                                jsonResult += jsonHelperGetString(thisWeather, "main") + "\n";

                                String maind=jsonHelperGetString(thisWeather, "main");

                                jsonResult += jsonHelperGetString(thisWeather, "description") + "\n";
                                jsonResult += "\n";

                                //Define Notification Manager
                                NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                                //Define sound URI
                                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                if(maind.toLowerCase().indexOf(thunderstorm.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("THUNDERSTORM")
                                            .setContentText("Stay at Home").setVibrate(new long[] { 1000, 1000 }); //This sets the sound to play

                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "Stay at Home, clouds are angry with you, Actually a thunderstorm";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                if(maind.toLowerCase().indexOf(drizzle.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("DRIZZLING")
                                            .setContentText("It might rain,pls take an umbrella with u and pick your clothes from outside if you left them for drying !").setVibrate(new long[] { 1000, 1000 });
                                    //This sets the sound to play

                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "It might rain,pls take an umbrella with u and pick your clothes from outside if you left them for drying !";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                if(maind.toLowerCase().indexOf(snow.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("SNOWING")
                                            .setContentText("It's snowing outside,do wear a heavy jacket !").setVibrate(new long[] { 1000, 1000 });
                                    //This sets the sound to play

                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "It's snowing outside,do wear a heavy jacket !";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                if(maind.toLowerCase().indexOf(clear.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("CLEAR SKY")
                                            .setContentText("Wow,the weather is clear outside !").setVibrate(new long[] { 1000, 1000 });
                                    //This sets the sound to play
                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "Wow,the weather is clear outside !";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                if(maind.toLowerCase().indexOf(haze.toLowerCase()) != -1)
                                {
                                    CustomNotification();
                                    String toSpeak = "It's Hazy today !";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                else if(maind.toLowerCase().indexOf(clouds.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("hey")
                                            .setContentText("It's cloudy").setVibrate(new long[] { 1000, 1000 });
                                    //This sets the sound to play

                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "It's cloudy";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }

                                else if(maind.toLowerCase().indexOf(rain.toLowerCase()) != -1)
                                {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(getActivity().getApplicationContext())
                                            .setSmallIcon(R.drawable.image3)
                                            .setContentTitle("RAINING")
                                            .setContentText("Pls take an umbrella with u when u go out and pick your clothes from outside if you left them for drying !").setVibrate(new long[] { 1000, 1000 });
                                    //This sets the sound to play

                                    //Display notification
                                    notificationManager.notify(0, mBuilder.build());
                                    String toSpeak = "Please take an umbrella with u when u go out and pick your clothes from outside if you left them for drying !";
                                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        }

                        JSONObject main = jsonHelperGetJSONObject(JsonObject, "main");
                        if(main != null){
                            jsonResult += "temp: " + jsonHelperGetString(main, "temp") + "\n";
                            jsonResult += "pressure: " + jsonHelperGetString(main, "pressure") + "\n";
                            jsonResult += "humidity: " + jsonHelperGetString(main, "humidity") + "\n";
                            jsonResult += "temp_min: " + jsonHelperGetString(main, "temp_min") + "\n";
                            jsonResult += "temp_max: " + jsonHelperGetString(main, "temp_max") + "\n";
                            jsonResult += "sea_level: " + jsonHelperGetString(main, "sea_level") + "\n";
                            jsonResult += "grnd_level: " + jsonHelperGetString(main, "grnd_level") + "\n";
                            jsonResult += "\n";
                        }

                        jsonResult += "visibility: " + jsonHelperGetString(JsonObject, "visibility") + "\n";
                        jsonResult += "\n";

                        JSONObject wind = jsonHelperGetJSONObject(JsonObject, "wind");
                        if(wind != null){
                            jsonResult += "wind:\n";
                            jsonResult += "speed: " + jsonHelperGetString(wind, "speed") + "\n";
                            jsonResult += "deg: " + jsonHelperGetString(wind, "deg") + "\n";
                            jsonResult += "\n";
                        }

                    }else if(cod.equals("404")){
                        String message = jsonHelperGetString(JsonObject, "message");
                        jsonResult += "cod 404: " + message;
                    }
                }else{
                    jsonResult += "cod == null\n";
                }

            } catch (JSONException e) {
                e.printStackTrace();
                jsonResult += e.getMessage();
            }

            return jsonResult;
        }

        private String jsonHelperGetString(JSONObject obj, String k){
            String v = null;
            try {
                v = obj.getString(k);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return v;
        }

        private JSONObject jsonHelperGetJSONObject(JSONObject obj, String k){
            JSONObject o = null;

            try {
                o = obj.getJSONObject(k);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return o;
        }

        private JSONArray jsonHelperGetJSONArray(JSONObject obj, String k){
            JSONArray a = null;

            try {
                a = obj.getJSONArray(k);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return a;
        }
    }

    public void CustomNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews remoteViews = new RemoteViews(getActivity().getPackageName(),
                R.layout.customnotification);

        // Set Notification Title
        // Set Notification Text

        // Open NotificationView.java Activity

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                // Set Icon
                .setSmallIcon(R.drawable.rain)
                // Set Ticker Message
                .setTicker(getString(R.string.customnotificationticker))
                // Dismiss Notification
                .setAutoCancel(true)
                .setContent(remoteViews);

        // Locate and set the Image into customnotificationtext.xml ImageViews
        remoteViews.setImageViewResource(R.id.imagenotileft,R.drawable.images);

        // Locate and set the Text into customnotificationtext.xml TextViews
        remoteViews.setTextViewText(R.id.text,"ITS HAZY TODAY");

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }

}
