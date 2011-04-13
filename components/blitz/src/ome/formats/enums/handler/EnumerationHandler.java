/*
 * ome.formats.enums.EnumerationHandler
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.enums.handler;

import java.util.HashMap;

import omero.model.IObject;

/**
 * An enumeration handler whose purpose is to provide extra logic, such as
 * regular expression matching, for the lookup of enumerations for a specific
 * type.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public interface EnumerationHandler
{

	/**
	 * Attempt to find an enumeration from an existing set of enumeration
	 * values.
	 * @param enumerations Exhaustive set of enumerations of this type.
	 * @param value Value to look for an enumeration of.
	 * @return An IObject object or <code>null</code>.
	 */
	IObject findEnumeration(HashMap<String, IObject> enumerations, String value);
}
