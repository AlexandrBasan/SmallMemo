/**
 * 
 */
package com.cbsb.smallmemo.ui;

import com.cbsb.smallmemo.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Displays information about this application. Also provides information on where to get the source code.
 * 
 * @ 
 */
public class About extends Activity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Create views
		setContentView(R.layout.about);
		
		// Get the version number and set the version text view
		try {
			((TextView) findViewById(R.id.version)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// should never get here; we are querying this application's package name
			((TextView) findViewById(R.id.version)).setText("...cannot find version...");
		}
		
		// Set the HTML formatted text and make sure the user can click on the link for the license
		TextView tv = (TextView) findViewById(R.id.copyleft_notice);
		tv.setText(Html.fromHtml(getResources().getString(R.string.copyleft)));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		
		// Clicking the button should allow the user to go to the market page for this app...to get ratings :)
		findViewById(R.id.rating_bar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.alexhilman.memoPad"));
				startActivity(intent);
			}
		});
		
		// Send the user to sourceforge to view the app "homepage" and sourcecode
		findViewById(R.id.get_source_code).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://sourceforge.net/p/memopad/")));
			}
		});
	}
}
