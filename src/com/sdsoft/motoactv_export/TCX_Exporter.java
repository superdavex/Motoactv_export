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

import android.util.Log;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import com.sdsoft.motoactv_export.DataUtil.LapDetails;
import com.sdsoft.motoactv_export.DataUtil.WorkoutActivityAGPX;


public class TCX_Exporter {


    String Running = "Running";
    String Biking = "Biking";
    String Other = "Other";
    
    
  // My Tracks categories that are considered as TCX biking sport type.
 // private static int TCX_SPORT_BIKING_IDS[] = {
 //     3 //Biking
 //     };
//
  // My Tracks categories that are considered as TCX running sport type.
 // private static int TCX_SPORT_RUNNING_IDS[] = {
 //    1, // Running
 //    2, // Walking
 //    4, // Hiking
 //   5, // Treadmill 
 //    };

  private PrintWriter printWriter;
  private String sportType;

  public TCX_Exporter(Writer writer) {
      this.printWriter= new PrintWriter(writer);

  }
  

  public void close() {
    if (printWriter != null) {
      printWriter.close();
      printWriter = null;
    }
  }

  public String getExtension() {
    return ".tcx";
  }

  public void writeHeader() {
    if (printWriter != null) {
      printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      printWriter.println("<TrainingCenterDatabase"
          + " xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\"");
      printWriter.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      printWriter.println("xsi:schemaLocation=" 
          + "\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2"
          + " http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\">");
    }
  }

  public void writeFooter(int Major, int Minor) {
    if (printWriter != null) {
      printWriter.println("<Author xsi:type=\"Application_t\">");
      printWriter.println("<Name>SD</Name>");
      // <Build>, <LangID>, and <PartNumber> are required by type=Application_t.
      printWriter.println("<Build>");
      writeVersion(Major, Minor);
      printWriter.println("</Build>");
      printWriter.println("<LangID>" + Locale.getDefault().getLanguage() + "</LangID>");
      printWriter.println("<PartNumber>000-00000-00</PartNumber>");
      printWriter.println("</Author>");
      printWriter.println("</TrainingCenterDatabase>");
    }
  }

  public void writeBeginLap(LapDetails ld) {
	    if (printWriter != null) {

	      printWriter.println("<Lap StartTime=\"" + formatDateTimeIso8601(ld.start_time) + "\">");
	      printWriter.println("<TotalTimeSeconds>" + (ld.duration/1000) + "</TotalTimeSeconds>");
	      printWriter.println("<DistanceMeters>" + ld.distance + "</DistanceMeters>");
	      printWriter.println("<MaximumSpeed>" + ld.speed + "</MaximumSpeed>");
	      
	      printWriter.println("<Calories>" + (int)ld.calorie+ "</Calories>");
	      
	      //if (sportType == Biking) {
		  //    printWriter.println("<Cadence xsi:type=\"CadenceValue_t\">");
		  //    printWriter.println("<Value>" + (byte)ld.cadence + "</Value>");
		  //    printWriter.println("</Cadence>");
	     // }
	      if((int)ld.heart_rate  >0)
	      {
		      printWriter.println("<AverageHeartRateBpm xsi:type=\"HeartRateInBeatsPerMinute_t\">");
		      printWriter.println("<Value>" + (int)ld.heart_rate + "</Value>");
		      printWriter.println("</AverageHeartRateBpm>");
	      }
	      
	      printWriter.println("<Intensity>Active</Intensity>");
	      
	      String tm = "";
	      if(ld.is_manual ==1)
	    	  tm = "Manual";
	      else
	    	  tm = "Distance";
	      
	      printWriter.println("<TriggerMethod>" + tm + "</TriggerMethod>");
	    }
	  }
  public void writeEndLap(int Steps) {
	  if(Steps >0)
	  {
		  printWriter.println("<Extensions>");
		  printWriter.println("<LX xmlns=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\">");
		  //printWriter.println("  <AvgSpeed>5.3120003</AvgSpeed>");
		 // printWriter.println("  <AvgRunCadence>75</AvgRunCadence>");
		 //printWriter.println("  <MaxRunCadence>76</MaxRunCadence>");
		  printWriter.println("<Steps>" + Steps + "</Steps>");
		  printWriter.println("</LX>");
		  printWriter.println(" </Extensions>");
	  }
	  printWriter.println("</Lap>");
  }
  public static String formatCData(String text) {
	    return "<![CDATA[" + text.replaceAll("]]>", "]]]]><![CDATA[>") + "]]>";
  }
  
  public void writeBeginActivity(LapDetails ld) {
	    if (printWriter != null) {
	      printWriter.println("<Activities>");
	      printWriter.println("<Activity Sport=\"" + ld.sport_type + "\">");
	      this.sportType = ld.sport_type;
	      
	      printWriter.println("<Id>" + formatDateTimeIso8601(ld.start_time) + "</Id>");
	    }
	  }
  
