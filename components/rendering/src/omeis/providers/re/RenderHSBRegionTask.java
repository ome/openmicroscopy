/*
 * omeis.providers.re.RenderHSBWaveTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;


//Java imports
import java.util.List;
import java.util.concurrent.Callable;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.Color;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/** 
 * A task object to render an image region asynchronously.
 * This task is used by the {@link HSBStrategy} to do concurrent rendering
 * if more than one region has to be processed.
 *
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:callan@blackcat.ca">callan@blackat.ca</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.4 $ $Date: 2005/06/17 12:57:33 $)
 * </small>
 * @since OMERO3.0
 */
class RenderHSBRegionTask
    implements RenderingTask
{

    /** Buffer to hold the output image's data. */
    private RGBBuffer       dataBuffer;
    
    /** The wavelength data. */
    private List<Plane2D>   wData;
    
    /** How to quantize a pixel intensity value. */
    private List<QuantumStrategy> strategies;
    
    /** The spatial transformations to apply to the quantized data. */
    private CodomainChain   cc;
    
    /** 
     * The color components used when mapping a quantized value
     * onto the color space.
     */
    private List<Color>     colors;
    
    /** The <i>X1/<i>-axis start */
    private int             x1Start;
    
    /** The <i>X1</i>-axis end */
    private int             x1End;
    
    /** The <i>X2</i>-axis start */
    private int             x2Start;
    
    /** The <i>X2</i>-axis end */
    private int             x2End;
    
    /**
     * Creates a new instance to render a wavelength.
     * 
     * @param dataBuffer    Buffer to hold the output image's data.
     * @param wData         The wavelength data.
     * @param strategies    The quantum strategy for each wavelength.
     * @param cc            The spatial transformations to apply to the
     *                      quantized data.
     * @param colors        The color components to use when mapping quantized
     *                      values onto the color space.
     * @param x1Start       The <i>X1</i>-axis start
     * @param x1End         The <i>X1</i>-axis start
     * @param x2Start       The <i>X2</i>-axis start
     * @param x2End         The <i>X2</i>-axis start
     */
    RenderHSBRegionTask(RGBBuffer dataBuffer, List<Plane2D> wData,
    		            List<QuantumStrategy> strategies, CodomainChain cc,
    		            List<Color> colors, int x1Start, int x1End, int x2Start,
    		            int x2End)
    {
        this.dataBuffer = dataBuffer;
        this.wData = wData;
        this.strategies = strategies;
        this.cc = cc;
        this.colors = colors;
        this.x1Start = x1Start;
        this.x1End = x1End;
        this.x2Start = x2Start;
        this.x2End = x2End;
    }

    /** 
     * Renders the region.
     * 
     * @throws QuantizationException If an error occurs while quantizing a
     *                               pixels intensity value.
     */
    public Object call()
            throws QuantizationException
    {
    	if (dataBuffer instanceof RGBIntBuffer)
    		renderPackedInt();
    	else
    		renderBanded();
		return null;
    }

    
    /**
     * Renders into a banded byte buffer.
     * 
     * @throws QuantizationException if there is an error during pixel value
     * quantization.
     */
    private void renderBanded() throws QuantizationException
    {
        int discreteValue, pix;
        int rValue, gValue, bValue;
        float v;
        
        int width = x1End - x1Start;
        int i = 0;
        byte[] r = dataBuffer.getRedBand();
        byte[] g = dataBuffer.getGreenBand();
        byte[] b = dataBuffer.getBlueBand();
        for (Plane2D plane : wData)
        {
        	Color color = colors.get(i);
        	QuantumStrategy qs = strategies.get(i);
        	int rColor = color.getRed();
        	int gColor = color.getGreen();
        	int bColor = color.getBlue();

        	float alpha = color.getAlpha().floatValue()/65025;//255*255
        	for (int x2 = x2Start; x2 < x2End; ++x2)
        	{
        		for (int x1 = x1Start; x1 < x1End; ++x1)
        		{
        			pix = width*x2+x1;
        			discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
        			discreteValue = cc.transform(discreteValue);
        			
        			// Pre-multiply the alpha component and add the existing
        			// colour value to the new colour value.
        			v = discreteValue*alpha;
        			rValue = ((int) (rColor * v)) + r[pix];
        			gValue = ((int) (gColor * v)) + g[pix];
        			bValue = ((int) (bColor * v)) + b[pix];
        			
        			// Ensure that each colour component value is between 0 and
        			// 255 (byte). We must make *certain* that values to not
        			// wrap over 255 otherwise there will be corruption
        			// introduced into the rendered image.
        			if (rValue > 255)
        				rValue = 255;
        			if (gValue > 255)
        				gValue = 255;
        			if (bValue > 255)
        				bValue = 255;
        			
        			r[pix] = (byte) (rValue & 0xFF);
        			g[pix] = (byte) (gValue & 0xFF);
        			b[pix] = (byte) (bValue & 0xFF);
        		}
        	}
        	
        	i++;
        }
    }
    
    /**
     * Renders into a packed integer array.
     * 
     * @throws QuantizationException if there is an error during pixel value
     * quantization.
     */
    private void renderPackedInt() throws QuantizationException
    {
        int discreteValue, pix;
        float v;
        int rColor, gColor, bColor;
        int rValue, gValue, bValue;
        int newRValue, newGValue, newBValue;
        
        int width = x1End - x1Start;
        int i = 0;
        int[] buf = ((RGBIntBuffer) dataBuffer).getDataBuffer();
        for (Plane2D plane : wData)
        {
        	Color color = colors.get(i);
        	QuantumStrategy qs = strategies.get(i);
        	rColor = color.getRed();
        	gColor = color.getGreen();
        	bColor = color.getBlue();

        	float alpha = color.getAlpha().floatValue()/65025;//255*255
        	for (int x2 = x2Start; x2 < x2End; ++x2)
        	{
        		for (int x1 = x1Start; x1 < x1End; ++x1)
        		{
        			pix = width*x2+x1;
        			discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
        			discreteValue = cc.transform(discreteValue);
        			
        			/*
        			// This is an optimization that can only be used when the
        			// channel colours are primary (red, green or blue).
        			buf[pix] |= 0xFF000000;
        			if (rColor == 255)
        				buf[pix] |= discreteValue << 16;
        			if (gColor == 255)
        				buf[pix] |= discreteValue << 8;
        			if (bColor == 255)
        				buf[pix] |= discreteValue;
        			*/
        			
        			// Pre-multiply the alpha for each colour component.
        			v = discreteValue * alpha;
        			newRValue = (int) (rColor > 0? rColor * v: 0);
        			newGValue = (int) (gColor > 0? gColor * v: 0);
        			newBValue = (int) (bColor > 0? bColor * v: 0);
        			
        			// Add the existing colour component values to the new 
        			// colour component values.
        			rValue = ((buf[pix] & 0x00FF0000) >> 16) + newRValue;
        			gValue = ((buf[pix] & 0x0000FF00) >> 8) + newGValue;
        			bValue =  (buf[pix] & 0x000000FF) + newBValue;
        			
        			// Ensure that each colour component value is between 0 and
        			// 255 (byte). We must make *certain* that values to not
        			// wrap over 255 otherwise there will be corruption
        			// introduced into the rendered image.
        			if (rValue > 255)
        				rValue = 255;
        			if (gValue > 255)
        				gValue = 255;
        			if (bValue > 255)
        				bValue = 255;
        			
        			// Packed each colour component along with a 1.0 alpha into
        			// the buffer so that buffered images that use this buffer
        			// can be type 1 (3 bands, pre-multiplied alpha) or type 2
        			// (4 bands, alpha component included).
        			buf[pix] = 0xFF000000 | rValue << 16 | gValue << 8 | bValue;
        		}
        	}
        	
        	i++;
        }
    }
}
