/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.SaveObject
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.imviewer.util.saver;

import org.openmicroscopy.shoola.util.filter.file.PNGFilter;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * Hosts the parameters need to save the image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveObject
{

	/** Save the main image. */
	public static final int	IMAGE = ImgSaver.IMAGE;

	/** Save the grid image. */
	public static final int	GRID_IMAGE = ImgSaver.GRID_IMAGE;

	/** 
	 * Save the images and an image of each channel composing the rendered 
	 * image. 
	 */
	public static final int	IMAGE_AND_COMPONENTS = 
		ImgSaver.IMAGE_AND_COMPONENTS;

	/** 
	 * Save the images and an image of each channel composing the rendered 
	 * image.  Each channel rendered in grey scale mode.
	 */
	public static final int	IMAGE_AND_COMPONENTS_GREY = 
		ImgSaver.IMAGE_AND_COMPONENTS_GREY;

	/** The name of the file. */
	private String fileName;
	
	/** The format to use. */
	private String format;
	
	/** The type of file to save. */
	private int type;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fileName  The name of the file.
	 * @param format	The format. 
	 * @param type		The type of images to save.
	 */
	SaveObject(String fileName, String format, int type)
	{
		this.fileName = fileName;
		this.format = format;
		this.type = type;
		if (format == null || format.trim().length() == 0)
			format = PNGFilter.PNG;
	}
	
	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	public String getFileName() { return fileName; }
	
	/**
	 * Returns the format used.
	 * 
	 * @return See above.
	 */
	public String getFormat() { return format; }
	
	/**
	 * Returns one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	
}
