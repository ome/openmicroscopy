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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory; 
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.config.Registry;

//Java imports

//Third-party libraries

//Application-internal dependencies


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
    private OMEDSGateway gateway;
    private Registry context;
    
    private final String GLOBAL_GRANULARITY = "G";
    private final String DATASET_GRANULARITY = "D";
    private final String IMAGE_GRANULARITY = "I";
    private final String FEATURE_GRANULARITY = "F";
    
    private final String GLOBAL_KEY = "id";
    private final String DATASET_KEY = "dataset_id";
    private final String IMAGE_KEY = "image_id";
    private final String FEATURE_KEY = "feature_id";
    
    public STSAdapter(OMEDSGateway gateway, Registry context)
    {
        if(gateway == null || context == null)
        {
            throw new IllegalArgumentException("INTERNAL ERROR: parameters" +
                "cannot be null in STSAdapter constructor.");
        }
        
        this.gateway = gateway;
        this.context = context;
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableGlobalTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildRetrieveTypeCriteria(GLOBAL_GRANULARITY);
        return (List)retrieveListData(SemanticType.class,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableDatasetTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildRetrieveTypeCriteria(DATASET_GRANULARITY);
        return (List)retrieveListData(SemanticType.class,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableImageTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildRetrieveTypeCriteria(IMAGE_GRANULARITY);
        return (List)retrieveListData(SemanticType.class,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableFeatureTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildRetrieveTypeCriteria(FEATURE_GRANULARITY);
        return (List)retrieveListData(SemanticType.class,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(DATASET_GRANULARITY,datasetID);
        return count(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countDatasetAttributes(type.getName(),datasetID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(IMAGE_GRANULARITY,imageID);
        return count(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countImageAttributes(type.getName(),imageID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public int countImageAttributes(String typeName, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        // test to see if the List is all Integers here
        for(Iterator iter = imageIDList.iterator(); iter.hasNext();)
        {
            if(!(iter.next() instanceof Number))
            {
                throw new IllegalArgumentException("Illegal ID type.");
            }
        }
        
        Integer[] ints = new Integer[imageIDList.size()];
        imageIDList.toArray(ints);
        Criteria criteria =
            buildDefaultCountCriteria(typeName,ints);
        DataFactory proxy = gateway.getDataFactory();
        return proxy.count(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(java.lang.String, java.util.List)
     */
    public int countImageAttributes(SemanticType type, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        return countImageAttributes(type.getName(),imageIDList);
    }


    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(FEATURE_GRANULARITY,featureID);
        return count(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        return countFeatureAttributes(type.getName(),featureID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(DATASET_GRANULARITY,datasetID);
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveDatasetAttributes(type.getName(),datasetID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveDatasetAttributes(String typeName,
                                        String childAttribute,
                                        int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        if(typeName == null)
        {
            return null;
        }
        
        Number[] dummyArray = new Number[] {new Integer(datasetID)};
        
        Criteria criteria =
            buildDefaultRetrieveCriteria(DATASET_GRANULARITY,childAttribute,
                                         dummyArray);
        
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveImageAttributes(String typeName, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(IMAGE_GRANULARITY,imageID);
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImageAttributes(type.getName(),imageID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveImageAttributes(String typeName,
                                        String childAttribute,
                                        int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        List dummyList = new ArrayList();
        dummyList.add(new Integer(imageID));
        return retrieveImageAttributes(typeName,childAttribute,dummyList);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveImageAttributes(String typeName, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if(typeName == null || imageIDs == null || imageIDs.size() == 0)
        {
            return null;
        }
        
        // test to see if the List is all Integers here
        for(Iterator iter = imageIDs.iterator(); iter.hasNext();)
        {
            if(!(iter.next() instanceof Number))
            {
                throw new IllegalArgumentException("Illegal ID type.");
            }
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria criteria =
            buildDefaultRetrieveCriteria(IMAGE_GRANULARITY,ints);
        
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveImageAttributes(SemanticType type, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImageAttributes(type.getName(),imageIDs);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.lang.String, java.util.List)
     */
    public List retrieveImageAttributes(String typeName,
                                        String childAttribute,
                                        List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if(typeName == null || imageIDs == null || imageIDs.size() == 0)
        {
            return null;
        }
        
        // test to see if the List is all Integers here
        for(Iterator iter = imageIDs.iterator(); iter.hasNext();)
        {
            if(!(iter.next() instanceof Number))
            {
                throw new IllegalArgumentException("Illegal ID type.");
            }
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria criteria = new Criteria();
        if(childAttribute == null)
        {
            criteria = buildDefaultRetrieveCriteria(IMAGE_GRANULARITY,ints);
        }
        else
        {
            criteria = buildDefaultRetrieveCriteria(IMAGE_GRANULARITY,
                                                    childAttribute,ints);
        }
        DataFactory proxy = gateway.getDataFactory();
        return (List)proxy.retrieveList(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveFeatureAttributes(String typeName, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(FEATURE_GRANULARITY,featureID);
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveFeatureAttributes(type.getName(),featureID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.lang.String, int)
     */
    public List retrieveFeatureAttributes(String typeName,
                                          String childAttribute,
                                          int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        List dummyList = new ArrayList();
        dummyList.add(new Integer(featureID));
        return retrieveFeatureAttributes(typeName,childAttribute,dummyList);
    }

    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveFeatureAttributes(String typeName, List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if(typeName == null || featureIDs == null || featureIDs.size() == 0)
        {
            return null;
        }
        
        // test to see if the List is all Integers here
        for(Iterator iter = featureIDs.iterator(); iter.hasNext();)
        {
            if(!(iter.next() instanceof Number))
            {
                throw new IllegalArgumentException("Illegal ID type.");
            }
        }
        
        Integer[] ints = new Integer[featureIDs.size()];
        featureIDs.toArray(ints);
        
        Criteria criteria =
            buildDefaultRetrieveCriteria(FEATURE_GRANULARITY,ints);
        
        return (List)retrieveListData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, java.util.List)
     */
    public List retrieveFeatureAttributes(SemanticType type, List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveFeatureAttributes(type.getName(),featureIDs);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(java.lang.String, java.lang.String, java.util.List)
     */
    public List retrieveFeatureAttributes(String typeName,
                                          String childAttribute,
                                          List featureIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if(typeName == null || featureIDs == null || featureIDs.size() == 0)
        {
            return null;
        }
        
        // test to see if the List is all Integers here
        for(Iterator iter = featureIDs.iterator(); iter.hasNext();)
        {
            if(!(iter.next() instanceof Number))
            {
                throw new IllegalArgumentException("Illegal ID type.");
            }
        }
        
        Integer[] ints = new Integer[featureIDs.size()];
        featureIDs.toArray(ints);
        
        Criteria criteria = new Criteria();
        if(childAttribute == null)
        {
            criteria = buildDefaultRetrieveCriteria(FEATURE_GRANULARITY,ints);
        }
        else
        {
            criteria = buildDefaultRetrieveCriteria(FEATURE_GRANULARITY,
                                                    childAttribute,ints);
        }
        DataFactory proxy = gateway.getDataFactory();
        return (List)proxy.retrieveList(typeName,criteria);
    }


    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute retrieveAttribute(String typeName, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        // cheap trick, I maybe should clear this confusion up (although this
        // is the only way to get global attributes; it applies to any
        // attribute which you select by individual ID)
        Criteria criteria =
            buildDefaultRetrieveCriteria(GLOBAL_GRANULARITY,attributeID);
        return (Attribute)retrieveData(typeName,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveAttribute(type.getName(),attributeID);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveSemanticType(org.openmicroscopy.ds.dto.SemanticType)
     */
    public SemanticType retrieveSemanticType(SemanticType type)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveSemanticType(type.getName());
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveSemanticType(java.lang.String)
     */
    public SemanticType retrieveSemanticType(String typeName)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildRetrieveSingleTypeCriteria(typeName);
        
        DataFactory proxy = gateway.getDataFactory();
        return (SemanticType)proxy.retrieve(SemanticType.class,criteria);
    }
    
    /**
     * Returns a count of the number of objects of the specified type that
     * meet the specified criteria.
     * @param type The attribute type to query.
     * @param criteria The search parameters.
     * @return The number of attributes that meet the parameters.
     * @throws DSOutOfServiceException If the connection is broken or if the
     *                                 user is not logged in.
     * @throws DSAccessException If there is a communication or server error.
     */
    private int count(String typeName, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        DataFactory proxy = gateway.getDataFactory();
        return proxy.count(typeName,criteria);
    }
    
    /**
     * Load a single attribute defined by the criteria.
     * 
     * @param typeName The name of attribute to retrieve.
     * @param criteria The criteria to search by.
     * @return An Attribute of the specified type that adheres to that criteria.
     * @throws DSOutOfServiceException If the connection is broken, or if the
     *                                 user isn't logged in.
     * @throws DSAccessException If a server communication error occurs.
     */
    private Object retrieveData(String typeName, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        DataFactory proxy = gateway.getDataFactory();
        return proxy.retrieve(typeName,criteria);
    }
    
    /**
     * Loads the attributes defined by the criteria.
     * @param type The type of attribute to retrieve.
     * @param criteria The criteria to search by.
     * @return All attributes of the specified type that adhere to the
     *         specified criteria.
     * @throws DSOutOfServiceException If the connection is broken, or if the
     *                                 user isn't logged in.
     * @throws DSAccessException If a server communication error occurs.
     */
    private Object retrieveListData(String typeName, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        DataFactory proxy = gateway.getDataFactory();
        return proxy.retrieveList(typeName,criteria);
    }
    
    /**
     * Loads the objects defined by the criteria (there is the same call
     * in DMSAdapter, but it fits well here)
     * 
     * @param typeClass The class of object to retrieve.
     * @param criteria The criteria to search by.
     * @return All objects of the specified type that adhere to the
     *         specified criteria.
     * @throws DSOutOfServiceException If the connection is broken, or if the
     *                                 user isn't logged in.
     * @throws DSAccessException If a server communication error occurs.
     */
    private Object retrieveListData(Class typeClass, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        DataFactory proxy = gateway.getDataFactory();
        return proxy.retrieveList(typeClass, criteria);
    }
    
    /**
     * Returns a Criteria object which contains the amount of information
     * required to call <code>count()</code>: that is, the granularity of
     * the attribute desired and the target ID.
     * 
     * @param granularity The granularity of the attribute to count.
     * @param targetID The ID of the target to count.
     * @return A criteria conforming to the above parameters.
     */
    private Criteria buildCountCriteria(String granularity, int targetID)
    {
        Criteria criteria = new Criteria();
        // TODO fix to actually count
        if(granularity.equals(GLOBAL_GRANULARITY))
        {
            criteria.addFilter(GLOBAL_KEY,new Integer(targetID));
        }
        else if(granularity.equals(DATASET_GRANULARITY))
        {
            criteria.addFilter(DATASET_KEY, new Integer(targetID));
        }
        else if(granularity.equals(IMAGE_GRANULARITY))
        {
            criteria.addFilter(IMAGE_KEY, new Integer(targetID));
        }
        else if(granularity.equals(FEATURE_GRANULARITY))
        {
            criteria.addFilter(FEATURE_KEY, new Integer(targetID));
        }
        else return null;
        return criteria;
    }
    
    /**
     * Returns a Criteria object which contains the default amount of
     * information to be returned in an attribute-- all the primitive fields,
     * and all references with just the ID object returned (such that
     * successive calls may be made to the server to retrieve those attributes
     * as well)
     *
     * @param st The type of object to specify.
     * @param granularity The type of target to specify.
     * @param targetID The ID of the target to filter by.
     * @return A Criteria object with the default depth with the specified
     *         parameters.
     */
    private Criteria buildDefaultRetrieveCriteria(String granularity,
                                                  int targetID)
    {
        Criteria criteria = new Criteria();
        
        // all non-references; has-ones with just ID's; no has-manys
        criteria.addWantedField(":all:");
        
        criteria.addWantedField("semantic_type");
        criteria.addWantedField("semantic_type","semantic_elements");
        criteria.addWantedField("semantic_type.semantic_elements","id");
        criteria.addWantedField("semantic_type.semantic_elements","name");
        criteria.addWantedField("semantic_type.semantic_elements","data_column");
        criteria.addWantedField("semantic_type.semantic_elements.data_column","id");
        criteria.addWantedField("semantic_type.semantic_elements.data_column","sql_type");
        criteria.addWantedField("semantic_type.semantic_elements.data_column","reference_semantic_type");
        
        if(granularity.equals(DATASET_GRANULARITY))
        {
            criteria.addFilter(DATASET_KEY, new Integer(targetID));
        }
        else if(granularity.equals(IMAGE_GRANULARITY))
        {
            criteria.addFilter(IMAGE_KEY, new Integer(targetID));
        }
        else if(granularity.equals(FEATURE_GRANULARITY))
        {
            criteria.addFilter(FEATURE_KEY, new Integer(targetID));
        }
        // the attribute is itself the target in the global case
        else if(granularity.equals(GLOBAL_GRANULARITY))
        {
            criteria.addFilter(GLOBAL_KEY, new Integer(targetID));
        }
        return criteria;
    }
    
    private Criteria buildDefaultCountCriteria(String granularity,
                                               Number[] targetIDs)
        throws IllegalArgumentException
    {
        if(targetIDs == null || targetIDs.length == 0)
        {
            return null;
        }
        Criteria criteria = new Criteria();
        if(granularity.equals(DATASET_GRANULARITY))
        {
            criteria.addFilter(DATASET_KEY, "IN", Arrays.asList(targetIDs));
        }
        else if(granularity.equals(IMAGE_GRANULARITY))
        {
            criteria.addFilter(IMAGE_KEY, "IN", Arrays.asList(targetIDs));
        }
        else if(granularity.equals(FEATURE_GRANULARITY))
        {
            criteria.addFilter(FEATURE_KEY, "IN", Arrays.asList(targetIDs));
        }
        // the attribute is itself the target in the global case
        else if(granularity.equals(GLOBAL_GRANULARITY))
        {
            criteria.addFilter(GLOBAL_KEY, "IN", Arrays.asList(targetIDs));
        }
        return criteria;
    }
    
    /**
     * Retrieves & fills in children.
     * @param granularity The granularity of the ST to query.
     * @param childString The tree of attributes to retrieve (OTF.instrument)
     * @param targetIDs The IDs to target.
     * @return The desired criteria.
     * @throws IllegalArgumentException If something sucks here.
     */
    private Criteria buildDefaultRetrieveCriteria(String granularity,
                                                  String childString,
                                                  Number[] targetIDs)
        throws IllegalArgumentException
    {
        if(targetIDs == null || targetIDs.length == 0)
        {
            return null;
        }
        Criteria criteria = new Criteria();
        
        boolean found = false;
        if(childString.indexOf(".") == -1)
        {
            return buildDefaultRetrieveCriteria(granularity,targetIDs);
        }
        
        childString = childString.substring(childString.indexOf(".")+1);
        
        criteria = buildDefaultRetrieveCriteria(granularity,targetIDs);
        int nextIndex = 0;
        while(!found)
        {
            nextIndex = childString.indexOf(".",nextIndex);
            if(nextIndex == -1)
            {
                criteria.addWantedField(childString,":all:");
                found = true;
            }
            else
            {
                String substr = childString.substring(0,nextIndex);
                criteria.addWantedField(substr,":all:");
            }
            nextIndex++;
        }
        return criteria;
    }
    
    /**
     * Returns a Criteria object which contains the default amount of
     * information to be returned in an attribute-- all the primitive fields,
     * and all references with just the ID object returned (such that
     * successive calls may be made to the server to retrieve those attributes
     * as well)
     *
     * @param st The type of object to specify.
     * @param granularity The type of target to specify.
     * @param targetID The ID of the target to filter by.
     * @return A Criteria object with the default depth with the specified
     *         parameters.
     * @throws IllegalArgumentException if the list of IDs contains invalid
     *                                  objects.
     */
    private Criteria buildDefaultRetrieveCriteria(String granularity,
                                                  Number[] targetIDs)
        throws IllegalArgumentException
    {
        if(targetIDs == null || targetIDs.length == 0)
        {
            return null;
        }
        Criteria criteria = new Criteria();
        
        // all non-references; has-ones with just ID's; no has-manys
        criteria.addWantedField(":all:");
        
        /*criteria.addWantedField("semantic_type");
        criteria.addWantedField("semantic_type","elements");
        criteria.addWantedField("semantic_type.elements","id");
        criteria.addWantedField("semantic_type.elements","name");
        criteria.addWantedField("semantic_type.elements","data_column");
        criteria.addWantedField("semantic_type.elements.data_column","id");
        criteria.addWantedField("semantic_type.elements.data_column","sql_type");
        criteria.addWantedField("semantic_type.elements.data_column","reference_semantic_type");*/

        if(granularity.equals(DATASET_GRANULARITY))
        {
            criteria.addFilter(DATASET_KEY, "IN", Arrays.asList(targetIDs));
        }
        else if(granularity.equals(IMAGE_GRANULARITY))
        {
            criteria.addFilter(IMAGE_KEY, "IN", Arrays.asList(targetIDs));
        }
        else if(granularity.equals(FEATURE_GRANULARITY))
        {
            criteria.addFilter(FEATURE_KEY, "IN", Arrays.asList(targetIDs));
        }
        // the attribute is itself the target in the global case
        else if(granularity.equals(GLOBAL_GRANULARITY))
        {
            criteria.addFilter(GLOBAL_KEY, "IN", Arrays.asList(targetIDs));
        }
        return criteria;
    }
    
    private Criteria buildRetrieveSingleTypeCriteria(String typeName)
    {
        if(typeName == null)
        {
            return new Criteria();
        }
        Criteria criteria = buildBasicTypeCriteria();
        criteria.addFilter("name",typeName);
        return criteria;
    }
    
    private Criteria buildRetrieveTypeCriteria(String granularity)
    {
        if(granularity == null)
        {
            return new Criteria();
        } 
        Criteria criteria = buildBasicTypeCriteria();
        criteria.addFilter("granularity",granularity);
        return criteria;
    }
    
    /**
     * Returns a Criteria object which will extract the appropriate amount
     * of information to get SemanticTypes.  Passing null will form the
     * Criteria to get all SemanticTypes.
     * 
     * @return An appropriate Criteria object.
     */
    private Criteria buildBasicTypeCriteria()
    {
        Criteria criteria = new Criteria();
        
        criteria.addWantedField("id");
        criteria.addWantedField("name");
        criteria.addWantedField("description");
        
        criteria.addWantedField("semantic_elements");
        criteria.addWantedField("semantic_elements","id");
        criteria.addWantedField("semantic_elements","name");
        criteria.addWantedField("semantic_elements","description");
        criteria.addWantedField("semantic_elements","data_column");
        criteria.addWantedField("semantic_elements","semantic_type");
        
        criteria.addWantedField("semantic_elements.data_column","id");
        criteria.addWantedField("semantic_elements.data_column","column_name");
        criteria.addWantedField("semantic_elements.data_column","sql_type");
        criteria.addWantedField("semantic_elements.data_column","reference_semantic_type");
        criteria.addWantedField("semantic_elements.data_column","data_table");
        
        criteria.addWantedField("semantic_elements.data_column.data_table","id");
        criteria.addWantedField("semantic_elements.data_column.data_table","table_name");
        
        criteria.addWantedField("semantic_elements.data_column." +
                                "reference_semantic_type","id");
        criteria.addWantedField("semantic_elements.data_column." +
                                "reference_semantic_type","name");
        
        return criteria;
    }
}
