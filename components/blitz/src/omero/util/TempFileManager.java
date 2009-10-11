/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * Creates temporary files and folders and makes a best effort to remove them on
 * exit (or sooner). Typically only a single instance of this class will exist
 * (static {@link #manager} constant)
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.1
 */
public class TempFileManager {

    private final static Log log = LogFactory.getLog(TempFileManager.class);

    /**
     * Global {@link TempFileManager} instance for use by the current process
     * and registered with the atexit module for cleaning up all created files
     * on exit. Other instances can be created for specialized purposes.
     */
    private final static TempFileManager manager = new TempFileManager();

    /**
     * User-accessible directory of the form $TMPDIR/omero_$USERNAME/. If the
     * given directory is not writable, an attempt is made to use a similar
     * name.
     */
    private final File userDir;

    /**
     * Directory under which all temporary files and folders will be created. An
     * attempt to remove a path not in this directory will result in an
     * exception.
     */
    private final File dir;

    /**
     * .lock file under {@link #dir} which is used to prevent other
     * {@link TempFileManager} instances (also in other languages) from cleaning
     * up this directory.
     */
    private final RandomAccessFile raf;

    /**
     * Lock held on {@link #raf}
     */
    private final FileLock lock;

    /**
     * Default constructor, passes "omero" to
     * {@link TempFileManager#TempFileManager(String)}
     */
    public TempFileManager() {
        this("omero");
    }

    /**
     * Initializes a {@link TempFileManager} instance with a {@link userDir}
     * containing the given prefix value. Also adds a
     * {@link Runtime#addShutdownHook(Thread) shutdown hook} to call
     * {@link #cleanup()} on exit.
     */
    public TempFileManager(String prefix) {
        File tmp = new File(tmpdir());
        File userDir = new File(tmp, String.format("%s_%s", prefix, username()));
        if (!this.create(userDir) && !this.access(userDir)) {
            int i = 0;
            while (i < 10) {
                File t = new File(userDir.getAbsolutePath() + "_" + i);
                if (this.create(t) || this.access(t)) {
                    userDir = t;
                    break;
                }
            }
            throw new RuntimeException("Failed to create temporary directory: "
                    + userDir.getAbsolutePath());
        }
        this.userDir = userDir;
        this.dir = new File(this.userDir, this.pid());
        
        // Now create the directory. If a later step throws an
        // exception, we should try to rollback this change.
        boolean created = false;
        if (!this.dir.exists()) {
            this.dir.mkdirs();
            created = true;
        }

        try {
            try {
                this.raf = new RandomAccessFile(new File(this.dir, ".lock"),
                        "rw");
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to open lock file", e);
            }

            try {
                lock = this.raf.getChannel().tryLock();
                if (lock == null) {
                    throw new RuntimeException("Failed to acquire lock");
                }
            } catch (Exception e) {
                try {
                    this.raf.close();
                } catch (Exception e2) {
                    log.warn("Exception on lock file close", e2);
                }
                throw new RuntimeException("Failed to lock file", e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        cleanup();
                    } catch (IOException io) {
                        log.error("Failed to cleanup TempFileManager", io);
                    }
                }
            });
        } catch (RuntimeException e) {
            if (created) {
                try {
                    cleanup();
                } catch (Exception e2) {
                    log.warn("Error on cleanup after error", e2);
                }
                
            }
            throw e;
        }
    }

    /**
     * Releases {@link #lock} and deletes {@link #dir}. The lock is released
     * first since on some platforms like Windows the lock file cannot be
     * deleted even by the owner of the lock.
     */
    protected void cleanup() throws IOException {
        try {
            if (this.lock != null) {
                this.lock.release();
            }
            if (this.raf != null) {
                this.raf.close();
            }
        } catch (Exception e) {
            log.error("Failed to release lock", e);
        }
        this.cleanTempDir();
    }

    /**
     * Returns a platform-specific user-writable temporary directory
     */
    protected String tmpdir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns the current OS-user's name.
     */
    protected String username() {
        return System.getProperty("user.name");
    }

    /**
     * Returns some representation of the current process's id
     */
    protected String pid() {
        String pid = java.lang.management.ManagementFactory.getRuntimeMXBean()
                .getName();
        return pid;
    }

    /**
     * Returns true if the current user can write to the give directory.
     */
    protected boolean access(File dir) {
        return dir.canWrite();
    }

    /**
     * If the given directory doesn't exist, creates it and returns true.
     * Otherwise false. Note: Java doesn't allow setting the mode for the
     * directory but it is intended to be 0700.
     */
    protected boolean create(File dir) {
        if (!dir.exists()) {
            // Can't set permissions
            dir.mkdirs();
            return true;
        }
        return false;
    }

    /**
     * Returns the directory under which all temporary files and folders will be
     * created.
     */
    public File getTempDir() {
        return dir;
    }

    /**
     * Uses {@link File#createTempFile(String, String, File)} to create
     * temporary files and folders under {@link #dir}. For folders, first a
     * temporary file is created, then deleted, and finally a directory
     * produced.
     */
    public File createPath(String prefix, String suffix, boolean folder)
            throws IOException {
        File file = File.createTempFile(prefix, suffix, this.dir);
        if (folder) {
            file.delete();
            file.mkdirs();
            log.debug("Added folder " + file.getAbsolutePath());
        } else {
            log.debug("Added file " + file.getAbsolutePath());
        }
        return file;
    }

    /**
     * If the given file is under {@link #dir}, then it is deleted whether file
     * or folder. Otherwise a {@link RuntimeException} is thrown.
     */
    public void removePath(File file) throws IOException {
        String f = file.getAbsolutePath();
        String d = dir.getAbsolutePath();
        if (!f.startsWith(d)) {
            throw new RuntimeException(d + " is not in " + f);
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                log.debug("Removed folder " + f);
            } else {
                file.delete();
                log.debug("Removed file " + f);
            }
        }

    }

    /**
     * Deletes {@link #dir}
     */
    protected void cleanTempDir() throws IOException {
        log.debug("Removing tree: " + dir.getAbsolutePath());
        FileUtils.deleteDirectory(dir); // Checks if dir exists
    }

    /**
     * Attempts to delete all directories under self.userdir other than the one
     * owned by this process. If a directory is locked, it is skipped.
     */
    @SuppressWarnings("unchecked")
    protected void cleanUserDir() throws IOException {
        log.debug("Cleaning user dir: " + userDir.getAbsolutePath());
        List<File> files = Arrays.asList(userDir.listFiles());
        final String d = dir.getCanonicalPath();
        for (File file : files) {
            String f = file.getCanonicalPath();
            if (f.equals(d)) {
                log.debug("Skipping self: " + d);
                continue;
            }
            File lock = new File(file, ".lock");
            RandomAccessFile raf = new RandomAccessFile(lock, "rw");
            try {
                FileLock fl = raf.getChannel().tryLock();
                if (fl == null) {
                    System.out.println("Locked: " + f);
                    continue;
                }
            } catch (Exception e) {
                System.out.println("Locked: " + f);
                continue;
            }
            FileUtils.deleteDirectory(file);
            System.out.println("Deleted: " + f);
        }
    }

    //
    // Static methods
    //

    /**
     * Emulates {@link File#createTempFile(String, String)} by calling
     * {@link #create_path(String, String)}.
     */
    public static File createTempFile(String prefix, String suffix)
            throws IOException {
        return create_path(prefix, suffix);
    }

    /**
     * Calls {@link #createPath(String, String, boolean)} on {@link #manager}
     * with defaults of "omero", ".tmp", and false.
     */
    public static File create_path() throws IOException {
        return manager.createPath("omero", ".tmp", false);
    }

    /**
     * Calls {@link #createPath(String, String, boolean)} on {@link #manager}
     * with defaults of ".tmp", and false.
     */
    public static File create_path(String prefix) throws IOException {
        return manager.createPath(prefix, ".tmp", false);
    }

    /**
     * Calls {@link #createPath(String, String, boolean)} on {@link #manager}
     * with ".tmp", and false arguments.
     */
    public static File create_path(String prefix, String suffix)
            throws IOException {
        return manager.createPath(prefix, suffix, false);
    }

    /**
     * Calls {@link #createPath(String, String, boolean)} on {@link #manager}.
     */
    public static File create_path(String prefix, String suffix, boolean folder)
            throws IOException {
        return manager.createPath(prefix, suffix, folder);
    }

    /**
     * Calls {@link #removePath(File)} on {@link #manager}.
     */
    public static void remove_path(File file) throws IOException {
        manager.removePath(file);
    }

    /**
     * Command-line interface to the global {@link TempFileManager} instance (
     * {@link #manger}). Valid arguments: "--debug", "clean", "dir", and for
     * testing, "lock"
     */
    public static void main(String[] _args) throws IOException {
        List<String> args = Arrays.asList(_args);

        if (args.size() > 0) {

            if (args.contains("--debug")) {
                ConsoleAppender console = new ConsoleAppender();
                console.setName("System.err");
                console.setTarget(ConsoleAppender.SYSTEM_ERR);
                console.setLayout(new SimpleLayout());
                console.activateOptions();
                Logger logger = Logger.getLogger("omero");
                logger.addAppender(console);
                logger.setLevel(Level.DEBUG);
                logger.addAppender(console);
            }

            if (args.contains("clean")) {
                manager.cleanUserDir();
                System.exit(0);
            } else if (args.contains("dir")) {
                System.out.println(manager.getTempDir().getAbsolutePath());
                System.exit(0);
            } else if (args.contains("lock")) {
                System.out.println("Locking "
                        + manager.getTempDir().getAbsolutePath());
                System.out.println("Waiting on user input...");
                System.in.read();
                System.exit(0);
            }
        }
        System.err.println("Usage: TempFileManager clean");
        System.err.println("   or: TempFileManager dir");
        System.exit(2);

    }
}
