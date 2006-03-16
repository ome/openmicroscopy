/*
 * omeis.providers.re.GreyScaleStrategy
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.Helper;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/** 
 * Transforms a plane within a given pixels set into a grayscale image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.7 $ $Date: 2005/06/22 17:09:48 $)
 * </small>
 * @since OME2.2
 */
class GreyScaleStrategy 
	extends RenderingStrategy
{
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(Renderer.class);
    
	/** 
     * Number of pixels on the <i>X1</i>-axis.
     * This is the <i>X</i>-axis in the case of an <i>XY</i> or <i>XZ</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i> plane.
     */
	private int 		sizeX1;
	
	/** 
     * Number of pixels on the X2-axis.
     * This is the <i>Y</i>-axis in the case of an <i>XY</i> or <i>ZY</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i> plane. 
     */
	private int 		sizeX2;
	
    /** The rendering context. */
	private Renderer	renderer;
	
    
    /** 
     * Initialize the <code>sizeX1</code> and <code>sizeX2</code> fields
     * according to the specified {@link PlaneDef#getSlice() slice}.
     * 
     * @param pd Reference to the plane definition defined for the strategy.
     * @param pixels Dimensions of the pixels set.
     */
    private void initAxesSize(PlaneDef pd, Pixels pixels)
    {
        try {
            switch (pd.getSlice()) {
                case PlaneDef.XY:
                    sizeX1 = pixels.getSizeX().intValue();
                    sizeX2 = pixels.getSizeY().intValue();
                    break;
                case PlaneDef.XZ:
                    sizeX1 = pixels.getSizeX().intValue();
                    sizeX2 = pixels.getSizeZ().intValue();
                    break;
                case PlaneDef.ZY:
                    sizeX1 = pixels.getSizeZ().intValue();
                    sizeX2 = pixels.getSizeY().intValue();
            }   
        } catch(NumberFormatException nfe) {   
            throw new RuntimeException("Invalid slice ID: "+pd.getSlice()+".", 
                                        nfe);
        } 
    }
    
    /**
     * Renders the specified wavelength (channel).
     * 
     * @param dataBuf Buffer to hold the output image's data.
     * @param plane Defines the plane to render.
     * @param qs Knows how to quantize a pixel intensity value.
     * @param rgba The color components used when mapping a quantized value
     *             onto the color space.
     * @throws QuantizationException If an error occurs while quantizing a
     *                               pixels intensity value.
     */
    private void renderWave(RGBBuffer dataBuf, Plane2D plane, Color color,
                            QuantumStrategy qs)
        throws QuantizationException
    {
        CodomainChain cc = renderer.getCodomainChain();
        int x1, x2, discreteValue, pixelIndex;
        byte value;
        float alpha =  color.getAlpha().floatValue()/255;
        byte[] red = dataBuf.getRedBand(), green = dataBuf.getGreenBand(),
               blue = dataBuf.getBlueBand();
        for (x2 = 0; x2 < sizeX2; ++x2) {
            for (x1 = 0; x1 < sizeX1; ++x1) {
                pixelIndex = sizeX1*x2+x1;
                discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                discreteValue = cc.transform(discreteValue);
                value = (byte) (discreteValue*alpha);
                red[pixelIndex] = value;
                green[pixelIndex] = value;
                blue[pixelIndex] = value;
            } 
        }
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
	RGBBuffer render(Renderer ctx, PlaneDef planeDef)
		throws IOException, QuantizationException
	{
		//Set the context and retrieve objects we're gonna use.
		renderer = ctx;
		QuantumManager qManager = renderer.getQuantumManager();
		PixelBuffer pixels = renderer.getPixels();
        Pixels metadata = renderer.getMetadata();
		ChannelBinding[] cBindings = renderer.getChannelBindings();
        RenderingStats performanceStats = renderer.getStats();
		
		//Initialize sizeX1 and sizeX2 according to the plane definition and
		//create the RGB buffer.
		initAxesSize(planeDef, metadata);
        performanceStats.startMalloc();
        log.info("Creating RGBBuffer of size " + sizeX1 + "x" + sizeX2);
        RGBBuffer renderedDataBuf = new RGBBuffer(sizeX1, sizeX2);
        performanceStats.endMalloc();
        
		//Process the first active wavelength. 
		for (int i = 0; i < cBindings.length; i++) {
			if (cBindings[i].getActive().booleanValue()) {
                //Get the raw data.
			    performanceStats.startIO(i);
                Plane2D wData =
                    Helper.createPlane(planeDef, i, metadata, pixels);
                performanceStats.endIO(i);
                
				try {  //Transform it into an RGB image.
                    performanceStats.startRendering();
                    renderWave(renderedDataBuf, wData, cBindings[i].getColor(),
                               qManager.getStrategyFor(i));
                    performanceStats.endRendering();
				} catch (QuantizationException e) {
					e.setWavelength(i);
					throw e;
				}
				break;
			}
		}

		//Done.
        return renderedDataBuf;
	}
    
    /**
     * Implemented as specified by the superclass.
     * @see RenderingStrategy#getImageSize(PlaneDef, PixelsDimensions)
     */
    int getImageSize(PlaneDef pd, Pixels pixels)
    {
        initAxesSize(pd, pixels);
        return sizeX1*sizeX2*3;
    }
	
    /**
     * Implemented as specified by the superclass.
     * @see RenderingStrategy#getPlaneDimsAsString(PlaneDef, PixelsDimensions)
     */
    String getPlaneDimsAsString(PlaneDef pd, Pixels pixels)
    {
        initAxesSize(pd, pixels);
        return sizeX1+"x"+sizeX2;
    }
    
}

