/*
 * org.openmicroscopy.shoola.env.data.ImageServerPixelsService
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.shoola.env.data;

import java.awt.Image;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Map;
import java.util.HashMap;

import org.openmicroscopy.is.ImageServer;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.is.PixelsFileFormat;
import org.openmicroscopy.is.PlaneStatistics;
import org.openmicroscopy.is.StackStatistics;
import org.openmicroscopy.is.CompositingSettings;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.ds.st.Repository;

/**
 * Heyo -- this one talks to the image server like a stone-cold mofo
 */

public class ImageServerPixelsService
    implements PixelsService
{
    /**
     * Stores all of the previously seen image servers.  It is
     * possible, though currently unlikely, that there will be
     * multiple Repositories in the system, each pointing to a
     * different image server.  To handle this case, we maintain a
     * map, with Repository ID's for the keys, and ImageServer objects
     * for the values.
     */
    protected Map imageServers;

    /**
     * Retrieves or creates an {@link ImageServer} object for
     * retrieving pixels for the given Repository attribute.
     */

    protected ImageServer activateRepository(Repository rep)
    {
        Integer id = new Integer(rep.getID());
        ImageServer is = (ImageServer) imageServers.get(id);

        if (is == null)
        {
            is = ImageServer.getHTTPImageServer(rep.getImageServerURL());
            imageServers.put(id,is);
        }

        return is;
    }

    /**
     * Retrieves or creates an {@link ImageServer} object for
     * retrieving pixels for the given Pixels attribute.
     */

    protected ImageServer activatePixels(Pixels pix)
    {
        Repository rep = pix.getRepository();
        return activateRepository(rep);
    }

    public Pixels newPixels(int sizeX,
                            int sizeY,
                            int sizeZ,
                            int sizeC,
                            int sizeT,
                            int bytesPerPixel,
                            boolean isSigned,
                            boolean isFloat)
        throws ImageServerException
    { return null; }

    public Pixels newPixels(final PixelsFileFormat format)
        throws ImageServerException
    { return null; }

    public PixelsFileFormat getPixelsInfo(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPixelsInfo(pixels.getPixelsID());
    }

    public String getPixelsSHA1(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPixelsSHA1(pixels.getPixelsID());
    }

    public String getPixelsServerPath(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPixelsServerPath(pixels.getPixelsID());
    }

    public boolean isPixelsFinished(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.isPixelsFinished(pixels.getPixelsID());
    }

    public byte[] getPixels(Pixels pixels,
                            boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPixels(pixels.getPixelsID(),bigEndian);
    }

    public byte[] getStack(Pixels pixels,
                           int theC, int theT,
                           boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getStack(pixels.getPixelsID(),theC,theT,bigEndian);
    }

    public byte[] getPlane(Pixels pixels,
                           int theZ, int theC, int theT,
                           boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPlane(pixels.getPixelsID(),theZ,theC,theT,bigEndian);
    }

    public byte[] getROI(Pixels pixels,
                         int x0,int y0,int z0,int c0,int t0,
                         int x1,int y1,int z1,int c1,int t1,
                         boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getROI(pixels.getPixelsID(),
                         x0,y0,z0,c0,t0,x1,y1,z1,c1,t1,
                         bigEndian);
    }

    public void setPixels(Pixels pixels,
                          byte[] buf,
                          boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.setPixels(pixels.getPixelsID(),buf,bigEndian);
    }

    public void setPixels(Pixels pixels,
                          File file,
                          boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        ImageServer is = activatePixels(pixels);
        is.setPixels(pixels.getPixelsID(),file,bigEndian);
    }

    public void setStack(Pixels pixels,
                         int theC, int theT,
                         byte[] buf,
                         boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.setStack(pixels.getPixelsID(),theC,theT,buf,bigEndian);
    }

    public void setStack(Pixels pixels,
                         int theC, int theT,
                         File file,
                         boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        ImageServer is = activatePixels(pixels);
        is.setStack(pixels.getPixelsID(),theC,theT,file,bigEndian);
    }

    public void setPlane(Pixels pixels,
                         int theZ, int theC, int theT,
                         byte[] buf,
                         boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.setPlane(pixels.getPixelsID(),theZ,theC,theT,buf,bigEndian);
    }

    public void setPlane(Pixels pixels,
                         int theZ, int theC, int theT,
                         File file,
                         boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        ImageServer is = activatePixels(pixels);
        is.setPlane(pixels.getPixelsID(),theZ,theC,theT,file,bigEndian);
    }

    public void setROI(Pixels pixels,
                       int x0,int y0,int z0,int c0,int t0,
                       int x1,int y1,int z1,int c1,int t1,
                       byte[] buf,
                       boolean bigEndian)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.setROI(pixels.getPixelsID(),
                  x0,y0,z0,c0,t0,x1,y1,z1,c1,t1,
                  buf,bigEndian);
    }

    public void setROI(Pixels pixels,
                       int x0,int y0,int z0,int c0,int t0,
                       int x1,int y1,int z1,int c1,int t1,
                       File file,
                       boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        ImageServer is = activatePixels(pixels);
        is.setROI(pixels.getPixelsID(),
                  x0,y0,z0,c0,t0,x1,y1,z1,c1,t1,
                  file,bigEndian);
    }

    public void finishPixels(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.finishPixels(pixels.getPixelsID());
    }

    public PlaneStatistics getPlaneStatistics(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getPlaneStatistics(pixels.getPixelsID());
    }

    public StackStatistics getStackStatistics(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getStackStatistics(pixels.getPixelsID());
    }

    public Image getComposite(Pixels pixels,
                              CompositingSettings settings)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getComposite(pixels.getPixelsID(),settings);
    }

    public void setThumbnail(Pixels pixels,
                             CompositingSettings settings)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        is.setThumbnail(pixels.getPixelsID(),settings);
    }

    public Image getThumbnail(Pixels pixels)
        throws ImageServerException
    {
        ImageServer is = activatePixels(pixels);
        return is.getThumbnail(pixels.getPixelsID());
    }
}