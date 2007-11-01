/*
 * org.openmicroscopy.shoola.agents.imviewer.CategorySaver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryData;
import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CategorySaver
	extends DataLoader	
{

	/** ID indicating to create a new category. */
	public static final int CREATE = 0;
	
	/** ID indicating to classify the image in the passed category. */
	public static final int CLASSIFY = 1;
	
	/** ID indicating to classify the image in the passed category. */
	public static final int CREATE_AND_CLASSIFY = 2;
	
	/** ID indicating to classify the image in the passed category. */
	public static final int DECLASSIFY = 3;
	
	/** One of constants defined by this class. */
	private int 				index;
	
	/** The id of the image to handle. */
	private long 				imageID;
	
	/** 
	 * Collection containing the categories to create or to update depending 
	 * on the index. 
	 */
	private Set<CategoryData> 	data;
	
	/** Collection containing the categories to update.*/
	private Set<CategoryData> 	toUpdate;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Controls if the passed index is valid.
     * 
     * @param i The index to control.
     */
    private void checkIndex(int i)
    {
    	switch (i) {
			case CREATE:
			case CLASSIFY:
			case DECLASSIFY:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Creates a new instance to create a new category and 
     * add the passed image to the newly created category.
     * 
     * @param viewer		The view this loader is for.
     *                  	Mustn't be <code>null</code>.
     * @param imageID		The id of the image.
     * @param data			Collection containing the categories to create
     * 						or to update depending on the index.
     * @param index			One of the constants defined by this class.
     */
	public CategorySaver(ImViewer viewer, long imageID, Set<CategoryData> data,
						int index)
	{
		super(viewer);
		checkIndex(index);
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("Collection not valid.");
		this.imageID = imageID;
		this.data = data;
		this.index = index;
	}
	
	/**
     * Creates a new instance to create a new category and 
     * add the passed image to the newly created category.
     * 
     * @param viewer		The view this loader is for.
     *                  	Mustn't be <code>null</code>.
     * @param imageID		The id of the image.
     * @param data			Collection containing the categories to create.
     * @param toUpdate		Collection containing the categories to update.
     */
	public CategorySaver(ImViewer viewer, long imageID, Set<CategoryData> data,
						Set<CategoryData> toUpdate)
	{
		super(viewer);
		index = CREATE_AND_CLASSIFY;
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("Collection not valid.");
		if (toUpdate == null || toUpdate.size() == 0)
			throw new IllegalArgumentException("Collection not valid.");
		this.imageID = imageID;
		this.data = data;
		this.toUpdate = toUpdate;
	}
	
	/**
     * Retrieves the categories the images is categorised into.
     * @see DataLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case CREATE:
				handle = dhView.createAndClassify(imageID, data, this);
				break;
			case CLASSIFY:
				Set<ImageData> toClassify = new HashSet<ImageData>(1);
				ImageData img = new ImageData();
				img.setId(imageID);
				toClassify.add(img);
				handle = dhView.classify(toClassify, data, this);
				break;
			case DECLASSIFY:
				Set<ImageData> images = new HashSet<ImageData>(1);
				ImageData image = new ImageData();
				image.setId(imageID);
				images.add(image);
				handle = dhView.declassify(images, data, this);
				break;
			case CREATE_AND_CLASSIFY:
				handle = dhView.createAndClassify(imageID, data, toUpdate, 
													this);
		}
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
       if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
       viewer.setImageClassified();
    }
    
}
