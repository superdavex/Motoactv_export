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

import java.sql.Date;
import java.util.ArrayList;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.sdsoft.motoactv_export.DataUtil.LapDetails;
import com.sdsoft.motoactv_export.DataUtil.WorkoutActivityAGPX;

public class RunKeeperUpdate {
	
	final static String CALLBACK = "oauth://runkeeper";
	private static final Token EMPTY_TOKEN = null;
	private static String TAG = "sd_motoactv_export";
	private DataUtil du = null;
	private SharedPreferences settings;
	public boolean Facebook = false;

	public RunKeeperUpdate (DataUtil DU,SharedPreferences settings)
	{
			this.du = DU;
			this.settings = settings;
	}
	
	public boolean UpdateAll(int NeedsUpdating)
	{
	
		Log.d(TAG,"Updating RunKeeper:" + NeedsUpdating );
		Cursor curUpdate = null;


		curUpdate = this.du.getNeedsUpdate(NeedsUpdating) ; 
		while(curUpdate.moveToNext())
		{
			int Workout_id = curUpdate.getInt(curUpdate.getColumnIndex("_id"));
			Log.d(TAG,"Needs:" + Workout_id );
			
			//Update shared pref to current id
			SharedPreferences.Editor editor = this.settings.edit();
		    editor.putInt("needs_updating", Workout_id);
		    editor.commit();
		        
			if( doPost(Workout_id))
			{
				// Needs new token
				Log.d(TAG,"Bad Post, needs Auth:" + NeedsUpdating );
				return false;

			}
			
		
		}
		curUpdate.close();
		
		SharedPreferences.Editor editor = this.settings.edit();
	    editor.putInt("needs_updating", 0);
	    editor.commit();
	    
		return true;
	}
	
	public boolean doPost(int id) {  

		boolean bGetNewToken = false;
		
		try
		{
			OAuthService s = new ServiceBuilder()
	        .provider(RunKeeperApi.class)
			.apiKey(ApiKeys.runkeeper_APIKEY )
			.apiSecret(ApiKeys.runkeeper_APISECRET)
			.debug()
			.build();
	        //		.callback(CALLBACK)
	
			
			Token accessToken = this.getToken();
					
			if ( accessToken !=null)
			{
		     
				bGetNewToken = false;

				OAuthRequest req = new OAuthRequest(Verb.POST, "http://api.runkeeper.com/fitnessActivities?");
				
				Gson gson = new Gson();			
				RunKeeperObjects.FitnessActivity ft = this.getActivities(id);
				
				// Discard
				if(ft == null)
					return true;
				
				String payload = gson.toJson(ft);
				//System.out.println(payload);
				
				req.addHeader("Content-Type", "application/vnd.com.runkeeper.NewFitnessActivity+json");				
				req.addPayload(payload);       			
				s.signRequest(accessToken, req);
				Response response = req.send();
				int rcode = response.getCode();
				
				// 201 = good, 401 = bad
				Log.d(TAG,"response code:" + rcode );
				switch(rcode)
				{
					case 201:
						//good
						Log.d(TAG,"Activty posted to runkeeper:" + rcode );
						break;
					case 401:
					case 403:
						// Need auth
						bGetNewToken = true;
						this.ClearToken();
						break;
					case 400:
						// Bad request
						Log.d(TAG,"Bad request 400:" + response.getBody() );
						break;
					default:
						Log.d(TAG,"Activty post received unkown respose code:" + rcode );
						break;
				}
			
				
			}
			else
			{
				bGetNewToken = true;
			}
		}
		catch(Exception e)
		{
			Log.d(TAG,"Error Posting to Runkeeper:" + e.getMessage() );
		}

		return bGetNewToken;
	}
	
	public Token getToken()
	{
		
		String sToken = this.settings.getString("rk_access_token", "");
		String sSecret = this.settings.getString("rk_access_secret","");
		Token accessToken =null;
		
		//sToken+="tst";
		
		if(sToken !="")
			accessToken =new Token(sToken,sSecret);
		
		return accessToken;
		
	
	}
	public void ClearToken()
	{
		 SharedPreferences.Editor editor = settings.edit();
	     editor.putString("rk_access_token", "");
	     editor.putString("rk_access_secret", "");
	     editor.commit();
	
	}
	
	private void Log(String text)
	{
		boolean bDebug = false;
		
		if(bDebug) Log.d(TAG,text);
	}
	
