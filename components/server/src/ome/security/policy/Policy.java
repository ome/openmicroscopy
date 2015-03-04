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
 * Strategy to permit the flexible restriction of certain actions
 * throughout OMERO. Code which intends to allow such checks should
 * create a new {@link Policy} class with a unique {@link #getName() name}
 * and add checks within that code. For example:
 *
 * <pre>
 *    public class MyPolicy implements Policy {
 *
 *        public final static string NAME = "MyPolicy";
 *
 *        public String getName() {
 *            return NAME;
 *        }
 *    }
 *
 *    public void someImportantMethod() {
 *        IObject objBeingAccessed = ...;
 *        policyService.checkRestriction(MyPolicy.NAME, objBeingAccessed);
 *        // Here an exception may be thrown
 *    }
 * </pre>
 */
public interface Policy {

    /**
     * Unique name for this type of {@link Policy}. This string will be sent to
     * clients via
     * {@link ome.model.internal.Permissions#copyExtendedRestrictions()} in
     * order to prevent exceptions.
     */
    String getName();

    /**
     * Each {@link Policy} should tell the {@link PolicyService} which types
     * of {@link IObject} instances it cares about. Only those which are of
     * interest to <em>some</em> {@link Policy} need be considered.
     */
    Set<Class<IObject>> getTypes();

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