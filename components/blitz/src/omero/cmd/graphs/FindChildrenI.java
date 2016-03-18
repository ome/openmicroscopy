/*
 * Copyright (C) 2014-2016 University of Dundee & Open Microscopy Environment.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ome.model.IObject;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.system.Login;
import ome.system.Roles;
import omero.cmd.FindChildren;
import omero.cmd.FoundChildren;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;

/**
 * Request to identify children or contents of model objects, whether direct or indirect.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class FindChildrenI extends FindChildren implements IRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindChildrenI.class);

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private static final Set<GraphPolicy.Ability> REQUIRED_ABILITIES = ImmutableSet.of();

    private final ACLVoter aclVoter;
    private final SystemTypes systemTypes;
    private final GraphPathBean graphPathBean;
    private final Set<Class<? extends IObject>> targetClasses;
    private final GraphPolicy graphPolicy;

    private final Set<Class<? extends IObject>> classesToFind = new HashSet<Class<? extends IObject>>();

    private Helper helper;
    private GraphHelper graphHelper;
    private GraphTraversal graphTraversal;

    private int targetObjectCount = 0;
    private int foundObjectCount = 0;

    /**
     * Construct a new <q>find-children</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param aclVoter ACL voter for permissions checking
     * @param securityRoles the security roles
     * @param systemTypes for identifying the system types
     * @param graphPathBean the graph path bean to use
     * @param targetClasses legal target object classes for the search
     * @param graphPolicy the graph policy to apply for the search
     */
    public FindChildrenI(ACLVoter aclVoter, Roles securityRoles, SystemTypes systemTypes, GraphPathBean graphPathBean,
            Set<Class<? extends IObject>> targetClasses, GraphPolicy graphPolicy) {
        this.aclVoter = aclVoter;
        this.systemTypes = systemTypes;
        this.graphPathBean = graphPathBean;
        this.targetClasses = targetClasses;
        this.graphPolicy = graphPolicy;
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
            arguments.addParameter("typesOfChildren", typesOfChildren);
            arguments.addParameter("stopBefore", stopBefore);
            LOGGER.debug("request: " + arguments);
        }

        this.helper = helper;
        helper.setSteps(1);
        this.graphHelper = new GraphHelper(helper, graphPathBean);

        if (CollectionUtils.isEmpty(typesOfChildren)) {
            final Exception e = new IllegalArgumentException("no types of children specified to find");
            throw helper.cancel(new ERR(), e, "no-types");
        }

        classesToFind.addAll(graphHelper.getClassesFromNames(typesOfChildren));

        final Set<String> targetClassNames = graphHelper.getTopLevelNames(graphHelper.getClassesFromNames(targetObjects.keySet()));
        final Set<String> childTypeNames = graphHelper.getTopLevelNames(classesToFind);
        final Set<String> currentStopBefore = graphHelper.getTopLevelNames(graphHelper.getClassesFromNames(stopBefore));
        final Set<String> suggestedStopBefore = StopBeforeHelper.get().getStopBeforeChildren(targetClassNames, childTypeNames);
        final Set<String> extraStopBefore = Sets.difference(suggestedStopBefore, currentStopBefore);
        stopBefore.addAll(extraStopBefore);
        if (!extraStopBefore.isEmpty() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("to stopBefore added: " + Joiner.on(',').join(Ordering.natural().sortedCopy(extraStopBefore)));
        }

        final Iterable<Function<GraphPolicy, GraphPolicy>> graphPolicyAdjusters;
        if (CollectionUtils.isEmpty(stopBefore)) {
            graphPolicyAdjusters = Collections.emptySet();
        } else {
            final Set<Class<? extends IObject>> typesToStopBefore = graphHelper.getClassesFromNames(stopBefore);
            final Function<GraphPolicy, GraphPolicy> graphPolicyAdjuster = new Function<GraphPolicy, GraphPolicy>() {
                @Override
                public GraphPolicy apply(GraphPolicy graphPolicy) {
                    return SkipTailPolicy.getSkipTailPolicy(graphPolicy, GraphUtil.getPredicateFromClasses(typesToStopBefore));
                }
            };
            graphPolicyAdjusters = Collections.singleton(graphPolicyAdjuster);
        }

        graphTraversal = graphHelper.prepareGraphTraversal(null, REQUIRED_ABILITIES, graphPolicy, graphPolicyAdjusters,
                aclVoter, systemTypes, graphPathBean, null, new NullGraphTraversalProcessor(REQUIRED_ABILITIES), false);
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
                        graphTraversal.planOperation(helper.getSession(), targetMultimap, true, true);
                if (!plan.getValue().isEmpty()) {
                    final Exception e = new IllegalStateException("querying the model graph does not delete any objects");
                    helper.cancel(new ERR(), e, "graph-fail");
                }
                return plan.getKey();
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
            SetMultimap<String, Long> result = (SetMultimap<String, Long>) object;
            result = Multimaps.filterKeys(result, new Predicate<String>() {
                @Override
                public boolean apply(String foundClassName) {
                    final Class<? extends IObject> foundClass;
                    try {
                        foundClass = Class.forName(foundClassName).asSubclass(IObject.class);
                    } catch (ClassCastException | ClassNotFoundException e) {
                        throw helper.cancel(new ERR(), e, "graph-fail");
                    }
                    for (final Class<? extends IObject> classToFind : classesToFind) {
                        if (classToFind.isAssignableFrom(foundClass)) {
                            return true;
                        }
                    }
                    return false;
                }});
            final Map<String, List<Long>> foundObjects = GraphUtil.copyMultimapForResponse(result);
            foundObjectCount += result.size();
            final FoundChildren response = new FoundChildren(foundObjects);
            helper.setResponseIfNull(response);
            helper.info("in finding children of " + targetObjectCount +
                    ", found " + foundObjectCount + " in total");

            if (LOGGER.isDebugEnabled()) {
                final GraphUtil.ParameterReporter arguments = new GraphUtil.ParameterReporter();
                arguments.addParameter("children", response.children);
                LOGGER.debug("response: " + arguments);
            }
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }
}
