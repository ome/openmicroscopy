/*
 * ome.logic.TypesImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.api.IPojos;
import ome.api.ITypes;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.containers.Project;
import ome.model.enums.EventType;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.query.CollectionCountQueryDefinition;
import ome.services.query.Definitions;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.services.util.CountCollector;
import ome.tools.AnnotationTransformations;
import ome.tools.HierarchyTransformations;
import ome.tools.lsid.LsidUtils;
import ome.util.builders.PojoOptions;


/**
 * implementation of the ITypes service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
@Transactional
public class TypesImpl extends AbstractLevel2Service implements ITypes
{

    private static Log log = LogFactory.getLog(TypesImpl.class);

    @Override
    protected final Class<? extends ServiceInterface> getServiceInterface()
    {
        return ITypes.class;
    }

    // ~ Service methods
    // =========================================================================
    
    public <T extends IEnum> List<T> allEnumerations(Class<T> k)
    {
        return iQuery.findAll(k,null);
    }

    public <T extends IEnum> T getEnumeration(Class<T> k, String string)
    {
        IEnum e = iQuery.findByString(k,"value",string);
        iQuery.initialize(e);
        if ( e == null )
        {
        	throw new ApiUsageException(String.format(
        			"An %s enum does not exist with the value: %s",
        			k.getName(),string));
        }
        return k.cast(e);
    }

    public <T extends IObject> List<Class<T>> getResultTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public <T extends IObject> List<Class<T>> getAnnotationTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public <T extends IObject> List<Class<T>> getContainerTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public <T extends IObject> List<Class<T>> getPojoTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public <T extends IObject> List<Class<T>> getImportTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public <T extends IObject> Permissions permissions(Class<T> k)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

}

