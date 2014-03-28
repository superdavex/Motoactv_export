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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class DataUtil {

	public static final Uri CONTENT_URI_LAP_DETAILS = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/lap_details");
	public static final Uri CONTENT_URI_WORKOUT_DATA = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/workout_data");
	public static final Uri CONTENT_URI_LAST_WORKOUT = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/workout_last_details");
	public static final Uri CONTENT_URI_APGX = Uri.parse("content://com.motorola.gault.activity.providers.workoutrawcontentprovider/workout_activity_apgx");	
	//public static final Uri CONTENT_URI_WORKOUT_ACTIVITY = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/view_workout_activity");
	public static final Uri CONTENT_URI_WORKOUT_ACTIVITY = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/workout_activity");
	public static final Uri CONTENT_URI_WORKOUT_ACTIVITY_SUMMARY = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/workout_activity_summary");
	public static final Uri CONTENT_URI_WORKOUT_SUB_ACTIVITY = Uri.parse("content://com.motorola.gault.activity.providers.summarycontentprovider/workout_sub_activity");
	
	private static String TAG = "sd_motoactv_export";
	private static int buildMajor = 1;
	private static int buildMinor = 7;

	private ContentResolver CR;
	
	
	public DataUtil(ContentResolver CR)
	{
			this.CR = CR;
	}
	
	public WorkoutActivity GetWorkoutActivtyInfo(int id)
	{
		Cursor curCSV = null;
		WorkoutActivity woa = new WorkoutActivity();
		
		if(id == 0)
			curCSV = this.CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "", null, "_id desc");
		else
			curCSV = this.CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "_id = " + id, null, null);
		
		curCSV.moveToNext();
		
		// Get Last Workout Info
		woa.id = curCSV.getInt(curCSV.getColumnIndex("_id"));
		woa.Activity_Start_Time = curCSV.getLong(curCSV.getColumnIndex("start_time"));
		woa.Activity_End_Time = curCSV.getLong(curCSV.getColumnIndex("end_time"));
		int activity_type = curCSV.getInt(curCSV.getColumnIndex("activity_type_id"));;
		switch(activity_type)
		{
		case 1:
		case 2:
		case 8:
		case 16:
			woa.Sport_Type = "Running";
			break;
		case 4:
			woa.Sport_Type = "Biking";
			break;
		default:
			woa.Sport_Type = "Other";
			break;
		
		}
		curCSV.close();
		
		return woa;
	}
	public Cursor getNeedsUpdate(int NeedsUpdating)
	{
		Cursor curUpdate = CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "_id >= " + NeedsUpdating, null, null);
		
		return curUpdate;
	}
	
	public Cursor getLapDetails(int id)
	{
		Cursor curLD = this.CR.query(CONTENT_URI_LAP_DETAILS, null, "workout_activity_id = " +  id,null, null);
		
		return curLD;
	}
	
	public Cursor getAGPX(long start, long end)
	{
		Cursor curAGPX = this.CR.query(CONTENT_URI_APGX, null, "time_of_day >= " + start+ " and time_of_day <= " + end,null, null);
		
		return curAGPX;
	}
	
	
	
	public Cursor getWorkoutSubActivity(int id)
	{
		Cursor curSub = this.CR.query(CONTENT_URI_WORKOUT_SUB_ACTIVITY, Workout_Activty_Map, "workout_activity_id = " + id,null, null);
		
		return  curSub;
		
	}
	public int GetLastWorkoutID( )
	{
		Cursor curCSV = null;

		
		curCSV = this.CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "", null, "_id desc");

		curCSV.moveToNext();
		
		// Get Last Workout Info
		int Workout_id = curCSV.getInt(curCSV.getColumnIndex("_id"));
		
		curCSV.close();
		
		return Workout_id;
		
	}
	private void Log(String text)
	{
		boolean bDebug = false;
		
		if(bDebug) Log.d(TAG,text);
	}
	public String exportTCX(int id, boolean bTempFile )
    {
		int LapSteps=0;
		int LastLapSteps=0;
		
		Cursor curCSV = null;
		Cursor curLAP = null;
		Log.d(TAG, "Workout ID: " + id,null);
		
		if(id == 0)
			curCSV = this.CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "", null, "_id desc");
		else
			curCSV = this.CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "_id = " + id, null, null);
		
		curCSV.moveToNext();
		
		// Get Last Workout Info
		int Workout_id = curCSV.getInt(curCSV.getColumnIndex("_id"));
		long Activity_Start_Time = curCSV.getLong(curCSV.getColumnIndex("start_time"));
		long Activity_End_Time = curCSV.getLong(curCSV.getColumnIndex("end_time"));
		String Sport_Type = "";
		int activity_type = curCSV.getInt(curCSV.getColumnIndex("activity_type_id"));;
		switch(activity_type)
		{
		case 1:
		case 2:
		case 8:
		case 16:
			Sport_Type = "Running";
			break;
		case 4:
			Sport_Type = "Biking";
			break;
		default:
			Sport_Type = "Other";
			break;
		
		}
		Log.d(TAG, "Workout ID: " + Workout_id,null);
		Log.d(TAG, "Activity_Start_Time: " + Activity_Start_Time,null);
		Log.d(TAG, "Activity_End_Time: " + Activity_End_Time,null);
		
		//Workout_id = 7;
	   // Activity_Start_Time = "1344894677082";
	    //Activity_End_Time = "1344897742960";
	    
		curCSV.close();
		
		
		// Get Lap Info
		long curLapDuration =0;
		int lap_num = 1;
		// Check for interval detail to use instead of laps
		curLAP = this.CR.query(CONTENT_URI_WORKOUT_SUB_ACTIVITY, Workout_Activty_Map, "workout_activity_id = " + Workout_id,null, null);
		if(curLAP.moveToNext())
		{
			Log.d(TAG, "Has Sub Activty: " + Workout_id,null);
			curLapDuration = curLAP.getLong(curLAP.getColumnIndex("duration"));
			lap_num = curLAP.getInt(curLAP.getColumnIndex("lap_number"));
		}
		else
		{
			// Get Lap Info
			curLAP = this.CR.query(CONTENT_URI_LAP_DETAILS, null, "workout_activity_id = " + Workout_id,null, null);
			
			if(curLAP.moveToNext())
			{
				curLapDuration = curLAP.getLong(curLAP.getColumnIndex("duration"));
				lap_num = curLAP.getInt(curLAP.getColumnIndex("lap_number"));
			}
			else
			{
				Log.d(TAG, "No lap found for workout id " + Workout_id + " setting lap duration to 6 hours",null);
				curLapDuration =21600000;
			}
		}
		Log.d(TAG, "Lap " + lap_num + " Duration: " + curLapDuration,null);
		
		
		// Get detail data
		curCSV = this.CR.query(CONTENT_URI_APGX, null, "time_of_day >= " + Activity_Start_Time + " and time_of_day <= " + Activity_End_Time,null, null);
	    
		
		File exportDir = new File(Environment.getExternalStorageDirectory(), "");

		SimpleDateFormat ISO_DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
		String sWorkoutDate= ISO_DATE_TIME_FORMAT.format(Activity_Start_Time) ;
		
		String fileName = "workout_" + sWorkoutDate + "_" + Workout_id ;
				
		if(bTempFile)
			fileName += ".tmp";
		else
			fileName += ".tcx";
		
        File file = new File(exportDir,fileName );
                
        
        try

        {

            file.createNewFile();
            
            
            TCX_Exporter tcxWrite = new TCX_Exporter(new FileWriter(file));
            
            tcxWrite.writeHeader();

            // Write First Lap 
            LapDetails ld = getLapDetails(curLAP);
            ld.start_time = Activity_Start_Time;
            ld.sport_type = Sport_Type;
                        
            tcxWrite.writeBeginActivity(ld);
            tcxWrite.writeBeginLap(ld);
            tcxWrite.writeBeginTrack();
            
            // Loop through data
            long curTime =0;
            long lapStart = Activity_Start_Time;
            while(curCSV.moveToNext())

            {
            	WorkoutActivityAGPX wagpx = getWorkoutActivityAGPX(curCSV);
            	
            	// Check for new lap
            	curTime = wagpx.time_of_day;
            	
            	Log("curTime:   " + curTime + " LS :" + lapStart + " Dif: " + (curTime - lapStart));
            	if(  curTime - lapStart >curLapDuration)
            	{
            		curLAP.moveToNext();
            		
            		if (! curLAP.isAfterLast() )
            		{
	            		// Close Last Lap
	            		tcxWrite.writeEndTrack();
	            		tcxWrite.writeEndLap(LapSteps-LastLapSteps);
	            		LastLapSteps = LapSteps;
	            		LapSteps =0;
	            		
	            		// Write New Lap Detail
	            		lapStart = lapStart + curLapDuration; 
	            		
	                    ld = getLapDetails(curLAP);
	                    ld.start_time = lapStart;
	                    ld.sport_type = Sport_Type;
	                    
	                    lap_num = ld.lap_number;
	            		curLapDuration = ld.duration;
	            		Log("Lap " + lap_num  + "  Duration: " + curLapDuration);
	
	                    tcxWrite.writeBeginLap(ld);
	                    tcxWrite.writeBeginTrack();
            		}
                    
            	}
            	LapSteps = wagpx.steps;
            	
            	//Write Detail      
            	Log( "Lat:  " + "" + wagpx.latitude);
            	Log( "Time:  " +wagpx.time_of_day);
            
            	tcxWrite.writeTrackPoint(wagpx);
            
                

            }
         // Close Last Lap
    		tcxWrite.writeEndTrack();
    		tcxWrite.writeEndLap(LapSteps-LastLapSteps);
    		tcxWrite.writeEndActivity(buildMajor, buildMinor);
    		tcxWrite.writeFooter(buildMajor, buildMinor);
    		tcxWrite.close();

            curCSV.close();

            Log.d(TAG,"TCX created at:"+ fileName);
            return fileName;

        }

        catch(SQLException sqlEx)

        {

            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);

            return "";

        }

        catch (IOException e)

        {

            Log.e("MainActivity", e.getMessage(), e);

            return "";

        }


    }
	
	public WorkoutActivityAGPX getWorkoutActivityAGPX(Cursor curs)
	   {
		   WorkoutActivityAGPX wagpx = new WorkoutActivityAGPX();
		  
		   wagpx.id = curs.getInt(curs.getColumnIndex("calorie_burn"));
		   wagpx.accuracy = curs.getDouble(curs.getColumnIndex("calorie_burn"));
		   wagpx.activity_type_id = curs.getInt(curs.getColumnIndex("calorie_burn"));
		   wagpx.barometric_pressure = curs.getInt(curs.getColumnIndex("calorie_burn"));
		   wagpx.bearing = curs.getInt(curs.getColumnIndex("calorie_burn"));
		   wagpx.cadence = curs.getInt(curs.getColumnIndex("cadence"));
		   wagpx.calorie_burn = curs.getDouble(curs.getColumnIndex("calorie_burn"));
		   wagpx.crank_toque = curs.getInt(curs.getColumnIndex("crank_toque"));
		   wagpx.distance = curs.getDouble(curs.getColumnIndex("distance"));
		   wagpx.elevation = curs.getInt(curs.getColumnIndex("elevation"));
		   wagpx.heart_rate = curs.getInt(curs.getColumnIndex("heart_rate"));
		   wagpx.latitude = curs.getDouble(curs.getColumnIndex("latitude"));
		   wagpx.longitude = curs.getDouble(curs.getColumnIndex("longitude"));
		   wagpx.pace = curs.getDouble(curs.getColumnIndex("pace"));
		   wagpx.power = curs.getInt(curs.getColumnIndex("power"));
		   wagpx.repetitions = curs.getInt(curs.getColumnIndex("repetitions"));
		   wagpx.speed = curs.getInt(curs.getColumnIndex("speed"));
		   wagpx.steps = curs.getInt(curs.getColumnIndex("steps"));
		   wagpx.temperature = curs.getInt(curs.getColumnIndex("temperature"));
		   wagpx.time_of_day = curs.getLong(curs.getColumnIndex("time_of_day"));
		   wagpx.wheel_torque = curs.getInt(curs.getColumnIndex("wheel_torque"));

		   return wagpx;
	   }
	    
	 
	   public class WorkoutActivityAGPX
	  {
	    public int id;
	    public double accuracy;
	    public int activity_type_id;
	    public int barometric_pressure;
	    public int bearing;
	    public double cadence;
	    public double calorie_burn;
	    public int crank_toque;
	    public double distance;
	    public int elevation;
	    public int heart_rate;
	    public double latitude;
	    public double longitude;
	    public double pace;
	    public String picture_file;
	    public int power;
	    public int repetitions;
	    public int rpm;
	    public int speed;
	    public int steps;
	    public int temperature;
	    public long time_of_day;
	    public int wheel_torque;

	  }
	   public String[] Workout_Activty_Map = {
			   
			   "0 as ascent_total",
			   "avg_cadence as cadence",
			   "avg_calorie as calorie",
			  "0 descent_total",
			   "distance",
			   "duration * 1000 as duration",
			    "avg_heart_rate as heart_rate",
			    "manual_end as is_manual",
			    "_id as lap_number",
			    "latitude",
			    "longitude",
			    "avg_power as power",
			    "avg_speed as speed",
			    "step_rate",
			    "0 start_time",
			    "0 sport_type"
	   };
	   
	   public LapDetails getLapDetails(Cursor curs)
	   {
		   LapDetails ld = new LapDetails();
		   if(curs.getCount() > 0 )
		   {
			   ld.ascent_total = curs.getInt(curs.getColumnIndex("ascent_total"));
			   ld.cadence = curs.getInt(curs.getColumnIndex("cadence"));
			   ld.calorie = curs.getInt(curs.getColumnIndex("calorie"));
			   ld.descent_total = curs.getInt(curs.getColumnIndex("descent_total"));
			   ld.distance = curs.getInt(curs.getColumnIndex("distance"));
			   ld.cadence = curs.getInt(curs.getColumnIndex("cadence"));
			   ld.duration = curs.getLong(curs.getColumnIndex("duration"));
			   ld.heart_rate = curs.getInt(curs.getColumnIndex("heart_rate"));
			   ld.is_manual = curs.getInt(curs.getColumnIndex("is_manual"));
			   ld.lap_number = curs.getInt(curs.getColumnIndex("lap_number"));
			   ld.heart_rate = curs.getInt(curs.getColumnIndex("heart_rate"));
			   ld.latitude = curs.getDouble(curs.getColumnIndex("latitude"));
			   ld.longitude = curs.getDouble(curs.getColumnIndex("longitude"));
			   ld.power = curs.getInt(curs.getColumnIndex("power"));
			   ld.speed = curs.getInt(curs.getColumnIndex("speed"));
			   ld.step_rate = curs.getInt(curs.getColumnIndex("step_rate"));
		   }
		   else
		   {
			   ld.ascent_total = 0;
			   ld.cadence = 0;
			   ld.calorie = 0;
			   ld.descent_total = 0;
			   ld.distance = 0;
			   ld.cadence = 0;
			   ld.duration = 21600000;
			   ld.heart_rate = 0;
			   ld.is_manual = 1;
			   ld.lap_number =1;
			   ld.heart_rate = 0;
			   ld.latitude = 0;
			   ld.longitude = 0;
			   ld.power = 0;
			   ld.speed = 0;
			   ld.step_rate = 0;

		   }

		   return ld;
	   }
	   
	   public class LapDetails
	   {
	     public long ascent_total;
	     public int cadence;
	     public double calorie;
	     public long descent_total;
	     public double distance;
	     public long duration;
	     public int heart_rate;
	     public int is_manual;
	     public int lap_number;
	     public double latitude;
	     public double longitude;
	     public int power;
	     public double speed;
	     public double step_rate;
	     public long start_time;
	     public String sport_type;
	   }
	   
	   public boolean exportAGPXCSV(int id,ContentResolver CR)
	    {
	
		   	Cursor curCSV = null;
			Cursor curLAP = null;
			Log.d(TAG, "Export CSV - Workout ID: " + id,null);
			
			if(id == 0)
				curCSV = CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "", null, "_id desc");
			else
				curCSV = CR.query(CONTENT_URI_WORKOUT_ACTIVITY, null, "_id = " + id, null, null);
			
			curCSV.moveToNext();
			
			// Get Last Workout Info
			int Workout_id = curCSV.getInt(curCSV.getColumnIndex("_id"));
			long Activity_Start_Time = curCSV.getLong(curCSV.getColumnIndex("start_time"));
			long Activity_End_Time = curCSV.getLong(curCSV.getColumnIndex("end_time"));

			Log.d(TAG, "Workout ID: " + Workout_id,null);
			Log.d(TAG, "Activity_Start_Time: " + Activity_Start_Time,null);
			Log.d(TAG, "Activity_End_Time: " + Activity_End_Time,null);
			
		    
			curCSV.close();
			
			// Get Lap Info
			curLAP = CR.query(CONTENT_URI_LAP_DETAILS, null, "workout_activity_id = " + Workout_id,null, null);
			
			long curLapDuration =0;
			int lap_num = 1;
			if(curLAP.moveToNext())
			{
				curLapDuration = curLAP.getLong(curLAP.getColumnIndex("duration"));
				lap_num = curLAP.getInt(curLAP.getColumnIndex("lap_number"));
			}
			else
			{
				Log.d(TAG, "No lap found for workout id " + Workout_id + " setting lap duration to 6 hours",null);
				curLapDuration =21600000;
			}

			
			Log.d(TAG, "Lap " + lap_num + " Duration: " + curLapDuration,null);
			
			
			// Get detail data
			curCSV = CR.query(CONTENT_URI_APGX, null, "time_of_day >= " + Activity_Start_Time + " and time_of_day <= " + Activity_End_Time,null, null);
		    
			File exportDir = new File(Environment.getExternalStorageDirectory(), "");

	        File file = new File(exportDir, "workout_" + Workout_id + ".csv");
	        
	        try

	        {

	            file.createNewFile();
	            
	            
	            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

	            csvWrite.writeNext(curCSV.getColumnNames());

	            while(curCSV.moveToNext())

	            {

	                String arrStr[] ={
	                		curCSV.getString(0),
	                		curCSV.getString(1),
	                		curCSV.getString(2),
	                		curCSV.getString(3),
	                		curCSV.getString(4),
	                		curCSV.getString(5),
	                		curCSV.getString(6),
	                		curCSV.getString(7),
	                		curCSV.getString(8),
	                		curCSV.getString(9),
	                		curCSV.getString(10),
	                		curCSV.getString(11),
	                		curCSV.getString(12),
	                		curCSV.getString(13),
	                		curCSV.getString(14),
	                		curCSV.getString(15),
	                		curCSV.getString(16),
	                		curCSV.getString(17),
	                		curCSV.getString(18),
	                		curCSV.getString(19),
	                		curCSV.getString(20),
	                		curCSV.getString(21),
	                		curCSV.getString(22),
	                		curCSV.getString(23)
	                };

	            /* curCSV.getString(3),curCSV.getString(4)};*/

	                csvWrite.writeNext(arrStr);

	            }

	            csvWrite.close();

	            curCSV.close();

	            return true;

	        }

	        catch(SQLException sqlEx)

	        {

	            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);

	            return false;

	        }

	        catch (IOException e)

	        {

	            Log.e("MainActivity", e.getMessage(), e);

	            return false;

	        }


	    }
}
