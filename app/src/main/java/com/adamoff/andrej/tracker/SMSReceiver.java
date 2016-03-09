package com.adamoff.andrej.tracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    private Intent mIntent;
    String action;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

  //      mIntent = intent;

        if (intent != null) {
    //        action = intent.getAction();

       //     if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            String address ="000", str = "";

            String phone =  PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                                               .getString("sendingphone", "111");


                android.telephony.SmsMessage[] msgs = getMessagesFromIntent(intent);
                if (msgs != null) {
                    for (int i = 0; i < msgs.length; i++) {
                        address = msgs[i].getDisplayOriginatingAddress();
                    }
                }

                 if (address.equals(phone)) {
                     for (int i = 0; i < msgs.length; i++) {
                         str += msgs[i].getMessageBody();
                         //     str += "\n";
                     }

         //            Toast.makeText(context, address, Toast.LENGTH_LONG).show();
         //            Toast.makeText(context, str, Toast.LENGTH_LONG).show();

                     Intent intent2 = new Intent(context, MyActivity.class);
                     intent2.putExtra("sms", str);
                     context.startActivity(intent2.addFlags(intent2.FLAG_ACTIVITY_NEW_TASK));

                 //    context.startActivity(new Intent(context, MapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("sms", str));
                  }

                     // запуск сервиса, который запускает диалоговое окно
                //     Intent sintent = new Intent(context, HuntingCameraService.class);
                //     sintent.putExtra("phone", address);
                //     sintent.putExtra("type", "SMS");
                     //
                //     context.startService(sintent);

         //          };
            }

        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    public android.telephony.SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        android.telephony.SmsMessage[] msgs = new android.telephony.SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = android.telephony.SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}


