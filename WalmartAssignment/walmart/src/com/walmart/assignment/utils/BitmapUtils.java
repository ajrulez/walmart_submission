package com.walmart.assignment.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Utility class that has method for Bitmap manipulation
 * 
 */
public class BitmapUtils {
	/**
	 * Method to decode an Image File to a Bitmap
	 * 
	 * @param File - (Image) File to decode as a Bitmap
	 * 
	 * @return Bitmap - Decoded Bitmap
	 * 
	 * @see http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 * @see http://stackoverflow.com/questions/13678156/decode-the-file-to-bitmap-in-android-throwing-exception
	 * 
	 */
	public static Bitmap convertFileToBitmap(File inputFile) {
		try {
			// Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            FileInputStream stream1 = new FileInputStream(inputFile);
            
            BitmapFactory.decodeStream(stream1, null, options);
            stream1.close();
            
            // Find the correct scale value
            final int DESIRED_SIZE = 70;
            
            int tempWidth = options.outWidth;
            int tempHeight = options.outHeight;
            int scale = 1;
            
            while(true) {
            	if(tempWidth / 2 < DESIRED_SIZE || tempHeight / 2 < DESIRED_SIZE) {
                    break;
            	}
            	
            	tempWidth /= 2;
            	tempHeight /= 2;
                scale *= 2;
            }
            
            // Decode with scale
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            
            FileInputStream stream2 = new FileInputStream(inputFile);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, options2);
            stream2.close();
            return bitmap;
        }
		
		catch (FileNotFoundException e) {
			Log.w("BitmapUtils", "convertFileToBitmap() - File not found exception: " + e.getLocalizedMessage());
        } 
        catch (IOException e) {
        	Log.w("BitmapUtils", "convertFileToBitmap() - Input-Output exception: " + e.getLocalizedMessage());
        }
		catch(Exception e) {
			Log.w("BitmapUtils", "convertFileToBitmap() - Exception: " + e.getLocalizedMessage());
		}
		
        return null;
    }
	
	/**
	 * Method to get a Bitmap's size in bytes
	 * 
	 * @param Bitmap - bitmap to get size for
	 * 
	 * @return long - Size (in bytes) of the Bitmap object
	 * 
	 */
	public static long getSizeInBytes(Bitmap bitmap) {
		if(bitmap == null) {
			return 0;
		}
		
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
