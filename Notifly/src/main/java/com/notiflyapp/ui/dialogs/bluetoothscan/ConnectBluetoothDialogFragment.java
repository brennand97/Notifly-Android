package com.notiflyapp.ui.dialogs.bluetoothscan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.notiflyapp.R;
import com.notiflyapp.services.bluetooth.scan.BluetoothScanService;
import com.notiflyapp.services.bluetooth.scan.BluetoothScanService.DeviceFoundCallback;

import java.util.ArrayList;

/**
 * Created by Brennan on 5/6/2016.
 */
public class ConnectBluetoothDialogFragment extends DialogFragment {
    
    private ConnectBluetoothAdapter connectBluetoothAdapter;
    private ConnectBluetoothOnItemClickListener connectBluetoothOnItemClickListener;
    private BluetoothScanService mService;
    private boolean mBound;
    private DeviceFoundCallback mDeviceFoundCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Intent intent = new Intent(getActivity(), BluetoothScanService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_title);

        View view = inflater.inflate(R.layout.dialog_fragment, null);
        ListView listView = (ListView) view.findViewById(R.id.dialog_list_view);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.dialog_scan_progress);
        TextView scanComplete = (TextView) view.findViewById(R.id.dialog_scan_complete);

        connectBluetoothAdapter = new ConnectBluetoothAdapter(getActivity(), new BluetoothDevice[0]);

        listView.setAdapter(connectBluetoothAdapter);

        connectBluetoothOnItemClickListener = new ConnectBluetoothOnItemClickListener(this);
        listView.setOnItemClickListener(connectBluetoothOnItemClickListener);

        builder.setView(view);

        mDeviceFoundCallback = new ConnectBluetoothDeviceFoundCallback(connectBluetoothAdapter, progressBar, scanComplete);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long total = 0;
                while (!mBound) {
                    try {
                        Thread.sleep(10);
                        total += 10;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(total % 100 == 0) {
                        Log.v(ConnectBluetoothDialogFragment.class.getSimpleName(), "BluetoothScanService not connected yet...");
                        Intent intent = new Intent(getActivity(), BluetoothScanService.class);
                        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }
                    if(total == 5000) {
                        Log.w(ConnectBluetoothDialogFragment.class.getSimpleName(), "Failed to connect to service to begin bluetooth discovery");
                        //TODO update dialog with a scan failed label
                        //for now a toast will suffice
                        Toast.makeText(getActivity(), "Bluetooth scan failed.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                mService.startScan(mDeviceFoundCallback);
            }
        };
        (new Thread(runnable)).start();

        return builder.create();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(mBound) {
            mService.stopScan();
            getActivity().unbindService(mConnection);
        }
    }

    public ArrayList<BluetoothDevice> getDeviceList() {
        return connectBluetoothAdapter.getDevices();
    }



    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothScanService.LocalBinder binder = (BluetoothScanService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
