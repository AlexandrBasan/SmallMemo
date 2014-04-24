/**
 * 
 */
package com.cbsb.smallmemo;

/**
 * All-you-need memo container! {@link Memo}s are also capable of running a search on themselves.
 * 
 * @
 */
public class Memo {
	/**
	 * Database id for the memo when it is saved. If it is 0 (the default) then the memo has not been saved to the database yet. Row ids for the database are
	 * long values, but I don't forsee anyone making more than 2,147,483,647 memos in their life-time :P
	 */
	private int		id;
	
	/**
	 * Title of the memo.
	 */
	private String	title;
	
	/**
	 * Body text for the memo.
	 */
	private String	body;
	
	/**
	 * True if this memo is to be placed into the status bar for a notification.
	 */
	private boolean	notify;
	
	/**
	 * Construct all the things!
	 * 
	 * @param id
	 *            Database ID
	 * @param title
	 *            Title for the memo
	 * @param body
	 *            Body text for the memo
	 * @param notify
	 *            True if it is to be placed in the status bar
	 */
	public Memo (int id, String title, String body, boolean notify) {
		this.id = id;
		this.title = title;
		this.body = body;
		this.notify = notify;
	}
	
	/**
	 * Make a blank memo
	 */
	public Memo () {
		title = "";
	}
	
	/**
	 * Make a memo with an ID
	 * 
	 * @param id
	 *            Database ID
	 */
	public Memo (int id) {
		this.id = id;
	}
	
	/**
	 * Get the ID.
	 * 
	 * @return id
	 */
	public int getId () {
		return id;
	}
	
	/**
	 * Get the title.
	 * 
	 * @return title
	 */
	public String getTitle () {
		return title;
	}
	
	/**
	 * Get the body.
	 * 
	 * @return body
	 */
	public String getBody () {
		return body;
	}
	
	/**
	 * Get the notify.
	 * 
	 * @return notify
	 */
	public boolean isNotify () {
		return notify;
	}
	
	/**
	 * Set the title
	 * 
	 * @param title
	 *            .
	 */
	public void setTitle (String title) {
		this.title = title;
	}
	
	/**
	 * Set the body.
	 * 
	 * @param body
	 */
	public void setBody (String body) {
		this.body = body;
	}
	
	/**
	 * Set the notify.
	 * 
	 * @param notify
	 */
	public void setNotify (boolean notify) {
		this.notify = notify;
	}
	
	/**
	 * Search this memo based on a bunch of keywords. The keywords array is assumed to be all lowercase and containing no null values!
	 * 
	 * @param keywords
	 *            String array of words or punctuation to search for (remember: all lowercase and no null values)
	 * @return True if this memo matches a basic ANDing search of the keywords in the title and body
	 */
	public boolean search (String[] keywords) {
		/*
		 * Get the title and body into lower case versions - the keywords are guaranteed to be in lower case and we want to do a "case insensitive" match. If
		 * either the title or body is null then we substitute an empty string to make the tests easier.
		 */
		String title = null;
		if (this.title != null)
			title = this.title.toLowerCase();
		else
			title = "";
		
		String body = null;
		if (this.body != null)
			body = this.body.toLowerCase();
		else
			body = "";
		
		/*
		 * Start the search tests
		 */
		// If there is no title or body then we auto fail
		if (title.length() == 0 && body.length() == 0)
			return false;
		
		// Search through the title and body respectively. Title first cuz it's smaller.
		for (int i = 0; i < keywords.length; ++i) {
			if (title.contains(keywords[i]))
				continue;
			else if (body.contains(keywords[i]))
				continue;
			else
				return false;
		}
		
		// If we got here then all keywords were found
		return true;
	}
}
