 /*
 * org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory
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

package org.openmicroscopy.shoola.env.rnd.quantum;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RenderingEngine;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;

/** 
 * The bit-depth constants cannot be modified b/c they have a meaning.
 * The class also defines the constants linked to {@link QuantumMap maps} 
 * implemented.
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
	
	/** Bit-depth 1Bit = 2^1-1. */
	public static final int   DEPTH_1BIT = 1;
	
	/** Bit-depth 1Bit = 2^2-1. */
	public static final int   DEPTH_2BIT = 3;
	
	/** Bit-depth 1Bit = 2^3-1. */
	public static final int   DEPTH_3BIT = 7;
	
	/** Bit-depth 1Bit = 2^4-1. */
	public static final int   DEPTH_4BIT = 15;
	
	/** Bit-depth 1Bit = 2^5-1. */
	public static final int   DEPTH_5BIT = 31;
	
	/** Bit-depth 1Bit = 2^6-1. */
	public static final int   DEPTH_6BIT = 63;
	
	/** Bit-depth 1Bit = 2^7-1. */
	public static final int   DEPTH_7BIT = 127;
	
	/** Bit-depth 1Bit = 2^8-1. */
	public static final int   DEPTH_8BIT = 255;
    
	/** Linear mapping: equation of the form y = a*x+b. */
	public static final int   LINEAR = 0;
	
	/** Exponential mapping: equation of the form y = a*(exp(x^k))+b. */
	public static final int   EXPONENTIAL = 1;
	
	/** Linear mapping: equation of the form y = a*log(x)+b. */
	public static final int   LOGARITHMIC = 2;
	
	/** 
	 * Linear mapping: equation of the form y = a*x^k+b.
	 * Note that LINEAR is a specific case of polynomial (k = 1).
	 * We keep the LINEAR constant for some UI reason but we apply the same 
	 * algorithm.
	 */
	public static final int   POLYNOMIAL = 3;

	public static QuantumStrategy getStrategy(QuantumDef qd)
	{
		verifyDef(qd);
		QuantumStrategy strg = null;
		QuantumMap qMap = null;
		switch (qd.family) {
			case LINEAR:
			case POLYNOMIAL:
				qMap = new PolynomialMap();
				break;
			case LOGARITHMIC:
				qMap = new LogarithmicMap();
				break;
			case EXPONENTIAL:
				qMap = new ExponentialMap(); 
				break;
			default: 
				qMap = new PolynomialMap();
				RenderingEngine.getRegistry().getLogger().debug(
					QuantumFactory.class, "Unsupported transformation");	
		}
		strg = getQuantization(qd);
		strg.setMap(qMap);
		if (strg == null)
			throw new IllegalArgumentException("Unsupported strategy");
		return strg;
	}
    
    /** Retrieve a {@link QuantumStrategy}. */
	private static QuantumStrategy getQuantization(QuantumDef qd)
	{
		QuantumStrategy qs = null;
		switch (qd.pixelType) {
			case DataSink.INT8:
			case DataSink.UINT8:
			case DataSink.INT16:
			case DataSink.UINT16:
			case DataSink.INT32:  
			case DataSink.UINT32:   
			case DataSink.FLOAT:  
			case DataSink.DOUBLE: 
				qs = new Quantization_8_16_bit(qd);
				break;
			case DataSink.BIT:    
			break;    
		}
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
		if (!b) throw new IllegalArgumentException("Unsupported output " +
				"interval type");
	}
    
	private static void verifyPixelType(int pixelType)
	{
		boolean b = false;
		switch (pixelType) {
			case DataSink.BIT:    b = true; break;
			case DataSink.INT8:   b = true; break;
			case DataSink.INT16:  b = true; break;
			case DataSink.INT32:  b = true; break;
			case DataSink.UINT8:  b = true; break;
			case DataSink.UINT16: b = true; break;
			case DataSink.UINT32: b = true; break;
			case DataSink.FLOAT:  b = true; break;
			case DataSink.DOUBLE: b = true;
		}
		if (!b) throw new IllegalArgumentException("Unsupported pixel type");
	}
  
}

