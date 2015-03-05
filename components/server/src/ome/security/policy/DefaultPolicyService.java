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
import java.util.Map;
import java.util.Set;

import ome.model.IObject;
import ome.tools.spring.OnContextRefreshedEventListener;

import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * {@link PolicyService} which is configured with all {@link Policy} instances
 * which are discovered in the Spring context and only focuses on a small
 * subset of {@link IObject} types as specified by {@link #filterObject(IObject)}.
 */
public class DefaultPolicyService
    extends OnContextRefreshedEventListener
    implements PolicyService {

    private final Set<Class<IObject>> types = new HashSet<Class<IObject>>();

    private final ListMultimap<String, Policy> policies = ArrayListMultimap.create();

    /**
     * Loads all {@link Policy} instances from the context,
     * and uses them to initialize this {@link PolicyService}.
     */
    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        for (Policy policy : event.getApplicationContext()
                .getBeansOfType(Policy.class).values()) {
            policies.put(policy.getName(), policy);
            types.addAll(policy.getTypes());
        }
    }

    //
    // INTERFACE METHODS
    //

    @Override
    public boolean isRestricted(final String name, final IObject obj) {

        if (name == null) {
            return false;
        }

        for (Policy check : policies.get(name))
            if (check.isRestricted(obj)) {
                return true;
        }
        return false;
    }

    @Override
    public void checkRestriction(final String name, final IObject obj) {
        for (Policy check : policies.get(name)) {
            check.checkRestriction(obj);
        }
    }

    @Override
    public Set<String> listAllRestrictions() {
        return policies.keySet();
    }

    @Override
    public Set<String> listActiveRestrictions(IObject obj) {

        if (filterObject(obj)) {
            return Collections.emptySet();
        }

        Set<String> rv = new HashSet<String>();
        for (Map.Entry<String, Policy> entry : policies.entries()) {
            if (entry.getValue().isRestricted(obj)) {
                rv.add(entry.getKey());
            }
        }
        return rv;
    }

    /**
     * Limit the objects to which {@link Policy} instances are applied. This
     * reduces the overhead of creating a {@link HashSet} for every object in
     * a returned graph.
     *
     * @param obj e.g. the argument to {@link #listActiveRestrictions(IObject)}.
     * @return true if the given object should <em>not</em> be restricted.
     */
    protected boolean filterObject(IObject obj) {
        if (obj == null) {
            return true;
        }
        return !types.contains(obj.getClass());
    }

}
