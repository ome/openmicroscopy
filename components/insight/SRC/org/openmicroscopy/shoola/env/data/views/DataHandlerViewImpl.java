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
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.FilesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ObjectFinder;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.data.views.calls.SwitchUserGroupLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
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
	 * @see DataHandlerView#loadImages(SecurityContext, Timestamp, Timestamp, long, 
	 * 								AgentEventListener)
	 */
	public CallHandle loadImages(SecurityContext ctx, Timestamp startTime,
			Timestamp endTime, long userID, AgentEventListener observer)
	{
		BatchCallTree cmd = new ImagesLoader(ctx, startTime, endTime, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#pasteRndSettings(SecurityContext, long, Class, List, 
	 * 										AgentEventListener)
	 */
	public CallHandle pasteRndSettings(SecurityContext ctx, long pixelsID,
			Class rootNodeType, List<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, pixelsID,
				rootNodeType, ids);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#pasteRndSettings(SecurityContext, long, TimeRefObject, 
	 * 										AgentEventListener)
	 */
	public CallHandle pasteRndSettings(SecurityContext ctx, long pixelsID,
			TimeRefObject ref, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, pixelsID, ref);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#resetRndSettings(SecurityContext, Class, List, AgentEventListener)
	 */
	public CallHandle resetRndSettings(SecurityContext ctx, Class rootNodeType,
			List<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, rootNodeType, ids,
								RenderingSettingsSaver.RESET);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#resetRndSettings(SecurityContext, TimeRefObject, AgentEventListener)
	 */
	public CallHandle resetRndSettings(SecurityContext ctx, TimeRefObject ref,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, ref,
									RenderingSettingsSaver.RESET);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setMinMaxSettings(SecurityContext, Class, List, AgentEventListener)
	 */
	public CallHandle setMinMaxSettings(SecurityContext ctx, Class rootNodeType,
			List<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, rootNodeType, ids,
									RenderingSettingsSaver.SET_MIN_MAX);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setMinMaxSettings(SecurityContext, TimeRefObject, AgentEventListener)
	 */
	public CallHandle setMinMaxSettings(SecurityContext ctx, TimeRefObject ref,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, ref,
								RenderingSettingsSaver.SET_MIN_MAX);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setOwnerRndSettings(SecurityContext, Class, List, AgentEventListener)
	 */
	public CallHandle setOwnerRndSettings(SecurityContext ctx,
			Class rootNodeType, List<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, rootNodeType, ids,
									RenderingSettingsSaver.SET_OWNER);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#setOwnerRndSettings(SecurityContext, TimeRefObject, AgentEventListener)
	 */
	public CallHandle setOwnerRndSettings(SecurityContext ctx,
			TimeRefObject ref, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(ctx, ref,
								RenderingSettingsSaver.SET_OWNER);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#advancedSearchFor(List, SearchDataContext,
	 * 										AgentEventListener)
	 */
	public CallHandle advancedSearchFor(List<SecurityContext> ctx,
			SearchDataContext context, AgentEventListener observer)
	{
		BatchCallTree cmd = new ObjectFinder(ctx, context);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#loadFiles(SecurityContext, int, long, AgentEventListener)
	 */
	public CallHandle loadFiles(SecurityContext ctx, int type, long userID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new FilesLoader(ctx, type, userID);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the view interface.
	 * @see DataHandlerView#switchUserGroup(SecurityContext, ExperimenterData, long, 
	 * AgentEventListener)
	 */
	public CallHandle switchUserGroup(SecurityContext ctx,
		ExperimenterData experimenter, long groupID,
		AgentEventListener observer)
	{
		BatchCallTree cmd = new SwitchUserGroupLoader(experimenter, groupID);
		return cmd.exec(observer);
	}

}
