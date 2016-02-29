package com.adamoff.andrej.tracker;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Handler;

import java.util.List;
import java.util.logging.LogRecord;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // FragmentActivity вместо Activity нужно для поддержки Support...


    private GoogleMap mMap;
    double lat, lat0, lng, lng0;
    SupportMapFragment mapFragment;
    Location loc;
    LocationManager locationManager;
    Handler handler;
    TextView txt1, txt2;
    //  Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toast.makeText(MapsActivity.this, "onCreate", Toast.LENGTH_LONG).show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        lat0 = -34;
        lng0 = 151;

        txt1 = (TextView)findViewById(R.id.textView);
        txt2 = (TextView)findViewById(R.id.textView2);

        handler = new Handler(Looper.getMainLooper());

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        List<String> list = locationManager.getAllProviders();
        for (String prov : list){
            //   String prov = list.get(i);
            txt1.setText(prov+"\nEnabled: "+locationManager.isProviderEnabled(prov));
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                if (i == LocationProvider.AVAILABLE)  txt2.setText("Status: available");
                if (i == LocationProvider.OUT_OF_SERVICE)  txt2.setText("Status: out of service");
                if (i == LocationProvider.TEMPORARILY_UNAVAILABLE)  txt2.setText("Status: temporarily unavailable");
              }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

       try {locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 0, listener);}
        catch (SecurityException se2) {se2.printStackTrace();}

        mapFragment.getMapAsync(this);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
          try { loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
          catch (SecurityException se) {
              se.printStackTrace();
          };
            if (loc == null) {
                Toast.makeText(MapsActivity.this, "Can not get location", Toast.LENGTH_LONG).show();   //{ lat = -34; lng = 151;}
                mMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
               }
            else {
               txt2.setText("Satellites: "+loc.getExtras().getInt("satellites"));
                lat = loc.getLatitude();
                lng = loc.getLongitude();
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot14)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            }

     //       handler.postDelayed(mRunnable,10000);
        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent); // сохраняем этот новый интент как основной

   //     Toast.makeText(MapsActivity.this, "onNewIntent", Toast.LENGTH_LONG).show();

        lat = intent.getDoubleExtra("lat",lat0);
        lng = intent.getDoubleExtra("lng", lng0);
        lat0 = lat;
        lng0 = lng;

        Toast.makeText(MapsActivity.this, "lat: "+lat+"\n"+"lng: "+lng, Toast.LENGTH_LONG).show();
     //   mapFragment.getMapAsync(this);
    try {
       mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker in Sydney"));
       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(MapsActivity.this, "Exception"+"\n"+"lat: "+lat+"\n"+"lng: "+lng, Toast.LENGTH_LONG).show();
    }
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



    //   try { Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
    //   catch (SecurityException s){
    //       s.printStackTrace();
    //   };
     //  if (loc == null)

     //  LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());

        // Add a marker in Sydney and move the camera
  //      LatLng sydney = new LatLng(lat0, lng0);

         handler.post(mRunnable);
    //    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    //    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
   }
