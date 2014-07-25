package com.walmart.assignment.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * This class is used to check if we have Network available
 * 
 */
public class NetworkUtils {
	/**
	 * Method checks the NetworkInfo and get it's
	 * connection state
	 * 
	 * @param Context - Context of the app
	 * 
	 * @return boolean - true if connected to network, false
	 * 						otherwise.
	 * 
	 */
	private static boolean isNetAvailable(Context context) {
		try {
			// Get the ConnectivityManager
			ConnectivityManager cm = 
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			// Get NetworkInfo from ConnectivityManager
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    
			// Return true if NetworkInfo is connected
			if (netInfo != null && netInfo.isConnected()) {
				return true;
			}
		}
		
		catch(Exception e) {
			Log.w("NetworkUtils", "isNetAvailable() - Exception in method - " + e.getLocalizedMessage());
		}
	    
	    return false;
	}
	
	/**
	 * This method checks if we are currently online by checking
	 * individual radio's NetworkInfo
	 * 
	 * @param Context - Application
	 * @return
	 */
	public static boolean isOnline(Context context) {
		try {
			// Get the ConnectivityManager
			ConnectivityManager cm = 
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
			// Get NetworkInfo for WiFi from ConnectivityManager
			NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
			// Get NetworkInfo for Mobile from ConnectivityManager
            NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            
            if(wifiInfo != null && 
            		wifiInfo.isConnected()) {
                return true;
            }
            
            if(mobileInfo != null && 
            		mobileInfo.isConnected()) {
            	return true;
            }
        }
		
        catch(Exception e) {
        	Log.w("NetworkUtils", "isOnline() - Exception in method - " + e.getLocalizedMessage());
        }
		
        return isNetAvailable(context);
    }
}
