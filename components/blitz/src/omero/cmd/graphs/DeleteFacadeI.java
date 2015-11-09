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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import ome.services.graphs.GraphException;
import omero.cmd.Delete;
import omero.cmd.Delete2Response;
import omero.cmd.DeleteRsp;
import omero.cmd.ERR;
import omero.cmd.GraphModify2;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

/**
 * Implements an approximation of {@link Delete}'s API using {@link omero.cmd.Delete2}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 * @deprecated will be removed in OMERO 5.3, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.2/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class DeleteFacadeI extends Delete implements IRequest {

    private final GraphRequestFactory graphRequestFactory;
    private final SetMultimap<String, Long> targetObjects = HashMultimap.create();

    private IRequest deleteRequest;
    private int steps;

    /**
     * Construct a new <q>delete</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphRequestFactory a means of instantiating a sub-request for root-anchored subgraphs
     */
    public DeleteFacadeI(GraphRequestFactory graphRequestFactory) {
        this.graphRequestFactory = graphRequestFactory;
        this.deleteRequest = graphRequestFactory.getRequest(Delete2I.class);
    }

    @Override
    public Map<String, String> getCallContext() {
       return deleteRequest.getCallContext();
    }

    /**
     * Add the given model object to the delete request's target objects.
     * To be called <em>before</em> {@link #init(Helper)}.
     * @param type the model object's type
     * @param id the model object's ID
     */
    public void addToTargets(String type, long id) {
        targetObjects.put(GraphUtil.getFirstClassName(type), id);
    }

    @Override
    public void init(Helper helper) {
        /* find class name at start of type string */
        if (type.charAt(0) == '/') {
            type = type.substring(1);
        }
        final Delete2I actualDelete = (Delete2I) deleteRequest;
        /* set target object then review options */
        addToTargets(type, id);
        actualDelete.targetObjects = new HashMap<String, List<Long>>();
        for (final Map.Entry<String, Collection<Long>> oneClassToTarget : targetObjects.asMap().entrySet()) {
            actualDelete.targetObjects.put(oneClassToTarget.getKey(), new ArrayList<Long>(oneClassToTarget.getValue()));
        }
        targetObjects.clear();
        try {
            GraphUtil.translateOptions(graphRequestFactory, options, actualDelete, helper.getEventContext().isCurrentUserAdmin());
        } catch (GraphException ge) {
            final Exception e = new IllegalArgumentException("could not accept request options");
            throw helper.cancel(new ERR(), e, "bad-options");
        }
        /* check for root-anchored subgraph */
        final int lastSlash = type.lastIndexOf('/');
        if (lastSlash > 0) {
            /* wrap in skip-head request for operating on subgraphs */
            final SkipHeadI wrapper = graphRequestFactory.getRequest(SkipHeadI.class);
            GraphUtil.copyFields(actualDelete, wrapper);
            wrapper.startFrom = Collections.singletonList(type.substring(lastSlash + 1));
            wrapper.request = actualDelete;
            deleteRequest = wrapper;
        }

        /* now the delete request is configured, complete initialization */
        deleteRequest.init(helper);
        steps = helper.getStatus().steps;
    }

    @Override
    public Object step(int step) throws Cancel {
        return deleteRequest.step(step);
    }

    @Override
    public void finish() {
        deleteRequest.finish();
    }

    @Override
    public void buildResponse(int step, Object object) {
        deleteRequest.buildResponse(step, object);
    }

    @Override
    public Response getResponse() {
        final Response responseToWrap = deleteRequest.getResponse();

        if (responseToWrap instanceof ERR) {
            return responseToWrap;
        }

        final Delete2Response actualResponse = (Delete2Response) responseToWrap;
        final DeleteRsp facadeResponse = new DeleteRsp();

        facadeResponse.warning = "";
        facadeResponse.steps = steps;
        facadeResponse.undeletedFiles = new HashMap<String, long[]>();

        if (actualResponse == null) {
            facadeResponse.scheduledDeletes = ((GraphModify2) deleteRequest).targetObjects.size();
            facadeResponse.actualDeletes = 0;
        } else {
            facadeResponse.scheduledDeletes = 0;
            facadeResponse.actualDeletes = actualResponse.deletedObjects.size();
        }

        return facadeResponse;
    }
}
