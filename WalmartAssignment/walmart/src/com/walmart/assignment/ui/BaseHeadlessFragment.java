package com.walmart.assignment.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import com.walmart.assignment.interfaces.IUserInterfaceUpdater;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * This is the super class for all Fragments that
 * we will use in this app. All fragments that we use
 * need to have certain common properties, and having
 * a super class helps with that.
 * 
 */
public class BaseHeadlessFragment extends Fragment {
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
	 * Method that gets called when Fragment is attached
	 * to an Activity
	 * 
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Set the attached flag ot true
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
	 * Method to set IUserInterfaceUpdater for this
	 * Fragment
	 * 
	 * @param uiInterfaceUpdater - An object that implements
	 * 		IUserInterfaceUpdater interface
	 * 
	 */
	public void setUserInterfaceUpdater(IUserInterfaceUpdater uiUpdater) {
		m_uiUpdater = uiUpdater;
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
}
