package com.walmart.assignment.lazyloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.walmart.assignment.R;
import com.walmart.assignment.utils.BitmapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * ImageLoader downloads images from Web or uses already
 * downloaded images from external file cache or a LRU memory
 * cache. The obtained images are then loaded on an ImageView
 * which is provided to ImageLoader class.
 * 
 */
public class ImageLoader {
	// Log Tag
	private final static String TAG = "ImageLoader";
	
	// LRU Memory Cache to display Bitmaps
	private LruMemoryCache m_lruMemoryCache = new LruMemoryCache();
	
	// File cache to store Image files on External Storage
    private ExternalFileCache m_externalCache;
    
    // Map of ImageViews to reuse existing ImageViews corresponding to their URL as key
    private Map<ImageView, String> m_mapImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    
    // Executor Service to download Images in parallel
    private ExecutorService m_executorService;
    
    // Maximum number of Images to download in parallel
    private final static int m_maxImageDownload = 6;
    
    // Handler to display images in the UI thread
    private Handler m_handler=new Handler();
    
    
    /**
     * Constructor for ImageLoader
     * 
     * @param Context
     * 
     */
	public ImageLoader(Context context) {
		// Construct an External File Cache
		m_externalCache = new ExternalFileCache(context);
		
		// Set up the ExecutorService
		m_executorService = Executors.newFixedThreadPool(m_maxImageDownload);
	}
	
