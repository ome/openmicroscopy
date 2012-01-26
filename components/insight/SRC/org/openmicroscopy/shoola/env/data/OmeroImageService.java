/*
 * org.openmicroscopy.shoola.env.data.OmeroImageService
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
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies
import omero.constants.projection.ProjectionType;
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.DataObject;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ROIData;
import pojos.WorkflowData;

/** 
 * List of methods to view images or thumbnails.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface OmeroImageService
{
  
	/** The maximum number of plane info objects.*/
	public static final int    MAX_PLANE_INFO = 6000;
	
	/** The extension causing a problem in bf lib. TMP solution. */
	public static final String ZIP_EXTENSION = ".zip";
	
	/** Identifies the <code>Maximum intensity</code> projection. */
	public static final int	MAX_INTENSITY = 
									ProjectionType.MAXIMUMINTENSITY.ordinal();
	
	/** Identifies the <code>Mean intensity</code> projection. */
	public static final int	MEAN_INTENSITY = 
								ProjectionType.MEANINTENSITY.ordinal();
	
	/** Identifies the <code>Sum intensity</code> projection. */
	public static final int	SUM_INTENSITY = ProjectionType.SUMINTENSITY.ordinal();
	
	/** Identifies the type used to store pixel values. */
	public static final String INT_8 = "int8";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_8 = "uint8";

	/** Identifies the type used to store pixel values. */
	public static final String INT_16 = "int16";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_16 = "uint16";

	/** Identifies the type used to store pixel values. */
	public static final String INT_32 = "int32";

	/** Identifies the type used to store pixel values. */
	public static final String UINT_32 = "uint32";

	/** Identifies the type used to store pixel values. */
	public static final String FLOAT = "float";

	/** Identifies the type used to store pixel values. */
	public static final String DOUBLE = "double";
	
	/**
	 * Initializes a {@link RenderingControl} proxy for the specified pixels
	 * set.
	 * 
	 * @param pixelsID The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException       If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public RenderingControl loadRenderingControl(long pixelsID)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Renders the specified 2D-plane. 
	 * 
	 * @param pixelsID  The ID of the pixels set.
	 * @param pd        The plane to render.
	 * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param largeImage Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
	 * @return The image representing the plane.
	 * @throws RenderingServiceException If the server cannot render the image.
	 */
	public Object renderImage(long pixelsID, PlaneDef pd, boolean asTexture,
			boolean largeImage)
		throws RenderingServiceException;

	/**
	 * Shuts downs the rendering service attached to the specified 
	 * pixels set.
	 *
	 * @param pixelsID  The ID of the pixels set.
	 */
	public void shutDown(long pixelsID);

	/**
	 * Returns a thumbnail of the currently selected 2D-plane for the
	 * passed pixels set.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param sizeX     The width of the thumbnail.
	 * @param sizeY     The height of the thumnail.
	 * @param userID	The id of the user the thumbnail is for.
	 * @return See above.
	 * @throws RenderingServiceException    If the server is out of service.
	 */
	public BufferedImage getThumbnail(long pixelsID, int sizeX, int sizeY, long
										userID)
		throws RenderingServiceException;

	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * pixels set.
	 * 
	 * @param pixelsID	The collection of pixels set.
	 * @param maxLength The maximum length of a thumbnail.
	 * @return See above.
	 * @throws RenderingServiceException  If the server is out of service.
	 */
	public Map<Long, BufferedImage> getThumbnailSet(List pixelsID, 
			                                        int maxLength)
		throws RenderingServiceException;
	
	/**
	 * Reloads the rendering engine for the passed set of pixels.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws RenderingServiceException If the rendering engine cannot be 
	 * 									 started.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public RenderingControl reloadRenderingService(long pixelsID)
		throws DSAccessException, RenderingServiceException;
	
	/**
	 * Reloads the rendering engine for the passed set of pixels.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws RenderingServiceException If the rendering engine cannot be 
	 * 									 started.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public RenderingControl resetRenderingService(long pixelsID)
		throws DSAccessException, RenderingServiceException;

	/**
	 * Loads the pixels set.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public PixelsData loadPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the XY-plane identified by the passed z-section, time-point 
	 * and wavelength.
	 * 
	 * @param pixelsID 	The id of pixels containing the requested plane.
	 * @param z			The selected z-section.
	 * @param t			The selected time-point.
	 * @param c			The selected wavelength.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException        If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets
	 * if the rootType is <code>DatasetData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodeIDs		The id of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map pasteRenderingSettings(long pixelsID, Class rootNodeType,
			List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param nodeIDs		The id of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map resetRenderingSettings(Class rootNodeType, List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Sets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodeIDs		The id of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map setMinMaxSettings(Class rootNodeType, 
											List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Sets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodeIDs		The id of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map setOwnerRenderingSettings(Class rootNodeType, 
											List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the rendering setting related to a given set of pixels.
	 * Returns a Map whose keys are the experimenter who created the rendering 
	 * settings and the value the settings itself.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 * @param userID	The id of the user currently logged in.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map getRenderingSettings(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the rendering setting related to a given set of pixels
	 * for the specified user.
	 *  
	 * @param pixelsID The id of the pixels set.
	 * @param userID	The id of the user currently logged in.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<RndProxyDef> getRenderingSettingsFor(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates a preview projected image 
	 * 
	 * @param pixelsID  The ID of the pixels set.
	 * @param startZ    The first optical section.
	 * @param endZ      The last optical section.
	 * @param stepping  The stepping used during the projection.
	 * @param type      The type of projection.
     * @param channels The collection of channels to project.
	 * @return The buffered image representing the projected image.
	 * @throws RenderingServiceException If the server cannot render the image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 */
	public BufferedImage renderProjected(long pixelsID, int startZ, int endZ, 
			 int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException; 
	
	/**
	 * Creates a preview projected image 
	 * 
	 * @param pixelsID  The ID of the pixels set.
	 * @param startZ    The first optical section.
	 * @param endZ      The last optical section.
	 * @param stepping  The stepping used during the projection.
	 * @param type      The type of projection.
     * @param channels The collection of channels to project.
	 * @return The buffered image representing the projected image.
	 * @throws RenderingServiceException If the server cannot render the image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 */
	public TextureData renderProjectedAsTexture(long pixelsID, int startZ, 
			int endZ, int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException; 
	
	/**
	 * Projects the specified set of pixels according to the projection's 
	 * parameters. Adds the created image to the passed dataset.
	 * 
	 * @param ref The object hosting the projection's parameters.
	 * @return The newly created image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public ImageData projectImage(ProjectionParam ref)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates rendering setting for the specified pixels set and
     * copies the settings from the passed rendering setting object if
     * not <code>null</code>. Returns <code>true</code> if the rendering
     * settings have been successfully created and updated, <code>false</code>
     * otherwise.
     * 
     * @param pixelsID	The id of the pixels set to handle.
     * @param rndToCopy The rendering settings to copy to the newly created one.
     * @param indexes	Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException       If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public Boolean createRenderingSettings(long pixelsID, RndProxyDef rndToCopy,
			List<Integer> indexes)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Loads the plane info objects related to the passed pixels set.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 * @param z 		The selected z-section or <code>-1</code>.
     * @param t 		The selected time-point or <code>-1</code>.
     * @param channel 	The selected time-point or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadPlaneInfo(long pixelsID, int z, int t, int channel)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Imports the collection of images into the specified container.
	 *  
	 * @param object    The object hosting the information about the 
	 * 					file to import.
	 * @param importable The file to import. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     * @param close Pass <code>true</code> to close the import,
     * 		<code>false</code> otherwise.
	 * @return See above.
	 * @throws ImportException If an error occurred while importing.
	 */
	public Object importFile(ImportableObject object, ImportableFile importable,
			long userID, long groupID, boolean close)
		throws ImportException;
	
	/**
	 * Returns the collection of supported file formats.
	 * 
	 * @return See above.
	 */
	public FileFilter[] getSupportedFileFormats();
	
	/**
	 * Creates a movie. Returns script call-back.
	 * 
	 * @param imageID 	The id of the image.
	 * @param pixelsID 	The id of the pixels set.	
     * @param channels 	The channels to map.
     * @param param 	The parameters to create the movie.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback createMovie(long imageID, long pixelsID, 
			List<Integer> channels, MovieExportParam param)
		throws ProcessException, DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the ROI related to the specified image and the file.
	 * 
	 * @param imageID 	The image's ID.
	 * @param fileIDs	The id of the original file.
	 * @param userID	The user's ID.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIResult> loadROI(long imageID, List<Long> fileIDs, 
			long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the ROI related to the specified image.
	 * 
	 * @param imageID 	The image's ID.
	 * @param userID	The user's ID.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIResult> loadROIFromServer(long imageID, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Exports the passed image as an XML file.
	 * 
	 * @param imageID The ID of the image.
	 * @param folder  The folder where to export the image.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object exportImageAsOMETiff(long imageID, File folder)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the ROI related to the specified image to the server
	 * 
	 * @param imageID 	The image's ID.
	 * @param userID	The user's ID.
	 * @param roiList	The list of ROI to save.
	 * @return True if save successful.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIData> saveROI(long imageID, long userID, List<ROIData> roiList)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates a figure composed of the specified objects.
	 * 
	 * @param ids The objects to use for the figure.
	 * @param type The type of object to handle.
	 * @param parameters The parameters to create the figure.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback createFigure(List<Long> ids, Class type, 
			Object parameters)
		throws ProcessException, DSOutOfServiceException, DSAccessException;

	/**
	 * Renders the passed plane with or without overlays depending on the 
	 * parameters.
	 * 
	 * @param pixelsID 	The id of the pixels set.
	 * @param pd		The plane to render.
	 * @param tableID	The id of the table hosting the mask.
	 * @param overlays	The overlays to render or <code>null</code>.
	 * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @return See above.
	 * @throws RenderingServiceException If the server cannot render the image.
	 */
	public Object renderOverLays(long pixelsID, PlaneDef pd, long tableID,
			Map<Long, Integer> overlays, boolean asTexture)
		throws RenderingServiceException; 
	
	
	/**
	 * Loads the measurement associated to a given object.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadROIMeasurements(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Performs a basic FRAP. Returns the file hosting the results.
	 * 
	 * @param ids	The objects to analyze.
	 * @param type 	The type of object to analyze.
	 * @param param	The extra parameters.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject analyseFrap(List<Long> ids, Class type, Object param)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns all the scripts the default one and the 
	 * uploaded ones depending on the specified flag. 
	 * If a user is specified, returns the scripts owned by the specified 
	 * user.
	 * 
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ScriptObject> loadAvailableScripts(long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns all the scripts with a UI.
	 * 
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ScriptObject> loadAvailableScriptsWithUI()
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the specified script and its parameters.
	 * 
	 * @param scriptID The id of the script.
	 * @return See above.
	 * @throws ProcessException  If the script could not be loaded.
	 */
	public ScriptObject loadScript(long scriptID)
		throws ProcessException;
	
	/**
	 * Returns all the scripts currently stored into the system.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<Long, String> getScriptsAsString()
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Runs the passed script.
	 * 
	 * @param script The script to run.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback runScript(ScriptObject script)
		throws ProcessException, DSOutOfServiceException, DSAccessException;
	
	/**
	 * Uploads the passed script.
	 * 
	 * @param script The script to run.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object uploadScript(ScriptObject script)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * files.
	 * 
	 * @param files		The files to handle.
	 * @param maxLength The maximum length of a thumbnail.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException        If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public Map<DataObject, BufferedImage> getFSThumbnailSet(
			List<DataObject> files, int maxLength, long userID)
		throws DSAccessException, DSOutOfServiceException, FSAccessException;
	
	/**
	 * Get all the available workflows from the server for the user.
	 * @param userID The users id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<WorkflowData> retrieveWorkflows(long userID) 
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Adds the workflows to the server for the user.
	 * 
	 * @param workflows See above.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public  Object storeWorkflows(List<WorkflowData> workflows, long userID) 
	throws DSAccessException, DSOutOfServiceException;

	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * experimenter.
	 * 
	 * @param experimenters	The experimenters to handle.
	 * @param maxLength The maximum length of a thumbnail.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<DataObject, BufferedImage> getExperimenterThumbnailSet(
			List<DataObject> experimenters, int maxLength)
		throws DSAccessException, DSOutOfServiceException;

	/**
	 * Saves locally the images as <code>JPEG</code>.
	 * 
	 * @param param Hosts the information about the objects to save,
	 * 				where to save etc.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback saveAs(SaveAsParam param)
		throws ProcessException, DSAccessException, DSOutOfServiceException;
	
	/**
	 * Indicates if the image corresponding to the specified pixels set is
	 * a large image.
	 * 
	 * @param pixelsId The identifier of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public Boolean isLargeImage(long pixelsId)
		throws DSAccessException, DSOutOfServiceException;

}
