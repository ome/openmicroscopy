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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import ome.model.IObject;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.system.Login;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Request2;
import omero.cmd.Response;
import omero.cmd.SkipHead;
import omero.cmd.Status;

/**
 * The skip-head request performs the wrapped request twice: once in dry run mode to discover from which model objects to start,
 * and then actually starting from those objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class SkipHeadI extends SkipHead implements IRequest {

    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private final GraphPathBean graphPathBean;
    private final GraphRequestFactory graphRequestFactory;
    private final Status graphRequestSkipStatus = new Status();
    private final Status graphRequestPerformStatus = new Status();
    private final List<Object> graphRequestSkipObjects = new ArrayList<Object>();
    private final List<Object> graphRequestPerformObjects = new ArrayList<Object>();

    private Request2 graphRequestSkip;
    private Request2 graphRequestPerform;

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
    public ImmutableMap<String, String> getCallContext() {
       return ALL_GROUPS_CONTEXT;
    }

    @Override
    public void init(Helper helper) {
        if (request == null) {
            throw new RuntimeException(new GraphException("must pass a request argument"));
        } else if (!(request instanceof WrappableRequest)) {
            throw new RuntimeException(new GraphException(
                    "cannot use " + SkipHead.class.getSimpleName() + " on " + request.getClass().getSimpleName()));
        } else {
            /* create the two wrapped requests */
            final Class<? extends Request2> requestClass = request.getClass();
            final WrappableRequest<Request2> wrappedRequest = (WrappableRequest<Request2>) request;
            graphRequestSkip = graphRequestFactory.getRequest(requestClass);
            graphRequestPerform = graphRequestFactory.getRequest(requestClass);
            wrappedRequest.copyFieldsTo(graphRequestPerform);
            wrappedRequest.copyFieldsTo(graphRequestSkip);
            /* the skip-head half takes on the top-level options and does not modify any model objects */
            copyFieldsTo(graphRequestSkip);
            graphRequestSkip.dryRun = true;
            if (dryRun) {
                graphRequestPerform.dryRun = true;
            }
        }

        /* initialize the two wrapped requests */
        ((IRequest) graphRequestSkip).init(helper.subhelper(graphRequestSkip, graphRequestSkipStatus));
        ((IRequest) graphRequestPerform).init(helper.subhelper(graphRequestPerform, graphRequestPerformStatus));

        /* adjust skip-head half to stop when it reaches the model objects from which to start */
        ((WrappableRequest<?>) graphRequestSkip).adjustGraphPolicy(new Function<GraphPolicy, GraphPolicy>() {
            @Override
            public GraphPolicy apply(GraphPolicy graphPolicy) {
                try {
                    return SkipHeadPolicy.getSkipHeadPolicy(graphPolicy, graphPathBean, startFrom);
                } catch (GraphException e) {
                    throw new RuntimeException("graph traversal policy adjustment failed: " + e, e);
                }
            }
        });

        this.helper = helper;
        helper.setSteps(graphRequestSkipStatus.steps + graphRequestPerformStatus.steps);
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        if (step < graphRequestSkipStatus.steps) {
            /* do a skip-head step */
            graphRequestSkipObjects.add(((IRequest) graphRequestSkip).step(step));
        } else {
            final int substep = step - graphRequestSkipStatus.steps;
            if (substep == 0) {
                /* if the skip-head half is now completed, construct its response to feed into the tail half */
                for (int i = 0; i < graphRequestSkipStatus.steps; i++) {
                    ((IRequest) graphRequestSkip).buildResponse(i, graphRequestSkipObjects.get(i));
                }
                final Response response = ((IRequest) graphRequestSkip).getResponse();
                final Map<String, long[]> allTargetedObjects = ((WrappableRequest<?>) graphRequestSkip).getStartFrom(response);
                graphRequestPerform.targetObjects = new HashMap<String, long[]>();
                /* pick out the model objects matching the startFrom classes */
                for (String startFromClassName : startFrom) {
                    final int lastDot = startFromClassName.lastIndexOf('.');
                    if (lastDot > 0) {
                        startFromClassName = startFromClassName.substring(lastDot + 1);
                    }
                    final Class<? extends IObject> startFromClass = graphPathBean.getClassForSimpleName(startFromClassName);
                    for (final Map.Entry<String, long[]> targetedObjectsByClass : allTargetedObjects.entrySet()) {
                        final String targetedClassName = targetedObjectsByClass.getKey();
                        final Class<? extends IObject> targetedClass;
                        try {
                            targetedClass = Class.forName(targetedClassName).asSubclass(IObject.class);
                        } catch (ClassNotFoundException e) {
                            throw helper.cancel(new ERR(), new IllegalStateException(),
                                    "response from " + graphRequestSkip.getClass() + " refers to class " + targetedClassName);
                        }
                        if (startFromClass.isAssignableFrom(targetedClass)) {
                            final long[] ids = targetedObjectsByClass.getValue();
                            graphRequestPerform.targetObjects.put(targetedClassName, ids);
                        }
                    }
                }
            }
            if (substep < graphRequestPerformStatus.steps) {
                /* do a tail step */
                graphRequestPerformObjects.add(((IRequest) graphRequestPerform).step(substep));
            } else {
                throw helper.cancel(new ERR(), new IllegalArgumentException(), "model object graph operation has no step " + step);
            }
            if (substep == graphRequestPerformStatus.steps - 1) {
                /* now completed the last step, so construct the response */
                for (int i = 0; i < graphRequestPerformStatus.steps; i++) {
                    ((IRequest) graphRequestPerform).buildResponse(i, graphRequestPerformObjects.get(i));
                }
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
            final Response response = ((IRequest) graphRequestPerform).getResponse();
            helper.setResponseIfNull(response);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /**
     * Copy the fields of this request to that of the given request.
     * @param request the target of the field copy
     */
    public void copyFieldsTo(Request2 request) {
        request.dryRun = dryRun;
        request.targetObjects = targetObjects;
        request.includeNs = includeNs;
        request.excludeNs = excludeNs;
        request.includeChild = includeChild;
        request.excludeChild = excludeChild;
    }
}
