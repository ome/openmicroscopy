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

import java.util.List;
import java.util.Map;

import ome.services.graphs.GraphPolicy;
import omero.cmd.IRequest;
import omero.cmd.Response;

import com.google.common.base.Function;

/**
 * Requests that can be wrapped by a {@link SkipHeadI} request.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public interface WrappableRequest<X> extends IRequest {
    /**
     * Copy the fields of this request to that of the given request.
     * @param request the target of the field copy
     */
    void copyFieldsTo(X request);

    /**
     * Transform the currently applicable graph policy for this request by the given function.
     * Must be called before {@link #init(omero.cmd.Helper)}.
     * @param adjuster a transformation function for graph policies
     */
    void adjustGraphPolicy(Function<GraphPolicy, GraphPolicy> adjuster);

    /**
     * Get the step of this request that suffices for assembling the request's response.
     * It is presumed that checking the permissibility of the planned operation occurs afterward.
     * @return a step number
     */
    int getStepProvidingCompleteResponse();

    /**
     * @return the action associated with nodes qualifying as start objects
     */
    GraphPolicy.Action getActionForStarting();

    /**
     * From the response of the head-skipping request, determine which model objects are the targets of the operation.
     * @param response the head-skipping request's response
     * @return the model objects to target
     */
    Map<String, List<Long>> getStartFrom(Response response);
}
