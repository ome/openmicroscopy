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
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import omero.cmd.Chgrp;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

/**
 * Implements an approximation of {@link Chgrp}'s API using {@link omero.cmd.Chgrp2}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class ChgrpFacadeI extends Chgrp implements IRequest {

    private final GraphRequestFactory graphRequestFactory;

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

    @Override
    public void init(Helper helper) {
        /* find class name at start of type string */
        if (type.charAt(0) == '/') {
            type = type.substring(1);
        }
        final int firstSlash = type.indexOf('/');
        final String targetType = firstSlash < 0 ? type : type.substring(0, firstSlash);
        final Chgrp2I actualChgrp = (Chgrp2I) chgrpRequest;
        /* set target object and group then review options */
        actualChgrp.targetObjects = ImmutableMap.of(targetType, new long[] {id});
        actualChgrp.groupId = grp;
        GraphUtil.translateOptions(graphRequestFactory, options, actualChgrp);
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
        chgrpRequest.getResponse();
        return new OK();
    }
}
