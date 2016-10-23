/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.tasks;

import android.content.Context;

import com.notiflyapp.data.requestframework.Response;

/**
 * Created by Brennan on 10/5/2016.
 */

public class RetrievePreviousSms extends Thread {

    private static final String TAG = RetrievePreviousSms.class.getSimpleName();
    private Context context;
    private Response response;
    private long startTime;
    private int messageCount;

    public RetrievePreviousSms(Context context, Response response, long startTime, int messageCount) {
        this.context = context;
        this.response = response;
        this.startTime = startTime;
        this.messageCount = messageCount;
    }

    @Override
    public void run() {
        //TODO retrieve sms called for
    }

}
