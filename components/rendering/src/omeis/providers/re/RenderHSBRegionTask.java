/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.util.PixelData;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.lut.LutReader;
import omeis.providers.re.quantum.BinaryMaskQuantizer;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * A task object to render an image region asynchronously. This task is used by
 * the {@link HSBStrategy} to do concurrent rendering if more than one region
 * has to be processed.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/17 12:57:33 $) </small>
 * @since OMERO3.0
 */
class RenderHSBRegionTask implements RenderingTask {
	
    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(RenderHSBRegionTask.class);

    /** Buffer to hold the output image's data. */
    private RGBBuffer dataBuffer;

    /** The wavelength data. */
    private List<Plane2D> wData;

    /** How to quantize a pixel intensity value. */
    private List<QuantumStrategy> strategies;

    /**
     * The spatial transformations to apply to the quantized data.
     * One per channel.
     */
    private List<CodomainChain> chains;

    /**
     * The color components used when mapping a quantized value onto the color
     * space.
     */
    private List<int[]> colors;

    /** The <i>X1</i>-axis start */
    private int x1Start;

    /** The <i>X1</i>-axis end */
    private int x1End;

    /** The <i>X2</i>-axis start */
    private int x2Start;

    /** The <i>X2</i>-axis end */
    private int x2End;
    
    /** The optimizations that the renderer has turned on for us. */
    private Optimizations optimizations;

    /** The collection of readers.*/
    private List<LutReader> readers;

    /**
     * Creates a new instance to render a wavelength.
     * 
     * @param dataBuffer
     *            Buffer to hold the output image's data.
     * @param wData
     *            The wavelength data.
     * @param strategies
     *            The quantum strategy for each wavelength.
     * @param cc
     *            The spatial transformations to apply to the quantized data.
     * @param colors
     *            The color components to use when mapping quantized values onto
     *            the color space.
     * @param x1Start
     *            The <i>X1</i>-axis start
     * @param x1End
     *            The <i>X1</i>-axis end
     * @param x2Start
     *            The <i>X2</i>-axis start
     * @param x2End
     *            The <i>X2</i>-axis end
     * @param readers The lookup table readers.
     */
    RenderHSBRegionTask(RGBBuffer dataBuffer, List<Plane2D> wData,
            List<QuantumStrategy> strategies, List<CodomainChain> chains,
            List<int[]> colors, Optimizations optimizations,
            int x1Start, int x1End, int x2Start, int x2End,
            List<LutReader> readers) {
        this.dataBuffer = dataBuffer;
        this.wData = wData;
        this.strategies = strategies;
        this.chains = chains;
        this.colors = colors;
        this.optimizations = optimizations;
        this.x1Start = x1Start;
        this.x1End = x1End;
        this.x2Start = x2Start;
        this.x2End = x2End;
        this.readers = readers;
    }

    /**
     * Renders the region.
     * 
     * @throws QuantizationException
     *             If an error occurs while quantizing a pixels intensity value.
     */
    public Object call() throws QuantizationException {
    	log.debug("Buffer type: "+dataBuffer);
        if (dataBuffer instanceof RGBIntBuffer) {
            renderPackedInt();
        } else if (dataBuffer instanceof RGBAIntBuffer){
            renderPackedIntAsRGBA();
        } else {
	      //renderPackedInt();
          renderBanded(); // cf. ticket #1646
    	}
        return null;
    }

