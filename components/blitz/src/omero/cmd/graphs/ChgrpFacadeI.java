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
import omero.cmd.Chgrp;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

/**
 * Implements an approximation of {@link Chgrp}'s API using {@link omero.cmd.Chgrp2}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 * @deprecated will be removed in OMERO 5.3, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ChgrpFacadeI extends Chgrp implements IRequest {

    private final GraphRequestFactory graphRequestFactory;
    private final SetMultimap<String, Long> targetObjects = HashMultimap.create();

    private IRequest chgrpRequest;

    /**
     * Construct a new <q>chgrp</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphRequestFactory a means of instantiating a sub-request for root-anchored subgraphs
     */
    public ChgrpFacadeI(GraphRequestFactory graphRequestFactory) {
        this.graphRequestFactory = graphRequestFactory;
        this.chgrpRequest = graphRequestFactory.getRequest(Chgrp2I.class);
    }

    @Override
    public Map<String, String> getCallContext() {
       return chgrpRequest.getCallContext();
    }

    /**
     * Add the given model object to the chgrp request's target objects.
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
        final Chgrp2I actualChgrp = (Chgrp2I) chgrpRequest;
        /* set target object and group then review options */
        addToTargets(type, id);
        actualChgrp.targetObjects = new HashMap<String, List<Long>>();
        for (final Map.Entry<String, Collection<Long>> oneClassToTarget : targetObjects.asMap().entrySet()) {
            actualChgrp.targetObjects.put(oneClassToTarget.getKey(), new ArrayList<Long>(oneClassToTarget.getValue()));
        }
        targetObjects.clear();
        actualChgrp.groupId = grp;
        try {
            GraphUtil.translateOptions(graphRequestFactory, options, actualChgrp, helper.getEventContext().isCurrentUserAdmin());
        } catch (GraphException ge) {
            final Exception e = new IllegalArgumentException("could not accept request options");
            throw helper.cancel(new ERR(), e, "bad-options");
        }
        /* check for root-anchored subgraph */
        final int lastSlash = type.lastIndexOf('/');
        if (lastSlash > 0) {
            /* wrap in skip-head request for operating on subgraphs */
            final SkipHeadI wrapper = graphRequestFactory.getRequest(SkipHeadI.class);
            GraphUtil.copyFields(actualChgrp, wrapper);
            wrapper.startFrom = Collections.singletonList(type.substring(lastSlash + 1));
            wrapper.request = actualChgrp;
            chgrpRequest = wrapper;
        }

        /* now the chgrp request is configured, complete initialization */
        chgrpRequest.init(helper);
    }

    @Override
    public Object step(int step) throws Cancel {
        return chgrpRequest.step(step);
    }

    @Override
    public void finish() {
        chgrpRequest.finish();
    }

    @Override
    public void buildResponse(int step, Object object) {
        chgrpRequest.buildResponse(step, object);
    }

    @Override
    public Response getResponse() {
        final Response responseToWrap = chgrpRequest.getResponse();

        if (responseToWrap instanceof ERR) {
            return responseToWrap;
        }

        /* no actual wrapping needed */
        return new OK();
    }
}
