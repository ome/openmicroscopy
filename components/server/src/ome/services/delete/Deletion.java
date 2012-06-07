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
package ome.services.delete;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.PixelsService;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.system.OmeroContext;
import ome.util.SqlAction;

/**
 * Maintain state about a delete itself. That makes a central class for
 * providing reusable delete logic. (Note: much of this code has been
 * refactored out of DeleteHandleI for reuse by DeleteI etc.)
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 * @see ome.api.IDelete
 * @see ome.services.blitz.impl.DeleteHandleI
 * @see omero.cmd.graphs.DeleteI
 */
public class Deletion {

    private static final Log log = LogFactory.getLog(Deletion.class);

    private static final List<String> fileTypeList = Collections.unmodifiableList(
            Arrays.asList( "OriginalFile", "Pixels", "Thumbnail"));

    //
    // Ctor/injection state
    //

    private final DeleteStepFactory factory;

    private final AbstractFileSystemService afs;

    private final ApplicationContext specs;

    //
    // Command state (on start)
    //

    private String type;

    private long id;

    private Map<String, String> options;

    private GraphState state;

    private GraphSpec spec;

    private int steps;

    //
    // Execution state (per steps)
    //

    private final StringBuilder error = new StringBuilder();

    private final StringBuilder warning = new StringBuilder();

    private CommonsLogStopWatch sw;

    private long scheduledDeletes;

    private long start;

    private long stop;

    private long bytesFailed = 0;

    private long filesFailed = 0;

    private long actualDeletes = 0;

    private HashMap<String, long[]> undeletedFiles;

    public Deletion(ApplicationContext specs, DeleteStepFactory factory,
            AbstractFileSystemService afs) {

        this.specs = specs;
        this.factory = factory;
        this.afs = afs;

    }

    //
    // Getters
    //

