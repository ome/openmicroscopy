/*
 * omeis.providers.re.metadata.PixelsDimensions
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

package omeis.providers.re.metadata;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The dimensions of a pixels set within an <i>OME</i> Image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/09 17:04:26 $)
 * </small>
 * @since OME2.2
 */
public class PixelsDimensions
{
	
	/** Number of pixels on the <i>X</i>-axis. */
	public final int    sizeX;
	
	/** Number of pixels on the <i>Y</i>-axis. */
	public final int    sizeY;
	
	/** Size of the stack, i.e. number of <i>XY</i>-planes. */
	public final int    sizeZ;
	
	/** Number of wavelengths (channels). */
	public final int    sizeC;
	
	/** Number of timepoints. */    
	public final int    sizeT;
    
    /** Actual <i>X</i>-size of a pixel, in microns. */
    public final double pixelSizeX;
    
    /** Actual <i>Y</i>-size of a pixel, in microns. */
    public final double pixelSizeY;
    
    /** Actual <i>Z</i>-size of a pixel, in microns. */
    public final double pixelSizeZ;
    
    
	/** 
	 * Creates a new object to store the passed dimensions.
	 *
	 * @param sizeX Number of pixels on the <i>X</i>-axis.
	 * @param sizeY Number of pixels on the <i>Y</i>-axis.
	 * @param sizeZ Size of the stack, i.e. number of <i>XY</i>-planes.
	 * @param sizeC Number of wavelengths (channels). 
	 * @param sizeT Number of timepoints.
     * @param pixelSizeX Actual <i>X</i>-size of a pixel, in microns.
     * @param pixelSizeY Actual <i>Y</i>-size of a pixel, in microns.
     * @param pixelSizeZ Actual <i>Z</i>-size of a pixel, in microns.
     * @throws IllegalArgumentException If bad parameters are passed in.
	 */
	public PixelsDimensions(int sizeX, int sizeY, int sizeZ, 
							int sizeC, int sizeT, double pixelSizeX, 
                            double pixelSizeY, double pixelSizeZ)
	{
		if (sizeX <= 0 || sizeY <= 0 ||  sizeZ <= 0)
			throw new IllegalArgumentException(
				"Spatial dimensions must be positive.");
		if (sizeC < 1)
			throw new IllegalArgumentException(
				"At least one wavelength is required.");
		if (sizeT < 1)
			throw new IllegalArgumentException("Timepoints must be positive.");
        if (pixelSizeX < 0 || pixelSizeY < 0 || pixelSizeZ < 0)
            throw new IllegalArgumentException("Pixel size must be positive.");
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.sizeC = sizeC;
		this.sizeT = sizeT;
        this.pixelSizeX = pixelSizeX;
        this.pixelSizeY = pixelSizeY;
        this.pixelSizeZ = pixelSizeZ;
	}
    
}
