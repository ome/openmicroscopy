/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.IRepositoryInfo;
import ome.api.ServiceInterface;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.io.nio.OriginalFilesService;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.tools.FileSystem;
import ome.tools.RepositoryTask;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class implementation of the IRepositoryInfo service interface.
 * <p>
 * Stateless ome.logic to determine disk space utilization at the server's data
 * image mount point, e.g. /OMERO See source code documentation for more.
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt
 * <p/>
 * 
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 * @see IRepositoryInfo
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Transactional
public class RepositoryInfoImpl extends AbstractLevel2Service implements
        IRepositoryInfo {

    /**
     * Time (2 minutes) between successive calls to
     * {@link #sanityCheckRepository()} needed to trigger an actual call to
     * {@link #getUsageFraction()}
     */
    public final static long INITIAL_DELAY = 2 * 60 * 1000L;

    /**
     * Percentage (100.0 - 0.0) of disk use which will cause an exception during
     * {@link #sanityCheckRepository()}
     */
    public final static double CRITICAL_USAGE = 95.0;

    /* The logger for this class. */
    private transient static Logger log = LoggerFactory
            .getLogger(RepositoryInfoImpl.class);

    /* repository filesystem */
    private transient String datadir;

    /* The ROMIO thumbnail service. */
    private transient ThumbnailService thumbnailService;

    /* The ROMIO pixels service. */
    private transient PixelsService pixelsService;

    /* The ROMIO file service. */
    private transient OriginalFilesService fileService;

    /* JDBC operations for removedUnusedFiles */
    private transient SqlAction sql;

    // Static state
    // =========================================================================

    /**
     * Time of the last true {@link #sanityCheckRepository()}
     */
    private volatile long lastCheck = 0;

    /**
     * Cached value from the last true {@link #sanityCheckRepository()}
     */
    private volatile double lastUsage = 0.0;

    /**
     * Lock-object around the call to the true {@link #sanityCheckRepository()}.
     * Should be used to protected all sections of code which change
     * {@link #lastCheck} and {@link #lastUsage}
     */
    private final Object lastLock = new Object();

    private static final String DEPRECATED =
        "This UNSAFE method has been deprecated. Server side functionality " +
        "has been REMOVED.";

    /**
     * Bean injection setter for ROMIO thumbnail service
     * @param thumbnailService the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService) {
        getBeanHelper().throwIfAlreadySet(this.thumbnailService,
                thumbnailService);
        this.thumbnailService = thumbnailService;
    }

    /**
     * Bean injection setter for ROMIO pixels service
     * @param pixelsService the pixels service
     */
    public void setPixelsService(PixelsService pixelsService) {
        getBeanHelper().throwIfAlreadySet(this.pixelsService, pixelsService);
        this.pixelsService = pixelsService;
    }

    /**
     * Bean injection setter for ROMIO file service
     * @param fileService the raw file service
     */
    public void setFileService(OriginalFilesService fileService) {
        getBeanHelper().throwIfAlreadySet(this.fileService, fileService);
        this.fileService = fileService;
    }

    /**
     * Bean injection setter for SQL operations
     * @param sql the SQL action instance
     */
    public void setSqlAction(SqlAction sql) {
        getBeanHelper().throwIfAlreadySet(this.sql, sql);
        this.sql = sql;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IRepositoryInfo#getFreeSpaceInKilobytes()
     */
    @RolesAllowed("user")
    public long getFreeSpaceInKilobytes() {
        FileSystem f;
        long result = 0L;

        try {
            f = new FileSystem(datadir);
            result = f.free(datadir);
            if (log.isInfoEnabled()) {
                log.info("Total kilobytes free: " + f.free(datadir));
            }
        } catch (Throwable t) {
            log.error("Error retrieving usage in KB.", t);
            throw new ResourceError(t.getMessage());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IRepositoryInfo#getUsedSpaceInKilobytes()
     */
    @RolesAllowed("user")
    @Deprecated
    public long getUsedSpaceInKilobytes() {
        throw new InternalException(DEPRECATED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IRepositoryInfo#getUsageFraction()
     */
    @RolesAllowed("user")
    @Deprecated
    public double getUsageFraction() {
        throw new InternalException(DEPRECATED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#getServiceInterface()
     */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IRepositoryInfo.class;
    }

    /**
     * Bean injection setter for data repository directory
     * @param datadir the data repository directory
     */
    public void setDatadir(String datadir) {
        this.datadir = datadir;
    }

    /**
     * Calculates based on the cached usage and the
     * elapsed time whether or not a real
     * {@link #sanityCheckRepository()} should be calculated.
     * @return if the repository needs a sanity check
     */
    public boolean needsSanityCheck() {

        // Disabled in 4.0. See ticket:1230
        // --------------------------------
        // The getUsage() information is not being properly calculated
        // and doing so in Java 1.5 requires undue hoop jumping.
        if (true) {
            return false;
        }

        long time = System.currentTimeMillis();
        long elapsed = time - lastCheck;

        // If the usage is within 5% of the critical usage
        // return true regardless
        if (lastUsage > CRITICAL_USAGE - 5.0) {
            return true;
        }

        // Otherwise, scale the initial delay down based on how high our
        // usage is, and if we've passed that value, then return true.
        // For example, if the initial delay 2 minutes and our previous usage is
        // 80 percent, then 100 - 80 / 100 = .2 * 2 minute = 24 seconds.
        double scale = (100 - lastUsage) / 100;
        if (elapsed > scale * INITIAL_DELAY) {
            return true;
        }

        return false;

    }

    /**
     * @see ome.api.IRepositoryInfo#sanityCheckRepository()
     */
    @RolesAllowed("user")
    public void sanityCheckRepository() throws InternalException {
        if (needsSanityCheck()) {
            synchronized (lastLock) {
                // Check the time again, in case another thread
                // updated the values.
                if (needsSanityCheck()) {
                    try {
                        lastUsage = getUsageFraction() * 100;
                    } catch (Throwable t) {
                        log.error(
                                "Critical failure sanity checking repository.",
                                t);
                        throw new InternalException(
                                "Error in sanityCheckRepository(): "
                                        + t.getMessage());
                    }
                    lastCheck = System.currentTimeMillis();
                }
            }
        }

        if (lastUsage > CRITICAL_USAGE) {
            throw new ResourceError(String.format(
                    "Server repository disk space usage (%s%%) exceeds %s%%",
                    lastUsage, CRITICAL_USAGE));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.IRepository#removeUnusedFiles()
     */
    @RolesAllowed("user")
    public void removeUnusedFiles() {

        RepositoryTask task = new RepositoryTask(sql);

        // get ids for any objects marked as deleted
        List<Long> files = task.getFileIds();
        List<Long> pixels = task.getPixelIds();
        List<Long> thumbs = task.getThumbnailIds();

        // cleanup any files
        if (files != null && files.size() > 0) {
            log.info("Removing files: " + files);
            fileService.removeFiles(files);
        }

        // cleanup any pixels
        if (pixels != null && pixels.size() > 0) {
            log.info("Removing pixels: " + pixels);
            pixelsService.removePixels(pixels);
        }

        // cleanup any thumbnails
        if (thumbs != null && thumbs.size() > 0) {
            log.info("Removing thumbnails: " + thumbs);
            thumbnailService.removeThumbnails(thumbs);
        }
    }

}
