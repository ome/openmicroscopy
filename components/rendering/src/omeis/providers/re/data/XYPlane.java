/*
 * omeis.providers.re.data.XYPlane
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
 * Provides the {@link Plane2D} implementation for <i>XY</i> planes.
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
class XYPlane
	extends Plane2D
{

    /**
     * Creates a new instance.
     * 
     * @param data          Contains the data of the whole stack in which the 
     *                      <i>XZ</i>-plane belongs.
     * @param pDef          The type of plane.
     * @param sizeX         The number of pixels along the <i>X</i>-axis.
     * @param sizeY         The number of pixels along the <i>Y</i>-axis.
     * @param bytesPerPixel How many bytes make up a pixel value.
     * @param strategy      The strategy to convert the pixel bytes into a
     *                      double.
     */
    XYPlane(byte[] data, PlaneDef pDef, int sizeX, int sizeY, 
            int bytesPerPixel, BytesConverter strategy)
	{
		super(data, pDef, sizeX, sizeY, bytesPerPixel, strategy);
	}

    /** 
     * Implemented as specified by the superclass. 
     * @see Plane2D#calculateOffset(int, int)
     */
	protected int calculateOffset(int x1, int x2) 
	{ 
		return bytesPerPixel*(sizeX*x2+x1);
	}

}
