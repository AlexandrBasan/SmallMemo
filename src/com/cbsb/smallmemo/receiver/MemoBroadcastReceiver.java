/**
 * 
 */
package com.cbsb.smallmemo.receiver;

import com.cbsb.smallmemo.service.MemoService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver of intents from the system. The only purpose of this receiver is to catch the {@link Intent#ACTION_BOOT_COMPLETED} broadcast from the Android
 * system. This will then start the {@link MemoService} with the boot completed intent. When the service receives this intent all memos that have notifications
 * are published to the status bar as notifications.
 * 
 * @
 */
public class MemoBroadcastReceiver extends BroadcastReceiver {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive (Context context, Intent intent) {
		// Start the MemoService with the received intent. MemoService will populate the needed notifications.
		intent.setClass(context, MemoService.class);
		context.startService(intent);
	}
	
}
