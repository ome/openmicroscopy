/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import omero.api.RawPixelsStorePrx;
import omero.api.ThumbnailStorePrx;
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import omero.gateway.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.Target;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.ROIData;
import omero.gateway.model.WorkflowData;


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
 * @since OME2.2
 */
public class NullRenderingService
    implements OmeroImageService
{

    /**
     * No-op implementation
     * @see OmeroImageService#loadRenderingControl(long)
     */
    public RenderingControl loadRenderingControl(SecurityContext ctx,
    		long pixelsID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroImageService#renderImage(SecurityContext, long, PlaneDef, boolean, int)
     */
    public Object renderImage(SecurityContext ctx, long pixelsID, PlaneDef pd,
    		boolean largeImage, int compression)
            throws RenderingServiceException
    {
        return null;
    }

    public boolean isAlive(SecurityContext ctx) { return false; }

    /**
     * No-op implementation
     * @see OmeroImageService#shutDown(long)
     */
    public void shutDown(SecurityContext ctx, long pixelsID) {}
    
	/**
     * No-op implementation
     * @see OmeroImageService#loadPixels(long)
     */
	public PixelsData loadPixels(SecurityContext ctx, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getPlane(long, int, int, int)
     */
	public byte[] getPlane(SecurityContext ctx, long pixelsID, int z, int t,
			int c) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#pasteRenderingSettings(long, Class, List)
     */
	public Map pasteRenderingSettings(SecurityContext ctx, 
			long pixelsID, Class rootNodeType, List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#reloadRenderingService(long)
     */
	public RenderingControl reloadRenderingService(SecurityContext ctx,
			long pixelsID)
		throws DSAccessException, RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#resetRenderingSettings(Class, Set)
     */
	public Map resetRenderingSettings(SecurityContext ctx, Class rootNodeType,
			List<Long> nodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getRenderingSettings(long, long)
     */
	public Map getRenderingSettings(SecurityContext ctx, long pixelsID,
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#resetRenderingService(long)
     */
	public RenderingControl resetRenderingService(SecurityContext ctx,
			long pixelsID)
		throws DSAccessException, RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getThumbnail(long, int, int, long)
     */
	public BufferedImage getThumbnail(SecurityContext ctx, long pixelsID,
			int sizeX, int sizeY, long userID)
		throws RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#setMinMaxSettings(Class, List)
     */
	public Map setMinMaxSettings(SecurityContext ctx, Class rootNodeType,
											List<Long> nodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getThumbnailSet(List, int)
     */
	public Map<Long, BufferedImage> getThumbnailSet(SecurityContext ctx,
			Collection<Long> pixelsID, int maxLength) 
	    throws RenderingServiceException
	{
		return null;
	}
	
	/**
     * No-op implementation
     * @see OmeroImageService#renderProjected(long, int, int, int, int, List)
     */
	public BufferedImage renderProjected(SecurityContext ctx, long pixelsID,
			int startZ, int endZ, int stepping, int type,
			List<Integer> channels) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#projectImage(ProjectionParam)
     */
	public ImageData projectImage(SecurityContext ctx, ProjectionParam ref)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#shutDownDataSink(long)
     */
	public void shutDownDataSink(SecurityContext ctx, long pixelsID) {}

	/**
     * No-op implementation
     * @see OmeroImageService#createRenderingSettings(long, RndProxyDef, List)
     */
	public Boolean createRenderingSettings(SecurityContext ctx, long pixelsID,
			RndProxyDef rndToCopy, List<Integer> indexes) 
		throws DSOutOfServiceException, DSAccessException
	{
		return Boolean.valueOf(true);
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadPlaneInfo(long, int, int, int)
     */
	public Collection loadPlaneInfo(SecurityContext ctx, long pixelsID, int z,
			int t, int channel)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getSupportedFileFormats()
     */
	public FileFilter[] getSupportedFileFormats()
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#importImage(ImportableObject, ImportableFile, 
     * long, long, boolean)
     */
	public Object importFile(ImportableObject object,
			ImportableFile file, boolean close) 
		throws ImportException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getFSFileSystemView()
     */
	public FileSystemView getFSFileSystemView(SecurityContext ctx)
	{
		return null;
	}

	public Object monitor(SecurityContext ctx, String path, DataObject container,
			long userID, long groupID)
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#createMovie(long, List, MovieExportParam)
     */
	public ScriptCallback createMovie(SecurityContext ctx, long imageID,
			long pixelsID, List<Integer> channels, MovieExportParam param)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}
	
	/**
     * No-op implementation
     * @see OmeroImageService#loadROI(long, List, long)
     */
	public List<ROIResult> loadROI(SecurityContext ctx, long imageID,
			List<Long>fileID, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getRenderingSettingsFor(long, long)
     */
	public List getRenderingSettingsFor(SecurityContext ctx, long pixelsID,
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#createFigure(List, Class, Object)
     */
	public ScriptCallback createFigure(SecurityContext ctx, 
		List<Long> ids, Class type, Object parameters)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#saveROI(long, long, List)
     */
	public List<ROIData> saveROI(SecurityContext ctx, long imageID, long userID,
		List<ROIData> list)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadROIFromServer(long, long)
     */
	public List<ROIResult> loadROIFromServer(SecurityContext ctx, long imageID,
			long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#renderOverLays(SecurityContext, long, PlaneDef, long, Map)
     */
	public Object renderOverLays(SecurityContext ctx, long pixelsID,
			PlaneDef pd, long tableID, Map<Long, Integer> overlays)
			throws RenderingServiceException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#runScript(ScriptObject)
     */
	public ScriptCallback runScript(SecurityContext ctx, ScriptObject script)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getScriptsAsString()
     */
	public Map<Long, String> getScriptsAsString(SecurityContext ctx)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadROIMeasurements(Class, long, long)
     */
	public Collection loadROIMeasurements(SecurityContext ctx, Class type,
			long id, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadAvailableScripts(long)
     */
	public List<ScriptObject> loadAvailableScripts(SecurityContext ctx,
			long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#loadAvailableScriptsWithUI()
     */
	public List<ScriptObject> loadAvailableScriptsWithUI(SecurityContext ctx)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}
	
	/**
     * No-op implementation
     * @see OmeroImageService#uploadScript(ScriptObject)
     */
	public Object uploadScript(SecurityContext ctx, ScriptObject script)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getFSThumbnailSet(List, int, long)
     */
	public Map<DataObject, BufferedImage> getFSThumbnailSet(SecurityContext ctx,
			List<DataObject> files,
			int maxLength, long userID) throws DSAccessException,
			DSOutOfServiceException, FSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#getExperimenterThumbnailSet(List, int)
     */
	public Map<DataObject, BufferedImage> getExperimenterThumbnailSet(
			SecurityContext ctx, List<DataObject> experimenters, int maxLength)
		throws DSAccessException, DSOutOfServiceException
	{
		return null;
	}
	
	/**
     * No-op implementation
     * @see OmeroImageService#loadScript(long)
     */
	public ScriptObject loadScript(SecurityContext ctx, long scriptID)
			throws ProcessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#setOwnerRenderingSettings(Class, List)
     */
	public Map setOwnerRenderingSettings(SecurityContext ctx,
		Class rootNodeType, List<Long> nodeIDs)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#retrieveWorkflows(long)
     */
	public List<WorkflowData> retrieveWorkflows(SecurityContext ctx,
			long userID)
			throws DSAccessException, DSOutOfServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroImageService#storeWorkflows(List, long)
     */
	public Object storeWorkflows(SecurityContext ctx, 
		List<WorkflowData> workflows, long userID)
			throws DSAccessException, DSOutOfServiceException
	{
		return null;
	}

	public ScriptCallback saveAs(SecurityContext ctx,
			SaveAsParam param) throws DSAccessException,
			DSOutOfServiceException
	{
		return null;
	}

	public Boolean isLargeImage(SecurityContext ctx, long pixelsId)
		throws DSAccessException,
			DSOutOfServiceException {
		return null;
	}

	public Object exportImageAsOMEFormat(SecurityContext ctx, 
			int index, long imageID, File folder,
			Target target) throws DSOutOfServiceException, DSAccessException {
		return null;
	}
	
	public Set<DataObject> getFileSet(SecurityContext ctx, long imageId)
	throws DSAccessException, DSOutOfServiceException
	{
		return null;
	}

	public ThumbnailStorePrx createThumbnailStore(SecurityContext ctx)
			throws DSAccessException, DSOutOfServiceException {
		return null;
	}

	@Override
	public Long getRenderingDef(SecurityContext ctx, long pixelsID, long userID)
			throws DSOutOfServiceException, DSAccessException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public RndProxyDef getSettings(SecurityContext ctx, long rndID)
            throws DSOutOfServiceException, DSAccessException {
        // TODO Auto-generated method stub
        return null;
    }

    public RawPixelsStorePrx createPixelsStore(SecurityContext ctx)
            throws DSAccessException, DSOutOfServiceException {
        return null;
    }
}
