/*
 * org.openmicroscopy.shoola.env.rnd.RenderingStrategy
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;

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
abstract class RenderingStrategy
{
	
	static RenderingStrategy makeNew(int model)
	{
		RenderingStrategy strategy = null;
		try {
			switch (model) {
				case RenderingDef.GS:
					strategy = new GreyScaleStrategy();
					break;
				case RenderingDef.HSB:
				case RenderingDef.RGB:
					strategy = new RGBStrategy();
					break;
				default:
					//set the GreyScaleStrategy as the default one
					strategy = new GreyScaleStrategy();
					RenderingEngine.getRegistry().getLogger().debug(
								RenderingStrategy.class, 
										"Wrong Rendering model identifier");
			}	
		} catch(NumberFormatException nfe) {   
			throw new Error("Invalid Action ID "+model, nfe);
		} 
		return strategy;
	}
	
	abstract BufferedImage render(Renderer ctx) 
		throws DataSourceException, QuantizationException;
	
}
