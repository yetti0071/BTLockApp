package com.example.vuher.btlockapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;


public class KostyaActivity extends AbstractTalkativeActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kostya);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kostya, menu);
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

    public void onClickClientButton(View view) {
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }

    public void onClicServerButton(View view) {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

/*
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
*/


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
        unregisterReceiver(receiver);
    }

}
