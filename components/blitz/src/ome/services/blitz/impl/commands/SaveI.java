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

package ome.services.blitz.impl.commands;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import ome.api.IUpdate;
import ome.model.IObject;
import ome.util.SqlAction;

import omero.api.Save;
import omero.api.SaveRsp;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.Status;
import omero.util.IceMapper;

/**
 * Permits saving a single IObject instance.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class SaveI extends Save implements IRequest {

    private final Log log = LogFactory.getLog(SaveI.class);

    private static final long serialVersionUID = -3434345656L;

    private Helper helper;

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Status status, SqlAction sql, Session session,
            ome.system.ServiceFactory sf) {
        this.helper = new Helper(this, status, sql, session, sf);
        status.steps = 1;
    }

    public void step(int step) {
        if (step != 0) {
            helper.cancel(new ERR(), null, "bad step", "step", ""+step);
            return;
        }
        try {
            IceMapper mapper = new IceMapper();
            IObject iobj = (IObject) mapper.reverse(this.obj);
            IUpdate update = helper.getServiceFactory().getUpdateService();
            iobj = update.saveAndReturnObject(iobj);
            SaveRsp rsp = new SaveRsp((omero.model.IObject) mapper.map(iobj));
            helper.setResponse(rsp);
        }
        catch (Throwable t) {
            helper.fail(new ERR(), "error", "obj",
                    String.format("%s", this.obj));
        }
    }

    public void finish() {
        // no-op
    }

    public Response getResponse() {
        return helper.getResponse();
    }
}
