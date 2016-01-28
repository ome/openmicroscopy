/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

import omero.gateway.model.ImageData;

/**
 * Helper class storing information about the movie to create.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class MovieActivityParam
{

	/** The image to create a movie from. */
    private ImageData 				image;
    
    /** The parameters to use.*/
    private MovieExportParam 		param;
    
    /** The select channels. */
    private List<Integer>			channels;
    
    /** The icon associated to the parameters. */
    private Icon					icon;
    
    /**
     * Creates a new instance.
     * 
     * @param param  	The parameters used to create the movie.
     * @param image		The image.
     */
	public MovieActivityParam(MovieExportParam param, ImageData image)
	{
		if (image == null)
			throw new IllegalArgumentException("Image not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		channels = param.getChannels();
		if (channels == null)
			channels = new ArrayList<Integer>();
		this.image = image;
	}
	
	/**
	 * Sets the icon associated to the activity.
	 * 
	 * @param icon The value to set.
	 */
	public void setIcon(Icon icon) { this.icon = icon; }
	
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the image the movie is for.
	 * 
	 * @return See above.
	 */
	public ImageData getImage() { return image; }
	
	/**
	 * Returns the selected channels.
	 * 
	 * @return See above.
	 */
	public List<Integer> getChannels() { return channels; }
	
	/**
	 * Returns the parameters.
	 * 
	 * @return See above.
	 */
	public MovieExportParam getParameters() { return param; }
	
}
