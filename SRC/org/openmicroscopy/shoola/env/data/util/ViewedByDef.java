/*
 * org.openmicroscopy.shoola.env.data.util.ViewedByDef 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;

//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.ExperimenterData;

/** 
 * Utility class storing the information about the user who viewed
 * the image i.e. the rendering settings, the rating etc.
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
public class ViewedByDef
{

	/** The experimenter this object is for. */
	private ExperimenterData experimenter;
	
	/** The rendering settings set by the experimenter. */ 
	private RndProxyDef		rndSettings;
	
	/** The ratings if any. */
	private Collection 		ratings;
	
	/** The id of the image. */
	private long			imageID;
	
	/** The id of the pixels set. */
	private long			pixelsID;
	
	/** 
	 * String hosting the formatted experimenter, b/c we override the 
	 * {@link #toString()} method.
	 */
	private String			formattedExperimenter;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter	The experimenter this object is for.
	 * @param rndSettings	The rendering settings set by the experimenter.
	 * @param ratings		The ratings if any.
	 */
	public ViewedByDef(ExperimenterData experimenter, RndProxyDef rndSettings, 
						Collection ratings)
	{
		if (experimenter == null)
			throw new IllegalArgumentException("No experimenter specified.");
		if (rndSettings == null)
			throw new IllegalArgumentException("No experimenter specified.");
		this.experimenter = experimenter;
		this.ratings = ratings;
		this.rndSettings = rndSettings;
	}
	
	public void setFormattedExperimenter(String formattedExperimenter)
	{
		this.formattedExperimenter = formattedExperimenter; 
	}
	
	/**
	 * Sets the pixels ID and image ID.
	 * 
	 * @param imageID	The id of the image.
	 * @param pixelsID	The id of the pixels set.
	 */
	public void setIds(long imageID, long pixelsID)
	{ 
		this.imageID = imageID;
		this.pixelsID = pixelsID;
	}
	
	/**
	 * Return the id of the pixels set.
	 * 
	 * @return See above.
	 */
	public long getPixelsID() { return pixelsID; }
	
	/**
	 * Return the id of the image.
	 * 
	 * @return See above.
	 */
	public long getImageID() { return imageID; }
	
	/**
	 * Returns the experimenter hosted by this class.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getExperimenter() { return experimenter; }
	
	/**
	 * Returns the rendering settings set by the experimenter.
	 * 
	 * @return See above.
	 */
	public RndProxyDef getRndSettings() { return rndSettings; }
	
	/**
	 * Returns the ratings if any.
	 * 
	 * @return See above.
	 */
	public Collection getRatings() { return ratings; }
	
	public String toString() { return formattedExperimenter; }
	
}
