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

import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.RemoteAuthenticationException;
import org.openmicroscopy.ds.RemoteConnectionException;
import org.openmicroscopy.ds.RemoteServerErrorException;
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.DataColumn;
import org.openmicroscopy.ds.dto.SemanticElement;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
 * NB: Temporary. DON'T code against it!
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
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
    private DataFactory proxy;
    private Registry context;
    
    private final String GLOBAL_GRANULARITY = "G";
    private final String DATASET_GRANULARITY = "D";
    private final String IMAGE_GRANULARITY = "I";
    private final String FEATURE_GRANULARITY = "F";
    
    private final String GLOBAL_KEY = "id";
    private final String DATASET_KEY = "dataset_id";
    private final String IMAGE_KEY = "image_id";
    private final String FEATURE_KEY = "feature_id";
    
    public STSAdapter(DataFactory proxy, Registry context)
    {
        if(proxy == null || context == null)
        {
            throw new IllegalArgumentException("INTERNAL ERROR: parameters" +
                "cannot be null in STSAdapter constructor.");
        }
        
        this.proxy = proxy;
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
    public int countDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(DATASET_GRANULARITY,datasetID);
        return count(type,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(IMAGE_GRANULARITY,imageID);
        return count(type,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = buildCountCriteria(FEATURE_GRANULARITY,featureID);
        return count(type,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(type,DATASET_GRANULARITY,datasetID);
        return (List)retrieveListData(type,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(type,IMAGE_GRANULARITY,imageID);
        return (List)retrieveListData(type,criteria);
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria =
            buildDefaultRetrieveCriteria(type,FEATURE_GRANULARITY,featureID);
        return (List)retrieveListData(type,criteria);
    }

    /**
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        // cheap trick, I maybe should clear this confusion up (although this
        // is the only way to get global attributes; it applies to any
        // attribute which you select by individual ID)
        Criteria criteria =
            buildDefaultRetrieveCriteria(type,GLOBAL_GRANULARITY,attributeID);
        return (Attribute)retrieveData(type,criteria);
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
    private int count(SemanticType type, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        try
        {
            return proxy.count(type,criteria);
        }
        catch(RemoteConnectionException rce)
        {
            throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
        }
        catch(RemoteAuthenticationException rae)
        {
            throw new DSOutOfServiceException("Not logged in", rae);
        }
        catch(RemoteServerErrorException rsee)
        {
            throw new DSAccessException("Can't count attributes", rsee);
        }
    }
    
    /**
     * Load a single attribute defined by the criteria.
     * 
     * @param type The type of attribute to retrieve.
     * @param criteria The criteria to search by.
     * @return An Attribute of the specified type that adheres to that criteria.
     * @throws DSOutOfServiceException If the connection is broken, or if the
     *                                 user isn't logged in.
     * @throws DSAccessException If a server communication error occurs.
     */
    private Object retrieveData(SemanticType type, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        Object retVal = null;
        
        try
        {
            retVal = proxy.retrieve(type,criteria);
        }
        catch(RemoteConnectionException rce)
        {
            throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
        }
        catch(RemoteAuthenticationException rae)
        {
            throw new DSOutOfServiceException("Not logged in", rae);
        }
        catch(RemoteServerErrorException rsee)
        {
            throw new DSAccessException("Can't load data", rsee);
        }
        return retVal;
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
    private Object retrieveListData(SemanticType type, Criteria criteria)
        throws DSOutOfServiceException, DSAccessException
    {
        Object retVal = null;
        
        try
        {
            retVal = proxy.retrieveList(type,criteria);
        }
        catch(RemoteConnectionException rce)
        {
            throw new DSOutOfServiceException("Can't connect to OMEDS",rce);
        }
        catch(RemoteAuthenticationException rae)
        {
            throw new DSOutOfServiceException("Not logged in",rae);
        }
        catch(RemoteServerErrorException rsee)
        {
            throw new DSAccessException("Can't retrieve data",rsee);
        }
        
        return retVal;
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
        Object retVal = null;
        
        try
        {
            retVal = proxy.retrieveList(typeClass, criteria);
        }
        catch(RemoteConnectionException rce)
        {
            throw new DSOutOfServiceException("Can't connect to OMEDS",rce);
        }
        catch(RemoteAuthenticationException rae)
        {
            throw new DSOutOfServiceException("Not logged in",rae);
        }
        catch(RemoteServerErrorException rsee)
        {
            throw new DSAccessException("Can't retrieve data",rsee);
        }
        
        return retVal;
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
    private Criteria buildDefaultRetrieveCriteria(SemanticType st,
                                                  String granularity,
                                                  int targetID)
    {
        if(st == null)
        {
            return null;
        }
        Criteria criteria = new Criteria();
        List elements = st.getElements();
        
        for(Iterator iter = elements.iterator(); iter.hasNext();)
        {
            SemanticElement element = (SemanticElement)iter.next();
            String elementName = element.getName();
            DataColumn columnType = element.getDataColumn();
            criteria.addWantedField(elementName);
            if(columnType.getSQLType().equals("reference"))
            {
                criteria.addWantedField(elementName,"id");
            }
        }
        
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
    
    /**
     * Returns a Criteria object which will extract the appropriate amount
     * of information to get SemanticTypes.  Passing null will form the
     * Criteria to get all SemanticTypes.
     * 
     * @param granularity The granularity of semantic types to get.
     * @return An appropriate Criteria object.
     */
    private Criteria buildRetrieveTypeCriteria(String granularity)
    {
        if(granularity == null)
        {
            return new Criteria();
        }
        Criteria criteria = new Criteria();
        
        criteria.addWantedField("id");
        criteria.addWantedField("name");
        criteria.addWantedField("description");
        
        criteria.addWantedField("elements");
        criteria.addWantedField("elements","id");
        criteria.addWantedField("elements","name");
        criteria.addWantedField("elements","description");
        criteria.addWantedField("elements","data_column");
        
        criteria.addWantedField("elements.data_column","id");
        criteria.addWantedField("elements.data_column","column_name");
        criteria.addWantedField("elements.data_column","sql_type");
        criteria.addWantedField("elements.data_column","reference_type");
        criteria.addWantedField("elements.data_column","data_table");
        
        criteria.addWantedField("elements.data_column.data_table","id");
        criteria.addWantedField("elements.data_column.data_table","table_name");
        
        criteria.addFilter("granularity",granularity);
        return criteria;
    }
}
