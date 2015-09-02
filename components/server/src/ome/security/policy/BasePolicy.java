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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;

/**
 * Simple base class for {@link Policy} implementations which always returns
 * true for {@link #isRestricted(IObject)} and always fails on
 * {@link #checkRestriction(IObject)}.
 */
public abstract class BasePolicy implements Policy {

    final protected Set<Class<IObject>> types;

    public BasePolicy(Set<Class<IObject>> types) {
        if (types == null) {
            this.types = Collections.emptySet();
        } else {
            this.types = Collections.unmodifiableSet(
                    new HashSet<Class<IObject>>(types));
        }
    }
    public abstract String getName();

    @Override
    public Set<Class<IObject>> getTypes() {
        return types;
    }

    @Override
    public boolean isRestricted(IObject obj) {
        return true;
    }

    @Override
    public void checkRestriction(IObject obj) {
        throw new SecurityViolation(getName()+ ":: disallowed.");
    }

}