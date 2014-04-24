/**
 * 
 */
package com.cbsb.smallmemo.ui;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.R;
import com.cbsb.smallmemo.database.MemoAdapter;
import com.cbsb.smallmemo.service.MemoService;
import com.cbsb.smallmemo.util.Codes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @
 */
public class MemoEdit extends Activity implements Codes {
	private MemoAdapter	adapter;
	private EditText	title;
	private EditText	body;
	private CheckBox	notify;
	private int			id	= 0;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Open the DB adapter. Although this is launched from the MainLauncher, we still need a DB adapter for this
		// instance because there is more than one potential entry point for the application
		adapter = new MemoAdapter(this);
		adapter.open();
		
		setContentView(R.layout.memo_edit);
		title = (EditText) findViewById(R.id.editTitle);
		body = (EditText) findViewById(R.id.editBody);
		notify = (CheckBox) findViewById(R.id.editNotify);
		
		/*
		 * Populate the fields and values in this object's memory space
		 */
		if (savedInstanceState == null) {
			// Use intent to populate
			Intent i = getIntent();
			Memo m = adapter.fetchMemo(i.getIntExtra(MEMO_KEY_ID, 0));
			if (m != null) {
				id = m.getId();
				title.setText(m.getTitle());
				body.setText(m.getBody());
				notify.setChecked(m.isNotify());
			}
		} else {
			// Was restarted by OS, fill with old values
			id = savedInstanceState.getInt(MEMO_KEY_ID);
			title.setText(savedInstanceState.getString(MEMO_KEY_TITLE));
			body.setText(savedInstanceState.getString(MEMO_KEY_BODY));
			notify.setChecked(savedInstanceState.getBoolean(MEMO_KEY_ONGOING_NOTIFICATION));
		}
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		adapter.close();
	}
	
	@Override
	public void onBackPressed () {
		if (title.getText().length() == 0) {
			// If the ID is 0 (new memo) and there is no body, then just cancel the whole thing
			if (id <= 0 && body.getText().length() == 0) {
				super.onBackPressed();
				return;
			}
			
			// Otherwise notify the user that he at least needs a title
			Toast.makeText(getApplicationContext(), R.string.prov_title, Toast.LENGTH_SHORT).show();
			return;
		} else if (!adapter.isTitleAvailable(id, title.getText().toString())) {
			// Fail to save (return) if the title is already taken...forces uniqueness
			Toast.makeText(getApplicationContext(), R.string.title_allredy_tek, Toast.LENGTH_SHORT).show();
			return;
		}
		
		saveState();
		
		// this MUST be called before the super method; the default of the super method is to return a RESULT_CANCELED with NULL data to onActivityResult(...)
		saveResult();
		
		super.onBackPressed();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		// Just in case the UI is killed after a long time on inactivity and the user is put back to this screen by the OS
		outState.putInt(MEMO_KEY_ID, id);
		outState.putBoolean(MEMO_KEY_ONGOING_NOTIFICATION, notify.isChecked());
		outState.putString(MEMO_KEY_TITLE, title.getText().toString());
		outState.putString(MEMO_KEY_BODY, body.getText().toString());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.cancel:
				// Not used anymore, may use it in the future, though
				break;
			case R.id.delete:
				/*
				 * If the memo is blank (and started that way) then it is an effective cancel action. Otherwise we need to alert the user and make sure that
				 * he/she wants to delete the memo.
				 */
				
				if (id == 0 && title.getText().length() == 0 && body.getText().length() == 0) {
					// This is the effective cancel
					finish();
					return true;
				}
				
				// Make the dialog for the confirmation of the deletion
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.confirm_del);
				builder.setMessage(R.string.del_memo_q);
				builder.setPositiveButton(R.string.delete, new OnClickListener() {
					@Override
					public void onClick (DialogInterface dialog, int which) {
						// Positive button confirms the delete
						
						if (id > 0) {
							// Only if the memo was saved in the first place do we actually need to delete
							adapter.deleteMemo(id);
							
							// At this point we should update PREFERENCE_REFRESH_LIST_ON_RESUME to true so the memo list can be updated
							PreferenceManager.getDefaultSharedPreferences(MemoEdit.this).edit().putBoolean(PREFERENCE_REFRESH_LIST_ON_RESUME, true).commit();
						}
						
						Intent i = new Intent();
						i.putExtra(MEMO_KEY_ID, id);
						i.setAction(ACTION_DELETE_MEMO);
						setResult(RESULT_CANCELED, i);
						startService(new Intent(MemoEdit.this, MemoService.class).setAction(ACTION_UPDATE_NOTIFICATION).putExtra(MEMO_KEY_ID, id));
						
						dialog.dismiss();
						finish();
					}
				});
				
				builder.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick (DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				builder.create().show();
				
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Save the current fields to the DB.
	 */
	private void saveState () {
		String title = this.title.getText().toString();
		String body = this.body.getText().toString();
		boolean notify = this.notify.isChecked();
		
		if (id == 0) {
			int id = adapter.createMemo(title, body, notify);
			if (id > 0) {
				this.id = id;
			}
		} else {
			adapter.updateMemo(this.id, title, body, notify);
		}
		
		Intent i = new Intent(this, MemoService.class).setAction(ACTION_UPDATE_NOTIFICATION).putExtra(MEMO_KEY_ID, id);
		if (notify) {
			i.putExtra(MEMO_KEY_ONGOING_NOTIFICATION, notify).putExtra(MEMO_KEY_TITLE, title).putExtra(MEMO_KEY_BODY, body);
		}
		startService(i);
	}
	
	public void saveResult () {
		Intent i = new Intent();
		i.putExtra(MEMO_KEY_ID, id);
		i.putExtra(MEMO_KEY_TITLE, title.getText().toString());
		i.putExtra(MEMO_KEY_ONGOING_NOTIFICATION, notify.isChecked());
		i.putExtra(MEMO_KEY_BODY, body.getText().toString());
		setResult(RESULT_OK, i);
		
		// At this point we should update PREFERENCE_REFRESH_LIST_ON_RESUME to true so the memo list can be updated
		PreferenceManager.getDefaultSharedPreferences(MemoEdit.this).edit().putBoolean(PREFERENCE_REFRESH_LIST_ON_RESUME, true).commit();
	}
}
