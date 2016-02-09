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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.model.OriginalFile;

/**
 * Local-only file transfer mechanism which makes use of soft-linking.
 * This is only useful where the command "ln -s source target" will work.
 *
 * @since 5.0
 * @deprecated replaced by AbstractExecFileTransfer2
 */
public abstract class AbstractExecFileTransfer extends AbstractFileTransfer {

    /**
     * "Transfer" files by soft-linking them into place. This method is likely
     * re-usable for other general "linking" strategies by overriding
     * {@link #createProcessBuilder(File, File)} and the other protected methods here.
     */
    public String transfer(TransferState state) throws IOException, ServerError {
        RawFileStorePrx rawFileStore = start(state);
        try {
            final OriginalFile root = state.getRootFile();
            final OriginalFile ofile = state.getOriginalFile();
            final File location = getLocalLocation(root, ofile);
            final File file = state.getFile();
            final long length = state.getLength();
            final ChecksumProvider cp = state.getChecksumProvider();
            state.uploadStarted();
            checkLocation(location, rawFileStore); // closes rawFileStore
            state.closeUploader();
            exec(file, location);
            checkTarget(location, state);
            cp.putFile(file.getAbsolutePath());
            state.stop(length);
            state.uploadBytes(length);
            return finish(state, length);
        } finally {
            state.closeUploader();
        }
    }

    /**
     * Executes a local command and fails on non-0 return codes.
     *
     * @param file the source file
     * @param location the target on the server
     * @throws IOException for problems with the source file
     */
    protected void exec(File file, File location) throws IOException {
        ProcessBuilder pb = createProcessBuilder(file, location);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        Integer rcode = null;
        while (rcode == null) {
            try {
                rcode = process.waitFor();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }
        if (rcode == null || rcode.intValue() != 0) {
            StringWriter sw = new StringWriter();
            sw.append("transfer process returned: ");
            sw.append(Integer.toString(rcode));
            sw.append("\n");
            sw.append("command:");
            for (String arg : pb.command()) {
                sw.append(" ");
                sw.append(arg);
            }
            sw.append("\n");
            sw.append("output:");
            sw.append(LINE);
            String line = "";
            BufferedReader br = new BufferedReader(
                   new InputStreamReader(process.getInputStream()));
            while ( (line = br.readLine()) != null) {
               sw.append(line);
               sw.append(SEPARATOR);
            }
            sw.append(LINE);
            String msg = sw.toString();
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Creates a {@link ProcessBuilder} instance ready to have
     * {@link ProcessBuilder#start()} called on it. The only critical
     * piece of information should be the return code.
     *
     * @param file File to be copied.
     * @param location Location to copy to.
     * @return an instance ready for performing the transfer
     */
    protected abstract ProcessBuilder createProcessBuilder(File file, File location);

}
