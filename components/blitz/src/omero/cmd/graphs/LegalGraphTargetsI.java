/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
import java.util.Map;

import ome.model.IObject;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.LegalGraphTargets;
import omero.cmd.LegalGraphTargetsResponse;
import omero.cmd.Response;

/**
 * Query which model object classes are legal as targets for a request.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.4
 */
public class LegalGraphTargetsI extends LegalGraphTargets implements IRequest {

    private final GraphRequestFactory graphRequestFactory;
    private Helper helper;

    /**
     * Construct a new legal graph targets query.
     * @param graphRequestFactory the graph request factory
     */
    public LegalGraphTargetsI(GraphRequestFactory graphRequestFactory) {
        this.graphRequestFactory = graphRequestFactory;
    }

    @Override
    public Map<String, String> getCallContext() {
        return null;
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(1);
    }

    @Override
    public Object step(int step) throws Cancel {
        if (step == 0) {
            try {
                return graphRequestFactory.getLegalTargets(request.getClass());
            } catch (Exception e) {
                throw helper.cancel(new ERR(), e, "graph-no-targets");
            }
        } else {
            final Exception e = new IllegalArgumentException("request has no step " + step);
            throw helper.cancel(new ERR(), e, "bad-step");
        }
    }

    @Override
    public void finish() throws Cancel {
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            final LegalGraphTargetsResponse response = new LegalGraphTargetsResponse();
            final Collection<Class<? extends IObject>> legalTargetClasses = (Collection<Class<? extends IObject>>) object;
            response.targets = new ArrayList<String>(legalTargetClasses.size());
            for (final Class<? extends IObject> legalTargetClass : legalTargetClasses) {
                response.targets.add(legalTargetClass.getName());
            }
            helper.setResponseIfNull(response);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }
}
