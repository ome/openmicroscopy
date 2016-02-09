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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.model.OriginalFile;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base {@link FileTransfer} implementation primarily providing the
 * {@link #start(TransferState)} and {@link #finish(TransferState, long)}
 * methods. Also used as the factory for {@link FileTransfer} implementations
 * via {@link #createTransfer(String)}.
 *
 * @since 5.0
 */
public abstract class AbstractFileTransfer implements FileTransfer {

    static final String LINE = "\n---------------------------------------------------\n";

    static final String SEPARATOR = System.getProperty("line.separator");
    
    /**
     * Enum of well-known {@link FileTransfer} names.
     * Note: these values are also in use in the fs.py
     * CLI plugin.
     */
    public enum Transfers {
        ln(HardlinkFileTransfer2.class),
        ln_rm(MoveFileTransfer.class),
        ln_s(SymlinkFileTransfer2.class),
        cp(CopyFileTransfer2.class),
        cp_rm(CopyMoveFileTransfer.class),
        upload(UploadFileTransfer.class),
        upload_rm(UploadRmFileTransfer.class);
        Class<?> kls;
        Transfers(Class<?> kls) {
            this.kls = kls;
        }
    }

    /**
     * Factory method for instantiating {@link FileTransfer} objects from
     * a string. Supported values can be found in the {@link Transfers} enum.
     * Otherwise, a FQN for a class on the classpath should be passed in.
     * @param arg a type of {@link FileTransfer} instance as named among {@link Transfers}
     * @return the new {@link FileTransfer} instance of the requested type
     */
    public static FileTransfer createTransfer(String arg) {
        Logger tmp = LoggerFactory.getLogger(AbstractFileTransfer.class);
        tmp.debug("Loading file transfer class {}", arg);
        try {
            try {
                return (FileTransfer) Transfers.valueOf(arg).kls.newInstance();
            } catch (Exception e) {
                // Assume not in the enum
            }
            Class<?> c = Class.forName(arg);
            return (FileTransfer) c.newInstance();
        } catch (Exception e) {
            tmp.error("Failed to load file transfer class " + arg);
            throw new RuntimeException(e);
        }
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Minimal start method which logs the file, calls
     * {@link TransferState#start()}, and loads the {@link RawFileStorePrx}
     * which any implementation will need.
     *
     * @param state the transfer state
     * @return a raw file store proxy for the upload
     * @throws ServerError if the uploader could not be obtained
     */
    protected RawFileStorePrx start(TransferState state) throws ServerError {
        log.info("Transferring {}...", state.getFile());
        state.start();
        return state.getUploader();
    }

    /**
     * Save the current state to disk and finish all timing and logging.
     *
     * @param state non-null
     * @param offset total length transferred.
     * @return client-side digest string.
     * @throws ServerError if the upload could not be completed and checksummed
     */
    protected String finish(TransferState state, long offset) throws ServerError {
        state.start();
        state.save();
        state.stop();
        state.uploadComplete(offset);
        return state.getChecksum();
    }

    /**
     * Utility method for closing resources.
     *
     * @param rawFileStore possibly null
     * @param stream possibly null
     * @throws ServerError presently not at all as errors are simply logged, but possibly in the future
     */
    protected void cleanupUpload(RawFileStorePrx rawFileStore,
            FileInputStream stream) throws ServerError {
        try {
            if (rawFileStore != null) {
                try {
                    rawFileStore.close();
                } catch (Exception e) {
                    log.debug("error in closing raw file store", e);
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

    /**
     * Uses os.name to determine whether or not this JVM is running
     * under Windows. This is mostly used for determining which executables
     * to run.
     */
    protected boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    protected void printLine() {
        log.error("*******************************************");
    }

    /**
     * Method used by subclasses during {@link FileTransfer#afterTransfer(int, List)}
     * if they would like to remove all the files transferred in the set.
     */
    protected void deleteTransferredFiles(int errors, List<String> srcFiles)
        throws CleanupFailure {

        if (errors > 0) {
            printLine();
            log.error("{} error(s) found.", errors);
            log.error("{} cleanup not performed!", getClass().getSimpleName());
            log.error("The following files will *not* be deleted:");
            for (String srcFile : srcFiles) {
                log.error("\t{}", srcFile);
            }
            printLine();
            return;
        }

        List<File> failedFiles = new ArrayList<File>();
        for (String path : srcFiles) {
            File srcFile = new File(path);
            try {
                log.info("Deleting source file {}...", srcFile);
                if (!srcFile.delete()) {
                    throw new RuntimeException("Failed to delete.");
                }
            } catch (Exception e) {
                log.error("Failed to remove source file {}", srcFile);
                failedFiles.add(srcFile);
            }
        }

        if (!failedFiles.isEmpty()) {
            printLine();
            log.error("Cleanup failed!");
            log.error("{} files could not be removed and will need to " +
                "be handled manually", failedFiles.size());
            for (File failedFile : failedFiles) {
                log.error("\t{}", failedFile.getAbsolutePath());
            }
            printLine();
            throw new CleanupFailure(failedFiles);
        }
    }

    /**
     * Build a path of the form "root.path/root.name/file.path/file.name".
     *
     * @param root the root directory
     * @param ofile a path relative to the root
     * @return the assembled path with separators suitable for the local filesystem
     */
    protected File getLocalLocation(OriginalFile root, OriginalFile ofile) {
        StringBuilder sb = new StringBuilder();
        sb.append(root.getPath().getValue());
        sb.append(File.separatorChar);
        sb.append(root.getName().getValue());
        sb.append(File.separatorChar);
        sb.append(ofile.getPath().getValue());
        sb.append(File.separatorChar);
        sb.append(ofile.getName().getValue());
        return new File(sb.toString());
    }

    /**
     * Check that the target location: 1) doesn't exist and 2) is properly
     * written to by the server. If either condition fails, no linking takes
     * place.
     *
     * @param location the source file
     * @param rawFileStore the target on the server
     * @throws ServerError if the raw file store could not be used
     * @throws IOException for problems with the source file
     */
    protected void checkLocation(File location, RawFileStorePrx rawFileStore)
            throws ServerError, IOException {

        final String uuid = UUID.randomUUID().toString();

        // Safety measures
        if (location.exists()) {
            throw new RuntimeException(location + " exists!");
        }

        // First we guarantee that we have the right file
        // If so, we remove it
        try {
            rawFileStore.write(uuid.getBytes(), 0, uuid.getBytes().length);
        } finally {
            rawFileStore.close();
        }
        try {
            if (!location.exists()) {
                throw failLocationCheck(location, "does not exist");
            } else if (!location.canRead()) {
                throw failLocationCheck(location, "cannot be read");
            } else if (!uuid.equals(FileUtils.readFileToString(location))) {
                throw failLocationCheck(location, "does not match check text");
            }
        } finally {
            if (!location.canWrite()) {
                throw failLocationCheck(location, "cannot be modified locally");
            } else {
                boolean deleted = FileUtils.deleteQuietly(location);
                if (!deleted) {
                    throw failLocationCheck(location, "could not be cleaned up");
                }
            }
        }
    }

    protected RuntimeException failLocationCheck(File location, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE);
        sb.append(String.format("Check failed: %s %s!\n", location, msg));
        sb.append("You likely do not have access to the ManagedRepository ");
        sb.append("for in-place import.\n");
        sb.append("Aborting...");
        sb.append(LINE);
        throw new RuntimeException(sb.toString());
    }
    /**
     * Check that the server can properly read the copied file.
     *
     * Like {@link #checkLocation(File, RawFileStorePrx)} but <em>after</em>
     * the invocation of {@link #exec(File, File)}, there is some chance, likely
     * due to file permissions, that the server will not be able to read the
     * transfered file. If so, raise an exception and leave the user to cleanup
     * and modifications.
     */
    protected void checkTarget(File location, TransferState state) throws ServerError {
        try {
            state.getUploader("r").size();
        } catch (Throwable t) {
            String message;
            if (t instanceof ServerError) {
                message = ((ServerError) t).message;
            } else {
                message = t.getMessage();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(t.getClass().getName());
            sb.append(" : ");
            sb.append(message);
            sb.append("\nThe server could not check the size of the file:\n");
            sb.append("-----------------------------------------------\n");
            sb.append(location);
            sb.append("\n-----------------------------------------------\n");
            sb.append("Most likely the server process has no read access\n");
            sb.append("and therefore in-place import cannot proceed. You\n");
            sb.append("should delete this file manually if you are sure\n");
            sb.append("that the original is safe.\n");
            throw new RuntimeException(sb.toString());
        }
    }

}
