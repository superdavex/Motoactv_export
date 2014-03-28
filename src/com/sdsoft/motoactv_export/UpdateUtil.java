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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class UpdateUtil {
	public enum AuthType { RUNKEEPER, DROPBOX, GOOGLEDRIVE }
	private static String TAG = "sd_motoactv_export";
	
	public static boolean EnableWifi_andWait(Context context)
    {
    	
    	if(isOnline(context))
    			return true;
    	
    	WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    	 wifiManager.setWifiEnabled(true);
    	 
    	 boolean wmsgDisplayed = false;
    	 boolean imsgDisplayed = false;
    	 
    	 long curTime = System.currentTimeMillis();
    	 while(wifiManager.getWifiState() !=3)
    	 {

    		 if(! wmsgDisplayed)
    		 {
    			 Log.d(TAG,"Waiting for WIFI to enable");
    			 wmsgDisplayed = true;
    		 }
    		 if(System.currentTimeMillis() - curTime > 30000)
    			 break;
	 
    	 }
    	 
    	 if( wifiManager.getWifiState() !=3)
    	 {
    		 wifiManager.setWifiEnabled(false);
    		 Log.d(TAG,"Wifi Not enabled correctly.");
    		 return false;
    	 }
    	 
    	 curTime = System.currentTimeMillis();
		 while (! isOnline(context))
		 {
			 if(! imsgDisplayed)
			 {
				 Log.d(TAG,"Waiting for active internet connection.");
				// Toast.makeText(getApplicationContext(), "Waiting for active internet connection.", Toast.LENGTH_SHORT).show();
				 imsgDisplayed= true;
			 }
			 
			 if(System.currentTimeMillis() - curTime > 30000)
    			 break;
		 }
    			 
		 if(! isOnline(context) )
		 {
			 Log.d(TAG,"No internet");
			// Toast.makeText(getApplicationContext(), "Internet connection could net be established.", Toast.LENGTH_LONG).show();
			 wifiManager.setWifiEnabled(false);
			 return false;
		 }
		 else{
			 Log.d(TAG,"Internet active");
			 return true;
		 }
				 
		 
        }
	
    public static boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
}	

