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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class WorkoutComplete extends BroadcastReceiver {
	private static String TAG = "sd_motoactv_export";
	
    @Override
    public void onReceive(Context context, Intent intent) { 
    	Log.d(TAG, context.toString(),null);
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Wait 5 seconds for processing.
    	
    	
    	// Store needs Updating Flag.
        SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(context);
        
    	// Get Last ID
    	DataUtil DU = new DataUtil(context.getContentResolver());
		int id = DU.GetLastWorkoutID();
		
		int needs_updating=0;
	       
	     try
		 {
	    	 needs_updating = shared_pref.getInt("needs_updating", 0);
		 }
	     catch( Exception e)
	     {
	    	 needs_updating=0;
	     }
        

        //Only store if not set
        if(needs_updating ==0)
        {
        	needs_updating = id;
	        SharedPreferences.Editor editor = shared_pref.edit();
	        editor.putInt("needs_updating", id);
	        editor.commit();
        }
      
        Log.d(TAG, "current id:"+id + " Needs:" +needs_updating);
		
        boolean bLocalTCX =  shared_pref.getBoolean("pref_local_TCX", true);
        
        if(bLocalTCX || isOnline(context))
        {
        	 Log.d(TAG, "Already online or local TCX, starting service");
        	Intent service = new Intent(context, SYNC_Service.class);
            context.startService(service);
        	
        }
    	
    }
    
    public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
