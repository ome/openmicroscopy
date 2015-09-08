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

import java.io.File;
import javax.swing.Icon;

import org.openmicroscopy.shoola.env.data.util.Target;

import omero.gateway.model.ImageData;

/**
 * Helper class storing information about the image to export.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ExportActivityParam
{

	/** Indicates to export the image as OME TIFF. */
	public static final int	EXPORT_AS_OME_TIFF = 0;
	
	/** The image to export. */
    private ImageData 	image;
    
    /** The folder where to export the image. */
    private File		folder; 
    
    /** One of the constants defined by this class. */
    private int			index;
    
    /** The icon associated to the parameters. */
    private Icon		icon;

    /** The selected schema.*/
    private Target target;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param folder The folder where to store the exported file.
	 * @param image	 The image to export.
	 * @param index	 One of the constants defined by this class.
	 * @param target The selected schema.
	 */
	public ExportActivityParam(File folder, ImageData image, int index,
			Target target)
	{
		if (image == null)
			throw new IllegalArgumentException("No image to export");
		if (folder == null)
			throw new IllegalArgumentException("No image name");
		this.target = target;
		this.image = image;
		this.folder = folder;
		this.index = index;
	}

	/**
	 * Returns the selected schema.
	 * 
	 * @return See above.
	 */
	public Target getTarget() { return target; }
	
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
	 * Returns the folder where to export the file
	 * 
	 * @return See above.
	 */
	public File getFolder() { return folder; }
	
	/**
	 * Returns one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
}
