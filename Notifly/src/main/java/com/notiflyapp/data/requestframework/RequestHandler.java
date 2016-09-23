/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data.requestframework;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.SMS;
import com.notiflyapp.data.Serial;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.tasks.ReceiveContactByThreadId;
import com.notiflyapp.services.bluetooth.connection.BluetoothClient;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    private final static String TAG = RequestHandler.class.getSimpleName();

    public final static class RequestCode {

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";
            public final static String EXTRA_CONTACT_BY_THREAD_ID_THREAD = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD";

        public final static String SEND_SMS = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.SEND_SMS";
            public final static String EXTRA_SEND_SMS_SMSOBJECT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT";
            public final static String CONFIRMATION_SEND_SMS_SENT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT";
            public final static String CONFIRMATION_SEND_SMS_FAILED = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED";

    }

    private HashMap<String, Request> requestHashMap = new HashMap<>();              //String is the UUID of the request in string form and the Request object is the request itself
    private HashMap<String, BluetoothClient> clientHashMap = new HashMap<>();       //String is the UUID of the request in string form and the BluetoothClient is the client related to the request
    private HashMap<String, ResponseCallback> callbackHashMap = new HashMap<>();    //String is the UUID of the request in string form and the ResponseCallback is the callback assigned with the request

    private static RequestHandler handler;
    private Context context;

    private RequestHandler(Context context) {
        this.context = context;
    }

    public interface ResponseCallback {
        void responseReceived(Request request, Response response);
    }

    public static RequestHandler getInstance(Context context) {
        if(handler == null) {
            handler = new RequestHandler(context);
        } else {
            handler.context = context;
        }
        return handler;
    }

    public void handleRequest(BluetoothClient client, Request request) {
        if(client != null) {
            Log.v(TAG, "Received request : " + request.getExtra().toString() + " from : " + client.getDeviceMac() + " for : " + request.getBody());
        } else {
            Log.v(TAG, "Received request : " + request.getExtra().toString() + " for : " + request.getBody());
        }
        clientHashMap.put(request.getExtra().toString(), client);
        Response response = Response.makeResponse(request);
        switch (request.getBody()) {
            case RequestCode.CONTACT_BY_THREAD_ID:
                ReceiveContactByThreadId task = new ReceiveContactByThreadId(context, response);
                task.start();
                break;
            case RequestCode.SEND_SMS:
                try {
                    Log.v(TAG, "Received send sms request");
                    Intent smsIntent = new Intent(context, SmsService.class);
                    smsIntent.setAction(SmsService.ACTION_SEND_SMS);
                    smsIntent.putExtra(SmsService.EXTRA_REQUEST, Serial.serialize(request));
                    context.startService(smsIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                //TODO handle if the given key does not match any of the defined request codes
                break;
        }
    }

    public void handleResponse(Response response) {
        if(callbackHashMap.containsKey(response.getExtra().toString()) && requestHashMap.containsKey(response.getExtra().toString())) {
            String uuid = response.getExtra().toString();
            callbackHashMap.get(uuid).responseReceived(requestHashMap.get(uuid), response);
            callbackHashMap.remove(response.getExtra().toString());
            requestHashMap.remove(response.getExtra().toString());
        } else {
            //The response doesn't have a matching request so for now just drop
        }
    }

    //TODO if the server framework is ever completely generalized on all OSes then change this function the the generalized client object
    public void sendRequest(BluetoothClient client, Request request, ResponseCallback callback) {
        requestHashMap.put(request.getExtra().toString(), request);
        callbackHashMap.put(request.getExtra().toString(), callback);
        if(client != null) {
            client.sendMsg(request);
        } else {
            this.handleRequest(null, request);
        }
    }

    public void sendResponse(Response response) {
        String uuid = response.getExtra().toString();
        if(clientHashMap.containsKey(uuid)) {
            BluetoothClient client = clientHashMap.get(uuid);
            if(client != null) {
                client.sendMsg(response);
            }  else {
                this.handleResponse(response);
            }
            Log.v(TAG, "Response : " + response.getExtra().toString() + " sent");
            clientHashMap.remove(uuid);
        }
    }

}
