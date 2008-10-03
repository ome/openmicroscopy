/*
 * org.openmicroscopy.shoola.env.data.views.DataHandlerView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views;



//Java imports
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
* Provides methods to support annotation.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public interface DataHandlerView
	extends DataServicesView
{

	/** 
	 * Creates an annotation of the specified type for the specified node.
	 * 
	 * @param annotatedObject   The <code>DataObject</code> to annotate.
	 *                          One of the following type:
	 *                          <code>DatasetData</code>,
	 *                          <code>ImageData</code>.   
	 *                          Mustn't be <code>null</code>.
	 * @param data              The annotation to create.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle createAnnotation(DataObject annotatedObject,
			AnnotationData data,  AgentEventListener observer);

	/**
	 * Updates the specified annotation.
	 * 
	 * @param annotatedObject   The annotated <code>DataObject</code>.
	 *                          One of the following type:
	 *                          <code>DatasetData</code>,
	 *                          <code>ImageData</code>.   
	 *                          Mustn't be <code>null</code>.
	 * @param data              The Annotation object to update.
	 *                          Mustn't be <code>null</code>.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateAnnotation(DataObject annotatedObject,
			AnnotationData data, AgentEventListener observer);

	/**
	 * Deletes the specified annotations.
	 * 
	 * @param annotatedObject   The annotated <code>DataObject</code>.
	 *                          One of the following type:
	 *                          <code>DatasetData</code>,
	 *                          <code>ImageData</code>.   
	 *                          Mustn't be <code>null</code>.
	 * @param data              Collection of annotation objects to delete. 
	 * 
	 *                          Mustn't be <code>null</code>.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle deleteAnnotation(DataObject annotatedObject,
							List data, AgentEventListener observer);
	
	/**
	 * Deletes the specified annotation.
	 * 
	 * @param annotatedObject   The annotated <code>DataObject</code>.
	 *                          Mustn't be <code>null</code>.
	 * @param data              The annotation to remove. 
	 *                          Mustn't be <code>null</code>.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle deleteAnnotation(DataObject annotatedObject,
							AnnotationData data, AgentEventListener observer);

	/** 
	 * Creates an annotation of the specified type for the specified node.
	 * 
	 * @param annotatedObjects  The <code>DataObject</code>s to annotate. 
	 *                          Mustn't be <code>null</code>.
	 * @param data              The annotation to create.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle createAnnotation(Set annotatedObjects, 
			AnnotationData data,  
			AgentEventListener observer);

	/**
	 * Updates the specified annotation.
	 * 
	 * @param annotatedObjects  The annotated <code>DataObject</code>s. 
	 *                          Mustn't be <code>null</code>.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateAnnotation(Map annotatedObjects,
			AgentEventListener observer);

	/**
	 * Updates and creates the specified annotation.
	 * 
	 * @param toUpdate  		The annotated <code>DataObject</code>s.
	 *                          Mustn't be <code>null</code>.
	 * @param toCreate  		The annotated <code>DataObject</code>s.
	 *                          Mustn't be <code>null</code>.                          
	 * @param data              The Annotation object to update.
	 *                          Mustn't be <code>null</code>.
	 * @param observer          Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateAndCreateAnnotation(Map toUpdate, Set toCreate, 
			AnnotationData data, AgentEventListener observer);

	/**
	 * Loads the original archived files if any linked to the set of pixels.
	 * 
	 * @param location	The location where to save the file.
	 * @param pixelsID	The pixels set ID.
	 * @param observer	Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadArchivedFiles(String location, long pixelsID, 
			AgentEventListener observer);

	/**
	 * Annotates the images contained in the passed folder.
	 * 
	 * @param folders		Collection of folders containing the images
	 * 						to annotate.
	 * @param annotation	The annotation.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle annotateChildren(Set folders, 
			AnnotationData annotation, AgentEventListener observer);
	
	/**
	 * Loads the images imported during the passed period.
	 * 
	 * @param constrain		One of the following constants: {@link #BEFORE},
	 * 						{@link #AFTER} or {@link #PERIOD}
	 * @param startTime		The lower bound of the period interval. 
	 * @param endTime		The upper bound of the interval. 
	 * @param userID		The id of the user the images belonged to.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle loadImages(Timestamp startTime, Timestamp endTime, 
								long userID, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(long pixelsID, Class rootNodeType,
			List<Long> ids, AgentEventListener observer);

	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle pasteRndSettings(long pixelsID, TimeRefObject ref, 
			AgentEventListener observer);
	
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(Class rootNodeType, List<Long> ids, 
									AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle resetRndSettings(TimeRefObject ref, 
										AgentEventListener observer);

	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param ids			The ids of the nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setRndSettings(Class rootNodeType, List<Long> ids, 
									AgentEventListener observer);

	/**
	 * Resets the rendering settings associated for the images imported during 
	 * a period of time
	 * 
	 * @param ref			The time reference object.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle setRndSettings(TimeRefObject ref, 
										AgentEventListener observer);

	/**
	 * Annotates the images imported during the given period of time.
	 * 
	 * @param timeRef		The time reference.
	 * @param annotation	The annotation.
	 * @param observer		Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle annotateChildren(TimeRefObject timeRef, 
			AnnotationData annotation, AgentEventListener observer);
	
	/**
	 * Retrieves the objects specified by the context of the search.
	 * 
	 * @param scope			The scope of the search.
	 * @param values		The terms to find.
	 * @param users			The users' data.
	 * @param start			The start of the time interval.
	 * @param end			The end of the time interval.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.
	 * @param observer	Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle advancedSearchFor(List<Class> scope, List<String> values, 
			List<ExperimenterData> users, Timestamp start, Timestamp end,
			String separator, boolean caseSensitive,
			AgentEventListener observer);
	
	/**
	 * Retrieves the objects specified by the context of the search.
	 * 
	 * @param context	The context of the search.
	 * @param observer	Callback handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle advancedSearchFor(SearchDataContext context, 
										AgentEventListener observer);
	
}
