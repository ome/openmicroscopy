/*
 * org.openmicroscopy.shoola.env.rnd.QuantumFactory
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

package org.openmicroscopy.shoola.env.rnd;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
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
public class QuantumFactory
{
	/**
	* The following constants cannot be modified b/c they have a special meaning
	* 1Bit = 2^1-1 2Bit = 2^2-1, ..., 8Bit = 2^8-1;
	*/
	public static final int   DEPTH_1BIT = 1;
	public static final int   DEPTH_2BIT = 3;
	public static final int   DEPTH_3BIT = 7;
	public static final int   DEPTH_4BIT = 15;
	public static final int   DEPTH_5BIT = 31;
	public static final int   DEPTH_6BIT = 63;
	public static final int   DEPTH_7BIT = 127;
	public static final int   DEPTH_8BIT = 255;
    

	/** ID of quantumMaps implemented. */
	public static final int   LINEAR = 0;
	public static final int   EXPONENTIAL = 1;
	public static final int   LOGARITHMIC = 2;
	public static final int   POLYNOMIAL = 3;

	static QuantumStrategy getStrategy(QuantumDef qd)
	{
		verifyDef(qd);
		QuantumStrategy     strg = null;
		QuantumMap qMap = null;
		switch (qd.family) {
			case LINEAR:
			case POLYNOMIAL:
				qMap = new PolynomialMap(qd.curveCoefficient);
				break;
			case LOGARITHMIC:
				qMap = new LogarithmicMap();
				break;
			case EXPONENTIAL:
				qMap = new ExponentialMap(qd.curveCoefficient); 
		}
		if (qMap == null)
			throw new IllegalArgumentException("Unsupportedtransformation");
		strg = getQuantization(qd, qMap);
		if (strg == null)
			throw new IllegalArgumentException("Unsupported strategy");
		return strg;
	}
    
	private static QuantumStrategy getQuantization(QuantumDef qd, 
				QuantumMap qMap)
	{
		QuantumStrategy     qs = null;
		/*
		switch (qd.pixelType) {
			case Pixels.INT8:
			case Pixels.UINT8:
			case Pixels.INT16:
			case Pixels.UINT16:
				qs = new Quantization_8_16_bit(qd, qMap);
				break;
			case Pixels.INT32:  //TODO when we support these types
			case Pixels.UINT32:
			case Pixels.BIT:    
			case Pixels.FLOAT:  
			case Pixels.DOUBLE: break;    
		}
		*/
		return qs;
	}
    
	private static void verifyDef(QuantumDef qd)
	{
		if (qd == null)    
			throw new NullPointerException("No quantum definition provided");
		verifyFamily(qd.family);
		verifyBitResolution(qd.bitResolution);
		verifyPixelType(qd.pixelType);
	}
    
	private static void verifyFamily(int family)
	{
		if (family != LINEAR && family != LOGARITHMIC && family != EXPONENTIAL
			&& family != POLYNOMIAL)  
			throw new IllegalArgumentException("Unsupported family type");
	}
    
	private static void verifyBitResolution(int bitResolution)
	{
		boolean  b = false;
		switch (bitResolution) {
			case DEPTH_1BIT: b = true; break;
			case DEPTH_2BIT: b = true; break;
			case DEPTH_3BIT: b = true; break;
			case DEPTH_4BIT: b = true; break;
			case DEPTH_5BIT: b = true; break;
			case DEPTH_6BIT: b = true; break;
			case DEPTH_7BIT: b = true; break;
			case DEPTH_8BIT: b = true; break;
		}
		if (!b) 
			throw new IllegalArgumentException("Unsupported output interval" +
				" type");
	}
    
	private static void verifyPixelType(int pixelType)
	{
		boolean     b = false;
		/*
		switch(pixelType) {
			case Pixels.BIT:    b = true; break;
			case Pixels.INT8:   b = true; break;
			case Pixels.INT16:  b = true; break;
			case Pixels.INT32:  b = true; break;
			case Pixels.UINT8:  b = true; break;
			case Pixels.UINT16: b = true; break;
			case Pixels.UINT32: b = true; break;
			case Pixels.FLOAT:  b = true; break;
			case Pixels.DOUBLE: b = true;
		}*/
        
		if (!b)
			throw new IllegalArgumentException("Unsupported pixel type");
	}
  
}
