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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import omero.cmd.Delete;
import omero.cmd.Delete2Response;
import omero.cmd.DeleteRsp;
import omero.cmd.GraphModify2;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

/**
 * Implements an approximation of {@link Delete}'s API using {@link omero.cmd.Delete2}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class DeleteFacadeI extends Delete implements IRequest {

    private final GraphRequestFactory graphRequestFactory;

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

    @Override
    public void init(Helper helper) {
        /* find class name at start of type string */
        if (type.charAt(0) == '/') {
            type = type.substring(1);
        }
        final int firstSlash = type.indexOf('/');
        final String targetType = firstSlash < 0 ? type : type.substring(0, firstSlash);
        final Delete2I actualDelete = (Delete2I) deleteRequest;
        /* set target object then review options */
        actualDelete.targetObjects = ImmutableMap.of(targetType, new long[] {id});
        GraphUtil.translateOptions(graphRequestFactory, options, actualDelete);
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
        final Delete2Response actualResponse = (Delete2Response) deleteRequest.getResponse();
        final DeleteRsp facadeResponse = new DeleteRsp();

        facadeResponse.warning = "";
        facadeResponse.steps = steps;
        facadeResponse.undeletedFiles = new HashMap<String, long[]>();

        if (actualResponse == null) {
            facadeResponse.scheduledDeletes = GraphUtil.getIdListMapSize(((GraphModify2) deleteRequest).targetObjects);
            facadeResponse.actualDeletes = 0;
        } else {
            facadeResponse.scheduledDeletes = 0;
            facadeResponse.actualDeletes = GraphUtil.getIdListMapSize(actualResponse.deletedObjects);
        }

        return facadeResponse;
    }
}
