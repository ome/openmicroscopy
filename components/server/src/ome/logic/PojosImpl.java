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

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.api.Pojos;
import ome.dao.DaoFactory;
import ome.dao.hibernate.queries.PojosQueryBuilder;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Experimenter;
import ome.model.Image;
import ome.model.Project;
import ome.tools.AnnotationTransformations;
import ome.tools.HierarchyTransformations;
import ome.util.builders.PojoOptions;


/**
 * implementation of the Pojos service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 2.0
 */
public class PojosImpl implements Pojos {

    private static Log log = LogFactory.getLog(PojosImpl.class);

    private DaoFactory daos;
    
    public PojosImpl(DaoFactory daoFactory){
    	this.daos = daoFactory;
    }

    private Map<String, Object> getParameters(Collection coll, PojoOptions po){ 
    	Map<String, Object> m = new HashMap<String, Object>();
    	if (null != coll) m.put("id_list",coll);
		//if (po.isExperimenter() && (po.isLeaves() || coll == null)) m.put("exp",po.getExperimenter()); 
        // ignoring unknown parameters for now. need to fix
        if (po.isExperimenter()) m.put("exp",po.getExperimenter()); 
        if (po.isGroup()) m.put("grp",po.getGroup());
    
        // TODO : this needs to be checked against both pojos_macros and pojos_load
        // specifically for variables: noIds, noLeaves, & doExperimenter. Tricky logic.
        // Explanation: When noLeaves then image-based items are turned off. However, when experimenter
        // is on and collection is null, then we have to check the images for their owner. Fallicacy.
        // Possibly need to throw IllegalArgumentException.
        // FIXME this logic really needs to be in XXXDao closer to the queries.
		return m;
    }
    
    public Set loadContainerHierarchy(Class rootNodeType, @Validate(Integer.class) Set rootNodeIds, Map options) {
        
        PojoOptions po = new PojoOptions(options);
        
        if (null==rootNodeIds && !po.isExperimenter() && !po.isGroup()) 
        	throw new IllegalArgumentException(
        			"Set of ids for loadContainerHierarchy() may not be null if experimenter and group options are null.");

        if (Project.class.equals(rootNodeType) ||
        		Dataset.class.equals(rootNodeType) ) {
        	// empty
        }
        		
        else if (CategoryGroup.class.equals(rootNodeType) || 
        		Category.class.equals(rootNodeType)) {
        	// empty
        }
        
        else {
        	throw new IllegalArgumentException(
                "Class parameter for loadContainerIHierarchy() must be in {Project,Dataset,Category,CategoryGroup}, not "
                        + rootNodeType);
        }

        Map m = getParameters(rootNodeIds, po);
    	String q = PojosQueryBuilder.buildLoadQuery(rootNodeType,rootNodeIds==null,po.map());//TODO and rootNodeIds.size()==0
    	List   l = daos.generic().queryListMap(q,m); // TODO make queryList and all generic calls parameterizeable
    	return new HashSet(l);
        
	}
    
	public Set findContainerHierarchies(Class rootNodeType, @NotNull @Validate(Integer.class) Set imageIds, Map options) {
		
        if (null == rootNodeType || null == imageIds)
			throw new IllegalArgumentException(
					"rootNodeType and set of ids for findContainerHierarcheies() may not be null.");

		PojoOptions po = new PojoOptions(options);
        Map m = getParameters(imageIds, po);
    	String q = PojosQueryBuilder.buildFindQuery(rootNodeType,po.map());
    	List   l;
    	
		if (Project.class.equals(rootNodeType)) {
			if (imageIds.size()==0){
				return new HashSet();
			}

			l = daos.generic().queryListMap(q,m);
			return HierarchyTransformations.invertPDI(new HashSet(l)); // logging, null checking. daos should never return null TODO then size!
			
		}

		else if (CategoryGroup.class.equals(rootNodeType)){
			if (imageIds.size()==0){
				return new HashSet();
			}
			
			l = daos.generic().queryListMap(q,m);
			return HierarchyTransformations.invertCGCI(new HashSet(l)); 
			// TODO; this if-else statement could be removed if Transformations did their own dispatching 
		}
		
		else {throw new IllegalArgumentException(
	                "Class parameter for findContainerHierarchies() must be in {Project,CategoryGroup}, not "
	                        + rootNodeType);
		}
		
	}

	public Map findAnnotations(Class rootNodeType, @NotNull @Validate(Integer.class) Set rootNodeIds, Map options) {
		
		if (null == rootNodeIds)
			throw new IllegalArgumentException(
					"Set of ids for findAnnotation() may not be null.");
		
		PojoOptions po = new PojoOptions(options);
		
		if (Dataset.class.equals(rootNodeType)){
			if (rootNodeIds.size()==0){
				return new HashMap();
			}

	        Map m = getParameters(rootNodeIds, po);m.remove("exp");//FIXME
	    	String q = PojosQueryBuilder.buildAnnsQuery(rootNodeType,po.map());
	    	List   l = daos.generic().queryListMap(q,m); 
	    	return AnnotationTransformations.sortDatasetAnnotatiosn(new HashSet(l));

		} 
		
		else if (Image.class.equals(rootNodeType)){
			if (rootNodeIds.size()==0){
				return new HashMap();
			}

	        Map m = getParameters(rootNodeIds, po);m.remove("exp");//FIXME
	    	String q = PojosQueryBuilder.buildAnnsQuery(rootNodeType,po.map());
	    	List   l = daos.generic().queryListMap(q,m); 
	    	return AnnotationTransformations.sortImageAnnotatiosn(new HashSet(l));
		}

		else { 
			throw new IllegalArgumentException(
                "Class parameter for findAnnotation() must be in {Dataset,Image}, not "
                        + rootNodeType);
		}

	}

