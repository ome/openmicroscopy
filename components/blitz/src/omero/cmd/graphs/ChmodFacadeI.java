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
import omero.cmd.Chmod;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

/**
 * Implements an approximation of {@link Chmod}'s API using {@link omero.cmd.Chmod2}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.2
 * @deprecated will be removed in OMERO 5.3, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChmodFacadeI extends Chmod implements IRequest {

    private final GraphRequestFactory graphRequestFactory;
    private final SetMultimap<String, Long> targetObjects = HashMultimap.create();

    private IRequest chmodRequest;

    /**
     * Construct a new <q>chmod</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphRequestFactory a means of instantiating a sub-request for root-anchored subgraphs
     */
    public ChmodFacadeI(GraphRequestFactory graphRequestFactory) {
        this.graphRequestFactory = graphRequestFactory;
        this.chmodRequest = graphRequestFactory.getRequest(Chmod2I.class);
    }

    @Override
    public Map<String, String> getCallContext() {
       return chmodRequest.getCallContext();
    }

    /**
     * Add the given model object to the chmod request's target objects.
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
        final Chmod2I actualChmod = (Chmod2I) chmodRequest;
        /* set target object and group then review options */
        addToTargets(type, id);
        actualChmod.targetObjects = new HashMap<String, List<Long>>();
        for (final Map.Entry<String, Collection<Long>> oneClassToTarget : targetObjects.asMap().entrySet()) {
            actualChmod.targetObjects.put(oneClassToTarget.getKey(), new ArrayList<Long>(oneClassToTarget.getValue()));
        }
        targetObjects.clear();
        actualChmod.permissions = permissions;
        try {
            GraphUtil.translateOptions(graphRequestFactory, options, actualChmod, helper.getEventContext().isCurrentUserAdmin());
        } catch (GraphException ge) {
            final Exception e = new IllegalArgumentException("could not accept request options");
            throw helper.cancel(new ERR(), e, "bad-options");
        }
        /* check for root-anchored subgraph */
        final int lastSlash = type.lastIndexOf('/');
        if (lastSlash > 0) {
            /* wrap in skip-head request for operating on subgraphs */
            final SkipHeadI wrapper = graphRequestFactory.getRequest(SkipHeadI.class);
            GraphUtil.copyFields(actualChmod, wrapper);
            wrapper.startFrom = Collections.singletonList(type.substring(lastSlash + 1));
            wrapper.request = actualChmod;
            chmodRequest = wrapper;
        }

        /* now the chmod request is configured, complete initialization */
        chmodRequest.init(helper);
    }

    @Override
    public Object step(int step) throws Cancel {
        return chmodRequest.step(step);
    }

    @Override
    public void finish() {
        chmodRequest.finish();
    }

    @Override
    public void buildResponse(int step, Object object) {
        chmodRequest.buildResponse(step, object);
    }

    @Override
    public Response getResponse() {
        final Response responseToWrap = chmodRequest.getResponse();

        if (responseToWrap instanceof ERR) {
            return responseToWrap;
        }

        /* no actual wrapping needed */
        return new OK();
    }
}
