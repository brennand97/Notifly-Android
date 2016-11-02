/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.notiflyapp.R;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.ui.activities.devices.buttons.ConnectListener;
import com.notiflyapp.ui.activities.devices.buttons.NotificationsListener;
import com.notiflyapp.ui.activities.devices.buttons.SMSListener;

import java.util.ArrayList;

/**
 * Created by Brennan on 4/27/2016.
 */
class DeviceActivityListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<DeviceInfo> devices = new ArrayList<>();
    LayoutInflater inflater;

    public DeviceActivityListAdapter(Activity activity, DeviceInfo[] devices) {
        this.activity = activity;
        addAll(devices);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(DeviceInfo[] array) {
        for(DeviceInfo bc: array) {
            devices.add(bc);
        }
    }

    public void add(DeviceInfo client) {
        devices.add(client);
    }

    private DeviceInfo[] getArray() {
        DeviceInfo[] array = new DeviceInfo[devices.size()];
        for(int i = 0; i < devices.size(); i++) {
            array[i] = devices.get(i);
        }
        return array;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.list_view_row, parent, false);
        }

        ImageView imageType = (ImageView) convertView.findViewById(R.id.image_type);
        TextView textName = (TextView) convertView.findViewById(R.id.text_name);
        TextView textMac = (TextView) convertView.findViewById(R.id.text_mac);
        ImageView imageNetwork = (ImageView) convertView.findViewById(R.id.image_network);
        ImageView imageBluetooth = (ImageView) convertView.findViewById(R.id.image_bluetooth);
        Button connectButton = (Button) convertView.findViewById(R.id.btn_connect);
        Button smsButton = (Button) convertView.findViewById(R.id.btn_sms);
        Button notificationButton = (Button) convertView.findViewById(R.id.btn_notifications);

        DeviceInfo device = devices.get(position);

        imageType.setImageResource(R.mipmap.phone_red);

        imageNetwork.setImageResource(R.mipmap.network_blue);
        imageBluetooth.setImageResource(R.mipmap.check_green);

        textName.setText(device.getDeviceName());
        textMac.setText(device.getDeviceMac());

        connectButton.setOnClickListener(new ConnectListener(activity, device));
        smsButton.setOnClickListener(new SMSListener(activity, device));
        notificationButton.setOnClickListener(new NotificationsListener(activity, device));

        return convertView;

    }

    public int indexByMac(DeviceInfo device) {
        for(DeviceInfo di: devices) {
            if(device.getDeviceMac().equals(di.getDeviceMac())) {
                return devices.indexOf(di);
            }
        }
        return -1;
    }

    public boolean containsByMac(DeviceInfo device) {
        for(DeviceInfo di: devices) {
            if(device.getDeviceMac().equals(di.getDeviceMac())) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        devices.clear();
    }

    public void update(int position, DeviceInfo di) {
        devices.add(position, di);
    }

    public void remove(int position) {
        devices.remove(position);
    }

    public void add(int position, DeviceInfo device) {
        devices.add(position, device);
    }
}
