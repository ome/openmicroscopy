/*
 * org.openmicroscopy.shoola.env.data.pix.UintBEConverter
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

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Packs a sequence of bytes representing an unsigned big-endian integer into 
 * an integer value of appropriate integer type. 
 * <p>This class handles the conversion of unsigned big-endian integers of 
 * <code>1, 2</code> and <code>4</code>-byte length 
 * (bytes are assumed to be <code>8</code>-bit long). 
 * Integers of <code>1</code> and <code>2</code>-byte length are packed into
 * an <code>Integer</code>, as <code>Long</code> is used for <code>4</code>-byte
 * integers.</p>
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
public class UintBEConverter
	extends BytesConverter
{ 
	/**
	 * We consider every byte value as a digit in base 2^8 =B. 
	 * This means that the numeric value is given by 
	 * LSB[0]*B^0 + LSB[1]*B^1 + ... + LSB[n]*B^n.
	 * So, if we know where the LSB in the input bytes is 
	 * (that is, the endianness), we can calculate the numeric value regardless 
	 * of the endianness of the platform we're running on.
	 * We use a left shift to calculate LSB[k]*B^k because this operator shifts 
	 * from LSB to MSB, regardless of endianness.
	 */
	public Object pack(byte[] data, int offset, int length)
	{
		long r = 0, tmp;
		for (int k = 0; k < length; ++k) {
			//get k-byte starting from MSB, that is LSB[length-k-1]
			tmp = data[offset+k]&0xFF;
			//add LSB[j]*(2^8)^j to r, where j=length-k-1  
			r |= tmp<<(length-k-1)*8; 
		}
		if (length < 4) return new Integer((int) r);	
		return new Long(r);
	}
    
}

