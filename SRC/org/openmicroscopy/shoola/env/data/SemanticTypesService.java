/*
 * org.openmicroscopy.shoola.env.data.SemanticTypeService
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.List;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author  <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">
 *                  jeffm@alum.mit.edu</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public interface SemanticTypesService
{
    /**
     * Gets a list of all available semantic types with global granularity.
     * @return A list of all available global types.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List getAvailableGlobalTypes()
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Gets a list of all available semantic types with dataset granularity.
     * @return A list of all available dataset types.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List getAvailableDatasetTypes()
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Gets a list of all available semantic types with image granularity.
     * @return A list of all available image types.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List getAvailableImageTypes()
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Gets a list of all available semantic types with feature granularity.
     * @return A list of all available feature types.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List getAvailableFeatureTypes()
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Counts the number of attributes of the given semantic type that
     * correspond to the dataset with the specified ID.  If the answer is 0,
     * the client should not attempt to retrieve a list of attributes.
     * 
     * @param type The type of attribute to count.
     * @param datasetID Which dataset to search.
     * @return The number of attributes of the given type associated with
     *         The specified dataset.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public int countDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public int countDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Counts the number of attributes of the given semantic type that
     * correspond to the image with the specified ID.  If the answer is 0,
     * the client should not attempt to retrieve a list of attributes.
     * 
     * @param type The type of attribute to count.
     * @param imageID Which image to search.
     * @return The number of attributes of the given type associated with
     *         The specified image.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public int countImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see countImageAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public int countImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Counts the number of attributes of the given semantic type that
     * correspond to the images with IDs in the specified list.  If the
     * answer is 0, the client should not attempt to retrieve a list
     * of attributes.
     * @param type The type of attribute to count.
     * @param imageIDList Which images to query.
     * @return The number of attributes of the given type associated with
     *         the specified image.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error. 
     */
    public int countImageAttributes(SemanticType type, List imageIDList)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see countImageAttributes(org.openmicroscopy.ds.dto.SemanticType,java.util.List)
     */
    public int countImageAttributes(String typeName, List imageIDList)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Counts the number of attributes of the given semantic type that
     * correspond to the feature with the specified ID.  If the answer is 0,
     * the client should not attempt to retrieve a list of attributes.
     * 
     * @param type The type of attribute to count.
     * @param featureID Which project to search.
     * @return The number of attributes of the given type associated with
     *         The specified feature.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see countFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public int countFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Creates an attribute of the specified type, so that you can set the
     * values of a new Attribute through its mutator methods, and appropriate
     * DTO values embedded.
     * @param type The type of Attribute to create.
     * @return The created attribute.
     */
    public Attribute createAttribute(SemanticType type)
		throws DSOutOfServiceException, DSAccessException;
        
    public Attribute createAttribute(SemanticType type, int objectID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Creates an attribute of the type with the specified name, so that you
     * can set the values of a new Attribute through its mutator methods, and
     * appropriate DTO values embedded.
     * @param typeName The name of the attribute to create.
     * @return The created attribute.
     */
    public Attribute createAttribute(String typeName)
		throws DSOutOfServiceException, DSAccessException;
        
        
    public Attribute createAttribute(String typeName, int objectID)
        throws DSOutOfServiceException, DSAccessException;

    /**
     * Retrieves the attribute with the given SemanticType and specified
     * attributeID.
     * @param type The type of attribute to retrieve.
     * @param attributeID The ID of the attribute to retrieve.
     * @return The Attribute corresponding to the specified ID.
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * @see retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public Attribute retrieveAttribute(String typeName, int attributeID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves all the Attributes with the given SemanticType that
     * belong to a dataset with the specified ID.
     * @param type The type of attribute to retrieve.
     * @param datasetID The ID of the dataset to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified dataset.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Returns all the Attributes with the given SemanticType that belong to a
     * dataset with the specified ID, and include (in the DB call) the attribute
     * with the specified name.  For example, if childAttribute is
     * <code>OTF.Instrument</code>, the retrieval will include both the
     * fully-spec'd embedded OTF and Instrument.  names must be specified by
     * their names in the semantic type, not their semantic type names.
     * Calling an invalid name will generate a <code>DSAccessException</code>.
     * 
     * @param typeName The type of attribute to retrieve.
     * @param childAttribute The child attribute tree to retrieve (by name)
     * @param datasetID The ID of the dataset to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified dataset.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there is a communication error or the
     *                           childAttribute name is invalid.
     */
    public List retrieveDatasetAttributes(String typeName, 
    									String childAttribute, int datasetID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Retrieves all the Attributes with the given SemanticType that
     * belong to an image with the specified ID.
     * @param type The type of attribute to retrieve.
     * @param imageID The ID of the image to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified image.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public List retrieveImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Returns all the Attributes with the given SemanticType that belong to an
     * image with the specified ID, and include (in the DB call) the attribute
     * with the specified name.  For example, if childAttribute is
     * <code>OTF.Instrument</code>, the retrieval will include both the
     * fully-spec'd embedded OTF and Instrument.  names must be specified by
     * their names in the semantic type, not their semantic type names.
     * Calling an invalid name will generate a <code>DSAccessException</code>.
     * 
     * @param typeName The type of attribute to retrieve.
     * @param childAttribute The child attribute tree to retrieve (by name)
     * @param imageID The ID of the image to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified image.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there is a communication error or the
     *                           childAttribute name is invalid.
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        int imageID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves all the Attributes with the given SemanticType that
     * belong to one of the images specified in the list below.
     * @param type The type to retrieve.
     * @param imageIDs A list of Integer objects, indicating which images to
     *                 query.
     * @return A list of all Attributes associated with the specified images.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List retrieveImageAttributes(SemanticType type, List imageIDs)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType,java.util.List)
     */
    public List retrieveImageAttributes(String typeName, List imageIDs)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Returns all the Attributes with the given SemanticType that belong to
     * images with the specified IDs, and include (in the DB call) the attribute
     * with the specified name.  For example, if childAttribute is
     * <code>OTF.Instrument</code>, the retrieval will include both the
     * fully-spec'd embedded OTF and Instrument.  names must be specified by
     * their names in the semantic type, not their semantic type names.
     * Calling an invalid name will generate a <code>DSAccessException</code>.
     * 
     * @param typeName The type of attribute to retrieve.
     * @param childAttribute The child attribute tree to retrieve (by name)
     * @param imageIDs A list of image IDs to query.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified images.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there is a communication error or the
     *                           childAttribute name is invalid.
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        List imageIDs)
        throws DSOutOfServiceException, DSAccessException;    
        
    /**
     * Retrieves all the Attributes with the given SemanticType that
     * belong to a feature with the specified ID.
     * @param type The type of attribute to retrieve.
     * @param featureID The ID of the feature to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified feature.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType,int)
     */
    public List retrieveFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Returns all the Attributes with the given SemanticType that belong to a
     * feature with the specified ID, and include (in the DB call) the attribute
     * with the specified name.  For example, if childAttribute is
     * <code>OTF.Instrument</code>, the retrieval will include both the
     * fully-spec'd embedded OTF and Instrument.  names must be specified by
     * their names in the semantic type, not their semantic type names.
     * Calling an invalid name will generate a <code>DSAccessException</code>.
     * 
     * @param typeName The type of attribute to retrieve.
     * @param childAttribute The child attribute tree to retrieve (by name)
     * @param imageID The ID of the feature to search.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified feature.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there is a communication error or the
     *                           childAttribute name is invalid.
     */
    public List retrieveFeatureAttributes(String typeName, 
    									 String childAttribute, int featureID)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves all the Attributes with the given SemanticType that belong
     * to one of the features specified in the supplied list of feature IDs.
     * @param type The type of attribute to retrieve.
     * @param featureIDs A list of Integer objects, indicating which features
     *                   to query.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to any of the specified features.
     * @throws DSOutOfServiceException If the use ris not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List retrieveFeatureAttributes(SemanticType type, List featureIDs)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * @see retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType,
     *								List)
     */
    public List retrieveFeatureAttributes(String typeName, List featureIDs)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Returns all the Attributes with the given SemanticType that belong to
     * features with the specified IDs, and include (in the DB call) the 
     * attribute with the specified name.  For example, if childAttribute is
     * <code>OTF.Instrument</code>, the retrieval will include both the
     * fully-spec'd embedded OTF and Instrument.  names must be specified by
     * their names in the semantic type, not their semantic type names.
     * Calling an invalid name will generate a <code>DSAccessException</code>.
     * 
     * @param typeName The type of attribute to retrieve.
     * @param childAttribute The child attribute tree to retrieve (by name)
     * @param featureIDs The IDs of the features to query.
     * @return A list of attributes (ordered by attribute ID) of the specified
     *         type that belong to the specified features.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there is a communication error or the
     *                           childAttribute name is invalid.
     */
    public List retrieveFeatureAttributes(String typeName, 
    									String childAttribute, List featureIDs)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieves/fills in a SemanticType with the requisite element and type
     * information, down to one level.  For attribute/ST children, includes
     * the id and the name.
     * 
     * @param type The SemanticType to fill in.
     * @return A filled semantic type from the DB.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public SemanticType retrieveSemanticType(SemanticType type)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * @see retrieveSemanticType(org.openmicroscopy.ds.dto.SemanticType)
     */
    public SemanticType retrieveSemanticType(String typeName)
        throws DSOutOfServiceException, DSAccessException;
        
    /**
     * Uses the AnnotationManager to update/create attributes that require
     * some form of user input record, such as DatasetAnnotations,
     * ImageAnnotations, Classifications, etc.
     * @param attributes The list of attributes to add to the system.
     */
    public void updateUserInputAttributes(List attributes)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Updates (already-created) attributes.
     * @param attributes The list of attributes to update.
     */
    public void updateAttributes(List attributes)
        throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveAttributesByMEXs(String typeName, List mexes)
		throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveTrajectoriesByMEXs(List mexes) 
   		throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveTrajectoryEntriesByMEXs(List mexes) 
		throws DSOutOfServiceException, DSAccessException;
 
    public List retrieveLocationsByFeatureID(List features)
    		throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveExtentsByFeatureID(List features)
        throws DSOutOfServiceException, DSAccessException;
}
