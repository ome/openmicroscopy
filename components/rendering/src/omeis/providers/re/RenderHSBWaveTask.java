/*
 * omeis.providers.re.RenderHSBWaveTask
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
import java.util.concurrent.Callable; //j.m

//Third-party libraries

//Application-internal dependencies
//j.m import ome.util.concur.tasks.Invocation; 
import ome.model.display.Color;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/** 
 * A task object to render a wavelength asynchronously.
 * This task is used by the {@link HSBStrategy} to do concurrent rendering
 * if more than one wavelength has to be processed.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.4 $ $Date: 2005/06/17 12:57:33 $)
 * </small>
 * @since OME2.2
 */
class RenderHSBWaveTask
    implements Callable//j.m
{

    /** Buffer to hold the output image's data. */
    private RGBBuffer       dataBuffer;
    
    /** The wavelength data. */
    private Plane2D         plane;
    
    /** How to quantize a pixel intensity value. */
    private QuantumStrategy qs;
    
    /** The spatial transformations to apply to the quantized data. */
    private CodomainChain   cc;
    
    /** 
     * The color components used when mapping a quantized value
     * onto the color space.
     */
    private Color           color;
    
    /** Number of pixels on the X1-axis. */
    private int             sizeX1;
    
    /** Number of pixels on the X2-axis. */
    private int             sizeX2;
    
    
    /**
     * Creates a new instance to render a wavelength.
     * 
     * @param dataBuffer    Buffer to hold the output image's data.
     * @param plane         The wavelength data.
     * @param qs            The quantum strategy associated to the wavelength.
     * @param cc            The spatial transformations to apply to the
     *                      quantized data.
     * @param color         The color component to use when mapping a quantized
     *                      value onto the color space.
     * @param sizeX1        The number of pixels along the <i>X1</i>-axis.
     * @param sizeX2        The number of pixels along the <i>X2</i>-axis.
     */
    RenderHSBWaveTask(RGBBuffer dataBuffer, Plane2D plane, QuantumStrategy qs,
                      CodomainChain cc, Color color, int sizeX1, int sizeX2)
    {
        this.dataBuffer = dataBuffer;
        this.plane = plane;
        this.qs = qs;
        this.cc = cc;
        this.color = color;
        this.sizeX1 = sizeX1;
        this.sizeX2 = sizeX2;
    }

    /** 
     * Renders wavelength.
     * 
     * @return The data buffer that holds the rendered data.
     * @throws QuantizationException If an error occurs while quantizing a
     *                               pixels intensity value.
     */
    public Object call()
            throws QuantizationException
    {
        int x1, x2, discreteValue, pix;
        float alpha = color.getAlpha().floatValue()/65025;//255*255
        byte[] red = dataBuffer.getRedBand(), green = dataBuffer.getGreenBand(),
               blue = dataBuffer.getBlueBand();
        float v;
        for (x2 = 0; x2 < sizeX2; ++x2) {
            for (x1 = 0; x1 < sizeX1; ++x1) {
                pix = sizeX1*x2+x1;
                discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                discreteValue = cc.transform(discreteValue);
                v = discreteValue*alpha;
                red[pix] = (byte) (red[pix] + color.getRed().intValue()*v);
                green[pix] = (byte) (green[pix]+color.getGreen().intValue()*v);
                blue[pix] = (byte) (blue[pix]+color.getBlue().intValue()*v);
            } 
        } 
        return dataBuffer;
    }

}
