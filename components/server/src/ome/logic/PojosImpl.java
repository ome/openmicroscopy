/*
 * ome.logic.PojosImpl
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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

//Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.api.IPojos;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.containers.Project;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.services.query.CollectionCountQueryDefinition;
import ome.services.query.PojosCGCPathsQueryDefinition;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.services.query.PojosGetUserImagesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.util.CountCollector;
import ome.tools.AnnotationTransformations;
import ome.tools.HierarchyTransformations;
import ome.tools.lsid.LsidUtils;
import ome.util.CBlock;
import ome.util.builders.PojoOptions;


/**
 * implementation of the Pojos service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 2.0
 */
@Transactional
@Stateless
@Remote(IPojos.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IPojos")
@Local(IPojos.class)
@LocalBinding (jndiBinding="omero/local/ome.api.IPojos")
@SecurityDomain("OmeroSecurity")
@Interceptors({SimpleLifecycle.class})
public class PojosImpl extends AbstractLevel2Service implements IPojos 
{

    @Override
    protected final Class<? extends ServiceInterface> getServiceInterface()
    {
        return IPojos.class;
    }
    
    // ~ READ
    // =========================================================================
    
    @RolesAllowed("user") 
    @Transactional(readOnly = true)
    public Set loadContainerHierarchy(Class rootNodeType, 
            Set rootNodeIds, Map options) {
        
        PojoOptions po = new PojoOptions(options);
        
        if (null==rootNodeIds && !po.isExperimenter() && !po.isGroup()) 
        	throw new IllegalArgumentException(
        			"Set of ids for loadContainerHierarchy() may not be null " +
                    "if experimenter and group options are null.");

        if (! Project.class.equals(rootNodeType) 
                && ! Dataset.class.equals(rootNodeType) 
                && ! CategoryGroup.class.equals(rootNodeType) 
                && ! Category.class.equals(rootNodeType))

            throw new IllegalArgumentException(
                "Class parameter for loadContainerIHierarchy() must be in " +
                "{Project,Dataset,Category,CategoryGroup}, not "
                        + rootNodeType);

        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosLoadHierarchyQueryDefinition.class.getName(),
                new Parameters()
                    .addClass(rootNodeType)
                    .addIds(rootNodeIds)
                    .addOptions(po.map())); //TODO no more "options" just QPs.
        List<IObject> l = iQuery.execute(q);

        collectCounts(l, po);
    	
        return new HashSet<IObject>(l);
        
	}
    
    @RolesAllowed("user") 
    @Transactional(readOnly=true)
	public Set findContainerHierarchies(
            final Class rootNodeType, 
            final Set imageIds, 
            Map options) {
		
		PojoOptions po = new PojoOptions(options);
        
        // TODO refactor to use Hierarchy class H.isTopLevel()
        if (!(Project.class.equals(rootNodeType)
                ||CategoryGroup.class.equals(rootNodeType)))
        {
            throw new ApiUsageException( 
                "Class parameter for findContainerHierarchies() must be" +
                " in {Project,CategoryGroup}, not " + rootNodeType);
        }
        
        
        Query<List<Image>> q = getQueryFactory().lookup(
                PojosFindHierarchiesQueryDefinition.class.getName(),
                new Parameters()
                    .addClass(rootNodeType)
                    .addIds(imageIds)
                    .addOptions(po.map()));
        List<Image> l = iQuery.execute(q);
        collectCounts(l,po);


        //
        // Destructive changes below this point.
        //
        @SuppressWarnings("unchecked")
        class EvictBlock<E extends IObject> implements CBlock {
        	public E call(IObject object) {
        		iQuery.evict(object);
        		return (E) object;
        	};
        }

        // TODO; this if-else statement could be removed if Transformations 
        // did their own dispatching 
        // TODO: logging, null checking. daos should never return null 
        // TODO then size!
		if (Project.class.equals(rootNodeType)) {
			if (imageIds.size()==0){
				return new HashSet();
			}
            
			return HierarchyTransformations.invertPDI(new HashSet<Image>(l),
					new EvictBlock<IObject>());
			
		}

		else if (CategoryGroup.class.equals(rootNodeType)){
			if (imageIds.size()==0){
				return new HashSet();
			}
			
			return HierarchyTransformations.invertCGCI(new HashSet<Image>(l),
					new EvictBlock<IObject>()); 
		}
		
		else {
            throw new InternalException("This can't be reached.");
		}
		
	}

    @RolesAllowed("user") 
    @Transactional(readOnly=true)
	public Map findAnnotations(
            Class rootNodeType, 
            Set rootNodeIds, 
            Set annotatorIds, Map options) {
		
        if (rootNodeIds.size()==0)
            return new HashMap();

		PojoOptions po = new PojoOptions(options);
		
        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosFindAnnotationsQueryDefinition.class.getName(),
                new Parameters()
                    .addIds(rootNodeIds)
                    .addClass(rootNodeType)
                    .addSet("annotatorIds",annotatorIds)
                    .addOptions(po.map()));

        List<IObject> l = iQuery.execute(q);
        // no count collection

        //
        // Destructive changes below this point.
        //
        for (Object object : l)
        {
            iQuery.evict(object);    
        }

        // TODO these here or in Query Definition? 
        // Does it belong to API or to query?
        if (Dataset.class.equals(rootNodeType)){ 
            return AnnotationTransformations.sortDatasetAnnotatiosn(
                    new HashSet<IObject>(l));
        } 
        else if (Image.class.equals(rootNodeType)){
            return AnnotationTransformations.sortImageAnnotatiosn(
                    new HashSet<IObject>(l));
        }
        else { 
            throw new IllegalArgumentException(
                    "Class parameter for findAnnotation() must be in " +
                    "{Dataset,Image}, not "+ rootNodeType);
        }
        

	}

    @RolesAllowed("user") 
    @Transactional(readOnly=true)
	public Set findCGCPaths(Set imgIds, 
            String algorithm, Map options) {

		if (imgIds.size()==0){
			return new HashSet();
		}

		if (! IPojos.ALGORITHMS.contains(algorithm)) {
			throw new IllegalArgumentException(
					"No such algorithm known:"+algorithm);
		}
		
		PojoOptions po = new PojoOptions(options);

        Query<List<Map<String,IObject>>> q = getQueryFactory().lookup(
                PojosCGCPathsQueryDefinition.class.getName(),
                new Parameters()
                    .addIds(imgIds)
                    .addAlgorithm(algorithm)
                    .addOptions(po.map()));
        
		List<Map<String,IObject>> result_set = iQuery.execute(q);

        Map<CategoryGroup,Set<Category>> map 
        = new HashMap<CategoryGroup,Set<Category>>();
        Set<CategoryGroup> returnValues = new HashSet<CategoryGroup>();
        
        // Parse
        for (Map<String,IObject> entry : result_set){
            CategoryGroup cg = (CategoryGroup) entry.get(CategoryGroup.class.getName());
            Category c = (Category) entry.get(Category.class.getName());

            if (!map.containsKey(cg)) map.put(cg,new HashSet<Category>());
            if (c != null) map.get(cg).add(c);

        }

        //
        // Destructive changes below this point.
        //
        for (CategoryGroup cg : map.keySet())
        {
            iQuery.evict(cg);
            // Overriding various checks.
            // Ticket #92 :  
            // We know what we're doing so we place a new HashSet here.
            cg.putAt(CategoryGroup.CATEGORYLINKS, new HashSet() );

            for (Category c : map.get(cg))
            {
                iQuery.evict(c);
                // Overriding various checks.
                // Ticket #92 again.  
                c.putAt(Category.CATEGORYGROUPLINKS, new HashSet() );
                cg.linkCategory(c);
            }
            returnValues.add(cg);
        }

        collectCounts(returnValues,po);
        return returnValues;
		
	}

    @RolesAllowed("user") 
    @Transactional(readOnly=true)
	public Set getImages(Class rootNodeType, 
            Set rootNodeIds, Map options) {
		
		if (rootNodeIds.size()==0){
			return new HashSet();
		}

		PojoOptions po = new PojoOptions(options);

        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosGetImagesQueryDefinition.class.getName(),
                new Parameters()
                    .addIds(rootNodeIds)
                    .addClass(rootNodeType)
                    .addOptions(po.map()));

        List<IObject> l = iQuery.execute(q);
        collectCounts(l,po);
        return new HashSet<IObject>(l);
		
	}

    @RolesAllowed("user") 
    @Transactional(readOnly=true)
	public Set getUserImages(Map options) {
		
		PojoOptions po = new PojoOptions(options);
		
		if (!po.isExperimenter() && !po.isGroup()){
			throw new IllegalArgumentException(
					"experimenter or group option " +
                    "is required for getUserImages().");
		}
        
        Query<List<Image>> q = getQueryFactory().lookup(
        		PojosGetUserImagesQueryDefinition.class.getName(),
        		new Parameters()
        			.addOptions(options));

        List<Image> l = iQuery.execute(q);
        collectCounts(l,po);
		return new HashSet<Image>(l);
		
	}
    
    @RolesAllowed("user") 
    @Transactional(readOnly=true)
    public Map getUserDetails(Set names, 
            Map options)
    {
        
        List results;
        Map<String, Experimenter> map = new HashMap<String, Experimenter>();
        
        /* query only if we have some ids */
        if (names.size() > 0)
        {
            Parameters params = new Parameters().addSet("name_list",names);
            results = iQuery.findAllByQuery(
                    "select e from Experimenter e " +
                    "left outer join fetch e.defaultGroupLink dgl " +
                    "left outer join fetch dgl.parent dg " +
                    "left outer join fetch e.groupExperimenterMap gs " +
                    "left outer join fetch gs.child g " +
                    "where e.omeName in ( :name_list )",
                    params
            );
            
            for (Object object : results)
            {
                Experimenter e = (Experimenter) object;
                map.put(e.getOmeName(),e);
            }
        }
        
        /* ensures all ids appear in map */
        for (Object object : names)
        {
            String name = (String) object;
            if (! map.containsKey(name)){
                map.put(name,null);
            }
        }        
        
        return map;
        
    }
    
    @RolesAllowed("user") 
    @Transactional(readOnly=true)
    public Map getCollectionCount(String type, String property, 
            Set ids, Map options)
    {
        
        String parsedProperty = LsidUtils.parseField(property);
        
        checkType(type);
        checkProperty(type,parsedProperty);

        Map<Long,Integer> results = new HashMap<Long,Integer>();
        
        String query = "select size(table."+parsedProperty+") from "+type+" table where table.id = :id";
        // FIXME: optimize by doing new list(id,size(table.property)) ... group by id
        for (Iterator iter = ids.iterator(); iter.hasNext();)
        {
            Long id = (Long) iter.next();
            Query<List<Integer>> q = getQueryFactory().lookup(query,new Parameters().addId(id));
            Integer count = iQuery.execute(q).get(0);
            results.put(id,count);
        }
        
        return results;
    }

    @RolesAllowed("user") 
    @Transactional(readOnly=true)
    public Collection retrieveCollection(IObject arg0, String arg1, Map arg2)
    {
        IObject context = (IObject) iQuery.get(arg0.getClass(),arg0.getId());
        Collection c = (Collection) context.retrieve(arg1); // FIXME not type.o.null safe
        iQuery.initialize(c);
        return c;
    }

    // ~ WRITE
    // =========================================================================
    
    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public IObject createDataObject(IObject arg0, Map arg1)
    {
       IObject retVal = iUpdate.saveAndReturnObject(arg0);
       collectCounts( retVal, new PojoOptions(arg1) );
       return retVal;
    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public IObject[] createDataObjects(IObject[] arg0, Map arg1)
    {
        IObject[] retVal = iUpdate.saveAndReturnArray(arg0);
        collectCounts( retVal, new PojoOptions(arg1) );
        return retVal;

    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public void unlink(ILink[] arg0, Map arg1)
    {
        deleteDataObjects(arg0,arg1);
    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public ILink[] link(ILink[] arg0, Map arg1)
    {
        ILink[] retVal = (ILink[])iUpdate.saveAndReturnArray(arg0);
        collectCounts( retVal, new PojoOptions(arg1) );
        return retVal;

    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public IObject updateDataObject(IObject arg0, Map arg1)
    {
        IObject retVal = iUpdate.saveAndReturnObject(arg0);
        collectCounts( retVal, new PojoOptions(arg1) );
        return retVal;

    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public IObject[] updateDataObjects(IObject[] arg0, Map arg1)
    {
        IObject[] retVal = iUpdate.saveAndReturnArray(arg0);
        collectCounts( retVal, new PojoOptions(arg1) );
        return retVal;
    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public void deleteDataObject(IObject row, Map arg1)
    {
        iUpdate.deleteObject(row);
    }

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
    public void deleteDataObjects(IObject[] rows, Map options)
    {
        for (IObject object : rows)
        {
            deleteDataObject(object,options);    
        }
        
    }

    //  ~ Helpers
    // =========================================================================
    
    /**
     * Determines collection counts for all <code>String[] fields</code> in 
     * the options.
     * 
     * TODO possibly move to CountCollector itself. It'll need an IQuery then.
     * or is it a part of the Pojo QueryDefinitions ?
     */
    private void collectCounts(Object retVal, PojoOptions po)
    {
        if (po.hasCountFields() && po.isCounts())
        {
            CountCollector c = new CountCollector(po.countFields());
            c.collect(retVal);
            for (String key : po.countFields())
            {
                
                if ( key == null 
                        || c.getIds( key ) == null 
                        || c.getIds( key ).size() == 0)
                {
                    getLogger().warn( " Skipping "+key+" in collection counts.");
                    continue;
                }
                
                Query<List<Object[]>> q_c = getQueryFactory().lookup(
                        /* TODO po.map() here */
                        CollectionCountQueryDefinition.class.getName(),
                        new Parameters()
                            .addIds(c.getIds(key))
                            .addString("field",key));
                
                List<Object[]> l_c = iQuery.execute(q_c);
                for (Object[] results : l_c)
                {
                   Long id = (Long) results[0];
                   Long count = (Long) results[1];
                   c.addCounts(key,id,count);
                }
                
            }
        }
    }
    
    final static String alphaNumeric = "^\\w+$";
    final static String alphaNumericDotted = "^\\w[.\\w]+$"; // TODO annotations

    protected void checkType(String type)
    {
        if (!type.matches(alphaNumericDotted))
        {
            throw new IllegalArgumentException(
                    "Type argument to getCollectionCount may ONLY be " +
                    "alpha-numeric with dots ("+alphaNumericDotted+")");
        }

        if (!iQuery.checkType(type)) 
        {
            throw new IllegalArgumentException(type +" is an unknown type.");
        }
    }
    
    protected void checkProperty(String type, String property)
    {
        
        if (!property.matches(alphaNumeric))
        {
            throw new IllegalArgumentException("Property argument to " +
                    "getCollectionCount may ONLY be alpha-numeric ("+
                    alphaNumeric+")");
        }
            
    
        if (!iQuery.checkProperty(type,property))
        {
            throw new IllegalArgumentException(type+"."+property+
                    " is an unknown property on type "+type);
        }
    
    }
    
}
