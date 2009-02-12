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
import ome.api.IMetadata;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.UriAnnotation;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.annotations.Annotation;
import ome.model.core.LogicalChannel;
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

	/**
	 * Returns the Interface implemented by this class.
	 * 
	 * @return See above.
	 */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IMetadata.class;
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
            Set<String> annotationTypes, Set<Long> annotatorIds)
    {
    	 Map<Long, Set<A>> map = new HashMap<Long, Set<A>>();

         if (rootNodeIds.size() == 0) {
             return map;
         }

         if (!IAnnotated.class.isAssignableFrom(rootNodeType)) {
             throw new ApiUsageException(
                     "Class parameter for findAnnotation() "
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
                	 set.addAll(supported);
            	 } else {
            		 set.addAll(list);
            	 }
             } else  set.addAll(list);
             
         }

         return map;
    }
    
    public <A extends Annotation> Set<A> loadSpecifiedAnnotations(
    		@NotNull Class<A> type, String nameSpace, 
    		 @Validate(Long.class) Set<Long> annotatorIds, 
    		 boolean linkedObjects)
    {
    	return new HashSet<A>();
    }
    
    
    
}
