/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRule;
import ome.system.Roles;
import omero.cmd.GraphModify2;
import omero.cmd.Request;
import omero.cmd.SkipHead;
import omero.cmd.graphs.ChildOption;

/**
 * Create request objects that are executed using the {@link ome.services.graphs.GraphPathBean}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphRequestFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRequestFactory.class);

    private final ACLVoter aclVoter;
    private final Roles securityRoles;
    private final SystemTypes systemTypes;
    private final GraphPathBean graphPathBean;
    private final Deletion deletionInstance;
    private final ImmutableSetMultimap<Class<? extends Request>, Class<? extends IObject>> allTargets;
    private final ImmutableMap<Class<? extends Request>, GraphPolicy> graphPolicies;
    private final ImmutableSetMultimap<String, String> unnullable;
    private final ImmutableSet<String> defaultExcludeNs;
    private final boolean isGraphsWrap;

    /**
     * Construct a new graph request factory.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean
     * @param deletionInstance a deletion instance for deleting files
     * @param allTargets legal target object classes for all request classes that use the graph path bean
     * @param allRules rules for all request classes that use the graph path bean
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     * @param defaultExcludeNs the default value for an unset excludeNs field
     * @param isGraphsWrap if {@link omero.cmd.GraphModify2} requests should substitute for the requests that they replace
     * @throws GraphException if the graph path rules could not be parsed
     */
    public GraphRequestFactory(ACLVoter aclVoter, Roles securityRoles, SystemTypes systemTypes, GraphPathBean graphPathBean,
            Deletion deletionInstance, Map<Class<? extends Request>, List<String>> allTargets,
            Map<Class<? extends Request>, List<GraphPolicyRule>> allRules, List<String> unnullable, Set<String> defaultExcludeNs,
            boolean isGraphsWrap) throws GraphException {
        this.aclVoter = aclVoter;
        this.securityRoles = securityRoles;
        this.systemTypes = systemTypes;
        this.graphPathBean = graphPathBean;
        this.deletionInstance = deletionInstance;

        final ImmutableSetMultimap.Builder<Class<? extends Request>, Class<? extends IObject>> allTargetsBuilder =
                ImmutableSetMultimap.builder();
        for (final Map.Entry<Class<? extends Request>, List<String>> rules : allTargets.entrySet()) {
            final Class<? extends Request> requestClass = rules.getKey();
            for (final String targetClassName : rules.getValue()) {
                allTargetsBuilder.put(requestClass, graphPathBean.getClassForSimpleName(targetClassName));
            }
        }
        this.allTargets = allTargetsBuilder.build();

        final ImmutableMap.Builder<Class<? extends Request>, GraphPolicy> graphPoliciesBuilder = ImmutableMap.builder();
        for (final Map.Entry<Class<? extends Request>, List<GraphPolicyRule>> rules : allRules.entrySet()) {
            graphPoliciesBuilder.put(rules.getKey(), GraphPolicyRule.parseRules(graphPathBean, rules.getValue()));
        }
        this.graphPolicies = graphPoliciesBuilder.build();

        final ImmutableSetMultimap.Builder<String, String> unnullableBuilder = ImmutableSetMultimap.builder();
        for (final String classProperty : unnullable) {
            final int period = classProperty.indexOf('.');
            final String classNameSimple = classProperty.substring(0, period);
            final String property = classProperty.substring(period + 1);
            final String classNameFull = graphPathBean.getClassForSimpleName(classNameSimple).getName();
            unnullableBuilder.put(classNameFull, property);
        }
        this.unnullable = unnullableBuilder.build();

        this.defaultExcludeNs = ImmutableSet.copyOf(defaultExcludeNs);

        final String logMessage = "substituting Chgrp, Chmod, Chown, Delete requests with Chgrp2, Chmod2, Chown2, Delete2 requests";
        if (isGraphsWrap) {
            LOGGER.debug(logMessage);
        } else {
            LOGGER.warn("not " + logMessage);
        }
        this.isGraphsWrap = isGraphsWrap;
    }

    /**
     * @return the graph path bean used by this instance
     */
    public GraphPathBean getGraphPathBean() {
        return graphPathBean;
    }

    /**
     * @return if {@link omero.cmd.GraphModify2} requests should substitute for the requests that they replace
     */
    public boolean isGraphsWrap() {
        return isGraphsWrap;
    }

    /**
     * Get the legal target object classes for the given request.
     * @param requestClass a request class
     * @return the legal target object classes for that type of request
     */
    public <R extends GraphModify2> Set<Class<? extends IObject>> getLegalTargets(Class<R> requestClass) {
        final Set<Class<? extends IObject>> targetClasses = allTargets.get(requestClass);
        if (targetClasses.isEmpty()) {
            throw new IllegalArgumentException("no legal target classes defined for request class " + requestClass);
        }
        return targetClasses;
    }

    /**
     * Construct a request.
     * @param requestClass a request class
     * @return a new instance of that class
     */
    public <R extends GraphModify2> R getRequest(Class<R> requestClass) {
        final R request;
        try {
            if (SkipHead.class.isAssignableFrom(requestClass)) {
                final Constructor<R> constructor = requestClass.getConstructor(GraphPathBean.class, GraphRequestFactory.class);
                request = constructor.newInstance(graphPathBean, this);
            } else {
                final Set<Class<? extends IObject>> targetClasses = getLegalTargets(requestClass);
                GraphPolicy graphPolicy = graphPolicies.get(requestClass);
                if (graphPolicy == null) {
                    throw new IllegalArgumentException("no graph traversal policy rules defined for request class " + requestClass);
                } else {
                    graphPolicy = graphPolicy.getCleanInstance();
                }
                final Constructor<R> constructor = requestClass.getConstructor(ACLVoter.class, Roles.class, SystemTypes.class,
                        GraphPathBean.class, Deletion.class, Set.class, GraphPolicy.class, SetMultimap.class);
                request =
                        constructor.newInstance(aclVoter, securityRoles, systemTypes, graphPathBean, deletionInstance,
                                targetClasses, graphPolicy, unnullable);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("cannot instantiate " + requestClass, e);
        }
        return request;
    }

    /**
     * @return an uninitialized child option instance
     */
    public ChildOption createChildOption() {
        return new ChildOptionI(graphPathBean, defaultExcludeNs);
    }
}
