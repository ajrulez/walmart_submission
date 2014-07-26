package com.walmart.assignment.interfaces;

import android.graphics.Bitmap;

public interface IProfilePictureDownloadReceiver {
	// Method that will get called when Display Picture is downloaded
	public void onProfilePictureDownloaded(Bitmap image);
}
