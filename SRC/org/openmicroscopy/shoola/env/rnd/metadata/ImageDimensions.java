/*
 * org.openmicroscopy.shoola.env.rnd.metadata.ImageDimensions
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

package org.openmicroscopy.shoola.env.rnd.metadata;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The dimension of an OME image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImageDimensions
{
	/** Number of pixels on the X-axis. */
	public final int    sizeX;
	
	/** Number of pixels on the Y-axis. */
	public final int    sizeY;
	
	/** Size of image stack i.e. number of XY-planes. */
	public final int    sizeZ;
	
	/** Number of wavelengths in the image. */
	public final int    sizeW;
	
	/** Number of timepoints in the image. */    
	public final int    sizeT;
    
	/** 
	 * Creates a new object to store the passed dimensions.
	 *
	 * @param   sizeX   Number of pixels on the X-axis.
	 * @param   sizeY   Number of pixels on the Y-axis.
	 * @param   sizeZ   Size of image stack, that is number of XY-planes.
	 * @param   sizeW   Number of wavelengths in the image. 
	 * @param   sizeT   Number of timepoints in the image. 
	 */
	public ImageDimensions(int sizeX, int sizeY, int sizeZ, 
							int sizeW, int sizeT)
	{
		if (sizeX <= 0 || sizeY <= 0 ||  sizeZ <= 0)
			throw new IllegalArgumentException("Spatial dimensions must " +
				"be positive");
		if (sizeW < 1)
			throw new IllegalArgumentException("At least one wavelength " +
				"is required");
		if (sizeT < 1)
			throw new IllegalArgumentException("Timepoints must be positive");
			
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.sizeW = sizeW;
		this.sizeT = sizeT;
	}
    
}
