package com.walmart.assignment.model;

/**
 * This class defines a LoggedInUser. This class is only
 * for demo purposes, and we are only retrieving a few
 * bits of information on a logged in user
 * 
 */
public class LoggedInUser {
	// Location
	private String m_location = "Not Available";
	
	// Name
	private String m_name = "Not Disclosed";
	
	// Organization Names - we'll use a maximum of three
	// organizations for Demo purposes. Best way to do this would
	// be to make a ListView of Organizations in the UI, and create
	// an ArrayList of Organizations
	private String m_organization = "Not Available";
	
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
	 * Method to set the organizations that logged in
	 * user belongs to
	 * 
	 */
	public void setOrganization(String organization) {
		m_organization = organization;
	}
	
	/**
	 * Method to get the organizations that logged in
	 * user belongs to
	 * 
	 */
	public String getOrganization() {
		return m_organization;
	}

}
