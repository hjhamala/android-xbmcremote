package org.xbmc.android.remote.presentation.appwidget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.object.Song;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateNowPlayingWidgetService extends Service implements Callback {
	private static final String LOG = "UpdateNowPlayingWidgetService";
	public static final String COMMAND = "org.xbmc.android.remote.StartCommand";
	public static final String START_SERVICE = "UpdateNowPlayingWidgetService.start_service";
	private int[] allWidgetIds;
	final Handler mNowPlayingHandler = new Handler(this);
	private BroadcastReceiver mReceiver;
	private boolean running = false;
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(LOG, "Starting");
		
		// Register system listener. This cannot be done in the android manifest
		if (mReceiver == null){
			Log.i(LOG, "Register Listener");
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			mReceiver = new SystemMessageReceiver();
			registerReceiver(mReceiver, filter);
		}
		
		if (!running){
			subscribePoller();
			running = true;
		}
		
		Log.i(LOG, "OnStart");
		 
		Bundle extras = intent.getExtras();
		if (extras == null){
			Log.i(LOG, "Null extras");
		}
		if (extras != null && extras.containsKey(COMMAND)){
			Log.i(LOG, "got command");
			
			String command = extras.getString(COMMAND);
			
			if (command.equals(Intent.ACTION_SCREEN_OFF)){
				// Log.i(LOG, extras.getString(COMMAND));
				Log.i(LOG, "Screen off");
				unSubscribePoller();
			} else if (command.equals(Intent.ACTION_SCREEN_ON)){
				subscribePoller();
			} else if (command.equals(START_SERVICE)){
				subscribePoller();
			}
		}
		super.onStart(intent, startId);

	}


	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying)data.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);
		Log.i("handle", "" + msg.what);
		updateWidgets(allWidgetIds, currentlyPlaying);
		return false;
	}
	
	public void updateWidgets(int[] allWidgetIds, ICurrentlyPlaying currentlyPlaying){
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				NowPlayingWidget.class);
		allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		for (int widgetId : allWidgetIds) {
			// Create some random data
			

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_now_playing);
			
			// Set the text
			remoteViews.setTextViewText(R.id.widget_now_playing_artist,
					currentlyPlaying.getArtist()+ " - " + currentlyPlaying.getAlbum());
			
			remoteViews.setTextViewText(R.id.widget_now_playing_song,
					currentlyPlaying.getTitle());
			int duration = currentlyPlaying.getDuration();
			int time = currentlyPlaying.getTime();
			
			remoteViews.setTextViewText(R.id.widget_now_playing_duration, "(" + Song.getDuration(time) + 
					(duration == 0 ? "unknown" : " / " + Song.getDuration(duration))+")");
			
			// Register an onClickListener
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

	}
		
		
	 @Override
	    public void onDestroy() {
	        // Cancel nowplaying polling
		 	Log.i(LOG, "Unsubscribing");
		 	unregisterReceiver(mReceiver);
		 	unSubscribePoller();
	    }

	public void subscribePoller(){
		Log.i(LOG, "Subscriping");
		final Context context = this.getApplicationContext();
		new Thread("nowplaying-spawning") {
			@Override
			public void run() {
				ConnectionFactory.getNowPlayingPoller(context).subscribe(mNowPlayingHandler);
			}
		}.start();
	}
	 
	public void unSubscribePoller(){
		Log.i(LOG, "UnSubscriping");
		
		ConnectionFactory.getNowPlayingPoller(this.getApplicationContext()).unSubscribe(mNowPlayingHandler);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
} 