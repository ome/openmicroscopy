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

    public List getAvailableImageTypes()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public int countImageAttributes(SemanticType type, List imageIDList)
            throws DSOutOfServiceException, DSAccessException
    {
        return 0;
    }

    public int countImageAttributes(String typeName, List imageIDList)
            throws DSOutOfServiceException, DSAccessException
    {
        return 0;
    }

    public List retrieveDatasetAttributes(String typeName, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImageAttributes(SemanticType type, List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImageAttributes(String typeName, List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public SemanticType retrieveSemanticType(SemanticType type)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public SemanticType retrieveSemanticType(String typeName)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public void updateAttributes(List attributes)
            throws DSOutOfServiceException, DSAccessException {}

    public List retrieveAttributesByMEXs(String typeName, List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveTrajectoriesByMEXs(List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveTrajectoryEntriesByMEXs(List mexes)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveLocationsByFeatureID(List features)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveExtentsByFeatureID(List features)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public Map getImageAnnotations(int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public Map getDatasetAnnotations(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public boolean updateImageAnnotation(AnnotationData data, int imgID)
            throws DSOutOfServiceException, DSAccessException
    {
        return true;
    }

    public boolean updateDatasetAnnotation(AnnotationData data, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return true;
    }

    public boolean removeImageAnnotation(AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        return true;
    }

    public boolean removeDatasetAnnotation(AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        return true;
    }

    public void createImageAnnotation(int imageID, String annotation, int theZ,
                                        int theT)
            throws DSOutOfServiceException, DSAccessException {}

    public void createDatasetAnnotation(int datasetID, String annotation)
            throws DSOutOfServiceException, DSAccessException {}

    public CategoryGroupData createCategoryGroup(CategoryGroupData data)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public CategoryData createCategory(CategoryData data, List images)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public void updateCategoryGroup(CategoryGroupData data,
                                    List categoriesToAdd)
            throws DSOutOfServiceException, DSAccessException {}
    
    public void updateCategory(CategoryData data, List imgsToRemove,
                                List imgsToAdd)
            throws DSOutOfServiceException, DSAccessException {}

    public List retrieveCategoriesNotInGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public PixelsDescription retrievePixels(int pixelsID, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ChannelData[] getChannelData(int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public void updateChannelData(ChannelData retVal)
            throws DSOutOfServiceException, DSAccessException {}

    public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID,
                                                int pixelType)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public void saveRenderingSettings(int pixelsID, int imageID,
            RenderingDef rDef)
            throws DSOutOfServiceException, DSAccessException {}

    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserGroupNotInCategoryGroup(
            CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInSystemNotInCategoryGroup(
            CategoryGroupData group)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, List datasetsID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesNotInCategoryGroup(int catGroupID, Map filters,
            Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesNotInCategoryGroup(CategoryGroupData group,
            Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveImagesInUserGroupNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasetsNotInCategoryGroup(
            CategoryGroupData group, List datasetsID, Map filters, 
            Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInSystemNotInCategoryGroup(
            CategoryGroupData group, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public CategoryGroupData retrieveCategoryGroupTree(int cgID,
                                                    boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public CategoryData retrieveCategoryTree(int cID, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveCGCIHierarchy(List imageSummaries, boolean in)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveCategoryGroups(boolean annotated, boolean in)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveAvailableGroups()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
}