  public void writeEndActivity(int Major, int Minor) {
    if (printWriter != null) {
    
      String TrackDescription = "";
      
      
      printWriter.println("<Notes>" + formatCData(TrackDescription) + "</Notes>");
      printWriter.println("<Creator xsi:type=\"Device_t\">");
      printWriter.println("<Name>SD</Name>");
      // <UnitId>, <ProductID>, and <Version> are required for type=Device_t.
      printWriter.println("<UnitId>0</UnitId>");
      printWriter.println("<ProductID>0</ProductID>");
      writeVersion(Major, Minor);
      printWriter.println("</Creator>");
      printWriter.println("</Activity>");
      printWriter.println("</Activities>");
    }
  }

  public void writeBeginTrack() {
    if (printWriter != null) {
      printWriter.println("<Track>");
    }
  }

  public void writeEndTrack() {
    if (printWriter != null) {
      printWriter.println("</Track>");
    }
  }

  public void writeTrackPoint(WorkoutActivityAGPX wagpx) {
	  //
	  
    if (printWriter != null  )
    {
      printWriter.println("<Trackpoint>");
      printWriter.println("<Time>" +formatDateTimeIso8601(wagpx.time_of_day)  + "</Time>"); //formatDateTimeIso8601
      if(wagpx.latitude >= -90 && wagpx.latitude <= 90 &&  wagpx.longitude >= -180 && wagpx.longitude <=180 )
      {
	      printWriter.println("<Position>");
	      printWriter.println("<LatitudeDegrees>" + wagpx.latitude  + "</LatitudeDegrees>");
	      printWriter.println("<LongitudeDegrees>" + wagpx.longitude  + "</LongitudeDegrees>");
	      printWriter.println("</Position>");
      }
      printWriter.println("<AltitudeMeters>" + wagpx.elevation  + "</AltitudeMeters>");
      printWriter.println("<DistanceMeters>" + wagpx.distance  + "</DistanceMeters>");
      

      if (wagpx.heart_rate > 0) {
        printWriter.println("<HeartRateBpm xsi:type=\"HeartRateInBeatsPerMinute_t\">");
        printWriter.println("<Value>" + wagpx.heart_rate + "</Value>");
        printWriter.println("</HeartRateBpm>");
      }

      // <Cadence> needs to be put before <Extensions>.
      // According to the TCX spec, <Cadence> is only for the biking sport
      // type. For others, use <RunCadence> in <Extensions>.
      if (sportType == Biking) {
        // The spec requires the max value be 254.
        printWriter.println("<Cadence>" + Math.min(254, (byte)wagpx.cadence) + "</Cadence>");
      }


		printWriter.println("<Extensions>");
		printWriter.println(
		    "<TPX xmlns=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\">");
		
		// <RunCadence> needs to be put before <Watts>.
		if ( wagpx.cadence > 0 && sportType != Biking) 
		{
		  // The spec requires the max value to be 254.
		  printWriter.println("<RunCadence>" + Math.min(254,wagpx.cadence) + "</RunCadence>");
		}
		
		if (wagpx.power > 0) {
		  printWriter.println("<Watts>" + wagpx.power + "</Watts>");
		}
		
		//if (wagpx.speed > 0) {
		//	//printWriter.println("<Speed>" + wagpx.speed + "</Speed>");
		//}
		
		//if (wagpx.steps > 0) {
		//	printWriter.println("<Steps>" + wagpx.steps + "</Steps>");
		//}
		
		printWriter.println("</TPX>");
		printWriter.println("</Extensions>");

        
      printWriter.println("</Trackpoint>");
    }
    else
    {
    	Log.d("TCX_Exporter", "Bad lat/lon skipping:  " +  wagpx.latitude + "/" + wagpx.longitude  ,null);
    	
    }
  }
  


  /**
   * Writes the TCX Version.
   */
  private void writeVersion(int Major, int Minor) {
    // Split the My Tracks version code into VersionMajor, VersionMinor, and,
    // BuildMajor to fit the integer type requirement for these fields in the
    // TCX spec.
    printWriter.println("<Version>");
    printWriter.println("<VersionMajor>" + Major + "</VersionMajor>");
    printWriter.println("<VersionMinor>" + Minor + "</VersionMinor>");
    // According to TCX spec, these are optional. But http://connect.garmin.com
    // requires them.
    printWriter.println("<BuildMajor>1</BuildMajor>");
    printWriter.println("<BuildMinor>0</BuildMinor>");
    printWriter.println("</Version>");
  }


  
  private static final SimpleDateFormat ISO_8601_DATE_TIME_FORMAT = new SimpleDateFormat( 	      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final SimpleDateFormat ISO_8601_BASE = new SimpleDateFormat(    	      "yyyy-MM-dd'T'HH:mm:ss");
  private static final Pattern ISO_8601_EXTRAS = Pattern.compile(    	      "^(\\.\\d+)?(?:Z|([+-])(\\d{2}):(\\d{2}))?$");
  static {
    ISO_8601_DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    ISO_8601_BASE.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  
public static String formatDateTimeIso8601(long time) {
return ISO_8601_DATE_TIME_FORMAT.format(time);
}


}


