package org.xbmc.android.remote.presentation.appwidget;

import java.util.Random;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.object.Song;


import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateNowPlayingWidgetService extends Service implements Callback {
	private static final String LOG = "de.vogella.android.widget.example";
	private int[] allWidgetIds;
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(LOG, "Called");
		// Create some random data

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				NowPlayingWidget.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

		for (int widgetId : allWidgetIds) {
			// Create some random data
			int number = (new Random().nextInt(100));

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_now_playing);
			Log.w("service", "onStart");
			// Set the text
			

			// Register an onClickListener
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();
		final Context context = this.getApplicationContext();
		final Handler mNowPlayingHandler = new Handler(this);
		new Thread("nowplaying-spawning") {
			@Override
			public void run() {
				ConnectionFactory.getNowPlayingPoller(context).subscribe(mNowPlayingHandler);
			}
		}.start();
		
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
} 