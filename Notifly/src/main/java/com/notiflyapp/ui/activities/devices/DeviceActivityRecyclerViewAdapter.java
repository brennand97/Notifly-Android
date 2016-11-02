/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.notiflyapp.R;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.ui.activities.devices.buttons.ConnectListener;
import com.notiflyapp.ui.activities.devices.buttons.SMSListener;

import java.util.ArrayList;

/**
 * Created by Brennan on 9/26/2016.
 */

public class DeviceActivityRecyclerViewAdapter extends RecyclerView.Adapter<DeviceActivityRecyclerViewAdapter.DeviceInfoViewHolder>{

    public static class DeviceInfoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textName, textMac;
        ImageView imageType;
        Button connectBtn, smsBtn;

        DeviceInfoViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.cv);
            textName = (TextView) view.findViewById(R.id.text_name);
            textMac = (TextView) view.findViewById(R.id.text_mac);
            imageType = (ImageView) view.findViewById(R.id.image_type);
            connectBtn = (Button) view.findViewById(R.id.btn_connect);
            smsBtn = (Button) view.findViewById(R.id.btn_sms);
        }
    }

    private ArrayList<DeviceInfo> devices;
    private Activity activity;
    private DeviceInfo currentConnected = null;

    public DeviceActivityRecyclerViewAdapter(Activity activity, ArrayList<DeviceInfo> deviceInfoArrayList) {
        this.activity = activity;
        this.devices = deviceInfoArrayList;
    }

    @Override
    public DeviceInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card, parent, false);
        return new DeviceInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceInfoViewHolder holder, int position) {
        DeviceInfo deviceInfo = devices.get(position);
        holder.textName.setText(deviceInfo.getDeviceName());
        holder.textMac.setText(deviceInfo.getDeviceMac());
        holder.imageType.setImageResource(R.mipmap.phone_red);

        holder.connectBtn.setOnClickListener(new ConnectListener(activity, deviceInfo));
        holder.smsBtn.setOnClickListener(new SMSListener(activity, deviceInfo));

        if(currentConnected != null && deviceInfo.getDeviceMac().equals(currentConnected.getDeviceMac())) {
            holder.cardView.setBackgroundColor(activity.getResources().getColor(R.color.cardViewConnected));
        } else {
            holder.cardView.setBackgroundColor(activity.getResources().getColor(R.color.cardView));
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addAll(DeviceInfo[] array) {
        for(DeviceInfo bc: array) {
            devices.add(bc);
        }
        this.notifyDataSetChanged();
    }

    public void add(DeviceInfo client) {
        devices.add(client);
        this.notifyDataSetChanged();
    }

    public void add(int position, DeviceInfo device) {
        devices.add(position, device);
        this.notifyDataSetChanged();
    }

    public void update(int position, DeviceInfo device) {
        devices.remove(position);
        devices.add(position, device);
        this.notifyDataSetChanged();
    }

    public void remove(int position) {
        devices.remove(position);
        this.notifyDataSetChanged();
    }

    public void clear() {
        devices.clear();
        this.notifyDataSetChanged();
    }

    private DeviceInfo[] getArray() {
        DeviceInfo[] array = new DeviceInfo[devices.size()];
        for(int i = 0; i < devices.size(); i++) {
            array[i] = devices.get(i);
        }
        return array;
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

    public DeviceInfo getCurrentConnected() {
        return currentConnected;
    }

    public void setCurrentConnected(DeviceInfo currentConnected) {
        this.currentConnected = currentConnected;
    }

}
