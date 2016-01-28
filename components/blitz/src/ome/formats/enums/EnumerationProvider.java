/*
 * ome.formats.enums.EnumerationProvider
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

package ome.formats.enums;

import java.util.HashMap;

import omero.model.IObject;

/**
 * An enumeration provider, whose job is to make OMERO enumerations available
 * to a consumer based on a set of criteria. Fundamentally, concrete
 * implementations are designed to isolate consumers from the semantics of
 * OMERO services such as IQuery and IObject and to provide a consistent, server
 * agnostic API to unit test code.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public interface EnumerationProvider
{
    /**
     * Retrieves an enumeration.
     * @param klass Enumeration's base class from <code>ome.model.enums</code>.
     * @param value Enumeration's string value.
     * @param loaded <code>true</code> if the enumeration returned should be
     * loaded, otherwise <code>false</code>.
     * @return Enumeration object.
     */
	<T extends IObject> T getEnumeration(Class<T> klass, String value,
		               boolean loaded);

    /**
     * Retrieves all enumerations of a specific type.
     * @param klass Enumeration's base class from <code>ome.model.enums</code>.
     * @return Enumeration object.
     */
	<T extends IObject> HashMap<String, T> getEnumerations(Class<T> klass);
}
