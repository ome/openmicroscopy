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
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;

/** 
 * Defines how to encapsulate a specific rendering algorithm. 
 * <p>Subclasses realize a given algorithm by implementing the 
 * {@link #render(Renderer, PlaneDef) render} method.  The image is rendered
 * according to the current settings in the rendering context which is accessed
 * through a {@link Renderer} object representing the rendering environment.</p>
 * <p>The {@link #makeNew(int) makeNew} factory method allows to select a
 * concrete strategy depending on on how transformed data is to be mapped into
 * a color space.</p>
 *
 * @see Renderer
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
	
    /**
     * Factory method to retrieve a concrete strategy.
     * The strategy is selected according to the model that dictates how 
     * transformed raw data is to be mapped into a color space.  This model
     * is identified by the passed argument.
     * 
     * @param model Identifies the color space model.  One of the constants
     *              defined by {@link RenderingDef}.
     * @return  A strategy suitable for the specified model.
     */
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
	
    
    /**
     * Encapsulates a specific rendering algorithm. 
     * The image is rendered according to the current settings hold by the
     * <code>ctx</code> argument.
     * Typically, active wavelenghts are processed by first quantizing the 
     * wavelenght data in the plane selected by <code>pd</code> &#151; the 
     * quantum strategy is retrieved from the {@link QuantumManager} (accessed
     * through the <code>ctx</code> object) and the actual data from the 
     * {@link org.openmicroscopy.shoola.env.rnd.data.DataSink DataSink} (again, 
     * retrieved through <code>ctx</code>).
     * Then the codomain transformations are applied &#151; by calling the 
     * transform method of the 
     * {@link org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain chain}
     * hold by <code>ctx</code>.  Transformed wavelenght data is finally packed
     * into a {@link BufferedImage} taking into account the color bindings
     * defined by the rendering context.
     * 
     * @param ctx   Represents the rendering environment.
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.
     * @return  An image rendered according to the current settings hold by
     *          <code>ctx</code>.
     * @throws DataSourceException  If an error occurred while accessing the
     *                              pixels raw data.
     * @throws QuantizationException If an error occurred while quantizing the
     *                                  pixels raw data.
     */
	abstract BufferedImage render(Renderer ctx, PlaneDef pd) 
		throws DataSourceException, QuantizationException;
    
    /**
     * Returns the size, in bytes, of the {@link BufferedImage} that would be
     * rendered from the plane selected by <code>pd</code> in a pixels set 
     * having dimensions <code>dims</code>.
     * 
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.
     * @param dims  The dimensions of the pixels set containing the plane
     *              selected by <code>pd</code>. 
     * @return  See above.
     */
    abstract int getImageSize(PlaneDef pd, PixelsDimensions dims);
	
}
