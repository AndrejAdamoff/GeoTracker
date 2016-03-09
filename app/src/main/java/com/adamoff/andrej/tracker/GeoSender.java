package com.adamoff.andrej.tracker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class GeoSender extends Activity {

    TextView txtlat, txtlong, prec, satnmb, gpsstat, txtspeed, txttest;
    EditText etphone, etperiod, sendperiod;
    Button stopbtn;
    String phone, period, sndperiod;
    String gpsstatus;

    boolean n,m;
    int smsNumber;
    int smssndint, mindist, locupdperiod;
    float accuracy;
    double latitude, longitude,speed;

    LocationManager lm;
    LocationListener listener;
    GpsStatus.NmeaListener nmealistener;
    Location loc;

  //  ArrayList<String> M;

    File Geodir;
    FileOutputStream fos;
 //   BufferedOutputStream bos; // = new BufferedOutputStream(fos);
    OutputStreamWriter out;

    BroadcastReceiver mybroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geosender);

        //   phone = "+381612751056";

        lm = (LocationManager)getSystemService(LOCATION_SERVICE);


        GpsStatus.Listener gpsstatlist = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int i) {
                if (i == GpsStatus.GPS_EVENT_FIRST_FIX) gpsstat.setText("GPS fixed");
                if (i == GpsStatus.GPS_EVENT_STARTED) gpsstat.setText("GPS started");
                if (i == GpsStatus.GPS_EVENT_STOPPED) gpsstat.setText("GPS stopped");
                if (i == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {  //lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).getExtras().getInt("satellites");  //    lm.getGpsStatus().getSatellites() gpsstat.setText("GPS fixed");
                    int satellitesInFix =0;
                    int satellites =0;
                    for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites()) {
                        if (sat.usedInFix()) {
                            satellitesInFix++;
                        }
                        satellites++;
                    }
                    satnmb.setText("Number of visible satellites: "+satellites+"\n"+"Satellites used in fix: "+satellitesInFix);
                }
           }
        };
        lm.addGpsStatusListener(gpsstatlist);

        //
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             latitude = loc.getLatitude();
             longitude = loc.getLongitude();
             accuracy = loc.getAccuracy();
             speed = loc.getSpeed();
         //       Toast.makeText(GeoSender.this, "lat: "+location.getLatitude()+"\n"+"long: "+location.getLongitude(), Toast.LENGTH_LONG).show();

             txtlat.setText("Lat: " + latitude);
             txtlong.setText("Long: " + longitude);
       //      gpsstat.setText("GPS status: " + gpsstatus);
         //    satnmb.setText("Number of satellites: " + loc.ge);
             prec.setText("Accuracy: " + accuracy);
             txtspeed.setText ("Speed: "+ speed);

                if (m) {  // time to send sms
                m=false;
                smsNumber++;
                String lat = Double.toString(latitude);
                String lng = Double.toString(longitude);
                String sp = Double.toString(speed);
                sendSMS (phone,lat,lng,sp);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                gpsstat.setText(s);
            }

            @Override
            public void onProviderEnabled(String s) {
                gpsstat.setText(s);
            }

            @Override
            public void onProviderDisabled(String s) {
                gpsstat.setText(s);
            }
        };
        // listerner is required to be registered, because GPS receiver doesn't start without it:
    //    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, listener);

        nmealistener = new GpsStatus.NmeaListener() {

            @Override
            public void onNmeaReceived(long l, String s) {

  //              double latitude =0, longitude=0, precise=0;

                //  final String sentence = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
                System.out.println("nmea: " + s);
    //            if (n) {  // if timer finished. If not then exit

                // save NMEA sentence to log:
                    try {
                        out.write(s);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

    /*               n = false;
                // start new timer:
                    new CountDownTimer(Integer.parseInt(period) * 1000, Integer.parseInt(period) * 1000) {
                        @Override
                        public void onTick(long l) {
                            n = true;
                        }
                        @Override
                        public void onFinish() {
                            n = true;
                        }
                    }.start();
*/
          /*      // displaying coordinates:
                    if (s.startsWith("$GPGGA")) {
                        String[] strValues = s.split(",");
                try {
                    latitude = Double.parseDouble(strValues[2]) * .01;
                    if (strValues[3].charAt(0) == 'S') {
                        latitude = -latitude;
                    }
                    longitude = Double.parseDouble(strValues[4]) * .01;
                    if (strValues[5].charAt(0) == 'W') {
                        longitude = -longitude;
                    }
                    precise = Double.parseDouble(strValues[8]);

                    if (strValues[6].equals("0")) gpsstatus = "GPS not fixed";
                    if (strValues[6].equals("1")) gpsstatus = "GPS fixed";
                    if (strValues[6].equals("2")) gpsstatus = "Differential GPS fix";
                }catch (Exception e){e.printStackTrace();}


                        txtlat.setText("Lat: " + latitude);
                        txtlong.setText("Long: " + longitude);
                        gpsstat.setText("GPS status: " + gpsstatus);
                        satnmb.setText("Number of satellites: " + strValues[7]);
                        prec.setText("HDOP: " + precise);

                    // check whether have to send SMS:
                        if (m){  // have to send SMS
                            m=false;
                            // start new timer:
                            new CountDownTimer(smssndint * 1000*60, smssndint * 1000*60) {
                                @Override
                                public void onTick(long l) {
                                    m = true;
                                }
                                @Override
                                public void onFinish() {
                                    m = true;
                                }
                            }.start();

         //            Toast.makeText(GeoSender.this, "Sending SMS",Toast.LENGTH_LONG).show();
                            smsNumber++;
                            sendSMS(phone, s);   // send NMEA string by SMS
                        }

                    } else { // if not GPGGA

                        //   txt2.setText(s);
                    }
                */
//            }
            }
        };

        txtlat = (TextView)findViewById(R.id.txtlat);
        txtlong = (TextView)findViewById(R.id.txtlong);
        prec = (TextView)findViewById(R.id.prec);
        txtspeed = (TextView)findViewById(R.id.txtspeed);
        gpsstat = (TextView)findViewById(R.id.gpsstat);
        satnmb = (TextView)findViewById(R.id.satnmb);
        txttest = (TextView)findViewById(R.id.txttest);
        txttest.setVisibility(View.INVISIBLE);

     /*   etphone = (EditText)findViewById(R.id.etphone);
        etperiod = (EditText)findViewById(R.id.etperiod);
        dist = (TextView)findViewById(R.id.mindist);
        sendperiod = (EditText)findViewById(R.id.etsendperiod);
*/
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        phone = sp.getString("receivingphone", "Receiving phone");
        locupdperiod = Integer.valueOf(sp.getString("updatetimeinterval","10"));
        mindist = Integer.valueOf(sp.getString("mindistance", "10"));
        smssndint = Integer.valueOf(sp.getString("smssendinginterval", "3"));

        try {if (getIntent().getStringExtra("mode").equals("testSMS"))
                 txttest.setVisibility(View.VISIBLE);
        }
        catch (Exception e){}

        final Button startwithlog = (Button)findViewById(R.id.startwithlog);
      //  Button startnolog = (Button) findViewById(R.id.startnolog);
        stopbtn = (Button)findViewById(R.id.stopbtn);
        stopbtn.setClickable(false);
    //    stopbtn.setText("Stopped");
    //    startwithlog.setText("Start");

        startwithlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

    //            M = new ArrayList<String>();

//                etphone.setText("Rec.phone: "+phone);
//                etperiod.setText("Loc.update period: "+locupdperiod);
//                dist.setText("Min.dist: "+ mindist);
//                sendperiod.setText("SMS sending period: " + smssndint);
         //       distance = dist.get
                // listener registration
                     //    try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, locupdperiod, mindist, listener);
            //}
                //         catch (SecurityException s) {s.printStackTrace();}
                // listener registration
             //   try  lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, listener);

             // create folder if needed
                Geodir = new File(Environment.getExternalStorageDirectory(), "GeoTracker");
                Geodir.mkdir();

       //      Toast.makeText(GeoSender.this, "Date: "+getCurrentTimeStamp(),Toast.LENGTH_LONG).show();

             // open log file
              try {File logfile = new File(Geodir.toString(), "GeoLog_"+getCurrentTimeStamp()+".txt");
                       fos = new FileOutputStream(logfile);
               //      bos = new BufferedOutputStream(fos);
                     out = new OutputStreamWriter(fos);
                 Toast.makeText(GeoSender.this, "Creating log file:"+"\n"+logfile, Toast.LENGTH_LONG).show();
              }
              catch (Exception e){
                     e.printStackTrace();
                 Toast.makeText(GeoSender.this, "file is NOT created",Toast.LENGTH_LONG).show();
              }

                //
                  lm.addNmeaListener(nmealistener); // nmealistener starts

                  n= true; // allow to start timer
                  m = true; // allow to start timer
                  smsNumber = 0;
              //  catch (SecurityException s) {s.printStackTrace();}

                startwithlog.setText("Working...");
                startwithlog.setClickable(false);
                stopbtn.setClickable(true);
                stopbtn.setText("Stop");

                try {if (getIntent().getStringExtra("mode").equals("testSMS"))
                { mybroadcast = new SMSReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(mybroadcast, filter);
                  }
                }
                catch (Exception e){}
            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           //     try {
                    stopbtn.setClickable(false);
                    stopbtn.setText("Stopped");
                    startwithlog.setText("Start");
                    startwithlog.setClickable(true);

                    lm.removeUpdates(listener);
                    lm.removeNmeaListener(nmealistener);
        Toast.makeText(GeoSender.this, "listeners are removed", Toast.LENGTH_LONG).show();

                //save log in a file:
              //     try {saveFile(M, "GeoLog.txt");}
              //     catch (Exception e){e.printStackTrace();}

                // close and save log file
                try {
                    out.close();
                  //  bos.flush();
                    fos.close();
                } catch (Exception e) {e.printStackTrace();}
                try { if (getIntent().getStringExtra("mode").equals("testSMS"))
                {unregisterReceiver(mybroadcast);
             //    txttest.setVisibility(View.INVISIBLE);
                }
                }
                catch (Exception e){}
            }
          //      catch (SecurityException s) {
          //          s.printStackTrace();}
          //  }
        });

    //    List<String> providers = lm.getAllProviders();
    //    for (String prov : providers ) {
    //        txt1.setText(prov+"\n");}

    }

    private void saveFile(ArrayList<String> M, String filename) throws IOException, FileNotFoundException {
        File logfile = new File(Environment.getExternalStorageDirectory(),filename);

        FileOutputStream fos = new FileOutputStream(logfile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
     //   BufferedInputStream bis = new BufferedInputStream(input);
    //    int aByte;
        OutputStreamWriter out = new OutputStreamWriter(bos);

       for (int i =0; i<M.size(); i++)  {
            out.write(M.get(i));
        }
        out.close();
        bos.flush();
        bos.close();
    }

    /*
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try { loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
            catch (SecurityException se) {
                se.printStackTrace();
            };
            if (loc == null) {
         //       Toast.makeText(MapsActivity.this, "Can not get location", Toast.LENGTH_LONG).show();   //{ lat = -34; lng = 151;}
         //       mMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)));
        //        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
                String lat = Double.toString(loc.getLatitude());
                String lng = Double.toString(loc.getLongitude());
                sendSMS (lat,lng);

            }
            else {
                txt2.setText("Satellites: "+loc.getExtras().getInt("satellites"));
                lat = loc.getLatitude();
                lng = loc.getLongitude();
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot14)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            }

            handler.postDelayed(mRunnable,10000);
        }
    };

    */



    public void sendSMS (String phoneNumber, String latitude, String longitude, String speed ){

        String SENT="SMS_SENT";
        String DELIVERED="SMS_DELIVERED";

        PendingIntent sentPI= PendingIntent.getBroadcast(this,0,
                new Intent(SENT),0);

        PendingIntent deliveredPI= PendingIntent.getBroadcast(this,0,
                new Intent(DELIVERED),0);

//---когда SMS отправлено---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1){
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), smsNumber+" SMS sent",
                                Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(),"Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(),"No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(),"Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(),"Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        String message = System.currentTimeMillis()+","+latitude+","+longitude+","+speed;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null, message, sentPI, deliveredPI);

      //  String message = String.valueOf(smsNumber);
      //  for (int i=0; i<M.size(); i++){
      //      message = message+M.get(i);
      //  }

        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        //         ContentValues values = new ContentValues();
        //         values.put("address", phoneNumber); // phone number to send
        //          values.put("date", System.currentTimeMillis()+"");
        //          values.put("read", "1"); // if you want to mark is as unread set to 0
        //          values.put("type", "2"); // 2 means sent message
        //          values.put("body", message);
        //          Uri uri = Uri.parse("content://sms/");
        //          Uri rowUri = getBaseContext().getContentResolver().insert(uri,values);

      /*  try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(),
                    "SMS sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS not sent", Toast.LENGTH_LONG).show();
        } */

        // inter SMS delay timer:
        new CountDownTimer(smssndint * 1000*60, smssndint * 1000*60) {
            @Override
            public void onTick(long l) {
                m = true;
            }
            @Override
            public void onFinish() {
                m = true;
            }
        }.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mapsactivity, menu);
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
        startActivity(new Intent(this,SettingsGeoSender.class));
        return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopbtn.performClick();
    }
}
