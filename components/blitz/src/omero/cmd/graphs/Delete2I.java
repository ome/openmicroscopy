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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.security.ACLVoter;
import ome.security.basic.LightAdminPrivileges;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.services.util.ReadOnlyStatus;
import ome.system.Login;
import ome.system.Roles;
import omero.cmd.Delete2;
import omero.cmd.Delete2Response;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * Request to delete model objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class Delete2I extends Delete2 implements IRequest, ReadOnlyStatus.IsAware, WrappableRequest<Delete2> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Delete2I.class);

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private static final Set<GraphPolicy.Ability> REQUIRED_ABILITIES = ImmutableSet.of(GraphPolicy.Ability.DELETE);

    private final ACLVoter aclVoter;
    private final GraphPathBean graphPathBean;
    private final Set<Class<? extends IObject>> targetClasses;
    private final Deletion deletionInstance;
    private GraphPolicy graphPolicy;  /* not final because of adjustGraphPolicy */
    private final SetMultimap<String, String> unnullable;
    private final ApplicationContext applicationContext;

    private List<Function<GraphPolicy, GraphPolicy>> graphPolicyAdjusters = new ArrayList<Function<GraphPolicy, GraphPolicy>>();
    private Helper helper;
    private GraphHelper graphHelper;
    private GraphTraversal graphTraversal;

    private GraphTraversal.PlanExecutor unlinker;
    private GraphTraversal.PlanExecutor processor;

    private int targetObjectCount = 0;
    private int deletedObjectCount = 0;

    /**
     * Construct a new <q>delete</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param graphPathBean the graph path bean to use
     * @param adminPrivileges the light administrator privileges helper
     * @param deletionInstance a deletion instance for deleting files
     * @param targetClasses legal target object classes for delete
     * @param graphPolicy the graph policy to apply for delete
     * @param unnullable properties that, while nullable, may not be nulled by a graph traversal operation
     * @param applicationContext the OMERO application context from Spring
     */
    public Delete2I(ACLVoter aclVoter, Roles securityRoles, GraphPathBean graphPathBean, LightAdminPrivileges adminPrivileges,
            Deletion deletionInstance, Set<Class<? extends IObject>> targetClasses, GraphPolicy graphPolicy,
            SetMultimap<String, String> unnullable, ApplicationContext applicationContext) {
        this.aclVoter = aclVoter;
        this.graphPathBean = graphPathBean;
        this.deletionInstance = deletionInstance;
        this.targetClasses = targetClasses;
        this.graphPolicy = graphPolicy;
        this.unnullable = unnullable;
        this.applicationContext = applicationContext;
    }

    @Override
    public Map<String, String> getCallContext() {
       return new HashMap<String, String>(ALL_GROUPS_CONTEXT);
    }

    @Override
    public void init(Helper helper) {
        if (LOGGER.isDebugEnabled()) {
            final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
            arguments.addParameter("targetObjects", targetObjects);
            arguments.addParameter("childOptions", childOptions);
            arguments.addParameter("typesToIgnore", typesToIgnore);
            arguments.addParameter("dryRun", dryRun);
            LOGGER.debug("request: " + arguments);
        }

        this.helper = helper;
        helper.setSteps(dryRun ? 4 : 6);
        this.graphHelper = new GraphHelper(helper, graphPathBean);

        graphPolicy = IgnoreTypePolicy.getIgnoreTypePolicy(graphPolicy, graphHelper.getClassesFromNames(typesToIgnore));

        graphTraversal = graphHelper.prepareGraphTraversal(childOptions, REQUIRED_ABILITIES, graphPolicy, graphPolicyAdjusters,
                aclVoter, graphPathBean, unnullable, new InternalProcessor(), dryRun);

        graphPolicyAdjusters = null;
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            switch (step) {
            case 0:
                final SetMultimap<String, Long> targetMultimap = graphHelper.getTargetMultimap(targetClasses, targetObjects);
                targetObjectCount += targetMultimap.size();
                final Entry<SetMultimap<String, Long>, SetMultimap<String, Long>> plan =
                        graphTraversal.planOperation(targetMultimap, false, true);
                if (!plan.getKey().isEmpty()) {
                    final Exception e = new IllegalStateException("deletion does not do anything other than delete");
                    helper.cancel(new ERR(), e, "graph-fail");
                }
                return GraphUtil.arrangeDeletionTargets(helper.getSession(), plan.getValue());
            case 1:
                graphTraversal.assertNoPolicyViolations();
                return null;
            case 2:
                processor = graphTraversal.processTargets();
                return null;
            case 3:
                unlinker = graphTraversal.unlinkTargets(true);
                graphTraversal = null;
                return null;
            case 4:
                unlinker.execute();
                return null;
            case 5:
                processor.execute();
                return null;
            default:
                final Exception e = new IllegalArgumentException("model object graph operation has no step " + step);
                throw helper.cancel(new ERR(), e, "bad-step");
            }
        } catch (Cancel c) {
            throw c;
        } catch (GraphException ge) {
            final omero.cmd.GraphException graphERR = new omero.cmd.GraphException();
            graphERR.message = ge.message;
            throw helper.cancel(graphERR, ge, "graph-fail");
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "graph-fail");
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
            final SetMultimap<String, Long> result = (SetMultimap<String, Long>) object;
            if (!dryRun) {
                try {
                    deletionInstance.deleteFiles(GraphUtil.trimPackageNames(result));
                } catch (Exception e) {
                    helper.cancel(new ERR(), e, "file-delete-fail");
                }
            }
            final Map<String, List<Long>> deletedObjects = GraphUtil.copyMultimapForResponse(result);
            deletedObjectCount += result.size();
            final Delete2Response response = new Delete2Response(deletedObjects);
            helper.setResponseIfNull(response);
            helper.info("in " + (dryRun ? "mock " : "") + "delete of " + targetObjectCount +
                    ", deleted " + deletedObjectCount + " in total");

            if (LOGGER.isDebugEnabled()) {
                final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
                arguments.addParameter("deletedObjects", response.deletedObjects);
                LOGGER.debug("response: " + arguments);
            }
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    @Override
    public void copyFieldsTo(Delete2 request) {
        GraphUtil.copyFields(this, request);
    }

    @Override
    public void adjustGraphPolicy(Function<GraphPolicy, GraphPolicy> adjuster) {
        if (graphPolicyAdjusters == null) {
            throw new IllegalStateException("request is already initialized");
        } else {
            graphPolicyAdjusters.add(adjuster);
        }
    }

    @Override
    public int getStepProvidingCompleteResponse() {
        return 0;
    }

    @Override
    public GraphPolicy.Action getActionForStarting() {
        return GraphPolicy.Action.DELETE;
    }

    @Override
    public Map<String, List<Long>> getStartFrom(Response response) {
        return ((Delete2Response) response).deletedObjects;
    }

    @Override
    public boolean isReadOnly(ReadOnlyStatus readOnly) {
        return dryRun;
    }

    /**
     * A <q>delete</q> processor that deletes model objects.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void deleteInstances(String className, Collection<Long> ids) throws GraphException {
            super.deleteInstances(className, ids);
            graphHelper.publishEventLog(applicationContext, "DELETE", className, ids);
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) throws GraphException {
            deleteInstances(className, ids);
        }

        @Override
        public Set<GraphPolicy.Ability> getRequiredPermissions() {
            return REQUIRED_ABILITIES;
        }
    }
}
