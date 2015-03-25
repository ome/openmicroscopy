/*
 * org.openmicroscopy.shoola.env.rnd.data.FloatConverter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package training.util;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Packs a sequence of bytes representing a big-endian float into 
 * a <code>double</code> value of appropriate integer type.
 * <p>This class handles the conversion of float of <code>4</code>-byte length 
 * (bytes are assumed to be <code>8</code>-bit long). 
 * </p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class FloatConverter
	extends BytesConverter
{

	/**
	 * Implemented as specified by {@link BytesConverter}
	 * @see BytesConverter#pack(ReadOnlyByteArray, int, int)
	 */
	public double pack(ReadOnlyByteArray data, int offset, int length)
	{
		int r = 0, tmp;
		for (int k = 0; k < length; ++k) {
			
			//Get k-byte starting from MSB, that is LSB[length-k-1].
			tmp = data.get(offset+k)&0xFF;
			//Add LSB[j]*(2^8)^j to r, where j=length-k-1.  
			r |= tmp<<(length-k-1)*8;
			
			/* 
			 * This probably deserves a quick explanation.
			 * We consider every byte value as a digit in base 2^8=B. 
			 * This means that the numeric value is given by 
			 * LSB[0]*B^0 + LSB[1]*B^1 + ... + LSB[n]*B^n.
			 * So, if we know where the LSB in the input bytes is (that is, the
			 * endianness), we can calculate the numeric value regardless of the
			 * endianness of the platform we're running on.
			 * We use a left shift to calculate LSB[k]*B^k because this operator
			 * shifts from LSB to MSB, regardless of endianness.
			 */ 

		}
		
		return Float.intBitsToFloat(r);
	}

}
