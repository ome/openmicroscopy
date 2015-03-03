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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ome.tools.spring.OnContextRefreshedEventListener;

import org.springframework.context.event.ContextRefreshedEvent;

public class DefaultPolicyService
    extends OnContextRefreshedEventListener
    implements PolicyService {

    private final Map<String, Policy> policies = new HashMap<String, Policy>();

    /**
     * Loads all {@link Policy} instances from the context,
     * and uses them to initialize this {@link PolicyService}.
     */
    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        policies.putAll(
                event.getApplicationContext()
                    .getBeansOfType(Policy.class));
    }

    //
    // INTERFACE METHODS
    //

    @Override
    public boolean isRestricted(String policyName) {
        if (!policies.containsKey(policyName)) {
            return false;
        }
        return policies.get(policyName).isActive();
    }

    @Override
    public void checkRestriction(String policyName) {
        if (policies.containsKey(policyName)) {
            policies.get(policyName).check();
        }
    }

    @Override
    public Set<String> listAllRestrictions() {
        return policies.keySet();
    }

    @Override
    public Set<String> listActiveRestrictions() {
        Set<String> active = new HashSet<String>();
        for (Map.Entry<String, Policy> entry : policies.entrySet()) {
            if (entry.getValue().isActive()) {
                active.add(entry.getKey());
            }
        }
        return active;
    }
}