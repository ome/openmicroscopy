/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

    public void init(Status status, SqlAction sql, Session session, ome.system.ServiceFactory sf) {
        this.helper = new Helper(this, status, sql, session, sf);
        status.steps = 0;
        for (Request req: this.list) {
            Status substatus = new Status();
            if (req instanceof IRequest) {
                IRequest ireq = (IRequest) req;
                ireq.init(substatus, sql, session, sf);
                status.steps += substatus.steps;
                statuses.add(substatus);
                offsets.add(status.steps);
            } else {
                log.error("Bad request: " + req);
                substatus.steps = 0;
                statuses.add(substatus);
                offsets.add(status.steps);
            }
        }
    }

    public void step(int step) {

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
            return; // Nothing found
        }

        Request subrequest = list.get(i);
        // At this point, it must also be an IRequest, because otherwise the
        // offset would have not changed.
        ((IRequest) subrequest).step(step - last);
    }

    public void finish() {
        for (Request subreq: list) {
            // Again, must be an irequest
            IRequest ireq = (IRequest) subreq;
            ireq.finish();
            responses.add(ireq.getResponse());
        }
        DoAllRsp rsp = new DoAllRsp(responses);
        helper.setResponse(rsp);
    }

    public Response getResponse() {
        return helper.getResponse();
    }
}
