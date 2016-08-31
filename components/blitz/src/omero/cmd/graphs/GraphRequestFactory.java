/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRule;
import omero.cmd.Request;

/**
 * Create request objects that are executed using the {@link ome.services.graphs.GraphPathBean}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public class GraphRequestFactory {
    private final GraphPathBean graphPathBean;
    private final Map<Class<? extends Request>, GraphPolicy> graphPolicies;

    /**
     * Construct a new graph request factory.
     * @param graphPathBean the graph path bean
     * @param allRules rules for all request classes that use the graph path bean
     * @throws GraphException if the graph path rules could not be parsed
     */
    public GraphRequestFactory(GraphPathBean graphPathBean, Map<Class<? extends Request>, List<GraphPolicyRule>> allRules)
            throws GraphException {
        this.graphPathBean = graphPathBean;

        final ImmutableMap.Builder<Class<? extends Request>, GraphPolicy> builder = ImmutableMap.builder();
        for (final Map.Entry<Class<? extends Request>, List<GraphPolicyRule>> rules : allRules.entrySet()) {
            builder.put(rules.getKey(), GraphPolicyRule.parseRules(graphPathBean, rules.getValue()));
        }
        this.graphPolicies = builder.build();
    }

    /**
     * Construct a request.
     * @param requestClass a request class
     * @return a new instance of that class
     */
    public <X extends Request> X getRequest(Class<X> requestClass) {
        final GraphPolicy graphPolicy = graphPolicies.get(requestClass);
        if (graphPolicy == null) {
            throw new IllegalArgumentException("no graph traversal policy rules defined for request class " + requestClass);
        }
        try {
            return requestClass.getConstructor(GraphPathBean.class, GraphPolicy.class).newInstance(graphPathBean, graphPolicy);
        } catch (Exception e) {
            /* TODO: easier to do a ReflectiveOperationException multi-catch in Java SE 7 */
            throw new IllegalArgumentException("cannot instantiate " + requestClass, e);
        }
    }
}
