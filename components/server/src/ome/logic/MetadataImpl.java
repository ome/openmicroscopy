/*
 *   Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

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
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.PlateAnnotationLink;
import ome.model.annotations.ProjectAnnotationLink;
import ome.model.annotations.ScreenAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.annotations.Annotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.fs.Fileset;
import ome.model.screen.Plate;
import ome.model.screen.PlateAcquisition;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.parameters.Parameters;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.services.query.Query;


/** 
 * Implement the {@link IMetadata} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class MetadataImpl 
	extends AbstractLevel2Service 
	implements IMetadata
{

	/** Query to load the original file related to a file annotation. */
	private final String LOAD_ORIGINAL_FILE = 
		"select p from OriginalFile as p left outer join fetch p.hasher where p.id = :id";

    /* HQL to translate given image IDs into corresponding fileset IDs */
    private static final String LOAD_FILESET_OF_IMAGE =
            "SELECT id, fileset.id FROM Image WHERE fileset IS NOT NULL AND id IN (:ids)";

    /* HQL to translate given fileset IDs into corresponding import log IDs */
    private static final String LOAD_IMPORT_LOGS =
            "SELECT fjl.parent.id, o FROM UploadJob u, FilesetJobLink fjl, JobOriginalFileLink jol, OriginalFile o " +
                    "WHERE fjl.parent.id IN (:ids) AND fjl.child = jol.parent AND jol.child.id = o.id AND fjl.child = u AND " +
                    "o.mimetype = '" + "application/omero-log-file" /*PublicRepositoryI.IMPORT_LOG_MIMETYPE*/ + "'";

	/** Identifies the file annotation class. */
	private final String FILE_TYPE = "ome.model.annotations.FileAnnotation";
	
	/** Identifies the tag annotation class. */
	private final String TAG_TYPE = "ome.model.annotations.TagAnnotation";
	
	/** Reference to the {@link IContainer} service. */
	private IContainer iContainer;

	/**
     * Builds the <code>StringBuilder</code> corresponding to the passed 
     * light source.
     * 
     * @param src The light source to handle.
     * @param instrument Pass <code>true</code> for clause on id,
     * 					<code>false</code> for clause on instrument
     * @return See above.
     */
    private StringBuilder createLightQuery(LightSource src, boolean idClause)
    {
    	if (src == null) return null;
    	StringBuilder sb = new StringBuilder();
    	if (src instanceof Laser) {
			sb.append("select l from Laser as l ");
			sb.append("left outer join fetch l.type ");
			sb.append("left outer join fetch l.laserMedium ");
			sb.append("left outer join fetch l.pulse as pulse ");
			//sb.append("left outer join fetch l.pump as pump ");
			if (idClause)
				sb.append("where l.id = :id");
			else sb.append("where l.instrument.id = :instrumentId");
		} else if (src instanceof Filament) {
			sb.append("select l from Filament as l ");
			sb.append("left outer join fetch l.type ");
			if (idClause)
				sb.append("where l.id = :id");
			else sb.append("where l.instrument.id = :instrumentId");
		} else if (src instanceof Arc) {
			sb.append("select l from Arc as l ");
			sb.append("left outer join fetch l.type ");
			if (idClause)
				sb.append("where l.id = :id");
			else sb.append("where l.instrument.id = :instrumentId");
		} else sb = null;
    	return sb;
    }
    
    /**
	 * Retrieves the annotation of the given type.
	 * 
	 * @param type    The type of annotation to retrieve.
	 * @param include The collection of name spaces to include.
	 * @param exclude The collection of name spaces to exclude.
	 * @param rootType The type of objects the annotations are linked to.
	 * @param rootNodeIds The identifiers of the objects.
	 * @param options The options if any.
	 * @return See above.
	 */
	private List<IObject> getAnnotation(@NotNull Class type,
    		Set<String> include, Set<String> exclude, Class rootType,
    		Set<Long> rootNodeIds, Parameters options)
    {
		StringBuilder sb = new StringBuilder();
		if (rootType == null)
			sb.append("select ann from Annotation as ann ");
		else if (Image.class.getName().equals(rootType.getName()))
			sb.append("select l from ImageAnnotationLink as l ");
		else if (Dataset.class.getName().equals(rootType.getName()))
			sb.append("select l from DatasetAnnotationLink as l ");
		else if (Project.class.getName().equals(rootType.getName()))
			sb.append("select l from ProjectAnnotationLink as l ");
		else if (Screen.class.getName().equals(rootType.getName()))
			sb.append("select l from ScreenAnnotationLink as l ");
		else if (Plate.class.getName().equals(rootType.getName()))
			sb.append("select l from PlateAnnotationLink as l ");
		else if (PlateAcquisition.class.getName().equals(rootType.getName()))
			sb.append("select l from PlateAcquisitionAnnotationLink as l ");
		else if (Well.class.getName().equals(rootType.getName()))
			sb.append("select l from WellAnnotationLink as l ");
		else if (Fileset.class.getName().equals(rootType.getName()))
		        sb.append("select l from FilesetAnnotationLink as l ");

		if (rootType != null) {
			sb.append("left outer join fetch l.parent ");
			sb.append("left outer join fetch l.child as ann ");
		}
    	
    	sb.append("left outer join fetch ann.details.creationEvent ");
    	sb.append("left outer join fetch ann.details.owner ");
    	sb.append("where ann member of "+type.getName());
    	
    	Parameters param = new Parameters();
    	Parameters po = new Parameters(options);
    	if (po.getExperimenter() != null) {
    		sb.append(" and ann.details.owner.id = :userId");
    		param.addLong("userId", po.getExperimenter());
    	} 

    	if (include != null && include.size() > 0) {
    		sb.append(" and ann.ns is not null and ann.ns in (:include)");
    		param.addSet("include", include);
    	}
    	if (exclude != null && exclude.size() > 0) {
    		sb.append(" and (ann.ns is null or ann.ns not in (:exclude))");
    		param.addSet("exclude", exclude);
    	}
    	
    	if (rootNodeIds != null && rootNodeIds.size() > 0) {
    		sb.append(" and l.parent.id in (:rootNodeIds)");
    		param.addSet("rootNodeIds", rootNodeIds);
    	}
    	return iQuery.findAllByQuery(sb.toString(), param);
    }
	
	/**
	 * Retrieves the annotation of the given type.
	 * 
	 * @param type    The type of annotation to retrieve.
	 * @param include The collection of name spaces to include.
	 * @param exclude The collection of name spaces to exclude.
	 * @param options The options if any.
	 * @return See above.
	 */
	private List<IObject> getAnnotation(@NotNull Class type, 
    		Set<String> include, Set<String> exclude, Parameters options)
    {
		return getAnnotation(type, include, exclude, null, null, options);
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
		sb = new StringBuilder();
    	sb.append("select p from Screen as p ");
		sb.append("left outer join fetch p.annotationLinks ail ");
		sb.append("where ail.child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) n += l.size();
		sb = new StringBuilder();
    	sb.append("select p from Plate as p ");
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
    private Set<IObject> loadObjects(long id, Parameters options)
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
		sb.append("left outer join fetch child.details.owner ownerChild ");
		sb.append("left outer join fetch parent.details.owner ownerParent ");
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
		sb.append("left outer join fetch child.details.owner ownerChild ");
		sb.append("left outer join fetch parent.details.owner ownerParent ");
		sb.append("where child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null) result.addAll(l);
		
		sb = new StringBuilder();
		sb.append("select pl from Plate as pl ");
		sb.append("left outer join fetch "
				+ "pl.annotationLinksCountPerOwner pl_a_c ");
		sb.append("left outer join fetch pl.annotationLinks ail ");
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
		sb.append("left outer join fetch child.details.owner ownerChild ");
		sb.append("left outer join fetch parent.details.owner ownerParent ");
		sb.append("where child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			Set<Long> ids = new HashSet<Long>();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				ids.add(((IObject) i.next()).getId());
			}
			Parameters po = new Parameters(options);
			po.noLeaves();
			po.noOrphan();
			Set p = iContainer.loadContainerHierarchy(Project.class, ids, po);
			result.addAll(p);
		}
		
		sb = new StringBuilder();
		sb.append("select s from Screen as s ");
		sb.append("left outer join fetch "
				+ "s.annotationLinksCountPerOwner s_a_c ");
		sb.append("left outer join fetch s.annotationLinks ail ");
		sb.append("left outer join fetch ail.child child ");
		sb.append("left outer join fetch ail.parent parent ");
		sb.append("left outer join fetch child.details.owner ownerChild ");
		sb.append("left outer join fetch parent.details.owner ownerParent ");
		sb.append("where child.id = :id");
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			Set<Long> ids = new HashSet<Long>();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				ids.add(((IObject) i.next()).getId());
			}
			Parameters po = new Parameters(options);
			po.noLeaves();
			po.noOrphan();
			Set p = iContainer.loadContainerHierarchy(Screen.class, ids, po);
			result.addAll(p);
		}
    	return result;
    }
    

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Instrument loadInstrument(long id)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select inst from Instrument as inst ");
    	sb.append("left outer join fetch inst.microscope as m ");
    	sb.append("left outer join fetch m.type ");
    	//objective
    	sb.append("left outer join fetch inst.objective as o ");
    	sb.append("left outer join fetch o.immersion ");
    	sb.append("left outer join fetch o.correction ");
    	//detector
    	sb.append("left outer join fetch inst.detector as d ");
    	sb.append("left outer join fetch d.type ");
    	//filter
    	sb.append("left outer join fetch inst.filter as f ");
    	sb.append("left outer join fetch f.type ");
    	sb.append("left outer join fetch f.transmittanceRange as trans ");
    	//filterset
    	sb.append("left outer join fetch inst.filterSet as fs ");
    	sb.append("left outer join fetch fs.dichroic as dichroic ");
    	//dichroic
    	sb.append("left outer join fetch inst.dichroic as di ");
    	//OTF
    	sb.append("left outer join fetch inst.otf as otf ");
    	sb.append("left outer join fetch otf.pixelsType as type ");
    	sb.append("left outer join fetch otf.objective as obj ");
    	sb.append("left outer join fetch obj.immersion ");
    	sb.append("left outer join fetch obj.correction ");
    	sb.append("left outer join fetch otf.filterSet ");
    	
    	//light source
    	sb.append("left outer join fetch inst.lightSource as ls ");
    	sb.append("where inst.id = :id ");
    	
    	Parameters params = new Parameters(); 
    	params.addId(id);
    	Instrument value = iQuery.findByQuery(sb.toString(), params);
    	if (value == null) return null;
    	
    	LightSource ls;
    	Iterator<LightSource> i = value.iterateLightSource();
    	Laser laser;
    	
    	if (i != null) {
    		params = new Parameters();  
			params.addLong("instrumentId", id);
    		List<String> names = new ArrayList<String>();
    		String name;
    		StringBuilder builder;
    		List<IObject> list = new ArrayList<IObject>();
    		while (i.hasNext()) {
    			ls = i.next();
    			if (ls instanceof LightEmittingDiode) {
    				list.add(ls);
    			} else {
    				name = ls.getClass().getName();
    				if (!names.contains(name)) {
        				names.add(name);
        				builder = createLightQuery(ls, false);
        				if (builder != null) {
        					list.addAll(
        						iQuery.findAllByQuery(builder.toString(), 
        								params));
        				} 
        			}
    			}
    		}
    		value.clearLightSource();
    		Iterator<IObject> j = list.iterator();
    		while (j.hasNext()) {
    			value.addLightSource((LightSource) j.next());
			}
    	}
    	return value;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set loadChannelAcquisitionData(@NotNull 
			@Validate(Long.class) Set<Long> ids)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select channel from LogicalChannel as channel ");
    	sb.append("left outer join fetch channel.mode as mode ");
        sb.append("left outer join fetch channel.illumination as illumination ");
        sb.append("left outer join fetch channel.contrastMethod as cm ");
		sb.append("left outer join fetch channel.detectorSettings as ds ");
        sb.append("left outer join fetch channel.lightSourceSettings as lss ");
        sb.append("left outer join fetch lss.microbeamManipulation ");
        
        //
        sb.append("left outer join fetch channel.otf as otf ");
        sb.append("left outer join fetch otf.pixelsType ");
        sb.append("left outer join fetch otf.objective as objective ");
        sb.append("left outer join fetch objective.immersion ");
        sb.append("left outer join fetch objective.correction ");
        sb.append("left outer join fetch otf.filterSet as otffilter ");
        sb.append("left outer join fetch otffilter.dichroic as otfdichroic ");
        
        
        sb.append("left outer join fetch channel.filterSet as filter ");
        sb.append("left outer join fetch filter.dichroic as dichroic ");
        
        //emission filters
        sb.append("left outer join fetch filter.emissionFilterLink as efl ");
        sb.append("left outer join fetch efl.child as ef ");
        sb.append("left outer join fetch ef.transmittanceRange as efTrans ");
        sb.append("left outer join fetch ef.type as type1 ");
        
        //excitation filters
        sb.append("left outer join fetch filter.excitationFilterLink as exfl ");
        sb.append("left outer join fetch exfl.child as exf ");
        sb.append("left outer join fetch exf.transmittanceRange as exfTrans ");
        sb.append("left outer join fetch exf.type as type2 ");
        
        sb.append("left outer join fetch channel.lightPath as lp ");
        sb.append("left outer join fetch lp.dichroic as dichroic ");
        
        //emission filters
        sb.append("left outer join fetch lp.emissionFilterLink as efLpl ");
        sb.append("left outer join fetch efLpl.child as efLp ");
        sb.append("left outer join fetch efLp.transmittanceRange as efLpTrans ");
        sb.append("left outer join fetch efLp.type as type3 ");
        
        //excitation filters
        sb.append("left outer join fetch lp.excitationFilterLink as exfLpl ");
        sb.append("left outer join fetch exfLpl.child as exfLp ");
        sb.append("left outer join fetch exfLp.transmittanceRange as exfLpTrans ");
        sb.append("left outer join fetch exfLp.type as type4 ");

        sb.append("left outer join fetch ds.detector as detector ");
        sb.append("left outer join fetch detector.type ");
        sb.append("left outer join fetch ds.binning as binning ");
        sb.append("left outer join fetch lss.lightSource as light ");
        sb.append("left outer join fetch light.instrument as instrument ");
        sb.append("where channel.id in (:ids)");
        List<LogicalChannel> list = iQuery.findAllByQuery(sb.toString(), 
        		new Parameters().addIds(ids));
        Iterator<LogicalChannel> i = list.iterator();
        LogicalChannel channel;
        LightSettings light;
        LightSource src, pump;
        Parameters params;
        Laser laser;
        while (i.hasNext()) {
        	channel = i.next();
			light = channel.getLightSourceSettings();
			if (light != null) {
				src = light.getLightSource();
				if (!(src instanceof LightEmittingDiode)) {
					sb = createLightQuery(src, true);
					if (sb != null) {
						params = new Parameters(); 
						params.addId(src.getId());
						src = iQuery.findByQuery(sb.toString(), params);
						if (src instanceof Laser) {
							laser = (Laser) src;
							pump = laser.getPump();
							if (pump != null && 
									!(pump instanceof LightEmittingDiode)) {
								params = new Parameters(); 
								params.addId(pump.getId());
								sb = createLightQuery(pump, true);
								if (sb != null)
									laser.setPump((LightSource)
											iQuery.findByQuery(sb.toString(), 
													params));
								light.setLightSource(laser);
							}
						} else light.setLightSource(src);
					}
				}
			}
		}
    	return new HashSet<LogicalChannel>(list);
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <T extends IObject, A extends Annotation> 
    	Map<Long, Set<A>> loadAnnotations(
            Class<T> rootNodeType, Set<Long> rootNodeIds, 
            Set<String> annotationTypes, Set<Long> annotatorIds, 
            Parameters options)
    {
    	 Map<Long, Set<A>> map = new HashMap<Long, Set<A>>();
         if (rootNodeIds.size() == 0)  return map;
         if (!IAnnotated.class.isAssignableFrom(rootNodeType)) {
             throw new ApiUsageException(
                     "Class parameter for loadAnnotation() "
                             + "must be a subclass of ome.model.IAnnotated");
         }

         Parameters po = new Parameters();

         Query<List<IAnnotated>> q = getQueryFactory().lookup(
                 PojosFindAnnotationsQueryDefinition.class.getName(),
                 po.addIds(rootNodeIds).addClass(rootNodeType)
                         .addSet("annotatorIds", annotatorIds));

         List<IAnnotated> l = iQuery.execute(q);
         iQuery.clear();
         // no count collection

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
         OriginalFile of;
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
             supported = new ArrayList<A>();
             if (list != null) {
            	 if (annotationTypes != null && annotationTypes.size() > 0) {
            		 j = list.iterator();
            		 
                	 while (j.hasNext()) {
                		 object = j.next();
                		 if (annotationTypes.contains(
                				 object.getClass().getName())) {
                			 supported.add(object);
                		 }
                	 }
            	 } else {
            		 supported.addAll(list);
            	 }
             } else supported.addAll(list);
             ann = supported.iterator();
             while (ann.hasNext()) {
            	 object = ann.next();
            	 //load original file.
            	 if (object instanceof FileAnnotation) {
            		 fa = (FileAnnotation) object;
            		 if (fa.getFile() != null) {
            			 of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
                				 new Parameters().addId(fa.getFile().getId()));
				 fa.setFile(of);
            		 }
            	 }
             }
             //Archived if no updated script.
            set.addAll(supported);
         }
         return map;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <A extends Annotation> Set<A> loadSpecifiedAnnotations(
    		@NotNull Class type, Set<String> include, Set<String> exclude,
    		 Parameters options)
    {
    	List<IObject> list = getAnnotation(type, include, exclude, options);
    	Iterator<IObject> i;
    	if (FILE_TYPE.equals(type.getName()) && list != null) {
    		i = list.iterator();
    		FileAnnotation fa;
    		OriginalFile of;
    		List<Annotation> toRemove = new ArrayList<Annotation>();
    		while (i.hasNext()) {
    			fa = (FileAnnotation) i.next();
    			if (fa.getFile() != null) {
    				of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
    	       				 new Parameters().addId(fa.getFile().getId()));
    	       		 fa.setFile(of);
    			} else toRemove.add(fa);
			}
    		if (toRemove.size() > 0) list.removeAll(toRemove);
    	}
    	if (list == null) return new HashSet<A>();
    	Set<A> set = new HashSet<A>(list.size());
    	i = list.iterator();
    	while (i.hasNext()) {
			set.add((A) i.next());
		}
    	return set;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Long countSpecifiedAnnotations(
    		@NotNull Class type, Set<String> include, Set<String> exclude,
    		 Parameters options)
    {
    	List list = getAnnotation(type, include, exclude, options);
    	if (list != null) return new Long(list.size());
    	return -1L;
    }

    @Override
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
				if (fa.getFile() != null) {
					of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
							new Parameters().addId(fa.getFile().getId()));
					fa.setFile((OriginalFile) of);
				}
			}
		}
    	return new HashSet<A>(list);
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map<Long, Set<IObject>> 
		loadTagContent(@NotNull @Validate(Long.class) Set<Long> tagIds, 
		 Parameters options)
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

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set<IObject> loadTagSets(Parameters options)
	{
    	Set result = new HashSet();
    	Parameters po = new Parameters(options);
    	Parameters param = new Parameters();
    	StringBuilder sb = new StringBuilder();
    	param.addString("include", NS_INSIGHT_TAG_SET);
    	sb.append("select tag from TagAnnotation as tag ");
		sb.append("left outer join fetch tag.annotationLinks as l ");
		sb.append("left outer join fetch l.parent as parent ");
		sb.append("left outer join fetch l.child as child ");
		sb.append("left outer join fetch child.details.owner as ownerChild ");
		sb.append("left outer join fetch parent.details.owner as ownerParent ");
		sb.append("left outer join fetch tag.details.owner as tagOwner ");
		sb.append("where tag.ns is not null and tag.ns = :include ");
		sb.append("and (l is null or child member of "+TAG_TYPE+")");
		if (po.isExperimenter()) {
			sb.append(" and tagOwner.id = :userID");
			param.addLong("userID", po.getExperimenter());
		}
		
		//All the tags
    	List l = iQuery.findAllByQuery(sb.toString(), param);
    	if (l != null) result.addAll(l);
    	//retrieve the orphan tags.
		if (po.isOrphan()) {
			List<Long> children = new ArrayList<Long>();
			if (l != null) {
				Iterator j = l.iterator();
				TagAnnotation tag;
				List list;
				Iterator k;
				Long id;
				while (j.hasNext()) {
					tag = (TagAnnotation) j.next();
					if (tag.sizeOfAnnotationLinks() > 0) {
						list = tag.linkedAnnotationList();
						k = list.iterator();
						while (k.hasNext()) {
							id = ((IObject) k.next()).getId();
							if (!children.contains(id))
								children.add(id);
						}
					}
				}
			}
			
			
			sb = new StringBuilder();
			param = new Parameters();
			param.addString("include", NS_INSIGHT_TAG_SET);
			sb.append("select ann from TagAnnotation as ann");
			//sb.append(" where ann.ns is null");
			sb.append(" where ((ann.ns is null) or " +
					"(ann.ns is not null and ann.ns != :include)) ");
			if (children.size() > 0) {
				sb.append(" and ann.id not in (:ids)");
				param.addList("ids", children);
			}
			if (po.isExperimenter()) {
				sb.append(" and ann.details.owner.id = :userID");
				param.addLong("userID", po.getExperimenter());
			}
			l = iQuery.findAllByQuery(sb.toString(), param);
		    if (l != null) {
		    	result.addAll(l);
		    }
		}

		return result;
	}
    

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map getTaggedObjectsCount(@NotNull @Validate(Long.class) 
    		Set<Long> tagIds, Parameters options)
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

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set<IObject> loadAnnotationsUsedNotOwned(@NotNull Class annotationType,
    		long userID)
    {
    	Set result = new HashSet();
    	String type = annotationType.getName();
    	List<Long> ids = new ArrayList<Long>();
    	Iterator i;
    	IObject o;
    	Parameters param = new Parameters();
    	param.addLong("userID", userID);
    	List<IObject> l;
    	StringBuffer sb = new StringBuffer();
		sb.append("select link from ImageAnnotationLink as link ");
		sb.append("left outer join fetch link.child child ");
		sb.append("left outer join fetch child.details.owner as co ");
		sb.append("left outer join fetch link.details.owner as lo ");
		sb.append("where co.id != :userID and lo.id = :userID " +
				"and child member of "+type);
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			i = l.iterator();
			ImageAnnotationLink link;
			while (i.hasNext()) {
				link = (ImageAnnotationLink) i.next();
				o = link.getChild();
				if (!ids.contains(o.getId())) {
					result.add(o);
					ids.add(o.getId());
				}
			}
		}
		sb = new StringBuffer();
		sb.append("select link from DatasetAnnotationLink as link ");
		sb.append("left outer join fetch link.child child ");
		sb.append("left outer join fetch child.details.owner as co ");
		sb.append("left outer join fetch link.details.owner as lo ");
		sb.append("where co.id != :userID and lo.id = :userID " +
				"and child member of "+type);
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			i = l.iterator();
			DatasetAnnotationLink link;
			while (i.hasNext()) {
				link = (DatasetAnnotationLink) i.next();
				o = link.getChild();
				if (!ids.contains(o.getId())) {
					result.add(o);
					ids.add(o.getId());
				}
			}
		}
		sb = new StringBuffer();
		sb.append("select link from ProjectAnnotationLink as link ");
		sb.append("left outer join fetch link.child child ");
		sb.append("left outer join fetch child.details.owner as co ");
		sb.append("left outer join fetch link.details.owner as lo ");
		sb.append("where co.id != :userID and lo.id = :userID " +
				"and child member of "+type);
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			i = l.iterator();
			ProjectAnnotationLink link;
			while (i.hasNext()) {
				link = (ProjectAnnotationLink) i.next();
				o = link.getChild();
				if (!ids.contains(o.getId())) {
					result.add(o);
					ids.add(o.getId());
				}
			}
		}
		sb = new StringBuffer();
		sb.append("select link from ScreenAnnotationLink as link ");
		sb.append("left outer join fetch link.child child ");
		sb.append("left outer join fetch child.details.owner as co ");
		sb.append("left outer join fetch link.details.owner as lo ");
		sb.append("where co.id != :userID and lo.id = :userID " +
				"and child member of "+annotationType.getName());
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			i = l.iterator();
			ScreenAnnotationLink link;
			while (i.hasNext()) {
				link = (ScreenAnnotationLink) i.next();
				o = link.getChild();
				if (!ids.contains(o.getId())) {
					result.add(o);
					ids.add(o.getId());
				}
			}
		}
		sb = new StringBuffer();
		sb.append("select link from PlateAnnotationLink as link ");
		sb.append("left outer join fetch link.child child ");
		sb.append("left outer join fetch child.details.owner as co ");
		sb.append("left outer join fetch link.details.owner as lo ");
		sb.append("where co.id != :userID and lo.id = :userID " +
				"and child member of "+annotationType.getName());
		l = iQuery.findAllByQuery(sb.toString(), param);
		if (l != null && l.size() > 0) {
			i = l.iterator();
			PlateAnnotationLink link;
			while (i.hasNext()) {
				link = (PlateAnnotationLink) i.next();
				o = link.getChild();
				if (!ids.contains(o.getId())) {
					result.add(o);
					ids.add(o.getId());
				}
			}
		}
    	return result;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Long countAnnotationsUsedNotOwned(@NotNull Class annotationType, 
    		long userID)
    {
    	Set s = loadAnnotationsUsedNotOwned(annotationType, userID);
    	if (s != null) return new Long(s.size());
    	return -1L;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public <A extends Annotation> Map<Long, Set<A>> loadSpecifiedAnnotationsLinkedTo(
    		@NotNull Class type, Set<String> include, Set<String> exclude,
    		@NotNull Class rootNodeType,
    		@NotNull @Validate(Long.class) Set<Long> rootNodeIds,
    		Parameters options)
    {
    	//First depending on the class
    	List<IObject> list = getAnnotation(type, include, exclude, rootNodeType,
    			rootNodeIds, options);
    	Map<Long, Set<A>> map = new HashMap<Long, Set<A>>(rootNodeIds.size());
    	if (list == null) return map;
    	Iterator<IObject> i = list.iterator();
    	ILink object;
    	Set<A> set;
    	Long parentID;
    	A ann;
    	FileAnnotation fa;
		OriginalFile of;
    	while (i.hasNext()) {
			object = (ILink) i.next();
			parentID = object.getParent().getId();
			set = map.get(parentID);
			if (set == null) {
				set = new HashSet<A>();
				map.put(parentID, set);
			}
			ann = (A) object.getChild();
			if (FILE_TYPE.equals(type.getName())) {
				fa = (FileAnnotation) ann;
				if (fa.getFile() != null) {
					of = iQuery.findByQuery(LOAD_ORIGINAL_FILE, 
							new Parameters().addId(fa.getFile().getId()));
					fa.setFile(of);
				}
			}
			set.add(ann);
		}
    	return map;
    }

    @Override
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map<Long, Set<IObject>> loadLogFiles(
            @NotNull Class<? extends IObject> rootNodeType,
            @Validate(Long.class) Set<Long> ids) {
        final SetMultimap<Long, Long> rootIdByFileset;
        if (Image.class.isAssignableFrom(rootNodeType)) {
            rootIdByFileset = HashMultimap.create();
            if (CollectionUtils.isNotEmpty(ids)) {
                for (final Object[] result : iQuery.projection(LOAD_FILESET_OF_IMAGE,
                        new Parameters().addIds(ids))) {
                    final Long imageId = (Long) result[0];
                    final Long filesetId = (Long) result[1];
                    rootIdByFileset.put(filesetId, imageId);
                }
            }
        } else if (!Fileset.class.isAssignableFrom(rootNodeType)) {
            throw new ApiUsageException("can load log files only by Fileset or Image");
        } else {
            rootIdByFileset = null;
        }
        final SetMultimap<Long, IObject> map = HashMultimap.create();
        final Set<Long> filesetIds = rootIdByFileset == null ? ids : rootIdByFileset.keySet();
        if (CollectionUtils.isNotEmpty(filesetIds)) {
            for (final Object[] result : iQuery.projection(LOAD_IMPORT_LOGS,
                    new Parameters().addIds(filesetIds))) {
                final Long filesetId = (Long) result[0];
                final OriginalFile logFile = (OriginalFile) result[1];
                final Set<Long> mapKeys;
                if (rootIdByFileset == null) {
                    mapKeys = Collections.singleton(filesetId);
                } else {
                    mapKeys = rootIdByFileset.get(filesetId);
                }
                for (final Long mapKey : mapKeys) {
                    map.put(mapKey, logFile);
                }
            }
        }
        /* wrap in a hash map so that ModelMapper may create a new instance */
        return new HashMap<Long, Set<IObject>>(Multimaps.asMap(map));
    }
}
