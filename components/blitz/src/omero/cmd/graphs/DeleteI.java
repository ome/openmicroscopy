/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;

import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.system.EventContext;

import omero.cmd.Delete;
import omero.cmd.DeleteRsp;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.Response;

/**
 * Replaces {@link ome.services.blitz.impl.DeleteHandleI} in order to have all asynchronous invocations
 * in a single API.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class DeleteI extends Delete implements IGraphModifyRequest {

    private static final long serialVersionUID = -3653081139095111039L;

    private final Deletion delegate;

    private/* final */Helper helper;

    private final Ice.Communicator ic;

    public DeleteI(Ice.Communicator ic, Deletion delegate) {
        this.ic = ic;
        this.delegate = delegate;
    }

    //
    // Methods required by refactoring of DeleteHandleI
    //
    public Deletion getDeletion() {
        return delegate;
    }

    //
    // IGraphModifyRequest
    //

    @Override
    public IGraphModifyRequest copy() {
        DeleteI copy = (DeleteI) ic.findObjectFactory(ice_id()).create(DeleteI.ice_staticId());
        copy.type = type;
        copy.id = id;
        return copy;
    }

    //
    // IRequest
    //

    public Map<String, String> getCallContext() {
        Map<String, String> negOne = new HashMap<String, String>();
        negOne.put("omero.group", "-1");
        return negOne;
    }

    public void init(Helper helper) {
        this.helper = helper;
        try {
            EventContext ec = helper.getEventContext();
            int steps = delegate.start(ec, helper.getSql(), helper.getSession(), type, id, options);
            helper.setSteps(steps);
        } catch (GraphException e) {
            this.helper.cancel(err(), e, "graph-state", "type", type, "id",
                    ""+id, "Error");
        }
    }

    public Object step(int i) throws Cancel {
        helper.assertStep(i);

        try {
            delegate.execute(i);
            return null;
        // This hierarchy is duplicated in Deletion
        } catch (GraphException ge) {
            throw helper.graphException(ge, i, id);
        } catch (ConstraintViolationException cve) {
            throw helper.cancel(err(), cve, "constraint-violation", "name",
                    cve.getConstraintName());
        } catch (Throwable t) {
            throw helper.cancel(err(), t, "failure");
        }
    }

    @Override
    public void finish() throws Cancel {
        try {
            delegate.finish();
        } catch (GraphException ge) {
            throw helper.graphException(ge, helper.getSteps()+1, id);
        } catch (Throwable t) {
            throw helper.cancel(err(), t, "on-finish");
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {

            try {
                // We're outside of the tx now
                delegate.deleteFiles();
            } finally {
                delegate.stop();
            }

            // Report after calling stop.
            DeleteRsp rsp = new DeleteRsp();
            rsp.actualDeletes = delegate.getActualDeletes();
            rsp.scheduledDeletes = delegate.getScheduledDeletes();
            rsp.steps = delegate.getSteps();
            rsp.undeletedFiles = delegate.getUndeletedFiles();
            rsp.warning = delegate.getWarning();
            helper.setResponseIfNull(rsp);

        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // Helpers
    //

    private ERR err() {
        ERR err = new ERR();
        err.parameters = new HashMap<String, String>();
        if (delegate != null) {
            String warnMsg = delegate.getWarning();
            String errMsg = delegate.getError();
            if (warnMsg != null && warnMsg.length() > 0) {
                err.parameters.put("Warning", warnMsg);
            }
            if (errMsg != null && errMsg.length() > 0) {
                err.parameters.put("Error", errMsg);
            }
        }
        return err;
    }

}
