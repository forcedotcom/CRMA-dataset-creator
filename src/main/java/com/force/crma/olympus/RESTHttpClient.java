package com.force.crma.olympus;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Class that communicates with CRMA External Data API via the REST API and parses
 * response JSON. Specifically it gets the oauth token, inserts the meta data into the header 
 * object and provides the method to insert BLOBs into the data part object. 
 * In addition it updates the header with the process command.
 * 
 * @author T J Wilson, 06/12/2022
 * 
 */



public class RESTHttpClient {
	

    public static final String OAUTH_2_URL ="https://login.salesforce.com/services/oauth2/token";
    public static final String OAUTH_2_URL_STEM ="/services/oauth2/token";
	public static final String END_POINT="/services/data/";
    public static final String API_VERSION="v54.0";
    public static final String SOBJECTS="/sobjects/";
    public static final String SOQL_URL_STEM ="/query/?q=SELECT+FolderId+FROM+InsightsApplication+WHERE+DeveloperName+='";
    
    private static String[] oauthTokens =new String[2];
    private static String headerId=null;
    private  boolean dataPartSuccess=false;

	public RESTHttpClient() {
		
	}
	private final static Logger logger = Logger.getLogger(RESTHttpClient.class.getName());
	

	public static void setOauthTokens(String aUserName, String aPassword, 
			String aCSecret, String aCKey, String aEndpoint) throws Exception{
			String oauth2Url=OAUTH_2_URL;
		    if(!StringUtils.isBlank(aEndpoint)) {
		    	oauth2Url=aEndpoint +OAUTH_2_URL_STEM;
		    }
		

		   Map<Object, Object> data = new HashMap<>();
		    data.put("grant_type", "password");
		    data.put("client_id", aCKey);
		    data.put("client_secret", aCSecret);
		    data.put("username", aUserName);
		    data.put("password", aPassword);
		
		   HttpRequest request =HttpRequest.newBuilder()
		     .uri(new URI(oauth2Url))
			.POST(setFormData(data))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.build();
		 
		 HttpClient client = HttpClient.newHttpClient();
		
		 HttpResponse<String> resp=client.send(request,BodyHandlers.ofString());
		 oauthTokens[0] =  parseAccessToken(resp.body());
		 oauthTokens[1] =parseIntanceUrl(resp.body());
		 
	}


	
	public static String[] getOauthTokens() {
		return oauthTokens;
	}
	
	public static BodyPublisher setFormData(Map<Object, Object> data) {
	    var builder = new StringBuilder();
	    for (Map.Entry<Object, Object> entry : data.entrySet()) {
	      if (builder.length() > 0) {
	        builder.append("&");
	      }
	      builder
	          .append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
	      builder.append("=");
	      builder
	          .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
	    }
	    return BodyPublishers.ofString(builder.toString());
	  }
	
	// Simple method to parse Access Token from salesforce response

	
	
	public static String parseAccessToken(String body) {
		 int posStart= body.indexOf(":");
		 body=body.substring(posStart+1);
		 int posEnd =body.indexOf(",");
		 body=body.substring(1,posEnd-1);
		 return body;
		
	}
	public static String parseIntanceUrl(String body){
		 int posStart= body.indexOf("instance_url");
		 body=body.substring(posStart+14);
		 int posEnd =body.indexOf(",");
		 body=body.substring(1,posEnd-1);
		 return body;
		
	}
	
	
	public static String parseHeaderId(String body){
		 int posStart= body.indexOf("id");
		 body=body.substring(posStart+4);
		 int posEnd =body.indexOf(",");
		 body=body.substring(1,posEnd-1);
		 return body;
		
	}
	
	public static String parseFolderId(String body){
		 int posStart= body.indexOf("FolderId");
		 if(posStart>-1) {
			 body=body.substring(posStart+10);
			 int posEnd =body.indexOf("}");
			 body=body.substring(1,posEnd-1);		 
		 }else {
			 body=null;
		 } 
		
		 return body;
		
	}
	
