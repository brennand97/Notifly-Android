package com.notiflyapp.data;

import java.io.*;

/**
 * Created by Brennan on 4/18/2016.
 *
 * Serialize Objects into byte arrays and then deserialize the byte arrays into objects.
 */
public class Serial {

    /**
     * Serialize the Object into a byte array
     *
     * @param obj Object to be serialized
     * @return byte[] containing serialized object
     * @throws IOException
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * Deserialize a byte[] into a Object
     *
     * @param data  byte[] to be dematerialized into an Object
     * @return Dematerialized Object from byte[]
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}
