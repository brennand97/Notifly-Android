package com.notiflyapp.ui.activities.devices.buttons;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.notiflyapp.data.DeviceInfo;

/**
 * Created by Brennan on 5/13/2016.
 */
public class NotificationsListener extends OptionButton {

    public NotificationsListener(Activity activity, DeviceInfo di) {
        super(activity, di);
    }

    @Override
    public void onClick(View v) {
        if(deviceInfo.getOptionNotification()) {
            //TODO update UI from connected to not
            deviceInfo.setOptionNotification(false);
        } else {
            //TODO update UI from not connected to connected
            deviceInfo.setOptionNotification(true);
        }
        Toast.makeText(activity, deviceInfo.getDeviceName() + " notifications are " + (deviceInfo.getOptionNotification() ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        updateDevice();
    }
}
