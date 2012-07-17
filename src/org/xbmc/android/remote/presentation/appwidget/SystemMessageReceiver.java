package org.xbmc.android.remote.presentation.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

/**
 * Monitors the system for the screen events
 * @author heikki
 *
 */
public class SystemMessageReceiver extends BroadcastReceiver {
     
    public static boolean wasScreenOn = true;
    public static final String LOG = "SystemMessageReceiver";
    public static final String COMMAND = "org.xbmc.android.remote.StartCommand";
    // This is really needed because in this api level we cannot access power managers functions
    public static volatile boolean is_screen_on = true;
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(LOG, "onReceive");

    	if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            is_screen_on = false;
            Log.i(LOG, "Screen off");
            Intent intent1 = new Intent(context,
    				UpdateNowPlayingWidgetService.class);
            intent1.putExtra(UpdateNowPlayingWidgetService.COMMAND, UpdateNowPlayingWidgetService.STOP_POLLING);
            
            context.startService(intent1);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
        	is_screen_on = true;
            Log.i(LOG, "Screen on");
            Intent intent1 = new Intent(context,
    				UpdateNowPlayingWidgetService.class);
            intent1.putExtra(UpdateNowPlayingWidgetService.COMMAND, UpdateNowPlayingWidgetService.START_POLLING);
            context.startService(intent1);
        }
    }
    
    /**
     * Returns last screen status, technically this is needed because xbmc remotes apilevel doesnt have necessary power manager
     * functions which support screen status polling
     * @return
     */
    public boolean isScreenOn(){
    	return is_screen_on;
    }
}