package com.notiflyapp.database;

/**
 * Created by Brennan on 6/1/2016.
 */
public class NullCursorException extends Exception {
    public NullCursorException(String s) { super(s); }
    public NullCursorException(Exception e) { super(e); }
    public static synchronized NullCursorException makeException(String table, String[] columns) {
        StringBuilder builder = new StringBuilder("Null Cursor received from call to table: ");
        for(int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            if(i != columns.length - 1) {
                builder.append(", ");
            }
        }
        return new NullCursorException(builder.toString());
    }
    public static synchronized NullCursorException makeException(String table, String[] columns, String[] where, String[] value) {
        if((columns == null) || (where.length != value.length)) {
            return new NullCursorException("Null Cursor received.");
        }
        StringBuilder builder = new StringBuilder("Null Cursor received from call to table: ");
        builder.append(table);
        builder.append("; With columns: ");
        for(int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            if(i != columns.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("; With values: ");
        for(int i = 0; i < where.length; i++) {
            builder.append(where[i]);
            builder.append(" = ");
            builder.append(value[i]);
            if(i != columns.length - 1) {
                builder.append(", ");
            }
        }
        return new NullCursorException(builder.toString());
    }
}
