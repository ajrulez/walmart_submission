package com.walmart.assignment.ui;

import com.walmart.assignment.R;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This fragment is used to display user information
 * on the UI of the app
 *
 */
public class LoggedInUserInfoFragment extends BaseHeadlessFragment {
	// TextView for name
	private TextView m_tvName;
	
	// TextView for Location
	private TextView m_tvLocation;
	
	// TextView for Organization
	private TextView m_tvOrganization;
	
	// Key to retrieve name text from bundle
	public static final String BUNDLE_KEY_USER_NAME = "key_user_name";
	
	// Key to retrieve user location from bundle
	public static final String BUNDLE_KEY_USER_LOCATION = "key_user_location";
	
	// Key to retrieve user organization from bundle
	public static final String BUNDLE_KEY_USER_ORGANIZATION = "key_user_organization";
	
	// Default Constructor
	public LoggedInUserInfoFragment() {
		m_fragmentName = "LoggedInUserInfoFragment";
	}
	
	/**
	 * (non-Javadoc)
	 * @see com.alifesoftware.assignment.BaseHeadlessFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, 
								ViewGroup container, Bundle savedInstanceState) {
		// Inflate the Fragment layout
		LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_user_info, container, false);
		
		// Set the Name TextView
		m_tvName = (TextView) fragmentLayout.findViewById(R.id.userName);
		m_tvName.setTextColor(Color.RED);
		
		// Set the Location TextView
		m_tvLocation = (TextView) fragmentLayout.findViewById(R.id.location);
		m_tvLocation.setTextColor(Color.BLACK);
		
		// Set the Organization TextView
		m_tvOrganization = (TextView) fragmentLayout.findViewById(R.id.organizations);
		m_tvOrganization.setTextColor(Color.BLUE);
		
		return fragmentLayout;
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		showUserInformation();
	}

	/**
	 * Method to show logged in user information
	 * on this Fragent
	 * 
	 */
	private void showUserInformation() {
		// If this Fragment is attached
		if (m_fragmentAttached.get() && 
				getArguments() != null) {
			
			// Get the friend data from Bundle
			m_tvName.setText(getResources().getString(R.string.nameLabel) + " " + getArguments().getString(BUNDLE_KEY_USER_NAME));
			m_tvLocation.setText(getResources().getString(R.string.locationLabel) + " " + getArguments().getString(BUNDLE_KEY_USER_LOCATION));
			m_tvOrganization.setText(getArguments().getString(BUNDLE_KEY_USER_ORGANIZATION));
		}					
	}
}
