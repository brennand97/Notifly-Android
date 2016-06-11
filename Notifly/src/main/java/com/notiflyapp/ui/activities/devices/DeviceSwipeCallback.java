package com.notiflyapp.ui.activities.devices;

import android.view.View;
import android.widget.Button;

import com.notiflyapp.R;

/**
 * Created by Brennan on 5/2/2016.
 */
class DeviceSwipeCallback implements DeviceSwipeTouchListener.SwipeCallback {

    private boolean lastCalledSwipeLeft = false;
    private boolean lastCalledSwipeRight = false;

    @Override
    public View getView(View view) {
        return view.findViewById(R.id.view_layout_top);
    }

    @Override
    public void onSwipe(View view) {

    }

    @Override
    public float getWidth(View view) {
        return view.findViewById(R.id.view_layout_bottom).getWidth();
    }

    @Override
    public void initializeBottomLayer(View view) {
        if(view != null) {
            Button connectButton = (Button) view.findViewById(R.id.btn_connect);
            Button smsButton = (Button) view.findViewById(R.id.btn_sms);
            Button notificationButton = (Button) view.findViewById(R.id.btn_notifications);
            connectButton.setEnabled(true);
            smsButton.setEnabled(true);
            notificationButton.setEnabled(true);
        }
    }

    @Override
    public void deinitializeBottomLayer(View view) {
        if(view != null) {
            Button connectButton = (Button) view.findViewById(R.id.btn_connect);
            Button smsButton = (Button) view.findViewById(R.id.btn_sms);
            Button notificationButton = (Button) view.findViewById(R.id.btn_notifications);
            connectButton.setEnabled(false);
            smsButton.setEnabled(false);
            notificationButton.setEnabled(false);
        }
    }

}
