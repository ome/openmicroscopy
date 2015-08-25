/*
 * org.openmicroscopy.shoola.env.data.model.ThumbnailData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


import pojos.ImageData;

/** 
 * Holds a {@link BufferedImage} serving as a thumbnail for a given <i>OME</i>
 * image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailData
    implements DataObject
{

	/** The id of the image to which the thumbnail belong. */
    private long            userID;
    
    /** The id of the image to which the thumbnail belong. */
    private long            imageID;
    
    /** The thumbnail pixels. */
    private BufferedImage   thumbnail;

    /** Flag indicating if the image is a default image or not. */
    private boolean			validImage;
    
    /** Used to store the image. */
    private ImageData		image;
    
    /** Flag indicating that the image required a pyramid to be build.*/
    private Boolean			requirePyramid;
    
    /** The object of reference. */
    private pojos.DataObject refObject;
    
    /** 
     * Exception if not possible to create the object. This should
     * be used when imported.
     */
    private Exception		error;
    
    /**
     * Creates a new instance.
     * 
     * @param imageID    The id of the image to which the thumbnail belong.
     *                   Must be positive.
     * @param thumbnail  The thumbnail pixels.  Mustn't be <code>null</code>.
     * @param userID   	 The id of the user the thumbnail is for.
     *                   Must be positive.
     * @param validImage Pass <code>true</code> if the image is a real image, 
     * 					 <code>false</code> otherwise.
     */
    public ThumbnailData(long imageID, BufferedImage thumbnail, long userID, 
    		boolean validImage)
    {
        if (imageID <= 0) 
            throw new IllegalArgumentException("Non-positive image id: "+
                                               imageID+".");
        this.imageID = imageID;
        this.thumbnail = thumbnail;
        this.userID = userID;
        this.validImage = validImage;
        requirePyramid = null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param imageID    The id of the image to which the thumbnail belong.
     *                   Must be positive.
     * @param thumbnail  The thumbnail pixels. Mustn't be <code>null</code>.
     * @param validImage Pass <code>true</code> if the image is a real image, 
     * 					 <code>false</code> otherwise.
     */
    public ThumbnailData(long imageID, BufferedImage thumbnail, 
    		boolean validImage)
    {
        this(imageID, thumbnail, -1, validImage);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param refOjbect The object of reference. Mustn't be <code>null</code>.
     * @param thumbnail The thumbnail pixels. Mustn't be <code>null</code>.
     * @param validImage Passed <code>true</code> if it is a valid image, 
     * <code>false</code> otherwise.
     */
    public ThumbnailData(pojos.DataObject refOjbect, BufferedImage thumbnail,
    		boolean validImage)
    {
    	  if (refOjbect == null)
    		  throw new IllegalArgumentException("No object.");
    	  if (!(refOjbect instanceof ImageData))
    		  throw new IllegalArgumentException("Type not valid.");
    	  this.refObject = refOjbect;
    	  this.validImage = validImage;
    	  this.thumbnail = thumbnail;
    	  requirePyramid = null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param refOjbect The object of reference. Mustn't be <code>null</code>.
     * @param requirePyramid Pass <code>true</code> if a pyramid is required,
     * 						<code>false</code> otherwise.
     */
    public ThumbnailData(pojos.DataObject refOjbect, Boolean requirePyramid)
    {
        this(refOjbect, null, false);
        this.requirePyramid = requirePyramid;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param refOjbect The object of reference. Mustn't be <code>null</code>.
     * @param thumbnail The thumbnail pixels. Mustn't be <code>null</code>.
     */
    public ThumbnailData(pojos.DataObject refOjbect, BufferedImage thumbnail)
    {
    	  this(refOjbect, thumbnail, true);
    }
    
    /**
     * Sets the time the flag indicating if the image requires a pyramid to be
     * built.
     * 
     * @param requirePyramid The value to set.
     */
    public void setBackOffForPyramid(Boolean requirePyramid)
    {
    	this.requirePyramid = requirePyramid;
    }
    
    /**
     * Sets the exception thrown when trying to create a thumbnail.
     * 
     * @param error The exception to set.
     */
    public void setError(Exception error) { this.error = error; }
    
    /**
     * Returns the exception.
     * 
     * @return See above.
     */
    public Exception getError() { return error; }
    
    /** 
     * Sets the image.
     * 
     * @param image The image to set.
     */
    public void setImage(ImageData image) { this.image = image; }
    
    /**
     * Clones this object.
     * This is a deep-copy, the thumbnail pixels are cloned too.
     * 
     * @see org.openmicroscopy.shoola.env.data.model.DataObject#makeNew()
     */
    public DataObject makeNew()
    {
        BufferedImage pixClone = null;
        if (thumbnail != null) {
        	pixClone = new BufferedImage( thumbnail.getWidth(),
                    thumbnail.getHeight(), 
                    thumbnail.getType());
        	 Graphics2D g2D = pixClone.createGraphics();
             g2D.drawImage(thumbnail, null, 0, 0);
        }
        ThumbnailData data;
        if (refObject != null) {
        	if (this.requirePyramid != null) {
        		data = new ThumbnailData(this.refObject, this.requirePyramid);
        	} else {
        		data = new ThumbnailData(this.refObject, pixClone,
        				this.validImage);
        	}
        } else data = new ThumbnailData(imageID, pixClone, this.validImage);
        data.setImage(this.image);
        return data;
    }

    /**
     * Returns <code>true</code> if the image is a real image, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isValidImage() { return validImage; }
    
    /**
     * Returns the id of the user.
     * 
     * @return See above.
     */
    public long getUserID() { return userID; }
    
    /**
     * Returns the id of the image to which the thumbnail belong.
     * 
     * @return See above.
     */
    public long getImageID() { return imageID; }
    
    /**
     * Returns the thumbnail pixels.
     * 
     * @return See above.
     */
    public BufferedImage getThumbnail() { return thumbnail; }
    
    /**
     * Returns the image.
     * 
     * @return See above.
     */
    public ImageData getImage() { return image; }
    
    /**
     * Returns the object of reference.
     * 
     * @return See above.
     */
    public pojos.DataObject getRefObject() { return refObject; }
    
    /**
     * Returns <code>true</code> if a pyramid is required, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public Boolean requirePyramid()
    { 
    	if (requirePyramid != null) return requirePyramid.booleanValue();
    	return null;
    }

}
