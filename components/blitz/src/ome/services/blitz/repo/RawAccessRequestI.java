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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

import ome.io.nio.FileBuffer;

import ome.services.blitz.fire.Registry;
import ome.services.blitz.util.ChecksumAlgorithmMapper;

import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.Unknown;
import omero.grid.InternalRepositoryPrx;
import omero.grid.RawAccessRequest;
import omero.grid.RepositoryException;
import omero.model.ChecksumAlgorithm;

/**
 * Command request for accessing a repository directly.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5.0
 */
public class RawAccessRequestI extends RawAccessRequest implements IRequest {

    private static final long serialVersionUID = -303948503984L;

    private static Logger log = LoggerFactory.getLogger(RawAccessRequestI.class);

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

    @Override
    public void finish() throws Cancel {
        // no-op
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
        catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "raw-access",
                    "command", command);
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
                final CheckedPath checked = servant.checkPath(parse(arg), null, __current);
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
        } else if ("exists".equals(command)) {
            final String arg = args.get(0);
            final CheckedPath checked = servant.checkPath(parse(arg), null, __current);
            if (!checked.exists()) {
                    throw new RepositoryException(null, null, "file does not exist: " + checked);
            }
        } else if ("mkdir".equals(command)) {
            boolean parents = false;
            for (String arg: args) {
                if ("-p".equals(arg)) {
                    parents = true;
                    continue;
                }
                final CheckedPath checked = servant.checkPath(parse(arg), null, __current);
                if (parents) {
                    checked.mkdirs();
                } else {
                    checked.mkdir();
                }
            }
        } else if ("rm".equals(command)) {
            if (args.size() == 1) {
                final CheckedPath checked = servant.checkPath(parse(args.get(0)), null, __current);
                if (!checked.delete()) {
                    throw new omero.grid.FileDeleteException(null, null,
                            "Delete file failed: " + args.get(0));
                }
            } else {
                throw new omero.ApiUsageException(null, null,
                        "Command: " + command + " takes just one argument");
            }
        } else if ("mv".equals(command)) {
            if (args.size() == 2) {
                final CheckedPath source = servant.checkPath(parse(args.get(0)), null, __current);
                final CheckedPath target = servant.checkPath(parse(args.get(1)), null, __current);
                boolean success = false;
                if (target.exists() && target.isDirectory()) {
                    try {
                        source.moveToDir(target, false);
                        success = true;
                    } catch (java.io.IOException ex) {
                        success = false;
                        log.warn("IOException on moveToDir: {}->{}",
                                source, target, ex);
                    }
                } else {
                    success = source.renameTo(target);
                }
                if (!success) {
                    throw new omero.ResourceError(null, null,
                        String.format("'mv %s %s' failed", source, target));
                }
            } else {
                throw new omero.ApiUsageException(null, null,
                        "Command: " + command + " takes two arguments");
            }
        } else if ("checksum".equals(command)) {
            if (args.size() == 3) {
                final String checksumType = args.get(0);
                final ChecksumAlgorithm algo = ChecksumAlgorithmMapper.getChecksumAlgorithm(checksumType);
                final String expectedHash = args.get(1);
                final CheckedPath checked = servant.checkPath(parse(args.get(2)), algo, __current);
                final String currentHash = checked.hash();
                if (!currentHash.equals(expectedHash)) {
                    // TODO: ADD ANNOTATION TO DATABASE HERE!
                    throw new omero.ResourceError(null, null, String.format(
                            "Checksum mismatch (%s): expected=%s found=%s",
                            checksumType, expectedHash, currentHash));
                }
            } else {
                throw new omero.ApiUsageException(null, null,
                        "'checksum' requires HASHER HASH FILEPATH, not: " + args.toString());
            }
        } else {
            throw new omero.ApiUsageException(null, null,
                    "Unknown command: " + command);
        }
    }

    /**
     * Prepend a "./" if necessary to make the path relative.
     */
    //TODO: this could likely be removed once the prefix is always stripped.
    private String parse(String arg) {
        if (arg.startsWith("/")) {
            arg = "./" + arg;
        }
        return arg;
    }
}
