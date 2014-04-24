/**
 * 
 */
package com.cbsb.smallmemo.database;

import java.util.ArrayList;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.util.Codes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @
 */
public class MemoAdapter implements Codes {
	/**
	 * Version of the database. When this value is changed the {@link DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)} method is called the next time the
	 * application is run on a device.
	 */
	private static final int	DB_VERSION					= 2;
	
	/**
	 * SQL for creating the memo table.
	 */
	private static final String	MEMO_CREATE_STATEMENT		= "create table memo (_id integer primary key autoincrement, title text not null, body text not null, persist_notify integer not null);";
	
	/**
	 * Identifier for the file name for the DB.
	 */
	private static final String	DB_NAME						= "data";
	
	/**
	 * My implementation of the {@link SQLiteOpenHelper} class. See {@link DatabaseHelper} for more info.
	 */
	private DatabaseHelper		dbHelper;
	
	/**
	 * The actual DB connector to the file system. All queries are made through this object.
	 */
	private SQLiteDatabase		db;
	
	/**
	 * Application, Activity, or Service context in which a {@link MemoAdapter} instance is to be run.
	 */
	private final Context		context;
	
	/**
	 * Aids in the creation and upgrade of the database file on a device.
	 * 
	 * @
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper (Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onCreate (SQLiteDatabase db) {
			db.execSQL(MEMO_CREATE_STATEMENT);
		}
		
		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
				case 1:
					// Upgrade to 2
					db.execSQL("create table setting (display_license_at_startup integer, new_memo_as_notification integer)");
					db.execSQL("insert into setting values (1,0)");
				case 2:
					// Remove setting table (lol) - now done with SharedPreferences
					db.execSQL("drp table setting");
			}
		}
	}
	
	/**
	 * Constructor that accepts only the context.
	 * 
	 * @param context
	 */
	public MemoAdapter (Context context) {
		this.context = context;
	}
	
	/**
	 * Creates/opens the database.
	 * 
	 * @return MemoDBAdapter this
	 */
	public MemoAdapter open () {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * Test to see if the adapter has been opened.
	 * 
	 * @return True if opened, false if it needs to be opened
	 */
	public boolean isOpen () {
		return db != null && db.isOpen();
	}
	
	/**
	 * Close the database.
	 */
	public void close () {
		db.close();
		dbHelper.close();
	}
	
	/**
	 * Write a new memo into the database.
	 * 
	 * @return int rowID or -1 on failure.
	 */
	public int createMemo (String title, String body, boolean persist) {
		ContentValues values = new ContentValues();
		values.put(MEMO_KEY_TITLE, title);
		values.put(MEMO_KEY_BODY, body);
		values.put(MEMO_KEY_ONGOING_NOTIFICATION, persist ? 1 : 0);
		
		return (int) db.insert(MEMO_TABLE, null, values);
	}
	
	/**
	 * Delete a memo from the DB.
	 * 
	 * @param id
	 * @return true on success, false on failure.
	 */
	public boolean deleteMemo (int id) {
		return db.delete(MEMO_TABLE, MEMO_KEY_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Get all notes from the DB.
	 * 
	 * @param fetchBody
	 *            True if you want to get the body of each memo along with the rest of the data.
	 * @return {@link ArrayList} of memos. If the fetchBody parameter is true, then the body of each memo will also be fetched.
	 */
	public ArrayList<Memo> fetchAllMemos (boolean fetchBody) {
		String[] fields = null;
		
		if (fetchBody) {
			fields = new String[] { MEMO_KEY_ID, MEMO_KEY_TITLE, MEMO_KEY_BODY, MEMO_KEY_ONGOING_NOTIFICATION };
		} else {
			fields = new String[] { MEMO_KEY_ID, MEMO_KEY_TITLE, MEMO_KEY_ONGOING_NOTIFICATION };
		}
		
		Cursor c = db.query(MEMO_TABLE, fields, null, null, null, null, MEMO_KEY_TITLE);
		ArrayList<Memo> memos = new ArrayList<Memo>(c.getCount());
		
		if (c.getCount() > 0) {
			Memo temp = null;
			while (c.moveToNext()) {
				temp = new Memo(c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ID)));
				temp.setTitle(c.getString(c.getColumnIndexOrThrow(MEMO_KEY_TITLE)));
				temp.setNotify(c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ONGOING_NOTIFICATION)) > 0);
				if (fetchBody)
					temp.setBody(c.getString(c.getColumnIndexOrThrow(MEMO_KEY_BODY)));
				memos.add(temp);
			}
		}
		
		c.close();
		
