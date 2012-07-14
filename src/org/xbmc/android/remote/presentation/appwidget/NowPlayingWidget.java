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

	public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";
	public static final String ACTION_WIDGET_CONTROL = "org.xbmc.android.remote.WIDGET_CONTROL";
	public static final String URI_SCHEME = "remote_controller_widget";


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// If the app is not initialized this should cause it to try connect to
		// the latest host and we also avoid noSettings exception
		HostFactory.readHost(context);
		
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
		
		Intent intent = new Intent(context.getApplicationContext(),
				UpdateNowPlayingWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);
	}

	
}
