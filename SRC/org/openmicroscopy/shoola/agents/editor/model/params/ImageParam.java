 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.ImageParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is the data Parameter for a link to an image.
 * This is a link to a locally saved image.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageParam 
	extends AbstractParam {
	
	/**
	 * The value of the parameter type attribute that defines this Parameter. 
	 */
	public static final String 			IMAGE_PARAM = "imageParam";
	
	/**
	 * Used by the Image Parameter to store a file path to an image. 
	 * This is the absolute local file path.
	 */
	public static final String 			ABSOLUTE_IMAGE_PATH = "imagePath";
	
	/**
	 * Used by the Image Parameter to store a file path to an image. 
	 * This is a relative file path, FROM the editor file in which this 
	 * parameter appears TO the image 
	 */
	public static final String 			RELATIVE_IMAGE_PATH = "relativeImagePath";
	
	/**
	 * This attribute stores an integer that is the image zoom (percentage) for 
	 * the Image Parameter. 
	 * eg imageZoom = "50" would display the image at 50% full size. 
	 */
	public static final String 			IMAGE_ZOOM = "imageZoom";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public ImageParam(String fieldType) 
	{
		super(fieldType);
	}

	@Override
	/**
	 * @see AbstractParam#getValueAttributes()
	 */
	public String[] getParamAttributes() 
	{
		return new String[] {ABSOLUTE_IMAGE_PATH, RELATIVE_IMAGE_PATH,
				IMAGE_ZOOM};
	}

	@Override
	/**
	 * @see AbstractParam#isFieldFilled()
	 */
	public boolean isParamFilled() 
	{
		String[] attributes = getParamAttributes();
		for (int i=0; i<attributes.length; i++) {
			// if any attribute is not null, then this field is filled. 
			if (getAttribute(attributes[i]) != null)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the absolute image path, or (if null) the relative image path
	 * 
	 * @see Object#toString()
	 */
	public String toString() 
	{
		String text = "";
		
		String path = getAttribute (ABSOLUTE_IMAGE_PATH);
		if (path == null) path = getAttribute (RELATIVE_IMAGE_PATH);
		
		if (path != null) {
			text = text + path;
		} else {
			text = text + "no image set.";
		}
		
		return text;
	}

}