		return memos;
	}
	
	/**
	 * Fetch all memos that are supposed to be in the status bar notification area.
	 * 
	 * @param fetchBody
	 *            True if you want to fetch the body of the memos as well as the rest of the data. Note: if you don't need the body for anything then pass in
	 *            false as it will speed up the file system read.
	 * @return {@link ArrayList} of {@link Memo}s.
	 */
	public ArrayList<Memo> fetchNotifiedMemos (boolean fetchBody) {
		String[] fields = null;
		
		if (fetchBody) {
			fields = new String[] { MEMO_KEY_ID, MEMO_KEY_TITLE, MEMO_KEY_BODY, MEMO_KEY_ONGOING_NOTIFICATION };
		} else {
			fields = new String[] { MEMO_KEY_ID, MEMO_KEY_TITLE, MEMO_KEY_ONGOING_NOTIFICATION };
		}
		
		Cursor c = db.query(MEMO_TABLE, fields, MEMO_KEY_ONGOING_NOTIFICATION + "=" + "1", null, null, null, MEMO_KEY_TITLE);
		ArrayList<Memo> memos = new ArrayList<Memo>(c.getCount());
		
		if (c.getCount() > 0) {
			Memo temp = null;
			while (c.moveToNext()) {
				temp = new Memo(c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ID)));
				temp.setTitle(c.getString(c.getColumnIndexOrThrow(MEMO_KEY_TITLE)));
				temp.setNotify(c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ONGOING_NOTIFICATION)) > 0);
				if (fetchBody)
					temp.setBody(c.getString(c.getColumnIndexOrThrow(MEMO_KEY_BODY)));
				memos.add(temp);
			}
		}
		
		c.close();
		
		return memos;
	}
	
	/**
	 * Retrieve one memo from the database.
	 * 
	 * @param id
	 * @return {@link Memo} containing all of the memo components.
	 */
	public Memo fetchMemo (int id) {
		Cursor c = db.query(MEMO_TABLE, new String[] { MEMO_KEY_ID, MEMO_KEY_TITLE, MEMO_KEY_BODY, MEMO_KEY_ONGOING_NOTIFICATION }, MEMO_KEY_ID + "=" + id,
				null, null, null, null);
		
		Memo m = null;
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			
			m = new Memo(c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ID)), c.getString(c.getColumnIndexOrThrow(MEMO_KEY_TITLE)), c.getString(c
					.getColumnIndexOrThrow(MEMO_KEY_BODY)), c.getInt(c.getColumnIndexOrThrow(MEMO_KEY_ONGOING_NOTIFICATION)) > 0);
		}
		
		c.close();
		
		return m;
	}
	
	/**
	 * Update a memo.
	 * 
	 * @param id
	 *            Database id of the memo to update
	 * @param title
	 *            Title to update
	 * @param body
	 *            Body to update
	 * @param notify
	 *            Notification status to update
	 * @return True if updated, false on failure.
	 */
	public boolean updateMemo (int id, String title, String body, boolean notify) {
		ContentValues values = new ContentValues();
		values.put(MEMO_KEY_TITLE, title.trim());
		values.put(MEMO_KEY_BODY, body);
		values.put(MEMO_KEY_ONGOING_NOTIFICATION, notify ? 1 : 0);
		
		return db.update(MEMO_TABLE, values, MEMO_KEY_ID + '=' + id, null) > 0;
	}
	
	/**
	 * Set notification status for a memo.
	 * 
	 * @param id
	 *            Database id of the memo to update
	 * @param on
	 *            True if the notification status is now on, false otherwise.
	 * @return True if updated, false otherwise.
	 */
	public boolean updateNotify (int id, boolean on) {
		ContentValues values = new ContentValues();
		values.put(MEMO_KEY_ONGOING_NOTIFICATION, on ? 1 : 0);
		return db.update(MEMO_TABLE, values, MEMO_KEY_ID + '=' + id, null) > 0;
	}
	
	/**
	 * Check whether or not a title is already taken.
	 * 
	 * @param id
	 *            Database ID of the existing memo (this memo must be excluded from the result set).
	 * @param title
	 *            Title to for which to check.
	 * @return True if the title is available, false if it is taken.
	 */
	public boolean isTitleAvailable (int id, String title) {
		Cursor c = db.query(MEMO_TABLE, new String[] { MEMO_KEY_TITLE }, MEMO_KEY_TITLE + "=? AND " + MEMO_KEY_ID + "<>" + id, new String[] { title }, null,
				null, null);
		boolean available = true;
		
		if (c.getCount() > 0) {
			available = false;
		}
		
		c.close();
		
		return available;
	}
	
	/**
	 * Get the body for a memo.
	 * 
	 * @param id
	 *            Database id of the memo
	 * @return String Body of the memo
	 */
	public String getMemoBody (int id) {
		Cursor c = db.query(MEMO_TABLE, new String[] { MEMO_KEY_BODY }, MEMO_KEY_ID + "=" + id, null, null, null, null);
		String body = null;
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			body = c.getString(c.getColumnIndex(MEMO_KEY_BODY));
		}
		
		c.close();
		
		return body;
	}
}
