/*
 * org.openmicroscopy.shoola.env.data.PixelsServiceAdapter
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.env.data;

//AF - needed for tmp hack, to be removed.
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.is.PixelsFactory;
import org.openmicroscopy.is.PixelsFileFormat;
import org.openmicroscopy.is.PlaneStatistics;
import org.openmicroscopy.is.StackStatistics;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * Class that implements the PixelsService, using the OMEDS PixelsFactory.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PixelsServiceAdapter
    implements PixelsService
{
    private DataFactory factory;
    private Registry registry;
    private PixelsFactory pixelsFactory;
    
    public PixelsServiceAdapter(DataFactory factory)
        throws IllegalArgumentException
    {
        if(factory == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        this.factory = factory;
        this.pixelsFactory = new PixelsFactory(factory);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#finishPixels(org.openmicroscopy.ds.st.Pixels)
     */
    public void finishPixels(Pixels pixels) throws ImageServerException
    {
        pixelsFactory.finishPixels(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getComposite(org.openmicroscopy.ds.st.Pixels, org.openmicroscopy.is.CompositingSettings)
     */
    public BufferedImage getComposite(Pixels pixels, CompositingSettings settings)
        throws ImageServerException
    {
        return pixelsFactory.getComposite(pixels,settings);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPixels(org.openmicroscopy.ds.st.Pixels, boolean)
     */
    public byte[] getPixels(Pixels pixels, boolean bigEndian)
        throws ImageServerException
    {
        return pixelsFactory.getPixels(pixels,bigEndian);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPixelsInfo(org.openmicroscopy.ds.st.Pixels)
     */
    public PixelsFileFormat getPixelsInfo(Pixels pixels)
        throws ImageServerException
    {
        return pixelsFactory.getPixelsInfo(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPixelsServerPath(org.openmicroscopy.ds.st.Pixels)
     */
    public String getPixelsServerPath(Pixels pixels)
        throws ImageServerException
    {
        return pixelsFactory.getPixelsServerPath(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPixelsSHA1(org.openmicroscopy.ds.st.Pixels)
     */
    public String getPixelsSHA1(Pixels pixels) throws ImageServerException
    {
        return pixelsFactory.getPixelsSHA1(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPlane(org.openmicroscopy.ds.st.Pixels, int, int, int, boolean)
     */
    public byte[] getPlane(
        Pixels pixels,
        int theZ,
        int theC,
        int theT,
        boolean bigEndian)
        throws ImageServerException
    {
        return pixelsFactory.getPlane(pixels,theZ,theC,theT,bigEndian);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getPlaneStatistics(org.openmicroscopy.ds.st.Pixels)
     */
    public PlaneStatistics getPlaneStatistics(Pixels pixels)
        throws ImageServerException
    {
        return pixelsFactory.getPlaneStatistics(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getROI(org.openmicroscopy.ds.st.Pixels, int, int, int, int, int, int, int, int, int, int, boolean)
     */
    public byte[] getROI(
        Pixels pixels,
        int x0,
        int y0,
        int z0,
        int c0,
        int t0,
        int x1,
        int y1,
        int z1,
        int c1,
        int t1,
        boolean bigEndian)
        throws ImageServerException
    {
        return pixelsFactory.getROI(pixels,x0,y0,z0,c0,t0,
                                           x1,y1,z1,c1,t1,bigEndian);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getStack(org.openmicroscopy.ds.st.Pixels, int, int, boolean)
     */
    public byte[] getStack(
        Pixels pixels,
        int theC,
        int theT,
        boolean bigEndian)
        throws ImageServerException
    {
        return pixelsFactory.getStack(pixels,theC,theT,bigEndian);
    }
    
    //AF - tmp hack, don't code against it!
	public InputStream getStackStream(
		Pixels pixels,
		int theC,
		int theT,
		boolean bigEndian)
		throws ImageServerException
	{
		return pixelsFactory.getStackStream(pixels,theC,theT,bigEndian);
	}
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getStackStatistics(org.openmicroscopy.ds.st.Pixels)
     */
    public StackStatistics getStackStatistics(Pixels pixels)
        throws ImageServerException
    {
        return pixelsFactory.getStackStatistics(pixels);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#getThumbnail(org.openmicroscopy.ds.st.Pixels)
     */
    public BufferedImage getThumbnail(Pixels pixels) throws ImageServerException
    {
        return pixelsFactory.getThumbnail(pixels);
    }
    
    public BufferedImage getThumbnail(Pixels pixels, int sizeX, int sizeY) throws ImageServerException
    {
        return pixelsFactory.getThumbnail(pixels,sizeX,sizeY);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#isPixelsFinished(org.openmicroscopy.ds.st.Pixels)
     */
    public boolean isPixelsFinished(Pixels pixels) throws ImageServerException
    {
        return pixelsFactory.isPixelsFinished(pixels);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#newPixels(int, int, int, int, int, int, boolean, boolean)
     */
    public Pixels newPixels(
        int sizeX,
        int sizeY,
        int sizeZ,
        int sizeC,
        int sizeT,
        int bytesPerPixel,
        boolean isSigned,
        boolean isFloat)
        throws ImageServerException
    {
        // TODO: figure out how to create new pixels with the specified
        // parameters within the PixelsFactory framework
        return null;
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#newPixels(org.openmicroscopy.is.PixelsFileFormat)
     */
    public Pixels newPixels(PixelsFileFormat format)
        throws ImageServerException
    {
        // TODO: figure out how to create new pixels with the specified
        // parameters within the PixelsFactory framework
        return null;
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setPixels(org.openmicroscopy.ds.st.Pixels, byte[], boolean)
     */
    public void setPixels(Pixels pixels, byte[] buf, boolean bigEndian)
        throws ImageServerException
    {
        pixelsFactory.setPixels(pixels,buf,bigEndian);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setPixels(org.openmicroscopy.ds.st.Pixels, java.io.File, boolean)
     */
    public void setPixels(Pixels pixels, File file, boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        pixelsFactory.setPixels(pixels,file,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setPlane(org.openmicroscopy.ds.st.Pixels, int, int, int, byte[], boolean)
     */
    public void setPlane(
        Pixels pixels,
        int theZ,
        int theC,
        int theT,
        byte[] buf,
        boolean bigEndian)
        throws ImageServerException
    {
        pixelsFactory.setPlane(pixels,theZ,theC,theT,buf,bigEndian);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setPlane(org.openmicroscopy.ds.st.Pixels, int, int, int, java.io.File, boolean)
     */
    public void setPlane(
        Pixels pixels,
        int theZ,
        int theC,
        int theT,
        File file,
        boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        pixelsFactory.setPlane(pixels,theZ,theC,theT,file,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setROI(org.openmicroscopy.ds.st.Pixels, int, int, int, int, int, int, int, int, int, int, byte[], boolean)
     */
    public void setROI(
        Pixels pixels,
        int x0,
        int y0,
        int z0,
        int c0,
        int t0,
        int x1,
        int y1,
        int z1,
        int c1,
        int t1,
        byte[] buf,
        boolean bigEndian)
        throws ImageServerException
    {
        pixelsFactory.setROI(pixels,x0,y0,z0,c0,t0,
                                    x1,y1,z1,c1,t1,buf,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setROI(org.openmicroscopy.ds.st.Pixels, int, int, int, int, int, int, int, int, int, int, java.io.File, boolean)
     */
    public void setROI(
        Pixels pixels,
        int x0,
        int y0,
        int z0,
        int c0,
        int t0,
        int x1,
        int y1,
        int z1,
        int c1,
        int t1,
        File file,
        boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        pixelsFactory.setROI(pixels,x0,y0,z0,c0,t0,
                                    x1,y1,z1,c1,t1,file,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setStack(org.openmicroscopy.ds.st.Pixels, int, int, byte[], boolean)
     */
    public void setStack(
        Pixels pixels,
        int theC,
        int theT,
        byte[] buf,
        boolean bigEndian)
        throws ImageServerException
    {
        pixelsFactory.setStack(pixels,theC,theT,buf,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setStack(org.openmicroscopy.ds.st.Pixels, int, int, java.io.File, boolean)
     */
    public void setStack(
        Pixels pixels,
        int theC,
        int theT,
        File file,
        boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        pixelsFactory.setStack(pixels,theC,theT,file,bigEndian);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.PixelsService#setThumbnail(org.openmicroscopy.ds.st.Pixels, org.openmicroscopy.is.CompositingSettings)
     */
    public void setThumbnail(Pixels pixels, CompositingSettings settings)
        throws ImageServerException
    {
        pixelsFactory.setThumbnail(pixels,settings);
    }

}
