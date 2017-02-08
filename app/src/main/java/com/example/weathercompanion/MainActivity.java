package com.example.weathercompanion;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    public String loadCity()
    {
        SharedPreferences prefs = getSharedPreferences("cities",Context.MODE_PRIVATE);
        String city=prefs.getString("name","Default");
        return city;
    }


    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         *Setup the DrawerLayout and NavigationView
         */

        if(isConnectedToInternet())
        {}
        else
        {
            Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }, 2000);

            final Handler handler1 = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }, 10000);

        }
             mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
             mNavigationView = (NavigationView) findViewById(R.id.drawer) ;


        /**
         * Accessing the NavigationView menu.
         * The getMenu method returns menu resource used by navigationView.
         * Later,we will add items to this menu.
         */
        Menu drawerMenu = mNavigationView.getMenu();


        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

             mFragmentManager = getSupportFragmentManager();
             mFragmentTransaction = mFragmentManager.beginTransaction();
             mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();

        /**
         * Creating object of AsyncClass - 'PopulateMenuItems' to get items from the database.
         * Here,we pass the above menu so that after retrieving items we can add this to it.
         */



        PopulateMenuItems populateMenuItems = new PopulateMenuItems();
        populateMenuItems.execute(drawerMenu);

        /**
         * Setup click events on the Navigation View Items.
         */
             mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
             @Override
             public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();



                 if (menuItem.getItemId() == R.id.add)
                 {
                    Intent i=new Intent(MainActivity.this,SearchActivity.class);
                     finish();
                     startActivity(i);
                 }

/**
                     if(menuItem.getItemId()==1273294)
                     {
                         FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                         xfragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
                     }

                if (menuItem.getItemId() == R.id.nav_item_inbox) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
                }
**/
                 return false;
            }

        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

                android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
                ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

                mDrawerLayout.setDrawerListener(mDrawerToggle);

                mDrawerToggle.syncState();

    }

    public class PopulateMenuItems extends android.os.AsyncTask<Menu,Void,Void> {
        /**
         * An arrayList to hold the items populated from database.
         */
        final ArrayList menuItems = new ArrayList();

        String arr=loadCity();

        private void ArraytoArraylist()
        {
                menuItems.add(arr);
        }

        @Override
        protected Void doInBackground(final Menu... params) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    /**
                     * params[0] refers to the first parameter passed to this method i.e a menu.
                     * Add items to this menu by iterating the arrayList.
                     */
                    ArraytoArraylist();
                    Menu drawerMenu = params[0];
                    for (int temp = 0; temp <= menuItems.size() - 1; temp++)
                    {
                        try {
                            Log.e("String", menuItems.get(temp).toString());
                            drawerMenu.add(0,0,findid(menuItems.get(temp).toString()),menuItems.get(temp).toString());

                        }catch (Exception e)
                        {}
                    }

                }});
            return null;
        }


        private int findid(String city)
        {
            int id=0;

            DatabaseTable db = new DatabaseTable(MainActivity.this);
            Cursor c = db.getWordMatches(city, null);
            c.moveToFirst();
            int nameindex=c.getColumnIndex(DatabaseTable.COL_NAME);
            int idindex=c.getColumnIndex(DatabaseTable.COL_ID);

            while (!c.isAfterLast())
            {
               if(city==c.getString(nameindex))
                  id=c.getInt(idindex);
            }
            return id;
        }




    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder a=new AlertDialog.Builder(MainActivity.this);
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