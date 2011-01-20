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
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import com.sun.opengl.util.texture.TextureData;

import pojos.DataObject;
import pojos.FileAnnotationData;
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
     * @see OmeroImageService#renderImage(long, PlaneDef, boolean, boolean)
     */
    public Object renderImage(long pixelsID, PlaneDef pd, boolean asTexture,
    		boolean largeImage)
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
     * @see OmeroImageService#getRenderingSettings(long, long)
     */
	public Map getRenderingSettings(long pixelsID, long userID) 
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
     * @see OmeroImageService#loadPlaneInfo(long, int, int, int)
     */
	public Collection loadPlaneInfo(long pixelsID, int z, int t, int channel) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getSupportedFileFilters()
     */
	public List<FileFilter> getSupportedFileFilters()
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#importImage(DataObject, File, StatusLabel, long, 
     * long, boolean, String)
     */
	public Object importImage(DataObject container, File image, 
			StatusLabel status, long userID, long groupID, boolean archived,
			String name) 
		throws ImportException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getFSFileSystemView()
     */
	public FileSystemView getFSFileSystemView() { return null; }

	public Object monitor(String path, DataObject container, 
			long userID, long groupID)
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#createMovie(long, List, MovieExportParam)
     */
	public FileAnnotationData createMovie(long imageID, long pixelsID,
			List<Integer> channels, MovieExportParam param) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#analyseFretFit(long, long, long)
     */
	public DataObject analyseFretFit(long controlID, long toAnalyzeID,
			long irfID) throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadROI(long, List, long)
     */
	public List<ROIResult> loadROI(long imageID, List<Long>fileID, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#exportImageAsOMETiff(long, File)
     */
	public Object exportImageAsOMETiff(long imageID, File folder)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#renderProjectedAsTexture(long, int, int, int, int, 
     * List)
     */
	public TextureData renderProjectedAsTexture(long pixelsID, int startZ,
			int endZ, int stepping, int type, List<Integer> channels)
			throws RenderingServiceException, DSOutOfServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getRenderingSettingsFor(long, long)
     */
	public List getRenderingSettingsFor(long pixelsID, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#importImage(DataObject, File, StatusLabel, long, 
     * long, boolean, String, int)
     */
	public Object importImage(DataObject container, File file,
			StatusLabel status, long userID, long groupID, boolean archived,
			String name, int depth) throws ImportException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#renderOverLays(long, PlaneDef, long, Map, boolean)
     */
	public Object renderOverLays(long pixelsID, PlaneDef pd, long tableID,
			Map<Long, Integer> overlays, boolean asTexture)
			throws RenderingServiceException
	{
		return null;
	}

}
