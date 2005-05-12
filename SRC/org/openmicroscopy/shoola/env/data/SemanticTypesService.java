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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;

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
     * Gets a list of all available semantic types with image granularity.
     * @return A list of all available image types.
     * @throws DSOutOfServiceException If the user is not logged in or the
     *                                 connection with the server is lost.
     * @throws DSAccessException If there was a communication error.
     */
    public List getAvailableImageTypes()
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
     * @see SemanticTypesService#retrieveDatasetAttributes(
     *          org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
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
    
    //Annotation
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Retrieve the image's annotations.
     * 
     * @param imageID        ID of the image.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public Map getImageAnnotations(int imageID)
        throws DSOutOfServiceException, DSAccessException;  
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Retrieve the dataset's annotations.
     * 
     * @param datasetID        ID of the dataset.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public Map getDatasetAnnotations(int datasetID)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Update the specified image's annotation.
     * 
     * @param AnnotationData data to update.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public boolean updateImageAnnotation(AnnotationData data, int imageID)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Update the specified dataset's annotation.
     * 
     * @param AnnotationData data to update.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public boolean updateDatasetAnnotation(AnnotationData data, int datasetID)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Remove the specified image's annotation.
     * 
     * @param ImageAnnotationData data to remove.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public boolean removeImageAnnotation(AnnotationData data)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Remove the specified dataset's annotation.
     * 
     * @param AnnotationData data to remove.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public boolean removeDatasetAnnotation(AnnotationData data)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Create a new image's annotation.
     * 
     * @param imageID       annotate the specified image.
     * @param annotation    annotation's text.
     * @param theZ          if not equals to {@link #ImageAnnotation.DEFAULT}
     *                      the value is inserted.
     * @param theT          if not equals to {@link #ImageAnnotation.DEFAULT}
     *                      the value is inserted.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public void createImageAnnotation(int imageID, String annotation, int theZ, 
                                        int theT)
        throws DSOutOfServiceException, DSAccessException; 
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Create a new dataset's annotation.
     * 
     * @param datasetID     annotate the specified dataset.
     * @param annotation    annotation's text.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public void createDatasetAnnotation(int datasetID, String annotation)
        throws DSOutOfServiceException, DSAccessException; 

    //Category
    /**
     * Retrieves the all hierarchy Image-Category-Group passing a list of 
     * {@link ImageSummary} objects.
     * B/c of the actual implementation, we have to return a list of 
     * {@link DataObject}, either {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageSummary}.
     * 
     * @param  List of {@link ImageSummary} objects.
     * @param  existing. If <code>true</code>, we retrieve the 
     *         {@link CategoryGroupData}-{@link CategoryData} hierarchy where
     *         the corresponding Categories contain the images.
     *         If <code>false</code>, we retrieve the 
     *         {@link CategoryGroupData}-{@link CategoryData} hierarchy where
     *         the corresponding Categories don't contain the images. 
     *         
     * @return See above.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public List retrieveCGCIHierarchy(List imageSummaries, boolean existing)
        throws DSOutOfServiceException, DSAccessException;
    
    /** 
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Retrieve all category groups.
     * Each {@link CategoryGroupData} object contains a list of 
     * {@link CategoryData} objects. Each {@link CategoryData} object has a map
     * of {@link ClassificationData}.
     * 
     * @param annotated If <code>true</code>, the image Annotations are 
     *          retrieved
     * @param withImages    If <code>true</code>, the images contained in the 
     *                      categories are also retrieved.
     * @return list of {@link CategoryGroupData}s.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveCategoryGroups(boolean annotated, boolean withImages)
        throws DSOutOfServiceException, DSAccessException;  
    
    /** 
     * Retrieve the categories not contained in the specified group, and 
     * containing images which aren't in the categories linked to the group.
     * 
     * @param group specified {@link CategoryGroupData}.
     * @return list of {@link CategorySummary}s.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */ 
    public List retrieveCategoriesNotInGroup(CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException; 
    
    /** 
     * Retrieve a list of images owned by the current user but
     * not contained in the specified category group.
     * 
     * @param group     specified {@link CategoryGroupData}.
     * 
     * @return list of {@link ImageSummary}s not contained in the 
     *          specified group.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException;

    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group, 
            Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveImagesNotInCategoryGroup(int catGroupID, Map filters, 
                                    Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;

    /** 
     * Retrieve a list of images owned by members of the user's group but
     * not contained in the specified category group.
     * 
     * @param group     specified {@link CategoryGroupData}.
     * 
     * @return list of {@link ImageSummary}s not contained in the 
     *          specified group.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveImagesInUserGroupNotInCategoryGroup(
                        CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveImagesInUserGroupNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;
    
    /** 
     * Retrieve a list of images used by the current user but
     * not contained in the specified category group.
     * 
     * @param group     specified {@link CategoryGroupData}.
     * 
     * @return list of {@link ImageSummary}s not contained in the 
     *          specified group.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
                        CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, List datasetsID)
        throws DSOutOfServiceException, DSAccessException;
    
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;

    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, List datasetsID, Map filters, 
            Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;
    
    /** 
     * Retrieve a list of all images but
     * not contained in the specified category group.
     * 
     * @param group     specified {@link CategoryGroupData}.
     * 
     * @return list of {@link ImageSummary}s not contained in the 
     *          specified group.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveImagesInSystemNotInCategoryGroup(
                                        CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException;

    public List retrieveImagesInSystemNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieve a CategoryGroup-Category-Images hierarchy for a
     * specified categoryGroup.
     * The corresponding DataObjects are:
     * {@link CategoryGroupData}-{@link CategoryData}-{@link ImageSummary}.
     * 
     * @param cgID      Specified categoryGroup.
     * @param annotated If <code>true</code>, retrieve the annotation for 
     *                  the images.
     * @return  Return a {@link CategoryGroupData} object or <code>null</code> 
     *          if no CategoryGroup is retrieved.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public CategoryGroupData retrieveCategoryGroupTree(int cgID, 
                                                    boolean annotated)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieve a Category-Images hierarchy for a specified category.
     * The corresponding DataObjects are:
     * {@link CategoryData}-{@link ImageSummary}.
     * 
     * @param cID      Specified category.
     * @param annotated If <code>true</code>, retrieve the annotation for 
     *                  the images.
     * @return  Return a {@link CategoryData} object or <code>null</code> if 
     *          no Category is retrieved.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public CategoryData retrieveCategoryTree(int cID, boolean annotated)
        throws DSOutOfServiceException, DSAccessException;
    
    /**
     * Retrieve the existing CategoryGroups. In this particular case,
     * the Category within the CategoryGroup aren't retrieve.
     * 
     * @return List of CategoryGroupData objects.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public List retrieveAvailableGroups()
        throws DSOutOfServiceException, DSAccessException;
    
    /** 
     * Create a new CategoryGroup.
     * 
     * @param data  {@link CategoryGroupData} object. 
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public CategoryGroupData createCategoryGroup(CategoryGroupData data)
        throws DSOutOfServiceException, DSAccessException; 
    
    /** 
     * Create a new Category.
     * 
     * @param data  {@link CategoryData} object. 
     * @param images List of images to classify.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public CategoryData createCategory(CategoryData data, List images)
        throws DSOutOfServiceException, DSAccessException; 
    
    /** 
     * Update an existing CategoryGroup.
     * 
     * @param data  {@link CategoryGroupData} object. 
     * @param categoriesToAdd List of categories to add.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public void updateCategoryGroup(CategoryGroupData data, 
                                    List categoriesToAdd)
        throws DSOutOfServiceException, DSAccessException; 
    
    /** 
     * Update an existing Category.
     * 
     * @param data  {@link CategoryData} object. 
     * @param imgsToRemove List of image's ids to declassify.
     * @param imgsToAdd List of image's ids to classify.
     * 
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service. 
     */
    public void updateCategory(CategoryData data, List imgsToRemove, 
                                List imgsToAdd)
        throws DSOutOfServiceException, DSAccessException;

    /**
     * Retrieves the common metadata (such as dimensions, type, etc.) associated
     * to a pixels set.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param imageID   The id of the OME image
     * @return An object containing the common pixels metadata.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service.  
     */
    public PixelsDescription retrievePixels(int pixelsID, int imageID)
        throws DSOutOfServiceException, DSAccessException;
    
    
    /**
     * Retrieve the data associated to the channels of a specified image.
     * 
     * @param imageID       image's ID.
     * @return retVal       List of channelData objects.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public ChannelData[] getChannelData(int imageID)
        throws DSOutOfServiceException, DSAccessException;  
    
    /**
     * Update a specified channelData.
     * 
     * @param retVal        ChannelData object to update.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     *         update data from OMEDS service.  
     */
    public void updateChannelData(ChannelData retVal)   
        throws DSOutOfServiceException, DSAccessException;
    
    
    /**
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Retrieve the setting for a specified image and specified set of pixels.
     * If not rendering settings found, return <code>null</code>.
     * 
     * @param pixelsID  set of pixels to take into account
     * @param imageID   imageID
     * @param pixelType 
     * @return
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID, 
                                            int pixelType)
        throws DSOutOfServiceException, DSAccessException;  
    
    /** 
     * NOTE: DON'T CODE AGAINST IT, SHOULD BE MODIFIED
     * Save the rendering settings for the specifed image and set of pixels.
     * 
     * @param pixelsID
     * @param imageID
     * @param rDef
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public void saveRenderingSettings(int pixelsID, int imageID, 
                                    RenderingDef rDef)
        throws DSOutOfServiceException, DSAccessException;  
    
    
}
