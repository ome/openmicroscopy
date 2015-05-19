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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import ome.conditions.SecurityViolation;
import ome.model.core.OriginalFile;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.spring.OnContextRefreshedEventListener;
import omero.api.ServiceFactoryPrx;
import omero.constants.GROUP;
import omero.constants.namespaces.NSDYNAMIC;
import omero.grid.JobParams;
import omero.grid.ParamsHelper;
import omero.grid.ParamsHelper.Acquirer;
import omero.model.ExperimenterGroupI;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import Ice.UserException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Caching replacement of {@link omero.grid.ParamsHelper} which maintains an
 * {@link OriginalFile} ID+SHA1-to-{@link JobParams} mapping in memory rather
 * than storing ParseJob instances in the database. All scripts are read once on
 * start and them subsequently based on the omero.scripts.cache.cron setting.
 * {@link JobParams} instances may be removed from the cache based on the
 * omero.scripts.cache.spec setting. If a key is not present in the cache on
 * {@link #getParams(String)} then an attempt will be made to load it. Any
 * exceptions thrown will be propagated to the caller.
 *
 * @since 5.1.2
 */
public class ParamsCache extends OnContextRefreshedEventListener implements
        ApplicationContextAware {

    /**
     * Thrown by {@link ParamsCache#_load(Long)} when a found
     * {@link JobsParams} object has the {@link NSDYNAMIC} namespace.
     * In that case, the value <em>should not</em> be stored in the
     * cache but should be regenerated for each call.
     */
    @SuppressWarnings("serial")
    private static class DynamicException extends Exception {

        private final JobParams params;

        DynamicException(JobParams params) {
            this.params = params;
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(ParamsCache.class);

    private final LoadingCache<Key, JobParams> cache;

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
                new CacheLoader<Key, JobParams>() {
                    public JobParams load(Key key) throws Exception {
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
    public JobParams getParams(Long id, String sha1) throws UserException {
        Slf4JStopWatch get = sw("get." + id);
        try {
            return cache.get(new Key(id, sha1));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DynamicException) {
                return ((DynamicException) cause).params;
            }
            UserException ue = new IceMapper().handleException(cause, ctx);
            log.warn("Error on scripts cache lookup", ue);
            throw ue;
        } finally {
            get.stop();
        }
    }

    /**
     * Remove a cached {@link JobParams} instance.
     */
    public void removeParams(Long id) {
        if (id == null) {
            return;
        }
        Set<Key> matching = new HashSet<Key>(cache.asMap().keySet());
        Iterator<Key> it = matching.iterator();
        while (it.hasNext()) {
            if (id.equals(it.next().id)) {
                it.remove();
            }
        }
        cache.invalidateAll(matching);
    }

    /**
     * Called by the {@link LoadingCache} when a cache-miss occurs.
     */
    public JobParams lookup(Key key) throws Exception {
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
    private JobParams _load(Key key) throws Exception {
        Slf4JStopWatch load = sw(key == null ? "all" : Long.toString(key.id));
        Loader loader = null;
        try {
            Slf4JStopWatch login = sw("login");
            try {
                loader = new Loader(reg, ctx, roles.getRootName());
            } finally {
                login.stop();
            }

            if (key != null) {
                // May not return null!
                JobParams params = loader.createParams(key);
                if (isDynamic(params)) {
                    throw new DynamicException(params);
                }
                return params;
            } else {
                Slf4JStopWatch list = sw("list");
                List<OriginalFile> files = scripts.loadAll(true);
                list.stop();
                for (OriginalFile file : files) {
                    try {
                        Slf4JStopWatch single = sw(file.getId().toString());
                        Key newkey = new Key(file.getId(), file.getHash());
                        JobParams params = loader.createParams(newkey);
                        if (!isDynamic(params)) {
                            cache.put(newkey, params);
                        }
                        single.stop();
                    } catch (omero.ValidationException ve) {
                        // Likely an invalid script
                        log.warn("Failed to load params for {}",
                                file.getId(), ve);
                    } catch (Exception e) {
                        log.error("Failed to load params for {}",
                                file.getId(), e);
                    }
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

    private boolean isDynamic(JobParams params) throws DynamicException {
        if (params.namespaces != null) {
            if (params.namespaces.contains(NSDYNAMIC.value)) {
                return true;
            }
        }
        return false;
    }

    /** Simple state class for holding the various instances needed for
     * logging in and creating a {@link JobParams} instance.
     */
    private static class Loader {

        final Registry reg;
        final OmeroContext ctx;
        final ParamsHelper helper;
        final ServiceFactoryI sf;
        final Ice.Current curr;
        final Long gid;

        Loader(Registry reg, OmeroContext ctx, String name) throws Exception {
            this(reg, ctx, name, null);
        }

        Loader(Registry reg, OmeroContext ctx, String name, Long gid)
                throws Exception {
            this.reg = reg;
            this.ctx = ctx;
            this.gid = gid;
            ServiceFactoryPrx prx = reg.getInternalServiceFactory(name,
                "unused", 3, 1, UUID.randomUUID().toString());
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
            if (gid != null) {
                sf.setSecurityContext(new ExperimenterGroupI(gid, false), curr);
                curr.ctx.put(GROUP.value, ""+gid);
            }

            // From ScriptI.java
            Acquirer acq = (Acquirer) sf.getServant(sf
                    .sharedResources(null).ice_getIdentity());
            helper = new ParamsHelper(acq, sf.getExecutor(),
                    sf.getPrincipal());
        }

        /**
         * Call {@link ParamsHelper#generateScriptParams(long, boolean, Ice.Current)}
         * either as the current admin user or if this is a user script, creating
         * a temporary loader with just that context.
         */
        JobParams createParams(Key key) throws Exception {
            try {
                return helper.generateScriptParams(key.id, false, curr);
            } catch (SecurityViolation e) {
                if (gid != null) {
                    throw e;
                }
                // This must be a non-official script, i.e. doesn't belong
                // to an admin in the "user" group. We'll spend the extra
                // time logging into the user/group context.
                Object[] context = new Object[2];
                findContext(key, context);
                if (context[1] == null) {
                    // This is the recursion stopping condition. If the gid
                    // is null, then throw before recursing. Note: it's highly
                    // unlikely that an original file won't have a gid, so this
                    // is just in case.
                    throw new RuntimeException("No group found!");
                }
                Loader tmp = new Loader(reg, ctx, (String) context[0], (Long) context[1]);
                try {
                    return tmp.createParams(key);
                } finally {
                    tmp.close();
                }
            }
        }

        void findContext(final Key key, final Object[] context) {
            Map<String, String> allGroups = new HashMap<String, String>();
            allGroups.put(GROUP.value, "-1");
            sf.executor.execute(allGroups, sf.getPrincipal(),
                    new Executor.SimpleWork(this, "setContext", key) {
                        @Transactional(readOnly=true)
                        @Override
                        public Object doWork(Session session, ServiceFactory sf) {
                            OriginalFile ofile = (OriginalFile) session.get(OriginalFile.class, key.id);
                            if (ofile != null) {
                                String uid = ofile.getDetails().getOwner().getOmeName();
                                Long gid = ofile.getDetails().getGroup().getId();
                                context[0] = uid;
                                context[1] = gid;
                            }
                            return null;
                        }});
        }

        void close() {
            if (sf != null) {
                sf.destroy(curr);
            }
        }

    }

    private static class Key {

        final Long id;
        final String sha1;

        Key(Long id, String sha1) {
            this.id = id;
            this.sha1 = sha1;
        }

        @Override
        public int hashCode() {
            final int prime = 113;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((sha1 == null) ? 0 : sha1.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (sha1 == null) {
                if (other.sha1 != null)
                    return false;
            } else if (!sha1.equals(other.sha1))
                return false;
            return true;
        }
    }

}