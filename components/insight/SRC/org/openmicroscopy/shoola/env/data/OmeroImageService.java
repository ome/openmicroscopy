/*
 * org.openmicroscopy.shoola.env.data.OmeroImageService
 *
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


//Java imports
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

//Third-party libraries






//Application-internal dependencies
import omero.api.RawPixelsStorePrx;
import omero.api.ThumbnailStorePrx;
import omero.constants.projection.ProjectionType;
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.util.Target;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.rnd.RenderingControl;
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
 * @since OME2.2
 */
public interface OmeroImageService
{
  
	/** Indicates to export the image as OME TIFF. */
	public static final int EXPORT_AS_OMETIFF = 0;
	
	/** Indicates to export the image as OME XML. */
	public static final int EXPORT_AS_OME_XML = 1;
	
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
	public static final int	SUM_INTENSITY =
		ProjectionType.SUMINTENSITY.ordinal();
	
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
	 * @param ctx The security context.
	 * @param pixelsID The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException       If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public RenderingControl loadRenderingControl(SecurityContext ctx,
			long pixelsID)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Renders the specified 2D-plane. 
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The ID of the pixels set.
	 * @param pd The plane to render.
	 * @param largeImage Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
	 * @param compression The compression level.
	 * @return The image representing the plane.
	 * @throws RenderingServiceException If the server cannot render the image.
	 */
	public Object renderImage(SecurityContext ctx,
		long pixelsID, PlaneDef pd, boolean largeImage, int compression)
		throws RenderingServiceException;

    /**
     * Returns true if a connection is available for the given
     * {@link SecurityContext}. This is equivalent to being able to
     * access a {@link Connector};
     *
     * @param ctx The security context.
     */
	public boolean isAlive(SecurityContext ctx)
	    throws DSOutOfServiceException;

	/**
	 * Shuts downs the rendering service attached to the specified 
	 * pixels set.
	 *
	 * @param ctx The security context.
	 * @param pixelsID  The ID of the pixels set.
	 */
	public void shutDown(SecurityContext ctx, long pixelsID);

	/**
	 * Returns a thumbnail of the currently selected 2D-plane for the
	 * passed pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @param sizeX The width of the thumbnail.
	 * @param sizeY The height of the thumnail.
	 * @param userID The id of the user the thumbnail is for.
	 * @return See above.
	 * @throws RenderingServiceException    If the server is out of service.
	 */
	public BufferedImage getThumbnail(SecurityContext ctx, long pixelsID,
		int sizeX, int sizeY, long userID)
		throws RenderingServiceException;

	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID	The collection of pixels set.
	 * @param maxLength The maximum length of a thumbnail.
	 * @return See above.
	 * @throws RenderingServiceException  If the server is out of service.
	 */
	public Map<Long, BufferedImage> getThumbnailSet(SecurityContext ctx,
		Collection<Long> pixelsID, int maxLength)
		throws RenderingServiceException;
	
