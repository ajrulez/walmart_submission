package com.walmart.assignment.ui;

import java.util.List;

import com.walmart.assignment.R;
import com.walmart.assignment.model.PersonAdapter;
import com.walmart.assignment.model.PersonInCircle;
import com.walmart.assignment.utils.NetworkUtils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * PeopleInCircleFragment class is used to display information
 * on a user's friends. Currently, we display people's names
 * and their display picture.
 * 
 * This class extends from BaseHeadleass fragment. Although it is
 * not required to do so in this case, but as a general practice,
 * I use a Base fragment for all fragments I work on to get some
 * common attributes (like tags, flags, click handler etc).
 * 
 */
public class PeopleInCircleFragment extends BaseHeadlessFragment {
	// List View for showing People in Circle
	private ListView m_lvFriendsList;
	
	// Clear Cache Button
	private Button m_btnClearCache;
	
	// PersonAdapter for People in Circle ListView
	private PersonAdapter m_personAdapter;
	
	// Key for getting PeopleInCircle list from Bundle
	public static final String PEOPLE_IN_CIRCLE_DATA_KEY = "people_in_circle_list";
	
	// Default Constructor
	public PeopleInCircleFragment() {
		m_fragmentName = "PeopleInCircleFragment";
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
		LinearLayout fragmentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_people, container, false);
		
		// Set the ListView
		m_lvFriendsList = (ListView) fragmentLayout.findViewById(R.id.peopleInCircleList);
		
		// Clear Cache Button
		m_btnClearCache = (Button) fragmentLayout.findViewById(R.id.clearCacheButton);
		
		// Set onClickListener for this button
		m_btnClearCache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Clear the adapter when Clear Cache button is clicked
				m_personAdapter.clearAdapter();
			}
		});
		
		if(m_personAdapter == null) {
			m_personAdapter = new PersonAdapter(getActivity(), null);
			m_lvFriendsList.setAdapter(m_personAdapter);
		}
		
		return fragmentLayout;
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		showPeopleList();
	}
	
	/**
	 * Method to show people list in Fragment
	 * 
	 */
	private void showPeopleList() {
		// If we do not have network, show an error
		if(! NetworkUtils.isOnline(getActivity())) {
			Log.i(m_fragmentName, "showPeopleList() - Cannot show people's list because Network is Not Available");
			
			final String title = getStringFromResources(R.string.error);
			final String message = getStringFromResources(R.string.noNetwork);
			if (m_uiUpdater != null) {
				m_uiUpdater.showAlert(title, message);
			}
			
			// Show Login UI
			if(m_uiUpdater != null) {
				m_uiUpdater.showLoginUi();
			}
		}
		
		// If this Fragment is attached
		if (m_fragmentAttached.get() && getArguments() != null) {
			// Get the friend data from Bundle
			List<PersonInCircle> personInCircleData = getArguments()
					.getParcelableArrayList(PEOPLE_IN_CIRCLE_DATA_KEY);

			if (personInCircleData != null && personInCircleData.size() > 0) {
				m_lvFriendsList.setVisibility(View.VISIBLE);

				// Update the data
				m_personAdapter.updateData(personInCircleData);
			}

			else {
				m_lvFriendsList.setVisibility(View.INVISIBLE);

				final String title = getStringFromResources(R.string.warning);
				final String message = getStringFromResources(R.string.emptyPeopleList);
				if (m_uiUpdater != null) {
					m_uiUpdater.showAlert(title, message);
				}
			}
		}

		else {
			m_lvFriendsList.setVisibility(View.INVISIBLE);

			final String title = getStringFromResources(R.string.warning);
			final String message = getStringFromResources(R.string.emptyPeopleList);
			if (m_uiUpdater != null) {
				m_uiUpdater.showAlert(title, message);
			}
		}
	}
}
