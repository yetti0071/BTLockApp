package com.example.vuher.btlockapp;

import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


public class ClientActivity extends AbstractTalkativeActivity {
    boolean lockedState = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client);
        final ImageButton button = (ImageButton) findViewById(R.id.blueButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int imgId;
                if (lockedState) {
                    lockedState = false;
                    imgId = R.drawable.blueunlock;
                } else {
                    lockedState = true;
                    imgId = R.drawable.bluelock;
                }
                button.setImageDrawable(getResources().getDrawable(imgId));
                sendMessages(lockedState ? "unlock" : "lock");
            }
        });
    }

    @Override
    protected void handleMessage(Message msg) {
        String lockString = lockedState ? "unlock" : "lock";
        if(msg.what == BluetoothChatService.MESSAGE_CONECTION_READY) {
            mChatService.write(lockString.getBytes());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client, menu);
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
}
