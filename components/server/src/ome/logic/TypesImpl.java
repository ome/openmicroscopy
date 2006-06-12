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
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.api.IPojos;
import ome.api.ITypes;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.containers.Project;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.services.query.CollectionCountQueryDefinition;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
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
    protected final String getName()
    {
        return ITypes.class.getName();
    }

    // ~ Service methods
    // =========================================================================
    
    public Class[] getResultTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Class[] getAnnotationTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Class[] getContainerTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Class[] getPojoTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Class[] getImportTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public IObject[] allEnumerations(Class k)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public IObject getEnumeration(Class k, String string)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Permissions permissions(Class k)
    {
        // TODO Auto-generated method stub
        return null;
        
    }
    
    
}

