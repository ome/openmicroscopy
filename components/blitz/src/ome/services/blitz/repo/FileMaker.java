/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import ome.conditions.InternalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileMaker {

    private final static Log log = LogFactory.getLog(FileMaker.class);

    private final Object[] mutex = new Object[0];

    private final String repoDir;

    private/* final */String dbUuid;

    private File repoLock;

    private RandomAccessFile raf;

    private FileChannel channel;

    private FileLock lock;

    public FileMaker(String repoDir) {
        this.repoDir = repoDir;
    }

    public String getDir() {
        return this.repoDir;
    }

    public boolean needsInit() {
        synchronized (mutex) {
            return this.dbUuid == null;
        }
    }

    /**
     * @param dbUuid
     * @throws Exception
     */
    public void init(String dbUuid) throws Exception {
        synchronized (mutex) {

            if (this.dbUuid != null) {
                throw new InternalException("Already initialized");
            }

            this.dbUuid = dbUuid;
            File mountDir = new File(repoDir);
            File omeroDir = new File(mountDir, ".omero");
            File repoCfg = new File(omeroDir, "repository");
            File uuidDir = new File(repoCfg, dbUuid);
            if (!uuidDir.exists()) {
                uuidDir.mkdirs();
                log.info("Creating " + uuidDir);
            }

            repoLock = new File(uuidDir, "repo_uuid");
            raf = new RandomAccessFile(repoLock, "rw");
            channel = raf.getChannel();

        }
    }

    public String getLine() throws Exception {
        synchronized (mutex) {

            if (dbUuid == null) {
                throw new InternalException("Not initialized");
            }

            lock = channel.lock();
            String line = null;
            try {
                raf.seek(0);
                line = raf.readUTF();
            } catch (EOFException eof) {
                // pass
            }
            return line;
        }
    }

    public void writeLine(String line) throws Exception {

        synchronized (mutex) {

            if (dbUuid == null) {
                throw new InternalException("Not initialized");
            }

            raf.seek(0);
            raf.writeUTF(line);
        }
    }

    public void close() {

        synchronized (mutex) {

            if (dbUuid == null) {
                return;
            }

            try {
                lock.release();
            } catch (IOException e) {
                log.warn("Failed to release lock");
            }

            try {
                raf.close();
            } catch (IOException e) {
                log.warn("Failed to close RandomAccessFile");
            }

            dbUuid = null;
            repoLock = null;
            raf = null;
            channel = null;

        }
    }

}