	/**
	 * Reloads the rendering engine for the passed set of pixels.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws RenderingServiceException If the rendering engine cannot be 
	 * 									 started.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public RenderingControl reloadRenderingService(SecurityContext ctx,
		long pixelsID)
		throws DSAccessException, RenderingServiceException;
	
	/**
	 * Reloads the rendering engine for the passed set of pixels.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws RenderingServiceException If the rendering engine cannot be 
	 * 									 started.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public RenderingControl resetRenderingService(SecurityContext ctx,
		long pixelsID)
		throws DSAccessException, RenderingServiceException;

	/**
	 * Loads the pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public PixelsData loadPixels(SecurityContext ctx, long pixelsID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns the XY-plane identified by the passed z-section, time-point 
	 * and wavelength.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of pixels containing the requested plane.
	 * @param z The selected z-section.
	 * @param t The selected time-point.
	 * @param c The selected wavelength.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException        If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public byte[] getPlane(SecurityContext ctx, long pixelsID, int z, int t,
		int c)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets
	 * if the rootType is <code>DatasetData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set of reference.
	 * @param rootNodeType The type of nodes.
	 * @param nodeIDs The id of the nodes to apply settings to. 
	 * Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map pasteRenderingSettings(SecurityContext ctx, long pixelsID,
		Class rootNodeType, List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param nodeIDs The id of the nodes to apply settings to. 
	 *                Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map resetRenderingSettings(SecurityContext ctx, Class rootNodeType,
		List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Sets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param ctx The security context.
	 * @param rootNodeType The type of nodes. Can either be 
	 *                     <code>ImageData</code>, <code>DatasetData</code>
	 * @param nodeIDs The id of the nodes to apply settings to.
	 *                Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map setMinMaxSettings(SecurityContext ctx, Class rootNodeType,
		List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Sets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType The type of nodes. Can either be 
	 *                     <code>ImageData</code>, <code>DatasetData</code>.
	 * @param nodeIDs The id of the nodes to apply settings to.
	 *                Mustn't be <code>null</code>.
	 * @return A map with two keys. A <code>True</code> key whose value 
	 * is a list of image's id, the settings have been applied to. 
	 * A <code>False</code> key whose value is a list
	 * of image's id, the settings couldn't be applied.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map setOwnerRenderingSettings(SecurityContext ctx,
			Class rootNodeType, List<Long> nodeIDs)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the rendering setting related to a given set of pixels.
	 * Returns a Map whose keys are the experimenter who created the rendering 
	 * settings and the value the settings itself.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @param userID The id of the user currently logged in.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<DataObject, Collection<RndProxyDef>>
	getRenderingSettings(SecurityContext ctx, long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the rendering setting related to a given set of pixels
	 * for the specified user.
	 *  
	 *  @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @param userID The id of the user currently logged in.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<RndProxyDef> getRenderingSettingsFor(SecurityContext ctx,
		long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Creates a preview projected image 
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The ID of the pixels set.
	 * @param startZ The first optical section.
	 * @param endZ The last optical section.
	 * @param stepping The stepping used during the projection.
	 * @param type The type of projection.
     * @param channels The collection of channels to project.
	 * @return The buffered image representing the projected image.
	 * @throws RenderingServiceException If the server cannot render the image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 */
	public BufferedImage renderProjected(SecurityContext ctx, long pixelsID,
		int startZ, int endZ, int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException; 
	
	/**
	 * Projects the specified set of pixels according to the projection's 
	 * parameters. Adds the created image to the passed dataset.
	 * 
	 * @param ctx The security context.
	 * @param ref The object hosting the projection's parameters.
	 * @return The newly created image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public ImageData projectImage(SecurityContext ctx, ProjectionParam ref)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates rendering setting for the specified pixels set and
     * copies the settings from the passed rendering setting object if
     * not <code>null</code>. Returns <code>true</code> if the rendering
     * settings have been successfully created and updated, <code>false</code>
     * otherwise.
     * 
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set to handle.
     * @param rndToCopy The rendering settings to copy to the newly created one.
     * @param indexes Collection of channel's indexes. 
     * Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException       If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public Boolean createRenderingSettings(SecurityContext ctx, long pixelsID,
		RndProxyDef rndToCopy, List<Integer> indexes)
		throws DSOutOfServiceException, DSAccessException, FSAccessException;

	/**
	 * Loads the plane info objects related to the passed pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @param z The selected z-section or <code>-1</code>.
     * @param t The selected time-point or <code>-1</code>.
     * @param channel The selected time-point or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadPlaneInfo(SecurityContext ctx,
		long pixelsID, int z, int t, int channel)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Imports the collection of images into the specified container.
	 *  
	 * @param object The object hosting the information about the file to import.
	 * @param importable The file to import. Mustn't be <code>null</code>.
	 * @param userID The id of the user.
     * @param close Pass <code>true</code> to close the import, 
     * <code>false</code> otherwise.
	 * @return See above.
	 * @throws ImportException If an error occurred while importing.
	 */
	public Object importFile(ImportableObject object,
		ImportableFile importable , boolean close)
		throws ImportException, DSAccessException, DSOutOfServiceException;
	
	/**
	 * Returns the collection of supported file formats.
	 * 
	 * @return See above.
	 */
	public FileFilter[] getSupportedFileFormats();
	
	/**
	 * Creates a movie. Returns script call-back.
	 * 
	 * @param ctx The security context.
	 * @param imageID The id of the image.
	 * @param pixelsID The id of the pixels set.	
     * @param channels The channels to map.
     * @param param The parameters to create the movie.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback createMovie(SecurityContext ctx,
		long imageID, long pixelsID, List<Integer> channels, 
		MovieExportParam param)
		throws ProcessException, DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the ROI related to the specified image and the file.
	 * 
	 * @param ctx The security context.
	 * @param imageID The image's ID.
	 * @param fileIDs The id of the original file.
	 * @param userID The user's ID.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIResult> loadROI(SecurityContext ctx, long imageID,
		List<Long> fileIDs, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the ROI related to the specified image.
	 * 
	 * @param ctx The security context.
	 * @param imageID The image's ID.
	 * @param userID The user's ID.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIResult> loadROIFromServer(SecurityContext ctx, long imageID,
		long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Exports the passed image as an XML file.
	 * 
	 * @param index One of the export contants defined by this class.
	 * @param imageID The ID of the image.
	 * @param folder  The folder where to export the image.
	 * @param target The selected schema.
	 * @param ctx The security context.
	 * @param imageID The ID of the image.
	 * @param folder The folder where to export the image.
	 * @param target Host information about the downgrade style sheets.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object exportImageAsOMEFormat(SecurityContext ctx, int index,
			long imageID, File folder, Target target)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the ROI related to the specified image to the server
	 * 
	 * @param ctx The security context.
	 * @param imageID The image's ID.
	 * @param userID The user's ID.
	 * @param roiList The list of ROI to save.
	 * @return True if save successful.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ROIData> saveROI(SecurityContext ctx, long imageID,
		long userID, List<ROIData> roiList)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Creates a figure composed of the specified objects.
	 * 
	 * @param ctx The security context.
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
	public ScriptCallback createFigure(SecurityContext ctx,
		List<Long> ids, Class type, Object parameters)
		throws ProcessException, DSOutOfServiceException, DSAccessException;

	/**
	 * Renders the passed plane with or without overlays depending on the 
	 * parameters.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID 	The id of the pixels set.
	 * @param pd		The plane to render.
	 * @param tableID	The id of the table hosting the mask.
	 * @param overlays	The overlays to render or <code>null</code>.
	 * @return See above.
	 * @throws RenderingServiceException If the server cannot render the image.
	 */
	public Object renderOverLays(SecurityContext ctx, long pixelsID,
		PlaneDef pd, long tableID, Map<Long, Integer> overlays)
		throws RenderingServiceException; 

	/**
	 * Loads the measurement associated to a given object.
	 * 
	 * @param ctx The security context.
	 * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who added attachments to the object 
     * or <code>-1</code> if the user is not specified.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadROIMeasurements(SecurityContext ctx, Class type,
		long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns all the scripts the default one and the 
	 * uploaded ones depending on the specified flag. 
	 * If a user is specified, returns the scripts owned by the specified 
	 * user.
	 * 
	 * @param ctx The security context.
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ScriptObject> loadAvailableScripts(SecurityContext ctx,
		long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns all the scripts with a UI.
	 * 
	 * @param ctx The security context.
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<ScriptObject> loadAvailableScriptsWithUI(SecurityContext ctx)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the specified script and its parameters.
	 * 
	 * @param ctx The security context.
	 * @param scriptID The id of the script.
	 * @return See above.
	 * @throws ProcessException  If the script could not be loaded.
	 */
	public ScriptObject loadScript(SecurityContext ctx, long scriptID)
		throws ProcessException;
	
	/**
	 * Returns all the scripts currently stored into the system.
	 * 
	 * @param ctx The security context.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<Long, String> getScriptsAsString(SecurityContext ctx)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Runs the passed script.
	 * 
	 * @param ctx The security context.
	 * @param script The script to run.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback runScript(SecurityContext ctx, ScriptObject script)
		throws ProcessException, DSOutOfServiceException, DSAccessException;
	
	/**
	 * Uploads the passed script.
	 * 
	 * @param ctx The security context.
	 * @param script The script to run.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object uploadScript(SecurityContext ctx, ScriptObject script)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * files.
	 * 
	 * @param ctx The security context.
	 * @param files The files to handle.
	 * @param maxLength The maximum length of a thumbnail.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws FSAccessException        If an error occurred while trying to 
	 *                                  retrieve data using OMERO.fs.
	 */
	public Map<DataObject, BufferedImage> getFSThumbnailSet(SecurityContext ctx,
		List<DataObject> files, int maxLength, long userID)
		throws DSAccessException, DSOutOfServiceException, FSAccessException;
	
	/**
	 * Returns all the available workflows from the server for the user.
	 * 
	 * @param ctx The security context.
	 * @param userID The users id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<WorkflowData> retrieveWorkflows(SecurityContext ctx,
		long userID)
		throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Adds the workflows to the server for the user.
	 * 
	 * @param ctx The security context.
	 * @param workflows See above.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public  Object storeWorkflows(SecurityContext ctx,
		List<WorkflowData> workflows, long userID) 
		throws DSAccessException, DSOutOfServiceException;

	/**
	 * Retrieves the thumbnails corresponding to the passed collection of
	 * experimenter.
	 * 
	 * @param ctx The security context.
	 * @param experimenters	The experimenters to handle.
	 * @param maxLength The maximum length of a thumbnail.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<DataObject, BufferedImage> getExperimenterThumbnailSet(
		SecurityContext ctx, List<DataObject> experimenters, int maxLength)
		throws DSAccessException, DSOutOfServiceException;

	/**
	 * Saves locally the images as <code>JPEG</code>.
	 * 
	 * @param ctx The security context.
	 * @param param Hosts the information about the objects to save,
	 * 				where to save etc.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public ScriptCallback saveAs(SecurityContext ctx, SaveAsParam param)
		throws ProcessException, DSAccessException, DSOutOfServiceException;
	
	/**
	 * Indicates if the image corresponding to the specified pixels set is
	 * a large image.
	 * 
	 * @param ctx The security context.
	 * @param pixelsId The identifier of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public Boolean isLargeImage(SecurityContext ctx, long pixelsId)
		throws DSAccessException, DSOutOfServiceException;

	/**
	 * Loads the file set corresponding to the specified image.
	 * 
	 * @param ctx The security context.
	 * @param imageId The identifier of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	public Set<DataObject> getFileSet(SecurityContext ctx, long imageId)
		throws DSAccessException, DSOutOfServiceException;

	/**
	 * Creates a thumbnail store for the specified security context.
	 * This method has to be used with care. The stateful service must be closed
	 * when the work is complete.
	 * 
	 * @param ctx The context to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	ThumbnailStorePrx createThumbnailStore(SecurityContext ctx)
			throws DSAccessException, DSOutOfServiceException;
	
	/**
	 * Retrieves the rendering settings for the specified pixels set.
	 *
	 * @param ctx The security context.
	 * @param pixelsID  The pixels ID.
	 * @param userID	The id of the user who set the rendering settings.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to
	 *                                  retrieve data from OMEDS service.
	 */
	Long getRenderingDef(SecurityContext ctx, long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException;

    /**
     * Retrieves the rendering settings for the specified pixels set.
     *
     * @param ctx The security context.
     * @param rndID  The rendering settings ID.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged
     *                                 in.
     * @throws DSAccessException       If an error occurred while trying to
     *                                 retrieve data from OMEDS service.
     */
	RndProxyDef getSettings(SecurityContext ctx, long rndID)
        throws DSOutOfServiceException, DSAccessException;

	/**
     * Creates a pixels store for the specified security context.
     * This method has to be used with care. The stateful service must be closed
     * when the work is complete.
     *
     * @param ctx The context to handle.
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged
     *                                  in.
     * @throws DSAccessException If an error occurred while trying to
     *                                  retrieve data from OMEDS service.
     */
	RawPixelsStorePrx createPixelsStore(SecurityContext ctx)
            throws DSAccessException, DSOutOfServiceException;
}
