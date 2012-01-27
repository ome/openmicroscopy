/*
 * org.openmicroscopy.shoola.env.data.views.ImageDataView
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
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

import pojos.DataObject;
import pojos.PixelsData;
import pojos.ROIData;

/** 
 * Provides methods to support image viewing and analyzing.
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
public interface ImageDataView
    extends DataServicesView
{

	/** Indicates to load the rendering engine. */
	public static final int LOAD = 0;
	
	/** Indicates to reload the rendering engine. */
	public static final int RELOAD = 1;
	
	/** Indicates to reload the rendering engine. */
	public static final int RESET = 2;
    
    /**
     * Loads the rendering proxy associated to the pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param index		One of the constants defined by this class.
     * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadRenderingControl(long pixelsID, int index,
                        AgentEventListener observer);
    
    /**
     * Shuts down the rendering proxy associated to the pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle shutDownRenderingControl(long pixelsID,
                        AgentEventListener observer);
    
    /**
     * Renders the specified plane.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param pd        The plane to render.
     * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param largeImae Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
     * @param observer  Call-back handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle render(long pixelsID, PlaneDef pd, boolean asTexture,
    		boolean largeImage, AgentEventListener observer);
    
    /**
     * Retrieves the pixels set.
     * 
     * @param pixelsID	The id of the pixels set.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle loadPixels(long pixelsID, 
    					AgentEventListener observer);
    
    /**
     * Retrieves the dimensions in microns of the pixels set.
     * 
     * @param pixels	The pixels set to analyze.
     * @param channels	Collection of active channels. 
     * 					Mustn't be <code>null</code>.
     * @param shapes	Collection of shapes to analyze. 
     * 					Mustn't be <code>null</code>.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle analyseShapes(PixelsData pixels, List channels, 
    							List shapes, AgentEventListener observer);
    
    /**
     * Retrieves all the rendering settings associated to a given set of pixels.
     * 
     * @param pixelsID The id of the pixels set.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle getRenderingSettings(long pixelsID, 
    									AgentEventListener observer);
    
    /**
     * Retrieves all the rendering settings associated to a given set of pixels.
     * for the specified user.
     * 
     * @param pixelsID 	The id of the pixels set.
     * @param userID	The if of the users.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle getRenderingSettings(long pixelsID, long userID,
    									AgentEventListener observer);
    
    /**
     * Projects a section of the stack and returns the projected image.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param startZ    The first optical section.
     * @param endZ      The last optical section.
     * @param stepping  Stepping used while projecting. 
     *                  Default value is <code>1</code>
     * @param algorithm The type of projection.
     * @param channels 	The collection of channels to project.
     * @param openGLSupport Pass <code>true</code> if openGL is supported,
     * 						<code>false</code> otherwise.
     * @param observer 	Call-back handler.
     * @return See above.
     */
    public CallHandle renderProjected(long pixelsID, int startZ, int endZ,
    		int stepping, int algorithm, List<Integer> channels,
    		boolean openGLSupport, AgentEventListener observer);
    
    /**
     * Projects a section of the stack and returns the projected image.
     * 
     * @param ref 		The object hosting the projection's parameters.
     * @param observer 	Call-back handler.
     * @return See above.
     */
    public CallHandle projectImage(ProjectionParam ref, 
    							AgentEventListener observer);

    /**
     * Creates rendering setting for the specified pixels set and
     * copies the settings from the passed rendering setting object if
     * not <code>null</code>.
     * 
     * @param pixelsID	The id of the pixels set to handle.
     * @param rndToCopy The rendering settings to copy to the newly created one.
     * @param indexes	Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
     * @param observer 	Call-back handler.
     * @return See above.
     */
    public CallHandle createRndSetting(long pixelsID, RndProxyDef rndToCopy,
    		List<Integer> indexes, AgentEventListener observer);
    
    /**
     * Loads the acquisition metadata for an image or a given channel.
     * 
     * @param refObject Either an image or a channel.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle loadAcquisitionData(Object refObject, 
    		AgentEventListener observer);
    
    /**
     * Saves the acquisition metadata related to an image or a given channel.
     * 
     * @param refObject Object hosting the metadata for either an image or
     * 					a channel.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle saveAcquisitionData(Object refObject, 
    		AgentEventListener observer);

    /**
     * Loads the plane info objects related to the passed pixels set.
     * 
     * @param pixelsID 	The id of the pixels set.
     * @param z 		The selected z-section or <code>-1</code>.
     * @param t 		The selected timepoint or <code>-1</code>.
     * @param channel 	The selected timepoint or <code>-1</code>.
     * @param observer	Call-back handler.
     * @return See above.
     */
    public CallHandle loadPlaneInfo(long pixelsID, int z, int t, int channel, 
    		AgentEventListener observer);
    
    /**
     * Loads the enumerations used for the image metadata.
     * 
     * @param observer	Call-back handler.
     * @return See above.
     */
	public CallHandle loadImageMetadataEnumerations(AgentEventListener observer);
    
	/**
     * Loads the enumerations used for the channel metadata.
     * 
     * @param observer	Call-back handler.
     * @return See above.
     */
	public CallHandle loadChannelMetadataEnumerations(
								AgentEventListener observer);
	
	/**
	 * Imports the collection of images into the specified container.
	 *  
	 * @param context   The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle importFiles(ImportableObject context, long userID, 
			long groupID, AgentEventListener observer);

	/**
	 * Monitors the passed directory.
	 * 
	 * @param directory	The directory to monitor.
	 * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle monitorDirectory(File directory, DataObject container, 
			long userID, long groupID, AgentEventListener observer);
	
	/**
	 * Loads the specified image.
	 * 
	 * @param imageID 	The id of the image to load.
	 * @param userID	The id of the user.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle loadImage(long imageID, long userID, 
			AgentEventListener observer);
	
	/**
	 * Creates a movie.
	 * 
	 * @param imageID 	The id of the image.
	 * @param pixelsID 	The id of the pixels set.	
	 * @param channels 	The channels to map.
	 * @param param 	The parameters to create the movie.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle createMovie(long imageID, long pixelsID, 
			List<Integer> channels, MovieExportParam param, 
			AgentEventListener observer);

	/**
	 * Creates a figure.
	 * 
	 * @param ids 	The id of the objects.
	 * @param type	The type of objects to handle.
     * @param param The parameters to create the movie.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle createFigure(List<Long> ids, Class type, Object param, 
			AgentEventListener observer);
	
	/**
	 * Loads the instrument and its components.
	 * 
	 * @param instrumentID   The id of instrument
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle loadInstrumentData(long instrumentID, 
			AgentEventListener observer);

	/**
	 * Loads the ROI.
	 * 
	 * @param imageID 	The image's id.
	 * @param fileID	The id of the file.
	 * @param userID	The user's id.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle loadROI(long imageID, List<Long> fileID, long userID, 
			AgentEventListener observer);
	/**
	 * Save the ROI for the image to the server..
	 * 
	 * @param imageID 	The image's ID.
	 * @param userID	The user's ID.
	 * @param roiList	The list of ROI to save.
	 */
	public CallHandle saveROI(long imageID,  long userID, List<ROIData> roiList,
			AgentEventListener observer);

	/**
	 * Exports the image as an XML file.
	 * 
	 * @param imageID	The image's id.
	 * @param file		The file where to export the image.
	 * @param observer 	Call-back handler.
	 * @return See above.
	 */
	public CallHandle exportImageAsOMETiff(long imageID, File file,
			AgentEventListener observer);

	/**
	 * Loads the ROI if possible from the server.
	 * 
	 * @param imageID 	The image's id.
	 * @param userID	The user's id.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle loadROIFromServer(long imageID, long userID,
			AgentEventListener serverSideROILoader);
	
	/**
	 * Renders the image with the overlays if the passed map is not 
	 * <code>null</code>, renders the image without the overlays if 
	 * <code>null</code>.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 * @param pd		The plane to render.
	 * @param tableID	The id of the table hosting the mask.
	 * @param overlays	The overlays to render or <code>null</code>.
	 * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle renderOverLays(long pixelsID, PlaneDef pd, long tableID,
			Map<Long, Integer> overlays, boolean asTexture,
			AgentEventListener observer);
	
	/**
	 * Performs a basic FRAP.
	 * 
	 * @param ids	The objects to analyze.
	 * @param type 	The type of object to analyze.
	 * @param param	The extra parameters.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle analyseFRAP(List<Long> ids, Class objectType, 
			Object param, AgentEventListener observer);
	
	/**
	 * Runs the passed script.
	 * 
	 * @param script The script to run.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle runScript(ScriptObject script, 
			AgentEventListener observer);

	/**
	 * Uploads the passed script.
	 * 
	 * @param script The script to upload.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle uploadScript(ScriptObject script,
			AgentEventListener observer);

	/**
	 * Retrieve the users workflows.
	 * 
	 * @param userID id of the user whose workflows are to be retrieved.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle retrieveWorkflows(long userID, AgentEventListener observer);
	
	/**
	 * Stores the newly created  workflows.
	 * 
	 * @param workflows The new workflows.
	 * @param userID id of the user whose workflows are to be retrieved.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle storeWorkflows(List<WorkflowData> workflows, long userID, 
			AgentEventListener observer);
	
	/**
	 * Saves the images in the specified folder as JPEG by default.
	 * 
	 * @param parameters The parameters used to save locally the images.
	 *.
	 * @param observer	Call-back handler.
	 * @return See above.
	 */
	public CallHandle saveAs(SaveAsParam parameters,
			AgentEventListener observer);

	/**
	 * Loads the tiles.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 * @param pDef		The plane to render.
	 * @param tiles		The tiles.
	 * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param observer Call-back handler.
	 * @return See above.
	 */
	public CallHandle loadTiles(long pixelsID, PlaneDef pDef, 
			Collection<Tile> tiles, boolean asTexture,
			AgentEventListener observer);
	
}