	public RunKeeperObjects.FitnessActivity getActivities(int id )
	{
		com.sdsoft.motoactv_export.RunKeeperObjects.FitnessActivity fitnessActivity = new RunKeeperObjects.FitnessActivity();
		ArrayList<RunKeeperObjects.path> paths = new ArrayList<RunKeeperObjects.path>();
		ArrayList<RunKeeperObjects.heartRate> heartRates = new ArrayList<RunKeeperObjects.heartRate>();

		RunKeeperObjects.path path = new RunKeeperObjects.path();
		RunKeeperObjects.heartRate heartRate = new RunKeeperObjects.heartRate();
		
		int LapSteps=0;
		int LastLapSteps=0;
		
		double total_calories =0;
		
		Cursor curCSV = null;

		Log.d(TAG, "Workout ID: " + id,null);
		
		WorkoutActivity woa = this.du.GetWorkoutActivtyInfo(id);

			
		// Get Lap Info
		long curLapDuration =0;
		int lap_num = 1;
		
		// Check for interval detail to use instead of laps
		Cursor curLAP = this.du.getWorkoutSubActivity( woa.id);
		
		if(curLAP.moveToNext())
		{
			Log.d(TAG, "Has Sub Activty: " +  woa.id,null);
			curLapDuration = curLAP.getLong(curLAP.getColumnIndex("duration"));
			lap_num = curLAP.getInt(curLAP.getColumnIndex("lap_number"));
		}
		else
		{
			// Get Lap Info
			curLAP = this.du.getLapDetails(woa.id);
						
			if(curLAP.moveToNext())
			{
				curLapDuration = curLAP.getLong(curLAP.getColumnIndex("duration"));
				lap_num = curLAP.getInt(curLAP.getColumnIndex("lap_number"));
			}
			else
			{
				Log.d(TAG, "No lap found for workout id " +  woa.id + " setting lap duration to 6 hours",null);
				curLapDuration =21600000;
			}
		}
		Log.d(TAG, "Lap " + lap_num + " Duration: " + curLapDuration,null);
		
		// Get detail data
		curCSV = this.du.getAGPX( woa.Activity_Start_Time,woa.Activity_End_Time);

        try
        {
        	fitnessActivity.notes = "Posted from SD Motoactv Exporter:ID-"+id; 
        	fitnessActivity.type = woa.Sport_Type;
        	fitnessActivity.equipment = "None";
    		
    		//final Calendar cal = Calendar.getInstance();
    		//cal.setTimeInMillis(Activity_Start_Time);
    		//java.util.Date date = cal.getTime();
    		
    		 double date = woa.Activity_Start_Time;
            // date += TimeZone.getDefault().getOffset((long) date);
             String sDate =  DateFormat.format("EEE, dd MMM yyyy hh:mm:ss", new Date((long) date)).toString();
    		
             fitnessActivity.start_time = sDate;//"Sat, 1 Jan 2011 00:00:00";//Activity_Start_Time;
    		System.out.println(sDate);
    		fitnessActivity.post_to_twitter= false;
    		fitnessActivity.post_to_facebook= this.Facebook;// true;   		

            // Write First Lap 
            LapDetails ld = du.getLapDetails(curLAP);
            ld.start_time = woa.Activity_Start_Time;
            ld.sport_type = woa.Sport_Type;                  
    		
            // Loop through data
            long curTime =0;
            long lapStart = woa.Activity_Start_Time;
            while(curCSV.moveToNext())
            {
            	WorkoutActivityAGPX wagpx = du.getWorkoutActivityAGPX(curCSV);
            	
            	// Check for new lap
            	curTime = wagpx.time_of_day;
            	
            	Log( "curTime:   " + curTime + " LS :" + lapStart + " Dif: " + (curTime - lapStart));
            	if(  curTime - lapStart >curLapDuration)
            	{
            		curLAP.moveToNext();
            		
            		if (! curLAP.isAfterLast() )
            		{
	            		// Close Last Lap
	            		LastLapSteps = LapSteps;
	            		LapSteps =0;
	            		
	            		// Write New Lap Detail
	            		lapStart = lapStart + curLapDuration; 
	            		
	                    ld = du.getLapDetails(curLAP);
	                    ld.start_time = lapStart;
	                    ld.sport_type = woa.Sport_Type;
	                    
	                    lap_num = ld.lap_number;
	            		curLapDuration = ld.duration;
	            		Log( "Lap " + lap_num  + "  Duration: " + curLapDuration);
	
            		}
                    
            	}
            	LapSteps = wagpx.steps;
            	
            	//Write Detail      
            	Log( "Lat:  " + "" + wagpx.latitude);
            	Log( "Time:  " +wagpx.time_of_day);
            
            	
            	// Path information
            	path = new RunKeeperObjects.path();
            	
            	path.timestamp = (double) ((wagpx.time_of_day-woa.Activity_Start_Time) * 0.001 );
        		path.altitude =(double) wagpx.elevation;
        		path.longitude = wagpx.longitude;
        		path.latitude=wagpx.latitude;
        		path.type= "gps";
        		
        		// Heart rate info
        		heartRate = new RunKeeperObjects.heartRate();
        		heartRate.timestamp = (double) ((wagpx.time_of_day-woa.Activity_Start_Time) * 0.001 );
        		heartRate.heart_rate  = wagpx.heart_rate;
        		
        		// Increment calories
        		total_calories = wagpx.calorie_burn;  // calories are accumulative
        		
        		// Check for valid distance
        		if(wagpx.latitude ==wagpx.longitude)
        		{	
        			// bad data
        		/*if(locPrev != null)
        		{
        			
	        		Location location = new Location("");
	        		location.setLatitude(wagpx.latitude);
	        		location.setLongitude(wagpx.longitude);
	        		
	        		float dist =  location.distanceTo(locPrev);
	        		
	        		if(dist > 2){
	        			System.out.println("distance " +dist);
	        			return null;
	        		}
        		
        		}
        		else
        		{
        			locPrev = new Location("");
        			locPrev.setLatitude(wagpx.latitude);
        			locPrev.setLongitude(wagpx.longitude);
        		}*/
        		}
        		else
        		{
        			//	wagpx.calorie_burn
        			paths.add(path);
            		if(wagpx.heart_rate >0)
            			heartRates.add(heartRate);
        		}
        		
        		
        		
        		
        		
        				
        		//Gson gson = new Gson();			
				//System.out.println(gson.toJson(path));

            }
        }
            catch(SQLException sqlEx)

            {

                Log.e("MainActivity", sqlEx.getMessage(), sqlEx);

                return null;

            }

            curCSV.close();

            fitnessActivity.path = paths.toArray(new RunKeeperObjects.path[paths.size()]);
            if(heartRates.size() > 0)
            	fitnessActivity.heart_rate = heartRates.toArray(new RunKeeperObjects.heartRate[heartRates.size()]);
            fitnessActivity.total_calories = total_calories;
            
            Log( "Total Calories " + total_calories  );
            
            
            if (paths.size() <2)
            {
            	Log.e("MainActivity", "Path must contain at leat two points, discarding.");
            	return null;
            }
            else
            	return fitnessActivity;
	}
	
