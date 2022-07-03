package com.force.crma.olympus;

import java.io.File;


public class CreateDataset {



	  public static void main(String[] args)
	  {
	    DatasetCreatorParams params = new DatasetCreatorParams();
	    if (args.length >= 1) {
	    	
	      for (int i = 1; i < args.length; i += 2) {
	    //	 System.out.println("Curent Argument " + args[(i - 1)]);
	    //	  System.out.println("Curent Value " + args[i]);
	        if ((args[(i - 1)].equalsIgnoreCase("--help")) || (args[(i - 1)].equalsIgnoreCase("-help")) || (args[(i - 1)].equalsIgnoreCase("help")))
	        {
	          printUsage();
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--u"))
	        {
	          params.username = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--p"))
	        {
	          params.password = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--cSecret"))
	        {
	          params.cSecret = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--cKey"))
	        {
	          params.cKey = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--schemaFile"))
	        {
	          String tmp = args[i];
	          if (tmp != null)
	          {
	            File tempFile = new File(tmp);
	            if (tempFile.exists())
	            {
	              params.schemaFile = tempFile.toString();
	            }
	            else
	            {
	              System.out.println("File {" + args[i] + "} does not exist");
	              System.exit(-1);
	            }
	          }
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--inputFile"))
	        {
	          String tmp = args[i];
	          if (tmp != null)
	          {
	            File tempFile = new File(tmp);
	            if (tempFile.exists())
	            {
	              params.inputFile = tempFile.toString();
	            }
	            else
	            {
	              System.out.println("File {" + args[i] + "} does not exist");
	              System.exit(-1);
	            }
	          }
	        }

	        else if (args[(i - 1)].equalsIgnoreCase("--dataset"))
	        {
	          params.datasetAlias = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--label"))
	        {
	          params.datasetLabel = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--app"))
	        {
	          params.app = args[i];
	        }

	        else if (args[(i - 1)].equalsIgnoreCase("--endpoint"))
	        {
	          params.endpoint = args[i];
	        }
	        //proxy parameters not used but left in for future use
	        else if (args[(i - 1)].equalsIgnoreCase("--proxyHost"))
	        {
	          params.proxyHost = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--proxyPort"))
	        {
	          params.proxyPort = Integer.valueOf(args[i]);
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--proxyUserName"))
	        {
	          params.proxyUsername = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--proxyPassword"))
	        {
	          params.proxyPassword = args[i];
	        }
	        else if (args[(i - 1)].equalsIgnoreCase("--operation"))
	        {
	          if (args[i] != null) {
	            if (args[i].equalsIgnoreCase("Overwrite"))
	            {
	              params.operation = args[i];
	            }
	            else if (args[i].equalsIgnoreCase("Upsert"))
	            {
	              params.operation = args[i];
	            }
	            else if (args[i].equalsIgnoreCase("Append"))
	            {
	              params.operation = args[i];
	            }
	            else if (args[i].equalsIgnoreCase("Delete"))
	            {
	              params.operation = args[i];
	            }
	            else
	            {
	              System.out.println("Invalid Operation {" + args[i] + "} Must be Overwrite or Upsert or Append or Delete");
	              System.exit(-1);
	            }
	          }
	        }
	      }
	    }
	 //   params.schemaFile = (params.inputFile.substring(0, params.inputFile.length() - 4) + "_schema.json");
	   // System.out.println("Schema file " + params.schemaFile);

	    long startTime = System.currentTimeMillis();
	    System.out.println("Start Time in ms : " + startTime);
	    File schemaFile = new File(params.schemaFile);
	    File dataFile = new File (params.inputFile);
	    try
	    {
	      checkArguments(schemaFile, dataFile, params.datasetAlias, params.username, 
	    		  params.password,params.cSecret,params.cKey, params.operation);
	      
	      DatasetCreator.uploadData(schemaFile, dataFile, params.datasetAlias,params.datasetLabel,
	    		  params.username, params.password,params.cSecret,params.cKey,
	    		  params.endpoint, params.operation, params.app );
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("End Time in ms : " + endTime);
	  }
	
	
		//Simple validation method for the arguments
		private static void checkArguments(File aMetadataJson, File aDataFile,String aDatasetAlias,String aUserName, String aPassword, 
				String aCSecret, String aCKey,String aOperation) throws DatasetCreatorException{
			//Validate JSON file
			if(aMetadataJson==null){
				throw new DatasetCreatorException("The JSON schema file cannot be null");
			}
			if(aMetadataJson.length()==0){
				throw new DatasetCreatorException("The JSON schema file cannot be empty");
			}
			//Validate security credentials
			if(StringUtils.isBlank(aUserName)||StringUtils.isBlank(aPassword)){
				throw new DatasetCreatorException("The username or password cannot be null or empty");
			}
			if(StringUtils.isBlank(aCSecret)||StringUtils.isBlank(aCKey)){
				throw new DatasetCreatorException("The client secret or key cannot be null or empty");
			}
			//Validate DatasetAlias credentials
			if(StringUtils.isBlank(aDatasetAlias)){
				throw new DatasetCreatorException("The Dataset Alias name cannot be null or empty");
			}
			if(!StringUtils.isBlank(aOperation)&&!StringUtils.arrayContainsValue(HeaderJSON.OPERATIONS_VALUE,aOperation)){
				throw new DatasetCreatorException("The operation "+aOperation +" is not valid, it must be one of : Append, Overwrite or Upsert");
			}
			//Validate data file file
			if(aDataFile==null){
				throw new DatasetCreatorException("The data file cannot be null");
			}
			if(aDataFile.length()==0){
				throw new DatasetCreatorException("The data file cannot be empty");
			}
			//check if csv or zip extension
			String fileExt=FileUtils.getFileExt(aDataFile);
			if(!(FileUtils.FILE_EXT_CSV.equals(fileExt)||FileUtils.FILE_EXT_ZIP.equals(fileExt))){
				throw new DatasetCreatorException("The data file must have .csv or .zip extension");
			}
			//if zip check that the format is gzip
			if(FileUtils.FILE_EXT_ZIP.equals(fileExt)&&!FileUtils.detectGZipped(aDataFile)){
				throw new DatasetCreatorException("The zip file must be GZip format");
			}
			
		}
	
	  public static void printUsage()
	  {
	    System.out.println("\n*******************************************************************************");
	    System.out.println("NOT COMPLETE YET Usage:");
	    System.out.print("java -jar DatasetCreator.jar --u userName --p password --operation operatiom --endpoint endPoint ");
	    System.out.println("--dataset datasetAlias --label datasetLabel --inputFile inputFile --schemaFile metajsonfile ");
	    System.out.println("--cKey custKey --cSecret custSecret --inputFile inputFile --schemaFile metajsonfile ");
	    System.out.println("--p       : required Salesforce.com password");
	    System.out.println("--endpoint: (Optional) The salesforce REST api endpoint (test/prod)");
	    System.out.println("          : Default: https://login.salesforce.com/");
	    System.out.println("--dataset :  the dataset alias. required");
	    System.out.println("--label :  the dataset label");
	    System.out.println("--inputFile :the input csv file. required ");
	    System.out.println("--schemaFile :the meta json file. required ");
	    System.out.println("--cKey :client key, required");
	    System.out.println("--cSecret :client secret, required");
	    System.out.println("--operation :the load operation, Overwrite, Append, Upsert or Delete. required ");
	    System.out.println("*******************************************************************************\n");
	    System.out.println("Usage Example 1: Upload a csv to a dataset");
	    System.out.println("java -jar DatasetCreator.jar --operation Overwrite  --schemaFile complaints_schema.json --inputFile complaints.csv --dataset Complaints");
	    System.out.println("--label Complaints  --u yname@acme.com --p pa33w@rd --endpoint https://login.salesforce.com --cSecret 844383BE0F0FVV --cKey 3MVG9FMtW0XJDLd2hVV");
	    System.out.println("");
	  }
	

}
