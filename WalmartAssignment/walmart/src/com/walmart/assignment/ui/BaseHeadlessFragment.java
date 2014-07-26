package com.walmart.assignment.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import com.walmart.assignment.interfaces.IUserInterfaceUpdater;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * This is the super class for all Fragments that
 * we will use in this app. All fragments that we use
 * need to have certain common properties, and having
 * a super class helps with that.
 * 
 */
public class BaseHeadlessFragment extends Fragment
								  implements OnClickListener {
	// Current Fragment Name
	protected String m_fragmentName = "";
		
	// Flag to see Fragment is Attached to Activity
	protected AtomicBoolean m_fragmentAttached = new AtomicBoolean(false);

	// UI Updater Interface
	protected IUserInterfaceUpdater m_uiUpdater;
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Update the Login US based on Fragment Name
		//
		if(m_uiUpdater != null) {
			m_uiUpdater.updateLoginUi(m_fragmentName);
		}
	}
	
	/**
	 * Method that gets called when Fragment is attached
	 * to an Activity
	 * 
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Get and Set the UI Updater
		m_uiUpdater = (IUserInterfaceUpdater) getActivity();
		
		// Set the attached flag to true
		m_fragmentAttached.compareAndSet(false, true);
		Log.d(m_fragmentName, "Attached to Activity");
	}
	
	/**
	 * Method that gets called when Fragment is detached
	 * from an Activity
	 * 
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		
		// Set the attached flag to false
		m_fragmentAttached.compareAndSet(true, false);
		Log.d(m_fragmentName, "No longer attached to Activity");
	}
	
	/**
	 * Method to get the Fragment name
	 * 
	 * @return Name of the fragment
	 */
	public String getFragmentName() {
		return m_fragmentName;
	}
	
	/**
	 * Utility method to get String from a resource ID
	 * 
	 * @param int - Resource ID for the String
	 * 
	 * @return String - String from R.String...
	 * 
	 */
	protected String getStringFromResources(int stringResourceId) {
		return getResources().getString(stringResourceId);
	}
	
	/**
	 * Method to check if a Fragment is attached
	 * 
	 * @param boolean - true if the Fragment is attached
	 * 					false otherwise
	 */
	public boolean isFragmentAttached() {
		return m_fragmentAttached.get();
	}
	
	/**
	 * OnClickListener - Used by Fragments
	 * that extend this class
	 * 
	 * @param View - view that was clicked
	 * 
	 */
	@Override
	public void onClick(View view) {
		// No default implementation
		//
		// Classes that extend this class, and that want
		// to respond to View clicks will need to override
		// this method
	}
}
