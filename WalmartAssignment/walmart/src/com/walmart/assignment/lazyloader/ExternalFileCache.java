package com.walmart.assignment.lazyloader;

import java.io.File;
import android.os.Environment;
import android.content.Context;
import android.util.Log;

/**
 * ExternalFileCache is used to store image files on
 * External Storage (SD Card). 
 *
 */
public class ExternalFileCache {
	// Log Tag
	private final static String TAG = "ExternalFileCache";
	
	// Name of the directory where Images will be saved
	private final static String m_directoryName = "ImageCache";
	
	// Cache Directory - Images will be saved in this directory
    private File m_cacheDirectory;
    
    /**
     * Constructor for ExternalFileCache
     * 
     * @param Context
     */
    public ExternalFileCache(Context context){
        // Check if external storage is mounted
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	Log.v(TAG, "ExternalFileCache() - External storage is mounted, create cache directory");
        	m_cacheDirectory = new File(Environment.getExternalStorageDirectory(), m_directoryName);
        }
        
        // Default to Application's cache directory
        else {
        	Log.v(TAG, "ExternalFileCache() - External storage is not mounted, default to application's cache directory");
        	m_cacheDirectory = context.getCacheDir();
        }
        
        // If cache directory does not exist, then create it
        if(! m_cacheDirectory.exists()) {
        	Log.v(TAG, "ExternalFileCache() - Cache directory does not exist, create it");
        	if(! m_cacheDirectory.mkdir()) {
        		Log.e(TAG, "ExternalFileCache() - Unable to create cache directory. Check Permission WRITE_EXTERNAL_STORAGE");
        	}
        }
    }
    
    /**
     * Method to get an File from external cache based on the URL.
     * 
     * @param String - URL to get the File for
     * 
     * @return File - File from the storage
     */
    public File getFileFromCache(String url) {
    	// File names are hashed by the URL
    	String fileName=String.valueOf(url.hashCode());
    	File file = new File(m_cacheDirectory, fileName);
    	
        return file;
    }
    
    /**
     * Method to clear the cache (i.e the files from storage)
     * 
     */
    public void clearCache(){
    	Log.v(TAG, "clearCache() - Clear the external storage cache");
        File[] fileArray = m_cacheDirectory.listFiles();
        
        if(fileArray == null ||
        		fileArray.length == 0) {
        	Log.v(TAG, "clearCache() - No files cache, nothing to clear");
        	return;
        }

        // Iterate through the file array, and delete the files
        for(File file : fileArray) {
        	file.delete();
        }
    }
}
