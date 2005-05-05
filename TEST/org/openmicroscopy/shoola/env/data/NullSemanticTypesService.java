/*
 * org.openmicroscopy.shoola.env.data.NullSemanticTypesService
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
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullSemanticTypesService
        implements SemanticTypesService
{

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableImageTypes()
     */
    public List getAvailableImageTypes()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public int countImageAttributes(SemanticType type, List imageIDList)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(java.lang.String, java.util.List)
     */
    public int countImageAttributes(String typeName, List imageIDList)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(java.lang.String, int)
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveImageAttributes(SemanticType type, List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(java.lang.String, java.util.List)
     */
    public List retrieveImageAttributes(String typeName, List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(java.lang.String, java.lang.String, java.util.List)
     */
    public List retrieveImageAttributes(String typeName, String childAttribute, List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveSemanticType(org.openmicroscopy.ds.dto.SemanticType)
     */
    public SemanticType retrieveSemanticType(SemanticType type)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveSemanticType(java.lang.String)
     */
    public SemanticType retrieveSemanticType(String typeName)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateAttributes(java.util.List)
     */
    public void updateAttributes(List attributes)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttributesByMEXs(java.lang.String, java.util.List)
     */
    public List retrieveAttributesByMEXs(String typeName, List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveTrajectoriesByMEXs(java.util.List)
     */
    public List retrieveTrajectoriesByMEXs(List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveTrajectoryEntriesByMEXs(java.util.List)
     */
    public List retrieveTrajectoryEntriesByMEXs(List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveLocationsByFeatureID(java.util.List)
     */
    public List retrieveLocationsByFeatureID(List features)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveExtentsByFeatureID(java.util.List)
     */
    public List retrieveExtentsByFeatureID(List features)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getImageAnnotations(int)
     */
    public Map getImageAnnotations(int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getDatasetAnnotations(int)
     */
    public Map getDatasetAnnotations(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateImageAnnotation(org.openmicroscopy.shoola.env.data.model.AnnotationData)
     */
    public boolean updateImageAnnotation(AnnotationData data, int imgID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateDatasetAnnotation(org.openmicroscopy.shoola.env.data.model.AnnotationData)
     */
    public boolean updateDatasetAnnotation(AnnotationData data, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#removeImageAnnotation(org.openmicroscopy.shoola.env.data.model.AnnotationData)
     */
    public boolean removeImageAnnotation(AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#removeDatasetAnnotation(org.openmicroscopy.shoola.env.data.model.AnnotationData)
     */
    public boolean removeDatasetAnnotation(AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createImageAnnotation(int, java.lang.String, int, int)
     */
    public void createImageAnnotation(int imageID, String annotation, int theZ, int theT)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createDatasetAnnotation(int, java.lang.String)
     */
    public void createDatasetAnnotation(int datasetID, String annotation)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public CategoryGroupData createCategoryGroup(CategoryGroupData data)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createCategory(org.openmicroscopy.shoola.env.data.model.CategoryData, java.util.List)
     */
    public CategoryData createCategory(CategoryData data, List images)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.List)
     */
    public void updateCategoryGroup(CategoryGroupData data, List categoriesToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateCategory(org.openmicroscopy.shoola.env.data.model.CategoryData, java.util.List, java.util.List)
     */
    public void updateCategory(CategoryData data, List imgsToRemove, List imgsToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveICGHierarchy(java.util.List)
     */
    public List retrieveCGCIHierarchy(List imageSummaries)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveCategoriesNotInGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public List retrieveCategoriesNotInGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrievePixels(int, int)
     */
    public PixelsDescription retrievePixels(int pixelsID, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getChannelData(int)
     */
    public ChannelData[] getChannelData(int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateChannelData(org.openmicroscopy.shoola.env.data.model.ChannelData)
     */
    public void updateChannelData(ChannelData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveRenderingSettings(int, int, int)
     */
    public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID, int pixelType)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#saveRenderingSettings(int, int, org.openmicroscopy.shoola.env.rnd.defs.RenderingDef)
     */
    public void saveRenderingSettings(int pixelsID, int imageID, RenderingDef rDef)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesNotInCategoryGroup(int)
     */
    public List retrieveImagesNotInCategoryGroup(int catGroupID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrievesImagesInUserGroupNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public List retrieveImagesInUserGroupNotInCategoryGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrievesImagesInUserDatasetsNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrievesImagesInSystemNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData)
     */
    public List retrieveImagesInSystemNotInCategoryGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesInUserDatasetsNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.List)
     */
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group, List datasetsID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesNotInCategoryGroup(int, java.util.Map, java.util.Map)
     */
    public List retrieveImagesNotInCategoryGroup(int catGroupID, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.Map, java.util.Map)
     */
    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesInUserGroupNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserGroupNotInCategoryGroup(CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesInUserDatasetsNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesInUserDatasetsNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.List, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group, List datasetsID, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImagesInSystemNotInCategoryGroup(org.openmicroscopy.shoola.env.data.model.CategoryGroupData, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInSystemNotInCategoryGroup(CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveCategoryGroups(boolean)
     */
    public List retrieveCategoryGroups(boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveCategoryGroupTree(int, boolean)
     */
    public CategoryGroupData retrieveCategoryGroupTree(int cgID, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveCategoryTree(int, boolean)
     */
    public CategoryData retrieveCategoryTree(int cID, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveCategoryGroupsWithoutImages()
     */
    public List retrieveCategoryGroupsWithoutImages()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