	 public void getAuthToken(WebView wv,Context context)
	   {
		  final ProgressDialog mProgress;
		   mProgress = ProgressDialog.show(context, "Loading", "Please wait for a moment...");
		   Log.d(TAG, "Requesting Runkeeper token");

		   try
		   {
		       final OAuthService s = new ServiceBuilder()
		       .provider(RunKeeperApi.class)
				.apiKey(ApiKeys.runkeeper_APIKEY)
				.apiSecret(ApiKeys.runkeeper_APISECRET)
				.debug()
				.callback(CALLBACK)
				.build();
		       
		       final Token requestToken = null;// s.getRequestToken();
		       final Context cx = context;
		       
		
				String authURL = s.getAuthorizationUrl(EMPTY_TOKEN);
		       	
				
				final  WebView webview = wv;
		        
				webview.setVisibility(View.VISIBLE);
				webview.requestFocus(View.FOCUS_DOWN);
		
				WebSettings webSettings = webview.getSettings();
		
				webSettings.setJavaScriptEnabled(true); //set to true to enable javascript
				webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //a true setting allows the window.open() js call
				webSettings.setLightTouchEnabled(true);// set to true enables touches and mouseovers
				webSettings.setSavePassword(true); //set to true save the user inputed passwords in forms
				webSettings.setSaveFormData(true);// set to true saves the user form data
				webSettings.setSupportZoom(true); //set to true to suport the zoom feature
				
		        //attach WebViewClient to intercept the callback url
		        webview.setWebViewClient(new WebViewClient(){
		        	
		        	public void onPageFinished(WebView view, String url) {
	                	Log.d(TAG,"finished");
	                    if(mProgress.isShowing()) {
	                        mProgress.dismiss();
	                    }
	                }
		        	
		        	@Override
		        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        		
		        		//check for our custom callback protocol otherwise use default behavior
		        		if(url.startsWith("oauth")){
		        			String ssToken = "";
		        			String ssSecret ="";
		        			try
		        			{
			        			Uri uri = Uri.parse(url);
			        			String verifier = uri.getQueryParameter("code");
			        			Log.d(TAG,verifier);
			        			Verifier v = new Verifier(verifier);
			        			
			        			//save this token for practical use.
			        			Token accessToken = s.getAccessToken(requestToken, v);
			        			ssToken = accessToken.getToken();
			        			ssSecret = accessToken.getSecret();
			        			
			        			// Close web view
			        			((Activity) cx).finish();
			        			Log.d(TAG,"Token Valid");
		        			}
		        			catch(Exception e)
		        			{
		        				Log.d(TAG,"Token InValid. Error:" + e.getMessage());
		        			}
		        				        			
		        	        SharedPreferences.Editor editor = settings.edit();
		        	        editor.putString("rk_access_token", ssToken);
		        	        editor.putString("rk_access_secret", ssSecret);
		        	        editor.commit();
		        	        
		        			return true;
		        		}
		        		
		        		
		        		return super.shouldOverrideUrlLoading(view, url);
		        	}
		        });
		        
		
		        //send user to authorization page if needed
		        webview.loadUrl(authURL);
		   }
		   catch (Exception e)
		   {
			   e.printStackTrace();
			   Log.d(TAG,"Failed Retreiving Token. Error:" + e.getMessage());
		   }
		   
		   
	   }
}
