package com.example.vuher.btlockappserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vuher.btlockapp.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class BTLockAppServerFragment extends Fragment {


    public BTLockAppServerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_btlock_app, container, false);
    }


}
