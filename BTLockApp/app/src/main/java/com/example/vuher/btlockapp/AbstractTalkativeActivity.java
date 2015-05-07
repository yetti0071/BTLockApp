package com.example.vuher.btlockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
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
public abstract class AbstractTalkativeActivity extends ActionBarActivity {
    protected final String ACT_LOCK = "lock";
    protected final String ACT_UNLOCK = "unlock";
    protected final String ACT_LOST = "lost";

    protected BluetoothChatService mChatService = null;
    protected BluetoothAdapter mBluetoothAdapter;
    protected final int REQUEST_ENABLE_BT = 6;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Name of the connected device
    private String mConnectedDeviceName = null;

    abstract protected void handleMessage(Message msg);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpBluetooth();
        //  registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));  // this is not working for connected devices
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("receive","receiving action:" + action);
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d("receive","receiving");
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = (TextView) findViewById(R.id.rssiOutput);
                rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(BluetoothChatService.D) Log.e(BluetoothChatService.TAG, "--- ON DESTROY ---");
        // TODO    java.lang.RuntimeException: Unable to destroy activity {com.example.vuher.btlockapp/com.example.vuher.btlockapp.ClientActivity}: java.lang.IllegalArgumentException: Receiver not registered: com.example.vuher.btlockapp.AbstractTalkativeActivity$1@437fede8
// check if the receiver is created
        unregisterReceiver(receiver);
    }

    private void setUpBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new RuntimeException("AAAAAA");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mChatService = new BluetoothChatService(getWindow().getContext(), mHandler);
        }

    }


    private void showOkDialog() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.ok_dialog_message)
                .setTitle(R.string.ok_dialog_title);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showFailDialog() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.fail_dialog_message)
                .setTitle(R.string.fail_dialog_title);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("code: " + requestCode);
        if(requestCode == REQUEST_ENABLE_BT) {
            System.out.println("result: " + resultCode);
            if(resultCode == Activity.RESULT_OK) {
                showOkDialog();
                mChatService = new BluetoothChatService(getWindow().getContext(), mHandler);
            } else if(resultCode == Activity.RESULT_CANCELED) {
                showFailDialog();
                // listDevices();
                // TODO exit app
            }
        }
    }


    /**
     * broadcasts a message
     * iterate all paired devices and try to send a message
     * @param message  A string o f text to send.
     */
    protected void sendMessages(String message) {
        Log.i("info", "broadcasting unlock command");

        String output = "";
        try{
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices

                for(BluetoothDevice device : pairedDevices) {
                    //output += device.getName() + "   "+ device.getAddress()+  "   "+ device.getBondState()+" "+checkDeviceAvailability(device)+"\n";
                    //tv.setText(output);
                    sendMessageToDevice(message, device, true);
                }
        }catch(Exception Ex){
            Log.w("chyba", Ex);
        }

    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if(BluetoothChatService.D) Log.e(BluetoothChatService.TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(BluetoothChatService.D) Log.e(BluetoothChatService.TAG, "-- ON STOP --");
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
            AbstractTalkativeActivity.this.handleMessage(msg);
        }
    };
}
