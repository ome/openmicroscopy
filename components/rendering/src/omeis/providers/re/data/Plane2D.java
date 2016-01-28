/*
 * omeis.providers.re.data.Plane2D
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.util.PixelData;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

/**
 * Allows to extract pixels intensity values from a given plane within a pixels
 * set. The plane can be one of the types defined by {@link PlaneDef} &#151;
 * <i>XY, ZY, XZ</i>. So extracting a pixel value from a sequence of bytes
 * given the pixel coordinates within the plane requires to know:
 * <ul>
 * <li>The offset, in the byte sequence, of the first byte containing the
 * value.</li>
 * <li>The number of bytes that make up the value.</li>
 * <li>How to interpret those bytes in order to convert them into a <code>
 *   double</code>
 * value.</li>
 * </ul>
 * <p>
 * An instance of this class is created with a byte array that contains the
 * plane. This array contains just the plane data in the case of an <i>XY</i>
 * plane, but the whole stack in which that plane belongs in the case of an
 * <i>ZY</i> or <i>XZ</i> plane. For this reason we have three concrete sub-
 * classes (one per plane type) that know how to calculate the offset.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 20:15:03 $) </small>
 * @since OME2.2
 */
/**
 * @author callan
 *
 */
public class Plane2D {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(Plane2D.class);

    /** Contains the plane data. */
    private PixelData data;

    /** The type of plane. */
    protected PlaneDef planeDef;

    /** How many bytes make up a pixel value. */
    protected int bytesPerPixel;

    /** Number of pixels along the <i>X</i>-axis. */
    protected int sizeX;

    /** Number of pixels along the <i>Y</i>-axis. */
    protected int sizeY;

    /** The sign of the type */
    protected boolean signed;
    
    /** The slice we're working with */
    protected int slice;

    /**
     * Constructor that sub-classes must call.
     * 
     * @param pDef
     *            The type of plane.
     * @param pixels
     *            The pixels set which the Plane2D references.
     * @param data
     *            The raw pixels.
     */
    public Plane2D(PlaneDef pDef, Pixels pixels, PixelData data) {
        this.planeDef = pDef;
        RegionDef region = pDef.getRegion();
        if (region != null) {
        	sizeX = region.getWidth();
        	sizeY = region.getHeight();
        } else {
        	sizeX = pixels.getSizeX();
            sizeY = pixels.getSizeY();
        }
        int stride = pDef.getStride();
        if (stride < 0) stride = 0;
        stride++;
        sizeX = sizeX/stride;
        sizeY = sizeY/stride;
        this.data = data;

        // Grab the pixel type from the pixels set
        PixelsType type = pixels.getPixelsType();

        this.bytesPerPixel = PlaneFactory.bytesPerPixel(type);
        //this.javaType = PlaneFactory.javaType(type);
        this.signed = PlaneFactory.isTypeSigned(type);
        this.slice = pDef.getSlice();

        log.debug("Created Plane2D with dimensions " + sizeX + "x" + sizeY + "x"
                + bytesPerPixel);
    }

    /**
     * Returns the pixel intensity value of the pixel at <code>(x1, x2)</code>.
     * The coordinates are relative to the <i>XY</i>, <i>ZY</i> or <i>XZ</i>
     * reference frame, depending on the plane type. (So <code>x1</code> is
     * <code>x</code> and <code>x2</code> is <code>y</code> if the plane
     * type is <i>XY</i>, etc.)
     * 
     * @param x1
     *            The first coordinate.
     * @param x2
     *            The second coordinate.
     * @return The intensity value.
     */
    public double getPixelValue(int x1, int x2) {
    	switch (slice)
    	{
    		case PlaneDef.XY:
    			return data.getPixelValueDirect(
    					bytesPerPixel * (sizeX * x2 + x1));
    		case PlaneDef.XZ:
    			return data.getPixelValueDirect(
    					bytesPerPixel
    	                * (x2 * sizeX * sizeY + sizeX * planeDef.getY() + x1));
    		case PlaneDef.ZY:
    			return data.getPixelValueDirect(
    					bytesPerPixel
    					* (x1 * sizeX * sizeY + sizeX * x2 + planeDef.getX()));
    		default:
    			throw new RuntimeException("Unknown PlaneDef slice: " + slice);
    	}
    }
    
    /**
     * Returns the pixel intensity value of the pixel at a given offset within
     * the backing buffer. This method takes into account bytes per pixel. So
     * the number of offsets is equal to the buffer size / 
     * <code>bytesPerPixel</code>.
     * 
     * @param offset The relative offset (taking into account the number of 
     * bytes per pixel) within the backing buffer.
     * @return The intensity value.
     */
    public double getPixelValue(int offset)
    {
    	return data.getPixelValue(offset);
    }

    /**
     * Returns <code>true</code> if the plane is an <code>XY-plane</code>,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isXYPlanar()
    {
    	return (slice == PlaneDef.XY);
    }

    /**
     * Returns the pixel data that is used to back this Plane.
     * 
     * @return See above.
     */
    public PixelData getData()
    {
    	return data;
    }
}
