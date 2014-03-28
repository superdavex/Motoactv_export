package com.sdsoft.motoactv_export;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {
	private static final String TAG = "sd_motoactv_export";
	
	@Override
    public void onReceive(Context context, Intent intent) { 
    	Log.d(TAG, context.toString(),null);
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Wait 5 seconds for processing.
    	
    	
    	if(isOnline(context))
    	{	
    		Log.d(TAG, "Wifi Online");
    		Intent service = new Intent(context, SYNC_Service.class);
    		context.startService(service);
    	}
    	else
    	{
    		Log.d(TAG, "Wifi Offline");
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