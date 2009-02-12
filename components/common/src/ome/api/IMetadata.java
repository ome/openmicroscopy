/*
 * ome.api.IMetadata 
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
package ome.api;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.core.LogicalChannel;


/** 
 * Provides method to interact with acquisition metadata and 
 * annotations.
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
public interface IMetadata 
	extends ServiceInterface
{

	/** The name space indicating that the tag is used a tag set. */
	public static final String NS_INSIGHT_TAG_SET = 
		"openmicroscopy.org/omero/insight/tagset";
	
	 /**
     * The name space indicating that the <code>Long</code> annotation is
     * a rating annotation i.e. an integer in the interval <code>[0, 5]</code>.
     */
    public static final String NS_INSIGHT_RATING = 
    	"openmicroscopy.org/omero/insight/rating";
    
    /**
     * The name space indicating that the <code>Boolean</code> annotation 
     * indicated if an archived image is imported with the image. 
     */
    public static final String NS_IMPORTER_ARCHIVED = 
    	"openmicroscopy.org/omero/importer/archived";
    
	/**
	 * Loads the <code>logical channels</code> and the acquisition metadata 
	 * related to them.
	 * 
	 * @param ids The collection of logical channel's ids. 
	 * 		      Mustn't be <code>null</code>.
	 * @return The collection of loaded logical channels.
	 */
	public Set<LogicalChannel> loadChannelAcquisitionData(@NotNull 
			@Validate(Long.class) Set<Long> ids);
	
    /**
     * Loads all the annotations of given types, 
     * that have been attached to the specified <code>rootNodes</code> 
     * for the specified <code>annotatorIds</code>.
     * If no types specified, all annotations will be loaded.
     * This method looks for the annotations that have been attached to each of
     * the specified objects. It then maps each <code>rootNodeId</code> onto
     * the set of annotations that were found for that node. If no
     * annotations were found for that node, then the entry will be
     * <code>null</code>. Otherwise it will be a <code>Set</code>
     * containing {@link Annotation} objects.
     * 
     * @param nodeType The type of the nodes the annotations are linked to. 
     *                 Mustn't be <code>null</code>.
     * @param nodeIds  Ids of the objects of type <code>rootNodeType</code>.
     * 				   Mustn't be <code>null</code>.
     * @param annotationType The types of annotation to retrieve. 
     * 						 If <code>null</code> all annotations will be
     *                       loaded. String of the type
     *                       <code>ome.model.annotations.*</code>.
     * @param annotatorIds Ids of the users for whom annotations should be 
     *                     retrieved. 
     *                     If <code>null</code>, all annotations returned.
     * @return A map whose key is rootNodeId and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     */
    public <T extends IObject, A extends Annotation> 
    Map<Long, Set<A>> loadAnnotations(
            @NotNull Class<T> nodeType, @NotNull @Validate(Long.class)
            Set<Long> rootNodeIds, @NotNull @Validate(String.class) 
            Set<String> annotationType,
            @Validate(Long.class) Set<Long> annotatorIds);
    
    /**
     * Loads the annotations of a given type.
     * 
     * @param type      The type of annotations to load.
     * @param nameSpace The name space, one of the constants defined by 
     *                  this class or <code>null</code>.
     * @param annotatorIds Ids of the users for whom annotations should be 
     *                     retrieved. 
     * @param linkedObjects Pass <code>true</code> to load the 
     * 						<code>IObject</code> related to the annotations.
     * @return A collection of found annotations.
     */
    public <A extends Annotation> Set<A> loadSpecifiedAnnotations(
    		@NotNull Class<A> type, String nameSpace, 
    		 @Validate(Long.class) Set<Long> annotatorIds, 
    		 boolean linkedObjects);
    
}
