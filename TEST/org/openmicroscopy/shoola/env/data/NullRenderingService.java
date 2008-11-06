/*
 * org.openmicroscopy.shoola.env.data.NullRenderingService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data;




//Java imports
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import omero.model.PixelsDimensions;
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.ImageData;
import pojos.PixelsData;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class NullRenderingService
    implements OmeroImageService
{

    /**
     * No-op implementation
     * @see OmeroImageService#loadRenderingControl(long)
     */
    public RenderingControl loadRenderingControl(long pixelsID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroImageService#renderImage(long, PlaneDef)
     */
    public BufferedImage renderImage(long pixelsID, PlaneDef pd)
            throws RenderingServiceException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroImageService#shutDown(long)
     */
    public void shutDown(long pixelsID) {}

	/**
     * No-op implementation
     * @see OmeroImageService#loadPixelsDimensions(long)
     */
	public PixelsDimensions loadPixelsDimensions(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadPixels(long)
     */
	public PixelsData loadPixels(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getPlane(long, int, int, int)
     */
	public byte[] getPlane(long pixelsID, int z, int t, int c) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#pasteRenderingSettings(long, Class, List)
     */
	public Map pasteRenderingSettings(long pixelsID, Class rootNodeType, 
			List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#reloadRenderingService(long)
     */
	public RenderingControl reloadRenderingService(long pixelsID) 
		throws DSAccessException, RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#resetRenderingSettings(Class, Set)
     */
	public Map resetRenderingSettings(Class rootNodeType, List<Long> nodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getRenderingSettings(long)
     */
	public Map getRenderingSettings(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#resetRenderingService(long)
     */
	public RenderingControl resetRenderingService(long pixelsID) 
		throws DSAccessException, RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getThumbnail(long, int, int, long)
     */
	public BufferedImage getThumbnail(long pixelsID, int sizeX, int sizeY, 
									long userID)
		throws RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#setOriginalRenderingSettings(Class, List)
     */
	public Map setOriginalRenderingSettings(Class rootNodeType, 
											List<Long> nodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getThumbnailSet(List, int)
     */
	public Map<Long, BufferedImage> getThumbnailSet(List pixelsID, 
			                                        int maxLength) 
	    throws RenderingServiceException
	{
		return null;
	}
	
	/**
     * No-op implementation
     * @see OmeroImageService#renderProjected(long, int, int, int, int, List)
     */
	public BufferedImage renderProjected(long pixelsID, int startZ, int endZ, 
			int stepping, int type, List<Integer> channels) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#projectImage(ProjectionParam)
     */
	public ImageData projectImage(ProjectionParam ref) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#shutDownDataSink(long)
     */
	public void shutDownDataSink(long pixelsID) {}

	/**
     * No-op implementation
     * @see OmeroImageService#createRenderingSettings(long, RndProxyDef, List)
     */
	public Boolean createRenderingSettings(long pixelsID, RndProxyDef rndToCopy,
			List<Integer> indexes) 
		throws DSOutOfServiceException, DSAccessException
	{
		return Boolean.TRUE;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadAcquisitionData(Object)
     */
	public Object loadAcquisitionData(Object refObject) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#saveAcquisitionData(Object)
     */
	public Object saveAcquisitionData(Object refObject) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

}
