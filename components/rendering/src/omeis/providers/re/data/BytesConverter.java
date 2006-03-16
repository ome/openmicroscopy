/*
 * omeis.providers.re.data.BytesConverter
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

package omeis.providers.re.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.model.enums.PixelsType;
import omeis.providers.re.Renderer;
import tmp.PixelTypeHelper;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a strategy to convert a pixel value stored in a sequence of bytes
 * into a numeric value.
 * <p><i>OME</i> supports different types to store a pixel value (those types 
 * are defined in {@link omeis.io.PixelTypeHelper}).
 * When a pixel value is stored as a sequence of bytes, the format of those 
 * bytes depends on the pixel type and on the endianness chosen to encode the
 * bytes (if the type is one of the integer types).  That leads to different
 * algorithms for converting sequence of bytes back into a numeric value, 
 * depending on the pixel type and on the endianness-order of the bytes.</p>
 * <p>Each subclass implements the {@link #pack(byte[], int, int) pack} method
 * to carry out a specific conversion algorithm, taking into account pixel type
 * and endianness.</p>
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/09 11:12:14 $)
 * </small>
 * @since OME2.2
 */
abstract class BytesConverter
{
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(Renderer.class);
    
	/** 
	 * Factory method to return an appropriate converter, depending on pixel 
	 * type and endianness.
	 *
	 * @param pixelType One of the constants defined by {@link PixelTypeHelper}.
	 * @param bigEndian   	Pass <code>true</code> if the bytes are in
	 * 						big-endian order, <code>false</code> otherwise.
	 * @return  A suitable converter.
	 */
	static BytesConverter makeNew(PixelsType pixelType, boolean bigEndian) 
	{
		BytesConverter bc = null;
		if (PixelTypeHelper.in(pixelType,
				new String[] { "uint8", "uint16", "uint32" }))
		{
			if (bigEndian)
            {
                log.info("Using 'UintBEConvertor'");
                bc = new UintBEConverter();
            }
            else 
            {
                log.info("Using 'UintLEConvertor'");
                bc = new UintLEConverter();
            }
		}
		else if (PixelTypeHelper.in(pixelType,
				new String[] { "int8", "int16", "int32" }))
		{
			if (bigEndian)
            {
                log.info("Using 'IntBEConvertor'");
                bc = new IntBEConverter();
            }
			else
            {
                log.info("Using 'UintLEConvertor'");
			    bc = new IntLEConverter();
            }
		}
		else if (pixelType.getValue().equals("float"))
        {
            log.info("Using 'FloatConverter'");
			bc = new FloatConverter();
        }
		else if (pixelType.getValue().equals("double"))
        {
            log.info("Using 'DoubleConverter'");
			bc = new DoubleConverter();
        }
		else
			throw new RuntimeException("Can't handle pixels of type '"
					+ pixelType + "'");
		
		return bc;
	}

    
	/**
	 * Converts a sequence of bytes, representing a pixel value, into a numeric 
	 * value of appropriate type, taking endianness into account (if the type
     * is an integer type).
 	 *
 	 * @param data The byte array containing the bytes to convert.
 	 * @param offset The position of the first byte making up the pixel value.
 	 * @param length The number of bytes that make up the pixel value.
 	 * @return The actual numeric value.
 	 */
	public abstract double pack(byte[] data, int offset, int length);

}
