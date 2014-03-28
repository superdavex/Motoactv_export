package com.sdsoft.motoactv_export;

/*
 * SDSOFT Motoactv Exporter 
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.support.v4.app.NavUtils;


public class WebActivity extends Activity {
	private static String TAG = "sd_motoactv_export";
	private UpdateUtil.AuthType AuthType;
	private ProgressDialog mProgress;
	 
	private SharedPreferences shared_pref;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_web);
        
        
        // Loads html-string into webview
       // browser.loadData("<html><body><strong>Loading....</body></html>", "text/html", "UTF-8");
        
       
      int itemp = getIntent().getIntExtra("auth_type", 0);
  	  
  	  if(itemp >= 0 && itemp < AuthType.values().length)
  		  this.AuthType = AuthType.values()[itemp];
  	  
  	  Log.d(TAG,"extra=" + this.AuthType);

  	  
  	shared_pref = PreferenceManager.getDefaultSharedPreferences(this);
  	
  	switch(this.AuthType )
  	{
  	case RUNKEEPER:
  		if(isOnline(true))
  			getRunKeeperAuth();

  		break;
  	case GOOGLEDRIVE:
  		if(isOnline(true))
  			getGoogleDriveAuth();

  		break;
  	case DROPBOX:
  		if(isOnline(true))
  			getDropboxAuth();
  		break;
  	}
  	
  	// Close form if not online.
  	if(! isOnline(false))
  	{
  		finish();
  	}
       // Intent returnIntent = new Intent();
      //  returnIntent.putExtra("result",browser);
       // setResult(RESULT_OK,returnIntent);     
        //finish();
        
    }
    

    private boolean getRunKeeperAuth()
    {
		RunKeeperUpdate rk = new RunKeeperUpdate(null,shared_pref);
    	rk.getAuthToken((WebView) findViewById(R.id.webview), this);  
    	
    	return true;
    	
    }
    
    private boolean getDropboxAuth()
    {
		DropboxUpdate db = new DropboxUpdate(null,shared_pref);	
		db.getAuthToken((WebView) findViewById(R.id.webview),this);
		
    	return true;
    	
    }
    
    private boolean getGoogleDriveAuth()
    {
    	GoogleDriveUpdate gd = new GoogleDriveUpdate(null,shared_pref);
		gd.getAuthToken((WebView) findViewById(R.id.webview),this);
		
    	return true;
    	
    }
    
    BroadcastReceiver WorkoutCompleted = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) { 
    	String str = intent.getAction();
    	Log.d(TAG, str);
    	

    }
    };
    
    public boolean promptforWifi()
	{
		if( ! isOnline(false) )	
		{
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			if( wifiManager.getWifiState() !=3  )
			{
				new AlertDialog.Builder(this)
				.setTitle("Enable WiFi.")
				.setMessage("Internet connection needed, would you like to enable WiFi?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				    public void onClick(DialogInterface dialog, int whichButton) {
				    	WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				    	 wifiManager.setWifiEnabled(true);
				    	 //UpdateUtil.EnableWifi(getBaseContext());
				    }})
				 .setNegativeButton(android.R.string.no, null).show();
			}
		}
		return true;
	}
    
    public boolean isOnline(boolean bAlert) {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
    	if(bAlert)
    	{
    		Toast.makeText(WebActivity.this, "Internet connection required, process aborted.", Toast.LENGTH_SHORT).show();
    	}
	    
	    return false;
	}

}