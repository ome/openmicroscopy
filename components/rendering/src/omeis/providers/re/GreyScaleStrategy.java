/*
 * omeis.providers.re.GreyScaleStrategy
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.io.IOException;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Transforms a plane within a given pixels set into a greyscale image.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.7 $ $Date:
 *          2005/06/22 17:09:48 $) </small>
 * @since OME2.2
 */
class GreyScaleStrategy extends RenderingStrategy {

    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(Renderer.class);

    /**
     * The number of pixels on the <i>X1</i>-axis. This is the <i>X</i>-axis
     * in the case of an <i>XY</i> or <i>XZ</i> plane. Otherwise it is the
     * <i>Z</i>-axis &#151; <i>ZY</i> plane.
     */
    private int sizeX1;

    /**
     * The number of pixels on the X2-axis. This is the <i>Y</i>-axis in the
     * case of an <i>XY</i> or <i>ZY</i> plane. Otherwise it is the <i>Z</i>-axis
     * &#151; <i>XZ</i> plane.
     */
    private int sizeX2;

    /** The rendering context. */
    private Renderer renderer;

    /**
     * Initializes the <code>sizeX1</code> and <code>sizeX2</code> fields
     * according to the specified {@link PlaneDef#getSlice() slice}.
     * 
     * @param pd
     *            Reference to the plane definition defined for the strategy.
     * @param pixels
     *            Dimensions of the pixels set.
     */
    private void initAxesSize(PlaneDef pd, Pixels pixels) {
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
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("Invalid slice ID: " + pd.getSlice()
                    + ".", nfe);
        }
    }

    /**
     * Renders the specified wavelength (channel).
     * 
     * @param dataBuf
     *            The buffer to hold the output image's data.
     * @param plane
     *            Defines the plane to render.
     * @param qs
     *            Knows how to quantize a pixel intensity value.
     * @param color
     *            The color components used when mapping a quantized value onto
     *            the color space.
     * @throws QuantizationException
     *             If an error occurs while quantizing a pixels intensity value.
     */
    private void renderWave(RGBBuffer dataBuf, Plane2D plane, Color color,
            QuantumStrategy qs) throws QuantizationException {
        CodomainChain cc = renderer.getCodomainChain();
        int x1, x2, discreteValue, pixelIndex;

        // Perform optimised pixel settings for integer arrays
        if (dataBuf instanceof RGBIntBuffer) {
            int alpha = color.getAlpha();
            int[] buf = ((RGBIntBuffer) dataBuf).getDataBuffer();
            for (x2 = 0; x2 < sizeX2; ++x2) {
                int index = sizeX1 * x2;
                for (x1 = 0; x1 < sizeX1; ++x1) {
                    discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                    discreteValue = cc.transform(discreteValue);
                    buf[index + x1] = alpha << 24 | discreteValue << 16
                            | discreteValue << 8 | discreteValue;
                }
            }
        } else // We have just a plain RGBBuffer
        {
            byte value;
            float alpha = color.getAlpha().floatValue() / 255;
            byte[] r = dataBuf.getRedBand();
            byte[] g = dataBuf.getBlueBand();
            byte[] b = dataBuf.getGreenBand();
            for (x2 = 0; x2 < sizeX2; ++x2) {
                for (x1 = 0; x1 < sizeX1; ++x1) {
                    pixelIndex = sizeX1 * x2 + x1;
                    discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                    discreteValue = cc.transform(discreteValue);
                    value = (byte) (discreteValue * alpha);
                    r[pixelIndex] = value;
                    g[pixelIndex] = value;
                    b[pixelIndex] = value;
                }
            }
        }
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBBuffer render(Renderer ctx, PlaneDef planeDef) throws IOException,
            QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        RenderingStats performanceStats = renderer.getStats();
        Pixels metadata = renderer.getMetadata();

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, metadata);
        performanceStats.startMalloc();
        log.info("Creating RGBBuffer of size " + sizeX1 + "x" + sizeX2);
        RGBBuffer buf = new RGBBuffer(sizeX1, sizeX2);
        performanceStats.endMalloc();

        render(buf, planeDef);
        return buf;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBIntBuffer renderAsPackedInt(Renderer ctx, PlaneDef planeDef)
            throws IOException, QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        RenderingStats performanceStats = renderer.getStats();
        Pixels metadata = renderer.getMetadata();

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, metadata);
        log.info("Creating RGBBuffer of size " + sizeX1 + "x" + sizeX2);
        RGBIntBuffer buf = new RGBIntBuffer(sizeX1, sizeX2);
        performanceStats.endMalloc();

        render(buf, planeDef);
        return buf;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    private void render(RGBBuffer buf, PlaneDef planeDef) throws IOException,
            QuantizationException {
        QuantumManager qManager = renderer.getQuantumManager();
        PixelBuffer pixels = renderer.getPixels();
        Pixels metadata = renderer.getMetadata();
        ChannelBinding[] cBindings = renderer.getChannelBindings();
        RenderingStats performanceStats = renderer.getStats();

        // Process the first active wavelength.
        Plane2D wData;
        for (int i = 0; i < cBindings.length; i++) {
            if (cBindings[i].getActive().booleanValue()) {
                // Get the raw data.
                performanceStats.startIO(i);
                wData = PlaneFactory.createPlane(planeDef, i, metadata, pixels);
                performanceStats.endIO(i);

                try { // Transform it into an RGB image.
                    performanceStats.startRendering();
                    renderWave(buf, wData, cBindings[i].getColor(), qManager
                            .getStrategyFor(i));
                    performanceStats.endRendering();
                } catch (QuantizationException e) {
                    e.setWavelength(i);
                    throw e;
                }
                break;
            }
        }
        // Make sure that the pixel buffer is cleansed properly.
        try {
            pixels.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceError(e.getMessage());
        }
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getImageSize(PlaneDef, Pixels)
     */
    @Override
    int getImageSize(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 * sizeX2 * 3;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getPlaneDimsAsString(PlaneDef, Pixels)
     */
    @Override
    String getPlaneDimsAsString(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 + "x" + sizeX2;
    }

}
