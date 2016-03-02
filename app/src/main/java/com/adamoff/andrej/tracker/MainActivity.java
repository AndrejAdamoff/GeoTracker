package com.adamoff.andrej.tracker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

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
                    startActivity(new Intent(MainActivity.this, SettingsGeoSender.class));
                        break;

                    case R.id.mapbtn:
                        startActivity(new Intent(MainActivity.this, MapsActivity.class));
                        break;

                    case R.id.bothbtn:
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
