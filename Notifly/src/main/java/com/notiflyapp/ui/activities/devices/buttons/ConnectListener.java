/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices.buttons;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.notiflyapp.R;
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
        CardView cardView = (CardView) v.getParent().getParent().getParent();

        if(deviceInfoOld.getOptionConnect()) {
            //TODO update UI from connected to not

            deviceInfo.setOptionConnect(false);
        } else {
            //TODO update UI from not connected to connected

            deviceInfo.setOptionConnect(true);
        }

        Snackbar.make(v, deviceInfo.getDeviceName() + " connection is " + (deviceInfo.getOptionConnect() ? "enabled" : "disabled"), Snackbar.LENGTH_SHORT).setAction("Action", null).show();

        updateDevice();
    }
}
