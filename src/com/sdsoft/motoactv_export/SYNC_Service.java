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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class SYNC_Service extends Service {
	private static final String TAG = "sd_motoactv_export";

	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		//Log.d("csv bind", "",null);
		return null;
	}
	
	public void onCreate() {
		//Log.d(TAG, "onCreate");	
		
		}	
	@Override	
	public void onDestroy() {	
		Log.d(TAG, "Exiting");
		}	
	@Override
	public void onStart(Intent intent, int startid) {	
		
		Log.d(TAG, "onStart");		
		}
	
	private void StartMain()
	{
		Intent dialogIntent = new Intent(this, MainActivity.class);
		dialogIntent.putExtra("com.sdsoft.motoactc_export.SERVICE_EXPORT",1);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(dialogIntent);
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service Started");	
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		

		Log.d(TAG, "Internet:" + isOnline());	
		
		
		 SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(this);

	     int needs_updating=0;
	       
	     try
		 {
	    	 needs_updating = shared_pref.getInt("needs_updating", 0);
		 }
	     catch( Exception e)
	     {
	    	 Log.d(TAG, "Error getting pref:" + e.toString());
	    	 needs_updating=0;
	     }
	     
	     if (needs_updating >0)
	     {
	    	 try
	    	 {
		    	 Log.d(TAG, "Needs Updating:" + needs_updating);	
		    	 
	    	 
		    	 DataUtil DU = new DataUtil(getContentResolver());
		 		 
		    	 if(shared_pref.getBoolean("pref_local_TCX", true))
		    	 {
		    		 Log.d(TAG, "Exporting TCX");
		    		 DU.exportTCX(0,false);
		    	 }
		    	 
		    	
		    	 if(isOnline())
		    	 {
		    		 
		    		 // Runkeeper Update
		    		if( shared_pref.getBoolean("pref_use_runkeeper", false) )
		    		{
		    		 
		    		 RunKeeperUpdate rk = new RunKeeperUpdate(DU, shared_pref);
		    		 rk.Facebook = shared_pref.getBoolean("pref_use_runkeeper_facebook", false);    		 
		    		 boolean ret=rk.UpdateAll(needs_updating);
	
		    		 if (! ret)
		    		 {
		    			Log.d(TAG,"RK Service Need New Token");
		    			StartMain();
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
			    			StartMain();
			    		 }
		    		}
		    	 }
	    	 }
	    	 catch( Exception e)
	    	 {
	    		 Log.d(TAG,"Error updating:" + e.getMessage());
	    	 }

	     }
	     else
	     {
	    	 Log.d(TAG, "No update needed");
	     }
	     
	     
		
		
		stopSelf();;
		
	    return  START_NOT_STICKY;
	}


    	 
   public boolean isOnline() {
    		    ConnectivityManager cm =
    		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    		        return true;
    		    }
    		    return false;
    		}
    	 
	
	 
	     
}

