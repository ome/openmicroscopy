/*
 * Copyright (C) 2014-2017 University of Dundee & Open Microscopy Environment.
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.security.ACLVoter;
import ome.security.basic.LightAdminPrivileges;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRule;
import ome.system.Roles;
import omero.cmd.GraphModify2;
import omero.cmd.GraphQuery;
import omero.cmd.Request;
import omero.cmd.SkipHead;

/**
 * Create request objects that are executed using the {@link ome.services.graphs.GraphPathBean}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphRequestFactory implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRequestFactory.class);

    private final ACLVoter aclVoter;
    private final Roles securityRoles;
    private final GraphPathBean graphPathBean;
    private final LightAdminPrivileges adminPrivileges;
    private final Deletion deletionInstance;
    private final ImmutableSetMultimap<Class<? extends Request>, Class<? extends IObject>> allTargets;
    private final ImmutableMap<Class<? extends Request>, GraphPolicy> graphPolicies;
    private final ImmutableSetMultimap<String, String> unnullable;
    private final ImmutableSet<String> defaultExcludeNs;

    private ApplicationContext applicationContext = null;

    /**
     * Construct a new graph request factory.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param graphPathBean the graph path bean
     * @param adminPrivileges the light administrator privileges helper
     * @param deletionInstance a deletion instance for deleting files
     * @param allTargets legal target object classes for all request classes that use the graph path bean
     * @param allRules rules for all request classes that use the graph path bean
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     * @param defaultExcludeNs the default value for an unset excludeNs field
     * @throws GraphException if the graph path rules could not be parsed
     */
    public GraphRequestFactory(ACLVoter aclVoter, Roles securityRoles, GraphPathBean graphPathBean,
            LightAdminPrivileges adminPrivileges, Deletion deletionInstance, Map<Class<? extends Request>, List<String>> allTargets,
            Map<Class<? extends Request>, List<GraphPolicyRule>> allRules, List<String> unnullable, Set<String> defaultExcludeNs)
                    throws GraphException {
        this.aclVoter = aclVoter;
        this.securityRoles = securityRoles;
        this.graphPathBean = graphPathBean;
        this.adminPrivileges = adminPrivileges;
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

        aclVoter.setPermittedClasses(ImmutableMap.of(
                Permissions.CHGRPRESTRICTION,
                (Set<Class<? extends IObject>>) this.allTargets.get(Chgrp2I.class),
                Permissions.CHOWNRESTRICTION,
                (Set<Class<? extends IObject>>) this.allTargets.get(Chown2I.class)));

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
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @return the graph path bean used by this instance
     */
    public GraphPathBean getGraphPathBean() {
        return graphPathBean;
    }

    /**
     * Get the legal target object classes for the given request.
     * @param requestClass a request class
     * @return the legal target object classes for that type of request
     */
    public <R extends GraphQuery> Set<Class<? extends IObject>> getLegalTargets(Class<R> requestClass) {
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
    public <R extends GraphQuery> R getRequest(Class<R> requestClass) {
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
                if (GraphModify2.class.isAssignableFrom(requestClass)) {
                    final Constructor<R> constructor = requestClass.getConstructor(ACLVoter.class, Roles.class,
                            GraphPathBean.class, LightAdminPrivileges.class, Deletion.class, Set.class, GraphPolicy.class,
                            SetMultimap.class, ApplicationContext.class);
                    request = constructor.newInstance(aclVoter, securityRoles, graphPathBean, adminPrivileges, deletionInstance,
                            targetClasses, graphPolicy, unnullable, applicationContext);
                } else {
                    final Constructor<R> constructor = requestClass.getConstructor(ACLVoter.class, Roles.class,
                            GraphPathBean.class, LightAdminPrivileges.class, Set.class, GraphPolicy.class);
                    request = constructor.newInstance(aclVoter, securityRoles, graphPathBean, adminPrivileges,
                            targetClasses, graphPolicy);
                }
            }
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
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
