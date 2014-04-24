package com.cbsb.smallmemo.ui;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.R;
import com.cbsb.smallmemo.database.MemoAdapter;
import com.cbsb.smallmemo.service.MemoService;
import com.cbsb.smallmemo.util.Codes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Main starting place for the app. This is the {@link Activity} where all on the system are listed in a {@link ListView}. We also use a custom
 * {@link ListAdapter} (specifically {@link MemoListAdapter}) which provides a search feature and special layouts for each row holding a memo. That special row
 * contains a check-box to allow easy notification settings.
 * 
 * @
 */
public class MemoLauncher extends Activity implements Codes {
	public static final String	TAG			= "MainLauncher";
	
	/**
	 * Status placeholder so we know whether or not we are in "search" mode. If we are in search mode, the user will have a Search field placed overtop of the
	 * "New Memo" bar.
	 */
	private boolean				searchMode	= false;
	
	/**
	 * Layout container for the New Memo field.
	 */
	private RelativeLayout		newMemo;
	
	/**
	 * Layout container for the Search facility.
	 */
	private RelativeLayout		search;
	
	/**
	 * {@link EditText} containing the search query. This query should be done in a simple AND fashion.
	 */
	private TextView			filter;
	
	/**
	 * The clickable image beside the search field. If clicked this will erase the query and show all memos.
	 */
	private ImageView			filterClearer;
	
	/**
	 * Connection to the DB.
	 */
	private MemoAdapter			adapter;
	
	/**
	 * The custom {@link ListAdapter} for holding the memos. Also searches the list of available memos.
	 */
	private MemoListAdapter		memoListAdapter;
	
	/**
	 * {@link ListView} for the memos on the system and / or matching search results.
	 */
	private ListView			listTank;
	
	/**
	 * Used to show or hide the keyboard when entering or leaving search mode.
	 */
	private InputMethodManager	inputMethodManager;
	
	/**
	 * Animation for showing the search field when entering search mode.
	 */
	private Animation			flyRevealDown;
	
	/**
	 * Animation for fading out the new memo bar when entering search mode.
	 */
	private Animation			fadeOut;
	
	/**
	 * Animation for hiding the search bar when leaving search mode.
	 */
	private Animation			flyConcealUp;
	
	/**
	 * Animation for fading in the new memo bar when leaving search mode.
	 */
	private Animation			fadeIn;
	
	/**
	 * Used to inflate the options menu and context menus.
	 */
	private MenuInflater		menuInflater;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set resource view from XML
		setContentView(R.layout.main);
		
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/Dungeon.TTF");
		TextView tv = (TextView) findViewById(R.id.newMemoText);
		ListView nml = (ListView) findViewById(R.id.memoList);
		
