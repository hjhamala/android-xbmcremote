package org.xbmc.android.remote.presentation.appwidget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.AppWidgetRemoteController;
import org.xbmc.android.util.HostFactory;
import org.xbmc.eventclient.ButtonCodes;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Base class for the remote controller widget based on RemoteController
 * activity in portrait mode.
 * 
 * @author Heikki Hämäläinen
 * 
 */
public class NowPlayingWidget extends AppWidgetProvider {

	public static final String LOG = "NowPlayingWidget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		ComponentName thisWidget = new ComponentName(context,
				NowPlayingWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Loop for all widgets
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteView = new RemoteViews(context.getPackageName(),
					R.layout.widget_now_playing);
			
			AppWidgetManager.getInstance(context).updateAppWidget(widgetId,
					remoteView);
		}
		
		Log.i(LOG, "onUpdate");
		Intent intent = new Intent(context.getApplicationContext(),
				UpdateNowPlayingWidgetService.class);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		intent.putExtra(UpdateNowPlayingWidgetService.COMMAND, UpdateNowPlayingWidgetService.START_POLLING);
		
		context.startService(intent);
	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		
		Intent intent = new Intent(context.getApplicationContext(),
				UpdateNowPlayingWidgetService.class);
		
		context.stopService(intent);
	}
	
	
	
}
