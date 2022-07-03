package com.force.crma.olympus;

/*
 * Simple JAVA object representing REST JSON of the header object InsightsExternalData
 * for to support both REST insert and update  Calls
 * 
 */

public class HeaderJSON {
	
	protected static final String EXT_DATA_OBJECT="InsightsExternalData";
	protected static final String FIELD_FORMAT="\"Format\"";
	protected static final String FIELD_OPERATION="\"Operation\"";
	protected static final String FIELD_EDGEMART_LABEL="\"EdgemartLabel\"";
	protected static final String FIELD_EDGEMART_ALIAS="\"EdgemartAlias\"";
	protected static final String FIELD_EDGEMART_CONTAINER="\"EdgemartContainer\"";
	protected static final String FIELD_METADATA_JSON="\"MetadataJson\"";
	protected static final String FIELD_ACTION="\"Action\"";
	protected static final String FIELD_DATA_FILE ="DataFile";
	protected static final String[] OPERATIONS_VALUE= {"Append","Overwrite","Upsert"};
	protected static final char LF = '\n';
	

		
	
	private String operation="Overwrite";
	private String datasetLabel=null;
	private String datasetAlias=null;
	private String metajson =null;
	private String action="None";
	private String app=null;
	
	
	
	

	public HeaderJSON(String aOperation, String aDatasetLabel,String aDatasetAlias, String aApp) {
		if(!StringUtils.isBlank(aOperation)) operation =aOperation;
		datasetAlias=aDatasetAlias;
		datasetLabel=aDatasetLabel;
		app=aApp;
		
	};
	
	public HeaderJSON() {
		
	}

	protected void setMetaJSON(String aMetajson) {
		metajson =aMetajson;
	}
	
	
	protected void setAction(String aAction) {
		action =aAction;
	}

	
	public String toString() {
		StringBuilder  requestBody =new StringBuilder();
		requestBody.append("{").append(LF);
		requestBody.append(FIELD_FORMAT).append(" : \"CSV\",").append(LF);
		requestBody.append(FIELD_EDGEMART_LABEL).append(" : \"").append(datasetLabel).append("\",").append(LF);
		requestBody.append(FIELD_EDGEMART_ALIAS).append(" : \"").append(datasetAlias).append("\",").append(LF);
		if(!StringUtils.isBlank(app)) {
			requestBody.append(FIELD_EDGEMART_CONTAINER).append(" : \"").append(app).append("\",").append(LF);
		}
		requestBody.append(FIELD_OPERATION).append(" : \"").append(operation).append("\",").append(LF);
		requestBody.append(FIELD_METADATA_JSON).append(" : \"").append(metajson).append("\",").append(LF);
		requestBody.append(FIELD_ACTION).append(" : \"").append(action).append("\"").append(LF);
		requestBody.append("}").append(LF);
		
		return requestBody.toString();
	}
	
	
	public String toPatchString() {
		StringBuilder  requestBody =new StringBuilder();
		requestBody.append("{").append(LF);
		requestBody.append(FIELD_ACTION).append(" : \"").append(action).append("\"").append(LF);
		requestBody.append("}").append(LF);	
		return requestBody.toString();
		
	}

}
