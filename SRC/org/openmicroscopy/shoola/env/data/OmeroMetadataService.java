/*
 * org.openmicroscopy.shoola.env.data.OmeroMetadataService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.model.AcquisitionMode;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.DetectorType;
import omero.model.FilamentType;
import omero.model.FilterType;
import omero.model.Illumination;
import omero.model.Immersion;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.Medium;
import omero.model.MicroscopeType;
import omero.model.PhotometricInterpretation;
import omero.model.Pulse;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.FileAnnotationData;

/** 
 * List of methods to retrieve metadata.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface OmeroMetadataService
{

	/** Identifies that the file is of type protocol. */
	public static final int		EDITOR_PROTOCOL = 0;
	
	/** Identifies that the file is of type experiment. */
	public static final int		EDITOR_EXPERIMENT = 1;
	
	/** Identifies that the file is of type movie. */
	public static final int		MOVIE = 2;
	
	/** Identifies that the file is of type other. */
	public static final int		OTHER = 3;
	
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

	/**
	 * Loads the ratings linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadRatings(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all annotations related to the object specified by the class
	 * type and the id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadStructuredAnnotations(Class type, long id, 
												long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads data related to the specified object.
	 * 
	 * @param object 	The object to handle.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @param viewed	Pass <code>true</code> to load the rendering settings 
	 * 					related to the objects, <code>false<code>
	 * 					otherwise.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public StructuredDataResults loadStructuredData(Object object, 
													long userID, boolean viewed)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads data related to the specified objects
	 * 
	 * @param data 		The objects to handle.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @param viewed	Pass <code>true</code> to load the rendering settings 
	 * 					related to the objects, <code>false<code>
	 * 					otherwise.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map loadStructuredData(List<DataObject> data, long userID, 
								boolean viewed)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the specified data object and returns the annotated object.
	 * 
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
	public DataObject annotate(DataObject toAnnotate, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the object and returns the annotated object.
	 * 
	 * @param type  		The type of object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param id			The id of the object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotation 	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject annotate(Class type, long id, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Removes all annotations of a given type from the specified object.
	 * 
	 * @param object			The object to handle. 
	 * 							Mustn't be <code>null</code>.
	 * @param annotationType	The type of annotation to clear.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(DataObject object, Class annotationType)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Removes all annotations from the specified object.
	 * 
	 * @param object	The object to handle. Mustn't be <code>null</code>.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(DataObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Clears the annotation related to a given type object.
	 * 
	 * @param type				The type of object the annotations are 
	 * 							related to. 
	 * @param id				The object's id.
	 * @param annotationType	The type of annotation to delete.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(Class type, long id, Class annotationType)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the annotation.
	 * 
	 * @param annotationID The id of the annotation.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject loadAnnotation(long annotationID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all annotations of a given type.
	 * 
	 * @param annotationType 	The type of annotation to retrieve.
	 * @param nameSpace			The name space of the annotation 
	 * 							or <code>null</code>.
	 * @param userID			The id of the user the annotations are related
	 * 							to.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadAnnotations(Class annotationType, String nameSpace,
									long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the object, adds (resp. removes) annotations to (resp. from)
	 * the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveData(Collection<DataObject> data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(Collection<DataObject> data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(TimeRefObject data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param file		The file to write the data into.
	 * @param fileID	The id of the file to download.
	 * @param size		The size of the file to download
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public File downloadFile(File file, long fileID, long size)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the ratings associated to the passed objects.
	 * 
	 * @param nodeType  The type of object.
	 * @param nodeIds	The ids of the object.
	 * @param userID	The user id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<Long, Collection> loadRatings(Class nodeType, 
			List<Long> nodeIds, long userID) 
			throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
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
	public Collection filterByAnnotation(Class nodeType, List<Long> nodeIds, 
		Class annotationType, List<String> terms, long userID) 
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
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
	public Collection filterByAnnotated(Class nodeType, List<Long> nodeIds, 
		Class annotationType, boolean annotated, long userID) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * 
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
	public Collection filterByAnnotation(Class nodeType, List<Long> ids, 
										FilterContext filter, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the data objects related to the tags contained within the specified
	 * tag set.
	 * 
	 * @param id			The id of the tag set or <code>-1</code>.
     * @param dataObject    Pass <code>true</code> to load the 
	 * 						<code>DataObject</code> related 
     * 						to the tags, <code>false</code> otherwise.
     * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTagSetsContainer(Long id, boolean images,
										long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the enumeration corresponding to the specified type.
	 * 
	 * @param type 	The type of enumeration. 
	 * 				One of the enumerations defined by this class.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection getEnumeration(String type)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the acquisition metadata for an image or a given channel.
	 * 
	 * @param refObject Either an <code>ImageData</code> or 
     * 					<code>ChannelData</code> node.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object loadAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException;
	
	
	/**
	 * Loads the acquisition metadata for an image or a given channel.
	 * 
	 * @param refObject Either an <code>ImageAcquisitionData</code> or 
     * 					<code>ChannelAcquisitionData</code> node.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the file back to the server.
	 * 
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
	public Object archivedFile(FileAnnotationData fileAnnotation, File file, int
			index, DataObject linkTo)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all rendering settings linked to the passed image.
	 * 
	 * @param imageID	The id of the image.
	 * @param pixelsID	The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadViewedBy(long imageID, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the <code>Tag Set</code> object(s) or <code>Tag</code> object(s)
	 * depending on the specified parameters. Returns a collection of 
	 * <code>TagAnnotationData</code>s.
	 * 
	 * @param id The id if specified.
	 * @param dataObject Pass <code>true</code> to load the 
	 * 					<code>DataObject</code>s linked to the <code>Tag</code>,
	 * 					<code>false</code> otherwise.
	 * @param topLevel  Pass <code>true</code> to indicate to load the 
	 * 					<code>Tag Set</code> objects, <code>false</code> to 
	 * 					load the <code>Tag</code> objects.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTags(Long id, boolean dataObject, boolean topLevel,
			long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Returns the number of files of a given type. The specified type is 
	 * one of the following values: {@link #EDITOR_PROTOCOL}, 
	 * {@link #EDITOR_EXPERIMENT} or {@link #OTHER}.
	 * 
	 * @param fileType One of the constants above.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public long countFileType(int fileType)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the files specified by the given type. Returns a collection
	 * of <code>FileAnnotationData</code>s. The specified type is 
	 * one of the following values: {@link #EDITOR_PROTOCOL}, 
	 * {@link #EDITOR_EXPERIMENT} or {@link #OTHER}.
	 * 
	 * @param fileType One of the constants above. 
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadFiles(int fileType, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the instrument.
	 * 
	 * @param instrumentID The id of the instrument.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object loadInstrument(long instrumentID)
		throws DSOutOfServiceException, DSAccessException;
	
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
	
}
