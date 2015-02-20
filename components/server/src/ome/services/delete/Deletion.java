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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.io.nio.AbstractFileSystemService;
import ome.services.delete.files.FileDeleter;
import ome.services.delete.files.FileDeleterGraphState;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphSpec;
import ome.services.graphs.GraphState;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

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
 * @deprecated all except setup and {@link #deleteFiles(SetMultimap)} will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
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

    private long actualDeletes = 0;

    private FileDeleter files;

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
        return files == null ? null : files.getUndeletedFiles();
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

        this.files = new FileDeleterGraphState(ctx, afs, state, type, id);
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
            files.run();
            if (files.getFailedFilesCount() > 0) {
                String warning = files.getWarning();
                this.warning.append(warning);
                log.warn(warning);
            }
        } finally {
            sw.stop("omero.delete.binary");
        }
    }

    /**
     * For each Report use the map of tables to deleted ids to remove the files
     * under Files, Pixels and Thumbnails if the ids no longer exist in the db.
     * Create a map of failed ids (not yet passed back to client).
      */
    public void deleteFiles(SetMultimap<String, Long> deleteTargets) {
        final StopWatch sw = new Slf4JStopWatch();
        try {
            final FileDeleter files = new FileDeleter(ctx, afs, deleteTargets);
            files.run();
            if (files.getFailedFilesCount() > 0) {
                log.warn(files.getWarning());
            }
        } finally {
            sw.stop("omero.delete.binary");
        }
    }
}
