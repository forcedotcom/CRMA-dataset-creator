package com.force.crma.olympus;


/*
 * Simple JAVA object representing REST JSON of the datapart object InsightsExternalDataPart
 * to support REST insert calls;
 * 
 */
public class DataPartJSON {
	
	
	protected static final String EXT_DATA_OBJECT_PARTS="InsightsExternalDataPart";
	protected static final String FIELD_DATA_FILE ="\"DataFile\"";
	protected static final String FIELD_INSIGHTS_EXT_DATA_ID="\"InsightsExternalDataId\"";
	protected static final String FIELD_PART_NUMBER="\"PartNumber\"";
	
	
	private static String headerId=null;
	private  String partNumber=null;
	private String dataPart=null;

	public DataPartJSON(String aHeaderId) {
		if (headerId ==null)  headerId=aHeaderId;
	}
	
	public void setPartNumber(String aPartNumber) {
		partNumber=aPartNumber;
	}
	
	public void setDataPart(String aDataPart) {
		dataPart=aDataPart;
	}
	public String toString() {
		StringBuilder  requestBody =new StringBuilder();
		requestBody.append("{").append(HeaderJSON.LF);
		requestBody.append(FIELD_INSIGHTS_EXT_DATA_ID).append(" : \"").append(headerId).append("\",").append(HeaderJSON.LF);
		requestBody.append(FIELD_PART_NUMBER).append(" : \"").append(partNumber).append("\",").append(HeaderJSON.LF);
		requestBody.append(FIELD_DATA_FILE).append(" : \"").append(dataPart).append("\"").append(HeaderJSON.LF);
		requestBody.append("}").append(HeaderJSON.LF);		
		return requestBody.toString();
	}

}
