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


}
