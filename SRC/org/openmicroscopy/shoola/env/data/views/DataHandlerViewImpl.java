/*
 * org.openmicroscopy.shoola.env.data.views.DataHandlerViewImpl 
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
import org.openmicroscopy.shoola.env.data.views.calls.AdminLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ClassificationSaver;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ObjectFinder;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
* Implementation of the {@link DataHandlerView} implementation.
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
public class DataHandlerViewImpl 
	implements DataHandlerView
{

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#createAnnotation(DataObject, AnnotationData,
	 *                                      AgentEventListener)
	 */
	public CallHandle createAnnotation(DataObject annotatedObject, 
			AnnotationData data,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
				AnnotationSaver.CREATE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#updateAnnotation(DataObject, AnnotationData,
	 *                                      AgentEventListener)
	 */
	public CallHandle updateAnnotation(DataObject annotatedObject,
			AnnotationData data,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
				AnnotationSaver.UPDATE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#deleteAnnotation(DataObject, List,
	 *                                       AgentEventListener)
	 */
	public CallHandle deleteAnnotation(DataObject annotatedObject,
			List data,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
				AnnotationSaver.DELETE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#deleteAnnotation(DataObject, AnnotationData,
	 *                                       AgentEventListener)
	 */
	public CallHandle deleteAnnotation(DataObject annotatedObject,
										AnnotationData data,
											AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObject, data, 
				AnnotationSaver.DELETE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#createAnnotation(Set, AnnotationData, 
	 * 										AgentEventListener)
	 */
	public CallHandle createAnnotation(Set annotatedObjects, 
			AnnotationData d, AgentEventListener observer) 
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObjects, d, false);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#updateAndCreateAnnotation(Map, Set, 
	 * 									AnnotationData, AgentEventListener)
	 */
	public CallHandle updateAndCreateAnnotation(Map toUpdate, Set toCreate, 
			AnnotationData data, AgentEventListener observer) 
	{
		BatchCallTree cmd = new AnnotationSaver(toUpdate, toCreate, data);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#updateAnnotation(Map, AgentEventListener)
	 */
	public CallHandle updateAnnotation(Map annotatedObjects, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObjects);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadClassificationPaths(Set, long, int,
	 * 												AgentEventListener)
	 */
	public CallHandle loadClassificationPaths(Set imageIDs, 
			long userID, int algorithm, AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationLoader(imageIDs, algorithm, 
				userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#classify(Set, Set, AgentEventListener)
	 */
	public CallHandle classify(Set images, Set categories, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationSaver(images, categories, true);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#declassify(Set, Set, AgentEventListener)
	 */
	public CallHandle declassify(Set images, Set categories, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationSaver(images, categories, false);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadArchivedFiles(String, long, AgentEventListener)
	 */
	public CallHandle loadArchivedFiles(String location, long pixelsID, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ArchivedFilesLoader(location, pixelsID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#classifyChildren(Set, Set, AgentEventListener)
	 */
	public CallHandle classifyChildren(Set containers, Set categories, 
			AgentEventListener observer) 
	{
		BatchCallTree cmd = new ClassificationSaver(containers, categories);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#annotateChildren(Set, AnnotationData, 
	 * 										AgentEventListener)
	 */
	public CallHandle annotateChildren(Set set, AnnotationData annotation, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(set, annotation, true);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#findCategoryPaths(long, boolean, long, 
	 * 											AgentEventListener)
	 */
	public CallHandle findCategoryPaths(long imageID, boolean leaves, 
			long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationLoader(imageID,
				leaves, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#findCategoryPaths(Set, boolean, long,
	 * 										AgentEventListener)
	 */
	public CallHandle findCategoryPaths(Set<Long> imagesID,  boolean leaves,
			long userID, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationLoader(imagesID, leaves, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadImages(Timestamp, Timestamp, long, 
	 * 								AgentEventListener)
	 */
	public CallHandle loadImages(Timestamp startTime, Timestamp endTime, 
								long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(startTime, endTime, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#pasteRndSettings(long, Class, Set, 
	 * 										AgentEventListener)
	 */
	public CallHandle pasteRndSettings(long pixelsID, Class rootNodeType, 
			Set<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(pixelsID, rootNodeType, 
								ids);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#pasteRndSettings(long, TimeRefObject, 
	 * 										AgentEventListener)
	 */
	public CallHandle pasteRndSettings(long pixelsID, TimeRefObject ref, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(pixelsID, ref);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#resetRndSettings(Class, Set, AgentEventListener)
	 */
	public CallHandle resetRndSettings(Class rootNodeType, Set<Long> ids, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(rootNodeType, ids, 
								RenderingSettingsSaver.RESET);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#resetRndSettings(TimeRefObject, AgentEventListener)
	 */
	public CallHandle resetRndSettings(TimeRefObject ref, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ref, 
									RenderingSettingsSaver.RESET);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setRndSettings(Class, Set, AgentEventListener)
	 */
	public CallHandle setRndSettings(Class rootNodeType, Set<Long> ids, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(rootNodeType, ids, 
									RenderingSettingsSaver.SET_ORIGINAL);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setRndSettings(TimeRefObject, AgentEventListener)
	 */
	public CallHandle setRndSettings(TimeRefObject ref, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ref, 
								RenderingSettingsSaver.SET_ORIGINAL);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#annotateChildren(TimeRefObject, AnnotationData, 
	 * 										AgentEventListener)
	 */
	public CallHandle annotateChildren(TimeRefObject timeRef, 
			AnnotationData annotation, AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(timeRef, annotation);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#classifyChildren(TimeRefObject, Set, 
	 * 										AgentEventListener)
	 */
	public CallHandle classifyChildren(TimeRefObject timeRef, Set categories, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationSaver(timeRef, categories);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#advancedSearchFor(List, List, List, Timestamp, 
	 * 									Timestamp, String, boolean,
	 * 									AgentEventListener)
	 */
	public CallHandle advancedSearchFor(List<Class> scope, List<String> values, 
			List<ExperimenterData> users, Timestamp start, Timestamp end,
			String separator, boolean caseSensitive, 
			AgentEventListener observer)
	{
		return null;
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#advancedSearchFor(SearchDataContext,
	 * 										AgentEventListener)
	 */
	public CallHandle advancedSearchFor(SearchDataContext context, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new ObjectFinder(context);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadAvailableGroups(AgentEventListener)
	 */
	public CallHandle loadAvailableGroups(AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(-1, AdminLoader.GROUP);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadUnlinkedTags(Set, long, AgentEventListener)
	 */
	public CallHandle loadUnlinkedTags(Set<Long> imageIDs, long userID, 
						AgentEventListener observer)
	{
		BatchCallTree cmd = new ClassificationLoader(imageIDs,
				ClassificationLoader.TAGS_AVAILABLE, userID);
		return cmd.exec(observer);
	}

}
