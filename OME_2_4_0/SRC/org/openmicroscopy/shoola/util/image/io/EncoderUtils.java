/*
 * org.openmicroscopy.shoola.util.image.io.EncoderUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.image.io;


//Java imports
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EncoderUtils
{

	/** Color band constants */
	static final int	RED_BAND = 0;
	static final int	GREEN_BAND = 1;
	static final int	BLUE_BAND = 2;
	
	/** Check if the output is valid. */
	static void checkOutput(DataOutputStream output)
			throws IllegalArgumentException
	{
		if (output == null) 
			new IllegalArgumentException("Output not valid");
	}
	
	/** Check if we support the model, we only support Gray and RGB. */
	static int checkColorModel(BufferedImage img)
			throws IllegalArgumentException
	{
		int colorType = img.getColorModel().getColorSpace().getType();
		if (colorType != ColorSpace.TYPE_RGB && 
			colorType != ColorSpace.TYPE_GRAY)
			throw new IllegalArgumentException("Color Type not supported");
		return colorType;
	}

}
