/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
 */

package ome.security;

import ome.model.IObject;

/**
 * Strategy for changing the permissions of objects in the database
 * as well as verifying that the permissions for the modified objects
 * are sensible after the change.
 *
 * This interface is designed to be used in an asynchronous situation
 * where as many individual steps as possible are performed rather
 * than performing everything in one go.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4
 */
public interface ChmodStrategy {

    /**
     * Return all the checks necessary to validate the
     * given object if it were to have its permissions.
     * The Object return value should be assumed opaque,
     * and is primarily intended for passing it back to
     * {@link #check(IObject, Object)}.
     */
    Object[] getChecks(IObject obj, String permissions);

    /**
     * Change the permissions for the given object.
     * This may do nothing if the permissions do not
     * differ from the current settings. In any case,
     * this method is intended to return quickly.
     * Once the change takes place, it will be necessary
     * to run {@link #check(IObject, Object)} to
     * guarantee that no invalid links are present.
     */
    void chmod(IObject obj, String permissions);

    /**
     * Performs one of the checks returned by
     * {@link #getChecks(IObject obj, String permissions)}.
     * These will typically be queries to be performed
     * across all tables.
     */
    void check(IObject obj, Object check);

}
