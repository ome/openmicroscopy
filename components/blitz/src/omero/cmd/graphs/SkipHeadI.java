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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.system.Login;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.GraphModify2;
import omero.cmd.Response;
import omero.cmd.SkipHead;
import omero.cmd.State;
import omero.cmd.Status;

/**
 * The skip-head request performs the wrapped request twice: once in dry run mode to discover from which model objects to start,
 * and then actually starting from those objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class SkipHeadI extends SkipHead implements IRequest {

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private static final ImmutableSet<State> REQUEST_FAILURE_FLAGS = ImmutableSet.of(State.CANCELLED, State.FAILURE);

    private final GraphPathBean graphPathBean;
    private final GraphRequestFactory graphRequestFactory;
    private final Status graphRequestSkipStatus = new Status();
    private final Status graphRequestPerformStatus = new Status();
    private final List<Object> graphRequestSkipObjects = new ArrayList<Object>();
    private final List<Object> graphRequestPerformObjects = new ArrayList<Object>();

    private GraphModify2 graphRequestSkip;
    private GraphModify2 graphRequestPerform;

    private Helper helper;

    /**
     * Construct a new <q>skip-head</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphPathBean the graph path bean to use
     * @param graphRequestFactory a means of instantiating the sub-request
     * @throws GraphException if the request was not of an appropriate type
     */
    public SkipHeadI(GraphPathBean graphPathBean, GraphRequestFactory graphRequestFactory) {
        this.graphPathBean = graphPathBean;
        this.graphRequestFactory = graphRequestFactory;
    }

    @Override
    public Map<String, String> getCallContext() {
        return new HashMap<String, String>(ALL_GROUPS_CONTEXT);
    }

    @Override
    public void init(Helper helper) {
        final GraphPolicy.Action startAction;
        final WrappableRequest<GraphModify2> wrappedRequest;

        if (request == null) {
            throw new RuntimeException(new GraphException("must pass a request argument"));
        } else if (!(request instanceof WrappableRequest)) {
            throw new RuntimeException(new GraphException(
                    "cannot use " + SkipHead.class.getSimpleName() + " on " + request.getClass().getSimpleName()));
        } else {
            /* create the two wrapped requests */
            final Class<? extends GraphModify2> requestClass = request.getClass();
            wrappedRequest = (WrappableRequest<GraphModify2>) request;
            startAction = wrappedRequest.getActionForStarting();
            graphRequestSkip = graphRequestFactory.getRequest(requestClass);
            graphRequestPerform = graphRequestFactory.getRequest(requestClass);
            wrappedRequest.copyFieldsTo(graphRequestPerform);
            wrappedRequest.copyFieldsTo(graphRequestSkip);
            /* the skip-head half takes on the top-level options and does not modify any model objects */
            GraphUtil.copyFields(this, graphRequestSkip);
            graphRequestSkip.dryRun = true;
            if (dryRun) {
                graphRequestPerform.dryRun = true;
            }
        }

        /* adjust the requests' graph traversal policies */
        final SetMultimap<String, Long> permissionsOverrides = HashMultimap.create();
        ((WrappableRequest<?>) graphRequestSkip).adjustGraphPolicy(new Function<GraphPolicy, GraphPolicy>() {
            @Override
            public GraphPolicy apply(GraphPolicy graphPolicy) {
                try {
                    /* adjust skip-head half to stop when it reaches the model objects from which to start */
                    return SkipHeadPolicy.getSkipHeadPolicySkip(graphPolicy, graphPathBean, startFrom, startAction,
                            permissionsOverrides);
                } catch (GraphException e) {
                    throw new RuntimeException("graph traversal policy adjustment failed: " + e, e);
                }
            }
        });
        ((WrappableRequest<?>) graphRequestPerform).adjustGraphPolicy(new Function<GraphPolicy, GraphPolicy>() {
            @Override
            public GraphPolicy apply(GraphPolicy graphPolicy) {
                /* adjust tail half to propagate permissions overrides from skip-head half */
                return SkipHeadPolicy.getSkipHeadPolicyPerform(graphPolicy, permissionsOverrides);
            }
        });

        try {
            /* initialize the two wrapped requests */
            ((IRequest) graphRequestSkip).init(helper.subhelper(graphRequestSkip, graphRequestSkipStatus));
            ((IRequest) graphRequestPerform).init(helper.subhelper(graphRequestPerform, graphRequestPerformStatus));
        } catch (Cancel c) {
            /* mark own status as canceled */
            Throwable t = c.getCause();
            if (t == null) {
                t = c;
            }
            helper.fail(new ERR(), t, "graph-fail");
            helper.getStatus().flags.add(State.CANCELLED);
            /* re-throw wrapped request Cancel */
            throw c;
        } catch (Throwable t) {
            /* cancel because of wrapped request exception */
            throw helper.cancel(new ERR(), t, "graph-fail");
        }
        graphRequestSkipStatus.steps = 1 + wrappedRequest.getStepProvidingCompleteResponse();

        this.helper = helper;
        helper.setSteps(graphRequestSkipStatus.steps + graphRequestPerformStatus.steps);
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        if (step < graphRequestSkipStatus.steps) {
            try {
                /* do a skip-head step */
                graphRequestSkipStatus.currentStep = step;
                graphRequestSkipObjects.add(((IRequest) graphRequestSkip).step(step));
            } catch (Cancel e) {
                /* the step failed, so propagate the error response to this request */
                helper.getStatus().flags.addAll(REQUEST_FAILURE_FLAGS);
                helper.setResponseIfNull(((IRequest) graphRequestSkip).getResponse());
                throw e;
            }
        } else {
            final int substep = step - graphRequestSkipStatus.steps;
            if (substep == 0) {
                /* if the skip-head half is now completed, construct its response to feed into the tail half */
                for (int i = 0; i < graphRequestSkipStatus.steps; i++) {
                    ((IRequest) graphRequestSkip).buildResponse(i, graphRequestSkipObjects.get(i));
                }
                final Response response = ((IRequest) graphRequestSkip).getResponse();
                final Map<String, List<Long>> allTargetedObjects = ((WrappableRequest<?>) graphRequestSkip).getStartFrom(response);
                graphRequestPerform.targetObjects = new HashMap<String, List<Long>>();
                /* pick out the model objects matching the startFrom classes */
                for (String startFromClassName : startFrom) {
                    final int lastDot = startFromClassName.lastIndexOf('.');
                    if (lastDot > 0) {
                        startFromClassName = startFromClassName.substring(lastDot + 1);
                    }
                    final Class<? extends IObject> startFromClass = graphPathBean.getClassForSimpleName(startFromClassName);
                    for (final Map.Entry<String, List<Long>> targetedObjectsByClass : allTargetedObjects.entrySet()) {
                        final String targetedClassName = targetedObjectsByClass.getKey();
                        final Class<? extends IObject> targetedClass;
                        try {
                            targetedClass = Class.forName(targetedClassName).asSubclass(IObject.class);
                        } catch (ClassNotFoundException cnfe) {
                            final Exception e = new IllegalStateException(
                                    "response from " + graphRequestSkip.getClass() + " refers to class " + targetedClassName);
                            throw helper.cancel(new ERR(), e, "bad-class");
                        }
                        if (startFromClass.isAssignableFrom(targetedClass)) {
                            final List<Long> ids = targetedObjectsByClass.getValue();
                            graphRequestPerform.targetObjects.put(targetedClassName, ids);
                        }
                    }
                }
            }
            if (substep < graphRequestPerformStatus.steps) {
                try {
                    /* do a tail step */
                    graphRequestPerformStatus.currentStep = substep;
                    graphRequestPerformObjects.add(((IRequest) graphRequestPerform).step(substep));
                } catch (Cancel e) {
                    /* the step failed, so propagate the error response to this request */
                    helper.getStatus().flags.addAll(REQUEST_FAILURE_FLAGS);
                    helper.setResponseIfNull(((IRequest) graphRequestPerform).getResponse());
                    throw e;
                }
            } else {
                final Exception e = new IllegalArgumentException("model object graph operation has no step " + step);
                throw helper.cancel(new ERR(), e, "bad-step");
            }
        }
        return null;
    }

    @Override
    public void finish() {
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            /* use the response of the tail half */
            final IRequest tailHalf = (IRequest) graphRequestPerform;
            for (int substep = 0; substep < graphRequestPerformStatus.steps; substep++) {
                tailHalf.buildResponse(substep, graphRequestPerformObjects.get(substep));
            }
            final Response response = tailHalf.getResponse();
            helper.setResponseIfNull(response);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }
}
