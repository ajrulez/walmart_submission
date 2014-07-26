package com.walmart.assignment.ui;

import java.util.List;

import com.walmart.assignment.R;
import com.walmart.assignment.interfaces.IProfilePictureDownloadReceiver;
import com.walmart.assignment.model.LoggedInUser;
import com.walmart.assignment.model.LoggedInUser.Organization;
import com.walmart.assignment.tasks.ProfilePictureDownloadTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This fragment is used to display user information
 * on the UI of the app
 *
 */
public class LoggedInUserInfoFragment extends BaseHeadlessFragment
									  implements IProfilePictureDownloadReceiver {
	// TextView - Name
	private TextView m_tvName;
	
	// TextView - Location
	private TextView m_tvLocation;
	
	// ImageView - Profile Picture
	private ImageView m_ivProfilePicture;
	
	// List View for showing Organizations
	private ListView m_lvOrganizationDetails;
	
	// Show People in Circles
	private Button m_btnShowPeopleInCircles;
	
	// ImageView for Location
	private ImageView m_ivLocation;
	
	// Organization Details ListView Adapter
	private OrganizationDetailsListAdapter m_organizationDetailsAdapter;
	
	// Logged-in User Data
	private LoggedInUser m_loggedInUser;
	
	// Key for getting LoggedInUserData object from Bundle
	public static final String LOGGED_IN_USER_DATA_KEY = "logged_in_user_data_key";

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
		RelativeLayout fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_user_info, container, false);
		
		// Set the UI widgets
		m_tvName = (TextView) fragmentLayout.findViewById(R.id.textViewNameValue);
		m_tvLocation = (TextView) fragmentLayout.findViewById(R.id.textViewLocationValue);
		m_ivProfilePicture = (ImageView) fragmentLayout.findViewById(R.id.profilePicture);
		m_lvOrganizationDetails = (ListView) fragmentLayout.findViewById(R.id.organizationDetailsListView);
		
		// Set the list <-> list adapter
		if(m_organizationDetailsAdapter == null) {
			m_organizationDetailsAdapter = new OrganizationDetailsListAdapter(getActivity(), R.layout.organization_list_item_layout);
			m_lvOrganizationDetails.setAdapter(m_organizationDetailsAdapter);
		}
		
		// Show People in Circles Button
		m_btnShowPeopleInCircles = (Button) fragmentLayout.findViewById(R.id.findFriendsButton);
		m_btnShowPeopleInCircles.setOnClickListener(this);
		
		// Set ImageView for Location
		m_ivLocation = (ImageView) fragmentLayout.findViewById(R.id.imageViewMapLauncher);
		m_ivLocation.setImageResource(R.drawable.location_icon);
		m_ivLocation.setOnClickListener(this);
		
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
			Log.d(m_fragmentName, "Fragment is attached and fragment has valid bundle");
			
			// Show the Placeholder
			m_ivProfilePicture.setBackgroundResource(R.drawable.ic_placeholder);
			
			// Get loggedInUser from Bundle
			m_loggedInUser = getArguments().getParcelable(LOGGED_IN_USER_DATA_KEY);

			if(m_loggedInUser != null) {
				Log.d(m_fragmentName, "Successfully retrieved loggedInUser from Fragment Bundle");
				
				// Get the URI for display picture and download it
				if(m_loggedInUser.getDisplayPictureUri() != null &&
						m_loggedInUser.getDisplayPictureUri().length() > 0) {
					ProfilePictureDownloadTask pictureDownloadTask = new ProfilePictureDownloadTask(this);
					pictureDownloadTask.execute(new String[] {m_loggedInUser.getDisplayPictureUri()});
				}
				
				m_tvName.setText(m_loggedInUser.getName());
				m_tvLocation.setText(m_loggedInUser.getLocation());
				
				// Do not show Organization details list view if there are no items
				if (m_loggedInUser.getOrganization() == null
						|| m_loggedInUser.getOrganization().isEmpty()) {
					m_lvOrganizationDetails.setVisibility(View.INVISIBLE);
				} 
				else {
					m_lvOrganizationDetails.setVisibility(View.VISIBLE);

					// Update the data
					m_organizationDetailsAdapter.updateData(m_loggedInUser.getOrganization());
				}
			}
			
			else {
				// TODO
			}
		}
		
		else {
			// TODO
		}
	}
	
	/**
	 * View Holder Class to be used
	 * in OrganizationDetailsListAdapter Adapter
	 *
	 */
	static class ViewHolder {
		// Type
		TextView m_tvType;
		
		// Name
		TextView m_tvName;
		
		// Title
		TextView m_tvTitle;
	}
	
	/**
	 * OrganizationDetailsListAdapter class extends ArrayAdapter to support custom
	 * list view that displays Organization objects
	 *
	 */
	private class OrganizationDetailsListAdapter extends ArrayAdapter<Organization> {
		// Data - ArrayList<Organization>
		private List<Organization> arrOrganizationDetails;

		
		// Default Constructor
		public OrganizationDetailsListAdapter(Context context, int resource) {
				super(context, resource);
		}
		
		
		// Update Organization Data
		public synchronized void updateData(final List<Organization> data) {
			Log.i(m_fragmentName, "Updating organization details list");
			
			try {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						arrOrganizationDetails = data;
						notifyDataSetChanged();
					}
				});
			}
			
			catch(Exception e) {
				Log.e(m_fragmentName, e.getMessage());
			}
		}
		
		// getView
		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.organization_list_item_layout, null);
                
                // Create a ViewHolder and store references to the children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.m_tvType = (TextView) convertView.findViewById(R.id.tvOrganizationType);
                holder.m_tvType.setTextColor(getResources().getColor(R.color.CustomGreen));
                
                holder.m_tvName = (TextView) convertView.findViewById(R.id.tvOrganizationName);
                holder.m_tvName.setTextColor(getResources().getColor(R.color.DarkRed));
                // We may want to make Organization Name auto-scroll horizontally
                // as some organization names are too long
                holder.m_tvName.setSelected(true);
                
                holder.m_tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.m_tvTitle.setTextColor(getResources().getColor(R.color.CustomBlue));
					
                convertView.setTag(holder);
            } 
			
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(arrOrganizationDetails != null && 
					arrOrganizationDetails.size() > 0 &&
					position < arrOrganizationDetails.size()) {
				// Bind the data efficiently with the holder.
				holder.m_tvType.setText(arrOrganizationDetails.get(position).getType());
				holder.m_tvName.setText(arrOrganizationDetails.get(position).getOrganizationName());
				holder.m_tvTitle.setText(arrOrganizationDetails.get(position).getTitle());
			}
           
			return convertView;                   
        }
		
		/**
		 * getCount
		 */
		public int getCount() {
			int nSize = 0;
			
			if(arrOrganizationDetails != null) {
				nSize = arrOrganizationDetails.size();
			}
			
			return nSize;
		}
	}

	/**
	 * Callback that is invoked when Profile Image is downloaded
	 * from URI
	 * 
	 * This method will then update the ImageVive
	 * 
	 */
	@Override
	public void onProfilePictureDownloaded(Bitmap image) {
		if(image == null) {
			m_ivProfilePicture.setBackgroundResource(R.drawable.ic_placeholder);
		}
		
		else {
			m_ivProfilePicture.setImageBitmap(image);
		}
	}
	
	/**
	 * OnClickListener
	 */
	@Override
	public void onClick(View v) {
		// If the user tapped on
		// Show People in Circle button
		if(v == m_btnShowPeopleInCircles) {
			// Tell the UI Updater to update the View
			if(m_uiUpdater != null) {
				m_uiUpdater.showPeopleInCircle();
			}
		}
		
		// Since we haven't consumed the Click, let super class
		// try to handle it
		else {
			super.onClick(v);
		}
	}
}