	/**
	 * This method is used to display an image on the provided
	 * ImageView
	 * 
	 * @param String - URL of the image to display
	 * @param ImageView - ImageView on which to display the image
	 * 
	 */
	public void displayImage(String url, ImageView imageView) {
		// Add the ImageView to the Map
		m_mapImageViews.put(imageView, url);
		
		// Get a Bitmap corresponding to this URL
		// from the LRU Cache
        Bitmap bitmap = m_lruMemoryCache.getBitmapFromCache(url);
        
        // If the image is found in LRU cache, then set the
        // bitmap for ImageView
        if(bitmap!=null) {
        	imageView.setImageBitmap(bitmap);
        }
        
        // Since the bitmap is not found in LRU cache, queue the
        // URL for download, and set the placeholder in ImageView
        else {
        	downloadImage(url, imageView);
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
	}
	
	/**
	 * Method to queue an image download using ExecutorService
	 * 
	 * @param URL - URL to download the image from
	 * @param ImageView - ImageView on which the image will be displayed
	 * 
	 */
    private void downloadImage(String url, ImageView imageView) {
    	ImageToLoad image = new ImageToLoad(url, imageView);
    	m_executorService.submit(new ImageDownloader(image));
    }
    
    // ImageToLoad for the Download Queue
    private class ImageToLoad {
    	// URL of the Image to download
        public String m_url;
        
        // ImageView on which we want to show this image
        public ImageView m_imageView;
        
        // Constructor
        public ImageToLoad(String url, ImageView imageView) {
        	m_url = url;
        	m_imageView = imageView;
        }
    }
    
    // Runnable task that is used to download an ImageToLoad
    class ImageDownloader implements Runnable {
    	// Image to download
    	ImageToLoad m_imageToLoad;
    	
    	// Constructor
    	ImageDownloader(ImageToLoad imageToLoad) {
    		m_imageToLoad = imageToLoad;
        }
        
    	/**
    	 * Run the ImageDownloader in the background
    	 * using ExecutorService
    	 * 
    	 * (non-Javadoc)
    	 * @see java.lang.Runnable#run()
    	 * 
    	 */
        @Override
        public void run() { 
        	try {
        		// If ImageView is being reused, then do not download
        		//
        		if(imageViewReused(m_imageToLoad)) {
                    return;
        		}
        		
        		// ImageView is not being reused, get the Bitmap
                Bitmap bmp = getBitmap(m_imageToLoad.m_url);
                
                // Add the Bitmap to LRU Cache
                m_lruMemoryCache.putBitmapInCache(m_imageToLoad.m_url, bmp);
                
                // If ImageView is being reused, then do not download
                if(imageViewReused(m_imageToLoad)) {
                    return;
                }
                
                BitmapDisplayer bitmapDisplayer = new BitmapDisplayer(bmp, m_imageToLoad);
                
                // Post to main thread
                m_handler.post(bitmapDisplayer);
            }
        	
        	catch(Throwable t){
        		Log.e(TAG, "ImageDownloader - Error thrown when running background task. Throwable: " + t.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Cleass used to display a Bitmap in the
     * UI thread
     * 
     */
    class BitmapDisplayer implements Runnable {
        // Bitmap to display
    	Bitmap m_bitmap;
    	
    	// Image to load for the Bitmap
    	ImageToLoad m_imageToLoad;
    	
    	// Constructor
        public BitmapDisplayer(Bitmap bitmap, ImageToLoad imageToLoad) {
        	m_bitmap = bitmap;
        	m_imageToLoad = imageToLoad;
        }
        
        // Runnable's run method
        public void run() {
        	// If ImageVie is being reused, just return
        	if(imageViewReused(m_imageToLoad)) {
                return;
        	}
        	
        	// If Bitmap is not null, set the image
            if(m_bitmap != null) {
            	m_imageToLoad.m_imageView.setImageBitmap(m_bitmap);
            }
            
            // No Bitmap, use the Placeholder
            else {
            	m_imageToLoad.m_imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
    
    /**
     * Method to check if ImageView is reused to show
     * this image
     * 
     * @param ImageToLoad
     * 
     * @return true - If the ImageView is being reused, false
     * 					otherwise
     * 
     */
    boolean imageViewReused(ImageToLoad imageToLoad) {
        String tag = m_mapImageViews.get(imageToLoad.m_imageView);
        
        if(tag == null || 
        		tag.length() == 0 || 
        		! tag.equals(imageToLoad.m_url)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Method to get a Bitmap from URL
     * 
     * @param String - URL to get a bitmap for
     * 
     * @return Bitmap
     */
    private Bitmap getBitmap(String url) {
    	// Try to get Bitmap from External Cache
    	File bmpFile = m_externalCache.getFileFromCache(url);
    	
    	// Try converting file to Bitmap
    	Bitmap bmp = BitmapUtils.convertFileToBitmap(bmpFile);
    	if(bmp != null) {
    		return bmp;
    	}
        
        // Download bitmap from web if we did not find the bitmap
    	// in external cache
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            
            // OpenConnection to get Image
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            
            // Get response data
            InputStream is = conn.getInputStream();
            
            // Create a new File
            OutputStream os = new FileOutputStream(bmpFile);
            copyStream(is, os);
            os.close();
            conn.disconnect();
            
            // Convert file to Bitmap
            bitmap = BitmapUtils.convertFileToBitmap(bmpFile);
            return bitmap;
        }
        
        catch (Throwable t) {
        	Log.e(TAG, "getBitmap() - Error thrown. Error: " + t.getLocalizedMessage());
        	
        	// If we are out of memory
           if(t instanceof OutOfMemoryError) {
               m_lruMemoryCache.clearCache();
           }
           
           return null;
        }
    }
    
    /**
     * Method to copy input stream to output stream
     * and create a new file with te input data
     * 
     * @param is - Input Stream
     * @param os - Output Stream
     * 
     * @see http://stackoverflow.com/questions/22645895/java-copy-part-of-inputstream-to-outputstream
     * @see http://www.mkyong.com/java/how-to-convert-inputstream-to-file-in-java/
     * 
     */
    private void copyStream(InputStream is, OutputStream os) {
        final int bufferSize = 1024; // 1 KB
        try {
        	byte[] bytes=new byte[bufferSize];
            for(;;) {
            	int count = is.read(bytes, 0, bufferSize);
            	
            	if(count == -1) {
            		break;
            	}
            	
            	// Write to OutputStream
            	os.write(bytes, 0, count);
            }
        }
        catch(Exception e){
        	Log.w(TAG, "copyStream() - Exception when copying input stream to output stream. Exception: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Method to clear the cache
     */
    public void clearCache() {
    	m_externalCache.clearCache();
    	m_lruMemoryCache.clearCache();
    }
}
