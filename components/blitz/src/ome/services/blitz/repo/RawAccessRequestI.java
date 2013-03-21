/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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

package ome.services.blitz.repo;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.io.nio.FileBuffer;
import ome.services.blitz.fire.Registry;

import omero.ServerError;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.Unknown;
import omero.grid.InternalRepositoryPrx;
import omero.grid.RawAccessRequest;
import omero.grid.RepositoryException;

/**
 * Command request for accessing a repository directly.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5.0
 */
public class RawAccessRequestI extends RawAccessRequest implements IRequest {

    private static final long serialVersionUID = -303948503984L;

    private static Log log = LogFactory.getLog(RawAccessRequestI.class);

    private final Registry reg;

    protected Helper helper;

    protected InternalRepositoryPrx repo;

    public RawAccessRequestI(Registry reg) {
        this.reg = reg;
    }

    //
    // IRequest methods
    //

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;

        if (!helper.getEventContext().isCurrentUserAdmin()) {
            throw helper.cancel(new ERR(), new omero.SecurityViolation(),
                    "not-admin");
        }

        log.debug("Looking repo " + repoUuid);
        try {
            String proposedName = "InternalRepository-" + repoUuid;
            InternalRepositoryPrx[] proxies = reg.lookupRepositories();
            for (InternalRepositoryPrx prx : proxies) {
                Ice.Identity id = prx.ice_getIdentity();
                if (proposedName.equals(id.name)) {
                    repo = prx;
                    log.debug("Found repo " + repoUuid);
                    break;
                }
            }
        }
        catch (Exception e) {
            throw helper.cancel(new ERR(), e, "registry-lookup", "repoUuid", repoUuid);
        }

        if (repo == null) {
            throw helper.cancel(new Unknown(), null, "unknown-repo", "repoUuid",
                    repoUuid);
        }
        this.helper.setSteps(1);

    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        return rawAccess();
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        helper.setResponseIfNull(((Response) object));
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    public Response rawAccess() {
        try {
            log.debug("Calling raw access for command " + command);
            return repo.rawAccess(this);
        }
        catch (ServerError e) {
            throw helper.cancel(new ERR(), e, "raw-access");
        } finally {
            log.debug("Done calling raw access for command " + command);
        }
    }

    /**
     * Method called locally to the repository during {@link #rawAccess()}. Only
     * the remoteable fields should be accessed during this method call since this
     * will be a copy of the object originally called.
     *
     * @param abstractRepositoryI
     * @param servant
     * @param __current
     */
    public void local(AbstractRepositoryI abstractRepositoryI,
            PublicRepositoryI servant, Current __current) throws Exception {

        if ("touch".equals(command)) {
            for (String arg : args) {
                final CheckedPath checked = servant.checkPath(parse(arg), __current);
                if (!checked.exists()) {
                    final CheckedPath parent = checked.parent();
                    if (!(parent.isDirectory() || checked.parent().mkdirs())) {
                        throw new RepositoryException(null, null, "cannot create directory: " + parent);
                    }
                    final FileBuffer buffer = checked.getFileBuffer("rw");
                    buffer.write(ByteBuffer.allocate(0));
                    buffer.close();
                } else if (!checked.markModified()) {
                    throw new RepositoryException(null, null, "cannot touch file: " + checked);
                }
            }
        } else if ("mkdir".equals(command)) {
            boolean parents = false;
            for (String arg: args) {
                if ("-p".equals(arg)) {
                    parents = true;
                    continue;
                }
                CheckedPath checked = servant.checkPath(parse(arg), __current);
                if (parents) {
                    checked.mkdirs();
                } else {
                    checked.mkdir();
                }
            }
        } else if ("rm".equals(command)) {
            if (args.size() == 1) {
                CheckedPath checked = servant.checkPath(parse(args.get(0)), __current);
                if (!checked.delete()) {
                    throw new omero.grid.FileDeleteException(null, null,
                            "Delete file failed: " + args.get(0));
                }
            } else {
                throw new omero.ApiUsageException(null, null,
                        "Command: " + command + " takes just one argument");
            }
        } else {
            throw new omero.ApiUsageException(null, null,
                    "Unknown command: " + command);
        }
    }

    /**
     * Prepend a "./" if necessary to make the path relative.
     * TODO: this could likely be removed once the prefix is always stripped.
     *
     * @param arg
     * @return
     */
    private String parse(String arg) {
        if (arg.startsWith("/")) {
            arg = "./" + arg;
        }
        return arg;
    }
}