/*
 * org.openmicroscopy.shoola.util.image.io.TIFFEncoderCst
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

//Third-party libraries

//Application-internal dependencies

/** 
 * Constant used to save a buffered image as a TIFF.
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
class TIFFEncoderCst
{

	static final int	NEW_SUBFILE_TYPE = 254;
	static final int 	IMAGE_WIDTH = 256;
	static final int 	IMAGE_LENGTH = 257;
	static final int 	BITS_PER_SAMPLE = 258;
	static final int 	PHOTO_INTERP = 262;
	static final int 	STRIP_OFFSETS = 273;
	static final int 	SAMPLES_PER_PIXEL = 277;
	static final int 	ROWS_PER_STRIP = 278;
	static final int 	STRIP_BYTE_COUNT = 279;
	static final int 	X_RESOLUTION = 282;
	static final int 	Y_RESOLUTION = 283;
	static final int 	RESOLUTION_UNIT = 296;
	
	static final int 	IMAGE_START = 768;
	static final int 	HDR_SIZE = 8;
	static final int 	MAP_SIZE = 768; // in 16-bit words
	static final int 	BPS_DATA_SIZE = 6;
	static final int 	SCALE_DATA_SIZE = 16;
	static byte[]		header = {0x4D, 0x4D, 0, 42, 0, 0, 0, 8};


	//field type
	static final int 	SHORT = 3;

	//color band constants
	static final int	RED_BAND = 0;
	static final int	GREEN_BAND = 1;
	static final int	BLUE_BAND = 2;
														
}
