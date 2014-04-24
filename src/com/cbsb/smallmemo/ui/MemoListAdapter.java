/**
 * 
 */
package com.cbsb.smallmemo.ui;

import java.util.ArrayList;

import com.cbsb.smallmemo.Memo;
import com.cbsb.smallmemo.R;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Custom adapter. This implementation handles search queries and list updates seamlessly.
 * 
 * @
 */
public class MemoListAdapter implements ListAdapter {
	public static final String			TAG						= "MemoListAdapter";
	private LayoutInflater				inflater;
	
	/*
	 * Notify or un-notify the memo in the status bar.
	 */
	private OnCheckedChangeListener		checkBoxChangeListener	= new OnCheckedChangeListener() {
																	@Override
																	public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
																		Integer id = (Integer) buttonView.getTag();
																		memoLauncher.setNotifyStatus(id, isChecked);
																	}
																};
	
	/**
	 * The full data set of memos.
	 */
	private ArrayList<Memo>				data;
	
	/**
	 * The data set of memos that are displayed to the user. If there is no search, this will be exactly equal to {@link #data}. If there is a search query then
	 * only matching items will be in the {@link #visibleData}.
	 */
	private ArrayList<Memo>				visibleData;
	
	/**
	 * Cache the previous query. We can increase performance on the next query as long as the next query is an expansion on the first query.
	 */
	private String						previousQuery			= null;
	
	/**
	 * Link back to the {@link MemoLauncher}. We need this link so we can update the notification status of memos when the user clicks on the checkbox in a row
	 * of the {@link ListView}.
	 */
	private MemoLauncher				memoLauncher;
	
	/**
	 * Container for those objects needing to know when the data in the adapter has been changed (new memo, edit, delete, or search).
	 */
	private ArrayList<DataSetObserver>	observers				= new ArrayList<DataSetObserver>(1);
	
	/**
	 * Make a new adapter with a blank set of data.
	 * 
	 * @param launcher
	 */
	public MemoListAdapter (MemoLauncher launcher) {
		this(launcher, new ArrayList<Memo>());
	}
	
	/**
	 * Create with an existing set of data.
	 * 
	 * @param launcher
	 *            Must have this!
	 * @param list
	 *            Must have this, too!
	 */
	public MemoListAdapter (MemoLauncher launcher, ArrayList<Memo> list) {
		if (launcher == null)
			throw new NullPointerException("The MemoLauncher cannot be null!");
		
		if (list == null)
			throw new NullPointerException("The data set cannot be null!");
		
		this.memoLauncher = launcher;
		this.inflater = launcher.getLayoutInflater();
		
		setMemos(list);
	}
	
	/**
	 * Set the list of memos in memory. If there was a search conducted and the search has not been cleared, re-run the search to update the visible list. All
	 * observers of this list are notified of the changes.
	 * 
	 * @param list
	 *            List of memos; cannot be null!
	 */
	public void setMemos (ArrayList<Memo> list) {
		if (list == null)
			throw new NullPointerException("The data set cannot be null!");
		
		this.data = list;
		
		// Optimize: only create the visible list if we need to, otherwise clear and re-update
		if (this.visibleData == null) {
			this.visibleData = new ArrayList<Memo>(this.data);
		} else {
			this.visibleData.clear();
			this.visibleData.addAll(this.data);
		}
		
		// Re-run the search if the list has been queried
		if (previousQuery != null && previousQuery.length() > 0) {
			String searchString = previousQuery;
			previousQuery = null;
			query(searchString);
		}
		
		notifyObservers();
	}
	
	/**
	 * Add a memo to the data set.
	 * 
	 * @param memo
	 *            Memo to add to the list
	 * @return True always
	 */
	public boolean addMemo (Memo memo) {
		return placeInOrder(memo);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ListAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled () {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ListAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled (int position) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount () {
		return this.visibleData.size();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem (int position) {
		return visibleData.get(position);
	}
	
	/**
	 * Get a memo based on a memo ID.
	 * 
	 * @param memoId
	 * @return Memo if found or null on failure.
	 */
	public Memo getMemo (int memoId) {
		for (int i = data.size() - 1; i >= 0; --i) {
			if (data.get(i).getId() == memoId) {
				return data.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Remove a memo if it is found.
	 * 
	 * @param memoId
	 * @return Memo that was removed or null if the memo was not found.
	 */
	public Memo removeMemo (int memoId) {
		// Remove from the visible items
		for (int i = 0; i < visibleData.size(); ++i) {
			if (visibleData.get(i).getId() == memoId)
				visibleData.remove(i);
		}
		
		// Remove from the complete data set
		for (int i = data.size() - 1; i >= 0; --i) {
			if (data.get(i).getId() == memoId) {
				return data.remove(i);
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId (int position) {
		return visibleData.get(position).getId();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType (int position) {
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		Memo memo = visibleData.get(position);
		CheckBox cb = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.memo_row, parent, false);
		}
		
		cb = (CheckBox) convertView.findViewById(R.id.itemNotify);
		
		// The old view will have a listener, remove it to stop random notify updates when re-using views
		cb.setOnCheckedChangeListener(null);
		
		((TextView) convertView.findViewById(R.id.itemTitle)).setText(memo.getTitle());
		cb.setChecked(memo.isNotify());
		cb.setOnCheckedChangeListener(checkBoxChangeListener);
		cb.setTag(memo.getId());
		convertView.setTag(memo.getId());
		
		return convertView;
	}
	
	/**
	 * Update a memo in the data set and its display (if we find it).
	 * 
	 * @param id
	 *            ID of the memo to update
	 * @param title
	 *            New title of the memo
	 * @param body
	 *            New body text of the memo
	 * @param notitfy
	 *            New update notification status of the memo
	 */
	public void updateMemo (int id, String title, String body, boolean notify) {
		Memo m = removeMemo(id);
		if (m == null)
			return;
		
		m.setTitle(title);
		m.setBody(body);
		m.setNotify(notify);
		
		placeInOrder(m);
	}
	
	/**
	 * Convenience method for the longer updateMemo(long, String, String, boolean) version.
	 * 
	 * @param m
	 */
	public void updateMemo (Memo m) {
		updateMemo(m.getId(), m.getTitle(), m.getBody(), m.isNotify());
	}
	
	/**
	 * Update the notification status of a memo.
	 * 
	 * @param id
	 *            ID of the memo to update
	 * @param notify
	 *            New notification status
	 */
	public void updateMemoNotificationStatus (int id, boolean notify) {
		getMemo(id).setNotify(notify);
	}
	
	/**
	 * Place a memo in alphabetical order within the list. Note: when we get the data from the DB it is already in order, so the only sorting we need to do is a
	 * single insertion.
	 * 
	 * @param m
	 * @return True always
	 */
	public boolean placeInOrder (Memo m) {
		// Place the edited memo in alphabetical order
		boolean added = false;
		int size = data.size();
		for (int i = 0; i < size; ++i) {
			if (data.get(i).getTitle().compareTo(m.getTitle()) > 0) {
				data.add(i, m);
				added = true;
				break;
			}
		}
		
		// In case it is the first or the lowest in the alphabet, add to the end
		if (!added) {
			added = true;
			data.add(m);
		}
		
		// Update the visible list
		String query = previousQuery;
		resetSearch();
		query(query);
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount () {
		return 1;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds () {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#isEmpty()
	 */
	@Override
	public boolean isEmpty () {
		return data.size() == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver (DataSetObserver observer) {
		observers.add(observer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver (DataSetObserver observer) {
		observers.remove(observer);
	}
	
	/**
	 * Refresh the visible state of the list on the UI.
	 */
	public void notifyObservers () {
		for (int i = 0; i < observers.size(); ++i) {
			observers.get(i).onChanged();
		}
	}
	
	/**
	 * Search for memos that match the query string. The query string is broken into an array of keywords and individual punctuation mark (if available). The
	 * array is then trimmed down to eliminate any duplicate, null, or zero-length items. During this process the keywords are transferred into lower-case.
	 * 
	 * @param query
	 */
	public void query (String query) {
		// Make sure the query is not null
		if (query == null)
			query = "";
		
		// Benchmark the timing. Need to keep the used time down!
		long start = System.currentTimeMillis();
		
		// Go to lower-case so we can do a case-insensitive search in the memos
		query = query.toLowerCase();
		
		// Trim down the keywords to a limited set of unique values
		String[] keywords = refineKeywords(query.trim().split("(\\b| )"));
		
		/*
		 * Compare this query to the previous query. We can optimize the search for the new valid items by searching the old results if the new query is an
		 * extended query from before (if the newest character was added to the right-end of the previous one).
		 */
		if (query == null || query.equals("") || (previousQuery != null && (query.length() <= previousQuery.length() || query.indexOf(previousQuery) != 0))) {
			// Show all if the query was reset or if it is not an addon from the previous one
			resetSearch();
		}
		
		if (keywords.length > 0) {
			// Filter available memos
			
			for (int i = 0; i < visibleData.size(); ++i) {
				if (!visibleData.get(i).search(keywords)) {
					// Unmatching items are removed and i is decremented (or we would technically skip the next value)
					visibleData.remove(i--);
				}
			}
		}
		
		// Update the visible list(s)
		notifyObservers();
		
		// Store the previous query
		previousQuery = query;
		
		// Log the benchmark to LogCat
		Log.d(TAG, "Query Benchmark: " + (System.currentTimeMillis() - start) + "ms");
	}
	
	/**
	 * Make all memos visible to the user and reset the caches for searches.
	 */
	private void resetSearch () {
		visibleData.clear();
		for (int i = 0; i < data.size(); ++i) {
			visibleData.add(data.get(i));
		}
		previousQuery = "";
	}
	
	/**
	 * Refine a keyword array set into a smaller set with the garbage values removed. We want to remove empty strings, whitespace, and duplicate values.
	 * 
	 * @param keywords
	 *            Original String array to refine
	 * @return Refined keyword String array
	 */
	private String[] refineKeywords (String[] keywords) {
		String[] refined = new String[keywords.length];
		int numIndicies = 0;
		
		// Loop through all keyword entries in the original keywords string array. Only copy over unique values and values that have a length > 0
		for (int i = 0; i < keywords.length; ++i) {
			// Get rid of whitespace
			keywords[i] = keywords[i].trim();
			
			if (keywords[i].length() > 0) {
				if (!foundIn(refined, keywords[i])) {
					refined[numIndicies++] = keywords[i];
				}
			}
		}
		
		// Get rid of null entries
		String[] finalVersion = new String[numIndicies];
		for (int i = 0; i < numIndicies; ++i) {
			finalVersion[i] = refined[i];
		}
		
		return finalVersion;
	}
	
	/**
	 * Search a String array for a specific value.
	 * 
	 * @param search
	 *            Array to search
	 * @param value
	 *            Value to find
	 * @return True if found, false otherwise.
	 */
	private boolean foundIn (String[] search, String value) {
		for (int i = 0; i < search.length; ++i) {
			if (search[i] != null && search[i].equals(value))
				return true;
		}
		
		return false;
	}
}
