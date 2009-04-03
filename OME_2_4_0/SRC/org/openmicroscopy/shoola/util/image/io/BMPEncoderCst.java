/*
 * org.openmicroscopy.shoola.util.image.io.BMPEncoderCst
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
class BMPEncoderCst
{

	static final int 	FILEHEADER_SIZE = 14;
	static final int	INFOHEADER_SIZE = 40;
	
	//file header
	static final byte 	TYPE [] =  {(byte) 'B', (byte) 'M'};
	static final int	RESERVED_1 = 0;
	static final int	RESERVED_2 = 0;
	static final int 	OFFBITS = FILEHEADER_SIZE+INFOHEADER_SIZE;
	
	//info header
	static final int 	PLANES = 1;
	static final int	COMPRESSION = 0;
	static final int	XPELSPERMETER = 0x0;
	static final int	YPELSPERMETER = 0x0;
	static final int	CLR_IMPORTANT = 0;
	static final int	BITCOUNT = 24;
	static final int	SIZE_IMAGE = 0x030000;
	
	/**
	 * intToWord converts an int to a word, where the return
	 * value is stored in a 2-byte array.
	 */
	static byte [] intToWord (int value)
	{
		byte retValue [] = new byte [2];
	   	retValue [0] = (byte) (value & 0x00FF);
	   	retValue [1] = (byte) ((value >> 8) & 0x00FF);
	   	return (retValue);
	}

	/**
	 * intToDWord converts an int to a double word, where the return
	 * value is stored in a 4-byte array.
	 */
	static byte [] intToDWord (int value)
	{
		byte retValue [] = new byte [4];
		retValue [0] = (byte) (value & 0x00FF);
		retValue [1] = (byte) ((value >> 8) & 0x000000FF);
		retValue [2] = (byte) ((value >> 16) & 0x000000FF);
		retValue [3] = (byte) ((value >> 24) & 0x000000FF);
		return (retValue);
	}
	
}
