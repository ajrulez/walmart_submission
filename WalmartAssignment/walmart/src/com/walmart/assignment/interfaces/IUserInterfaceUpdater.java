package com.walmart.assignment.interfaces;

/**
 * This interface includes several methods that
 * are used to update the user interface. It is basically
 * used to send UI update commands to the Base activity
 * from a Fragment.
 * 
 * In the case of this assignment, we do not have multiple
 * fragments to switch\swap\replace, so this interface
 * is not used much. I am using it more for demo
 * purposes here.
 *
 */
public interface IUserInterfaceUpdater {
	// Method to show an alert dialog
	public void showAlert(String title, String message);
	
	// Method to show Login UI
	public void showLoginUi();
	
	// Method to show UserInformation
	public void showUserInformation();
	
	// Show People in Circle
	public void showPeopleInCircle();
	
	// Update Login UI
	public void updateLoginUi(String fragmentName);
}
