/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor.maptable;

import omero.model.NamedValue;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

/**
 * UI utility class for dealing with MapAnnotations
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class MapUtils {

	/** Text content of the 'Add entry' row */
	static final String DUMMY_KEY = "Add Key";

	/** Text content of the 'Add entry' row */
	static final String DUMMY_VALUE = "Add Value";

	/**
	 * Checks if a NamedValue contains sensible data, i. e. name and value are
	 * neither empty nor dummy values.
	 * 
	 * @param nv
	 *            The NamedValue to check
	 * @return <code>true</code> if it doesn't contain sensible data,
	 *         <code>false</code> if it does.
	 */
	static boolean isEmpty(NamedValue nv) {
		return nv == null
				|| (CommonsLangUtils.isEmpty(nv.name) || DUMMY_KEY.equals(nv.name))
				&& (CommonsLangUtils.isEmpty(nv.value) || DUMMY_VALUE
						.equals(nv.value));
	}
}
