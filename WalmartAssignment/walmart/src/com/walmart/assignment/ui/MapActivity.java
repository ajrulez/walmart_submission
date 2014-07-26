package com.walmart.assignment.ui;

import java.text.DecimalFormat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.walmart.assignment.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity {
	// Google Map
	private GoogleMap m_map;
	
	// Location of user
	private LatLng m_userLocation;
	
	// Intent Key for Location Lat
	public static String INTENT_LOCATION_LAT_KEY = "location_lat";
	
	// Intent Key for Location Lng
	public static String INTENT_LOCATION_LNG_KEY = "location_lng";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_fragment);

        // Get a handle to the Map Fragment
        m_map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        
        // Check if the intent is valid and it has extras
        if(getIntent() != null &&
        		getIntent().getExtras() != null) {
        	// Get Lat and Lng from Intent Extras Bundle
        	String lat = getIntent().getExtras().getString(INTENT_LOCATION_LAT_KEY);
        	String lng = getIntent().getExtras().getString(INTENT_LOCATION_LNG_KEY);
        	
        	// Create LatLng userLocation
        	if(lat != null &&
        			lat.length() >= 2 &&
        			lng != null &&
        			lng.length() >= 2) {
        		// Google Maps has problems showing negative LatLng
        		// Kinda sucks...
        		m_userLocation = new LatLng(doubleToThreeDecimals(Double.parseDouble(lat)), doubleToThreeDecimals(Double.parseDouble(lng)));
        	}
        }
        
        if(m_userLocation != null) {
        	m_map.addMarker(new MarkerOptions()
                	.position(m_userLocation));
        	
        	// Move the camera instantly to User Location with a zoom of 15.
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(m_userLocation, 15));

            // Zoom in, animating the camera.
            m_map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
    }
    
    /**
     * Method to Format a double to exactly three decimal places
     * 
     */
    private double doubleToThreeDecimals(double value) {
    	String strValue = null;
    	try {
    		DecimalFormat df = new DecimalFormat("#.000"); 
    		String newVal = df.format(value);
    		strValue = newVal;
    	}
    	
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	if(strValue != null && 
    			strValue.length() > 0) {
    		return Double.parseDouble(strValue);
    	}
    	
    	// Something went wrong, just return the original value
    	return value;
    }
}
