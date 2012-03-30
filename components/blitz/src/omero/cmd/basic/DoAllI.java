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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import ome.util.SqlAction;

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

    private final Log log = LogFactory.getLog(DoAllI.class);

    private static final long serialVersionUID = -323423435135556L;

    /**
     * Provides the offsets of each status into the total number
     * of offsets. For each if there are 3 actions, each of which
     * contain 3 steps, then the offsets list will contain 3, 6 and
     * 9. Then if step 4 is requested, only 2 steps will need to be
     * made through the offset list.
     */
    private List<Integer> offsets = new ArrayList<Integer>();

    private List<Status> statuses = new ArrayList<Status>();

    private List<Response> responses = new ArrayList<Response>();

    private Helper helper;

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Status status, SqlAction sql, Session session,
            ome.system.ServiceFactory sf) {
        this.helper = new Helper(this, status, sql, session, sf);
        status.steps = 0;
        for (Request req : this.requests) {
            Status substatus = new Status();
            if (req instanceof IRequest) {
                IRequest ireq = (IRequest) req;
                ireq.init(substatus, sql, session, sf);
                status.steps += substatus.steps;
                statuses.add(substatus);
                offsets.add(status.steps);
            }
            else {
                log.error("Bad request: " + req);
                substatus.steps = 0;
                statuses.add(substatus);
                offsets.add(status.steps);
            }
        }
    }

    public Object step(int step) {
        helper.assertStep(step);
        Pointer p = new Pointer(this, step);

        try {
            return p.step();
        }
        catch (Cancel c) {
            // TODO: Better to have our own ERR here with the responses
            // of all the other subrequests for partial results.
            helper.cancel(new ERR(), c, "subrequest-cancel");
            return null; // Never reached.
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        Pointer p = new Pointer(this, step);
        p.buildResponse(object);

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

    /**
     * Class to calculate which subrequest is intended by a given top-level
     * step. For example, if the subrequests have the steps:
     * 
     * <pre>
     * [0,1,2], [0,1,2,3], [0,1,2]
     * </pre>
     * 
     * these would map to:
     * 
     * <pre>
     * [0,1,2,  3,4,5,6,    7,8,9]
     * </pre>
     * 
     * And if 5 were mapped in for "step" then the {@link #req} instance would
     * be the second from {@link DoAllI#list} and the substep value would be 2.
     */
    static class Pointer {
        IRequest req;
        int substep;

        Pointer(DoAllI doall, int step) {
            List<Integer> offsets = doall.offsets;
            // Find the right offset
            int i = 0;
            int last = 0;
            int current = 0;
            while (i < offsets.size()) {
                current = offsets.get(i);
                if (step < current) {
                    break;
                }
                ++i;
                last = current;
            }

            if (i > offsets.size()) {
                throw new RuntimeException(
                        "Wrong step! This should never happen!");
            }

            // At this point, it must also be an IRequest, because otherwise the
            // offset would have not changed.
            Request subrequest = doall.requests.get(i);
            req = (IRequest) subrequest;
            substep = step - last;
        }

        Object step() {
            return req.step(substep);
        }

        void buildResponse(Object object) {
            req.buildResponse(substep, object);
        }
    }
}
