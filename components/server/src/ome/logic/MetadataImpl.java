/*
 * ome.logic.MetadataImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.logic;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.RolesAllowed;
import ome.annotations.Validate;
import ome.api.IContainer;
import ome.api.IMetadata;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.annotations.Annotation;
import ome.model.containers.Project;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.parameters.Parameters;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.Query;
import ome.util.CBlock;
import ome.util.builders.PojoOptions;


/** 
 * Implement the {@link IMetadata} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MetadataImpl 
	extends AbstractLevel2Service 
	implements IMetadata
{

	/** Qeury to load the original file related to a file annotation. */
	private final String LOAD_ORIGINAL_FILE = 
		"select p from OriginalFile as p left outer join fetch p.format " +
					"where p.id = :id";
	
	/** Identifies the file annotation class. */
	private final String FILE_TYPE = "ome.model.annotations.FileAnnotation";
	
	/** Identifies the tag annotation class. */
	private final String TAG_TYPE = "ome.model.annotations.TagAnnotation";
	
	/** Reference to the {@link IContainer} service. */
	private IContainer iContainer;

	/**
	 * Retrieves the annotation of the given type.
	 * 
	 * @param <A>	  The annotation returned.
	 * @param type    The type of annotation to retrieve.
	 * @param include The collection of name spaces to include.
	 * @param exclude The collection of name spaces to exclude.
	 * @param options The options if any.
	 * @return See above.
	 */
	private <A extends Annotation> List<A> getAnnotation(@NotNull Class type, 
    		Set<String> include, Set<String> exclude, Map options)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select ann from Annotation as ann ");
    	sb.append("left outer join fetch ann.details.creationEvent ");
    	sb.append("left outer join fetch ann.details.owner ");
    	sb.append("where ann member of "+type.getName());
    	
    	String restriction = "";
    	Parameters param = new Parameters();
    	PojoOptions po = new PojoOptions(options);
    	long id = -1;
    	boolean group = false;
    	if (po.getExperimenter() != null) id = po.getExperimenter();
    	if (po.getGroup() != null) {
    		group = true;
    		id = po.getGroup();
    	}
    	if (id < 0) {
    		group = false;
    		id = sec.getEventContext().getCurrentUserId();
    	}
    	if (id >= 0) {
    		if (group) restriction += " and ann.details.group.id = :id";
    		else restriction += " and ann.details.owner.id = :id";
    		param.addId(id);
    	}
    	sb.append(restriction);
    	
    	if (include != null && include.size() > 0) {
    		sb.append(" and ann.ns is not null and ann.ns in (:include)");
    		param.addSet("include", include);
    	}
    	if (exclude != null && exclude.size() > 0) {
    		sb.append(" and (ann.ns is null or ann.ns not in (:exclude))");
    		param.addSet("exclude", exclude);
    	}
    	return iQuery.findAllByQuery(sb.toString(), param);
    }
	
	/**
	 * Returns the Interface implemented by this class.
	 * 
	 * @return See above.
	 */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IMetadata.class;
    }
    
    /**
     * IContainer bean injector. For use during configuration. Can only be called 
     * once.
     * @param iContainer The value to set.
     */
    public final void setIContainer(IContainer iContainer)
    {
        getBeanHelper().throwIfAlreadySet(this.iContainer, iContainer);
        this.iContainer = iContainer;
    }
    
    /**
     * Counts the number of <code>IObject</code>s (Project, Dataset or Image)
     * linked to the specified tag.
     * 
     * @param tagID The id of the tag.
     * @return See above.
     */
    private long countTaggedObjects(long tagID)
    {
    	Parameters param = new Parameters();
    	param.addId(tagID);
    	StringBuilder sb = new StringBuilder();
    	sb.append("select img from Image as img ");
		sb.append("left outer join fetch img.annotationLinks ail ");
		sb.append("where ail.child.id = :id");
		List l = iQuery.findAllByQuery(sb.toString(), param);
		long n = 0; 
		if (l != null) n += l.size();
		sb = new StringBuilder();
    	sb.append("select d from Dataset as d ");
		sb.append("left outer join fetch d.annotationLinks ail ");
		sb.append("where ail.child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) n += l.size();
		sb = new StringBuilder();
    	sb.append("select p from Project as p ");
		sb.append("left outer join fetch p.annotationLinks ail ");
		sb.append("where ail.child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) n += l.size();
		return n;
    }
    
    /**
     * Loads the objects linked to a tag.
     * 
     * @param id 		The id of the tag.
     * @param options 	The options.
     * @return See above.
     */
    private Set<IObject> loadObjects(long id, Map options)
    {
    	Parameters param = new Parameters();
    	param.addId(id);
    	StringBuilder sb = new StringBuilder();
    	Set result = new HashSet();    	//images linked to it.
    	
    	sb.append("select img from Image as img ");
		sb.append("left outer join fetch "
				+ "img.annotationLinksCountPerOwner img_a_c ");
		sb.append("left outer join fetch img.annotationLinks ail ");
		sb.append("left outer join fetch ail.child child ");
		sb.append("left outer join fetch ail.parent parent ");
		sb.append("left outer join fetch img.pixels as pix ");
		sb.append("left outer join fetch pix.pixelsType as pt ");
		sb.append("where child.id = :id");
		List l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) result.addAll(l);
		sb = new StringBuilder();
		sb.append("select d from Dataset as d ");
		sb.append("left outer join fetch "
				+ "d.annotationLinksCountPerOwner d_a_c ");
		sb.append("left outer join fetch d.annotationLinks ail ");
		sb.append("left outer join fetch ail.child child ");
		sb.append("left outer join fetch ail.parent parent ");
		sb.append("where child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) result.addAll(l);
		sb = new StringBuilder();
		sb.append("select p from Project as p ");
		sb.append("left outer join fetch "
				+ "p.annotationLinksCountPerOwner p_a_c ");
		sb.append("left outer join fetch p.annotationLinks ail ");
		sb.append("left outer join fetch ail.child child ");
		sb.append("left outer join fetch ail.parent parent ");
		sb.append("where child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			Set<Long> ids = new HashSet<Long>();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				ids.add(((IObject) i.next()).getId());
			}
			PojoOptions po = new PojoOptions(options);
			po.noLeaves();
			po.noOrphan();
			Set p = iContainer.loadContainerHierarchy(Project.class, ids, 
					po.map());
			result.addAll(p);
		}
		
    	return result;
    }
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadChannelAcquisitionData(Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set loadChannelAcquisitionData(@NotNull 
			@Validate(Long.class) Set<Long> ids)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select channel from LogicalChannel as channel ");
		sb.append("left outer join fetch channel.detectorSettings as ds ");
        sb.append("left outer join fetch channel.lightSourceSettings as lss ");
        sb.append("left outer join fetch ds.detector as detector ");
        sb.append("left outer join fetch detector.type as dt ");
        sb.append("left outer join fetch ds.binning as binning ");
        sb.append("left outer join fetch lss.lightSource as light ");
        sb.append("left outer join fetch light.type as lt ");
        sb.append("where channel.id in (:ids)");
        List<LogicalChannel> list = iQuery.findAllByQuery(sb.toString(), 
        		new Parameters().addIds(ids));
        Iterator<LogicalChannel> i = list.iterator();
        LogicalChannel channel;
        LightSettings light;
        LightSource src;
        IObject object;
        while (i.hasNext()) {
        	channel = i.next();
			light = channel.getLightSourceSettings();
			if (light != null) {
				sb = new StringBuilder();
				src = light.getLightSource();
				if (src instanceof Laser) {
					sb.append("select laser from Laser as laser ");
					sb.append("left outer join fetch laser.type as type ");
					sb.append("left outer join fetch laser.laserMedium as " +
							"medium ");
					sb.append("left outer join fetch laser.pulse as pulse ");
			        sb.append("where laser.id = :id");
				} else if (src instanceof Filament) {
					sb.append("select filament from Filament as filament ");
					sb.append("left outer join fetch filament.type as type ");
			        sb.append("where filament.id = :id");
				} else if (src instanceof Arc) {
					sb.append("select arc from Arc as arc ");
					sb.append("left outer join fetch arc.type as type ");
			        sb.append("where arc.id = :id");
				} else if (src instanceof LightEmittingDiode) {
					sb = null;
				}
				if (sb != null) {
					object = iQuery.findByQuery(sb.toString(), 
			        		new Parameters().addId(src.getId()));
					light.setLightSource((LightSource) object);
				}
			}
		}
    	return new HashSet<LogicalChannel>(list);
    }

    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadAnnotations(Class, Set, Set, Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <T extends IObject, A extends Annotation> 
    	Map<Long, Set<A>> loadAnnotations(
            Class<T> rootNodeType, Set<Long> rootNodeIds, 
            Set<String> annotationTypes, Set<Long> annotatorIds, 
            Map options)
    {
    	 Map<Long, Set<A>> map = new HashMap<Long, Set<A>>();
         if (rootNodeIds.size() == 0)  return map;
         if (!IAnnotated.class.isAssignableFrom(rootNodeType)) {
             throw new ApiUsageException(
                     "Class parameter for loadAnnotation() "
                             + "must be a subclass of ome.model.IAnnotated");
         }

         PojoOptions po = new PojoOptions();

         Query<List<IAnnotated>> q = getQueryFactory().lookup(
                 PojosFindAnnotationsQueryDefinition.class.getName(),
                 new Parameters().addIds(rootNodeIds).addClass(rootNodeType)
                         .addSet("annotatorIds", annotatorIds).addOptions(
                                 po.map()));

         List<IAnnotated> l = iQuery.execute(q);
         // no count collection

         //
         // Destructive changes below this point.
         //
         for (IAnnotated annotated : l) {
             iQuery.evict(annotated);
             annotated.collectAnnotationLinks(new CBlock<ILink>() {

                 public ILink call(IObject object) {
                     ILink link = (ILink) object;
                     iQuery.evict(link);
                     iQuery.evict(link.getChild());
                     return null;
                 }

             });
         }

         // SORT
         Iterator<IAnnotated> i = new HashSet<IAnnotated>(l).iterator();
         IAnnotated annotated;
         Long id; 
         Set<A> set;
         List<A> list;
         List<A> supported;
         Iterator<A> j;
         A object;
         Iterator<A> ann;
         IObject of;
         FileAnnotation fa;
         while (i.hasNext()) {
             annotated = i.next();
             id = annotated.getId();
             set = map.get(id);
             if (set == null) {
                 set = new HashSet<A>();
                 map.put(id, set);
             }
             list = (List<A>) annotated.linkedAnnotationList();
             if (list != null) {
            	 if (annotationTypes != null && annotationTypes.size() > 0) {
            		 j = list.iterator();
            		 supported = new ArrayList<A>();
                	 while (j.hasNext()) {
                		 object = j.next();
                		 if (annotationTypes.contains(
                				 object.getClass().getName())) {
                			 supported.add(object);
                		 }
                	 }
                	 //set.addAll(supported);
            	 } else {
            		 supported = list;
            		 //set.addAll(list);
            	 }
             } else supported = list; //  set.addAll(list);
             ann = supported.iterator();
             while (ann.hasNext()) {
            	 object = ann.next();
            	 //load original file.
            	 if (object instanceof FileAnnotation) {
            		 fa = (FileAnnotation) object;
            		 of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
            				 new Parameters().addId(fa.getFile().getId()));
            		 fa.setFile((OriginalFile) of);
            	 }
             }
             //Archived if no updated script.
            set.addAll(list);
         }
         return map;
    }

    
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadSpecifiedAnnotations(Class, Set, Set, Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <A extends Annotation> Set<A> loadSpecifiedAnnotations(
    		@NotNull Class type, Set<String> include, Set<String> exclude,
    		 Map options)
    {
    	List<A> list = getAnnotation(type, include, exclude, options);
    	if (FILE_TYPE.equals(type.getName()) && list != null) {
    		Iterator<A> i = list.iterator();
    		FileAnnotation fa;
    		OriginalFile of;
    		while (i.hasNext()) {
    			fa = (FileAnnotation) i.next();
    			of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
       				 new Parameters().addId(fa.getFile().getId()));
       		 	fa.setFile(of);
			}
    	}
    	if (list == null) return new HashSet<A>();
    	return new HashSet<A>(list);
    }
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#countSpecifiedAnnotations(Class, Set, Set, Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Long countSpecifiedAnnotations(
    		@NotNull Class type, Set<String> include, Set<String> exclude,
    		 Map options)
    {
    	List list = getAnnotation(type, include, exclude, options);
    	if (list != null) return new Long(list.size());
    	return -1L;
    }
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadAnnotation(Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <A extends Annotation> Set<A> loadAnnotation(
    		@NotNull @Validate(Long.class) Set<Long> annotationIds)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select ann from Annotation as ann ");
    	sb.append("left outer join fetch ann.details.creationEvent ");
    	sb.append("left outer join fetch ann.details.owner ");
    	sb.append("where ann.id in (:ids)");
   
    	List<A> list = iQuery.findAllByQuery(sb.toString(), 
    			new Parameters().addIds(annotationIds));
    	if (list == null) return new HashSet<A>();
    	Iterator<A> i = list.iterator();
    	A object;
    	FileAnnotation fa;
    	Object of;
    	while (i.hasNext()) {
			object =  i.next();
			if (object instanceof FileAnnotation) {
				fa = (FileAnnotation) object;
				of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
						new Parameters().addId(fa.getFile().getId()));
				fa.setFile((OriginalFile) of);
			}
		}
    	return new HashSet<A>(list);
    }
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadTagContent(Set, Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map<Long, Set<IObject>> 
		loadTagContent(@NotNull @Validate(Long.class) Set<Long> tagIds, 
		 Map options)
	{
		Map<Long, Set<IObject>> m = new HashMap<Long, Set<IObject>>();
		Iterator<Long> i = tagIds.iterator();
		Long id;
		while (i.hasNext()) {
			id = i.next();
			m.put(id, loadObjects(id, options));
		}
    	return m;
	}
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadTagSets(Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set<IObject> loadTagSets(Map options)
	{
    	Set result = new HashSet();
    	PojoOptions po = new PojoOptions(options);
    	Parameters param = new Parameters();
    	StringBuilder sb = new StringBuilder();
    	sb.append("select link from AnnotationAnnotationLink as link");
		sb.append(" left outer join fetch link.child child");
		sb.append(" left outer join fetch link.parent parent");
		sb.append(" where child member of "+TAG_TYPE);
		sb.append(" and parent member of "+TAG_TYPE);

    	List l = iQuery.findAllByQuery(sb.toString(), param);
    	List<Long> ids = new ArrayList<Long>();
    	List<Long> children = new ArrayList<Long>();
    	Annotation ann;
		Long id;
		Iterator i;
		AnnotationAnnotationLink link;
    	if (l != null) {
    		i = l.iterator();
    		while (i.hasNext()) {
				link = (AnnotationAnnotationLink) i.next();
				id = link.getId();
				ann = link.parent();
				if (NS_INSIGHT_TAG_SET.equals(ann.getNs())) {
					if (!ids.contains(id)) {
						ids.add(id);
						result.add(link);
					}
				}
				id = link.getChild().getId();
				if (!children.contains(id))
					children.add(id);
			}
    	}
    	
		if (po.isOrphan() && children.size() > 0) {
			sb = new StringBuilder();
			sb.append("select ann from Annotation as ann");
			sb.append(" where ann member of "+TAG_TYPE);
			sb.append(" and ann.id not in (:ids)");
			param = new Parameters();
	    	param.addIds(ids);
			l = iQuery.findAllByQuery(sb.toString(), param);
		    if (l != null) {
		    	i = l.iterator();
	    		while (i.hasNext()) {
					ann = (Annotation) i.next();
					if (!NS_INSIGHT_TAG_SET.equals(ann.getNs()))
						result.add(ann);
	    		}
		    }
		}

		return result;
	}
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#lgetTaggedObjectsCount(Set, Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map getTaggedObjectsCount(@NotNull @Validate(Long.class) 
    		Set<Long> tagIds, Map options)
    {
    	Map<Long, Long> counts = new HashMap<Long, Long>();
    	Iterator<Long> i = tagIds.iterator();
    	Long id;
    	while (i.hasNext()) {
			id = i.next();
			counts.put(id, countTaggedObjects(id));
		}
    	return counts;
    }

    /*


    @RolesAllowed("user")
    @Transactional(readOnly = true) 
    public Map loadTags(long id, boolean withObjects,  Map options)
    {
    	Map m = new HashMap();
    	Annotation annotation;
    	Parameters param = new Parameters();
    	StringBuilder sb = new StringBuilder();
    	sb.append("select ann from Annotation as ann ");
    	if (id >= 0) {
    		sb.append("where ann.id = :id");
    		param.addId(id);
    		annotation = iQuery.findByQuery(sb.toString(), param);
    		if (annotation == null) return m;
    		//make sure it is not a tag set.
    		if (NS_INSIGHT_TAG_SET.equals(annotation.getNs()))
    			return m;
    		m.put(annotation, loadObjects(annotation.getId(), options));
    		return m;
    	}
    	param.addString("ns", NS_INSIGHT_TAG_SET);
    	sb.append(" where ann member of "+TAG_TYPE);
		sb.append(" and ann.nameSpace not like :ns");
		List l = iQuery.findAllByQuery(sb.toString(), param);
		if (l == null || l.size() == 0) return m;
		Iterator i = l.iterator();
		while (i.hasNext()) {
			annotation = (Annotation) i.next();
			if (!NS_INSIGHT_TAG_SET.equals(annotation.getNs())) 
				m.put(annotation, loadObjects(annotation.getId(), options));
		}
    	return m;
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map loadTagSets(long id, boolean withObjects, Map options)
    {
    	Map m = new HashMap();
    	Annotation parent = null;
		Annotation child;
		AnnotationAnnotationLink link;
		Map children;
		List l;
		Iterator i;
    	Parameters param = new Parameters();
    	StringBuilder sb = new StringBuilder();
    	sb.append("select link from AnnotationAnnotationLink as link ");
		sb.append("left outer join link.child child");
		sb.append("left outer join link.parent parent");
		sb.append(" where child member of "+TAG_TYPE);
		
    	if (id >= 0) { //load the specified tag set.
    		param.addId(id);
    		sb.append(" and parent.id = :id");
    		l = iQuery.findAllByQuery(sb.toString(), param);
    		if (l == null || l.size() == 0) return m;
    		children = new HashMap();
    		i = l.iterator();
    		if (withObjects) {
    			while (i.hasNext()) {
					link = (AnnotationAnnotationLink) i.next();
					if (parent == null) parent = link.parent();
					child = link.child();
					if (child != null) 
						children.put(child, loadObjects(child.getId(), options));
				}
    		} else {
    			while (i.hasNext()) {
					link = (AnnotationAnnotationLink) i.next();
					if (parent == null) parent = link.parent();
					child = link.child();
					if (child != null) children.put(child, null);
				}
    			
    		}
    		if (parent != null)
    			m.put(parent, children);
    		return m;
    	}
    	param.addString("ns", NS_INSIGHT_TAG_SET);
    	sb.append(" and parent member of "+TAG_TYPE);
		sb.append(" and parent.nameSpace like :ns");
		
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l == null || l.size() == 0) return m;
		
		i = l.iterator();
		List<Long> parentIds = new ArrayList<Long>();
		long parentID;
		children = null;
		if (withObjects) {
			while (i.hasNext()) {
				link = (AnnotationAnnotationLink) i.next();
				parent = link.parent();
				parentID = parent.getId();
				child = link.child();
				if (!parentIds.contains(parentID)) {
					children = new HashMap();
					m.put(parent, children);
					parentIds.add(parentID);
				}
				if (child != null) 
					children.put(child, loadObjects(child.getId(), options));
				
			}
		} else {
			while (i.hasNext()) {
				link = (AnnotationAnnotationLink) i.next();
				parent = link.parent();
				parentID = parent.getId();
				child = link.child();
				if (!parentIds.contains(parentID)) {
					children = new HashMap();
					m.put(parent, children);
					parentIds.add(parentID);
				}
				if (child != null) 
					children.put(child, null);
			}
		}
    	return m;
    }
     */
}
