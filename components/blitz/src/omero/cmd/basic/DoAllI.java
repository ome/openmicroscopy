/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import omero.cmd.DoAll;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;

/**
 * Permits performing multiple operations
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class DoAllI extends DoAll implements IRequest {

    /**
     * Pointer-like object which is saved for each
     * sub-request. The logic for properly mapping
     * from the global step number to the individual
     * substep number is done here. In order to find
     * the proper {@link X} instance, use the lookup
     * table:
     *
     * <pre>
     * X x = substeps.get(lookup[step]);
     * </pre>
     */
    private static class X {
        /** Number of steps that should be deducted from the global step
         * count in order to have the proper substep number */
        final int offset;
        final Helper h;
        final IRequest r;
        X(int offset, Helper h, IRequest r) {
            this.offset = offset;
            this.h = h;
            this.r = r;
        }
        Object step(int step) {
            return r.step(step - offset);
        }
        void buildResponse(int step, Object object) {
            r.buildResponse(step - offset, object);
        }
    }

    private static final long serialVersionUID = -323423435135556L;

    private final List<Status> statuses = new ArrayList<Status>();

    private final List<Response> responses = new ArrayList<Response>();

    /**
     * Helper instance for this class. Will create a number of sub-helper
     * instances for each request.
     */
    private Helper helper;

    /**
     * State-objects for each subrequest
     */
    private final List<X> substeps = new ArrayList<X>();

    /**
     * Used to find the proper {@link X} instance in {@link #substeps}
     */
    private int[] lookup;

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        int steps = 0;
        try {
            for (Request req : this.requests) {
                final Status substatus = new Status();
                final Helper subhelper = helper.subhelper(req, substatus);
                if (req instanceof IRequest) {
                    try {
                        ((IRequest) req).init(subhelper);
                        statuses.add(substatus);
                        substeps.add(new X(steps, subhelper, (IRequest) req));
                        steps += substatus.steps;
                    } catch (Cancel c) {
                        helper.setResponse(subhelper.getResponse());
                        throw c;
                    }
                }
                else {
                    throw helper.cancel(new ERR(), null, "bad-request",
                        "type", req.ice_id());
                }
            }
            int count = 0;
            lookup = new int[steps];
            for (int i = 0; i < substeps.size(); i++) {
                X x = substeps.get(i);
                for (int j = 0; j < x.h.getSteps(); j++) {
                    lookup[count] = i;
                    count++;
                }
            }
        }  catch (Cancel c) {
            throw c; // just re-throw
        } catch (Throwable t) {
            helper.cancel(new ERR(), t, "bad-init");
        }
        helper.setSteps(steps);
    }

    public Object step(int step) {
        helper.assertStep(step);
        final X x = substeps.get(lookup[step]);
        try {
            return x.step(step);
        }
        catch (Cancel c) {
            Response subrsp = x.h.getResponse();
            helper.setResponse(subrsp);
            throw c;
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        X x = substeps.get(lookup[step]);
        x.buildResponse(step, object);

        if (helper.isLast(step)) {
            for (Request subreq : requests) {
                // Again, must be an irequest
                IRequest ireq = (IRequest) subreq;
                responses.add(ireq.getResponse());
            }
            DoAllRsp rsp = new DoAllRsp(responses, statuses);
            helper.setResponse(rsp);
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

}
