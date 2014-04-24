/**
 * 
 */
package com.cbsb.smallmemo.ui;

import com.cbsb.smallmemo.R;
import com.cbsb.smallmemo.util.Codes;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * Display the full GPL license.
 * 
 * @
 */
public class LicenseDisplay extends Activity implements Codes {
	/**
	 * {@link CheckBox} for the display on startup feature.
	 */
	private CheckBox	mCheckBox;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the layout
		setContentView(R.layout.license);
		
		// For some reason, the dialog window doesn't want to stretch to max width even though we have a gigantic amount of text...this fixes that
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		/*
		 * Text Display
		 */
		// Format the string as HTML and display in the text view
		TextView tv = (TextView) findViewById(R.id.license);
		Resources res = getResources();
		tv.setText(Html.fromHtml(res.getString(R.string.license_disclaimer)));
		
		// Movement method is needed so the user can actually click on TextView links
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		
		/*
		 * Controls
		 */
		// Link to full version of the license
		tv = (TextView) findViewById(R.id.view_full_license);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(Html.fromHtml(res.getString(R.string.view_full_license)));
		
		// Set the initial state of the checkbox for the display on startup preference
		mCheckBox = (CheckBox) findViewById(R.id.display_on_startup);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFERENCE_DISPLAY_LICENSE_AT_STARTUP, true)) {
			mCheckBox.setChecked(true);
		}
		
		// Attach a check changed listener to update the preference as soon as it is clicked
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
				PreferenceManager.getDefaultSharedPreferences(LicenseDisplay.this).edit().putBoolean(PREFERENCE_DISPLAY_LICENSE_AT_STARTUP, isChecked)
						.commit();
			}
			
		});
	}
}
