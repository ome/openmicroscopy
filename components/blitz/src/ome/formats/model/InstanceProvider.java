/*
 * ome.formats.enums.InstanceProvider
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

package ome.formats.model;

import omero.model.IObject;

/**
 * An instance provider, whose job is to make OMERO Blitz classes available
 * to a consumer based on a set of criteria. Fundamentally, concrete
 * implementations are designed to isolate consumers from the semantics of
 * OMERO Blitz model object instantiation.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public interface InstanceProvider
{
    /**
     * Retrieves an instance.
     * @param klass Instance's base class from <code>omero.model</code>.
     * @return Concrete instance of <code>klass</code>.
     * @throws ModelException If there is an error retrieving the instance.
     */
	<T extends IObject> T getInstance(Class<T> klass) throws ModelException;
}
