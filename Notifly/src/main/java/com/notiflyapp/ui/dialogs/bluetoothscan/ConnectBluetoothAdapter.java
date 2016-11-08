/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.dialogs.bluetoothscan;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.notiflyapp.R;

import java.util.ArrayList;

/**
 * Created by Brennan on 5/8/2016.
 */
public class ConnectBluetoothAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    LayoutInflater inflater;

    public ConnectBluetoothAdapter(Context context, BluetoothDevice[] devices) {
        this.context = context;
        addAll(devices);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(BluetoothDevice[] array) {
        if(array != null) {
            for(BluetoothDevice bc: array) {
                devices.add(bc);
            }
        }
    }

    public void add(BluetoothDevice device) {
        devices.add(device);
    }

    public void add(int position, BluetoothDevice device) {
        devices.add(position, device);
    }

    public int indexOf(BluetoothDevice device) {
        return devices.indexOf(device);
    }

    public int indexOfMac(BluetoothDevice device) {
        String mac = device.getAddress();
        for(int i = 0; i < devices.size(); i++ ) {
            if(mac.equals(devices.get(i).getAddress())) {
                return i;
            }
        }
        return -1;
    }

    public BluetoothDevice get(int position) {
        return devices.get(position);
    }

    private BluetoothDevice[] getArray() {
        BluetoothDevice[] array = new BluetoothDevice[devices.size()];
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
            convertView = inflater.inflate(R.layout.dialog_list_view_row, parent, false);
        }

        BluetoothDevice device = devices.get(position);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.dialog_image_type);
        TextView nameView = (TextView) convertView.findViewById(R.id.dialog_text_name);
        TextView macView = (TextView) convertView.findViewById(R.id.dialog_text_mac);

        //TODO set imageView with appropriate icon

        String name = device.getName();
        if(name == null) {
            name = "Unknown";
        }
        String address = device.getAddress();

        imageView.setImageResource(R.mipmap.phone_red);
        nameView.setText(name);
        macView.setText(address);

        return convertView;

    }

    public boolean contains(BluetoothDevice device) {
        return devices.contains(device);
    }

    public ArrayList<BluetoothDevice> getDevices() {
        return devices;
    }
}

