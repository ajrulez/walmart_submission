package com.walmart.assignment.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.walmart.assignment.interfaces.IProfilePictureDownloadReceiver;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class ProfilePictureDownloadTask extends AsyncTask<String, Void, Bitmap> {
	// Image Download Receiver
	private IProfilePictureDownloadReceiver m_receiver;
	
	// Constructor
	public ProfilePictureDownloadTask(IProfilePictureDownloadReceiver receiver) {
		// Set the IImageDownloadReceiver object
		m_receiver = receiver;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Nothing to do as we are not showing any UI
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Bitmap doInBackground(String... userIds) {
		if(userIds != null && 
				userIds.length > 0) {
			// Take the first URI
			String displayPictureUri = userIds[0];
			
			// Download the file
			Bitmap bitmap = downloadFile(displayPictureUri);
			return bitmap;
		}
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Bitmap bmp) {
		// Invoke the callback
		m_receiver.onProfilePictureDownloaded(bmp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override
	protected void onCancelled() {
		// Invoke the callback
		m_receiver.onProfilePictureDownloaded(null);
	}
	
	/**
	 * Method to get image from a URL and convert
	 * it to a bitmap
	 * 
	 * @param url
	 * @return Bitmap object
	 * 
	 */
	private Bitmap downloadFile(String url) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
		}
		
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
