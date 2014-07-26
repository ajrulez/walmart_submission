package com.walmart.assignment.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * This class defines a LoggedInUser. This class is only
 * for demo purposes, and we are only retrieving a few
 * bits of information on a logged in user
 * 
 */
public class LoggedInUser implements Parcelable {
	
	// Class for Orgnization details
	public static class Organization implements Parcelable {
		// Organization type
		private String m_type;
		
		// Name of the Organization
		private String m_orgName;
		
		// Title
		private String m_title;
		
		// Accessors and Mutators
		//
		public String getType() {
			return m_type;
		}
	
		public void setType(String type) {
			m_type = type;
		}
	
		public String getOrganizationName() {
			return m_orgName;
		}
	
		public void setOrganizationName(String orgName) {
			m_orgName = orgName;
		}
	
		public String getTitle() {
			return m_title;
		}
	
		public void setTitle(String title) {
			m_title = title;
		}
		
		// Creator Method for Parcelable
		//
		public static final Parcelable.Creator<Organization> CREATOR
		= new Parcelable.Creator<Organization>() {
			public Organization createFromParcel(Parcel in) {
				return new Organization(in);
			}
			
			public Organization[] newArray(int size) {
				return new Organization[size];
			}
		};
		
		// Default Constructor
		public Organization() {
			m_type = "";
			m_orgName = "";
			m_title = "";
		}
		
		// Constructor with Parcel
		private Organization(Parcel in) {
			// Must be done in order
			//
			//
			m_type = in.readString();
			m_orgName = in.readString();
			m_title = in.readString();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(m_type);
			dest.writeString(m_orgName);
			dest.writeString(m_title);
		}

		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	// Location
	private String m_location = "Not Available";
	
	// Name
	private String m_name = "Not Disclosed";
	
	// Profile Picture URI
	private String m_displayPictureUri;
	
	// User's list of organization
	// Each organization item contains type, title, name, 
	private List<Organization> m_organizationList = new ArrayList<Organization> ();
	
	/**
	 * Method to set the location of logged in user
	 * 
	 */
	public void setLocation(String location) {
		m_location = location;
	}
	
	/**
	 * Method to get the location of logged in user
	 * 
	 */
	public String getLocation() {
		return m_location;
	}
	
	/**
	 * Method to set the name of logged in user
	 * 
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * Method to get the name of logged in user
	 * 
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Method to set the display picture URI
	 * 
	 */
	public void setDisplayPictureUri(String uri) {
		m_displayPictureUri = uri;
	}
	
	/**
	 * Method to get the display picture URI
	 * 
	 */
	public String getDisplayPictureUri() {
		return m_displayPictureUri;
	}
	
	/**
	 * Method to set the organizations that logged in
	 * user belongs to
	 * 
	 */
	public void setOrganization(List<Organization> organizations) {
		m_organizationList = organizations;
	}
	
	/**
	 * Method to get the organization list that logged in
	 * user belongs to
	 * 
	 */
	public List<Organization> getOrganization() {
		return m_organizationList;
	}
	
	// Creator Method for Parcelable
	//
	public static final Parcelable.Creator<LoggedInUser> CREATOR
	= new Parcelable.Creator<LoggedInUser>() {
		public LoggedInUser createFromParcel(Parcel in) {
			return new LoggedInUser(in);
		}
		
		public LoggedInUser[] newArray(int size) {
			return new LoggedInUser[size];
		}
	};
	
	// Default Constructor
	public LoggedInUser() {
		m_name = "";
		m_location = "";
		m_displayPictureUri = "";
	}
	
	// Constructor with Parcel
	private LoggedInUser(Parcel in) {
		// Must be done in order
		//
		//
		m_name = in.readString();
		m_location = in.readString();
		m_displayPictureUri = in.readString();
		in.readList(m_organizationList, Organization.class.getClassLoader());
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(m_name);
		dest.writeString(m_location);
		dest.writeString(m_displayPictureUri);
		dest.writeList(m_organizationList);
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}
}
