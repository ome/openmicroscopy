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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;

import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;

import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteReport;
import omero.cmd.Delete;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;

/**
 * Replaces {@link ome.services.blitz.impl.DeleteHandleI} in order to have all asynchronous invocations
 * in a single API.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class DeleteI extends Delete implements IRequest {

    private static final long serialVersionUID = -3653081139095111039L;

    private final Deletion delegate;

    private/* final */Helper helper;

    public DeleteI(Deletion delegate) {
        this.delegate = delegate;
    }

    //
    // Methods required by refactoring of DeleteHandleI
    //
    public Deletion getDeletion() {
        return delegate;
    }


    public DeleteReport getDeleteReport() {
        DeleteReport rpt = new DeleteReport();
        rpt.command = new DeleteCommand(type, id, options);
        rpt.actualDeletes = delegate.getActualDeletes();
        rpt.error = delegate.getError();
        rpt.scheduledDeletes = delegate.getScheduledDeletes();
        rpt.start = delegate.getStart();
        rpt.stop = delegate.getStop();
        rpt.undeletedFiles = delegate.getUndeletedFiles();
        rpt.warning = delegate.getWarning();
        return rpt;
    }

    //
    // IRequest API
    //

    public Map<String, String> getCallContext() {
        Map<String, String> negOne = new HashMap<String, String>();
        negOne.put("omero.group", "-1");
        return negOne;
    }

    public void init(Helper helper) {
        this.helper = helper;
        try {
            int steps = delegate.start(helper.getSql(), helper.getSession(), type, id, options);
            helper.setSteps(steps);
        } catch (GraphException e) {
            this.helper.cancel(new ERR(), e, "graph-state", "type", type, "id",
                    ""+id);
        }
    }

    public Object step(int i) throws Cancel {
        helper.assertStep(i);

        try {
            delegate.execute(i);
            return null;
        // This hierarchy is duplicated in Deletion
        } catch (GraphException ge) {
            throw helper.cancel(new ERR(), ge, "STEP ERR", "step", ""+i, "id", ""+id);
        } catch (ConstraintViolationException cve) {
            throw helper.cancel(new ERR(), cve, "constraint-violation", "name",
                    cve.getConstraintName());
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "failure");
        }
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {

            try {
                // We're outside of the tx now
                delegate.deleteFiles();
                
                helper.setResponseIfNull(new OK());
            } finally {
                delegate.stop();
            }
        }
    }
    
    public Response getResponse() {
        return helper.getResponse();
    }


}
