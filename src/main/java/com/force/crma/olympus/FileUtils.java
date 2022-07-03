package com.force.crma.olympus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;




import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;






public class FileUtils {
	
	protected static final String FILE_EXT_CSV="csv";
	protected static final String FILE_EXT_ZIP="zip";
	protected static final String END_THREAD = "END_FILE_PART";
	
	private final static Logger logger = Logger.getLogger(FileUtils.class.getName());

	public FileUtils() {
		// TODO Auto-generated constructor stub
	}
	
	//delete files quietly
	private static  void  cleanExistingFile(File aFile){
		if(aFile!=null&& aFile.exists()){
				aFile.delete();
			
			if(aFile.exists()){
				logger.log(Level.FINE, "Failed to clean up temporary file : "+aFile.getName());
			}else{
				logger.log(Level.FINE,"Removed temporary file : "+aFile.getName());
			}
		}
	}
	

	
	public static void deleteQuietly (Map<Integer, File> fileParts) {
		for (Map.Entry<Integer, File> entry : fileParts.entrySet()) {
			cleanExistingFile(entry.getValue());						
		}
		
	}
	
	

	//Gzips single file
    protected  static boolean gzipFile(File aDataFile, File aGZIPFile) {
    	logger.log(Level.INFO,"In gzipFile");
    	FileInputStream fis =null;
    	FileOutputStream fos=null;
    	GZIPOutputStream gzipos=null;
    	boolean gzipOK=false;
        try {
             fis = new FileInputStream(aDataFile);
             fos = new FileOutputStream(aGZIPFile);
             gzipos = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipos.write(buffer, 0, len);
            }
         gzipOK= true;

        } catch (IOException e) {
           logger.log(Level.WARNING,"Unable to gzip file ",e);
           //do not throw
        }finally{
        	if(gzipos!=null)try{gzipos.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}     	
    		if(fis!=null)try{fis.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}
    		if(fos!=null)try{fos.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}       	
        	
        }
        return gzipOK;
         
    }
    
    protected  File  unzip(File aGZIPFile) throws IOException {
    	logger.log(Level.INFO,"In unzip");
    	FileInputStream fis =null;
    	FileOutputStream fos=null;
    	GZIPInputStream gzipis=null;
    	File csvFilePart=new File(aGZIPFile.getName()+"."+FILE_EXT_CSV);
        try {
             fis = new FileInputStream(aGZIPFile);
             fos = new FileOutputStream(csvFilePart);
             gzipis = new GZIPInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while((len=gzipis.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }

        } catch (IOException e) {
           logger.log(Level.WARNING,"Unable to unzip file ",e);
           throw e;
        }finally{
        	if(gzipis!=null)try{gzipis.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}     	
    		if(fis!=null)try{fis.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}
    		if(fos!=null)try{fos.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}       	
        	
        }
        return csvFilePart;
         
    }
    


    
    //detect if compressed file is GZip
    protected static boolean detectGZipped(File aDataFile) {
    	  int magic = 0;
    	  try {
    	   RandomAccessFile raf = new RandomAccessFile(aDataFile, "r");
    	   magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
    	   raf.close();
    	  } catch (Throwable e) {
    	   logger.log(Level.WARNING,"Unable to detect if file is GZipped " +e.getMessage());
    	  }
    	  return magic == GZIPInputStream.GZIP_MAGIC;
    	 }
	
    

 		protected static Map<Integer,File> chunkBinary(File aDataFile) throws DatasetCreatorException{
 		
 			ConcurrentHashMap<Integer,File> filePartMap = new ConcurrentHashMap<Integer,File>();
 			//split file into chunks
 			logger.info("In chunkBinary");
 			String fileExt="."+getFileExt(aDataFile);
 			String fileBaseName= getBaseName(aDataFile);
 			int maxBytesToChunk=DatasetCreator.MAX_BYTES_PER_CHUNK;

 			byte[] buffer =new byte[maxBytesToChunk];
 			FileOutputStream tmpOut = null;
 			FileInputStream fis=null;
 			BufferedInputStream bis=null;
 			try{
 				fis=new FileInputStream(aDataFile);
 				bis=new BufferedInputStream(fis);
 				int bytesRead=0;
 				int filePartNo=1;
 				while((bytesRead=bis.read(buffer))>0){
 					logger.log(Level.INFO,"Creating file part number "+filePartNo);
 					File filePart=new File(fileBaseName+".part."+filePartNo +fileExt);
 					
 					cleanExistingFile(filePart);
 					tmpOut=new FileOutputStream(filePart);
 					tmpOut.write(buffer,0,bytesRead);
 					tmpOut.close();
 					tmpOut=null;
 			        filePartMap.put(filePartNo, filePart);
 					filePartNo++;
 				}

 			}catch(IOException ioe){
 				throw new DatasetCreatorException("Unable to create data file part "+ ioe.getMessage(), ioe.getCause());
 			}finally {
 				if(bis!=null)try{fis.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}
 				if(fis!=null)try{fis.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}
 				if(tmpOut!=null)try{tmpOut.close();}catch(IOException ioe){ logger.log(Level.WARNING,ioe.getMessage());}
 			}
 		
 			return filePartMap;	
 			
 		}
 		
   protected byte[] readFile(File aDataFile) throws IOException {
	   Path path = Paths.get(aDataFile.getPath());
	      return Files.readAllBytes(path);
	   
   }
    
   protected  byte[] getBase64ByteArray(String aFileString) {
	   return Base64.getEncoder().encode(aFileString.getBytes());
   }
   
   protected  String getBase64String(byte[] aFileBytes) {
	   return Base64.getEncoder().encodeToString(aFileBytes);
   }
   
   protected static String getBaseName(File  aFile) {
	   String fileName =aFile.getName();
	 		String bN = null;
	 		if (fileName.contains(".")) {
	 			int i = fileName.lastIndexOf('.');
	 			bN = i > 0 ? fileName.substring(0,fileName.length() - i) : "";
	 		}
	   	return bN;
   }
   
	
   protected static String getFileExt(File aFile){
   	  String fileName =aFile.getName();
 		String fe = null;
 		if (fileName.contains(".")) {
 			int i = fileName.lastIndexOf('.');
 			fe = i > 0 ? fileName.substring(i + 1) : "";
 		}
   	return fe;

   }

}