	public Set findCGCPaths(@NotNull @Validate(Integer.class) Set imgIds, String algorithm, Map options) {
        if (null == imgIds){
			throw new IllegalArgumentException(
					"Set of ids for findCGCPaths() may not be null");
		}
		
		if (imgIds.size()==0){
			return new HashSet();
		}

		if (! Pojos.ALGORITHMS.contains(algorithm)) {
			throw new IllegalArgumentException(
					"No such algorithm known:"+algorithm);
		}
		
		PojoOptions po = new PojoOptions(options);
		
		String q = PojosQueryBuilder.buildPathsQuery(algorithm,po.map());
		Map m = getParameters(imgIds,po);
        
		List<List> result_set = daos.generic().queryListMap(q,m);
        Map<CategoryGroup,Set<Category>> map = new HashMap<CategoryGroup,Set<Category>>();
        Set<CategoryGroup> returnValues = new HashSet<CategoryGroup>();
        
        // Parse
        for (List result_row : result_set)
        {
            CategoryGroup cg = (CategoryGroup) result_row.get(0);
            Category c = (Category) result_row.get(1);

            if (!map.containsKey(cg)) map.put(cg,new HashSet<Category>());
            map.get(cg).add(c);
         
        }
        
        for (CategoryGroup cg : map.keySet())
        {
            cg.setCategories(map.get(cg));
            returnValues.add(cg);
        }
        
		return returnValues;
		
	}

	public Set getImages(Class rootNodeType, @NotNull @Validate(Integer.class) Set rootNodeIds, Map options) {
		
        if (null == rootNodeType || null == rootNodeIds){
			throw new IllegalArgumentException(
					"rootNodeType and set of ids for getImages() may not be null");
		}
		
		if (rootNodeIds.size()==0){
			return new HashSet();
		}

		PojoOptions po = new PojoOptions(options);
		
		String q = PojosQueryBuilder.buildGetQuery(rootNodeType,po.map());
		Map m = getParameters(rootNodeIds,po);
		return new HashSet(daos.generic().queryListMap(q,m));
		
	}

	public Set getUserImages(Map options) {
		
		PojoOptions po = new PojoOptions(options);
		
		if (!po.isExperimenter() && !po.isGroup()){
			throw new IllegalArgumentException(
					"experimenter or group option is required for getUserImages().");
		}
	
		String q = PojosQueryBuilder.buildGetQuery(Image.class,po.map());
		Map m = getParameters(null,po);
		return new HashSet(daos.generic().queryListMap(q,m));
		
	}
    
    /**
     * @DEV.TODO move query to queryBuilder
     */
    public Map getUserDetails(@Validate(String.class) Set names, Map options)
    {
        
        /* test for type guarantee and non-null*/
        for (Object object : names)
        {
            if (!(object instanceof String)){
                throw new IllegalArgumentException("names parameter to getUserDetails may only contain Strings.");    
            }
        }
        
        List results;
        Map<String, Experimenter> map = new HashMap<String, Experimenter>();
        
        /* query only if we have some ids */
        if (names.size() > 0)
        {
            Map<String, Set> params = new HashMap<String, Set>();
            params.put("name_list",names);
        
            results = daos.generic().queryListMap(
                    "select e from Experimenter e left outer join fetch e.group where e.omeName in ( :name_list )",
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
    
    public Map getCollectionCount(@NotNull String type, @NotNull String property, @NotNull @Validate(Integer.class) Set ids, Map options)
    {
        Map results = new HashMap();
        String alphaNumeric = "^\\w+$";
        String alphaNumericDotted = "^\\w[.\\w]+$"; // TODO annotations
        
        if (!type.matches(alphaNumericDotted))
        {
            throw new IllegalArgumentException("Type argument to getCollectionCount may ONLY be alpha-numeric with dots ("+alphaNumericDotted+")");
        }
        
        if (!property.matches(alphaNumeric))
        {
            throw new IllegalArgumentException("Property argument to getCollectionCount may ONLY be alpha-numeric ("+alphaNumeric+")");
        }
                
        if (!daos.generic().checkType(type)) 
        {
            throw new IllegalArgumentException(type+"."+property+" is an unknown type.");
        }
        
        if (!daos.generic().checkProperty(type,property))
        {
            throw new IllegalArgumentException(type+"."+property+" is an unknown property on type "+type);
        }
        
        String query = "select size(table."+property+") from "+type+" table where table.id = ?";
        // FIXME: optimize by doing new list(id,size(table.property)) ... group by id
        for (Iterator iter = ids.iterator(); iter.hasNext();)
        {
            Integer id = (Integer) iter.next();
            Integer count = (Integer) daos.generic().queryUnique(query,new Object[]{id});
            results.put(id,count);
        }
        
        return results;
    }

}

