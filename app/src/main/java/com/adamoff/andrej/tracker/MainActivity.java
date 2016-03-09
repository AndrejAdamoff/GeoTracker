package com.adamoff.andrej.tracker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button senderbtn = (Button)findViewById(R.id.senderbtn);
        Button mapbtn = (Button)findViewById(R.id.mapbtn);
        Button bothbtn = (Button)findViewById(R.id.bothbtn);

        View.OnClickListener l = new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                switch (view.getId()){

                    case R.id.senderbtn:
                        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean("dontshow_s", false))
                    startActivity(new Intent(MainActivity.this, SettingsGeoSender.class));
                        else startActivity(new Intent(MainActivity.this, GeoSender.class));

                        break;

                    case R.id.mapbtn:
                        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .getBoolean("dontshow_m", false))
                        startActivity((new Intent(MainActivity.this, SettingsMap.class)).putExtra("mode","receive"));
                        else startActivity((new Intent(MainActivity.this, MapsActivity.class)).putExtra("mode","receive"));
                        break;

                    case R.id.bothbtn:
                     //   if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                     //           .getBoolean("dontshow_m", false))
                            startActivity((new Intent(MainActivity.this, GeoSender.class)).putExtra("mode","testSMS"));
                     //   else startActivity((new Intent(MainActivity.this, MapsActivity.class)).putExtra("mode","selftrack"));

                        break;
                }
            }
        };

        senderbtn.setOnClickListener(l);
        mapbtn.setOnClickListener(l);
        bothbtn.setOnClickListener(l);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
