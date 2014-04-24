/**
 * 
 */
package com.cbsb.smallmemo.ui;

import com.cbsb.smallmemo.R;
import com.cbsb.smallmemo.service.MemoService;
import com.cbsb.smallmemo.util.Codes;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * Contains all of the preferences and settings for MemoPad. Each setting updates immediately based on the user interaction.<br/>
 * <br/>
 * Contained preferences:
 * <ol>
 * <li>Display License on Startup</li>
 * <li>View the License (just a link to the GPL 3.0 documentation online)</li>
 * <li>New Memo Notification</li>
 * </ol>
 * 
 * @
 */
public class Preferences extends PreferenceActivity implements Codes {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load layout from XML
		addPreferencesFromResource(R.xml.preferences);
		
		// Set the view license pref to open a browser and display the full license
	Resources r = getResources();
	findPreference(r.getString(R.string.pref_view_license)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
		@Override
		public boolean onPreferenceClick (Preference preference) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://www.gnu.org/licenses/gpl.html"));
			startActivity(intent);
			return true;
		}
			
	}
	);
		
		// Enable/Disable the actual notification when the setting changes
		findPreference(r.getString(R.string.pref_new_memo_notification)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange (Preference preference, Object newValue) {
				startService(new Intent(Preferences.this, MemoService.class).setAction(ACTION_NEW_MEMO_NOTIFICATION).putExtra(
						PREFERENCE_NEW_MEMO_AS_NOTIFICATION, newValue.equals(new Boolean(true))));
				return true;
			}
			
		});
	}
}
