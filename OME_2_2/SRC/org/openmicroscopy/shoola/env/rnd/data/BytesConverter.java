/*
 * org.openmicroscopy.shoola.env.rnd.data.BytesConverter
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

package org.openmicroscopy.shoola.env.rnd.data;

import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a strategy to convert a pixel value stored in a sequence of bytes
 * into a numeric value.
 * <p>OME supports different types to store a pixel value (those types are 
 * defined in {@link DataSink}).
 * When a pixel value is stored as a sequence of bytes, the format of those 
 * bytes depends on the pixel type and on the endianness chosen to encode the
 * bytes.  That leads to different algorithms for converting sequence of bytes 
 * back into a numeric value, depending on the pixel type and on the 
 * endianness-order of the bytes.</p>
 * <p>Each subclass implements the {@link #pack(ReadOnlyByteArray,int,int) pack}
 * method to carry out a specific conversion algorithm, taking into account
 * pixel type and endianness.  The value returned by this method is an object
 * that wraps the actual numeric value.  For {@link DataSink#INT8},
 * {@link DataSink#INT16}, {@link DataSink#INT32}, {@link DataSink#UINT8} and
 * {@link DataSink#UINT16} types, the returned value is an instance of
 * <code>Integer</code>, as <code>Long</code> is used for
 * {@link DataSink#UINT32}.</p>
 * <p>TODO: when we support all other pixel types, explain the mapping here.</p>
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
abstract class BytesConverter
{
	
	/** 
	 * Factory method to return an appropriate converter, depending on pixel 
	 * type and endianness.
	 *
	 * @param pixelType   	One of the constants defined by {@link DataSink}.
	 * @param bigEndian   	Pass <code>true</code> if the bytes are in
	 * 						big-endian order, <code>false</code> otherwise.
	 * @return  A suitable converter.
	 */
	static BytesConverter getConverter(int pixelType, boolean bigEndian) 
	{
		BytesConverter    bc = null;
		switch (pixelType) {
			case DataSink.UINT8:
			case DataSink.UINT16:
			case DataSink.UINT32:
				bc = createUintConverter(bigEndian);
				break;
			case DataSink.INT8:
			case DataSink.INT16:
			case DataSink.INT32:
				bc = createIntConverter(bigEndian);
				break;
			// not yet implemented
			//case DataSink.BIT:
			//    break;
			//case DataSink.FLOAT:
			//    break;
			//case DataSink.DOUBLE:
			//    break;
		}
		return bc;
	}


	/** 
	 * Creates a converter suitable for unsigned integers.
 	 *
 	 * @param bigEndian	Pass <code>true</code> if the bytes are in big-endian 
 	 * 					order, <code>false</code> otherwise.   
 	 * @return the suitable converter.
 	 */
	private static BytesConverter createUintConverter(boolean bigEndian)
	{
		if (bigEndian) return new UintBEConverter();
		return new UintLEConverter();   
	}
 
	/** 
	 * Creates the suitable converter.
 	 *
 	 * @param bigEndian	Pass <code>true</code> if the bytes are in big-endian 
 	 * 					order, <code>false</code> otherwise.   
 	 * @return the suitable converter.
 	 */
	private static BytesConverter createIntConverter(boolean bigEndian)
	{
		if (bigEndian) return new IntBEConverter();
		return new IntLEConverter();  
	}    

	/**
	 * Converts a sequence of bytes, representing a pixel value, into a numeric 
	 * value of appropriate type, taking endianness into account. 
	 * The value returned by this method is an object that wraps the actual 
	 * numeric value. For {@link DataSink#INT8}, {@link DataSink#INT16}, 
	 * {@link DataSink#INT32}, {@link DataSink#UINT8}, and 
	 * {@link DataSink#UINT16} types, the returned value is an 
	 * instance of <code>Integer</code>, as <code>Long</code> is used for 
	 * {@link DataSink#UINT32}.
 	 * <p>TODO: when we support all other pixel types, explain the mapping 
 	 * here.</p>
 	 *
 	 * @param data    The byte array containing the bytes to convert.
 	 * @param offset  The position of the first byte making up the pixel value.
 	 * @param length  The number of bytes that make up the pixel value.
 	 * @return An object to wrap the actual numeric value.
 	 */
	public abstract Object pack(ReadOnlyByteArray data, int offset, int length);

}
