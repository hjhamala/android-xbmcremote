package org.xbmc.android.remote.presentation.appwidget;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.NowPlayingPollerThread;
import org.xbmc.android.remote.presentation.activity.MovieLibraryActivity;
import org.xbmc.android.remote.presentation.activity.MusicLibraryActivity;
import org.xbmc.android.remote.presentation.activity.TvShowLibraryActivity;
import org.xbmc.android.remote.presentation.controller.AppWidgetRemoteController;
import org.xbmc.android.util.ConnectionFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.data.IControlClient.ICurrentlyPlaying;
import org.xbmc.api.info.PlayStatus;
import org.xbmc.api.object.Song;
import org.xbmc.eventclient.ButtonCodes;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateNowPlayingWidgetService extends Service implements Callback {
	private static final String LOG = "UpdateNowPlayingWidgetService";
	public static final String COMMAND = "org.xbmc.android.remote.StartCommand";
	public static final int START_SERVICE = 1;
	public static final int STOP_POLLING = 2;
	public static final int START_AFTER_SLEEP = 3;
	public static final int SEND_BUTTON = 4;

	public static final String URI_SCHEME = "remote_controller_widget";

	private int[] allWidgetIds;
	final Handler mNowPlayingHandler = new Handler(this);
	private SystemMessageReceiver mReceiver;

	/** Number of errors in row */
	private int error_count = 0;
	/** How many times try again */
	private final int ERROR_RETRIES = 3;
	private final long SLEEP_TIME = 10000;
	/** Timestamp for last error */
	private long last_error_milli_seconds = -SLEEP_TIME;

	private AppWidgetRemoteController mAppWidgetRemoteController;

	@Override
	public void onStart(Intent intent, int startId) {
		// Log.i(LOG, "Starting");

		// Register system listener. Screen status intent filtering cannot be
		// done in the android manifest
		registerSystemListener();

		// Setup AppWidgetRemoteController
		setupAppWidgetRemoteController();

		Log.i(LOG, "OnStart");
		Bundle extras = intent.getExtras();

		if (extras != null && extras.containsKey(COMMAND)) {
			switch (extras.getInt(COMMAND)) {
			case START_SERVICE:
				if (mReceiver.isScreenOn()) {
					error_count = 0;
					subscribeNowPlayingPoller();
				} else {
					// Screen is off, there is no need to keep service polling
					unSubscribeNowPlayingPoller();
				}
				break;
			case START_AFTER_SLEEP:
				subscribeNowPlayingPoller();
				break;
			case STOP_POLLING:
				unSubscribeNowPlayingPoller();
				break;
			case SEND_BUTTON:

				Log.i(LOG, "" + extras.getString("" + SEND_BUTTON));
				error_count = 0;
				mAppWidgetRemoteController.sendButton(extras.getString(""
						+ SEND_BUTTON));
				subscribeNowPlayingPoller();
				break;
			}
		}
		super.onStart(intent, startId);
	}

	private void setupAppWidgetRemoteController() {
		if (mAppWidgetRemoteController == null) {
			// If the app is not initialized this should cause it to try connect
			// to
			// the latest host and we also avoid noSettings exception
			HostFactory.readHost(getApplicationContext());
			mAppWidgetRemoteController = new AppWidgetRemoteController(
					getApplicationContext());

		}
	}

	private void registerSystemListener() {
		if (mReceiver == null) {
			Log.i(LOG, "Register Listener");
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(SystemMessageReceiver.COMMAND);
			mReceiver = new SystemMessageReceiver();
			registerReceiver(mReceiver, filter);
		}
	}

	/**
	 * Makes service sleep and registers alarm manager to wake up service
	 */
	private void sleepService() {
		if (last_error_milli_seconds < SystemClock.elapsedRealtime()
				- SLEEP_TIME) {
			unSubscribeNowPlayingPoller();
			if (error_count < ERROR_RETRIES) {
				Log.e(LOG, "Wait for 10 seconds");
				Intent wakeup_intent = new Intent(this.getApplicationContext(),
						UpdateNowPlayingWidgetService.class);
				wakeup_intent.putExtra(UpdateNowPlayingWidgetService.COMMAND,
						UpdateNowPlayingWidgetService.START_AFTER_SLEEP);
				PendingIntent sender = PendingIntent.getService(
						getApplicationContext(), 0, wakeup_intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				// Get the AlarmManager service
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				am.set(AlarmManager.RTC, System.currentTimeMillis()
						+ SLEEP_TIME, sender);
			} else {
				// Too many errors, user need to manually start the widget again
				Log.e(LOG, "Too many errors");
				return;
			}
			last_error_milli_seconds = SystemClock.elapsedRealtime();
			error_count++;
		} else {
			Log.e(LOG, "We are most propably sleeping all ready");
		}
	}

	public boolean handleMessage(Message msg) {

		final Bundle data = msg.getData();
		final ICurrentlyPlaying currentlyPlaying = (ICurrentlyPlaying) data
				.getSerializable(NowPlayingPollerThread.BUNDLE_CURRENTLY_PLAYING);
		Log.i("handle", "" + msg.what);

		switch (msg.what) {
		case NowPlayingPollerThread.MESSAGE_CONNECTION_ERROR:
		case NowPlayingPollerThread.MESSAGE_RECONFIGURE:
			updateWidgetsOnError();
			sleepService();
			break;
		case NowPlayingPollerThread.MESSAGE_PROGRESS_CHANGED:
		case NowPlayingPollerThread.MESSAGE_COVER_CHANGED:
		case NowPlayingPollerThread.MESSAGE_PLAYSTATE_CHANGED:
			updateWidgets(currentlyPlaying);
			break;
		}
		return true;
	}

	private void updateWidgetsOnError() {
		// TODO Auto-generated method stub
		Log.i(LOG, "Updating widgets");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				NowPlayingWidget.class);
		allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_now_playing);

			remoteViews.setTextViewText(R.id.widget_now_playing_artist,
					getApplicationContext().getString(R.string.error));
			remoteViews
					.setTextViewText(
							R.id.widget_now_playing_title,
							getApplicationContext()
									.getString(
											R.string.make_sure_XBMC_webserver_is_enabled_and_XBMC_is_running));
			remoteViews.setTextViewText(R.id.widget_now_playing_song, getString(R.string.click_here_to_retry_connection));
			
			AppWidgetRemoteController.setupWidgetButtonforService(remoteViews,
					getApplicationContext(), R.id.widget_now_playing_song, this,
					"", COMMAND, START_SERVICE);
			remoteViews.setTextViewText(R.id.widget_now_playing_duration, "");

			attachPendingIntents(this.getApplicationContext(), remoteViews);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	/**
	 * Update all the widgets
	 * 
	 * @param allWidgetIds
	 * @param currentlyPlaying
	 */
	public void updateWidgets(ICurrentlyPlaying currentlyPlaying) {

		Log.i(LOG, "Updating widgets");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				NowPlayingWidget.class);
		allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_now_playing);

			switch (currentlyPlaying.getPlayStatus()) {
			case PlayStatus.PLAYING:
			case PlayStatus.PAUSED:
				remoteViews.setTextViewText(R.id.widget_now_playing_artist,
						currentlyPlaying.getArtist());
				remoteViews.setTextViewText(R.id.widget_now_playing_title,
						currentlyPlaying.getAlbum());
				remoteViews.setTextViewText(R.id.widget_now_playing_song,
						currentlyPlaying.getTitle());

				int duration = currentlyPlaying.getDuration();
				int time = currentlyPlaying.getTime();

				remoteViews.setTextViewText(
						R.id.widget_now_playing_duration,
						"("
								+ Song.getDuration(time)
								+ (duration == 0 ? "unknown" : " / "
										+ Song.getDuration(duration)) + ")");
				break;
			case PlayStatus.STOPPED:
			case PlayStatus.UNKNOWN:
				remoteViews.setTextViewText(R.id.widget_now_playing_artist,
						getString(R.string.nothing_playing));
				remoteViews.setTextViewText(R.id.widget_now_playing_title, "");
				remoteViews.setTextViewText(R.id.widget_now_playing_song, "");

				remoteViews.setTextViewText(R.id.widget_now_playing_duration,
						"");

				break;
			}

			attachPendingIntents(this.getApplicationContext(), remoteViews);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

	}

	@Override
	public void onDestroy() {
		// Cancel nowplaying polling
		Log.i(LOG, "onDestroy");
		unregisterReceiver(mReceiver);
		unSubscribeNowPlayingPoller();
	}

	public void subscribeNowPlayingPoller() {
		Log.i(LOG, "Subscriping");
		final Context context = this.getApplicationContext();
		new Thread("nowplaying-spawning") {
			@Override
			public void run() {
				ConnectionFactory.subscribeNowPlayingPollerThread(context,
						mNowPlayingHandler);
			}
		}.start();
	}

	public void unSubscribeNowPlayingPoller() {
		Log.i(LOG, "UnSubscriping");

		ConnectionFactory.unSubscribeNowPlayingPollerThread(
				this.getApplicationContext(), mNowPlayingHandler);

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Attaches pending intents to view
	 * 
	 * @param context
	 * @param remoteView
	 * @param widgetId
	 */
	private void attachPendingIntents(Context context, RemoteViews remoteView) {
		Log.i(LOG, "AttachinPendingIntents");

		AppWidgetRemoteController.setupWidgetButtonforService(remoteView,
				context, R.id.widget_now_playing_button_prev, this,
				ButtonCodes.REMOTE_SKIP_MINUS, COMMAND, SEND_BUTTON);

		AppWidgetRemoteController.setupWidgetButtonforService(remoteView,
				context, R.id.widget_now_playing_button_stop, this,
				ButtonCodes.REMOTE_STOP, COMMAND, SEND_BUTTON);

		AppWidgetRemoteController.setupWidgetButtonforService(remoteView,
				context, R.id.widget_now_playing_button_play, this,
				ButtonCodes.REMOTE_PLAY, COMMAND, SEND_BUTTON);

		AppWidgetRemoteController.setupWidgetButtonforService(remoteView,
				context, R.id.widget_now_playing_button_next, this,
				ButtonCodes.REMOTE_SKIP_PLUS, COMMAND, SEND_BUTTON);

		AppWidgetRemoteController.setupWidgetButtonforActivity(remoteView,
				context, R.id.widget_now_playing_open_tv_shows_library,
				TvShowLibraryActivity.class);

		AppWidgetRemoteController.setupWidgetButtonforActivity(remoteView,
				context, R.id.widget_now_playing_open_music_library,
				MusicLibraryActivity.class);

		AppWidgetRemoteController.setupWidgetButtonforActivity(remoteView,
				context, R.id.widget_now_playing_open_movie_library,
				MovieLibraryActivity.class);

	}

}