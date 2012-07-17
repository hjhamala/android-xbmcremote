package org.xbmc.android.remote.presentation.controller;

import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.presentation.activity.MusicLibraryActivity;
import org.xbmc.android.remote.presentation.activity.TvShowLibraryActivity;
import org.xbmc.android.remote.presentation.appwidget.RemoteControllerWidget;
import org.xbmc.android.remote.presentation.appwidget.SystemMessageReceiver;
import org.xbmc.android.remote.presentation.appwidget.UpdateNowPlayingWidgetService;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.presentation.INotifiableController;

import org.xbmc.android.remote.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class AppWidgetRemoteController extends RemoteController implements
		INotifiableController {
	public static final String COMMAND = "remote.controller.widget.command";
	public static final String LOG = "AppWidgetRemoteController";
	public static final String ERROR_MESSAGE = "remote.controller.widget.error_message";
	private static long ERROR_TIME_MILLISEC = 0;
	private Context context;
	
	
	public AppWidgetRemoteController(Context context) {
		super(context);
		// We need context to send broadcast to the system
		this.context = context;
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		Log.w("onWrongConnectionState","catch");
		// super.onWrongConnectionState(state, manager, source);
	}

	@Override
	public void onError(Exception exception) {
		
		if (context != null){
				// Sending error to widget to dot what it likes
				Log.w("onCatch","Catch Error on:" + this.getClass().toString());
				// Only send errors if last error has been send over five seconds ago, this prevents toast spamming
				if (SystemClock.elapsedRealtime()>ERROR_TIME_MILLISEC+1000*5){
					Intent active = new Intent(context, RemoteControllerWidget.class);				
					active.setAction(RemoteControllerWidget.ACTION_WIDGET_CONTROL);																		
					active.putExtra(ERROR_MESSAGE, R.string.make_sure_XBMC_webserver_is_enabled_and_XBMC_is_running);
					context.sendBroadcast(active);
					ERROR_TIME_MILLISEC = SystemClock.elapsedRealtime();
				}
		}
	}
	
	@Override
	public void onMessage(final String message){
		Log.i("onMessage",message);
	}
	
	
	
	/**
	 * Attaches pendingIntents to widget buttons
	 * Static method because there is no need to make Controller object in widget update
	 * @param viewId
	 * @param buttonCode
	 * @param widgetId
	 */
	public static void setupWidgetButton(RemoteViews remoteView, int viewId, Context context, Object caller,  String buttonCode, int widgetId, String uri, String action){
	
        Intent active = new Intent(context, caller.getClass());
        active.setAction(action);
        
        active.putExtra(COMMAND, buttonCode);
        
        // Make this pending intent unique to prevent updating other intents
        Uri data = Uri.withAppendedPath(Uri.parse(uri + "://widget/id/#"+buttonCode), String.valueOf(widgetId));	
		active.setData(data);
        remoteView.setOnClickPendingIntent(viewId, PendingIntent.getBroadcast(context, 0, active, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public void sendButton(String buttonCode){
		mEventClientManager.sendButton("R1", buttonCode, false, true, false, (short)0, (byte)0);
	}
	
	public static void setupWidgetButtonforService(RemoteViews remoteView, Context context, int viewId, Object serviceObject, String buttonCode,String extra_key, int extra_value){
		
        Intent active = new Intent(context, serviceObject.getClass());        
        active.putExtra(extra_key, extra_value);
        active.putExtra("" + extra_value, buttonCode);
     // Make this pending intent unique to prevent updating other intents
        
        remoteView.setOnClickPendingIntent(viewId, PendingIntent.getService(context, viewId, active, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static void setupWidgetButtonforActivity(RemoteViews remoteView,
			Context context, int view_id,
			Class class1) {

		Intent active = new Intent(context, class1);        
	    // Make this pending intent unique to prevent updating other intents
		
        remoteView.setOnClickPendingIntent(view_id, PendingIntent.getActivity(context, view_id, active, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static void removeWidgetButtonForService(RemoteViews remoteView, Context context, int view_id, Object serviceObject, String buttonCode,String extra_key, int extra_value) {
		Intent active = new Intent(context, serviceObject.getClass());        
        active.putExtra(extra_key, extra_value);
        active.putExtra("" + extra_value, buttonCode);
        PendingIntent cancel = PendingIntent.getService(context, view_id, active, PendingIntent.FLAG_CANCEL_CURRENT);
		cancel.cancel();
	}
	
	
	
}
