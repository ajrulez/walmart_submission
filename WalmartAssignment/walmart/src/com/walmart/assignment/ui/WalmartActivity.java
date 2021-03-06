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
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.walmart.assignment.R;
import com.walmart.assignment.interfaces.IUserInterfaceUpdater;
import com.walmart.assignment.model.PersonInCircle;
import com.walmart.assignment.utils.NetworkUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

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
	
	// Key (String) for saving PersonInCircle List
	private static final String SAVED_CIRCLE_INFORMATION = "saved_circle_information";
	
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
		
		// People in Circle
		SHOW_PEOPLE_IN_CIRCLE,
		
		// MAX
		MAX
	}
	
	
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
        m_signInButton.setOnClickListener(this);;

        // Create a new List for people
        m_peopleList = new ArrayList<PersonInCircle>();

        // Build and get a Google API Client to use Google Play Services
        m_googleApiClient = fetchGoogleApiClient();
        
        // If we are getting re-created
        if(savedInstanceState != null) {
        	Log.d(TAG, "onCreate() - Called on recreation, restore connection state from Bundle");
        	
        	// Get saved login state
        	int nLoginState = savedInstanceState.getInt(SAVED_LOGIN_STATE, LoginState.STATE_NORMAL.ordinal());
        	m_loginState = LoginState.values()[nLoginState];
        	
        	// Get saved People in Circle list
        	if(savedInstanceState.containsKey(SAVED_CIRCLE_INFORMATION)) {
        		m_peopleList = savedInstanceState.getParcelableArrayList(SAVED_CIRCLE_INFORMATION);
        	}
        	
        	// If we are already logged in and we have network
        	if(m_loginState == LoginState.STATE_NORMAL) {
        		m_signInButton.setVisibility(View.GONE);
        		
        		if(m_peopleList != null && 
        				m_peopleList.size() > 0) {
        			updateUi(UiState.SHOW_PEOPLE_IN_CIRCLE);
        		}
        		
        		else {
        			updateUi(UiState.SHOW_LOGIN_UI);
        		}
        	}
        }
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
		
		// Save Circle Information
		outState.putParcelableArrayList(SAVED_CIRCLE_INFORMATION, m_peopleList);
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
	    
	    // Request for people in circles
	    getPeopleInCircles();

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
			
			updateUi(UiState.SHOW_PEOPLE_IN_CIRCLE);
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
		
		switch(state) {
			case SHOW_LOGIN_UI:
				// Remove Fragment
				removeFragment();
				m_currentFragment = null;
				m_signInButton.setVisibility(View.VISIBLE);
				break;
				
			case SHOW_PEOPLE_IN_CIRCLE: 
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
				Bundle bundle = getIntent().getExtras();
				
				if(bundle == null) {
					bundle = new Bundle();
				}
				
				// Add the PeopleInCircle list to bundle to be sent to the Fragment
				bundle.putParcelableArrayList(PeopleInCircleFragment.PEOPLE_IN_CIRCLE_DATA_KEY, m_peopleList);
				peopleInCircleFragment.setArguments(bundle);

				// Add the fragment to the container
				getSupportFragmentManager().beginTransaction()
						.replace(m_fragmentContainer.getId(), peopleInCircleFragment)
						.commit();

				// Update the current Fragment
				m_currentFragment = peopleInCircleFragment;
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
}
