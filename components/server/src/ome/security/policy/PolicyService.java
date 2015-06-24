/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.policy;

import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;

/**
 * Internal service containing a number of configured {@link Policy} instances.
 * Each {@link Policy} is stored under a unique name, for which there may be
 * several other {@link Policy} instances. Consumers can either check whether
 * such a policy restriction is active via
 * {@link #isRestricted(String, IObject)} or let an exception be thrown by the
 * {@link Policy} itself via {@link #checkRestriction(String, IObject)}.
 * Further, the list of currently active restrictions can be provided in bulk to
 * clients via {@link #listActiveRestrictions(IObject)} so that restricted
 * operations need not be called only to have an exception thrown.
 */
public interface PolicyService {

    /**
     * Ask each configured {@link Policy} instance with the given name argument
     * if it considers the restriction active for the given {@link IObject}
     * argument. If any are active, return true.
     *
     * @param name
     *            non-null identifier of a class of {@link Policy} instances.
     * @param obj
     *            non-null "context" for this check.
     * @return true if any {@link Policy} returns true from
     *         {@link Policy#isRestricted(IObject)}.
     */
    boolean isRestricted(String name, IObject obj);

    /**
     * Give each configured {@link Policy} instance the chance to throw a
     * {@link SecurityViolation} from its
     * {@link Policy#checkRestriction(IObject)} method.
     *
     * @param name
     *            non-null identifier of a class of {@link Policy} instances.
     * @param obj
     *            non-null "context" for this check.
     */
    void checkRestriction(String name, IObject obj) throws SecurityViolation;

    /**
     * Return all configured identifier strings as would be passed as the first
     * argument to {@link #isRestricted(String, IObject)} or
     * {@link #checkRestriction(String, IObject)}.
     */
    Set<String> listAllRestrictions();

    /**
     * Return all identifier strings as would be passed as the first argument to
     * {@link #isRestricted(String, IObject)} or
     * {@link #checkRestriction(String, IObject)} <em>which</em> considers
     * itself active for the given argument.
     *
     * @param obj
     *            non-null context passed to each {@link Policy} instance.
     * @return a possibly empty string set of identifiers which should be
     *         returned to clients via
     *         {@link ome.model.internal.Permissions#copyExtendedRestrictions()}
     *         .
     */
    Set<String> listActiveRestrictions(IObject obj);

}
