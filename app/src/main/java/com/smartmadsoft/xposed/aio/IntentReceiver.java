package com.smartmadsoft.xposed.aio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("com.smartmadsoft.xposed.aio.FLASHLIGHT_TOGGLE")){
            Intent intentService = new Intent(context, FlashlightService.class);
            context.startService(intentService);
        }

    }
}
