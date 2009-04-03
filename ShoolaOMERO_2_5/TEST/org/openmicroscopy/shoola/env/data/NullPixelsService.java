/*
 * org.openmicroscopy.shoola.env.data.NullPixelsService
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

package org.openmicroscopy.shoola.env.data;



//Java imports
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.is.PixelsFileFormat;
import org.openmicroscopy.is.PlaneStatistics;
import org.openmicroscopy.is.StackStatistics;

/** 
 * 
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
public class NullPixelsService
        implements PixelsService
{

    public Pixels newPixels(int sizeX, int sizeY, int sizeZ, int sizeC,
                            int sizeT, int bytesPerPixel, boolean isSigned,
                            boolean isFloat)
            throws ImageServerException
    {
        return null;
    }

    public Pixels newPixels(PixelsFileFormat format)
            throws ImageServerException
    {
        return null;
    }

    public PixelsFileFormat getPixelsInfo(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public String getPixelsSHA1(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public String getPixelsServerPath(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public boolean isPixelsFinished(Pixels pixels)
            throws ImageServerException
    {
        return false;
    }

    public byte[] getPixels(Pixels pixels, boolean bigEndian)
            throws ImageServerException
    {
        return null;
    }

    public byte[] getStack(Pixels pixels, int theC, int theT, boolean bigEndian)
            throws ImageServerException
    {
        return null;
    }

    public byte[] getPlane(Pixels pixels, int theZ, int theC, int theT,
                            boolean bigEndian)
            throws ImageServerException
    {
        return null;
    }

    public byte[] getROI(Pixels pixels, int x0, int y0, int z0, int c0, int t0,
                        int x1, int y1, int z1, int c1, int t1,
                        boolean bigEndian)
            throws ImageServerException
    {
        return null;
    }

    public void setPixels(Pixels pixels, byte[] buf, boolean bigEndian)
            throws ImageServerException {}

    public void setPixels(Pixels pixels, File file, boolean bigEndian)
            throws ImageServerException, FileNotFoundException {}

    public void setStack(Pixels pixels, int theC, int theT, byte[] buf,
                        boolean bigEndian)
            throws ImageServerException {}

    public void setStack(Pixels pixels, int theC, int theT, File file, 
                        boolean bigEndian)
            throws ImageServerException, FileNotFoundException {}

    public void setPlane(Pixels pixels, int theZ, int theC, int theT,
                        byte[] buf, boolean bigEndian)
            throws ImageServerException {}

    public void setPlane(Pixels pixels, int theZ, int theC, int theT, File file,
                        boolean bigEndian)
            throws ImageServerException, FileNotFoundException {}

    public void setROI(Pixels pixels, int x0, int y0, int z0, int c0, int t0,
                        int x1, int y1, int z1, int c1, int t1, byte[] buf,
                        boolean bigEndian)
            throws ImageServerException {}

    public void setROI(Pixels pixels, int x0, int y0, int z0, int c0, int t0,
                        int x1, int y1, int z1, int c1, int t1, File file,
                        boolean bigEndian)
            throws ImageServerException, FileNotFoundException {}

    public void finishPixels(Pixels pixels)
            throws ImageServerException {}

    public PlaneStatistics getPlaneStatistics(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public StackStatistics getStackStatistics(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public BufferedImage getComposite(Pixels pixels,
                                    CompositingSettings settings)
            throws ImageServerException
    {
        return null;
    }

    public void setThumbnail(Pixels pixels, CompositingSettings settings)
            throws ImageServerException {}

    public BufferedImage getThumbnail(Pixels pixels)
            throws ImageServerException
    {
        return null;
    }

    public BufferedImage getThumbnail(Pixels pixels, int width, int height)
            throws ImageServerException
    {
        return null;
    }

}