		tv.setTypeface(tf);


		
		
		
		
		
		// Get the memo list object and set the click listener for each row
		listTank = (ListView) findViewById(R.id.memoList);
		listTank.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick (AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				editMemo(((Integer) arg1.getTag()).intValue());
			}
		});
		
		// Make sure the rows of the list view can run a context menu when long-pressed
		registerForContextMenu(listTank);
		
		// Grab the new memo view and attach the click event to make a new memo (when clicked :P)
		newMemo = (RelativeLayout) findViewById(R.id.newMemo);
		newMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				// Launch the memo edit activity with the "create" flag.
				newMemo();
			}
		});
		
		// Grab the search bar and its contents
		search = (RelativeLayout) findViewById(R.id.searchContainer);
		filter = (TextView) search.findViewById(R.id.filter);
		
		// This text watcher enables us to run the query on every key-press
		filter.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged (CharSequence s, int start, int before, int count) {
				queryList(s.toString());
			}
			
			@Override
			public void beforeTextChanged (CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged (Editable s) {
			}
		});
		
		// Grab the search clearer
		filterClearer = (ImageView) search.findViewById(R.id.filterClearer);
		
		// Attach on click event for filter clearer; it should clear the search query and return the list to normal
		filterClearer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				filter.setText("");
				// The text watcher will run the query when this text is reset
			}
		});
		
		// Get input method manager
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		
		/*
		 * Open the DB
		 */
		adapter = new MemoAdapter(this);
		adapter.open();
		
		// Check if we need to display the license on start-up
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFERENCE_DISPLAY_LICENSE_AT_STARTUP, false)) {
		//	startActivity(new Intent(this, LicenseDisplay.class));
		}
		
		// Get all the things!
		fillMemoList();
	}
	
	@Override
	protected void onStart () {
		super.onStart();
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		
		// Do we need to refresh the list?
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFERENCE_REFRESH_LIST_ON_RESUME, false)) {
			// Refresh list
			fillMemoList();
			
			// flag is cancelled in fullMemoList()
		}
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		adapter.close();
	}
	
	@Override
	public boolean onContextItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.deleteMemo:
				// Delete the memo that was long-pressed
				AdapterContextMenuInfo mMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int id = ((Integer) mMenuInfo.targetView.getTag()).intValue();
				deleteMemo(id);
				break;
		}
		return true;
	}
	
	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (menuInflater == null)
			menuInflater = new MenuInflater(this);
		
		// Open the context menu for this memo
		menuInflater.inflate(R.menu.context_menu, menu);
		Memo m = adapter.fetchMemo(((Integer) ((AdapterContextMenuInfo) menuInfo).targetView.getTag()).intValue());
		
		// Add a title to the context menu (the memo's title) so the user knows which one he/she is deleting
		menu.setHeaderTitle("\"" + m.getTitle() + "\"");
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		if (menuInflater == null)
			menuInflater = new MenuInflater(this);
		
		menuInflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search:
				// Only enter search mode, don't exit with this feature
				if (!searchMode)
					toggleSearch();
				return true;
				
			case R.id.menu_preferences:
				// Load the settings activity
				startActivity(new Intent(this, Preferences.class));
				return true;
				
			case R.id.menu_about:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://proalab.com/?page_id=517"));
				startActivity(browserIntent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Fill the {@link MemoListAdapter} with memo info and display the list in the {@link ListView} ({@link #listTank}). This is the last step of the creation
	 * process for this activity.
	 */
	public void fillMemoList () {
		// Make the adapter for the data set
		// TODO Make this a background call? In all of my tests the fetch is done extremely quickly (even with 100 memos) so I think this is ok
		if (memoListAdapter == null) {
			memoListAdapter = new MemoListAdapter(this, adapter.fetchAllMemos(true));
			
			// Set the adapter
			listTank.setAdapter(memoListAdapter);
		} else {
			// Just re-set the memos if we are resuming with a refresh preference
			memoListAdapter.setMemos(adapter.fetchAllMemos(true));
		}
		
		// Cancel the flag for automatic refresh if there is any
		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREFERENCE_REFRESH_LIST_ON_RESUME, false).commit();
	}
	
	/**
	 * Launch the {@link MemoEdit} activity with no intent data. The database operations will take place in {@link MemoEdit}.
	 */
	public void newMemo () {
		Intent i = new Intent(this, MemoEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE_MEMO);
	}
	
	/**
	 * Launch the {@link MemoEdit} activity with the ID of the memo we are going to edit. The actual saving of the changed data is done by the {@link MemoEdit}
	 * activity (and the new data is also passed back to this activity so we may efficiently update the list without another database read).
	 * 
	 * @param id
	 *            ID of the memo we are going to edit
	 */
	public void editMemo (int id) {
		Intent i = new Intent(this, MemoEdit.class);
		i.putExtra(MemoAdapter.MEMO_KEY_ID, id);
		startActivityForResult(i, ACTIVITY_EDIT_MEMO);
	}
	
	/**
	 * Deletes a memo from the database and updates the memo {@link ListView}.
	 * 
	 * @param id
	 */
	public void deleteMemo (int id) {
		// First, delete from storage
		adapter.deleteMemo(id);
		
		// Remove it from the ListView
		memoListAdapter.removeMemo(id);
		memoListAdapter.notifyObservers();
		
		// Update the notification for this memo (this call will just remove the notification if it exists)
		startService(new Intent(this, MemoService.class).setAction(ACTION_UPDATE_NOTIFICATION).putExtra(MEMO_KEY_ID, id));
	}
	
	/**
	 * Change the notification status of an existing {@link Memo}. This will call the intent service and signal it to check the {@link Memo} for a notification
	 * setting. Any existing notification will be removed.
	 * 
	 * @param id
	 * @param enabled
	 */
	public void setNotifyStatus (int id, boolean enabled) {
		// Store the new notification; persist it to the database.
		adapter.updateNotify(id, enabled);
		
		// Update the in-memory content (held in the list adapter) so the Memo object reflects the new status
		memoListAdapter.updateMemoNotificationStatus(id, enabled);
		
		// Update the status bar with the new notification setting; if the notification has been removed then we don't need to add the notification details
		// (title, body, etc)
		Intent i = new Intent(this, MemoService.class).setAction(ACTION_UPDATE_NOTIFICATION).putExtra(MEMO_KEY_ID, id);
		if (enabled) {
			i.putExtra(MEMO_KEY_ONGOING_NOTIFICATION, memoListAdapter.getMemo(id).isNotify()).putExtra(MEMO_KEY_TITLE, memoListAdapter.getMemo(id).getTitle())
					.putExtra(MEMO_KEY_BODY, adapter.getMemoBody(id));
		}
		
		// Call the intent service to update the notification
		startService(i);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_EDIT_MEMO && resultCode == RESULT_CANCELED && data != null && data.getAction() != null
				&& data.getAction().equals(ACTION_DELETE_MEMO)) {
			// This will only be called if the MemoEdit activity had it's menu option of "Delete" selected
			// In that case we should remove the memo from the in-memory list (the DB state will have already been updated)
			memoListAdapter.removeMemo(data.getIntExtra(MEMO_KEY_ID, 0));
			memoListAdapter.notifyObservers();
		} else {
			switch (requestCode) {
				case ACTIVITY_CREATE_MEMO:
				case ACTIVITY_EDIT_MEMO:
					// By this time a memo has already been created/edited and is already in the DB; we just need to persist the changes to the in-memory list
					// in the memoListAdapter
					if (resultCode == RESULT_OK) {
						Memo m = memoListAdapter.getMemo(data.getIntExtra(MEMO_KEY_ID, 0));
						
						/*
						 * If the memo is null then there was a newly created memo, otherwise it was edited. In either case the MemoEditActivity sends the new
						 * memo's info (id, title, body, and persistent notification bool) to the calling activity (this will save a DB look-up)
						 */
						if (m == null) {
							// New memo
							m = new Memo(data.getIntExtra(MEMO_KEY_ID, 0));
							m.setTitle(data.getStringExtra(MEMO_KEY_TITLE));
							m.setNotify(data.getBooleanExtra(MEMO_KEY_ONGOING_NOTIFICATION, false));
							
							memoListAdapter.addMemo(m);
						} else {
							// Update fields
							m.setTitle(data.getStringExtra(MEMO_KEY_TITLE));
							m.setNotify(data.getBooleanExtra(MEMO_KEY_ONGOING_NOTIFICATION, false));
							
							memoListAdapter.updateMemo(m);
						}
						
						// Notify the any ListViews that are watching this adapter that data has changed.
						memoListAdapter.notifyObservers();
					}
					return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onSearchRequested () {
		// Only turn the search on with this feature, makes it similar to the normal google search done from the home screen
		if (!searchMode)
			toggleSearch();
		return true;
	}
	
	/**
	 * Open or close the search capability of the memo list.
	 */
	private void toggleSearch () {
		searchMode = !searchMode;
		
		if (searchMode) {
			// Go into search mode
			/*
			 * Tween in the search bar and keep visible
			 */
			if (flyRevealDown == null)
				flyRevealDown = AnimationUtils.loadAnimation(this, R.anim.fly_reveal_down);
			search.startAnimation(flyRevealDown);
			search.setVisibility(View.VISIBLE);
			filter.requestFocus();
			
			/*
			 * Tween out the new memo bar
			 */
			if (fadeOut == null)
				fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
			newMemo.startAnimation(fadeOut);
			newMemo.setVisibility(View.GONE);
			
			/*
			 * Show the keyboard
			 */
			inputMethodManager.showSoftInput(filter, 0);
		} else {
			// Go out of search mode
			/*
			 * Hide the keyboard
			 */
			inputMethodManager.hideSoftInputFromInputMethod(filter.getWindowToken(), 0);
			
			/*
			 * Tween out the search bar and keep invisible
			 */
			if (flyConcealUp == null)
				flyConcealUp = AnimationUtils.loadAnimation(this, R.anim.fly_conceal_up);
			filter.setText("");
			search.startAnimation(flyConcealUp);
			search.setVisibility(View.GONE);
			
			/*
			 * Tween in the new memo bar
			 */
			if (fadeIn == null)
				fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			newMemo.startAnimation(fadeIn);
			newMemo.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onRestart () {
		super.onRestart();
		
		// In case the user made a search, edited a memo, and changed the memo so it doesn't match the new search items we must re-query the list
		if (filter.getVisibility() == View.VISIBLE)
			queryList(filter.getText().toString());
	}
	
	/**
	 * Search for memos with this in their title and/or body.
	 * 
	 * @param query
	 */
	private void queryList (String query) {
		memoListAdapter.query(query);
	}
	
	@Override
	public void onBackPressed () {
		// Make sure we back-out of search mode before quitting the app
		if (searchMode) {
			toggleSearch();
			return;
		}
		super.onBackPressed();
	}
}