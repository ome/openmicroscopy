/*
 * org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsSaved 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.iviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

/** 
 * Event posted when the rendering settings have been saved from the preview.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RndSettingsSaved 
	extends RequestEvent
{
	
	/** The identifier of the pixels set of reference. */
	private long		refPixelsID;
	
	/** The save rendering settings. */
	private RndProxyDef settings;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refPixelsID The identifier of the pixels set.
	 * @param settings    The rendering settings.
	 */
	public RndSettingsSaved(long refPixelsID, RndProxyDef settings)
	{
		this.refPixelsID = refPixelsID;
		this.settings = settings;
	}
	
	/**
	 * Returns the id of the pixels set of reference.
	 * 
	 * @return See above.
	 */
	public long getRefPixelsID() { return refPixelsID; }
	
	/**
	 * Returns the settings.
	 * 
	 * @return See above.
	 */
	public RndProxyDef getSettings() { return settings; }
	
}
