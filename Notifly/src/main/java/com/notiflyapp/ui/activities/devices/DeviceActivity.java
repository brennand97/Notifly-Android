/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices;

import android.Manifest;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.notiflyapp.R;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.DeviceDatabase;
import com.notiflyapp.database.DeviceNotFoundException;
import com.notiflyapp.database.NullCursorException;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.ui.dialogs.suredialog.SureDialogFragment;
import com.notiflyapp.ui.dialogs.bluetoothscan.ConnectBluetoothDialogFragment;
import com.notiflyapp.services.bluetooth.connection.BluetoothService;
import com.notiflyapp.data.DeviceInfo;

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    RecyclerView recyclerView;
    DeviceActivityRecyclerViewAdapter listAdapter;
    FloatingActionButton floatingActionButton;
    DeviceDatabase deviceDatabase;

    boolean smsActive = false;
    boolean bluetoothActive = false;

    private BroadcastReceiver bluetoothMessageReceiver;

    public static final int ADD_DEVICE = 0;
    public final Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_DEVICE:
                    addDevice(msg.arg1);
                    break;
            }
        }
    };

    public final static int REQUEST_COARSE_LOCATION_PERMISSIONS = 0;
    public final static int REQUEST_CODE_PHONE_PERMISSIONS = 1;
    private final static int REQUEST_CODE_BLUETOOTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int devicePosition;
                switch (intent.getAction()) {
                    case BluetoothService.DEVICE_CONNECTED:
                        devicePosition = intent.getIntExtra(BluetoothService.DEVICE_DATABASE_POSITION, -1);
                        try {
                            DeviceInfo deviceInfo = deviceDatabase.getDeviceInfo(devicePosition);
                            Toast.makeText(getApplicationContext(), deviceInfo.getDeviceName() + " connected.", Toast.LENGTH_SHORT).show();
                            listAdapter.setCurrentConnected(deviceInfo);
                            listAdapter.setCurrentConnecting(null);
                            listAdapter.notifyDataSetChanged();
                        } catch (DeviceNotFoundException | NullCursorException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BluetoothService.DEVICE_CONNECTING:
                        devicePosition = intent.getIntExtra(BluetoothService.DEVICE_DATABASE_POSITION, -1);
                        try {
                            DeviceInfo deviceInfo = deviceDatabase.getDeviceInfo(devicePosition);
                            listAdapter.setCurrentConnecting(deviceInfo);
                            listAdapter.notifyDataSetChanged();
                        } catch (DeviceNotFoundException | NullCursorException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BluetoothService.DEVICE_CONNECT_FAILED:
                        devicePosition = intent.getIntExtra(BluetoothService.DEVICE_DATABASE_POSITION, -1);
                        try {
                            if(devicePosition >= 0) {
                                DeviceInfo deviceInfo = deviceDatabase.getDeviceInfo(devicePosition);
                                Toast.makeText(getApplicationContext(), deviceInfo.getDeviceName() + " failed.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Connection failed.", Toast.LENGTH_SHORT).show();
                            }
                            listAdapter.setCurrentConnected(null);
                            listAdapter.setCurrentConnecting(null);
                            listAdapter.notifyDataSetChanged();
                        } catch (DeviceNotFoundException | NullCursorException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BluetoothService.DEVICE_DISCONNECTED:
                        devicePosition = intent.getIntExtra(BluetoothService.DEVICE_DATABASE_POSITION, -1);
                        try {
                            if(devicePosition >= 0) {
                                DeviceInfo deviceInfo = deviceDatabase.getDeviceInfo(devicePosition);
                                Toast.makeText(getApplicationContext(), deviceInfo.getDeviceName() + " disconnected.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Connection disconnected.", Toast.LENGTH_SHORT).show();
                            }
                            listAdapter.setCurrentConnected(null);
                            listAdapter.setCurrentConnecting(null);
                            listAdapter.notifyDataSetChanged();
                        } catch (DeviceNotFoundException | NullCursorException e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }
        };

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_BLUETOOTH);
            } else {
                startBluetoothService();
            }
        }

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                if(bta != null) {
                    if(bta.isEnabled()) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            int coarsePermission = ActivityCompat.checkSelfPermission(DeviceActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                            if (coarsePermission == PackageManager.PERMISSION_GRANTED) {
                                bluetoothDeviceSelect();
                                return;
                            }
                            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{ android.Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_COARSE_LOCATION_PERMISSIONS);
                        } else {
                            bluetoothDeviceSelect();
                        }
                    } else {
                        Toast.makeText(DeviceActivity.this, R.string.fab_click_no_bluetooth, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        deviceDatabase = DatabaseFactory.getDeviceDatabase(this);

        recyclerView = (RecyclerView) findViewById(R.id.device_activity_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        listAdapter = new DeviceActivityRecyclerViewAdapter(this, new ArrayList<DeviceInfo>());
        recyclerView.setAdapter(listAdapter);

        //DeviceSwipeTouchListener deviceSwipeTouchListener = new DeviceSwipeTouchListener(recyclerView, new DeviceSwipeCallback());

        //recyclerView.setOnTouchListener(deviceSwipeTouchListener);
        //recyclerView.setOnScrollListener(deviceSwipeTouchListener.makeScrollListener());

        startSmsService();

        try {
            for(int i = 0; i < deviceDatabase.getCount(); i++) {
                addDevice(i + 1);
            }
        } catch (DeviceNotFoundException e) {
            //No need to add any devices
        } catch (NullCursorException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.clear_list:
                DialogFragment dialogFragment = new SureDialogFragment();
                Bundle args = new Bundle();
                args.putString(SureDialogFragment.MESSAGE, getString(R.string.clear_device_sure));
                SureDialogFragment.SureCallback callback = new SureDialogFragment.SureCallback() {
                    @Override
                    public void onPositive() {
                        deviceDatabase.clearDatabase();
                        Intent intent = new Intent(DeviceActivity.this, BluetoothService.class);
                        intent.setAction(BluetoothService.DISCONNECT_DEVICE);
                        startService(intent);
                        listAdapter.clear();
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegative() {
                        //Do nothing
                    }
                };
                args.putSerializable(SureDialogFragment.CALLBACK, callback);
                dialogFragment.setArguments(args);
                dialogFragment.show(getFragmentManager(), "Are you sure");
                return true;
            case R.id.service_toggle:
                if(bluetoothActive) {
                    stopBluetoothService();
                    item.setTitle(R.string.options_menu_toggle_service_start);
                } else {
                    startBluetoothService();
                    item.setTitle(R.string.options_menu_toggle_service_stop);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((bluetoothMessageReceiver),
                new IntentFilter(BluetoothService.DEVICE_CONNECTED)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((bluetoothMessageReceiver),
                new IntentFilter(BluetoothService.DEVICE_CONNECTING)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((bluetoothMessageReceiver),
                new IntentFilter(BluetoothService.DEVICE_CONNECT_FAILED)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((bluetoothMessageReceiver),
                new IntentFilter(BluetoothService.DEVICE_DISCONNECTED)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothMessageReceiver);
    }

    private void startSmsService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] stringList = new String[] {
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.RECEIVE_MMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS
            };
            int[] permissions = new int[stringList.length];
            for (int i = 0; i < stringList.length; i++) {
                permissions[i] = ActivityCompat.checkSelfPermission(DeviceActivity.this, stringList[i]);
            }
            int total = 0;
            for (int i = 0; i < permissions.length; i++) {
                total += permissions[i];
            }
            if (total == (PackageManager.PERMISSION_GRANTED * stringList.length)) {
                continueStartSmsService();
                return;
            }

            ActivityCompat.requestPermissions(DeviceActivity.this, stringList, REQUEST_CODE_PHONE_PERMISSIONS);
        } else {
            continueStartSmsService();
        }
    }

    private void continueStartSmsService() {
        Intent intent = new Intent(this, SmsService.class);
        intent.setAction(SmsService.READ_PHONE_STATE_PERMISSION_GRANTED);
        startService(intent);
        smsActive = true;
    }

    private void stopSmsService() {
        Intent intent = new Intent(this, SmsService.class);
        stopService(intent);
        smsActive = false;
    }

    private void bluetoothDeviceSelect() {

        DialogFragment dialogFragment = new ConnectBluetoothDialogFragment();
        dialogFragment.show(getFragmentManager(), "Bluetooth Device Select");

    }

    private void startBluetoothService() {
        Intent bsIntent = new Intent(this, BluetoothService.class);
        bsIntent.setAction(BluetoothService.CONNECT_LAST_DEVICE);
        startService(bsIntent);
        bluetoothActive = true;
    }

    private void stopBluetoothService() {
        Intent bsIntent = new Intent(this, BluetoothService.class);
        stopService(bsIntent);
        bluetoothActive = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_BLUETOOTH && resultCode == RESULT_OK) {
            Intent startDevices = new Intent(this, BluetoothService.class);
            startDevices.setAction(BluetoothService.CONNECT_LAST_DEVICE);
            startService(startDevices);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if ((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    bluetoothDeviceSelect();
                } else {
                    Toast.makeText(this, R.string.permission_failure, Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_CODE_PHONE_PERMISSIONS: {
                boolean allAccepted = false;
                for(int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }
                    if((i + 1) == grantResults.length) {
                        allAccepted = true;
                    }
                }
                if(allAccepted) {
                    continueStartSmsService();
                } else {
                    Toast.makeText(this, "Cannot continue without SMS + Contact permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void addDevice(int dbPosition) {
        DeviceInfo device = null;
        try {
            device = deviceDatabase.getDeviceInfo(dbPosition);
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
            Log.w(TAG, "Device, id: " + dbPosition + ", not found in device database");
        } catch (NullCursorException e) {
            e.printStackTrace();
            return;
        }
        if(!listAdapter.containsByMac(device)) {
            listAdapter.add(device);
        } else {
            int position = listAdapter.indexByMac(device);
            listAdapter.remove(position);
            listAdapter.add(position, device);
            listAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(position);
        }
    }

}
