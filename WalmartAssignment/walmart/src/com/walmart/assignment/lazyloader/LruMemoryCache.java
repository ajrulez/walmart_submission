package com.walmart.assignment.lazyloader;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * LRU Memory Cache is used to store images in memory
 * using a Map while implementing an LRU algorithm
 * of maintaining cache
 */
public class LruMemoryCache {
	// Log Tag
    private static final String TAG = "LruMemoryCache";
    
    // HashMap for LRU Cache
    private Map<String, Bitmap> m_cacheMap = Collections.synchronizedMap(
    		new LinkedHashMap<String, Bitmap>(10,1.5f,true));
    
    // Current size of the Cache
    private long m_cacheSize = 0;
    
    // Capacity of the Cache (in bytes)
    private long m_cacheCapacity = 2 * 1024 * 1024; // 2 MB by default
    
    /**
     * Default constructor for LruMemoryCache
     * 
     */
    public LruMemoryCache() {
    	Log.d(TAG, "LruMemoryCache - Creating LRU Memory Cache with default cache capacity");
    	
    	// By default, use up to 10% of the available Heap in this implementation
        setCacheCapacity(Runtime.getRuntime().maxMemory() / 10);
    }
    
    /**
     * Constructor with specific amount of cache capacity for LruMemoryCache
     * 
     * @param long - Cache capacity in bytes
     */
    public LruMemoryCache(long cacheCapacity) {
    	Log.d(TAG, "LruMemoryCache - Creating LRU Memory Cache with cache capacity = " + ((cacheCapacity / 1024) / 1024) + " MB");
    	
    	// Do not allow more than 25% of the available Heap in this implementation
    	if(Runtime.getRuntime().maxMemory() / 4 > cacheCapacity) {
    		setCacheCapacity(cacheCapacity);
    	}
    	
    	else {
    		Log.w(TAG, "LruMemoryCache() - Unable to initialize cache with capacity = " + ((cacheCapacity / 1024) / 1024) + " MB");
    	}
    }
    
    /**
     * Method to update and change the capacity of this LRU Cache
     * 
     * @param long - new capacity of LRU cache in bytes
     * 
     */
    public void setCacheCapacity(long cacheCapacity){
    	Log.d(TAG, "LRU Memory Cache capacity set to: "+ ((cacheCapacity / 1024) / 1024) +" MB");
    	
    	// Update the capacity
    	m_cacheCapacity = cacheCapacity;
    }

    /**
     * Method to get an Bitmap from LRU Memory Cache using the
     * input URL
     * 
     * @param String - URL to get corresponding bitmap
     * 
     * @return Bitmap - Bitmap bitmap from LRU Cache
     */
    public Bitmap getBitmapFromCache(String URL) {
    	try {
    		// If the bitmap is not in the cache, return null
    		if(! m_cacheMap.containsKey(URL)) {
    			return null;
    		}
    		
    		// bitmap in cache, return the bitmap
            return m_cacheMap.get(URL);
        }
    	
    	catch(Exception e) {
    		Log.e(TAG, "getBitmapFromCache() - LRU Cache caught exception - " + e.getLocalizedMessage());
            return null;
        }
    }

    /** 
     * Method to put a bitmap in the cache along with a URL
     * 
     * @param String - URL associated with the bitmap
     * 	      Bitmap - Bitmap
     */
    public void putBitmapInCache(String url, Bitmap bitmap){
        try {
        	// Update the cache
        	//
        	// If we already have the item in cache, then we need to update the LRU list
            if(m_cacheMap.containsKey(url)) {
            	// We are going to re-add the object size when we update it, so 
            	// decrement it here
            	m_cacheSize -= BitmapUtils.getSizeInBytes(m_cacheMap.get(url));
            }
            
            // Update the LRU cache and the size
            m_cacheMap.put(url, (Bitmap) bitmap);
            m_cacheSize += BitmapUtils.getSizeInBytes((Bitmap) bitmap);
            
            // If our cache has grown to size that is greater than
            // the assigned capacity, then remove least recently used items
            //
            removeLruItems();
        }
        
        catch(Throwable t){
        	Log.e(TAG, "Throwable when adding object to LRU Memory Cache. Throwable: " + t.getLocalizedMessage());
        }
    }
    

    /**
     * Method to clear the LRU Memory Cache
     * 
     */
	public void clearCache() {
        try {
        	m_cacheMap.clear();
        	m_cacheSize = 0;
        }
        
        catch(NullPointerException e){
        	Log.e(TAG, "clearCache() - Exception when clearing cache. Exception: " + e.getLocalizedMessage());
        }
	}

	/**
	 * Method to check the size of the LRU
	 * Memory Cache
	 * 
	 */
	private void removeLruItems() {
        Log.d(TAG, "LRU Memory Cache Size = " + m_cacheSize + " Items = " + m_cacheMap.size());
        
        // If the current size of the LRU Memory cache is
        // greater than the assigned capacity
        if(m_cacheSize > m_cacheCapacity) {
        	// LRU item will be the first one iterated 
        	Iterator<Entry<String, Bitmap>> iter = 
        			m_cacheMap.entrySet().iterator();
        	
        	// Remove LRU items until we are down to a size
        	// less than capacity
            while(iter.hasNext()) {
            	Entry<String, Bitmap> entry = iter.next();
            	// Update the cache size to account for removal of this entry
            	m_cacheSize -= BitmapUtils.getSizeInBytes((Bitmap) entry.getValue());
            	
            	// Remove the item from cache
                iter.remove();
                
                // If we have reached a size lower than our capacity, break out of the loop
                if(m_cacheSize <= m_cacheCapacity) {
                    break;
                }
            }
        }
    }
}
