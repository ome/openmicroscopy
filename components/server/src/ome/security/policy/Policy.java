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

/**
 * Extensible service and security policies which can influence
 * whether a user, group, or other agent can perform a certain
 * action.
 */
package ome.security.policy;

import ome.conditions.SecurityViolation;
import ome.model.IObject;

public interface Policy {

    /**
     * Unique name for this type of {@link Policy}. This string will be sent to
     * clients via
     * {@link ome.model.internal.Permissions#copyExtendedRestrictions()} in
     * order to prevent exceptions.
     */
    String getName();

    /**
     * Checks whether or not this instance would throw a
     * {@link SecurityViolation} if the same instance were passed to
     * {@link #checkRestriction(IObject)}. This is likely determined by first
     * testing the type of the {@link IObject} and then that the
     * current user context has access to the given context.
     * 
     * @param obj
     *            a non-null {@link IObject} instance.
     * 
     * @return true if this {@link Policy} decides that a restriction should be
     *         placed on the passed context.
     */
    boolean isRestricted(IObject obj);

    /**
     * Like {@link #isRestricted(Policy)} but throws an appropriate
     * {@link SecurityViolation} subclass if the restriction is active.
     */
    void checkRestriction(IObject obj) throws SecurityViolation;

}