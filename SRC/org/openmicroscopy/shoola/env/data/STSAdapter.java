/*
 * org.openmicroscopy.shoola.env.data.STSAdapter
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.data.map.STSMapper;

/** 
 *  @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">
 *                  jeffm@alum.mit.edu</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class STSAdapter
	implements SemanticTypesService
{
	
    private OMEDSGateway 	gateway;
    
    public STSAdapter(OMEDSGateway gateway)
    {
        this.gateway = gateway;
    }
    
    /** @see SemanticTypesService#getAvailableGlobalTypes(). */
    public List getAvailableGlobalTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildRetrieveTypeCriteria(
        										STSMapper.GLOBAL_GRANULARITY);
        										
        return (List) gateway.retrieveListData(SemanticType.class, criteria);
    }
    
    /** @see SemanticTypesService#getAvailableGlobalTypes(). */
    public List getAvailableDatasetTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildRetrieveTypeCriteria(
        									STSMapper.DATASET_GRANULARITY);
        return (List) gateway.retrieveListData(SemanticType.class, criteria);
    }
    
    /** @see SemanticTypesService#getAvailableGlobalTypes(). */
    public List getAvailableImageTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildRetrieveTypeCriteria(
        									STSMapper.IMAGE_GRANULARITY);
        return (List) gateway.retrieveListData(SemanticType.class, criteria);
    }
    
    /** @see SemanticTypesService#getAvailableGlobalTypes(). */
    public List getAvailableFeatureTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildRetrieveTypeCriteria(
        									STSMapper.FEATURE_GRANULARITY);
        return (List) gateway.retrieveListData(SemanticType.class, criteria);
    }
    
    /**
     * @see SemanticTypesService#countDatasetAttributes(
     * 				org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildCountCriteria(
        								STSMapper.DATASET_GRANULARITY, 
        								datasetID);
        return gateway.countData(typeName, criteria);
    }
    
    /**
     * @see SemanticTypesService#countDatasetAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countDatasetAttributes(type.getName(),datasetID);
    }
    
    /**
     * @see SemanticTypesService#countDatasetAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildCountCriteria(
        							STSMapper.IMAGE_GRANULARITY, imageID);
        return gateway.countData(typeName, criteria);
    }
    
    /**
     * @see SemanticTypesService#countImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countImageAttributes(type.getName(), imageID);
    }
    
    /**
     * @see SemanticTypesService#countImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public int countImageAttributes(String typeName, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDList.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDList.size()];
        imageIDList.toArray(ints);
        Criteria c = STSMapper.buildDefaultCountCriteria(typeName, ints);
        return gateway.countData(typeName, c);
    }
    
    /** @see SemanticTypesService#countImageAttributes(String, List). */
    public int countImageAttributes(SemanticType type, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        return countImageAttributes(type.getName(), imageIDList);
    }

    /**
     * @see SemanticTypesService#countDatasetAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildCountCriteria(
        							STSMapper.FEATURE_GRANULARITY, featureID);
        return gateway.countData(typeName, criteria);
    }
    
    /**
     * @see SemanticTypesService#countFeatureAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countFeatureAttributes(type.getName(), featureID);
    }
    
    /**
     * @see SemanticTypesService#createAttribute(
     * org.openmicroscopy.ds.dto.SemanticType).
     */
    public Attribute createAttribute(SemanticType type)
		throws DSOutOfServiceException, DSAccessException
    {
        return createAttribute(type.getName());
    }
    
    public Attribute createAttribute(SemanticType type, int objectID)
        throws DSOutOfServiceException, DSAccessException
    {
        return createAttribute(type.getName(),objectID);
    }
    
    /** @see SemanticTypesService#createAttribute(String). */
    public Attribute createAttribute(String typeName)
		throws DSOutOfServiceException, DSAccessException
    {
        return gateway.createNewData(typeName);
    }
    
    public Attribute createAttribute(String typeName, int objectID)
        throws DSOutOfServiceException, DSAccessException
    {
        return gateway.createNewData(typeName,objectID);   
    }
    
    /**
     * @see SemanticTypesService#retrieveDatasetAttributes(
     * 			org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        						STSMapper.DATASET_GRANULARITY, datasetID);
		return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveDatasetAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveDatasetAttributes(type.getName(),datasetID);
    }
    
    /**
     * @see SemanticTypesService#retrieveDatasetAttributes(String, String, int).
     */
    public List retrieveDatasetAttributes(String typeName, 
    									String childAttribute, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null) return null;
        Number[] dummyArray = new Number[] {new Integer(datasetID)};
        
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        							STSMapper.DATASET_GRANULARITY,
        							childAttribute, dummyArray);
        
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes
	 * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        						STSMapper.IMAGE_GRANULARITY, imageID);
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImageAttributes(type.getName(), imageID);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(String, String, int).
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        List dummyList = new ArrayList();
        dummyList.add(new Integer(imageID));
        return retrieveImageAttributes(typeName, childAttribute, dummyList);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveImageAttributes(String typeName, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || imageIDs == null || imageIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDs.iterator(); iter.hasNext();) {
            if(!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        					STSMapper.IMAGE_GRANULARITY, ints);
        
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveImageAttributes(SemanticType type, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImageAttributes(type.getName(), imageIDs);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(String, String,
     * 														List).
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || imageIDs == null || imageIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDs.iterator(); iter.hasNext();) {
            if(!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria c = new Criteria();
        if (childAttribute == null)
            c = STSMapper.buildDefaultRetrieveCriteria(
            				STSMapper.IMAGE_GRANULARITY, ints);
        else
            c = STSMapper.buildDefaultRetrieveCriteria(
            				STSMapper.IMAGE_GRANULARITY, childAttribute,ints);
        
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        						STSMapper.FEATURE_GRANULARITY, featureID);
		return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveFeatureAttributes(type.getName(), featureID);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(String, String, int).
     */
    public List retrieveFeatureAttributes(String typeName, 
    									String childAttribute, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        List dummyList = new ArrayList();
        dummyList.add(new Integer(featureID));
        return retrieveFeatureAttributes(typeName, childAttribute, dummyList);
    }

    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveFeatureAttributes(String typeName, List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || featureIDs == null || featureIDs.size() == 0)
            return null;
      
        // test to see if the List is all Integers here
        for (Iterator iter = featureIDs.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[featureIDs.size()];
        featureIDs.toArray(ints);
        
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        					STSMapper.FEATURE_GRANULARITY, ints);
        
		return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveFeatureAttributes(SemanticType type, List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveFeatureAttributes(type.getName(), featureIDs);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(String, String,
     * 														List).
     */
    public List retrieveFeatureAttributes(String typeName, 
    									String childAttribute, List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || featureIDs == null || featureIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = featureIDs.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[featureIDs.size()];
        featureIDs.toArray(ints);
        
        Criteria c = new Criteria();
        if (childAttribute == null)
            c = STSMapper.buildDefaultRetrieveCriteria(
            			STSMapper.FEATURE_GRANULARITY, ints);
        else
            c = STSMapper.buildDefaultRetrieveCriteria(
            			STSMapper.FEATURE_GRANULARITY, childAttribute, ints);
		return (List) gateway.retrieveListSTSData(typeName, c);
    }

    /**
     * @see SemanticTypesService#retrieveAttribute(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public Attribute retrieveAttribute(String typeName, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        // cheap trick, I maybe should clear this confusion up (although this
        // is the only way to get global attributes; it applies to any
        // attribute which you select by individual ID)
        Criteria criteria = STSMapper.buildDefaultRetrieveCriteria(
        							STSMapper.GLOBAL_GRANULARITY, attributeID);
        return (Attribute) gateway.retrieveSTSData(typeName, criteria);
    }
    
    /**
     * @see SemanticTypesService#retrieveAttribute(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveAttribute(type.getName(), attributeID);
    }
    
    /**
     * @see SemanticTypesService#retrieveSemanticType(
     * org.openmicroscopy.ds.dto.SemanticType).
     */
    public SemanticType retrieveSemanticType(SemanticType type)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveSemanticType(type.getName());
    }
    
    /** @see SemanticTypesService#retrieveSemanticType(String). */
    public SemanticType retrieveSemanticType(String typeName)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildRetrieveSingleTypeCriteria(typeName);
        return (SemanticType) gateway.retrieveData(SemanticType.class, c);
    }
    
    /**
     * Updates (new) attributes that have some sort of user input associated
     * with them, such as ImageAnnotation, DatasetAnnotation, Classification and
     * others.  Each user input attribute has an associated ModuleExecution,
     * and updating these attributes requires the AnnotationManager for
     * proper database storage.
     * 
     * @param attributes The list of attributes to update.
     */
    public void updateUserInputAttributes(List attributes)
		throws DSOutOfServiceException, DSAccessException
	{
        gateway.annotateAttributesData(attributes);
    }
    
    /**
     * Updates attributes that have already been created, regardless of
     * creation method.
     */
    public void updateAttributes(List attributes)
        throws DSOutOfServiceException, DSAccessException
    {
        gateway.updateAttributes(attributes);
    }
}
