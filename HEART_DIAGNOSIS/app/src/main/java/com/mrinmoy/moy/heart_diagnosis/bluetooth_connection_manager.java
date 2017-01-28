package com.mrinmoy.moy.heart_diagnosis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Mrinmoy on 11/17/2015.
 */
public class bluetooth_connection_manager extends Thread {
    private final BluetoothAdapter bluetooth;
    private final BluetoothDevice device;
    private BluetoothSocket mmSocket=null;

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;
    public static final int MESSAGE_READ = 1;
    public static final int SUCCESS_CONNECT = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String mac;
    private static boolean connection_status;



    public bluetooth_connection_manager(BluetoothAdapter _bluetooth, Handler h,String _mac) {
        this.bluetooth = _bluetooth;
        mac = _mac;
        this.mHandler = h;
        device = this.bluetooth.getRemoteDevice(mac);
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            this.mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            this.mmSocket.connect();
            tmpIn = this.mmSocket.getInputStream();
            tmpOut = this.mmSocket.getOutputStream();
            connection_status = true;
        } catch (IOException e) {
            e.printStackTrace();
            connection_status = false;
        }

        this.mmInStream = tmpIn;
        this.mmOutStream = tmpOut;
    }
    public boolean init()
    {
        return connection_status;
    }
    public void run() {
        byte[] buffer;  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream

                if (mmInStream.available() > 0) {
                    int n = mmInStream.available();
                    buffer = new byte[n];
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    //Log.i("stupid",new String(buffer));
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
