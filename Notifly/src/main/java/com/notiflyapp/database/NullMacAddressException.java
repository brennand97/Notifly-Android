/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

/**
 * Created by Brennan on 6/1/2016.
 */
public class NullMacAddressException extends Exception {
    public NullMacAddressException(String s) { super(s); }
    public NullMacAddressException(Exception e) { super(e); }
    public static synchronized NullMacAddressException makeException() {
        return new NullMacAddressException("DeviceInfo contained a null mac address.");
    }
}
