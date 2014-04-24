package com.cbsb.smallmemo.service;

import java.util.ArrayList;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.R;
import com.cbsb.smallmemo.database.MemoAdapter;
import com.cbsb.smallmemo.receiver.MemoBroadcastReceiver;
import com.cbsb.smallmemo.ui.MemoEdit;
import com.cbsb.smallmemo.util.Codes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Short-lived service for handling notifications and some DB actions (when needed). This also receives broadcast messages from {@link MemoBroadcastReceiver}
 * (currently only {@link Intent#ACTION_BOOT_COMPLETED}).
 * 
 * @
 */
public class MemoService extends IntentService implements Codes {
	public static final String	TAG						= "MemoService";
	
	/**
	 * Notification System Service. Used to publish notifications.
	 */
	private NotificationManager	notifier;
	
	/**
	 * Database adapter... for the memos...
	 */
	private MemoAdapter			adapter;
	
	/**
	 * Maximum length of the body to put in a {@link Notification}. Reason: if the body is really really long then the status bar will actually display each
	 * line and character of the memo in an animation in the status bar. That animation sequence covers up other notifications as well as the system time. Since
	 * we don't want to annoy the user we will cap the body out at this character length when making each {@link Notification}.<br/>
	 * <br/>
	 * Note: if the body text has a carriage return in it, then the body length will be truncated to the carriage return instead.
	 */
	public static final int		MAX_NOTIFICATION_LENGTH	= 50;
	
	public MemoService () {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent (Intent intent) {
		/*
		 * Only handle intents with an action. This is either application specific or the boot completed one.
		 */
		if (intent == null || intent.getAction() == null)
			return;
		
		if (intent.getAction().equals(ACTION_UPDATE_NOTIFICATION)) {
			// The ID of the memo should be in KEY_ID of the bundled extras
			int id = intent.getIntExtra(MEMO_KEY_ID, -1);
			
			// If we didn't get an ID, just quietly fail
			if (id < 0)
				return;
			
			// Need to cancel regardless: either disabled, deleted, or changed conent
			cancelNotification(id);
			
			// If the notification is to be published then we must have the true value in KEY_PERSIST_NOTIFICATION
			boolean enabled = intent.getBooleanExtra(MEMO_KEY_ONGOING_NOTIFICATION, false);
			
			if (enabled) {
				// Publish the notification
				String title = intent.getStringExtra(MEMO_KEY_TITLE);
				String body = intent.getStringExtra(MEMO_KEY_BODY);
				
				addNotification(id, title, body, false);
			}
		} else if (intent.getAction().equals(ACTION_NEW_MEMO_NOTIFICATION)) {
			if (intent.getBooleanExtra(PREFERENCE_NEW_MEMO_AS_NOTIFICATION, false))
				showNewMemoNotification();
			else
				removeNewMemoNotification();
		} else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			showNotifications();
		}
	}
	
	@Override
	public void onCreate () {
		super.onCreate();
		notifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	
	/**
	 * Add a notification to the status bar.
	 * 
	 * @param id
	 *            ID of the notification, recommend that you use the {@link Memo#getId()} as the notification ID.
	 * @param label
	 *            Label of the memo; recommend that you use the memo's title
	 * @param text
	 *            Text of the notification; recommend that you use the memo's body (it will get trimed to 50 characters if it is too long).
	 * @param ongoing
	 *            True if you want it to ALWAYS be visible
	 */
	public void addNotification (int id, String label, String text, boolean ongoing) {
		// Limit the text of the notification to a max of 50 chars or to the first \n (whichever comes first)
		int a;
		int l = text.length();
		if ((a = text.indexOf('\n')) >= 0) {
			if (a < l) {
				text = text.substring(0, a);
			} else {
				text = text.substring(0, MAX_NOTIFICATION_LENGTH) + "...";
			}
		} else if (l > MAX_NOTIFICATION_LENGTH) {
			text = text.substring(0, MAX_NOTIFICATION_LENGTH) + "...";
		}
		
		// Which icon? If id == 0 then it's a new one and we should display the add icon
		int drawable = R.drawable.memo;
		if (id == 0)
			drawable = R.drawable.memo_add;
		
		// Make the notification
		Notification notification = new Notification(drawable, text, System.currentTimeMillis());
		
		// Set as an ongoing notification?
		if (ongoing)
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		else
			notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
		
		// Make the intent to launch the editing window
		Intent i = new Intent(this, MemoEdit.class);
		i.setAction(ACTION_SET_NOTIFICATION_MODIFIER);
		i.putExtra(MemoAdapter.MEMO_KEY_ID, new Integer(id));
		i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, id, i, 0);
		
		// Launch the notification
		notification.setLatestEventInfo(this, label, text, contentIntent);
		notifier.notify(id, notification);
	}
	
	/**
	 * Show all memos that should be in the status bar.
	 */
	public void showNotifications () {
		// Open the database connection
		adapter = new MemoAdapter(this);
		adapter.open();
		
		// Clear all notifications and only show the ones selected
		notifier.cancelAll();
		
		// Should we be showing the new memo notification?
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFERENCE_NEW_MEMO_AS_NOTIFICATION, false)) {
			showNewMemoNotification();
		}
		
		// Add all memos with notifications enabled
		ArrayList<Memo> memos = adapter.fetchNotifiedMemos(true);
		for (int i = 0; i < memos.size(); ++i) {
			addNotification(memos.get(i).getId(), memos.get(i).getTitle(), memos.get(i).getBody(), false);
		}
		
		// Done with the connection; close it!
		adapter.close();
	}
	
	/**
	 * Cancel a notification based on the notification ID. Remember that if you used the {@link Memo}'s ID then it's really easy :P
	 */
	public void cancelNotification (int id) {
		notifier.cancel(id);
	}
	
	/**
	 * Add the Create new Memo notification to the status bar.
	 */
	public void showNewMemoNotification () {
		addNotification(0, "Create a New Memo", "Tap to make a new memo.", true);
	}
	
	/**
	 * Remove the create new memo notification
	 */
	public void removeNewMemoNotification () {
		cancelNotification(0);
	}
}
