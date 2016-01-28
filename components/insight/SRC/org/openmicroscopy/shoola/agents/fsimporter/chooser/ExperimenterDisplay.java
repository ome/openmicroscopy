/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;

import org.openmicroscopy.shoola.agents.util.EditorUtil;

import omero.gateway.model.ExperimenterData;

/** 
 * Provides a wrapper to display the experimenter 
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public class ExperimenterDisplay  {

	/** The user being wrapped */
	private ExperimenterData data;
	
	/** Creates an instance wrapping this user data for display */
	public ExperimenterDisplay(ExperimenterData data)
	{
		this.data = data;
	}
	
	/**
	 * Returns a formatted representation of this user "{firstName} {lastName}"
	 */
	public String toString()
	{
	    String value = EditorUtil.formatExperimenter(data);
	    return EditorUtil.truncate(value);
	}

	/**
	 * Returns the wrapped user data.
	 * @return see above.
	 */
	public ExperimenterData getData() {
		return data;
	}
}
