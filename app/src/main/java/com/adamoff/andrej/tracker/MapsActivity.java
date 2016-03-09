package com.adamoff.andrej.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // FragmentActivity вместо Activity нужно для поддержки Support...

    private GoogleMap mMap;
    double lat, lat0, lng, lng0, latp, lngp, speed;
    int mindist, mininterval;
    String mode;

    SupportMapFragment mapFragment;
    Location loc;
    LocationManager locationManager;
    LocationListener listener;
    PolylineOptions options;

    Handler handler;
    boolean selftrack, addownway;
    boolean n,m;
    ArrayList<LatLng> route;

    SharedPreferences sp;

    BroadcastReceiver mybroadcast;

    SharedPreferences.OnSharedPreferenceChangeListener splistener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
      //      Toast.makeText(MapsActivity.this, "shared preferences changed", Toast.LENGTH_LONG).show();
            if (s.equals("addownway")) {
              //  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.getBoolean("addownway", false)) {
                    //  if (!selftrack) {
                    mininterval = Integer.valueOf(sharedPreferences.getString("updatetimeinterval", "10"));
                    mindist = Integer.valueOf(sharedPreferences.getString("mindistance", "10"));
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mininterval, mindist, listener); //handler.post(mRunnable);
                    // }

                } else {
                    locationManager.removeUpdates(listener);
                }
            }
            if (s.equals("updatetimeinterval")) {
                locationManager.removeUpdates(listener);
                mininterval = Integer.valueOf(sharedPreferences.getString("updatetimeinterval", "10"));
                mindist = Integer.valueOf(sharedPreferences.getString("mindistance", "10"));
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mininterval, mindist, listener); //handler.post(mRunnable);
            }

            if (s.equals("mindistance")) {
                locationManager.removeUpdates(listener);
                mininterval = Integer.valueOf(sharedPreferences.getString("updatetimeinterval", "10"));
                mindist = Integer.valueOf(sharedPreferences.getString("mindistance", "10"));
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mininterval, mindist, listener); //handler.post(mRunnable);
            }

            if (s.equals("recviasms")) {
                //    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.getBoolean("recviasms", false)) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(mybroadcast, filter);
                } else unregisterReceiver(mybroadcast);
            }

            if (s.equals("sendingphone")) {
                unregisterReceiver(mybroadcast);
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(mybroadcast, filter);
            }
        }
    };

 /*   @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
*/
    //    TextView txt1, txt2;
    //  Runnable mRunnable;

 /*   public void onResume()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mybroadcast, filter);
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mybroadcast = new SMSReceiver();
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(splistener);
    //    mode = getIntent().getStringExtra("mode");

        setContentView(R.layout.activity_maps);
        final TextView info = (TextView)findViewById(R.id.info);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

// ------------- location listener -----------------------------------------------------
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double angle=0;
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat = loc.getLatitude();
                lng = loc.getLongitude();

                if (n) {    // for first time
                    n = false;
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_downward_black_24dp))
                            .flat(true)
                            .anchor((float) 0.5, (float) 0.5)
                            .rotation(0)
                            .position(new LatLng(lat, lng)));
                    latp = lat;
                    lngp = lng;
                    Toast.makeText(MapsActivity.this, "lat: "+lat+"\n"+"lng: "+lng, Toast.LENGTH_LONG).show();

                    Double radius = Double.valueOf(sp.getString("initialscale","1"));
//Toast.makeText(MapsActivity.this, "Radius: "+radius,Toast.LENGTH_LONG).show();
                    double m = radius/111;
                    LatLng begin = new LatLng((lng-m),lat-m); //new LatLng(44,20);
                    LatLng end = new LatLng((lng+m),lat+m); //new LatLng(45,21); //
                    LatLngBounds area = new LatLngBounds(begin,end);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(area, 0)); // animateCamera doesn't work here

                } else {
                    try {
                        angle = Math.toDegrees(Math.atan((lng - lngp)/(lat - latp)));
                        if ((lat - latp) < 0 & (lng - lngp) > 0) angle = 180 + angle; //90 - angle;
                        if ((lat - latp) < 0 & (lng - lngp) < 0) angle = angle - 180;
                        //  if((lat-latp)>0 & (lng-lngp)<0) angle = 360 - angle;
                        Toast.makeText(MapsActivity.this, String.valueOf(angle), Toast.LENGTH_LONG).show();
                    } catch (NullPointerException e) {
                        if ((lat-latp)>=0) angle = (double) 0;
                        if ((lat-latp)<0) angle = (double) 180;
                        Toast.makeText(MapsActivity.this, "Exception 90", Toast.LENGTH_LONG).show();
                    }

                    info.setText("Accuracy:" + loc.getAccuracy() + "\n" + "Speed: " + loc.getSpeed());
                    if (loc.getSpeed() == 0)
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_downward_black_24dp))
                                .flat(true)
                                .anchor((float) 0.5, (float) 0.5)
                                .position(new LatLng(lat, lng)));
                    else {
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_black_24dp))
                                .flat(true)
                                .anchor((float) 0.5, (float) 0.5)
                                .rotation((float) angle)
                                .position(new LatLng(lat, lng)));

                        mMap.addPolyline(options.geodesic(true).add(new LatLng(latp, lngp), new LatLng(lat, lng)).width(3).color(Color.RED));
                        latp = lat;
                        lngp = lng;
                    }
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
          /*      if (i == LocationProvider.AVAILABLE) txt2.setText("Provider status: available");
                if (i == LocationProvider.OUT_OF_SERVICE)
                    txt2.setText("Provider status: out of service");
                if (i == LocationProvider.TEMPORARILY_UNAVAILABLE)
                    txt2.setText("Provider status: temporarily unavailable");
                */
            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
  // ---------- end of listener ---------------------------------

    //    if (mode.equals("selftrack")) selftrack = true;

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.getBoolean("recviasms", false)) {
      //      selftrack = false;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(mybroadcast, filter);
        }
        if (sp.getBoolean("receiveviafacebook", false)) {


        }
        if (sp.getBoolean("receiveviatwitter", false)) {

        }

        if (sp.getBoolean("addownway", false)) {
            mininterval = Integer.valueOf(sp.getString("updatetimeinterval", "10"));
            mindist = Integer.valueOf(sp.getString("mindistance", "10"));
   Toast.makeText(MapsActivity.this, "time interval"+mininterval+"\n"+"distance"+mindist,Toast.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mininterval, mindist, listener); //handler.post(mRunnable);
        }


            Toast.makeText(MapsActivity.this, "onCreate", Toast.LENGTH_LONG).show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        options = new PolylineOptions().geodesic((true));

        //     txt1 = (TextView)findViewById(R.id.txt1);
        //     txt2 = (TextView)findViewById(R.id.txt2);
   //     addownway = sp.getBoolean("addownway", false);

   //     if (selftrack || addownway) {
     //       handler = new Handler(Looper.getMainLooper());


 /*       List<String> list = locationManager.getAllProviders();
        for (String prov : list) {
            //   String prov = list.get(i);
            txt1.setText(prov + "\nEnabled: " + locationManager.isProviderEnabled(prov));
        }
        */
            n = true;
            m=true;

   // }
        mapFragment.getMapAsync(this);
    }

  /*  Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
          try { loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
          catch (SecurityException se) {
              se.printStackTrace();
          }
            if (loc == null) {
                Toast.makeText(MapsActivity.this, "Can not get location", Toast.LENGTH_LONG).show();   //{ lat = -34; lng = 151;}
                mMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
               }
            else {
    //           txt2.setText("Satellites: "+loc.getExtras().getInt("satellites"));
                lat = loc.getLatitude();
                lng = loc.getLongitude();
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));     //.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot14)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            }
     //       handler.postDelayed(mRunnable,10000);
        }
    };
*/
    @Override
    protected void onNewIntent(Intent intent) {  // new Intents are coming from SMS receiver
        super.onNewIntent(intent);

     //   if (!selftrack) {

            setIntent(intent); // сохраняем этот новый интент как основной

            //     Toast.makeText(MapsActivity.this, "onNewIntent", Toast.LENGTH_LONG).show();
            String sms = intent.getStringExtra("sms");
            String[] strValues = sms.split(",");
            double time = Double.parseDouble(strValues[0]);
            lat = Double.parseDouble(strValues[1]);
            lng = Double.parseDouble(strValues[2]);
            speed = Double.parseDouble(strValues[3]);

            //    lat = intent.getDoubleExtra("lat", lat0);
            //    lng = intent.getDoubleExtra("lng", lng0);

            if (m) {   // for first time
                m = false;
                latp = lat;
                lngp = lng;
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_downward_black_24dp))
                        .flat(true)
                        .anchor((float) 0.5, (float) 0.5)
                        .position(new LatLng(lat, lng)));

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Double radius = Double.valueOf(sp.getString("initialscale","1"));
                double m = radius/111;
                LatLng begin = new LatLng((lat-m),lng-m);
                LatLng end = new LatLng((lat+m),lng+m);
                LatLngBounds area = new LatLngBounds(begin,end);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(area, 0)); // animateCamera doesn't work here

            } else {
                if (speed == 0) {
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_downward_black_24dp))
                            .flat(true)
                            .anchor((float) 0.5, (float) 0.5)
                            .position(new LatLng(lat, lng)));
                    mMap.addPolyline(options.geodesic(true).add(new LatLng(latp, lngp), new LatLng(lat, lng)).width(3).color(Color.YELLOW));
                    latp = lat;
                    lngp = lng;
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                } else {
                    // calculate angle of marker rotation:
                    double angle = 0.0;
                    try {
                        angle = Math.toDegrees(Math.atan((lng - lngp)/(lat - latp)));
                        if ((lat - latp) < 0 & (lng - lngp) > 0) angle = 180 + angle; //90 - angle;
                        if ((lat - latp) < 0 & (lng - lngp) < 0) angle = angle - 180;
                        //  if((lat-latp)>0 & (lng-lngp)<0) angle = 360 - angle;
                        Toast.makeText(MapsActivity.this, String.valueOf(angle), Toast.LENGTH_LONG).show();
                    } catch (NullPointerException e) {
                        if ((lat-latp)>=0) angle = (double) 0;
                        if ((lat-latp)<0) angle = (double) 180;
                        Toast.makeText(MapsActivity.this, "Exception 90", Toast.LENGTH_LONG).show();
                    }

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_black_24dp))
                            .flat(true)
                            .anchor((float) 0.5, (float) 0.5)
                            .rotation((float)angle)
                            .position(new LatLng(lat, lng)));

                    mMap.addPolyline(options.geodesic(true).add(new LatLng(latp, lngp), new LatLng(lat, lng)).width(3).color(Color.RED));
                    latp = lat;
                    lngp = lng;
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                }
            }
             //      mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
     //   }

  /*
        Toast.makeText(MapsActivity.this, "lat: " + lat + "\n" + "lng: " + lng, Toast.LENGTH_LONG).show();
        //   mapFragment.getMapAsync(this);
        try {
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MapsActivity.this, "Exception" + "\n" + "lat: " + lat + "\n" + "lng: " + lng, Toast.LENGTH_LONG).show();
        }
        */
  }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (selftrack || addownway)

        {  mininterval= Integer.valueOf(sp.getString("updatetimeinterval","10"));
           mindist = Integer.valueOf(sp.getString("mindistance","10"));
           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mininterval, mindist, listener); //handler.post(mRunnable);
    }


    //   try { Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
    //   catch (SecurityException s){
    //       s.printStackTrace();
    //   };
     //  if (loc == null)

     //  LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());

        // Add a marker in Sydney and move the camera
      //      LatLng sydney = new LatLng(lat0, lng0);

  // handler.post(mRunnable);


    //    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    //    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

/*    @Override
    public void onBackPressed() {
        super.onBackPressed();
       if (autotrack) locationManager.removeUpdates(listener);
       // route = null;
    }
*/
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
            startActivity(new Intent(this,SettingsMap.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(listener);
        unregisterReceiver(mybroadcast);
    }
}
