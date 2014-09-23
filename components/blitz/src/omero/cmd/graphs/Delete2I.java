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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.system.EventContext;
import ome.system.Login;
import omero.cmd.Delete2;
import omero.cmd.Delete2Response;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * An experimental Delete for exercising the {@link ome.services.graphs.GraphPathBean} from clients.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class Delete2I extends Delete2 implements IRequest {

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private final ACLVoter aclVoter;
    private final SystemTypes systemTypes;
    private final GraphPathBean graphPathBean;
    private final GraphPolicy graphPolicy;

    private Helper helper;
    private GraphTraversal graphTraversal;

    int targetObjectCount = 0;
    int deletedObjectCount = 0;

    /**
     * Construct a new <q>delete</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean to use
     * @param graphPolicy the graph policy to apply for delete
     */
    public Delete2I(ACLVoter aclVoter, SystemTypes systemTypes, GraphPathBean graphPathBean, GraphPolicy graphPolicy) {
        this.aclVoter = aclVoter;
        this.systemTypes = systemTypes;
        this.graphPathBean = graphPathBean;
        this.graphPolicy = graphPolicy;
    }

    @Override
    public ImmutableMap<String, String> getCallContext() {
       return ALL_GROUPS_CONTEXT;
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(3);

        final EventContext eventContext = helper.getEventContext();

        final GraphPolicy graphPolicyWithOptions =
                AnnotationNamespacePolicy.getAnnotationNamespacePolicy(graphPolicy, includeNs, excludeNs);

        graphTraversal = new GraphTraversal(eventContext, aclVoter, systemTypes, graphPathBean, graphPolicyWithOptions,
                new InternalProcessor());
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            switch (step) {
            case 0:
                /* if targetObjects were an IObjectList then this would need IceMapper.reverse */
                final SetMultimap<String, Long> targetMultimap = HashMultimap.create();
                for (final Entry<String, long[]> oneClassToTarget : targetObjects.entrySet()) {
                    String className = oneClassToTarget.getKey();
                    if (className.lastIndexOf('.') < 0) {
                        className = graphPathBean.getClassForSimpleName(className).getName();
                    }
                    for (final long id : oneClassToTarget.getValue()) {
                        targetMultimap.put(className, id);
                        targetObjectCount++;
                    }
                }
                return graphTraversal.planOperation(helper.getSession(), targetMultimap);
            case 1:
                graphTraversal.unlinkTargets();
                return null;
            case 2:
                graphTraversal.processTargets();
                return null;
            default:
                throw helper.cancel(new ERR(), new IllegalArgumentException(), "model object graph operation has no step " + step);
            }
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "model object graph operation failed");
        }
    }

    @Override
    public void finish() {
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            /* if the results object were in terms of IObjectList then this would need IceMapper.map */
            final Entry<SetMultimap<String, Long>, SetMultimap<String, Long>> result =
                    (Entry<SetMultimap<String, Long>, SetMultimap<String, Long>>) object;
            final SetMultimap<String, Long> resultMoved = result.getKey();
            final SetMultimap<String, Long> resultDeleted = result.getValue();
            final Map<String, long[]> deletedObjects = new HashMap<String, long[]>();
            for (final String className : Sets.union(resultMoved.keySet(), resultDeleted.keySet())) {
                final Set<Long> ids = Sets.union(resultMoved.get(className), resultDeleted.get(className));
                final long[] idArray = new long[ids.size()];
                int index = 0;
                for (final long id : ids) {
                    idArray[index++] = id;
                    deletedObjectCount++;
                }
                deletedObjects.put(className, idArray);
            }
            final Delete2Response response = new Delete2Response(deletedObjects);
            helper.setResponseIfNull(response);
            helper.info("in delete of " + targetObjectCount + ", deleted " + deletedObjectCount + " in total");
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /**
     * A <q>delete</q> processor that deletes model objects.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        private final Collection<GraphPolicy.Ability> requiredAbilities = ImmutableSet.of(GraphPolicy.Ability.DELETE);

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) {
            deleteInstances(className, ids);
        }

        @Override
        public Collection<GraphPolicy.Ability> getRequiredPermissions() {
            return requiredAbilities;
        }
    }
}
