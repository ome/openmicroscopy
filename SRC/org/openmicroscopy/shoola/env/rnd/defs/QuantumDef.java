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

/** 
 * Contain the parameters that apply to all wavelengths in the quantization
 * process.
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
     * Identifies a family of maps. 
     * One of the constants defined by 
     * {@link org.openmicroscopy.shoola.env.rnd.domain.QuantumFactory 
     * QuantumFactory}. */
	public final int       family;
	
	/** 
	 * The storage type of image data.
	 * One of the constants defined by 
	 * {@link org.openmicroscopy.shoola.env.rnd.DataSink DataSink}.
	 */
	public final int       pixelType;
	
	/** Selects a curve in the family. */
	public final double    curveCoefficient;
	
	/** The lower bound of the codomain interval of the quantum map. */
	public final int       cdStart;
	
	/** The upper bound of the codomain interval of the quantum map. */
	public final int       cdEnd;
	
	/** 
	* The rendered image depth. 
	* One of the constants defined by 
	* {@link org.openmicroscopy.shoola.env.rnd.domain.QuantumFactory 
	* QuantumFactory}. */
	public final int       bitResolution;
	
	public QuantumDef(int family, int pixelType, double curveCoefficient,
				int cdStart, int cdEnd, int bitResolution)
	{ 
		this.family = family;
		this.pixelType = pixelType;
		this.curveCoefficient = curveCoefficient;
		this.cdStart = cdStart;
		this.cdEnd = cdEnd;
		this.bitResolution = bitResolution;
	}

}