    /**
     * Renders into a banded byte buffer.
     * 
     * @throws QuantizationException
     *             if there is an error during pixel value quantization.
     */
    private void renderBanded() throws QuantizationException {
        int discreteValue, pix;
        int rValue, gValue, bValue;
        float v;

        int width = x1End - x1Start;
        int i = 0;
        byte[] r = dataBuffer.getRedBand();
        byte[] g = dataBuffer.getGreenBand();
        byte[] b = dataBuffer.getBlueBand();
        LutReader reader;
        CodomainChain cc;
        for (Plane2D plane : wData) {
            int[] color = colors.get(i);
            reader = readers.get(i);
            cc = chains.get(i);
            boolean hasMap = cc.hasMapContext();
            QuantumStrategy qs = strategies.get(i);
            int rColor = color[ColorsFactory.RED_INDEX];
            int gColor = color[ColorsFactory.GREEN_INDEX];
            int bColor = color[ColorsFactory.BLUE_INDEX];

            float alpha = new Float(
                    color[ColorsFactory.ALPHA_INDEX]).floatValue() / 65025;// 255*255
            for (int x2 = x2Start; x2 < x2End; ++x2) {
                for (int x1 = x1Start; x1 < x1End; ++x1) {
                    pix = width * x2 + x1;
                    discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
                    if (hasMap) {
                        discreteValue = cc.transform(discreteValue);
                    }

                    if (reader != null) {
                        int r1 = ((r[pix] & 0x00FF0000) >> 16);
                        int r2 = reader.getRed(discreteValue) & 0xFF;
                        int g1 = ((g[pix] & 0x0000FF00) >> 8);
                        int g2 = reader.getGreen(discreteValue) & 0xFF;
                        int b1 = (b[pix] & 0x000000FF);
                        int b2 = reader.getBlue(discreteValue) & 0xFF;
                        r[pix] = (byte) (r1+r2);
                        g[pix] = (byte) (g1+g2);
                        b[pix] = (byte) (b1+b2);
                        continue;
                    }
                    // Pre-multiply the alpha component and add the existing
                    // colour value to the new colour value.
                    v = discreteValue * alpha;
                    rValue = (int) (rColor * v) + r[pix];
                    gValue = (int) (gColor * v) + g[pix];
                    bValue = (int) (bColor * v) + b[pix];

                    // Ensure that each colour component value is between 0 and
                    // 255 (byte). We must make *certain* that values to not
                    // wrap over 255 otherwise there will be corruption
                    // introduced into the rendered image.
                    if (rValue > 255) {
                        rValue = 255;
                    }
                    if (gValue > 255) {
                        gValue = 255;
                    }
                    if (bValue > 255) {
                        bValue = 255;
                    }
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
     * @throws QuantizationException
     *             if there is an error during pixel value quantization.
     */
    private void renderPackedInt() throws QuantizationException {
        int discreteValue, pix;
        double redRatio, greenRatio, blueRatio;
        int rValue, gValue, bValue;
        int newRValue, newGValue, newBValue;
        int colorOffset = 24;  // Only used when we're doing primary color.

        int width = x1End - x1Start;
        int i = 0;
        int[] buf = ((RGBIntBuffer) dataBuffer).getDataBuffer();
        boolean isPrimaryColor = optimizations.isPrimaryColorEnabled();
        boolean isAlphaless = optimizations.isAlphalessRendering();
        LutReader reader;
        CodomainChain cc;

        for (Plane2D plane : wData) {
            int[] color = colors.get(i);
            reader = readers.get(i);
            cc = chains.get(i);
            boolean hasMap = cc.hasMapContext();
            QuantumStrategy qs = strategies.get(i);
            boolean isMask = qs instanceof BinaryMaskQuantizer? true : false;
            redRatio = color[ColorsFactory.RED_INDEX] > 0 ? 
                    color[ColorsFactory.RED_INDEX] / 255.0 : 0.0;
            greenRatio = color[ColorsFactory.GREEN_INDEX] > 0 ? 
                     color[ColorsFactory.GREEN_INDEX] / 255.0 : 0.0;
            blueRatio = color[ColorsFactory.BLUE_INDEX] > 0 ? 
                     color[ColorsFactory.BLUE_INDEX] / 255.0 : 0.0;
            boolean isXYPlanar = plane.isXYPlanar();
            PixelData data = plane.getData();
            int bytesPerPixel = data.bytesPerPixel();

            // Get our color offset if we've got the primary color optimization
            // enabled.
            if (isPrimaryColor && reader == null)
                colorOffset = getColorOffset(color);

            float alpha = new Integer(
                    color[ColorsFactory.ALPHA_INDEX]).floatValue() / 255;
            for (int x2 = x2Start; x2 < x2End; ++x2) {
                for (int x1 = x1Start; x1 < x1End; ++x1) {
                    pix = width * x2 + x1;
                    if (isXYPlanar)
                        discreteValue =
                        qs.quantize(
                                data.getPixelValueDirect(pix * bytesPerPixel));
                    else
                        discreteValue =
                            qs.quantize(plane.getPixelValue(x1, x2));
                    if (hasMap) {
                        discreteValue = cc.transform(discreteValue);
                    }
                    if (reader != null) {
                        int r1 = ((buf[pix] & 0x00FF0000) >> 16);
                        int r2 = reader.getRed(discreteValue) & 0xFF;
                        int g1 = ((buf[pix] & 0x0000FF00) >> 8);
                        int g2 = reader.getGreen(discreteValue) & 0xFF;
                        int b1 = (buf[pix] & 0x000000FF);
                        int b2 = reader.getBlue(discreteValue) & 0xFF;
                        int r = r1+r2;
                        if (r > 255) {
                            r = 255;
                        }
                        int g = g1+g2;
                        if (g > 255) {
                            g = 255;
                        }
                        int b = b1+b2;
                        if (b > 255) {
                            b = 255;
                        }
                        buf[pix] = 0xFF000000 | r << 16 | g << 8 | b;
                        continue;
                    }
                    // Primary colour optimization is in effect, we don't need
                    // to do any of the sillyness below just shift the value
                    // into the correct colour component slot and move on to
                    // the next pixel value.
                    if (colorOffset != 24)
                    {
                        buf[pix] |= 0xFF000000;  // Alpha.
                        buf[pix] |= discreteValue << colorOffset;
                        continue;
                    }
                    newRValue = (int) (redRatio * discreteValue);
                    newGValue = (int) (greenRatio * discreteValue);
                    newBValue = (int) (blueRatio * discreteValue);
                    
                    // Pre-multiply the alpha for each colour component if the
                    // image has a non-1.0 alpha component.
                    if (!isAlphaless)
                    {
                    	newRValue *= alpha;
                    	newGValue *= alpha;
                    	newBValue *= alpha;
                    }

                    if (isMask && discreteValue == 255) {
                    	// Since the mask is a hard value, we do not want to
                    	// compromise on colour fidelity. Packed each colour
                    	// component along with a 1.0 alpha into the buffer so
                    	// that buffered images that use this buffer can be
                    	// type 1 (3 bands, pre-multiplied alpha) or type 2
                        // (4 bands, alpha component included).
                        buf[pix] = 0xFF000000 | newRValue << 16
                                   | newGValue << 8 | newBValue;
                        continue;
                    }
                    // Add the existing colour component values to the new
                    // colour component values.
                    rValue = ((buf[pix] & 0x00FF0000) >> 16) + newRValue;
                    gValue = ((buf[pix] & 0x0000FF00) >> 8) + newGValue;
                    bValue = (buf[pix] & 0x000000FF) + newBValue;

                    // Ensure that each colour component value is between 0 and
                    // 255 (byte). We must make *certain* that values do not
                    // wrap over 255 otherwise there will be corruption
                    // introduced into the rendered image. The value may be over
                    // 255 if we have mapped two high intensity channels to
                    // the same color.
                    if (rValue > 255) {
                        rValue = 255;
                    }
                    if (gValue > 255) {
                        gValue = 255;
                    }
                    if (bValue > 255) {
                        bValue = 255;
                    }

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

    /**
     * Renders into a packed integer array.
     * 
     * @throws QuantizationException
     *             if there is an error during pixel value quantization.
     */
    private void renderPackedIntAsRGBA() throws QuantizationException {
        int discreteValue, pix;
        double redRatio, greenRatio, blueRatio;
        int rValue, gValue, bValue;
        int newRValue, newGValue, newBValue;
        int colorOffset = 32;  // Only used when we're doing primary color.

        int width = x1End - x1Start;
        int i = 0;
        int[] buf = ((RGBAIntBuffer) dataBuffer).getDataBuffer();
        boolean isPrimaryColor = optimizations.isPrimaryColorEnabled();
        boolean isAlphaless = optimizations.isAlphalessRendering();
        LutReader reader;
        CodomainChain cc;
        for (Plane2D plane : wData) {
            int[] color = colors.get(i);
            reader = readers.get(i);
            cc = chains.get(i);
            boolean hasMap = cc.hasMapContext();
            QuantumStrategy qs = strategies.get(i);
            redRatio = color[ColorsFactory.RED_INDEX] > 0 ? 
                    color[ColorsFactory.RED_INDEX] / 255.0 : 0.0;
            greenRatio = color[ColorsFactory.GREEN_INDEX] > 0 ? 
                    color[ColorsFactory.GREEN_INDEX] / 255.0 : 0.0;
            blueRatio = color[ColorsFactory.BLUE_INDEX] > 0 ? 
                    color[ColorsFactory.BLUE_INDEX] / 255.0 : 0.0;
            boolean isXYPlanar = plane.isXYPlanar();
            PixelData data = plane.getData();
            int bytesPerPixel = data.bytesPerPixel();

            // Get our color offset if we've got the primary color optimization
            // enabled.
            if (isPrimaryColor)
                colorOffset = getColorOffsetAsRGBA(color);

            float alpha = new Integer(color[ColorsFactory.ALPHA_INDEX]).floatValue() / 255;
            for (int x2 = x2Start; x2 < x2End; ++x2) {
                for (int x1 = x1Start; x1 < x1End; ++x1) {
                    pix = width * x2 + x1;
                    if (isXYPlanar)
                        discreteValue =
                        qs.quantize(
                                data.getPixelValueDirect(pix * bytesPerPixel));
                    else
                        discreteValue =
                            qs.quantize(plane.getPixelValue(x1, x2));
                    if (hasMap) {
                        discreteValue = cc.transform(discreteValue);
                    }
                    if (reader != null) {
                        int r1 = ((buf[pix] & 0xFF000000) >> 24);
                        int r2 = reader.getRed(discreteValue) & 0xFF;
                        int g1 = ((buf[pix] & 0x00FF0000) >> 16);
                        int g2 = reader.getGreen(discreteValue) & 0xFF;
                        int b1 = ((buf[pix] & 0x0000FF00) >> 8);
                        int b2 = reader.getBlue(discreteValue) & 0xFF;
                        int r = r1+r2;
                        if (r > 255) {
                            r = 255;
                        }
                        int g = g1+g2;
                        if (g > 255) {
                            g = 255;
                        }
                        int b = b1+b2;
                        if (b > 255) {
                            b = 255;
                        }
                        buf[pix] = 0x000000FF | r << 24 | g << 16 | b << 8;
                        continue;
                    }
                    // Primary colour optimization is in effect, we just shift
                    // the value into the correct colour component slot and
                    // move on to the next pixel value.
                    if (colorOffset != 32)
                    {
                        buf[pix] |= 0x000000FF;  // Alpha.
                        buf[pix] |= discreteValue << colorOffset;
                        continue;
                    }
                    newRValue = (int) (redRatio * discreteValue);
                    newGValue = (int) (greenRatio * discreteValue);
                    newBValue = (int) (blueRatio * discreteValue);

                    // Pre-multiply the alpha for each colour component if the
                    // image has a non-1.0 alpha component.
                    if (!isAlphaless)
                    {
                        newRValue *= alpha;
                        newGValue *= alpha;
                        newBValue *= alpha;
                    }

                    // Add the existing colour component values to the new
                    // colour component values.
                    rValue = ((buf[pix] & 0xFF000000) >> 24) + newRValue;
                    gValue = ((buf[pix] & 0x00FF0000) >> 16) + newGValue;
                    bValue = ((buf[pix] & 0x0000FF00) >> 8) + newBValue;

                    // Ensure that each colour component value is between 0 and
                    // 255 (byte). We must make *certain* that values do not
                    // wrap over 255 otherwise there will be corruption
                    // introduced into the rendered image. The value may be over
                    // 255 if we have mapped two high intensity channels to
                    // the same color.
                    if (rValue > 255) {
                        rValue = 255;
                    }
                    if (gValue > 255) {
                        gValue = 255;
                    }
                    if (bValue > 255) {
                        bValue = 255;
                    }

                    // Packed each colour component along with a 1.0 alpha into
                    // the buffer so that buffered images that use this buffer
                    // can be type 1 (3 bands, pre-multiplied alpha) or type 2
                    // (4 bands, alpha component included).
                    buf[pix] = 0x000000FF | rValue << 24 | gValue << 16 | bValue<<8;
                }
            }

            i++;
        }
    }
    
    /**
     * Returns a color offset based on which color component is 0xFF.
     * @param color the color to check.
     * @return an integer color offset in bits.
     */
    private int getColorOffset(int[] color)
    {
    	if (color[ColorsFactory.RED_INDEX] == 255)
    		return 16;
    	if (color[ColorsFactory.GREEN_INDEX] == 255)
    		return 8;
    	if (color[ColorsFactory.BLUE_INDEX] == 255)
    		return 0;
    	throw new IllegalArgumentException(
    			"Unable to find color component offset in color.");
    }

    /**
     * Returns a color offset based on which color component is 0xFF.
     * @param color the color to check. This is for colour components
	 * of RGBA, rather than java colour components which are ARGB.
     * @return an integer color offset in bits.
     */
    private int getColorOffsetAsRGBA(int[] color)
    {
    	if (color[ColorsFactory.RED_INDEX] == 255)
    		return 24;
    	if (color[ColorsFactory.GREEN_INDEX] == 255)
    		return 16;
    	if (color[ColorsFactory.BLUE_INDEX] == 255)
    		return 8;
    	throw new IllegalArgumentException(
    			"Unable to find color component offset in color.");
    }
}
