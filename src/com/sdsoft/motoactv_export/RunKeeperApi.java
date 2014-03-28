package com.sdsoft.motoactv_export;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

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

public class RunKeeperApi  extends DefaultApi20
{
	 
	 @Override
	 public String getAccessTokenEndpoint()
	 {
	    return "https://runkeeper.com/apps/token?grant_type=authorization_code";
	 }
	 
	 @Override
	 public Verb getAccessTokenVerb()
	 {
	    return Verb.POST;
	 }
	 
	 @Override
	 public AccessTokenExtractor getAccessTokenExtractor()
	 {
	    return new JsonTokenExtractor();
	 }
	 
	 @Override
	 public String getAuthorizationUrl(OAuthConfig config)
	 {
	
	    //setHostname();
	    StringBuilder authUrl = new StringBuilder();
	    authUrl.append("https://runkeeper.com/apps/authorize?response_type=code");

	    
	    // add redirect URI if callback isn't equal to 'oob'
	   // if (!config.getCallback().equalsIgnoreCase("oob"))
	   // {
	      authUrl.append("&redirect_uri=").append(OAuthEncoder.encode(config.getCallback()));
	    //}
	  
	    authUrl.append("&client_id=").append( OAuthEncoder.encode(config.getApiKey()));
	    System.out.println(authUrl.toString());
	    
	    return authUrl.toString();
	 }
	 @Override
	 public OAuthService createService(OAuthConfig config)
	  {
	    return new OAuth20ServiceImpl(this, config);
	  }
   
}