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

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Log;
import com.sdsoft.motoactv_export.R;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener{


   private static String TAG = "sd_motoactv_export";
   private Spinner spinner1;
   private static int buildMajor = 1;
   private static int buildMinor = 12;
   private boolean bLaunchingSettings = false;

   private SharedPreferences shared_pref = null;
   
  /* BroadcastReceiver WorkoutCompleted = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) { 
	    	Log.e("MainActivity", context.toString(),null);
	    	
	    	getData();
	    }
	};*/
	@Override
	public void onNewIntent(Intent intent)
	{
	  super.onNewIntent(intent);
	  Log.d(TAG,"new" );
	  int iextra = intent.getIntExtra("com.sdsoft.motoactc_export.SERVICE_EXPORT", 0);
	  
	  Log.d(TAG,"extra=" + iextra);
	  // Why isn't this performed by the framework in the line above?
	  setIntent(intent);
	}
	
   private static final SimpleDateFormat ISO_DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    
   private void CheckServiceCalledUpate()
   {
	   int needs_updating = 0;
	   
	   try
		{
		needs_updating = shared_pref.getInt("needs_updating", 0);
		}
		catch( Exception e)
		{
			needs_updating=0;
		}
		
		
		// Check if an updated is needed
		if(needs_updating > 0)
		{
		   Log.d(TAG,"Main OnCreate-Needs Updating");
		   
		   if (! isOnline(false) )
				return;
		   
		   DataUtil DU = new DataUtil(getContentResolver());
		   
			// Runkeeper Update
			if(  shared_pref.getBoolean("pref_use_runkeeper", false) )
			{
				RunKeeperUpdate rk = new RunKeeperUpdate(DU, shared_pref);
				rk.Facebook = shared_pref.getBoolean("pref_use_runkeeper_facebook", false);    		 
				boolean ret=rk.UpdateAll(needs_updating);
			
				if (! ret)
				{
					Log.d(TAG,"RK Service Need New Token");
					//StartMain();
				}
			}
			
			// Dropbox Update
			if( shared_pref.getBoolean("pref_use_dropbox", false) )
			{
				DropboxUpdate db = new DropboxUpdate(DU,shared_pref);//,getBaseContext()
				boolean ret=db.UpdateAll(needs_updating);
				
				if (! ret)
				{
					Log.d(TAG,"DB Service Need New Token");
					//StartMain();
	   		 	}
			}
		
		}
   }

   
   @Override
   public void onStop()
   {
	   super.onStop();
	   // handle attempt to close when settings is launched.
	   if( ! bLaunchingSettings && shared_pref.getBoolean("pref_disable_wifi_onexit", true))
	   {
		   WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
		   wifiManager.setWifiEnabled(false);
		   Log.d(TAG, "Wifi Disabled" );
	   }
	   else
	   {
		   bLaunchingSettings = false;
	   }
   }
    @Override
    public void onCreate(Bundle savedInstanceState) {
    

	    shared_pref = PreferenceManager.getDefaultSharedPreferences(this);
	    
    	Log.d(TAG, "Version " + buildMajor + "." + buildMinor );
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pefs
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
       
        ArrayList<String> options=new ArrayList<String>();

        try
        {
	        Cursor cworkout = getContentResolver().query(DataUtil.CONTENT_URI_WORKOUT_ACTIVITY, null, "",null, null);
	        //getContentResolver().insert(CONTENT_URI_WORKOUT_ACTIVITY, values)
	        while(cworkout.moveToNext())
	        {
	        	String id = cworkout.getString(cworkout.getColumnIndex("_id"));
	        	Cursor csum =getContentResolver().query(DataUtil. CONTENT_URI_WORKOUT_ACTIVITY_SUMMARY, null, "workout_activity_id = " + id + " and metric_id = 4",null, null);
	        	csum.moveToNext();
	        	double dDistance =csum.getDouble(csum.getColumnIndex("summary_value")) * 0.000621371;
	        	String sDistance = String.format( "%.2fM", dDistance ) ; 
	        	//Log.d(TAG, sDistance);
	        	csum.close();
	        	
	        	options.add(id + " " +  			ISO_DATE_TIME_FORMAT.format(cworkout.getLong(cworkout.getColumnIndex("start_time"))) + " " + sDistance );
	        
	        }
        }
        catch(Exception e)
        {
        	Log.d(TAG,"Error loading activities:"+ e.toString());
        	
        }

        // use default spinner item to show options in spinner
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_item,options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setSelection(spinner1.getCount() -1);

		promptforWifi();
		
		// Check whether needs a service update.
		CheckServiceCalledUpate();
		
		// Set wifi button state
		SetWifiButton(true,false);


    }
   	public void SetWifiButton(boolean bUseManager,boolean bState)
   	{
   		// Set wifi button state
   		try
        {
   			Button p1_button = (Button)findViewById(R.id.Button02);
   			if(bUseManager)
   			{
		   		WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);

				if(wifiManager.isWifiEnabled())
					p1_button.setText("Disable WiFi");
				else
					p1_button.setText("Enable WiFi");
   			}
   			else
   			{
   				if(bState)
   					p1_button.setText("Enable WiFi");
   				else
   					p1_button.setText("Disable WiFi");
   			}
        }
   		catch(Exception e)
   		{
   			
   		}
   		
   	}
   	
		public boolean promptforWifi()
		{
			if( ! isOnline(false) )
			{
				WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				if( wifiManager.getWifiState() !=3  )
				{
					if( shared_pref.getBoolean("pref_use_runkeeper", false) || shared_pref.getBoolean("pref_use_dropbox", false) ||
								shared_pref.getBoolean("pref_use_googledrive", false) )
					{
						Log.d(TAG,"Prompt Wifi");
						AlertDialog dl = new AlertDialog.Builder(this)
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
						
						dl.getWindow().setGravity(Gravity.TOP);
					}
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
    		Toast.makeText(MainActivity.this, "Internet connection required, process aborted.", Toast.LENGTH_SHORT).show();
	    
	    return false;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	Log.d(TAG, "exit");
            	
            	startActivity(new Intent(this, SettingsActivity.class));
            	//Intent intent = new Intent(Intent.ACTION_MAIN);
            	//intent.addCategory(Intent.CATEGORY_HOME);
            	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	//startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
    	   public void run() {
    		   Button butExport=(Button)findViewById(R.id.button1);
    		   butExport.setText("Export");  
    		   butExport.setEnabled(true);
    	   }
    	};
	 private Runnable mExport_data = new Runnable() {
	   public void run() {
		   try
		   {
		   int id = 0;

		   // Dont do on emulator
		   //if(! Build.FINGERPRINT.startsWith("generic") )
		  // {
		   String[] sID = spinner1.getSelectedItem().toString().split(" ");
		   id = Integer.parseInt( sID[0]);
		   
		  // }
			  
		   
		   DataUtil DU = new DataUtil(getContentResolver());
		   
	 		 
		   Log.d(TAG, "Manual Export");
		   
		   boolean reta = shared_pref.getBoolean("pref_local_TCX", true);
	    	 if(reta)
	    	 {
	    		 Log.d(TAG, "Exporting TCX");
	    		 DU.exportTCX(id,false);
	    	 }
	    	 
	    		 
	    		 // Runkeeper Update
	    		if(  shared_pref.getBoolean("pref_use_runkeeper", false) && isOnline(true) )
	    		{
	    			Log.d(TAG,"Posting to Runkeeper");
	    			RunKeeperUpdate rk = new RunKeeperUpdate(DU, shared_pref);
	    			rk.Facebook = shared_pref.getBoolean("pref_use_runkeeper_facebook", false);    		 
	    			boolean ret=rk.doPost(id);

	    			if ( ret)
		    		 {
		    			Log.d(TAG,"RK Service Need New Token");
		    			Intent dialogIntent = new Intent(getBaseContext(), WebActivity.class);
			    		UpdateUtil.AuthType send = UpdateUtil.AuthType.RUNKEEPER;
			    		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    			dialogIntent.putExtra("auth_type", send.ordinal());
		    			startActivity(dialogIntent);
		    		 }
	    		}
	    		
	    		// Dropbox Update
	    		if( shared_pref.getBoolean("pref_use_dropbox", false) && isOnline(true))
	    		{
	    			Log.d(TAG,"Posting to Dropbox");
	    			DropboxUpdate db = new DropboxUpdate(DU,shared_pref);//,
	    			boolean ret=db.doPost(id);
	    			
		    		if ( ret)
		    		 {
		    			Log.d(TAG,"DB Service Need New Token");
		    			Intent dialogIntent = new Intent(getBaseContext(), WebActivity.class);
			    		UpdateUtil.AuthType send = UpdateUtil.AuthType.DROPBOX;
		    			dialogIntent.putExtra("auth_type", send.ordinal());
		    			dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    			getBaseContext().startActivity(dialogIntent);
		    		 }
	    		}
	    		
	    		// Google Drive
	    		if( shared_pref.getBoolean("pref_use_googledrive", false) && isOnline(true))
	    		{
	    			Log.d(TAG,"Posting to Google Drive");
	    			GoogleDriveUpdate gd = new GoogleDriveUpdate(DU,shared_pref);//,getBaseContext()
	    			boolean ret=gd.doPost(id);
	    			
		    		if ( ret)
		    		 {
		    			Log.d(TAG,"GD Service Need New Token");
		    			Intent dialogIntent = new Intent(getBaseContext(), WebActivity.class);
			    		UpdateUtil.AuthType send = UpdateUtil.AuthType.GOOGLEDRIVE;
		    			dialogIntent.putExtra("auth_type", send.ordinal());
		    			dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    			getBaseContext().startActivity(dialogIntent);
		    		 }
	    		}

		   
		   mHandler.postDelayed(mUpdateTimeTask,500);
		   }
		   catch(Exception e)
		   {
			   Log.d(TAG,e.toString());
		   }
	   }
	};
    	 
	public void wifi_toggle(View v) throws InterruptedException
	{
		WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
	   	
		Button p1_button = (Button)findViewById(R.id.Button02);
		if( p1_button.getText().equals("Disable WiFi") )
		{
			Log.d(TAG,"Toggling wifi off.");
			wifiManager.setWifiEnabled(false);
			SetWifiButton(false,true);
		}
		else
		{
			Log.d(TAG,"Toggling wifi on");
			wifiManager.setWifiEnabled(true);
			SetWifiButton(false,false);
		}
		
		
	}
    public void settings(View v) throws InterruptedException
    {
       bLaunchingSettings=true;
       startActivity(new Intent(this, SettingsActivity.class));
    }  
    public void export_data(View v) throws InterruptedException
    {
    	
    	Button butExport=(Button)findViewById(R.id.button1);
    	butExport.setEnabled(false);
    	butExport.setText("Wait..."); 
    	
    	mHandler.postDelayed(mExport_data,500);
		

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    //called when the preferences are changed in any way
    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences,String key)  {

    	
        final Context context = this;
        
    	if(key.equals("pref_use_runkeeper"))
    	{
    		
    		// ReAuth
    		if(sharedPreferences.getBoolean(key, false))
    		{
	    		Intent dialogIntent = new Intent(context, WebActivity.class);
	    		UpdateUtil.AuthType send = UpdateUtil.AuthType.RUNKEEPER;
    			dialogIntent.putExtra("auth_type", send.ordinal());
    			context.startActivity(dialogIntent);
    		}
  				
    	}
    	else if (key.equals("pref_use_dropbox"))
    	{
    		
    		// ReAuth
    		if(sharedPreferences.getBoolean(key, false))
    		{
    			Intent dialogIntent = new Intent(context, WebActivity.class);
	    		UpdateUtil.AuthType send = UpdateUtil.AuthType.DROPBOX;
    			dialogIntent.putExtra("auth_type", send.ordinal());
    			context.startActivity(dialogIntent);
    		}
    	}
    	else if (key.equals("pref_use_googledrive"))
    	{
    		// ReAuth
    		if(sharedPreferences.getBoolean(key, false))
    		{
    			Intent dialogIntent = new Intent(context, WebActivity.class);
	    		UpdateUtil.AuthType send = UpdateUtil.AuthType.GOOGLEDRIVE;
    			dialogIntent.putExtra("auth_type", send.ordinal());
    			context.startActivity(dialogIntent);
    		}
    	}
    	
    	
    	
        


    }
    


}




