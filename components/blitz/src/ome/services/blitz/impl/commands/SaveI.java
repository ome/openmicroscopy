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

import ome.api.IUpdate;
import ome.model.IObject;
import omero.api.Save;
import omero.api.SaveRsp;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;
import omero.util.IceMapper;

/**
 * Permits saving a single IObject instance.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class SaveI extends Save implements IRequest {

    private static final long serialVersionUID = -3434345656L;

    private Helper helper;

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        this.helper.setSteps(1);
    }

    public Object step(int step) {
        helper.assertStep(0, step);
        try {
            IceMapper mapper = new IceMapper();
            IObject iobj = (IObject) mapper.reverse(this.obj);
            IUpdate update = helper.getServiceFactory().getUpdateService();
            helper.info("saveAndReturnObject(%s)", iobj);
            return update.saveAndReturnObject(iobj);
        }
        catch (Throwable t) {
            // TODO: Should probably not catch throwable, but more specific
            // conditions.
            throw helper.cancel(new ERR(), t, "failed", "obj",
                    String.format("%s", this.obj));
        }
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertStep(0, step);
        if (helper.isLast(step)) {
            IceMapper mapper = new IceMapper();
            SaveRsp rsp = new SaveRsp(
                    (omero.model.IObject) mapper.map((IObject) object));
            helper.setResponseIfNull(rsp);
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }
}
