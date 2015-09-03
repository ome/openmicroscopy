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
package org.openmicroscopy.shoola.env.data.views;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.Target;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataSaver;
import org.openmicroscopy.shoola.env.data.views.calls.Analyser;
import org.openmicroscopy.shoola.env.data.views.calls.EnumerationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ExportLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FigureCreator;
import org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesImporter;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.MovieCreator;
import org.openmicroscopy.shoola.env.data.views.calls.OverlaysRenderer;
import org.openmicroscopy.shoola.env.data.views.calls.PixelsDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.PlaneInfoLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ProjectionSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ROILoader;
import org.openmicroscopy.shoola.env.data.views.calls.ResultsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.SaveAsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ScriptRunner;
import org.openmicroscopy.shoola.env.data.views.calls.ScriptUploader;
import org.openmicroscopy.shoola.env.data.views.calls.ServerSideROILoader;
import org.openmicroscopy.shoola.env.data.views.calls.ROISaver;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingControlLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.TileLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

import omero.gateway.model.DataObject;
import omero.gateway.model.PixelsData;
import omero.gateway.model.ROIData;


/** 
 * Implementation of the {@link ImageDataView} interface.
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
class ImageDataViewImpl
    implements ImageDataView
{

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadRenderingControl(long, int, 
     * 											AgentEventListener)
     */
    public CallHandle loadRenderingControl(SecurityContext ctx, long pixelsID,
    		int index, AgentEventListener observer)
    {
    	int i = -1;
    	switch (index) {
    		default:
			case LOAD:
				i = RenderingControlLoader.LOAD;
				break;
			case RELOAD:
				i = RenderingControlLoader.RELOAD;
				break;
			case RESET:
				i = RenderingControlLoader.RESET;
		}
        BatchCallTree cmd = new RenderingControlLoader(ctx, pixelsID, i);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#render(SecurityContext, long, PlaneDef, boolean, int,
     * AgentEventListener)
     */
    public CallHandle render(SecurityContext ctx, long pixelsID, PlaneDef pd,
    		boolean largeImage, int compression, AgentEventListener observer)
    {
        BatchCallTree cmd = new ImageRenderer(ctx, pixelsID, pd,
        		largeImage, compression);
        return cmd.exec(observer);
    }

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPixels(long, AgentEventListener)
     */
	public CallHandle loadPixels(SecurityContext ctx, long pixelsID,
			AgentEventListener observer) 
	{
		BatchCallTree cmd = new PixelsDataLoader(ctx, pixelsID,
									PixelsDataLoader.SET);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#analyseShapes(PixelsData, Collection, List, 
     * 									AgentEventListener)
     */
	public CallHandle analyseShapes(SecurityContext ctx, PixelsData pixels,
			Collection channels, List shapes, AgentEventListener observer)
	{
		BatchCallTree cmd = new Analyser(ctx, pixels, channels, shapes);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#getRenderingSettings(long, AgentEventListener)
     */
	public CallHandle getRenderingSettings(SecurityContext ctx, long pixelsID, 
										AgentEventListener observer)
	{
		return getRenderingSettings(ctx, pixelsID, -1, observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#getRenderingSettings(long, long, AgentEventListener)
     */
	public CallHandle getRenderingSettings(SecurityContext ctx,
			long pixelsID, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsLoader(ctx, pixelsID, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#renderProjected(long, int, int, int, int, List, 
     *                       AgentEventListener)
     */
	public CallHandle renderProjected(SecurityContext ctx,
		long pixelsID, int startZ, int endZ, int stepping, int algorithm,
		List<Integer> channels, AgentEventListener observer)
    {
		BatchCallTree cmd = new ProjectionSaver(ctx, pixelsID, startZ, endZ,
				                  stepping, algorithm, channels);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#projectImage(ProjectionParam, AgentEventListener)
     */
	public CallHandle projectImage(SecurityContext ctx, ProjectionParam ref,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ProjectionSaver(ctx, ref);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createRndSetting(long, RndProxyDef, List, 
     * 										AgentEventListener)
     */
	public CallHandle createRndSetting(SecurityContext ctx, long pixelsID,
		RndProxyDef rndToCopy, List<Integer> indexes,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, pixelsID, rndToCopy,
									indexes);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle loadAcquisitionData(SecurityContext ctx, Object refObject,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataLoader(ctx, refObject);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadInstrumentData(long, AgentEventListener)
     */
	public CallHandle loadInstrumentData(SecurityContext ctx, long instrumentID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataLoader(ctx, 
				AcquisitionDataLoader.INSTRUMENT, instrumentID);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle saveAcquisitionData(SecurityContext ctx, Object refObject,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataSaver(ctx, refObject);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPlaneInfo(long, int, int, int, AgentEventListener)
     */
	public CallHandle loadPlaneInfo(SecurityContext ctx, long pixelsID, int z,
			int t, int channel, AgentEventListener observer)
	{
		BatchCallTree cmd = new PlaneInfoLoader(ctx, pixelsID, z, t, channel);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadChannelMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadChannelMetadataEnumerations(SecurityContext ctx,
		AgentEventListener observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(ctx, 
				EnumerationLoader.CHANNEL);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadImageMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadImageMetadataEnumerations(SecurityContext ctx, 
			AgentEventListener observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(ctx, EnumerationLoader.IMAGE);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#importImages(long, long, AgentEventListener)
     */
	public CallHandle importFiles(ImportableObject context, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesImporter(context);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#monitorDirectory(File, DataObject, long, long,
     * AgentEventListener)
     */
	public CallHandle monitorDirectory(SecurityContext ctx, File directory,
		DataObject container, long userID, long groupID,
		AgentEventListener observer)
	{
		return null;
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadImage(long, AgentEventListener)
     */
	public CallHandle loadImage(SecurityContext ctx, long imageID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(ctx, imageID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createMovie(long, long, List, MovieExportParam, 
     * 	AgentEventListener)
     */
	public CallHandle createMovie(SecurityContext ctx, long imageID,
			long pixelsID, List<Integer> channels, MovieExportParam param, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new MovieCreator(ctx, imageID, pixelsID, channels,
				param);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createFigure(List, Class, Object, AgentEventListener)
     */
	public CallHandle createFigure(SecurityContext ctx, List<Long> ids,
			Class type, Object param, AgentEventListener observer)
	{
		BatchCallTree cmd = new FigureCreator(ctx, ids, type, param);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadROI(long, Long, long, AgentEventListener)
     */
	public CallHandle loadROI(SecurityContext ctx, long imageID,
			List<Long> fileID, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ROILoader(ctx, imageID, fileID, userID);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveROI(long, Long, long, AgentEventListener)
     */
	public CallHandle saveROI(SecurityContext ctx, long imageID, long userID,
			List<ROIData> roiList, AgentEventListener observer)
	{
		BatchCallTree cmd = new ROISaver(ctx, imageID, userID, roiList);
		return cmd.exec(observer);
	}
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#exportImageAsOMETiff(SecurityContext, long, File, Target,
     * AgentEventListener)
     */
	public CallHandle exportImageAsOMETiff(SecurityContext ctx, long imageID,
			File file, Target target, AgentEventListener observer)
	{
		BatchCallTree cmd = new ExportLoader(ctx, imageID, file,
				ExportLoader.EXPORT_AS_OMETIFF, target);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadROIFromServer(long, Long, AgentEventListener)
     */
	public CallHandle loadROIFromServer(SecurityContext ctx, long imageID,
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ServerSideROILoader(ctx, imageID, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#renderOverLays(long, PlaneDef, long, Map,
     * AgentEventListener)
     */
	public CallHandle renderOverLays(SecurityContext ctx, long pixelsID,
		PlaneDef pd, long tableID, Map<Long, Integer> overlays,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new OverlaysRenderer(ctx, pixelsID, pd, tableID,
				overlays);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#runScript(ScriptObject, AgentEventListener)
     */
	public CallHandle runScript(SecurityContext ctx, ScriptObject script,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ScriptRunner(ctx, script);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#uploadScript(ScriptObject, AgentEventListener)
     */
	public CallHandle uploadScript(SecurityContext ctx,ScriptObject script,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ScriptUploader(ctx, script);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveAs(SaveAsParam, AgentEventListener)
     */
	public CallHandle saveAs(SecurityContext ctx,SaveAsParam parameters,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new SaveAsLoader(ctx, parameters);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadTiles(long, PlaneDef, List, AgentEventListener)
     */
	public CallHandle loadTiles(SecurityContext ctx, long pixelsID,
		PlaneDef pDef, RenderingControl proxy, Collection<Tile> tiles,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new TileLoader(ctx, pixelsID, pDef, proxy, tiles);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#shutDownRenderingControl(long, AgentEventListener)
     */
	public CallHandle shutDownRenderingControl(SecurityContext ctx,
			long pixelsID, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingControlLoader(ctx, pixelsID,
				RenderingControlLoader.SHUTDOWN);
        return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see ImageDataView#saveResults(SecurityContext, ResultsObject, AgentEventListener)
	 */
	public CallHandle saveResults(SecurityContext ctx,
	        ResultsObject results, AgentEventListener observer)
	{
	    BatchCallTree cmd = new ResultsSaver(ctx, results);
	    return cmd.exec(observer);
	}

}
