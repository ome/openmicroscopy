/*
 * org.openmicroscopy.shoola.env.rnd.defs.RenderingDef
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

import java.util.List;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Aggregates all information needed to render an image.
 * Define constants that dictate how quantized data is mapped into a 
 * color space. 
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
public class RenderingDef
{

	/** GrayScale model. */
	public static final int 	GS = 0;
	
	/** RGB model. */
	public static final int 	RGB = 1;
	
	/** HSB model. */
	public static final int 	HSB = 2;
	
	/** The XY-plane to display when the image is open. */
	public final int 			defaultZ;
	
	/** The timepoint to display when the image is open. */
	public final int 			defaultT;
	
	/** One the constants defined above. */
	public final int 			model;
	
	public List					channelBindings;
	
	public RenderingDef(int defaultZ, int defaultT, int model)
	{
		this.defaultZ = defaultZ;
		this.defaultT = defaultT;
		this.model = model;
	}
	
}
