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

package ome.formats.model.handler;

import omero.model.IObject;

/**
 * A model object handler whose purpose is to provide extra logic, such as
 * enumeration not-null constraint fulfilment.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public interface ModelObjectHandler
{

	/**
	 * Attempt to handle the model object of this type.
	 * @param object Object to attempt to handle.
	 * @return <code>object</code>.
	 */
	IObject handle(IObject object);
}
