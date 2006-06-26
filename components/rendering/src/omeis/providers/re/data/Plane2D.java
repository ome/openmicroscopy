/*
 * omeis.providers.re.data.Plane2D
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

package omeis.providers.re.data;

//Java imports

//Third-party libraries
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import omeis.providers.re.Renderer;

/** 
 * Allows to extract pixels intensity values from a given plane within a
 * pixels set.
 * The plane can be one of the types defined by {@link PlaneDef} &#151; <i>XY,
 * ZY, XZ</i>.  So extracting a pixel value from a sequence of bytes given the 
 * pixel coordinates within the plane requires to know:
 * <ul>
 *  <li>The offset, in the byte sequence, of the first byte containing the
 *   value.</li>
 *  <li>The number of bytes that make up the value.</li>
 *  <li>How to interpret those bytes in order to convert them into a <code>
 *   double</code> value.</li>
 * </ul>
 * <p>An instance of this class is created with a byte array that contains the
 * plane.  This array contains just the plane data in the case of an <i>XY</i>
 * plane, but the whole stack in which that plane belongs in the case of an 
 * <i>ZY</i> or <i>XZ</i> plane.  For this reason we have three concrete sub-
 * classes (one per plane type) that know how to calculate the offset.  The
 * value conversion is delegated to a {@link BytesConverter} Strategy.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/08 20:15:03 $)
 * </small>
 * @since OME2.2
 */
public abstract class Plane2D
{  
    
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(Renderer.class);
    
    /** Contains the plane data. */
    private MappedByteBuffer data;
    
    /** The type of plane. */
	protected PlaneDef	   planeDef;
	
    /** How many bytes make up a pixel value. */
    protected int          bytesPerPixel;
	
    /** Number of pixels along the <i>X</i>-axis. */
    protected int          sizeX;
    
    /** Number of pixels along the <i>Y</i>-axis. */
    protected int          sizeY;
    
    /** The Java type that we're using for pixel value retrieval */
    protected int          javaType;
	

    /**
     * Constructor that sub-classes must call.
     * 
     * @param pDef The type of plane.
     * @param pixels The pixels set which the Plane2D references.
     * @param data The raw pixels.
     */
	protected Plane2D(PlaneDef pDef, Pixels pixels, MappedByteBuffer data)
	{
        this.planeDef = pDef;
        this.sizeX = pixels.getSizeX();
        this.sizeY = pixels.getSizeY();
        this.data = data;
        
        // FIXME: Hack for now while we're still using pixel data from the old
        // OMEIS repository! There really should be a configuration option for
        // this or potentially usage of an endianness flag on the Pixels set.
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        
        // Grab the pixel type from the pixels set
        PixelsType type = pixels.getPixelsType();
        
        this.bytesPerPixel = PlaneFactory.bytesPerPixel(type);
        this.javaType = PlaneFactory.javaType(type);
        
        log.info("Created Plane2D with dimensions " + sizeX + "x" + sizeY + "x"
                + bytesPerPixel);
	}
	
    /**
     * Sub-classes have to implement this method to return the offset of
     * the first byte storing the pixel intensity value at <code>(x1, x2)
     * </code>.
     * The coordinates are relative to the <i>XY</i>, <i>ZY</i> or <i>XZ</i>
     * reference frame, depending on the plane type.
     *  
     * @param x1 The first coordinate.
     * @param x2 The second coordinate.
     * @return The offset.
     */
	protected abstract int calculateOffset(int x1, int x2);

    /**
     * Returns the pixel intensity value of the pixel at <code>(x1, x2)</code>.
     * The coordinates are relative to the <i>XY</i>, <i>ZY</i> or <i>XZ</i>
     * reference frame, depending on the plane type.  (So <code>x1</code> is
     * <code>x</code> and <code>x2</code> is <code>y</code> if the plane type
     * is <i>XY</i>, etc.)
     * 
     * @param x1 The first coordinate.
     * @param x2 The second coordinate.
     * @return The intensity value.
     */
	public double getPixelValue(int x1, int x2) 
	{
		int offset = calculateOffset(x1, x2);

		switch(javaType)
		{
			case PlaneFactory.BYTE:
				byte i = data.get(offset);
				return i;
			case PlaneFactory.SHORT:
				return data.getShort(offset);
			case PlaneFactory.INT:
				return data.getInt(offset);
			case PlaneFactory.FLOAT:
				return data.getFloat(offset);
			case PlaneFactory.DOUBLE:
				return data.getDouble(offset);
		}
		throw new RuntimeException("Unknown pixel type.");
	}
	
}
