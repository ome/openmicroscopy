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

import java.util.List;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;


//Java imports

//Third-party libraries

//Application-internal dependencies

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
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableGlobalTypes()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableDatasetTypes()
     */
    public List getAvailableDatasetTypes()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

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
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableFeatureTypes()
     */
    public List getAvailableFeatureTypes()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countDatasetAttributes(SemanticType type, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(java.lang.String, int)
     */
    public int countDatasetAttributes(String typeName, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countImageAttributes(SemanticType type, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(java.lang.String, int)
     */
    public int countImageAttributes(String typeName, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
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
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countFeatureAttributes(java.lang.String, int)
     */
    public int countFeatureAttributes(String typeName, int featureID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createAttribute(org.openmicroscopy.ds.dto.SemanticType)
     */
    public Attribute createAttribute(SemanticType type)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute createAttribute(SemanticType type, int objectID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createAttribute(java.lang.String)
     */
    public Attribute createAttribute(String typeName)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#createAttribute(java.lang.String, int)
     */
    public Attribute createAttribute(String typeName, int objectID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(java.lang.String, int)
     */
    public Attribute retrieveAttribute(String typeName, int attributeID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
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
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveDatasetAttributes(String typeName,
            String childAttribute, int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageClassifications(java.util.List, int)
     */
    public List retrieveImageClassifications(List imageIDs, int parentDatasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(java.lang.String, int)
     */
    public List retrieveImageAttributes(String typeName, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
            int imageID)
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
    public List retrieveImageAttributes(String typeName, String childAttribute,
            List imageIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, int)
     */
    public List retrieveFeatureAttributes(String typeName, int featureID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveFeatureAttributes(String typeName,
            String childAttribute, int featureID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveFeatureAttributes(SemanticType type, List featureIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.util.List)
     */
    public List retrieveFeatureAttributes(String typeName, List featureIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.lang.String, java.util.List)
     */
    public List retrieveFeatureAttributes(String typeName,
            String childAttribute, List featureIDs)
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
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#updateUserInputAttributes(java.util.List)
     */
    public void updateUserInputAttributes(List attributes)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

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

}
