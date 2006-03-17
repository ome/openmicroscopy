/*
 * omeis.providers.re.RenderingStrategy
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

package omeis.providers.re;


//Java imports
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import tmp.RenderingDefConstants;

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
 * (<b>Internal version:</b> $Revision: 1.5 $ $Date: 2005/06/18 14:36:02 $)
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
     * @param model Identifies the color space model. 
     * @return A strategy suitable for the specified model.
     */
	static RenderingStrategy makeNew(int model)
	{
		RenderingStrategy strategy = null;
		try {
			switch (model) {
				case RenderingDefConstants.GS:
					strategy = new GreyScaleStrategy();
					break;
				case RenderingDefConstants.HSB:
                    strategy = new HSBStrategy();
                    break;
				case RenderingDefConstants.RGB:
					strategy = new RGBStrategy();
					break;
				default:
					//Set the GreyScaleStrategy as the default one.
					strategy = new GreyScaleStrategy();
					//TODO: log debug?
			}	
		} catch(NumberFormatException nfe) {   
			throw new RuntimeException("Invalid model ID "+model+".", nfe);
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
     * {@link omeis.providers.re.data.PixelsData PixelsData} service (again, 
     * retrieved through <code>ctx</code>).
     * Then the codomain transformations are applied &#151; by calling the 
     * transform method of the 
     * {@link omeis.providers.re.codomain.CodomainChain chain}
     * hold by <code>ctx</code>.  Transformed wavelength data is finally packed
     * into a {@link RGBBuffer} taking into account the color bindings defined
     * by the rendering context.
     * 
     * @param ctx   Represents the rendering environment.
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.
     * @return  An image rendered according to the current settings hold by
     *          <code>ctx</code>.
     * @throws IOException  If an error occurred while accessing the pixels raw
     *                      data.
     * @throws QuantizationException If an error occurred while quantizing the
     *                               pixels raw data.
     */
	abstract RGBBuffer render(Renderer ctx, PlaneDef pd) 
		throws IOException, QuantizationException;
    
    /**
     * Returns the size, in bytes, of the {@link RGBBuffer} that would be
     * rendered from the plane selected by <code>pd</code> in a pixels set 
     * having dimensions <code>dims</code>.
     * 
     * @param pd     Selects a plane orthogonal to one of the <i>X</i>,
     *               <i>Y</i>, or <i>Z</i> axes.
     * @param pixels A pixels set. 
     * @return See above.
     */
    abstract int getImageSize(PlaneDef pd, Pixels pixels);
    
    /**
     * Returns a string with the dimensions of the specified plane.
     * The returned string has the format <code>AxB</code>, where <code>A</code>
     * is the number of pixels on the <i>X1</i>-axis and <code>B</code> the
     * the number of pixels on the the <i>X2</i>-axis.
     * The <i>X1</i>-axis is the <i>X</i>-axis in the case of an <i>XY</i> or 
     * <i>XZ</i> plane.  Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i>
     * plane.
     * The <i>X2</i>-axis is the <i>Y</i>-axis in the case of an <i>XY</i> or 
     * <i>ZY</i> plane.  Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i>
     * plane. 
     * 
     * @param pd     Selects a plane orthogonal to one of the <i>X</i>,
     *               <i>Y</i>, or <i>Z</i> axes.
     * @param pixels A pixels set.
     * @return See above.
     */
    abstract String getPlaneDimsAsString(PlaneDef pd, Pixels pixels);
	
}
