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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.PixelsService;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.services.messages.DeleteLogMessage;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.SetMultimap;

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

    /**
     * Inner class which can be used to generate a Deletion. The use of 
     * {@link OmeroContext} makes creating this object from the command-line
     * somewhat complicated, but with a Deletion.Builder inside of the Spring
     * configuration it should be possible to use:
     * <pre>
     * Deletion d = ctx.getBean("Deletion", Deletion.class);
     * </pre>
     * anywhere that a new deletion is needed.
     */
    public static class Builder extends AbstractFactoryBean<Deletion>
        implements ApplicationContextAware {

        protected OmeroContext ctx;

        protected ApplicationContext specs;

        protected AbstractFileSystemService afs;

        protected ExtendedMetadata em;

        public Builder(AbstractFileSystemService afs, ExtendedMetadata em) {
            this.afs = afs;
            this.em = em;
        }

        @Override
        public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
            this.ctx = (OmeroContext) ctx;
        }

        @Override
        protected Deletion createInstance()
            throws Exception {
            ClassPathXmlApplicationContext specs = new ClassPathXmlApplicationContext(
                new String[]{"classpath:ome/services/spec.xml"}, this.ctx);
            DeleteStepFactory dsf = new DeleteStepFactory(this.ctx, em);
            return new Deletion(specs, dsf, afs, this.ctx);
        }

        @Override
        public Class<? extends Deletion> getObjectType() {
            return Deletion.class;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(Deletion.class);

    private static final List<String> fileTypeList = Collections.unmodifiableList(
            Arrays.asList( "OriginalFile", "Pixels", "Thumbnail"));

    //
    // Ctor/injection state
    //

    private final OmeroContext ctx;

    private final DeleteStepFactory factory;

    private final AbstractFileSystemService afs;

    private final ApplicationContext specs;

    //
    // Command state (on start)
    //

    private Session session;

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

    private StopWatch sw;

    private long scheduledDeletes;

    private long start;

    private long stop;

    private long bytesFailed = 0;

    private long filesFailed = 0;

    private long actualDeletes = 0;

    private HashMap<String, long[]> undeletedFiles;

    public Deletion(ApplicationContext specs, DeleteStepFactory factory,
            AbstractFileSystemService afs, OmeroContext ctx) {

        this.specs = specs;
        this.factory = factory;
        this.afs = afs;
        this.ctx = ctx;

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
    public int start(EventContext ec, SqlAction sql, Session session,
            String type, long id,
            Map<String, String> options)  throws GraphException {

        this.session = session;
        this.sw = new Slf4JStopWatch();
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
            if (this.spec != null) {
                steps = spec.initialize(id, "", options);
            }
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
            StopWatch sw = new Slf4JStopWatch();

            state = new GraphState(ec, factory, sql, session, this.spec);
            scheduledDeletes = state.getTotalFoundCount();
            if (scheduledDeletes == 0L) {
                throw new GraphException("Object missing");
            }
            // TODO: missing stepStarts/stepStops report
            sw.stop("omero.delete.ids." + scheduledDeletes);
        }

        if (scheduledDeletes > Integer.MAX_VALUE) {
            throw new GraphException("Too many results! (" + scheduledDeletes
                + ") Delete a subgraph first.");
        }

        return (int) scheduledDeletes;

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
        final StopWatch sw = new Slf4JStopWatch();
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
     * Should be called within the transaction boundaries but as the last
     * processing step.
     */
    public void finish() throws GraphException {
        // TODO: should implement validate in DeleteStep
        Set<Long> filesets = new HashSet<Long>();
        for (int i = 0; i < state.getTotalFoundCount(); i++) {
            DeleteStep step = (DeleteStep) state.getStep(i);
            Long fsId = step.getFilesetId();
            if (fsId != null) {
                filesets.add(fsId);
            }
        }
        if (filesets.size() > 0) {
            QueryBuilder qb = new QueryBuilder();
            qb.select("fs.id").from("Fileset", "fs");
            qb.join("fs.images", "img", false, false);
            qb.where().and("fs.id in (:ids)").paramList("ids", filesets);
            List<?> rv = qb.query(session).list();
            if (rv != null && rv.size() > 0) {
                String msg = "Filesets found after deletion: " + rv;
                error.append(msg);
                throw new GraphException(msg);
            }
        }
    }

    /**
     * For each Report use the map of tables to deleted ids to remove the files
     * under Files, Pixels and Thumbnails if the ids no longer exist in the db.
     * Create a map of failed ids (not yet passed back to client).
      */
    public void deleteFiles() {
        StopWatch sw = new Slf4JStopWatch();
        try {
            _deleteFiles(new Function<String, Set<Long>>() {
                @Override
                public Set<Long> apply(String fileType) {
                    return state.getProcessedIds(fileType);
                }
            });
        } finally {
            sw.stop("omero.delete.binary");
        }
    }

    /**
     * Delete the actual files corresponding to the given class names and IDs.
     * Provides access to file deletion for objects not derived through {link @GraphSpec}-based operations.
     * @param processedIds the class names and IDs whose corresponding files are to be deleted
     */
    public void deleteFiles(final SetMultimap<String, Long> processedIds) {
        final StopWatch sw = new Slf4JStopWatch();
        try {
            _deleteFiles(new Function<String, Set<Long>>() {
                @Override
                public Set<Long> apply(String fileType) {
                    return processedIds.get(fileType);
                }
            });
        } finally {
            sw.stop("omero.delete.binary");
        }
    }

    private void _deleteFiles(Function<String, Set<Long>> processedIds) {

        File file;
        String filePath;

        HashMap<String, ArrayList<Long>> failedMap = new HashMap<String, ArrayList<Long>>();

        bytesFailed = 0;
        filesFailed = 0;
        for (String fileType : fileTypeList) {
            final Set<Long> deletedIds = processedIds.apply(fileType);
            failedMap.put(fileType, new ArrayList<Long>());
            if (deletedIds != null && deletedIds.size() > 0) {
                log.debug(String.format("Binary delete of %s for %s:%s: %s",
                        fileType, type, id,
                        deletedIds));
                for (Long id : deletedIds) {
                    file = null; // Clear
                    if (fileType.equals("OriginalFile")) {
                        // First we give the repositories a chance to delete
                        // FS-based files.
                        DeleteLogMessage dlm = new DeleteLogMessage(this, id);
                        try {
                            ctx.publishMessage(dlm);
                        }
                        catch (Throwable e) {
                            log.warn("Error on DeleteLogMessage", e);
                            filesFailed++;
                            failedMap.get(fileType).add(id);
                            // No way to calculate size!
                        }
                        // Regardless of what type of exception may have been
                        // thrown above, if no logs were found via the publish
                        // message, we have to assume that the files are local.
                        // This may just log that the file doesn't exist.
                        if (dlm.count() == 0) {
                            filePath = afs.getFilesPath(id);
                            file = new File(filePath);
                        }
                    } else if (fileType.equals("Thumbnail")) {
                        filePath = afs.getThumbnailPath(id);
                        file = new File(filePath);
                    } else { // Pixels
                        filePath = afs.getPixelsPath(id);
                        file = new File(filePath);
                        // Try to remove a _pyramid file if it exists
                        File pyrFile = new File(filePath + PixelsService.PYRAMID_SUFFIX);
                        deleteSingleFile(pyrFile, fileType, id, failedMap);

                        File dir = file.getParentFile();
                        // Now any lock file
                        File lockFile = new File(dir, "." + id + PixelsService.PYRAMID_SUFFIX
                                + BfPyramidPixelBuffer.PYR_LOCK_EXT);
                        deleteSingleFile(lockFile, fileType, id, failedMap);

                        // Now any tmp files
                        FileFilter tmpFileFilter = new WildcardFileFilter("."
                                + id + PixelsService.PYRAMID_SUFFIX + "*.tmp");
                        File[] tmpFiles = dir.listFiles(tmpFileFilter);
                        if(tmpFiles != null) {
                            for (int i = 0; i < tmpFiles.length; i++) {
                                deleteSingleFile(tmpFiles[i], fileType, id, failedMap);
                            }
                        }
                    }

                    // File will be null, for example if this is a repository
                    // file.
                    if (file != null) {
                        // Finally delete main file for any type.
                        deleteSingleFile(file, fileType, id, failedMap);
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
    private void deleteSingleFile(File file, String fileType, Long id,
            HashMap<String, ArrayList<Long>> failedMap)
    {
        if (file.exists()) {
            if (file.delete()) {
                log.debug("DELETED: " + file.getAbsolutePath());
            } else {
                log.debug("Failed to delete " + file.getAbsolutePath());
                failedMap.get(fileType).add(id);
                filesFailed++;
                bytesFailed += file.length();
            }
        } else {
            log.debug("File " + file.getAbsolutePath() + " does not exist.");
        }
    }


}
