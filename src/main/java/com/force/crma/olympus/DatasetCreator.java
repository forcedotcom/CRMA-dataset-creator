package com.force.crma.olympus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger ;

/**
 * Class containing  code to upload  csv files to Salesforce CRMA to create a Dataset. 
 * The class uses the standard Salesforce REST API in conjunction with the  External Data API to create the dataset.
 * It supports Overwrite/Append/Upsert Operations and requires a valid data csv file and schema file 
 * in accordance with CRMA external data upload specifications.
 * The file format  is csv or gzip with csv or zip extension respectively.
 * The encoding of the file must be UTF-8. 
 * The process  attempts to upload the file once, no retries are attempted.
 * Current API limit for upload is 40Gb per file and 50Gb total in 24 hours.
 * 
 * @author T J Wilson, 06/12/2022
 * 
 */

public class DatasetCreator {
	

	//datapart size limits
	protected static final int MAX_BYTES_PER_CHUNK=33554432; // 32 Mb  per datapart
	private static  int ADD_DATAPART_THREADS =3; //loading threads count
	private static Map<Integer, File> fileParts =null;
	//logger
	protected static AtomicInteger dataPartErrors =new AtomicInteger(0);
	
	private final static Logger logger = Logger.getLogger(DatasetCreator.class.getName());
	
	
	

	/**
	 * Main method to Upload the datafile to CMRA platform and create a Dataset.Using the Salesforce REST API and oauth 
	 * it creates the Data header with the  meta information that describes the dataset configuration.  It compresses 
	 * the data file if necessary and  chunks it into 32Mb parts and writes those file parts to disk. Using a blocking 
	 * queue of 10  and upload thread count of 3 it controls the sequencing of loading each data part file into  the
	 * Data part object. 
	 * @param aMetadataJson the schema file containing field definitions
	 * @param aDataFile the comma separated value data file containing records, must be zip or csv extension
	 * @param aDatasetAlias, the alias name of the Dataset to be created.
	 * @param aDatasetLabel, the dataset label
	 * @param aUserName, the login to be used to upload in the target Org
	 * @param aPassword, the password for the upload login
	 * @param aCSecret, the consumer secret.
	 * @param aCKey, the consumer key.
	 * @param aEndpoint, the REST API endpoint url.
	 * @param aOperation, the load operation ; Overwrite, Append or Upsert
	 * @throws Exception
	 */
	
	public static void uploadData(File aMetadataJson, File aDataFile,String aDatasetAlias,String aDatasetLabel,
			String aUserName, String aPassword, String aCSecret, String aCKey,
			String aEndpoint,String aOperation, String aApp) throws Exception{
		
		
		RESTHttpClient.setOauthTokens(aUserName, aPassword, aCSecret, aCKey,(aEndpoint));
	
		RESTHttpClient.createHeader(aOperation, aDatasetLabel,aDatasetAlias, aMetadataJson, aApp);
		try {
			addDataParts(aDataFile);
			RESTHttpClient.updateHeader();
		}catch (Exception e)	{
			throw new DatasetCreatorException("Unable to process dataparts "+e.getMessage());
		}finally {
			FileUtils.deleteQuietly(fileParts);
		} 
	}
	


	
	
	private static void addDataParts(File aDataFile) throws DatasetCreatorException {
		logger.info("Adding data parts");
		try {
		
			File fileToChunk = null;
			//if file extension is zip it is already in gzipped format
		 if (FileUtils.FILE_EXT_ZIP.equals(FileUtils.getFileExt(aDataFile))) {
				fileToChunk = aDataFile;

			} else {
				// Try to compress file, if unable use original file
				File gzipFile = new File(FileUtils.getBaseName(aDataFile)+".zip");
				if (FileUtils.gzipFile(aDataFile, gzipFile)) {
					fileToChunk = gzipFile;
				} else {
					fileToChunk = aDataFile;
				}
			}

			fileParts = FileUtils.chunkBinary(fileToChunk); // Split the file
			//created queue bounded to 10
			BlockingQueue<Map<Integer, File>> queue = new LinkedBlockingQueue<Map<Integer, File>>(10);
			LinkedList<AddDataPartThread> loadThreads = new LinkedList<AddDataPartThread>();
			if(fileParts.size()<=ADD_DATAPART_THREADS) 
						ADD_DATAPART_THREADS = 1; 
			AtomicInteger cnt = new AtomicInteger();
			for (int i = 0; i < ADD_DATAPART_THREADS; i++) {
				AddDataPartThread addDataPart = new  AddDataPartThread(queue,i, cnt);
				Thread th = new Thread(addDataPart,"AddDataPartsThread-"+i);
				th.setDaemon(true);
				th.start();
				loadThreads.add(addDataPart);

			}
			for (Map.Entry<Integer, File> entry : fileParts.entrySet()) {
				Map<Integer, File> row =new HashMap<Integer, File>();
				logger.info("Adding data part number to queue: "+entry.getKey());
				row.put(entry.getKey(), entry.getValue());
				queue.offer(row,240,TimeUnit.SECONDS);
								
			}	
			logger.info("Completed queue entries ");
			Integer endPart =4000;
			for(int i=0;i<loadThreads.size();i++) {
				endPart++;
				Map<Integer, File> endRow =new HashMap<Integer, File>();
				File endFile= new File(FileUtils.END_THREAD);
				logger.info("Adding data part number to queue: End File  "+endPart);
				endRow.put(endPart, endFile);
				queue.put(endRow);
			}
			if(dataPartErrors.get()>0) {
				throw new DatasetCreatorException("There were "+dataPartErrors.get()+" errors with data part entry");
			}
			for(int i=0;i<loadThreads.size();i++) {
				logger.fine("Iterate through threads to end them #Threads "+loadThreads.size());
				logger.fine("thread # "+ i);
				AddDataPartThread addDataPart =loadThreads.get(i);
				logger.fine("Checking thread is finished " + addDataPart.isDone());
				while(!addDataPart.isDone()) {
				 logger.fine("isDone checkloop " + addDataPart.isDone());
				 Thread.sleep(100000);
				}
			}

		} catch (Exception ioe) {
			throw new DatasetCreatorException(
					"Errors in adding data parts "
							+ ioe.getMessage(), ioe.getCause());
		}

	}
	


}
