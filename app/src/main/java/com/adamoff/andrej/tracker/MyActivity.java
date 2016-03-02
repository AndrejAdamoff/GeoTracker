package com.adamoff.andrej.tracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sms = getIntent().getStringExtra("sms");
      //  double lat=0;
      //  double lng=0;
        // converting string to double:
    //    String lattxt = sms.substring(sms.indexOf("lat:")+4, sms.indexOf("lng:"));
    //    String lngtxt = sms.substring(sms.indexOf("lng:")+4);
      // if (lattxt.length()>0)

        String[] strValues = sms.split(",");
        double latitude = Double.parseDouble(strValues[2]) * .01;
        double longitude = Double.parseDouble(strValues[4]) * .01;
      //     double lat = Double.parseDouble(lattxt);
     //  if (lngtxt.length()>0)
      //     double lng = Double.parseDouble(lngtxt);

        startActivity(new Intent(this, MapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("lat",latitude).putExtra("lng",longitude));
        finish();
    }

    }