    public int getSteps() {
        return steps;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

    public String getError() {
        return error.toString();
    }

    public String getWarning() {
        return warning.toString();
    }

    public long getActualDeletes() {
        return actualDeletes;
    }

    public long getScheduledDeletes() {
        return scheduledDeletes;
    }

    public Map<String, long[]> getUndeletedFiles() {
        return undeletedFiles;
    }

    //
    // Lifecycle
    //

    /**
     * Returns the number of steps that are available.
     */
    public int start(SqlAction sql, Session session, String type, long id,
            Map<String, String> options)  throws GraphException {

        this.sw = new CommonsLogStopWatch();
        this.start = System.currentTimeMillis();
        this.type = type;
        this.id = id;
        this.options = options;

        // INITIALIZATION
        try {
            this.spec = specs.getBean(type, GraphSpec.class);
            if (this.spec == null) {
                throw new NullPointerException(); // handled in catch
            }
        } catch (Exception e) {
            error.append("Specification not found: ");
            error.append(type);
        }

        try {
            steps = spec.initialize(id, "", options);
        } catch (GraphException de) {
            error.append("Failed initialization: ");
            error.append(de.message);
        }

        if (error()) {
            log.info("Initialization cancelled " + type
                    + ":" + id + " -- " + error.toString());
        } else {
            log.info(String.format("Deleting %s:%s", type, id));

            // STATE PARSING
            // Initialize. Any exceptions should cancel the process
            StopWatch sw = new CommonsLogStopWatch();
            state = new GraphState(factory, sql, session, this.spec);
            scheduledDeletes = state.getTotalFoundCount();
            if (scheduledDeletes == 0L) {
                throw new GraphException("Object missing");
            }
            // TODO: missing stepStarts/stepStops report
            sw.stop("omero.delete.ids." + scheduledDeletes);
        }

        return steps;

    }

    public void stop() {
        // If we reach this far, then the delete was successful, so save
        // the deleted id count.
        this.actualDeletes = state.getTotalProcessedCount();
        this.sw.stop("omero.delete.command");
        this.stop = start + sw.getElapsedTime();
    }

    public boolean error() {
        return error.length() > 0;
    }

    public void execute(int step) throws Throwable {
        final CommonsLogStopWatch sw = new CommonsLogStopWatch();
        try {
            warning.append(state.execute(step));
            // TODO: missing stepStart/stepStart calculation here.
        // This hierarchy is duplicated in DeleteI
        } catch (GraphException de) {
            error.append(de.message);
            throw de;
        } catch (ConstraintViolationException cve) {
            error.append("ConstraintViolation: " + cve.getConstraintName());
            throw cve;
        } catch (Throwable t) {
            String msg = "Failure during DeleteHandle.steps :";
            error.append(msg + t);
            log.error(msg, t);
            throw t;
        } finally {
            sw.stop("omero.delete.step." + step);
        }

    }

    /**
     * For each Report use the map of tables to deleted ids to remove the files
     * under Files, Pixels and Thumbnails if the ids no longer exist in the db.
     * Create a map of failed ids (not yet passed back to client).
      */
    public void deleteFiles() {
        CommonsLogStopWatch sw = new CommonsLogStopWatch();
        try {
            _deleteFiles();
        } finally {
            sw.stop("omero.delete.binary");
        }
    }

    private void _deleteFiles() {

        File file;
        String filePath;

        HashMap<String, ArrayList<Long>> failedMap = new HashMap<String, ArrayList<Long>>();

        bytesFailed = 0;
        filesFailed = 0;
        for (String fileType : fileTypeList) {
            Set<Long> deletedIds = state.getProcessedIds(fileType);
            failedMap.put(fileType, new ArrayList<Long>());
            if (deletedIds != null && deletedIds.size() > 0) {
                log.debug(String.format("Binary delete of %s for %s:%s: %s",
                        fileType, type, id,
                        deletedIds));
                for (Long id : deletedIds) {
                    if (fileType.equals("OriginalFile")) {
                        filePath = afs.getFilesPath(id);
                        file = new File(filePath);
                    } else if (fileType.equals("Thumbnail")) {
                        filePath = afs.getThumbnailPath(id);
                        file = new File(filePath);
                    } else { // Pixels
                        filePath = afs.getPixelsPath(id);
                        file = new File(filePath);
                        // Try to remove a _pyramid file if it exists
                        File pyrFile = new File(filePath + PixelsService.PYRAMID_SUFFIX);
                        if(!deleteSingleFile(pyrFile)) {
                            failedMap.get(fileType).add(id);
                            filesFailed++;
                            bytesFailed += pyrFile.length();
                        }
                        File dir = file.getParentFile();
                        // Now any lock file
                        File lockFile = new File(dir, "." + id + PixelsService.PYRAMID_SUFFIX
                                + BfPyramidPixelBuffer.PYR_LOCK_EXT);
                        if(!deleteSingleFile(lockFile)) {
                            failedMap.get(fileType).add(id);
                            filesFailed++;
                            bytesFailed += lockFile.length();
                        }
                        // Now any tmp files
                        FileFilter tmpFileFilter = new WildcardFileFilter("."
                                + id + PixelsService.PYRAMID_SUFFIX + "*.tmp");
                        File[] tmpFiles = dir.listFiles(tmpFileFilter);
                        if(tmpFiles != null) {
                            for (int i = 0; i < tmpFiles.length; i++) {
                                if(!deleteSingleFile(tmpFiles[i])) {
                                    failedMap.get(fileType).add(id);
                                    filesFailed++;
                                    bytesFailed += tmpFiles[i].length();
                                }
                            }
                        }
                    }
                    // Finally delete main file for any type.
                    if(!deleteSingleFile(file)) {
                        failedMap.get(fileType).add(id);
                        filesFailed++;
                        bytesFailed += file.length();
                    }
                }
            }
        }

        undeletedFiles = new HashMap<String, long[]>();
        for (String key : failedMap.keySet()) {
            List<Long> ids = failedMap.get(key);
            long[] array = new long[ids.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = ids.get(i);
            }
            undeletedFiles.put(key, array);
        }
        if (filesFailed > 0) {
            String warning = "Warning: " + Long.toString(filesFailed) + " file(s) comprising "
                    + Long.toString(bytesFailed) + " bytes were not removed.";
            this.warning.append(warning);
            log.warn(warning);
        }
        if (log.isDebugEnabled()) {
            for (String table : failedMap.keySet()) {
                log.debug("Failed to delete files : " + table + ":"
                        + failedMap.get(table).toString());
            }
        }
    }


    /**
     * Helper to delete and log
     */
    private boolean deleteSingleFile(File file)
    {
        if (file.exists()) {
            if (file.delete()) {
                log.debug("DELETED: " + file.getAbsolutePath());
            } else {
                log.debug("Failed to delete " + file.getAbsolutePath());
                return false;
            }
        } else {
            log.debug("File " + file.getAbsolutePath() + " does not exist.");
        }
        return true;
    }

}
