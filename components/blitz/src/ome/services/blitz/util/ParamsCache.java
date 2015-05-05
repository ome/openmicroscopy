/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.services.blitz.util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import ome.model.core.OriginalFile;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.scripts.ScriptRepoHelper;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.spring.OnContextRefreshedEventListener;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.grid.JobParams;
import omero.grid.ParamsHelper;
import omero.grid.ParamsHelper.Acquirer;
import omero.util.IceMapper;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;

import Ice.UserException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Caching replacement of {@link omero.grid.ParamsHelper} which maintains an
 * {@link OriginalFile} ID-to-{@link JobParams} mapping in memory rather than
 * storing ParseJob instances in the database. All scripts are read once on
 * start and them subsequently based on the omero.scripts.cache.cron setting.
 * {@link JobParams} instances may be removed from the cache based on the
 * omero.scripts.cache.spec setting. If a key is not present in the cache on
 * {@link #getParams(long)} then an attempt will be made to load it. Any
 * exceptions thrown will be propagated to the callter.
 *
 * @since 5.1.2
 */
public class ParamsCache extends OnContextRefreshedEventListener implements
        ApplicationContextAware {

    private final static Logger log = LoggerFactory
            .getLogger(ParamsCache.class);

    private final LoadingCache<Long, JobParams> cache;

    private final Registry reg;

    private final Roles roles;

    private final ScriptRepoHelper scripts;

    private/* final */OmeroContext ctx;

    public ParamsCache(Registry reg, Roles roles, ScriptRepoHelper scripts,
            String spec) {
        this.reg = reg;
        this.roles = roles;
        this.scripts = scripts;
        this.cache = CacheBuilder.from(spec).build(
                new CacheLoader<Long, JobParams>() {
                    public JobParams load(Long key) throws Exception {
                        return lookup(key);
                    }
                });
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    /**
     * Called once on startup. This can typically take many minutes before
     * being called, and usually takes a few seconds per script to be loaded.
     */
    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        new Thread() {
            public void run() {
                Slf4JStopWatch startup = sw("startup");
                log.info("in handle");
                try {
                    lookupAll();
                } catch (Throwable t) {
                    throw new FatalBeanException("Failed to initially load", t);
                } finally {
                    startup.stop();
                }
            }
        }.run();
    }

    //
    // Public API
    //

    /**
     * Lookup a cached {@link JobParams} instance for the given key. If none
     * is present, then an attempt will be made to load one, possibly throwing
     * a {@link UserException}.
     *
     * @param key
     * @return
     * @throws UserException
     */
    public JobParams getParams(long key) throws UserException {
        Slf4JStopWatch get = sw("get." + Long.toString(key));
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            UserException ue = new IceMapper().handleException(cause, ctx);
            log.warn("Error on scripts cache lookup", ue);
            throw ue;
        } finally {
            get.stop();
        }
    }

    /**
     * Called by the {@link LoadingCache} when a cache-miss occurs.
     */
    public JobParams lookup(Long key) throws Exception {
        return _load(key);
    }

    /**
     * Called from a Quartz cron trigger for periodically reloading all scripts.
     */
    public void lookupAll() throws Exception {
        _load(null);
    }

    //
    // HELPERS
    //

    private Slf4JStopWatch sw(String suffix) {
        return new Slf4JStopWatch("omero.scripts.cache." + suffix);
    }

    /**
     * Internal loading method which uses a {@link Loader} to create
     * a session as root and perform the necessary script invocation.
     */
    private JobParams _load(Long key) throws Exception {
        Slf4JStopWatch load = sw(key == null ? "all" : Long.toString(key));
        Loader loader = null;
        try {
            Slf4JStopWatch login = sw("login");
            try {
                loader = new Loader();
            } finally {
                login.stop();
            }

            if (key != null) {
                return loader.createParams(key);
            } else {
                Slf4JStopWatch list = sw("list");
                List<OriginalFile> files = scripts.loadAll(true);
                list.stop();
                for (OriginalFile file : files) {
                    Slf4JStopWatch single = sw(file.getId().toString());
                    cache.put(file.getId(),
                            loader.createParams(file.getId()));
                    single.stop();
                }
                log.info("New size of scripts cache: {} ({} ms.)",
                        cache.size(), load.getElapsedTime());
                return null;
            }
        } finally {
            load.stop();
            if (loader != null) {
                loader.close();
            }
        }
    }

    /** Simple state class for holding the various instances needed for
     * logging in and creating a {@link JobParams} instance.
     */
    private class Loader {

        ParamsHelper helper = null;
        ServiceFactoryI sf = null;
        Ice.Current curr = null;

        Loader() throws Exception {
            ServiceFactoryPrx prx = reg.getInternalServiceFactory(roles
                .getRootName(), "unused", 3, 1, UUID.randomUUID()
                .toString());
            Ice.Identity id = prx.ice_getIdentity();
            FindServiceFactoryMessage msg = new FindServiceFactoryMessage(
                    this, id);
            try {
                ctx.publishMessage(msg);
            } catch (Throwable t) {
                throw new IceMapper().handleException(t, ctx);
            }
            sf = msg.getServiceFactory();
            curr = sf.newCurrent(id, "loadScripts");

            // From ScriptI.java
            Acquirer acq = (Acquirer) sf.getServant(sf
                    .sharedResources(null).ice_getIdentity());
            helper = new ParamsHelper(acq, sf.getExecutor(),
                    sf.getPrincipal());
        }

        JobParams createParams(Long key) throws ServerError {
            return helper.generateScriptParams(key, false, curr);
        }

        void close() {
            if (sf != null) {
                sf.destroy(curr);
            }
        }

    }

}