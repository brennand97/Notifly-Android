/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data.requestframework;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.notiflyapp.data.DataString;
import com.notiflyapp.data.Serial;
import com.notiflyapp.services.sms.SmsService;
import com.notiflyapp.tasks.RetrieveContactByThreadId;
import com.notiflyapp.services.bluetooth.connection.BluetoothClient;
import com.notiflyapp.tasks.RetrieveContactPicture;
import com.notiflyapp.tasks.RetrieveSms;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    private final static String TAG = RequestHandler.class.getSimpleName();

    public final static class RequestCode {

        public final static String EXTRA_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA__THREAD_ID";
        public final static String EXTRA_CONTACT_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_ID";

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";
            public final static String EXTRA_CONTACT_BY_THREAD_ID_THREAD = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD";

        public final static String SEND_SMS = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.SEND_SMS";
            public final static String EXTRA_SEND_SMS_SMSOBJECT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT";
            public final static String CONFIRMATION_SEND_SMS_SENT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT";
            public final static String CONFIRMATION_SEND_SMS_FAILED = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED";

        public final static String RETRIEVE_SMS = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.RETRIEVE_SMS";
            public final static String EXTRA_RETRIEVE_SMS_START_TIME = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_RETRIEVE_SMS_START_TIME";
            public final static String EXTRA_RETRIEVE_SMS_MESSAGE_COUNT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_RETRIEVE_SMS_MESSAGE_COUNT";
            public final static String EXTRA_RETRIEVE_SMS_OLD = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_RETRIEVE_SMS_OLD";
            public final static String EXTRA_RETRIEVE_SMS_NEW = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_RETRIEVE_SMS_NEW";
            //EXTRA_THREAD_ID

        public final static String PUSH_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.PUSH_THREAD_ID";
            //EXTRA_THREAD_ID

        public final static String RETRIEVE_CONTACT_PICTURE = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.RETRIEVE_CONTACT_PICTURE";
            public final static String EXTRA_PICTURE_BYTE_ARRAY = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_PICTURE_BYTE_ARRAY";
            //EXTRA_CONTACT_ID
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
        String threadId;

        if(client != null) {
            Log.v(TAG, "Received request : " + request.getExtra().toString() + " from : " + client.getDeviceMac() + " for : " + request.getBody());
        } else {
            Log.v(TAG, "Received request : " + request.getExtra().toString() + " for : " + request.getBody());
        }
        clientHashMap.put(request.getExtra().toString(), client);
        Response response = Response.makeResponse(request);
        switch (request.getBody()) {
            case RequestCode.CONTACT_BY_THREAD_ID:
                threadId = ((DataString) request.getItem(RequestCode.EXTRA_THREAD_ID)).getBody();
                RetrieveContactByThreadId task = new RetrieveContactByThreadId(context, response, threadId);
                task.start();
                break;
            case RequestCode.RETRIEVE_CONTACT_PICTURE:
                long contactId = Long.parseLong(((DataString) request.getItem(RequestCode.EXTRA_CONTACT_ID)).getBody());
                RetrieveContactPicture retrieveContactPicture = new RetrieveContactPicture(context, response, contactId);
                retrieveContactPicture.start();
                break;
            case RequestCode.SEND_SMS:
                try {
                    Log.v(TAG, "Received SMS to send");
                    Intent smsIntent = new Intent(context, SmsService.class);
                    smsIntent.setAction(SmsService.ACTION_SEND_SMS);
                    smsIntent.putExtra(SmsService.EXTRA_REQUEST, Serial.serialize(request));
                    context.startService(smsIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case RequestCode.RETRIEVE_SMS:
                String startTime = ((DataString) request.getItem(RequestCode.EXTRA_RETRIEVE_SMS_START_TIME)).getBody();
                String messageCount = ((DataString) request.getItem(RequestCode.EXTRA_RETRIEVE_SMS_MESSAGE_COUNT)).getBody();
                threadId = ((DataString) request.getItem(RequestCode.EXTRA_THREAD_ID)).getBody();
                String inequality;
                if(request.getHashMap().containsKey(RequestCode.EXTRA_RETRIEVE_SMS_OLD)) {
                    inequality = "<";
                } else if(request.getHashMap().containsKey(RequestCode.EXTRA_RETRIEVE_SMS_NEW)) {
                    inequality = ">";
                } else {
                    inequality = "=";
                }
                if(startTime == null || messageCount == null || threadId == null) {
                    RequestHandler.getInstance(context).sendResponse(response);
                }
                try {
                    long startTimeLong = Long.parseLong(startTime);
                    int messageCountInt = Integer.parseInt(messageCount);
                    RetrieveSms retrievePreviousSmsTask = new RetrieveSms(context, response, startTimeLong, messageCountInt, threadId, inequality);
                    retrievePreviousSmsTask.start();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    RequestHandler.getInstance(context).sendResponse(response);
                }
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
