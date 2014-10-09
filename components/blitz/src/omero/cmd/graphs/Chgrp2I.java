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

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.model.meta.ExperimenterGroup;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.system.EventContext;
import ome.system.Login;
import omero.cmd.Chgrp2;
import omero.cmd.Chgrp2Response;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * An experimental Chgrp for exercising the {@link ome.services.graphs.GraphPathBean} from clients.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class Chgrp2I extends Chgrp2 implements IRequest, WrappableRequest<Chgrp2> {

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private static final Collection<GraphPolicy.Ability> REQUIRED_ABILITIES = ImmutableSet.of(GraphPolicy.Ability.OWN);

    private final ACLVoter aclVoter;
    private final SystemTypes systemTypes;
    private final GraphPathBean graphPathBean;
    private GraphPolicy graphPolicy;  /* not final because of adjustGraphPolicy */
    private final SetMultimap<String, String> unnullable;
 
    private Helper helper;
    private GraphTraversal graphTraversal;

    int targetObjectCount = 0;
    int deletedObjectCount = 0;
    int movedObjectCount = 0;

    /**
     * Construct a new <q>chgrp</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean to use
     * @param graphPolicy the graph policy to apply for chgrp
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     */
    public Chgrp2I(ACLVoter aclVoter, SystemTypes systemTypes, GraphPathBean graphPathBean, GraphPolicy graphPolicy,
            SetMultimap<String, String> unnullable) {
        this.aclVoter = aclVoter;
        this.systemTypes = systemTypes;
        this.graphPathBean = graphPathBean;
        this.graphPolicy = graphPolicy;
        this.unnullable = unnullable;
    }

    @Override
    public Map<String, String> getCallContext() {
        return new HashMap<String, String>(ALL_GROUPS_CONTEXT);
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(3);

        /* check that the user is a member of the destination group */
        final EventContext eventContext = helper.getEventContext();
        if (!(eventContext.isCurrentUserAdmin() || eventContext.getMemberOfGroupsList().contains(groupId))) {
            throw helper.cancel(new ERR(), new IllegalArgumentException(), "not a member of the chgrp destination group");
        }

        GraphPolicy graphPolicyWithOptions = graphPolicy;

        graphPolicyWithOptions = AnnotationNamespacePolicy.getAnnotationNamespacePolicy(graphPolicyWithOptions,
                includeNs, excludeNs);

        graphPolicyWithOptions = OrphanOverridePolicy.getOrphanOverridePolicy(graphPolicyWithOptions, graphPathBean,
                includeChild, excludeChild);

        graphTraversal = new GraphTraversal(helper.getSession(), eventContext, aclVoter, systemTypes, graphPathBean, unnullable,
                graphPolicyWithOptions, dryRun ? new NullGraphTraversalProcessor(REQUIRED_ABILITIES) : new InternalProcessor());
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
                return graphTraversal.planOperation(helper.getSession(), targetMultimap, true);
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
            final Map<String, long[]> movedObjects = new HashMap<String, long[]>();
            final Map<String, long[]> deletedObjects = new HashMap<String, long[]>();
            for (final Entry<String, Collection<Long>> oneMovedClass : result.getKey().asMap().entrySet()) {
                final String className = oneMovedClass.getKey();
                final Collection<Long> ids = oneMovedClass.getValue();
                final long[] idArray = new long[ids.size()];
                int index = 0;
                for (final long id : ids) {
                    idArray[index++] = id;
                    movedObjectCount++;
                }
                movedObjects.put(className, idArray);
            }
            for (final Entry<String, Collection<Long>> oneDeletedClass : result.getValue().asMap().entrySet()) {
                final String className = oneDeletedClass.getKey();
                final Collection<Long> ids = oneDeletedClass.getValue();
                final long[] idArray = new long[ids.size()];
                int index = 0;
                for (final long id : ids) {
                    idArray[index++] = id;
                    deletedObjectCount++;
                }
                deletedObjects.put(className, idArray);
            }
            final Chgrp2Response response = new Chgrp2Response(movedObjects, deletedObjects);
            helper.setResponseIfNull(response);
            helper.info("in " + (dryRun ? "mock " : "") + "chgrp to " + groupId + " of " + targetObjectCount +
                    ", moved " + movedObjectCount + " and deleted " + deletedObjectCount + " in total");
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    @Override
    public void copyFieldsTo(Chgrp2 request) {
        request.dryRun = dryRun;
        request.targetObjects = targetObjects;
        request.includeNs = includeNs;
        request.excludeNs = excludeNs;
        request.includeChild = includeChild;
        request.excludeChild = excludeChild;
        request.groupId = groupId;
    }

    @Override
    public void adjustGraphPolicy(Function<GraphPolicy, GraphPolicy> adjuster) {
        this.graphPolicy = adjuster.apply(this.graphPolicy);
    }

    @Override
    public Map<String, long[]> getStartFrom(Response response) {
        return ((Chgrp2Response) response).includedObjects;
    }

    /**
     * A <q>chgrp</q> processor that updates model objects' group.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) {
            final ExperimenterGroup group = (ExperimenterGroup) session.load(ExperimenterGroup.class, groupId);
            final String update = "UPDATE " + className + " SET details.group = :group WHERE id IN (:ids)";
            session.createQuery(update).setParameter("group", group).setParameterList("ids", ids).executeUpdate();
        }

        @Override
        public Collection<GraphPolicy.Ability> getRequiredPermissions() {
            return REQUIRED_ABILITIES;
        }
    }
}
