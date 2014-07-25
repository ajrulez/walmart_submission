package com.walmart.assignment.model;

import java.util.List;

import com.walmart.assignment.R;
import com.walmart.assignment.lazyloader.ImageLoader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Person Adapter class extends BaseAdapter and is used to
 * display information about a Person in user's circle as
 * a list item.
 * 
 */
public class PersonAdapter extends BaseAdapter {
    // PersonInCircle list
    private List<PersonInCircle> m_personData;
    
    // LayoutInflater
    private static LayoutInflater m_inflater = null;
    
    // ImageLoader to support Lazy Loading
    public ImageLoader m_imageLoader; 
    
    
    /**
     * This is the constructor for PersonAdatper
     * 
     * @param Context - Context of the Activity
     * @param List<PersonInCircle> - List of "in-circle"
     * 				friends of the user that will be displayed
     * 
     */
    public PersonAdapter(Context context, List<PersonInCircle> data) {
    	// Set the List<PersonInCircle>
    	m_personData = data;
    	
    	// Get LayoutInflater Service
    	m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	// Create an ImageLoader
    	m_imageLoader = new ImageLoader(context);
    }

    /**
     * Method to set the data for this Adapter
     * 
     * @param List<PersonInCircle> - List of "in-circle"
     * 				friends of the user that will be displayed
     */
    public void setAdapterData(List<PersonInCircle> data) {
    	m_personData = data;
    }
    
    /**
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
	@Override
	public int getCount() {
		// If we have person data List, then return the size
		if(m_personData != null && m_personData.size() > 0) {
			return m_personData.size();
		}
		
		// Person data list is empty or null, return 0
		else {
			return 0;
		}
	}

	/**
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return position;
	}


	/**
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	
	/**
	 * View Holder Class
	 *
	 */
	static class ViewHolder {
		// Friend Name
		TextView m_tvName;
		
		// Image
		ImageView m_tvImage;
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		// If this view in the list hasn't been created, create it
		if(convertView == null) {
			// Inflate the list item layout
            convertView = m_inflater.inflate(R.layout.person_item, null);
            
            // Creates a ViewHolder and store references to the children views
            // we want to bind data to.
            holder = new ViewHolder();
            
            // Person Name
            holder.m_tvName = (TextView) convertView.findViewById(R.id.personName);
            
            // Person Image
            holder.m_tvImage = (ImageView) convertView.findViewById(R.id.personImage);
            
            // Bind the view and holder
            convertView.setTag(holder);
		}
		
		// Reuse existing View
		else {
			// Get the ViewHolder back to get fast access
			holder = (ViewHolder) convertView.getTag();
		}
		
		if(position >= 0 && 
				position < m_personData.size()) {
			// Set the values
			holder.m_tvName.setText(m_personData.get(position).getPersonName());
			
			// Load the Image into ImageView using ImageLoader that manages the cache
			m_imageLoader.displayImage(m_personData.get(position).getPersonImageUrl(), holder.m_tvImage);
		}
        
        return convertView;
	}
	
	/**
	 * This method is called to update the data
	 * in the adapter.
	 * 
	 * @param List<PersonInCircle> - New set of data for
	 * 			the adapter
	 */
	public synchronized void updateData(final List<PersonInCircle> personData) {
		Log.d("PersonAdapter", "updateData() - Updating People In Circle");

		try {
			m_personData = personData;
			
			// Notify the adapter that we have changed the data
			notifyDataSetChanged();
		}
		catch (Exception e) {
			Log.e("PersonAdapter", "updateData() - Exception: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Method to clear the cache
	 */
	public void clearAdapter() {
		m_imageLoader.clearCache();
	}
}
