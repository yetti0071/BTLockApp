package com.example.vuher.btlockapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by kshabashov on 5/6/2015.
 */
public class AbstractTalkativeActivity extends ActionBarActivity {
    protected BluetoothChatService mChatService = null;
    protected BluetoothAdapter mBluetoothAdapter;
    protected final int REQUEST_ENABLE_BT = 6;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Name of the connected device
    private String mConnectedDeviceName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpBluetooth();
        //  registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));  // this is not working for connected devices
    }

    private void setUpBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new RuntimeException("AAAAAA");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            mChatService = new BluetoothChatService(getWindow().getContext(), mHandler);
        }

    }

    /**
     * broadcasts a message.
     * @param message  A string of text to send.
     */
    protected void sendMessages(String message) {
        Log.i("info", "broadcasting unlock command");

        ArrayList<String> pairedDevicesToList = new ArrayList<>();
        String output = "";
        try{
            TextView tv = (TextView)findViewById(R.id.listOfDevices);
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    pairedDevicesToList.add(device.getName() + "   "+ device.getAddress());
                }
            }
            if(pairedDevicesToList.size() != 0){
                for(BluetoothDevice device : pairedDevices) {
                    //output += device.getName() + "   "+ device.getAddress()+  "   "+ device.getBondState()+" "+checkDeviceAvailability(device)+"\n";
                    //tv.setText(output);
                    sendMessageToDevice(message, device, true);
                    Thread.sleep(3000);
                }
            }else {
                tv.setText("This Will be a list of devices");
            }
        }catch(Exception Ex){
            Log.w("chyba", Ex);
        }

        // iterate all paired devices and try to send a message

    }


    protected void sendMessageToDevice(String message, BluetoothDevice device, boolean secure) {

        connectDevice(device, secure); // sending is done in Handler so we do not need to sleep untill  the connection is created
    }

    protected void sendMessage(String message){
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }

    }

    private void connectDevice(BluetoothDevice device, boolean secure) {
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatService.MESSAGE_STATE_CHANGE:
                    if(BluetoothChatService.D) Log.i(BluetoothChatService.TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d(BluetoothChatService.TAG, "CONNECTED");
                            String message = "unlock";
                            byte[] send = message.getBytes();
                            mChatService.write(send);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d(BluetoothChatService.TAG,"CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            Log.d(BluetoothChatService.TAG,"LISTENING");
                            break;
                        case BluetoothChatService.STATE_NONE:
                            Log.d(BluetoothChatService.TAG,"NONE");
                            break;
                    }
                    break;
                case BluetoothChatService.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    Log.d(BluetoothChatService.TAG, writeBuf.toString());
                    break;
                case BluetoothChatService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(BluetoothChatService.TAG, readMessage);
                    break;
                case BluetoothChatService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatService.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothChatService.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
