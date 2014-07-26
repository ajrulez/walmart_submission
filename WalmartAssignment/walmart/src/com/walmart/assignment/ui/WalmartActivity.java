package com.walmart.assignment.ui;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Organizations;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.walmart.assignment.R;
import com.walmart.assignment.interfaces.IUserInterfaceUpdater;
import com.walmart.assignment.model.LoggedInUser;
import com.walmart.assignment.model.LoggedInUser.Organization;
import com.walmart.assignment.model.PersonInCircle;
import com.walmart.assignment.utils.NetworkUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.location.Location;
import android.location.LocationListener;

public class WalmartActivity extends FragmentActivity
							 implements ConnectionCallbacks, // To receive Connection related callback from Google Play Services
							 			OnConnectionFailedListener, // Receive connection failed\error callback with the resolution intent
							 			ResultCallback<LoadPeopleResult>, // Get people result for logged-in user's circles
							 			OnClickListener, // View.OnClickListener to handle button clicks
							 			IUserInterfaceUpdater { // For switching between different views requested by Fragments (not used much in this assignment)
	
	
	// Log Tag
	private static final String TAG = "WalmartActivity";
	
	// Key (String) for saving LoginState
	private static final String SAVED_LOGIN_STATE = "saved_login_state";
	
	// Enum for Signed In State
	private static enum LoginState {
		// Normal
		STATE_NORMAL,
		
		// Signing In - User has clicked on Sign In button
		STATE_SIGNING_IN,
		
		// Resolution in progress - Error resolution for sign in is in progress
		STATE_RESOLUTION_IN_PROGRESS,
		
		// Unknown
		STATE_UNKNOWN
	}
	
	// LoginState of Google+
	private LoginState m_loginState = LoginState.STATE_UNKNOWN;
	
	// Request Code for Sign-In request
	private static final int REQUEST_SIGN_IN = 0;

	// Default Google Play Services Error
	private static final int DIALOG_PLAY_SERVICES_ERROR = 0;
	
	// GoogleApiClient is the main entry point for Google Play services integration.
	private GoogleApiClient m_googleApiClient;

	// PendingIntent most recently returned by Google Play Services
	private PendingIntent m_signInResolutionIntent;
	
	// Error code most recently returned by Google Play services
	private int m_signInErrorCode;
	
	// People (List of Person) in user's circles
	private ArrayList<PersonInCircle> m_peopleList;

	// Sign-In Button - UI Provided by Google Play Services
	private SignInButton m_signInButton;
	
	// Sign Out Button
	private Button m_signOutButton;
	
	// Fragment Container
	protected RelativeLayout m_fragmentContainer;
	
	// Saved Instance Bundle
	protected Bundle m_mainBundle;
	
	// Current Fragment
	protected BaseHeadlessFragment m_currentFragment;
	
	// UI State - Even though we only have one Fragment, I am using
	// this as a general approach to handle multiple Fragment updates
	static enum UiState {
		// Login UI
		SHOW_LOGIN_UI,
		
		// User Information
		SHOW_USER_INFORMATION,
		
		// People in Circle
		SHOW_PEOPLE_IN_CIRCLE,
		
		// User Location
		SHOW_USERS_LOCATION,
		
		// MAX
		MAX
	}
	
	// LoggedInUser - Current uset who is logged in
	private LoggedInUser m_loggedInUser;
	
	// User's Current Location
    //
    // User Location Lat - Per LocationManagaer
    private String m_userLocationMgrLat;
    
    // User Location lng - Per LocationManager
    private String m_userLocationMgrLng;
    
    // LocationManager
    private LocationManager m_locationMgr;
    
	
	/**
	 * Load the activity_walmart Layout, and set up the user interface
	 * 
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// Set the Main Bundle
		m_mainBundle = savedInstanceState;

        // Load the layout
        setContentView(R.layout.activity_walmart);
        
		// Set the Fragment Container
		m_fragmentContainer = (RelativeLayout) findViewById(R.id.fragmentLayout);
        
        // Get the Google Plus sign in button
        m_signInButton = (SignInButton) findViewById(R.id.sign_in_button);
 
        // Set the onClickListener for Sign-In button
        m_signInButton.setOnClickListener(this);
        
        // Sign Out Button
        m_signOutButton = (Button) findViewById(R.id.sign_out_button);
        
        // Set the onClickListener for Sign Out button
        m_signOutButton.setOnClickListener(this);

        // Create a new List for people
        m_peopleList = new ArrayList<PersonInCircle>();

        // Build and get a Google API Client to use Google Play Services
        m_googleApiClient = fetchGoogleApiClient();
        
        // If we are getting re-created
        if(savedInstanceState != null) {
        	Log.d(TAG, "onCreate() - Called on recreation, restore connection state from Bundle");
        	
        	int nLoginState = savedInstanceState.getInt(SAVED_LOGIN_STATE, LoginState.STATE_NORMAL.ordinal());
        	m_loginState = LoginState.values()[nLoginState];
        	
        	// If we are already logged in and we have network
        	if(m_loginState == LoginState.STATE_NORMAL) {
        		m_signInButton.setVisibility(View.GONE);
        		updateUi(UiState.SHOW_USER_INFORMATION);
        	}
        }
        
		// Get LocationManager
		m_locationMgr = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    /**
     * Override onStart because we want to request Google
     * Play Services API to connect
     * 
     */
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.v(TAG, "onStart() - Request Google Play Services to connect the user");
		// Request to connect Google Play Services
		m_googleApiClient.connect();
	}
	
	/**
	 * Override onResume to listen for location
	 * updates
	 * 
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Register for location updates from LocationManager
		// Chances are this app will be tested while indoors, so
		// I have decided to use NETWORK_PROVIDER instead of GPS_PROVIDER
		//
		// Update location every 5 minutes = 1000 ms * 60 = 1 minute * 5 = 5 minutes
		//
		Log.d(TAG, "onResume() - Register for location updates from LocationManager");
		m_locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (1000*60*5), 1000, onLocationChange);
	}
	
	/**
	 * Override onPause to unregister location
	 * update notification listener
	 * 
	 */
	/**
	 * Method to override default Activity Lifecycle
	 * onPause
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		// Unregister for location updates from LocationManager
		Log.d(TAG, "onPause() - Request LocationManager to unregister location updates");
		m_locationMgr.removeUpdates(onLocationChange);
	}

	/**
	 * Override onStop because we want to disconnect the user if they
	 * are already connected
	 * 
	 */
	@Override
	protected void onStop() {
		super.onStop();

		if (m_googleApiClient.isConnected()) {
			Log.v(TAG, "onStop() - Request Google Play Services to disconnect the user");
			m_googleApiClient.disconnect();
		}
	}
	
	/**
	 * Override onSaveInstanceState because we want to save
	 * current login state
	 * 
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save LoginState enum's int value
		outState.putInt(SAVED_LOGIN_STATE, m_loginState.ordinal());
	}
	
	/**
	 * This event callback is invoked when user sign-in activity
	 * returns with result
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SIGN_IN:
				// If the error resolution was successful we should continue
		        // processing errors.
				if(resultCode == RESULT_OK) {
					m_loginState = LoginState.STATE_SIGNING_IN;
				}
				
				// If the error resolution was not successful or the user canceled,
		        // we should stop processing errors.
				else {
					m_loginState = LoginState.STATE_NORMAL;
				}

				// If Google Play services resolved the issue with a dialog then
				// onStart is not called so we need to re-attempt connection here.
				if(!m_googleApiClient.isConnecting()) {
					m_googleApiClient.connect();
				}
				break;
			
			default:
				// Nothing to do
				break;
	    }
	}
	
	/**
	 * This method is used to build and get an instance of GoogleApiCLient
	 * which is used to issue requests to Google Play Services
	 * 
	 * @return GoogleApiClient
	 */
    private GoogleApiClient fetchGoogleApiClient() {
    	Log.v(TAG, "fetchGoogleApiClient() - Building a GoogleApiClient instance");
    	
        return new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this) // Specify that we want to get connection callbacks in this Activity
            .addOnConnectionFailedListener(this) // Specify that this activity is the connection failed listener
            .addApi(Plus.API, Plus.PlusOptions.builder().build()) // Add the APIs we need access to
            .addScope(Plus.SCOPE_PLUS_LOGIN) // Add the scope for OAuth 2.0 for our app
            .build();
     }

    /**
     * This callback is invoked by Google Play Services
     * when a connection to Google Play Services could not
     * be established. 
     * There are multiple reasons that result in connection
     * error.
     * 
     * @param ConnectionResult - Indicates error and resolution information
     * 
     * (non-Javadoc)
     * @see com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)
     */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Note: ConnectionResult error codes are defined at this URL:
		// http://developer.android.com/reference/com/google/android/gms/common/ConnectionResult.html
		Log.d(TAG, "onConnectionFailed: Error Code: " + result.getErrorCode());
		
		// If we are not already resolving an error, we save the resolution
		// Intent that will be used to resolve sign in error later
	    if(m_loginState != LoginState.STATE_RESOLUTION_IN_PROGRESS) {
	    	// Get the Pending Intent which may be used to resolve the error
	    	m_signInResolutionIntent = result.getResolution();
	    	
	    	// Get the error code
	    	m_signInErrorCode = result.getErrorCode();

	    	// Since the current state is signing in, we will continue
	    	// processing the errors
	    	if(m_loginState == LoginState.STATE_SIGNING_IN) {
	    		// Request Sign-In
	    		requestSignIn();
	    	}
	    }
	}
	
	/**
	 * Resolve any connection errors that the user is experiencing or
	 * initiate a sign-in request
	 * 
	 */
	@SuppressWarnings("deprecation")
	private void requestSignIn() {
		Log.v(TAG, "requestSignIn() - Request or Resolve a Sign-In");
		
		// Check Network before proceeding
		if(! NetworkUtils.isOnline(this)) {
			showAlert(getResources().getString(R.string.error), 
					getResources().getString(R.string.noNetwork));
		}
		
		// If we have an intent for sign-in, this may involve user to select an
		// account or to allow this app certain permissions
		if (m_signInResolutionIntent != null) {
			Log.v(TAG, "requestSignIn() - We have a intent for sign-in resolution");

			try {
				// Resolve the sign-in using PendingInten
				m_loginState = LoginState.STATE_RESOLUTION_IN_PROGRESS;
				startIntentSenderForResult(m_signInResolutionIntent.getIntentSender(),
											REQUEST_SIGN_IN,
											null, 0, 0, 0);
			}
			
			catch (SendIntentException e) {
				Log.w(TAG, "requestSignIn() - Sign in intent could not be sent. Error: " + e.getMessage());
				
				// Update the login state, and try to connect
				m_loginState = LoginState.STATE_SIGNING_IN;
				m_googleApiClient.connect();
			}
		}
		
		// No sign-in Intent
		else {
			// Show default error because we don't know what
			// went wrong as there is no intent
			showDialog(DIALOG_PLAY_SERVICES_ERROR);
		}
	}
	
	/**
	 * This callback is invoked when user is successfully
	 * connected to Google Play Services (i.e the user
	 * is considered signed-in)
	 * 
	 * @param Bundle - Data provided to application by Googl
	 *
	 * 
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
	    // Reaching onConnected means we consider the user signed in.
	    Log.v(TAG, "onConnected - User is now connected to Google Play Services");
	    
	    // Hide the Sign In Button 
	    m_signInButton.setVisibility(View.GONE);
	    
	    // Retrieve some information about logged in user
	    Person currentUser = Plus.PeopleApi.getCurrentPerson(m_googleApiClient);
	    
	    if(currentUser != null) {
	    	m_loggedInUser = new LoggedInUser();
	    	String  location = "Not Available";
	    	String name = "Not Disclosed";
	    	
	    	// Location
	    	if(currentUser.hasCurrentLocation()) {
	    		location = currentUser.getCurrentLocation();
	    	}
	    	
	    	// Set location
	    	m_loggedInUser.setLocation(location);
	    	
	    	// Name
	    	if(currentUser.hasName()) {
	    		name = currentUser.getName().getGivenName() + " " + currentUser.getName().getFamilyName();
	    	}
	    	
	    	// Set name
	    	m_loggedInUser.setName(name);
	    	
	    	// Display Image URI
	    	if(currentUser.hasImage()) {
	    		m_loggedInUser.setDisplayPictureUri(currentUser.getImage().getUrl());
	    	}
	    	
	    	// If display image is not available, user Cover Photo
	    	else if(currentUser.hasCover()) {
	    		m_loggedInUser.setDisplayPictureUri(currentUser.getCover().getCoverPhoto().getUrl());
	    	}
	    	
	    	// Organization
	    	if(currentUser.hasOrganizations()) {
	    		ArrayList<Organization> organizationList = new ArrayList<Organization> ();
	    		for(Organizations org : currentUser.getOrganizations()) {
	    			String orgName = "Not Available";
	    			String title = "Unknown";
	    			String type = "N/A";
	    			
	    			if(org.hasName()) {
	    				orgName = org.getName();
	    			}
	    			
	    			if(org.hasTitle()) {
	    				title = org.getTitle();
	    			}
	    			
	    			if(org.hasType()) {
	    				if(org.getType() == Person.Organizations.Type.SCHOOL) {
	    					type = "School";
	    				}
	    				else {
	    					type = "Work";
	    				}
	    			}
	    			
	    			// Create a new Organization object
	    			Organization organization = new Organization();
	    			organization.setOrganizationName(orgName);
	    			organization.setTitle(title);
	    			organization.setType(type);
	    			
	    			// Add to list of Organizations
	    			organizationList.add(organization);
	    			
	    		}

	    		// Set organization list
	    		m_loggedInUser.setOrganization(organizationList);
	    	}
	    }
	    
	    // Request for people in circles
	    getPeopleInCircles();
	    
	    // Show user information
	    updateUi(UiState.SHOW_USER_INFORMATION);

	    // Indicate that the sign in process is complete.
	    m_loginState = LoginState.STATE_NORMAL;
	}

	/**
	 * This callback is invoked when connection to Google Play
	 * Services is lost.
	 * 
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnectionSuspended(int)
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(TAG, "onConnectionSuspended() - Connection to Google Play Services lost. Cause: " + cause);
		
	    // Call connect() to attempt to re-establish the connection or get a
	    // ConnectionResult that we can attempt to resolve.
	    m_googleApiClient.connect();
	}
	
	/**
	 * This callback is invoked when PeopleApi.loadVisible response
	 * is received (i.e. when list of people in user's circles are
	 * fetched).
	 * 
	 *  @param LoadPeopleResult - Result of people in circles
	 *  
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.ResultCallback#onResult(com.google.android.gms.common.api.Result)
	 */
	@Override
	public void onResult(LoadPeopleResult circleData) {
		Log.v(TAG, "onResult(LoadPeopleResult) - Retrieved result for people in circles request");
		
		// If the request was successful
		if(circleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
			// Clear existing results
			m_peopleList.clear();
			
			// Get People Buffer
			PersonBuffer people = circleData.getPersonBuffer();
			
			try {
				int nCount = people.getCount();
				Log.v(TAG, String.format("onResult(LoadPeopleResult) - Retrieved %d people in circles", nCount));
				
				// Get information on each person from People Buffer
				for(int ndx = 0; ndx < nCount; ndx++) {
					PersonInCircle person = new PersonInCircle();
					person.setPersonName(people.get(ndx).getDisplayName());
					person.setPersonImageUrl(people.get(ndx).getImage().getUrl());
					
					m_peopleList.add(person);
				}
			}
			
			catch(Exception e) {
				Log.w(TAG, "onResult(LoadPeopleResult) - Exception when processing result" + e.getMessage());
			}
			
			finally {
				people.close();
			}
		}
		
		else {
			Log.v(TAG, "onResult(LoadPeopleResult) - Error requesting people in circles: " + circleData.getStatus());
		}
	}
	
	/**
	 * This is a utility method to get people (friends\followers)
	 * in logged-in user's circles
	 * 
	 * @return boolean - true if request was successful, false
	 * 						if request was unsuccessful.
	 * 
	 */
	private boolean getPeopleInCircles() {
		Log.v(TAG, "getPeopleInCircles - Requesting people in circles");
		
		// Cannot make a request if the user is not
		// connected
		if(m_googleApiClient.isConnected()) {
			Plus.PeopleApi.loadVisible(m_googleApiClient, null)
	        .setResultCallback(this);
			
			return true;
		}
		
		Log.w(TAG, "getPeopleInCircles - Cannot request people in circles because user is not connected");
		return false;
	}

	/**
	 * This click listener is called when buttons\views on this
	 * Activity are clicked
	 * 
	 * @param View - View in this activity that was clicked
	 * 
	 */
	@Override
	public void onClick(View v) {
		// If we are not already in a connecting state
		if(! m_googleApiClient.isConnecting()) {
			switch(v.getId()) {
				
				// Sign-in button clicked
				case R.id.sign_in_button:
					Log.d(TAG, "onClick() - Sign-in button tapped");
					requestSignIn();
					break;
					
				// Sign Out button clicked
				case R.id.sign_out_button:
					Log.d(TAG, "onClick() - Sign out button tapped");
					requestSignOut();
					break;
			
				default:
					// Nothing to do
					break;
		    }
		}
	}
	
	/**
	 * Show any error dialog realted to the sign-in process
	 * 
	 * @param int - Dialog ID
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PLAY_SERVICES_ERROR:
			// If the error is recoverable by user
			if (GooglePlayServicesUtil
					.isUserRecoverableError(m_signInErrorCode)) {
				return GooglePlayServicesUtil.getErrorDialog(m_signInErrorCode,
						this, REQUEST_SIGN_IN,
						new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Log.e(TAG,
										"Google Play services resolution cancelled");
								m_loginState = LoginState.STATE_NORMAL;
							}
						});
			}
			
			else {
				return new AlertDialog.Builder(this)
						.setMessage(R.string.play_services_error)
						.setPositiveButton(R.string.close,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Log.e(TAG,
												"Google Play services error could not be "
														+ "resolved: "
														+ m_signInErrorCode);
										m_loginState = LoginState.STATE_NORMAL;
									}
								}).create();
			}
		default:
			return super.onCreateDialog(id);
		}
	}
	
	/**
	 * Update UI Method is used to switch between
	 * fragments. 
	 * 
	 * It is not used much in this application because
	 * we only have one fragment. I am providing this as
	 * a general example.
	 * 
	 * @param UiState - State to switch the UI to
	 * 
	 */
	private void updateUi(UiState state) {
		Bundle bundle;
		
		switch(state) {
			case SHOW_LOGIN_UI:
				// Remove Fragment
				removeFragment();
				m_currentFragment = null;
				m_signInButton.setVisibility(View.VISIBLE);
				m_signOutButton.setVisibility(View.GONE);
				break;
			
			case SHOW_USER_INFORMATION:
				// Chck that the activity is using the container for fragments
				//
				if (m_fragmentContainer != null) {

					// However, if we're being restored from a previous state,
					// then we don't need to do anything and should return or else
					// we could end up with overlapping fragments.
					if (m_mainBundle != null) {
						Log.e(TAG, "updateUi() - UI cannot be updated because mainBundle of the app is not null");
						return;
					}
				}
				
				// If we are already showing LoggedInUserInfoFragment Fragment
				if(m_currentFragment != null &&
						m_currentFragment instanceof LoggedInUserInfoFragment &&
						m_currentFragment.isFragmentAttached()) {
					Log.d(TAG, "updateUi() - Already showing LoggedInUserInfoFragment, Nothing to update");
					return;
				}
				
				// Create an instance of LoggedInUserInfoFragment
				LoggedInUserInfoFragment loggedInUserFragment = new LoggedInUserInfoFragment();

				// In case this activity was started with special instructions
				// from an Intent,
				// pass the Intent's extras to the fragment as arguments
				bundle = getIntent().getExtras();
				
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add the LoggedInUser object to bundle to be sent to the Fragment
				bundle.putParcelable(LoggedInUserInfoFragment.LOGGED_IN_USER_DATA_KEY, m_loggedInUser);
				loggedInUserFragment.setArguments(bundle);
				
				// Add the fragment to the container
				getSupportFragmentManager().beginTransaction()
						.replace(m_fragmentContainer.getId(), loggedInUserFragment)
						.commit();

				// Update the current Fragment
				m_currentFragment = loggedInUserFragment;
				
				break;
				
			case SHOW_PEOPLE_IN_CIRCLE: 
				// Check that the activity is using container for fragments
				//
				if (m_fragmentContainer != null) {

					// However, if we're being restored from a previous state,
					// then we don't need to do anything and should return or else
					// we could end up with overlapping fragments.
					if (m_mainBundle != null) {
						Log.e(TAG, "updateUi() - UI cannot be updated because mainBundle of the app is not null");
						return;
					}
				}
				
				// If we are already showing PeopleInCircleFragment Fragment
				if(m_currentFragment != null &&
						m_currentFragment instanceof PeopleInCircleFragment &&
						m_currentFragment.isFragmentAttached()) {
					Log.d(TAG, "updateUi() - Already showing PeopleInCircleFragment, Nothing to update");
					return;
				}
				
				// Create an instance of PeopleInCircleFragment
				PeopleInCircleFragment peopleInCircleFragment = new PeopleInCircleFragment();

				// In case this activity was started with special instructions
				// from an Intent,
				// pass the Intent's extras to the fragment as arguments
				bundle = getIntent().getExtras();
				
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add the PeopleInCircle list to bundle to be sent to the Fragment
				bundle.putParcelableArrayList(PeopleInCircleFragment.PEOPLE_IN_CIRCLE_DATA_KEY, m_peopleList);
				peopleInCircleFragment.setArguments(bundle);

				// Add the fragment to the container
				getSupportFragmentManager().beginTransaction()
						.replace(m_fragmentContainer.getId(), peopleInCircleFragment)
						.addToBackStack(null)
						.commit();

				// Update the current Fragment
				m_currentFragment = peopleInCircleFragment;
				break;
			
			case SHOW_USERS_LOCATION:
				Log.d(TAG, "Attempting to update the UI to user's location on Map");
				Intent launchMapActivity = new Intent(WalmartActivity.this, MapActivity.class);
				launchMapActivity.putExtra(MapActivity.INTENT_LOCATION_LAT_KEY, m_userLocationMgrLat);
				launchMapActivity.putExtra(MapActivity.INTENT_LOCATION_LNG_KEY, m_userLocationMgrLng);
					
				startActivity(launchMapActivity);
				break;
				
			default:
				// Nothing to do
				break;
		}
	}

	/**
	 * This method is used to show an AlertDialog in case
	 * of Error or Warnings, when an action cannot be
	 * completed
	 * 
	 * @param String - Title of the AlertDialog
	 * @param String - Message of the AlertDialog
	 */
	@Override
	public void showAlert(final String title, final String message) {
		runOnUiThread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				AlertDialog alertDialog1 = new AlertDialog.Builder(WalmartActivity.this).create();

				// Setting Dialog Title
		        alertDialog1.setTitle(title);

		        // Setting Dialog Message
		        alertDialog1.setMessage(message);

		        // Setting Icon to Dialog
		        alertDialog1.setIcon(R.drawable.ic_launcher);

		        // Setting OK Button
		        alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int which) {
		            	// Nothing to do
		            }
		        });

		        // Showing Alert Message
		        alertDialog1.show();
			}
		});
	}
	
	/**
	 * Method to show login UI
	 * 
	 */
	@Override
	public void showLoginUi() {
		Log.d(TAG, "showLoginUi() - Updating UI to show Login screen");
		updateUi(UiState.SHOW_LOGIN_UI);
	}
	
	/**
	 * Utility method to remove current fragment
	 * 
	 * This is related to Fragment management strategy
	 * explained earlier in comments in this source file
	 */
	private void removeFragment() {
		Log.d(TAG, "removeFragment() - Method to remove current fragment invoked");
		
		// If we have any other Fragment, remove it first
		if (m_currentFragment != null
				&& m_currentFragment.isFragmentAttached()) {
			Log.d(TAG, "Attempting to remove current fragment");
			getSupportFragmentManager().beginTransaction()
					.remove(m_currentFragment).commit();
		}
		
		else {
			Log.d(TAG, "Cannot remove current fragment because current fragment is null or is not attached");
		}
	}

	/**
	 * Show user information View
	 * 
	 */
	@Override
	public void showUserInformation() {
		Log.d(TAG, "showUserInformation() - Update UI to show user information fragment");
		updateUi(UiState.SHOW_USER_INFORMATION);
	}
	
	/**
	 * Show people in user's circle View
	 */
	@Override
	public void showPeopleInCircle() {
		Log.d(TAG, "showPeopleInCircle() - Update UI to show people in circle fragment");
		updateUi(UiState.SHOW_PEOPLE_IN_CIRCLE);
	}
	
	/**
	 * Method to sign out and revoke user
	 * access on this device until they sign in
	 * 
	 */
	private void requestSignOut() {
		Log.d(TAG, "requestSignOut() - Method to sign out of Google Plus invoked");
		
		Plus.AccountApi.clearDefaultAccount(m_googleApiClient);
		m_googleApiClient.disconnect();
		m_googleApiClient.connect();
		
		/** Not sure if we want to revoke the access
		 * on this device on sign out. If we want to, 
		 * here's the code
		 * 
		 * 
		 * Plus.AccountApi.clearDefaultAccount(m_googleApiClient);
		 * Plus.AccountApi.revokeAccessAndDisconnect(m_googleApiClient);
		 * m_googleApiClient = fetchGoogleApiClient();
		 * m_googleApiClient.connect();
		 */
		
		// Update the UI
		updateUi(UiState.SHOW_LOGIN_UI);
	}
	
	/**
	 * Method to update Sign In and Sign Out buttons
	 * 
	 */
	public void updateLoginUi(String fragmentName) {
		Log.d(TAG, "updateLoginUi() - Updating login UI for Fragment Name " + fragmentName);
		
		if(fragmentName.equalsIgnoreCase("PeopleInCircleFragment")) {
			m_signInButton.setVisibility(View.GONE);
			m_signOutButton.setVisibility(View.GONE);
		}
		
		else if(fragmentName.equalsIgnoreCase("LoggedInUserInfoFragment")) {
			m_signInButton.setVisibility(View.GONE);
			m_signOutButton.setVisibility(View.VISIBLE);
		}
	}
	
	/************************** Location Related Functionality *************************************/
	
	/**
	 * This method is used to user's current location
	 * on a Map
	 * 
	 */
	@Override
	public void showLocation() {
		Log.d(TAG, "showLocation() - Updating UI to show user's current location");
		
		if(m_userLocationMgrLat != null &&
				m_userLocationMgrLat.length() > 0 &&
				m_userLocationMgrLng != null &&
				m_userLocationMgrLng.length() > 0) {
			Log.d(TAG, "We have user's most recent current location available. Use that location to show in the Map");
			
			// Update the UI
			this.updateUi(UiState.SHOW_USERS_LOCATION);
		}
		
		else {
			String title = getResources().getString(R.string.error);
			String message = getResources().getString(R.string.locationUnavailable);
			showAlert(title, message);
		}
	}
	
	/**
	 * Location Listener to listen for location change notifications
	 * 
	 */
	public LocationListener onLocationChange = new LocationListener() {
	    public void onLocationChanged(Location location) {
	    	Log.d(TAG, "Received location update from LocationManager");
	    	if(location != null) {
	    		m_userLocationMgrLat = String.valueOf(location.getLatitude());
	    		m_userLocationMgrLng = String.valueOf(location.getLongitude());
	    	}
	    }
	    
	    public void onProviderDisabled(String provider) {
	      // required for interface, not used
	    }
	    
	    public void onProviderEnabled(String provider) {
	      // required for interface, not used
	    }
	    
	    public void onStatusChanged(String provider, int status,
	                                  Bundle extras) {
	      // required for interface, not used
	    }
	 };
}
