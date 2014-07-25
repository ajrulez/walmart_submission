package com.walmart.assignment.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class provides a data model for a friend or a person
 * that exists in logged-in user's circles.
 * 
 */
public class PersonInCircle implements Parcelable {	
	// Name of the person
	private String m_personName;
	
	// Image URL of the person
	private String m_personImageUrl;
	

	/**
	 * Creator method for Parcelable - Required for
	 * implementing Parcelable
	 * 
	 */
	public static final Parcelable.Creator<PersonInCircle> CREATOR = new Parcelable.Creator<PersonInCircle>() {
		public PersonInCircle createFromParcel(Parcel in) {
			return new PersonInCircle(in);
		}

		public PersonInCircle[] newArray(int size) {
			return new PersonInCircle[size];
		}
	};
		
	/**
	 * Default Constructor - Required for implementing Parcelable
	 * 
	 */
	public PersonInCircle() {
		m_personName = "";
		m_personImageUrl = "";
	}
	
	/**
	 * Contructor with a Parcel as input - Required for
	 * implementing Parcelable
	 * 
	 * @param Parcel - Input Parcel to construct an object from
	 * 
	 */
	public PersonInCircle(Parcel in) {
		// Must be done in same order as 
		// writeToParcel method
		//
		m_personName = in.readString();
		m_personImageUrl = in.readString();
    }
	
	/**
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Method to write PersonInCircle object to a Parcel
	 * 
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Must be written in the same order
		// it is read (see constructor)
		dest.writeString(m_personName);
		dest.writeString(m_personImageUrl);
	}

	/***** Getters and Setters *****/
	
	/**
	 * Method to get Person's name
	 * 
	 * @return String - Person Name
	 * 
	 */
	public String getPersonName() {
		return m_personName;
	}

	/**
	 * Method to set a Person's name
	 * 
	 * @param String - Person Name
	 */
	public void setPersonName(String m_personName) {
		this.m_personName = m_personName;
	}

	/**
	 * Method to get a Person's Image URL
	 * 
	 * @return String - Image URL of the person
	 */
	public String getPersonImageUrl() {
		return m_personImageUrl;
	}

	/**
	 * Method to set a Person's Image URL
	 * 
	 * @param String - Image URL of the person
	 */
	public void setPersonImageUrl(String m_personImageUrl) {
		this.m_personImageUrl = m_personImageUrl;
	}
}
