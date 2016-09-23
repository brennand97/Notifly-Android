/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.services.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Brennan on 5/21/2016.
 */
public class DeliveredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Delivered
        Log.i("Debug-DeliveredReceiver", "Message delivered");
    }
}
