package com.adamoff.andrej.tracker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import java.util.ArrayList;


public class GeoSender extends AppCompatActivity {

    TextView txtloc, txtsat;
    EditText etphone, etperiod, sendperiod;
    String phone, period, sndperiod;

    boolean n,m;
    int smsNumber;

    LocationManager lm;
    LocationListener listener;
    GpsStatus.NmeaListener nmealistener;

  //  ArrayList<String> M;

    FileOutputStream fos;
 //   BufferedOutputStream bos; // = new BufferedOutputStream(fos);
    OutputStreamWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geosender);

        //   phone = "+381612751056";

        lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        //
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        // listerner is required to be registered, because GPS receiver doesn't start without it:
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, listener);

        nmealistener = new GpsStatus.NmeaListener() {

            @Override
            public void onNmeaReceived(long l, String s) {
                //  final String sentence = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
                System.out.println("nmea: " + s);
                if (n) {  // if timer finished. If not then exit
                    if (s.startsWith("$GPRMC")) {
                        n = false;
                    // save to log:
                     try {
                         out.write(s);
                     }
                        catch (Exception e){
                            e.printStackTrace();
                        }
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

                    // displaying coordinates on screen:
                        String[] strValues = s.split(",");
                        double latitude = Double.parseDouble(strValues[3]) * .01;
                        if (strValues[4].charAt(0) == 'S') {
                            latitude = -latitude;
                        }
                        double longitude = Double.parseDouble(strValues[5]) * .01;
                        if (strValues[6].charAt(0) == 'W') {
                            longitude = -longitude;
                        }
                        double course = Double.parseDouble(strValues[8]);
                        txtloc.setText("Lat: " + latitude + "\n" + "Long: " + longitude + "\n" + "course: " + course);
                        //        System.out.println("latitude="+latitude+" ; longitude="+longitude+" ; course = "+course);

                    // check whether have to send SMS:
                        if (m){  // have to send SMS
                            m=false;
                            // start new timer:
                            new CountDownTimer(Integer.parseInt(sndperiod) * 1000*60, Integer.parseInt(sndperiod) * 1000*60) {
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
                            sendSMS(phone, s);   // send log by SMS

                        }

                    } else { // if not GPRMC

                        //   txt2.setText(s);
                    }
            }
            }
        };

        txtloc = (TextView)findViewById(R.id.txtloc);
        txtsat = (TextView)findViewById(R.id.txtsat);
        etphone = (EditText)findViewById(R.id.etphone);
        etperiod = (EditText)findViewById(R.id.etperiod);
        sendperiod = (EditText)findViewById(R.id.etsendperiod);
        Button startwithlog = (Button)findViewById(R.id.startwithlog);
        Button startnolog = (Button) findViewById(R.id.startnolog);
        Button stopbtn = (Button)findViewById(R.id.stopbtn);

        startwithlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

    //            M = new ArrayList<String>();

                phone = etphone.getText().toString();
                period = etperiod.getText().toString();
                sndperiod = sendperiod.getText().toString();
                // listener registration
                //          try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, listener);}
                //          catch (SecurityException s) {s.printStackTrace();}
                // listener registration
             //   try  lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, listener);

                // open log file
              try {  File logfile = new File(Environment.getExternalStorageDirectory(),"GeoLog.txt");
                       fos = new FileOutputStream(logfile);
               //      bos = new BufferedOutputStream(fos);
                     out = new OutputStreamWriter(fos);}
              catch (Exception e){
                     e.printStackTrace();
              }

                //
                  lm.addNmeaListener(nmealistener); // nmealistener starts

                  n= true; // allow to start timer
                  m = true; // allow to start timer
                  smsNumber = 0;
              //  catch (SecurityException s) {s.printStackTrace();}

            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           //     try {lm.removeUpdates(listener);
                    lm.removeNmeaListener(nmealistener);

                //save log in a file:
              //     try {saveFile(M, "GeoLog.txt");}
              //     catch (Exception e){e.printStackTrace();}

                // close and save log file
                try {
                    out.close();
                  //  bos.flush();
                    fos.close();
                } catch (Exception e) {e.printStackTrace();}



            }
          //      catch (SecurityException s) {
          //          s.printStackTrace();}
          //  }
        });

    //    List<String> providers = lm.getAllProviders();
    //    for (String prov : providers ) {
    //        txt1.setText(prov+"\n");}



    }

    private void addtoLog (String s){



    }

    private void sendLog(String[] s){


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



    public void sendSMS (String phoneNumber, String message ){

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

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_geosender, menu);
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
