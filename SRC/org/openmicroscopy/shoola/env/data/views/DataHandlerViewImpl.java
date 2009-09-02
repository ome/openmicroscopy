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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.FilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ObjectFinder;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

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
	 * @see DataHandlerView#pasteRndSettings(long, Class, List, 
	 * 										AgentEventListener)
	 */
	public CallHandle pasteRndSettings(long pixelsID, Class rootNodeType, 
			List<Long> ids, AgentEventListener observer)
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
	 * @see DataHandlerView#resetRndSettings(Class, List, AgentEventListener)
	 */
	public CallHandle resetRndSettings(Class rootNodeType, List<Long> ids, 
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
	 * @see DataHandlerView#setRndSettings(Class, List, AgentEventListener)
	 */
	public CallHandle setRndSettings(Class rootNodeType, List<Long> ids, 
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
	 * @see DataHandlerView#loadFiles(int, long, AgentEventListener)
	 */
	public CallHandle loadFiles(int type, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesLoader(type, userID);
		return cmd.exec(observer);
	}

}
