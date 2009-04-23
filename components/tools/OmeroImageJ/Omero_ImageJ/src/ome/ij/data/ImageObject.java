/*
 * ome.ij.data.ImageObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.ij.data;


//Java imports
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries
import ij.ImagePlus;

//Application-internal dependencies
import omero.model.Pixels;
import omero.model.StatsInfo;
import omerojava.util.GatewayUtils;
import omerojava.util.Plane1D;

/** 
 * Wrap image data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImageObject 
{
	
	/** The pixels hosted by this class. */
	private Pixels 				pixels;
	
	/** The map hosting the plane. */
	private Map<Integer, byte[]> byteMap;
	
	/**
	 * Returns the number of bytes per pixels.
	 * 
	 * @return See above.
	 */
	private int getBytesPerPixels()
	{
		String type = pixels.getPixelsType().getValue().getValue();
		return GatewayUtils.getBytesPerPixels(type);
	}
	
    /**
     * Transforms 3D coords into linear coords.
     * The returned value <code>L</code> is calculated as follows: 
     * <nobr><code>L = sizeZ*sizeW*t + sizeZ*w + z</code></nobr>.
     * 
     * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
     * @param c The c coord.  Must be in the range <code>[0, sizec)</code>.
     * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
     * @return The linearized value corresponding to <code>(z, w, t)</code>.
     */
    private Integer linearize(int z, int c, int t)
    {
    	int sizeZ = pixels.getSizeZ().getValue();
    	int sizeC = pixels.getSizeC().getValue();
        if (z < 0 || sizeZ <= z) 
            throw new IllegalArgumentException(
                    "z out of range [0, "+sizeZ+"): "+z+".");
        if (c < 0 || sizeC <= c) 
            throw new IllegalArgumentException(
                    "w out of range [0, "+sizeC+"): "+c+".");
        if (t < 0 || pixels.getSizeT().getValue() <= t) 
            throw new IllegalArgumentException(
                    "t out of range [0, "+pixels.getSizeT()+"): "+t+".");
        return new Integer(sizeZ*sizeC*t + sizeZ*c + z);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param name The name of the image.
	 * @param pixels The pixels set to host.
	 */
	ImageObject(Pixels pixels)
	{
		if (pixels == null)
			throw new IllegalArgumentException("No pixels set.");
		this.pixels = pixels;
		byteMap = new HashMap<Integer, byte[]>();
	}
	
	/**
	 * Sets the plane.
	 * 
	 * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
     * @param c The c coord.  Must be in the range <code>[0, sizec)</code>.
     * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
     * @param array The value to set.
	 */
	/*
	void setPlane(int z, int c, int t, byte[] array)
	{
		Integer v = linearize(z, c, t);
		byteMap.put(v, array);
	}
	*/
	/**
	 * Returns the array of bytes corresponding to the plane.
	 * 
	 * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
     * @param c The c coord.  Must be in the range <code>[0, sizec)</code>.
     * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
	 * @return See above.
	 */
	/*
	public byte[] getPlane(int z, int c, int t)
	{
		Integer v = linearize(z, c, t);
		return byteMap.get(v);
	}
	*/
	
	/**
	 * Converts the array of bytes into a mapped plane depending on
	 * the pixels type.
	 * 
	 * @param plane The plane to handle.
	 * @return See above.
	 */
	public Object getMappedPlane(byte[] plane)
	{
		if (plane == null) return null;
		try {
			Plane1D p1;
			switch (getBytesPerPixels()) {
				case 1: return plane;
				case 2: 
					p1 = GatewayUtils.getPlane1D(pixels, plane);
					return p1.getPixelsArrayAsShort(); 
				case 4: 
					p1 = GatewayUtils.getPlane1D(pixels, plane);
					return p1.getPixelsArrayAsInt();
			}
		} catch (Exception e) {}
		return null;
	}
	

	/**
	 * Returns the number of z-sections.
	 * 
	 * @return See above.
	 */
	public int getSizeZ() { return pixels.getSizeZ().getValue(); }
	
	/**
	 * Returns the number of timepoints.
	 * 
	 * @return See above.
	 */
	public int getSizeT() { return pixels.getSizeT().getValue(); }
	
	/**
	 * Returns the number of channels.
	 * 
	 * @return See above.
	 */
	public int getSizeC() { return pixels.getSizeC().getValue(); }
	
	/**
	 * Returns the number of pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	public int getSizeX() { return pixels.getSizeX().getValue(); }
	
	/**
	 * Returns the number of pixels along the Y-axis.
	 * 
	 * @return See above.
	 */
	public int getSizeY() { return pixels.getSizeY().getValue(); }
	
	/**
	 * Returns the <code>ImagePlus type</code> corresponding to the pixels
	 * type.
	 * 
	 * @return See above.
	 */
	public int getImagePlusType()
	{
		switch (getBytesPerPixels()) {
	        case 1: return ImagePlus.GRAY8;
	        case 2: return ImagePlus.GRAY16;
	        case 4:
	        default:
	        	return ImagePlus.GRAY32;
		}
	}
	
	/**
	 * Returns the global minimum for the specified channel.
	 * 
	 * @param c The channel
	 * @return See above.
	 */
	public double getGlobalMin(int c)
	{
		StatsInfo info = pixels.getChannel(c).getStatsInfo();
		if (info == null) return 0;
		return info.getGlobalMin().getValue();
	}
	
	/**
	 * Returns the global maximum for the specified channel.
	 * 
	 * @param c The channel
	 * @return See above.
	 */
	public double getGlobalMax(int c)
	{
		StatsInfo info = pixels.getChannel(c).getStatsInfo();
		if (info == null) return 1;
		return info.getGlobalMax().getValue();
	}

}
