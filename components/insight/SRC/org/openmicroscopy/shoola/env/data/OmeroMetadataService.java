/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import omero.model.AcquisitionMode;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.DetectorType;
import omero.model.FilamentType;
import omero.model.FilterType;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Immersion;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.Medium;
import omero.model.MicroscopeType;
import omero.model.PhotometricInterpretation;
import omero.model.Pulse;

import org.openmicroscopy.shoola.env.data.model.TableParameters;
import omero.gateway.model.TableResult;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;

/** 
 * List of methods to retrieve metadata.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface OmeroMetadataService
{

	/** Identifies that the file is of type movie. */
	public static final int MOVIE = 2;
	
	/** Identifies that the file is of type other. */
	public static final int OTHER = 3;
	
	/** Identifies that the tag and tag set not owned. */
	public static final int TAG_NOT_OWNED = 4;
	
	/** Indicates to retrieve the tags. */
	public static final int LEVEL_TAG = 0;
	
	/** Indicates to retrieve the tag sets. */
	public static final int LEVEL_TAG_SET = 1;
	
	/** Indicates to retrieve the tag sets and the tags. */
	public static final int LEVEL_ALL = 2;
	
	/** Identified the <code>Immersion</code> enumeration. */
	public static final String IMMERSION = Immersion.class.getName();
	
	/** Identified the <code>Correction</code> enumeration. */
	public static final String CORRECTION = Correction.class.getName();
	
	/** Identified the <code>Medium</code> enumeration. */
	public static final String MEDIUM = Medium.class.getName();
	
	/** Identified the <code>Microscope type</code> enumeration. */
	public static final String MICROSCOPE_TYPE = MicroscopeType.class.getName();
	
	/** Identified the <code>Detector type</code> enumeration. */
	public static final String DETECTOR_TYPE = DetectorType.class.getName();
	
	/** Identified the <code>Filter type</code> enumeration. */
	public static final String FILTER_TYPE = FilterType.class.getName();
	
	/** Identified the <code>Binning</code> enumeration. */
	public static final String BINNING = Binning.class.getName();
	
	/** Identified the <code>contrast method</code> enumeration. */
	public static final String CONTRAST_METHOD = ContrastMethod.class.getName();
	
	/** Identified the <code>illumination type</code> enumeration. */
	public static final String ILLUMINATION_TYPE = Illumination.class.getName();
	
	/** Identified the <code>photometric Interpretation</code> enumeration. */
	public static final String PHOTOMETRIC_INTERPRETATION = 
		PhotometricInterpretation.class.getName();
	
	/** Identified the <code>Acquisition mode</code> enumeration. */
	public static final String ACQUISITION_MODE = 
								AcquisitionMode.class.getName();
	
	/** Identified the <code>laser medium</code> enumeration. */
	public static final String LASER_MEDIUM = LaserMedium.class.getName();
	
	/** Identified the <code>Pulse</code> enumeration. */
	public static final String LASER_PULSE = Pulse.class.getName();
	
	/** Identified the <code>laser type</code> enumeration. */
	public static final String LASER_TYPE = LaserType.class.getName();
	
	/** Identified the <code>arc type</code> enumeration. */
	public static final String ARC_TYPE = ArcType.class.getName();
	
	/** Identified the <code>filament type</code> enumeration. */
	public static final String FILAMENT_TYPE = FilamentType.class.getName();

	/** Identified the <code>Format</code> enumeration. */
	public static final String FORMAT = Format.class.getName();
	
	/**
	 * Loads the ratings linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param ctx The security context.
	 * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who added attachments to the object 
     *               or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadRatings(SecurityContext ctx, Class type, long id,
			long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all annotations related to the object specified by the class
	 * type and the id.
	 * 
	 * @param ctx The security context.
	 * @param type The type of the object.
     * @param id The id of the object.
     * @param userID The id of the user who added attachments to the object 
     *               or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadStructuredAnnotations(SecurityContext ctx, Class type,
			long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads data related to the specified object.
	 * 
	 * @param ctx The security context.
	 * @param object The object to handle.
     * @param userID The id of the user who added attachments to the object 
     *               or <code>-1</code> if the user is not specified.
     * @param viewed Pass <code>true</code> to load the rendering settings 
	 *               related to the objects, <code>false<code> otherwise.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public StructuredDataResults loadStructuredData(SecurityContext ctx,
			Object object, long userID, boolean viewed)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads data related to the specified objects
	 * 
	 * @param ctx The security context.
	 * @param data The objects to handle.
     * @param userID The id of the user who added attachments to the object 
     *               or <code>-1</code> if the user is not specified.
     * @param viewed Pass <code>true</code> to load the rendering settings 
	 *               related to the objects, <code>false<code> otherwise.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<DataObject, StructuredDataResults> loadStructuredData(
			SecurityContext ctx, List<DataObject> data, long userID,
			boolean viewed)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the specified data object and returns the annotated object.
	 * 
	 * @param ctx The security context.
	 * @param toAnnotate	The object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotation 	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject annotate(SecurityContext ctx, DataObject toAnnotate,
			AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the object and returns the annotated object.
	 * 
	 * @param ctx The security context.
	 * @param type The type of object to annotate. Mustn't be <code>null</code>.
	 * @param id The id of the object to annotate. Mustn't be <code>null</code>.
	 * @param annotation The annotation to create. Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject annotate(SecurityContext ctx, Class type, long id,
			AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Removes all annotations of a given type from the specified object.
	 * 
	 * @param ctx The security context.
	 * @param object The object to handle. Mustn't be <code>null</code>.
	 * @param annotationType The type of annotation to clear.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(SecurityContext ctx, DataObject object,
			Class annotationType)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Removes all annotations from the specified object.
	 * 
	 * @param ctx The security context.
	 * @param object The object to handle. Mustn't be <code>null</code>.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(SecurityContext ctx, DataObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Clears the annotation related to a given type object.
	 * 
	 * @param ctx The security context.
	 * @param type The type of object the annotations are related to. 
	 * @param id The object's id.
	 * @param annotationType The type of annotation to delete.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(SecurityContext ctx, Class type, long id,
			Class annotationType)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the annotation.
	 * 
	 * @param ctx The security context.
	 * @param annotationID The id of the annotation.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject loadAnnotation(SecurityContext ctx, long annotationID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all annotations of a given type.
	 * 
	 * @param ctx The security context.
	 * @param annotationType The type of annotation to retrieve.
	 * @param nameSpace The name space of the annotation or <code>null</code>.
	 * @param userID The id of the user the annotations are related to.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadAnnotations(SecurityContext ctx, Class annotationType,
			String nameSpace, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the object, adds (resp. removes) annotations to (resp. from)
	 * the object if any.
	 * 
	 * @param ctx The security context.
	 * @param data The data object to handle.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove. 
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveData(SecurityContext ctx, Collection<DataObject> data,
	 List<AnnotationData> toAdd, List<Object> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param ctx The security context.
	 * @param data The data object to handle.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(SecurityContext ctx,
			Collection<DataObject> data, List<AnnotationData> toAdd,
			List<Object> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param ctx The security context.
	 * @param data The data object to handle.
	 * @param toAdd Collection of annotations to add.
	 * @param toRemove Collection of annotations to remove.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(SecurityContext ctx, TimeRefObject data, 
		List<AnnotationData> toAdd, List<Object> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param ctx The security context.
	 * @param file The file to write the data into.
	 * @param fileID The id of the file to download.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public File downloadFile(SecurityContext ctx, File file, long fileID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the ratings associated to the passed objects.
	 * 
	 * @param ctx The security context.
	 * @param nodeType  The type of object.
	 * @param nodeIds The ids of the object.
	 * @param userID The user id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<Long, Collection> loadRatings(SecurityContext ctx,
			Class nodeType, List<Long> nodeIds, long userID) 
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
	 * @param ctx The security context.
	 * @param nodeType
	 * @param nodeIds
	 * @param annotationType
	 * @param terms
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotation(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, List<String> terms,
		long userID) 
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
	 * @param ctx The security context.
	 * @param nodeType
	 * @param nodeIds
	 * @param annotationType
	 * @param annotated
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotated(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, boolean annotated,
		long userID) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * 
	 * @param ctx The security context.
	 * @param nodeType
	 * @param ids
	 * @param filter
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotation(SecurityContext ctx, Class nodeType,
			List<Long> ids, FilterContext filter, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the enumeration corresponding to the specified type.
	 * 
	 * @param ctx The security context.
	 * @param type The type of enumeration.
	 * One of the enumerations defined by this class.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getEnumeration(SecurityContext ctx, String type)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the acquisition metadata for an image or a given channel.
	 * 
	 * @param ctx The security context.
	 * @param refObject Either an <code>ImageData</code> or 
     * 					<code>ChannelData</code> node.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object loadAcquisitionData(SecurityContext ctx, Object refObject)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the acquisition metadata for an image or a given channel.
	 * 
	 * @param ctx The security context.
	 * @param refObject Either an <code>ImageAcquisitionData</code> or 
     * 					<code>ChannelAcquisitionData</code> node.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveAcquisitionData(SecurityContext ctx, Object refObject)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the file back to the server.
	 * 
	 * @param ctx The security context.
	 * @param fileAnnotation 	The annotation hosting the previous info.
     * @param file				The file to save.
     * @param index				One of the constants defined by this class.
     * @param linkTo			The <code>DataObject</code> to link the 
     *  						annotation to.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object archivedFile(SecurityContext ctx,
		FileAnnotationData fileAnnotation, File file, int index,
		DataObject linkTo)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the <code>Tag Set</code> object(s) or <code>Tag</code> object(s)
	 * depending on the specified parameters. Returns a collection of 
	 * <code>TagAnnotationData</code>s.
	 * 
	 * @param ctx The security context.
	 * @param id The id if specified.
	 * @param topLevel  Pass <code>true</code> to indicate to load the 
	 * 					<code>Tag Set</code> objects, <code>false</code> to 
	 * 					load the <code>Tag</code> objects.
	 * @param userID	The identifier of the user.
	 * @param groupID	The identifier of the user's group.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTags(SecurityContext ctx, Long id,
		boolean topLevel, long userID, long groupID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the number of files of a given type.
	 * 
	 * @param ctx The security context.
	 * @param userID The user's identifier.
	 * @param fileType One of the constants above.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public long countFileType(SecurityContext ctx, long userID, int fileType)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the files specified by the given type. Returns a collection
	 * of <code>FileAnnotationData</code>s.
	 * 
	 * @param ctx The security context.
	 * @param fileType One of the constants above. 
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadFiles(SecurityContext ctx, int fileType, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the instrument.
	 * 
	 * @param ctx The security context.
	 * @param instrumentID The id of the instrument.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object loadInstrument(SecurityContext ctx, long instrumentID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns a collection of tabular data corresponding to the specified
	 * parameters.
	 * 
	 * @param ctx The security context.
	 * @param parameters The parameters to handle.
	 * @param userID     The user's identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<TableResult> loadTabularData(SecurityContext ctx,
		TableParameters parameters, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the parents of the specified annotation
	 * (taking the current user context into account).
	 * @param ctx The security context.
	 * @param annotationId The annotation to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<DataObject> loadParentsOfAnnotations(SecurityContext ctx,
		long annotationId)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
         * Loads the parents of the specified annotation
         * (in the scope of the given user context)
         * 
         * @param ctx The security context.
         * @param annotationId The annotation to handle.
         * @param userId The id of the user
         * @return See above.
         * @throws DSOutOfServiceException  If the connection is broken, or logged
         *                                  in.
         * @throws DSAccessException        If an error occurred while trying to 
         *                                  retrieve data from OMEDS service.
         */
	public List<DataObject> loadParentsOfAnnotations(SecurityContext ctx,
                long annotationId, long userId) throws DSOutOfServiceException, DSAccessException;
                
	/**
	 * Saves the channels. Applies the changes to the images contained in
	 * the specified objects whose number of channels matches the number of 
	 * speficied channels. This could be datasets, plates or images.
	 * Returns the identifiers of the images whose channels have been updated,
	 * or an empty list if no images were updated.
	 * 
	 * @param ctx The security context.
	 * @param channels The channels to update.
	 * @param objects The objects to apply the changes to. If the objects are
	 * datasets, then all the images within the datasets will be updated.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<Long> saveChannelData(SecurityContext ctx,
			List<ChannelData> channels, List<DataObject> objects)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Retrieves the channel data for the specified pixels set.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID The id of pixels set.
	 * @return A list of channels.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	public List<ChannelData> getChannelsMetadata(SecurityContext ctx,
			long pixelsID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param ctx The security context.
	 * @param file The file to write the data into.
	 * @param id The id of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @throws ProcessException If an error occurred while starting the process.
	 */
	public RequestCallback downloadMetadataFile(SecurityContext ctx, File file,
			long id)
		throws DSOutOfServiceException, DSAccessException, ProcessException;

	/**
	 * Loads the annotations of the given type linked to the specified objects.
	 * Returns a map whose keys are the object's id and the values are a
	 * collection of annotation linked to that object.
	 * 
	 * @param ctx The security context.
	 * @param rootType The type of object the annotations are linked to e.g.
	 * Image.
	 * @param rootIDs The collection of object's ids the annotations are linked
	 * to.
	 * @param annotationType The type of annotation to load.
	 * @param nsInclude The annotation's name space to include if any.
	 * @param nsExlcude The annotation's name space to exclude if any.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Map<Long, Collection<AnnotationData>>
	loadAnnotations(SecurityContext ctx, Class<?> rootType,
		List<Long> rootIDs, Class<?> annotationType, List<String> nsInclude,
		List<String> nsExlcude)
	throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the log files linked to the specified objects.
	 *
	 * @param ctx The security context.
	 * @param rootType The type of object to handle.
	 * @param rootIDs The collection of object's identifiers.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	public Map<Long, List<IObject>> loadLogFiles(SecurityContext ctx,
	        Class<?> rootType, List<Long> rootIDs)
	                throws DSOutOfServiceException, DSAccessException;

    /**
     * Add (resp. removes) annotations to (resp. from) the objects if any.
     *
     * @param ctx The security context.
     * @param toAdd Collection of annotations to add.
     * @param toRemove Collection of annotations to remove.
     * @param userID The id of the user.
     * @throws DSOutOfServiceException  If the connection is broken, or logged
     *                                   in.
     * @throws DSAccessException        If an error occurred while trying to 
     *                                  retrieve data from OMEDS service.
     */
    public void saveAnnotationData(SecurityContext ctx,
            Map<DataObject, List<AnnotationData>> toAdd,
            Map<DataObject, List<AnnotationData>> toRemove, long userID)
        throws DSOutOfServiceException, DSAccessException;
}