	public static boolean parseResponseIsSuccess(String aBody) {
		 int posStart= aBody.indexOf("success");
		 if(posStart>-1) {
			 String body=aBody.substring(posStart+9);
			 int posEnd =body.indexOf(",");
			 body=body.substring(0,posEnd);
			 if (!StringUtils.isBlank(body)&&body.equalsIgnoreCase("true")){
				 return true;
			 }else
			 return false;
		 }else return false;
			 
	}

	
	protected boolean isDataPartSuccess() {
		return dataPartSuccess;
	}


	
	public static void createHeader(String aOperation, String aDatasetLabel, 
			String aDatasetAlias,File aMetadataJson, String aApp ) throws Exception {
		    
		   String appId =getAppId(aApp);
		   
		   logger.info("App Folder Id : "+appId);
		   
		   HeaderJSON header= new HeaderJSON(aOperation,aDatasetLabel,aDatasetAlias, null);
		   FileUtils fileUtils = new FileUtils();
		   header.setMetaJSON(fileUtils.getBase64String(fileUtils.readFile(aMetadataJson)));
		   	   		   
		   HttpRequest request =HttpRequest.newBuilder()
				   .uri(new URI(getOauthTokens()[1]+END_POINT+API_VERSION+SOBJECTS+HeaderJSON.EXT_DATA_OBJECT+"/"))
				   .header("Content-Type", "application/json")
				   .header("Authorization", "Bearer "+getOauthTokens()[0])
				   .POST(BodyPublishers.ofString(header.toString()))	
				   .build();
		 
		 HttpClient client = HttpClient.newHttpClient();

		 HttpResponse<String> resp=client.send(request,BodyHandlers.ofString());
		 if(!parseResponseIsSuccess(resp.body())) {
			 throw new DatasetCreatorException("Unable to create header : " + resp.body());
		 }
		 headerId=parseHeaderId(resp.body());
		 
				
	}
	
	
	public  void createDataPart(File aDataPart, int aPartNumber) throws Exception {
		
		   
		   DataPartJSON dataPart= new DataPartJSON(headerId) ;
		   FileUtils fileUtils = new FileUtils();
		   dataPart.setDataPart(fileUtils.getBase64String(fileUtils.readFile(aDataPart)));
		   dataPart.setPartNumber(String.valueOf(aPartNumber));
		   	   		   
		   HttpRequest request =HttpRequest.newBuilder()
				   .uri(new URI(getOauthTokens()[1]+END_POINT+API_VERSION+SOBJECTS+DataPartJSON.EXT_DATA_OBJECT_PARTS+"/"))
				   .header("Content-Type", "application/json")
				   .header("Authorization", "Bearer "+getOauthTokens()[0])
				   .POST(BodyPublishers.ofString(dataPart.toString()))	
				   .build();
		   
		 
		 HttpClient client = HttpClient.newHttpClient();

		 HttpResponse<String> resp=client.send(request,BodyHandlers.ofString());
		 logger.fine("File Part:"+aPartNumber +" Response :"+resp.body());
		 if(!parseResponseIsSuccess(resp.body())) {
			 // this will be changed with new data part error handling code
			 // obvious two ways that are contradictory her
			 throw new DatasetCreatorException(resp.body());
		 }else {
			 dataPartSuccess=true;
		 }
				
	}
	
   public static void updateHeader() throws Exception{
	   HeaderJSON header= new HeaderJSON();
	   logger.fine("Setting action to Process");
	   header.setAction("Process");
	   	   		   
	   HttpRequest request =HttpRequest.newBuilder()
			   .uri(new URI(getOauthTokens()[1]+END_POINT+API_VERSION+SOBJECTS+
					   HeaderJSON.EXT_DATA_OBJECT+"/" +headerId))
			   .header("Content-Type", "application/json")
			   .header("Authorization", "Bearer "+getOauthTokens()[0])
			   .method("PATCH",BodyPublishers.ofString(header.toPatchString()))	
			   .build();
	   	 
	 HttpClient client = HttpClient.newHttpClient();
	 HttpResponse<String> resp=client.send(request,BodyHandlers.ofString());
	 logger.info("Header action set to process ");
	 
	    
   }
	
	public static String getAppId(String aApp ) throws Exception {
		
		   //SOQL query
		
		   	   		   
		   HttpRequest request =HttpRequest.newBuilder()
				   .uri(new URI(getOauthTokens()[1]+END_POINT+API_VERSION+SOQL_URL_STEM +aApp+"'"))
				   .header("Content-Type", "application/json")
				   .header("Authorization", "Bearer "+getOauthTokens()[0])
				   .GET()	
				   .build();
		 
		 HttpClient client = HttpClient.newHttpClient();

		 HttpResponse<String> resp=client.send(request,BodyHandlers.ofString());
		 logger.fine("Query response : "+resp.body());
		 	 
		 return parseFolderId(resp.body());
		 
		
		 
				
	}

	


}
