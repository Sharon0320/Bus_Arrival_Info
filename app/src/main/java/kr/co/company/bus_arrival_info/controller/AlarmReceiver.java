package kr.co.company.bus_arrival_info.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show a notification or toast when alarm triggers
        Toast.makeText(context, "It's time for your bus arrival alert!", Toast.LENGTH_LONG).show();
    }
}