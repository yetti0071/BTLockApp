package com.example.vuher.btlockapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class BTLockApp extends ActionBarActivity {

    Activity mParent;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 6;

    private BluetoothChatService mChatService = null;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Name of the connected device
    private String mConnectedDeviceName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btlock_app);

        setUpBluetooth();
      //  registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));  // this is not working for connected devices


        Button boton = (Button) findViewById(R.id.button);
        boton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // listener na click
                sendMessages("unlock");
                //  listDevices();
            }
        });
    }

    private void connectDevice(BluetoothDevice device, boolean secure) {
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    /**
     * broadcasts a message.
     * @param message  A string of text to send.
     */
    private void sendMessages(String message) {
        Log.i("info", "broadcasting unlock command");

        ArrayList<String> pairedDevicesToList = new ArrayList<>();
        String output = "";
        try{
            TextView tv = (TextView)findViewById(R.id.ListOfDevices);
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


    private void sendMessageToDevice(String message, BluetoothDevice device, boolean secure) {

        connectDevice(device, secure); // sending is done in Handler so we do not need to sleep untill  the connection is created
    }

    private void sendMessage(String message){
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_btlock_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                //listDevices();
            } else if(resultCode == Activity.RESULT_CANCELED) {
                showFailDialog();
               // listDevices();
                // TODO exit app
            }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(BluetoothChatService.D) Log.e(BluetoothChatService.TAG, "--- ON DESTROY ---");
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



    private float checkDeviceAvailability(BluetoothDevice mBluetoothDevice) {

        BluetoothSocket mSocket = null;

        Method method;
        long time = 0;
        boolean result = false;
        try {
            Log.w("chyba", "chyba1");
            Class<?> clazz = mBluetoothDevice.getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {Integer.valueOf(5)};
            mSocket = (BluetoothSocket) m.invoke(mBluetoothDevice, params);
            Log.w("chyba", "chyba2");
            //  mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.randomUUID());
            time = System.nanoTime();
            mSocket.connect();
            Log.w("chyba", "chyba3");
            mSocket.close();// TODO uncomment if needed
            Log.w("chyba", "chyba4");
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception Ex){
            Ex.printStackTrace();
        } finally {
            time = System.nanoTime() - time;
            if(result) {
                return time/1000000f;
            } else {
                return -1;
            }
        }
    }



    //   http://stackoverflow.com/questions/15312858/get-bluetooth-signal-strength
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_rssi, menu);
        return true;
    }
*/

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d("receive","receiving action:" + action);
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d("receive","receiving");
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = (TextView) findViewById(R.id.textView2);
                rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
    }
}

