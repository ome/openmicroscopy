/*
 * org.openmicroscopy.shoola.env.rnd.defs.QuantumDef
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

package org.openmicroscopy.shoola.env.rnd.defs;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

/** 
 * Defines the mapping context used during the quantization process.
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
public class QuantumDef
{
	
	/** 
	 * The storage type of image data.
	 * One of the constants defined by {@link DataSink}.
	 */
	public final int       pixelType;
	
	/** The lower bound of the codomain interval of the quantum map. */
	public final int       cdStart;
	
	/** The upper bound of the codomain interval of the quantum map. */
	public final int       cdEnd;
	
	/**
     * The depth, in bits, of the rendered image. 
     * One of the constants defined by {@link QuantumFactory}.
     */ 
	public final int       bitResolution;
	
    /**
     * Apply or not the algorithm to reduce the noise.
     * If <code>true</code>, the values close to the min or max are map to 
     * a constant.
     */
    //public final boolean    noiseReduction; 
    
    /**
     * Creates a new instance.
     * 
     * @param pixelType The storage type of image data.  Must be one of the 
     *                  constants defined by {@link DataSink}.
     * @param cdStart The lower bound of the codomain interval of the 
     *                  quantum map.
     * @param cdEnd The upper bound of the codomain interval of the quantum map.
     * @param bitResolution The depth, in bits, of the rendered image.  Must be
     *                      one of the constants defined by 
     *                      {@link QuantumFactory}.
     */
	public QuantumDef(int pixelType, int cdStart, int cdEnd, int bitResolution)
	{ 
        this.pixelType = pixelType;
        this.cdStart = cdStart;
        this.cdEnd = cdEnd;
        this.bitResolution = bitResolution;
	}

	/**
     * Returns an exact copy of this object.
     * 
	 * @return See above.
	 */
	QuantumDef copy()
	{	
        return new QuantumDef(pixelType, cdStart, cdEnd, bitResolution);
	}
	
}
