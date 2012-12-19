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
package ome.services.blitz.repo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.services.blitz.impl.AbstractAmdServant;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.cmd.HandlePrx;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportProcessPrxHelper;
import omero.grid.ImportSettings;
import omero.grid._ImportProcessOperations;
import omero.grid._ImportProcessTie;
import omero.model.Fileset;
import omero.model.FilesetActivity;
import omero.model.FilesetEntry;
import omero.model.OriginalFile;

/**
 * Represents a single import within a defined-session
 * all running server-side.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5
 */
public class ManagedImportProcessI extends AbstractAmdServant
    implements _ImportProcessOperations {

    private final static Log log = LogFactory.getLog(ManagedImportProcessI.class);

    static class UploadState {
        final RawFileStorePrx prx;
        /** Next byte which should be written */
        long offset = 0;

        UploadState(RawFileStorePrx prx) {
            if (prx == null) {
                throw new RuntimeException("Null not allowed!");
            }
            this.prx = prx;
        }

        void setOffset(long offset) {
            this.offset = offset;
        }
    }

    /**
     * The managed repo instance which created (and ultimately is reponsible
     * for) this import process.
     */
    private final ManagedRepositoryI repo;

    /**
     * A proxy to this servant which can be given to clients to monitor the
     * import process.
     */
    private final ImportProcessPrx proxy;

    /**
     * The model object as originally passed in by the client and then
     * modified and saved by the managed repository.
     */
    private final Fileset fs;

    /**
     * The settings as passed in by the user. Never null.
     */
    private final ImportSettings settings;

    /**
     * The import location as defined by the managed repository during
     * prepareImport. Never null.
     */
    private final ImportLocation location;

    /**
     * A sparse and often empty map of the {@link UploadState} instances which
     * this import process is aware of. In a single-threaded model, this map
     * will likely only have at most one element, but depending on threads,
     * pauses, and restarts, this may contain more elements. After close
     * is called on each of the proxies, {@link #closeCalled(int)} will be
     * invoked with the integer lookup to this map, in which case the instance
     * will be purged.
     */
    private ConcurrentHashMap<Integer, UploadState> uploaders
        = new ConcurrentHashMap<Integer, UploadState>();

    /**
     * Create and register a servant for servicing the import process
     * within a managed repository.
     *
     * @param repo
     * @param fs
     * @param location
     * @param settings
     * @param __current
     */
    public ManagedImportProcessI(ManagedRepositoryI repo, Fileset fs,
            ImportLocation location, ImportSettings settings, Current __current)
                throws ServerError {
        super(null, null);
        this.repo = repo;
        this.fs = fs;
        this.settings = settings;
        this.location = location;
        this.proxy = registerProxy(__current);
    }

    /**
     * Adds this instance to the current session so that clients can communicate
     * with it. Once we move to opening a new session for this import, care
     * must be taken to guarantee that these instances don't leak:
     * i.e. who's responsible for closing them and removing them from the
     * adapter.
     */
    protected ImportProcessPrx registerProxy(Ice.Current current) throws ServerError {
        _ImportProcessTie tie = new _ImportProcessTie(this);
        Ice.Current adjustedCurr = repo.makeAdjustedCurrent(current);
        Ice.ObjectPrx prx = repo.registerServant(tie, this, adjustedCurr);
        return ImportProcessPrxHelper.uncheckedCast(prx);
   }

    public ImportProcessPrx getProxy() {
        return this.proxy;
    }

    //
    // INTERFACE METHODS
    //

    public RawFileStorePrx getUploader(int i, Current __current)
            throws ServerError {

        UploadState state = uploaders.get(i);
        if (state != null) {
            return state.prx; // EARLY EXIT!
        }

        final String path = location.usedFiles.get(i);

        boolean success = false;
        RawFileStorePrx prx = repo.file(path, "rw", __current);
        try {
            state = new UploadState(prx); // Overwrite
            if (uploaders.putIfAbsent(i, state) != null) {
                // The new object wasn't used.
                // Close it.
                prx.close();
                prx = null;
            } else {
                success = true;
            }
            return prx;
        } finally {
            if (!success && prx != null) {
                try {
                    prx.close(); // Close if anything happens.
                } catch (Exception e) {
                    log.error("Failed to close RawFileStorePrx", e);
                }
            }
        }
    }

    public List<HandlePrx> verifyUpload(List<String> hashes, Current __current)
            throws ServerError {

        final int size = fs.sizeOfFilesetEntry();
        if (hashes == null) {
            throw new omero.ApiUsageException(null, null,
                    "hashes list cannot be null");
        } else if (hashes.size() != size) {
            throw new omero.ApiUsageException(null, null,
                    String.format("hashes size should be %s not %s", size,
                            hashes.size()));
        }

        for (int i = 0; i < size; i++) {
            FilesetEntry entry = fs.getFilesetEntry(i);
            String usedFile = location.usedFiles.get(i);
            CheckedPath cp = repo.checkPath(usedFile, __current);
            String client = hashes.get(i);
            String server = cp.sha1();
            if (!server.equals(client)) {
                throw new omero.ValidationException(null, null,
                        String.format("Hash mis-match for %s: " +
					"server:%s<>client:%s",
                                usedFile, server, client));
            }
        }

        return null;
    }

    public long getUploadOffset(int i, Current __current) throws ServerError {
        UploadState state = uploaders.get(i);
        if (state == null) {
            return 0;
        }
        return state.offset;
    }

    public String getSession(Current __current) throws ServerError {
        return null; // TODO
    }

    public ImportLocation getLocation(Current __current) throws ServerError {
        return location;
    }

    public ImportSettings getSettings(Current __current) throws ServerError {
        return settings;
    }

    public FilesetActivity getCurrentActivity(Current __current)
            throws ServerError {
        return null; // TODO
    }

    public boolean pauseImport(boolean _wait, Current __current)
            throws ServerError {
        throw new ServerError(null, null, "NYI");
    }

    public void cancelImport(Ice.Current __current)
               throws ServerError {
        throw new omero.InternalException(null, null, "NYI"); // FIXME
    }

    //
    // LOCAL INVOCATIONS
    //

    public void setOffset(int idx, long offset) {
        UploadState state = uploaders.get(idx);
        if (state == null) {
            log.warn(String.format("setOffset(%s, %s) - no such object", idx, offset));
        } else {
            state.setOffset(offset);
            log.debug(String.format("setOffset(%s, %s) successfully", idx, offset));
        }
    }

    public void closeCalled(int idx) {
        UploadState state = uploaders.remove(idx);
        if (state == null) {
            log.warn(String.format("closeCalled(%s) - no such object", idx));
        } else {
            log.debug(String.format("closeCalled(%s) successfully", idx));
        }
    }

}
