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

public class RunKeeperObjects{
	public static class FitnessActivity {
		public String type; //The type of activity, as one of the following values: "Running", "Cycling", "Mountain Biking", "Walking", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Wheelchair", "Rowing", "Elliptical", "Other"
		public String equipment;
		public String start_time; //The starting time for the activity (e.g., "Sat, 1 Jan 2011 00:00:00")
		public String notes; //Any notes that the user has associated with the activity
		public path[] path; //The sequence of geographical points along the route (optional)
		
		public Double total_distance; //The total distance traveled, in meters (optional)
		public Double duration; //The duration of the activity, in seconds
		public Integer average_heart_rate; //The user's average heart rate, in beats per minute (optional)
		public heartRate[] heart_rate; //The sequence of time-stamped heart rate measurements (optional)
		public Double total_calories; //The total calories burned (optional)

		public Boolean post_to_facebook; //True to post this activity to Facebook, false to prevent posting (optional; if not specified, the user's default preference is used)
		public Boolean post_to_twitter; //True to post this activity to Twitter, false to prevent posting (optional; if not specified, the user's default preference is used)
		public Boolean detect_pauses;
		
	}
	
	public static class path { //WGS84  Path
		public Double timestamp; //The number of seconds since the start of the activity
		public Double latitude; //The latitude, in degrees (values increase northward and decrease southward)
		public Double longitude; //The longitude, in degrees (values increase eastward and decrease westward)
		public Double altitude; //The altitude of the point, in meters
		public String type; //One of the following values: "start", "end", "gps", "pause", "resume", "manual"

	}
	
	public static class calories {
		public Double timestamp; //The number of seconds since the start of the activity
		public Double calories; //The total calories burned since the start of the activity
	}
	
	public static class heartRate{
		public Double timestamp; //The number of seconds since the start of the activity
		public Integer heart_rate; //The instantaneous heart rate, in beats per minute
	}
}