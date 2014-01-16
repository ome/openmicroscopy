/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
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

package ome.formats.importer.transfers;

import java.io.FileInputStream;
import java.io.IOException;

import ome.formats.importer.FileTransfer;
import omero.ServerError;
import omero.api.RawFileStorePrx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traditional file transfer mechanism which uploads
 * files using the API.
 *
 * @since 5.0
 */
public abstract class AbstractFileTransfer implements FileTransfer {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected RawFileStorePrx start(TransferState state) throws ServerError {
        log.info("Transferring {}...", state.getFile());
        state.start();
        return state.getUploader();
    }

    protected String finish(TransferState state, long offset) throws ServerError {
        state.start();
        state.save();
        state.stop();
        state.uploadComplete(offset);
        return state.getChecksum();
    }

    protected void cleanupUpload(RawFileStorePrx rawFileStore,
            FileInputStream stream) throws ServerError {
        try {
            if (rawFileStore != null) {
                try {
                    rawFileStore.close();
                } catch (Exception e) {
                    log.error("error in closing raw file store", e);
                }
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("I/O in error closing stream", e);
                }
            }
        }

    }
}
