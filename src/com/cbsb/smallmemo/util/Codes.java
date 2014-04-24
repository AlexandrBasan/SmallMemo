package com.cbsb.smallmemo.util;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.ui.MemoEdit;
import com.cbsb.smallmemo.ui.MemoLauncher;

/**
 * The source for all codes and ids (other than the generated ones).
 * 
 * @author alex
 */
public interface Codes {
	/*
	 * MemoPad intent actions
	 */
	/**
	 * When creating/editing a memo via a {@link Notification}, this action should be set in the {@link Intent}. This will signal the {@link MemoEdit} activity
	 * to set a preference that the last action taken was a notification edit. When the control passes back to the {@link MemoLauncher}, that activity will
	 * refresh its list so the newly created/edited memo information is in its list.
	 */
	public static final String	ACTION_SET_NOTIFICATION_MODIFIER		= "com.alexhilman.memoPad.set_notification_modifier";
	public static final String	ACTION_CREATE_MEMO						= "com.alexhilman.memoPad.create_memo";
	public static final String	ACTION_EDIT_MEMO						= "com.alexhilman.memoPad.edit_memo";
	
	/**
	 * If using this action for your {@link Intent} in {@link Activity#startService(Intent)} then you need to supply
	 * {@link #PREFERENCE_NEW_MEMO_AS_NOTIFICATION} in the extras and set it to true.
	 */
	public static final String	ACTION_NEW_MEMO_NOTIFICATION			= "com.alexhilman.memoPad.new_memo";
	
	/**
	 * Used to signify that a memo should be deleted by the receiving object. When using this action you also need to supply {@link #MEMO_KEY_ID} containing the
	 * ID of the memo to delete.
	 */
	public static final String	ACTION_DELETE_MEMO						= "com.alexhilman.memoPad.delete_memo";
	
	/**
	 * Use this action to update the notification for a memo in the status bar. If you want to remove the notification, just supply this key as well as the
	 * {@link #MEMO_KEY_ID} to identify the memo to remove from the notifications. If you want to enable or update the text on the notification you must also
	 * supply the {@link #MEMO_KEY_TITLE}, {@link #MEMO_KEY_BODY}, and {@link #MEMO_KEY_ONGOING_NOTIFICATION} as fields within the intent.
	 */
	public static final String	ACTION_UPDATE_NOTIFICATION				= "com.alexhilman.memoPad.set_notification";
	
	/*
	 * Memo-related fields for database and intents.
	 */
	/**
	 * Name of the memo table.
	 */
	public static final String	MEMO_TABLE								= "memo";
	
	/**
	 * Key of the ID for {@link Intent} and the database field.
	 */
	public static final String	MEMO_KEY_ID								= "_id";
	
	/**
	 * Key of the title for {@link Intent}s and the database field.
	 */
	public static final String	MEMO_KEY_TITLE							= "title";
	
	/**
	 * Key of the body for {@link Intent}s and the database field.
	 */
	public static final String	MEMO_KEY_BODY							= "body";
	
	/**
	 * Key of the notification status for {@link Intent}s and the database field.
	 */
	public static final String	MEMO_KEY_ONGOING_NOTIFICATION			= "persist_notify";
	
	/*
	 * Settings-related fields for database and intents.
	 */
	/**
	 * Settings item for displaying the license notification at startup.
	 */
	public static final String	PREFERENCE_DISPLAY_LICENSE_AT_STARTUP	= "license_on_startup";
	
	/**
	 * Settings item for putting a new memo notification item in the status bar.
	 */
	public static final String	PREFERENCE_NEW_MEMO_AS_NOTIFICATION		= "new_memo_notification";
	
	/**
	 * When using a {@link Notification} to create/edit (with {@link #ACTION_SET_NOTIFICATION_MODIFIER} as the action on the {@link Intent}) the
	 * {@link MemoEdit} activity will set this preference to true. In {@link MemoLauncher#onResume()} the activity will check for this preference and refresh
	 * the list of memos if it is set to true (then it will set it to false, clearing the pref).
	 */
	public static final String	PREFERENCE_REFRESH_LIST_ON_RESUME		= "refresh_list_on_resume";
	
	/*
	 * Activity Codes
	 */
	/**
	 * Signifies a new memo to be created by the receiving object. If you want data to be pushed with the {@link Intent}, use {@link #MEMO_KEY_ID},
	 * {@link #MEMO_KEY_TITLE}, {@link #MEMO_KEY_BODY}, and {@link #MEMO_KEY_ONGOING_NOTIFICATION} in the {@link Intent}.
	 */
	public static final int		ACTIVITY_CREATE_MEMO					= 0;
	
	/**
	 * Signifies an edit of an existing memo. If you want data to be pushed with the {@link Intent}, use {@link #MEMO_KEY_ID}, {@link #MEMO_KEY_TITLE},
	 * {@link #MEMO_KEY_BODY}, and {@link #MEMO_KEY_ONGOING_NOTIFICATION} in the {@link Intent}.
	 */
	public static final int		ACTIVITY_EDIT_MEMO						= 1;
	
	/**
	 * Signifies a deletion of an existing memo. With this {@link Intent} you must also supply {@link #MEMO_KEY_ID} with the {@link Memo}'s ID.
	 */
	public static final int		ACTIVITY_DELETE_MEMO					= 2;
	
	/**
	 * Signifies a {@link Memo}'s notification status should be updated. You should include {@link #MEMO_KEY_ONGOING_NOTIFICATION} in the intent (and set it to
	 * true) if you want to enable a notification.
	 */
	public static final int		CHANGE_NOTIFY							= 3;
	
	/*
	 * Dialog Codes
	 */
	/**
	 * Launch a confirm dialog to the user. Currently just used to make sure the user actually wants to delete the memo.
	 */
	public static final int		DIALOG_CONFIM							= 0;
}
