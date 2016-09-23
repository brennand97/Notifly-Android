/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

/**
 * Created by Brennan on 6/1/2016.
 */
public class DeviceNotFoundException extends Exception {
    public DeviceNotFoundException(String s) { super(s); }
    public DeviceNotFoundException(Exception e) { super(e); }
    public static synchronized DeviceNotFoundException makeException(String table, String type, String id) {
        return new DeviceNotFoundException("No device found in table: " + table + "; matching " + type + ": " + id);
    }
}
