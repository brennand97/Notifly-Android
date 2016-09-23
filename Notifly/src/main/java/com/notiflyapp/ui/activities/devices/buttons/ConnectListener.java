/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices.buttons;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.notiflyapp.data.DeviceInfo;

/**
 * Created by Brennan on 5/13/2016.
 */
public class ConnectListener extends OptionButton  {

    public ConnectListener(Activity activity, DeviceInfo di) {
        super(activity, di);
    }

    @Override
    public void onClick(View v) {
        if(deviceInfoOld.getOptionConnect()) {
            //TODO update UI from connected to not
            deviceInfo.setOptionConnect(false);
        } else {
            //TODO update UI from not connected to connected
            deviceInfo.setOptionConnect(true);
        }
        Toast.makeText(activity, deviceInfo.getDeviceName() + " connection is " + (deviceInfo.getOptionConnect() ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        updateDevice();
    }
}
