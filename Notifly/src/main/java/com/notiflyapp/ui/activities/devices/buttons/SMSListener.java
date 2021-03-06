/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices.buttons;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.notiflyapp.data.DeviceInfo;

/**
 * Created by Brennan on 5/13/2016.
 */
public class SMSListener extends OptionButton {

    public SMSListener(Activity activity, DeviceInfo di) {
        super(activity, di);
    }

    @Override
    public void onClick(View v) {
        if(deviceInfo.getOptionSMS()) {
            //TODO update UI from connected to not
            deviceInfo.setOptionSMS(false);
        } else {
            //TODO update UI from not connected to connected
            deviceInfo.setOptionSMS(true);
        }
        Snackbar.make(v, deviceInfo.getDeviceName() + " SMS are " + (deviceInfo.getOptionSMS() ? "enabled" : "disabled"), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        //Toast.makeText(activity, deviceInfo.getDeviceName() + " SMS are " + (deviceInfo.getOptionSMS() ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        updateDevice();
    }
}
