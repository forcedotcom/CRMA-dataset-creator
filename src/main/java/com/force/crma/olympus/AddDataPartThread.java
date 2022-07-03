package com.force.crma.olympus;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddDataPartThread implements Runnable {
	
	private final BlockingQueue<Map<Integer,File>> queue;
	private final int threadNo;
	private final static Logger logger = Logger.getLogger(AddDataPartThread.class.getName());
	private volatile AtomicInteger dataPartCount;
	
	private volatile AtomicBoolean done = new AtomicBoolean(false);

	public AddDataPartThread(BlockingQueue<Map<Integer,File>> aFileParts, int aThreadNo, AtomicInteger aCnt) {
		queue= aFileParts;
		threadNo=aThreadNo+1;
		dataPartCount=aCnt;
		logger.info("Creating AddDataPartThread : "+threadNo);
	}

	@Override
	public void run() {


		try {
			RESTHttpClient restClient = new RESTHttpClient();
			logger.fine("Before queue take on thread : " + threadNo);
			Map<Integer, File> filePartRow = queue.take();

			File filePart = null;
			Integer partNo = 0;
			for (Integer key : filePartRow.keySet()) {
				filePart = filePartRow.get(key);
				partNo = key;

			}
			logger.fine("After queue take on thread : " + threadNo + " & Part No " + partNo);
			while (filePartRow != null && !filePartRow.isEmpty()) {
				restClient.createDataPart(filePart, partNo);
				dataPartCount.incrementAndGet();
				if (!restClient.isDataPartSuccess()) {
					throw new DatasetCreatorException("REST insert of Data part failed, part number:  " + partNo);
				}
				logger.info("Adding data part number : " + partNo + " of rows added: " + dataPartCount.get());
				filePartRow = queue.take();
				for (Integer key : filePartRow.keySet()) {
					filePart = filePartRow.get(key);
					partNo = key;
					logger.info("Thread : " + threadNo + " File Name :" + filePart.getName());

				}
				if (filePart != null && FileUtils.END_THREAD.equalsIgnoreCase(filePart.getName())) {
					logger.info("Checking queue and break: " + partNo);
					break;
				}


			}
			done.set(true);
			logger.info("End :" + Thread.currentThread().getName());

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error :" + e.getMessage());
			Thread.currentThread().interrupt();
			DatasetCreator.dataPartErrors.incrementAndGet();
			done.set(true);
			

		}
	}
	
	protected boolean isDone() {
		return done.get();
	}

	protected void initAtomicInteger() {
		
	}
	

	
}


