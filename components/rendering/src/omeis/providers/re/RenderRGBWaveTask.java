/*
 * omeis.providers.re.RenderRGBWaveTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;


//Java imports
import java.util.concurrent.Callable;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/** 
 * A task object to render a wavelength asynchronously.
 * This task is used by the {@link RGBStrategy} to do concurrent rendering
 * if more than one wavelength has to be processed.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.3 $ $Date: 2005/06/16 17:31:27 $)
 * </small>
 * @since OME2.2
 */
class RenderRGBWaveTask
    implements RenderingTask
{

    /** Buffer to hold the output image's data. */
    private byte[]          band;
    
    /** The wavelength data. */
    private Plane2D         plane;
    
    /** How to quantize a pixel intensity value. */
    private QuantumStrategy qs;
    
    /** The spatial transformations to apply to the quantized data. */
    private CodomainChain   cc;
    
    /** 
     * The alpha component of the color used when mapping a quantized value
     * onto the color space.
     */
    private int             alpha;
    
    /** The number of pixels along the <i>X1</i>-axis. */
    private int             sizeX1;
    
    /** The number of pixels along the <i>X2</i>-axis. */
    private int             sizeX2;
    
    
    /**
     * Creates a new instance to render a wavelength.
     * 
     * @param band      Buffer to hold the output image's data.
     * @param plane     The wavelength data.
     * @param qs        The quantum strategy associated to the wavelength.
     * @param cc        The spatial transformations to apply to the quantized
     *                  data.
     * @param alpha     The alpha component to apply to the final image.
     * @param sizeX1    The number of pixels along the <i>X1</i>-axis.
     * @param sizeX2    The number of pixels along the <i>X2</i>-axis.
     */
    RenderRGBWaveTask(byte[] band, Plane2D plane, QuantumStrategy qs,
                      CodomainChain cc, int alpha, int sizeX1, int sizeX2)
    {
        this.band = band;
        this.plane = plane;
        this.qs = qs;
        this.cc = cc;
        this.alpha = alpha;
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
        float alpha = ((float) this.alpha)/255;
        for (x2 = 0; x2 < sizeX2; ++x2) {
            for (x1 = 0; x1 < sizeX1; ++x1) {
                pix = sizeX1*x2+x1;
                discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                discreteValue = cc.transform(discreteValue);
                band[pix] = (byte) (discreteValue*alpha);
            } 
        } 
        return band;
    }

}
