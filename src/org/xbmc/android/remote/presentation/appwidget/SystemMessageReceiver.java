package org.xbmc.android.remote.presentation.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemMessageReceiver extends BroadcastReceiver {
     
    // thanks Jason
    public static boolean wasScreenOn = true;
    public static final String LOG = "SystemMessageReceiver";
    
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;
            Log.i(LOG, "Screen off");
            Intent intent1 = new Intent(context,
    				UpdateNowPlayingWidgetService.class);
            intent1.putExtra(UpdateNowPlayingWidgetService.COMMAND, Intent.ACTION_SCREEN_OFF);
            
            context.startService(intent1);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;
            Log.i(LOG, "Screen on");
            Intent intent1 = new Intent(context,
    				UpdateNowPlayingWidgetService.class);
            intent1.putExtra(UpdateNowPlayingWidgetService.COMMAND, Intent.ACTION_SCREEN_ON);
            context.startService(intent1);
        }
    }

}