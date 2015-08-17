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

import java.util.Map;

import ome.io.nio.AbstractFileSystemService;
import ome.services.delete.files.FileDeleter;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.SetMultimap;

/**
 * Maintain state about a delete itself. That makes a central class for
 * providing reusable delete logic. (Note: much of this code has been
 * refactored out of DeleteHandleI for reuse by DeleteI etc.)
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 * @see ome.api.IDelete
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
            return new Deletion(afs, this.ctx);
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

    private final AbstractFileSystemService afs;

    //
    // Execution state (per steps)
    //

    private FileDeleter files;

    public Deletion(AbstractFileSystemService afs, OmeroContext ctx) {

        this.afs = afs;
        this.ctx = ctx;

    }

    //
    // Getters
    //

    public Map<String, long[]> getUndeletedFiles() {
        return files == null ? null : files.getUndeletedFiles();
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
                log.warn(files.getWarning());
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
