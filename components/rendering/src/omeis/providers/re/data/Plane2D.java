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

//Application-internal dependencies

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
	
    /** Contains the plane data. */
    private byte[]         data;
    
    /** Knows how to convert the pixel bytes into a double. */
	private BytesConverter strategy;
    
    /** The type of plane. */
	protected PlaneDef	   planeDef;
	
    /** How many bytes make up a pixel value. */
    protected int          bytesPerPixel;
	
    /** Number of pixels along the <i>X</i>-axis. */
    protected int          sizeX;
    
    /** Number of pixels along the <i>Y</i>-axis. */
    protected int          sizeY;
	

    /**
     * Constructor that sub-classes must call.
     * 
     * @param data Contains the plane data.
     * @param pDef The type of plane.
     * @param sizeX Number of pixels along the <i>X</i>-axis.
     * @param sizeY Number of pixels along the <i>Y</i>-axis.
     * @param bytesPerPixel How many bytes make up a pixel value
     * @param strategy Knows how to convert the pixel bytes into a double.
     */
	protected Plane2D(byte[] data, PlaneDef pDef, int sizeX, int sizeY, 
                      int bytesPerPixel, BytesConverter strategy)
	{
        this.data = data;
        this.planeDef = pDef;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
		this.bytesPerPixel = bytesPerPixel;
		this.strategy = strategy;
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
		return strategy.pack(data, offset, bytesPerPixel);
	}
	
}
