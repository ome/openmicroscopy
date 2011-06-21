/*
 * org.openmicroscopy.shoola.env.data.views.ImViewerViewImpl
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

package org.openmicroscopy.shoola.env.data.views;



//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import pojos.WorkflowData;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataSaver;
import org.openmicroscopy.shoola.env.data.views.calls.Analyser;
import org.openmicroscopy.shoola.env.data.views.calls.EnumerationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ExportLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FRAPAnalyser;
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
import org.openmicroscopy.shoola.env.data.views.calls.SaveAsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ScriptRunner;
import org.openmicroscopy.shoola.env.data.views.calls.ScriptUploader;
import org.openmicroscopy.shoola.env.data.views.calls.ServerSideROILoader;
import org.openmicroscopy.shoola.env.data.views.calls.ROISaver;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingControlLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.TileLoader;
import org.openmicroscopy.shoola.env.data.views.calls.WorkflowHandler;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

import pojos.DataObject;
import pojos.PixelsData;
import pojos.ROIData;


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
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
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
    public CallHandle loadRenderingControl(long pixelsID, int index,
                                        AgentEventListener observer)
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
        BatchCallTree cmd = new RenderingControlLoader(pixelsID, i);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#render(long, PlaneDef, boolean, boolean, 
     * AgentEventListener)
     */
    public CallHandle render(long pixelsID, PlaneDef pd, boolean asTexture, 
                        boolean largeImage, AgentEventListener observer)
    {
        BatchCallTree cmd = new ImageRenderer(pixelsID, pd, asTexture, 
        		largeImage);
        return cmd.exec(observer);
    }

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPixels(long, AgentEventListener)
     */
	public CallHandle loadPixels(long pixelsID, AgentEventListener observer) 
	{
		BatchCallTree cmd = new PixelsDataLoader(pixelsID, 
									PixelsDataLoader.SET);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#analyseShapes(PixelsData, List, List, 
     * 									AgentEventListener)
     */
	public CallHandle analyseShapes(PixelsData pixels, List channels, 
			List shapes, AgentEventListener observer)
	{
		BatchCallTree cmd = new Analyser(pixels, channels, shapes);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#getRenderingSettings(long, AgentEventListener)
     */
	public CallHandle getRenderingSettings(long pixelsID, 
										AgentEventListener observer)
	{
		return getRenderingSettings(pixelsID, -1, observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#getRenderingSettings(long, long, AgentEventListener)
     */
	public CallHandle getRenderingSettings(long pixelsID, long userID,
										AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsLoader(pixelsID, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#renderProjected(long, int, int, int, int, List, 
     *                       AgentEventListener)
     */
	public CallHandle renderProjected(long pixelsID, int startZ, int endZ, 
			int stepping, int algorithm, List<Integer> channels, 
			boolean openGLSupport, AgentEventListener observer)
    {
		BatchCallTree cmd = new ProjectionSaver(pixelsID, startZ, endZ, 
				                  stepping, algorithm, channels, openGLSupport);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#projectImage(ProjectionParam, AgentEventListener)
     */
	public CallHandle projectImage(ProjectionParam ref, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ProjectionSaver(ref);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createRndSetting(long, RndProxyDef, List, 
     * 										AgentEventListener)
     */
	public CallHandle createRndSetting(long pixelsID, RndProxyDef rndToCopy, 
			List<Integer> indexes, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(pixelsID, rndToCopy, 
									indexes);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle loadAcquisitionData(Object refObject, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataLoader(refObject);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadInstrumentData(long, AgentEventListener)
     */
	public CallHandle loadInstrumentData(long instrumentID, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataLoader(
				AcquisitionDataLoader.INSTRUMENT, instrumentID);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle saveAcquisitionData(Object refObject, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataSaver(refObject);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPlaneInfo(long, int, int, int, AgentEventListener)
     */
	public CallHandle loadPlaneInfo(long pixelsID, int z, int t, int channel,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new PlaneInfoLoader(pixelsID, z, t, channel);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadChannelMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadChannelMetadataEnumerations(AgentEventListener 
					observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(EnumerationLoader.CHANNEL);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadImageMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadImageMetadataEnumerations(AgentEventListener observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(EnumerationLoader.IMAGE);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#importImages(ImportContext, long, long, 
     * AgentEventListener)
     */
	public CallHandle importFiles(ImportableObject context, long userID, 
			long groupID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesImporter(context, userID, groupID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#monitorDirectory(File, DataObject, long, long,
     * AgentEventListener)
     */
	public CallHandle monitorDirectory(File directory, DataObject container, 
			long userID, long groupID, AgentEventListener observer)
	{
		return null;
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadImage(long, long, AgentEventListener)
     */
	public CallHandle loadImage(long imageID, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(imageID, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createMovie(long, long, List, MovieExportParam, 
     * 	AgentEventListener)
     */
	public CallHandle createMovie(long imageID, long pixelsID,
			List<Integer> channels, MovieExportParam param, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new MovieCreator(imageID, pixelsID, channels,
				param);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createFigure(List, Class, Object, AgentEventListener)
     */
	public CallHandle createFigure(List<Long> ids, Class type, Object param, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FigureCreator(ids, type, param);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadROI(long, Long, long, AgentEventListener)
     */
	public CallHandle loadROI(long imageID, List<Long> fileID, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ROILoader(imageID, fileID, userID);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveROI(long, Long, long, AgentEventListener)
     */
	public CallHandle saveROI(long imageID, long userID, List<ROIData> roiList,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ROISaver(imageID, userID, roiList);
		return cmd.exec(observer);
	}
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#exportImageAsOMETiff(long, File, AgentEventListener)
     */
	public CallHandle exportImageAsOMETiff(long imageID, File file,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ExportLoader(imageID, file, 
				ExportLoader.EXPORT_AS_OMETIFF);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadROIFromServer(long, Long, AgentEventListener)
     */
	public CallHandle loadROIFromServer(long imageID, long userID, 
								AgentEventListener observer)
	{
		BatchCallTree cmd = new ServerSideROILoader(imageID, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#renderOverLays(long, PlaneDef, long, Map, boolean, 
     * AgentEventListener)
     */
	public CallHandle renderOverLays(long pixelsID, PlaneDef pd, long tableID,
			Map<Long, Integer> overlays, boolean asTexture,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new OverlaysRenderer(pixelsID, pd, tableID, 
				overlays, asTexture);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#analyseFRAP(List, Class, Object, AgentEventListener)
     */
	public CallHandle analyseFRAP(List<Long> ids, Class objectType,
			Object param, AgentEventListener observer)
	{
		BatchCallTree cmd = new FRAPAnalyser(ids, objectType, param);
		return cmd.exec(observer);
	}
	

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#runScript(ScriptObject, AgentEventListener)
     */
	public CallHandle runScript(ScriptObject script, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ScriptRunner(script);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#uploadScript(ScriptObject, AgentEventListener)
     */
	public CallHandle uploadScript(ScriptObject script,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ScriptUploader(script);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#retrieveWorkflows(long, AgentEventListener)
     */
	public CallHandle retrieveWorkflows(long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new WorkflowHandler(userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#storeWorkflows(List, long, AgentEventListener)
     */
	public CallHandle storeWorkflows(List<WorkflowData> workflows, long userID, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new WorkflowHandler(workflows, userID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveAs(SaveAsParam, AgentEventListener)
     */
	public CallHandle saveAs(SaveAsParam parameters,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new SaveAsLoader(parameters);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadTiles(long, PlaneDef, List, boolean,
     * AgentEventListener)
     */
	public CallHandle loadTiles(long pixelsID, PlaneDef pDef,
			Collection<Tile> tiles, boolean asTexture,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new TileLoader(pixelsID, pDef, tiles, asTexture);
		return cmd.exec(observer);
	}

}
