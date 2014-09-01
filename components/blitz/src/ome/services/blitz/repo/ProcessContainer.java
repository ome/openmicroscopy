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
package ome.services.blitz.repo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import omero.grid.ImportProcessPrx;
import omero.model.Fileset;

/**
 * Helper class for holding the individual {@link ManagedImportProcessI}
 * instances created by {@link ManagedRepositoryI}. Responsible for providing
 * lookups as well as keep-alive and reap methods which will be called by
 * background threads.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5
 */
public class ProcessContainer {

    private final static Logger log = LoggerFactory.getLogger(ProcessContainer.class);

    public interface Process {
        ImportProcessPrx getProxy();
        Fileset getFileset();
        long getGroup();
        void ping();
        void shutdown();
    }

    private final static class Data {

        private final Map<Process, Object> processes =
                new ConcurrentHashMap<Process, Object>();

        void add(Process process) {
            processes.put(process, process);
        }

        void remove(Process process) {
            processes.remove(process);
        }

        void fill(List<Process> toFill) {
            for (Process process : processes.keySet()) {
                toFill.add(process);
            }
        }

    }

    private final ConcurrentHashMap<Long, Data> perGroup
        = new ConcurrentHashMap<Long, Data>();

    private Data getOrCreate(Long group) {
        Data data = new Data();
        Data old = perGroup.putIfAbsent(group, data);
        if (old != null) {
            data = old;
        }
        return data;
    }

    /**
     * Called once the {@link ManagedImportProcessI} service is created.
     * @param prx
     */
    public void addProcess(Process process) {
        Data data = getOrCreate(process.getGroup());
        data.add(process);
    }

    public void removeProcess(Process process) {
        Data data = perGroup.get(process.getGroup());
        if (data != null) {
            data.remove(process);
        }
    }

    public List<Process> listProcesses(Collection<Long> groups) {
        if (groups == null) {
            groups = perGroup.keySet();
        }

        final List<Process> rv
            = new ArrayList<Process>();

        for (Long group : groups) {
            Data d = perGroup.get(group);
            if (d != null) {
                d.fill(rv);
            }
        }
        return rv;
    }

    /**
     * Called periodically by quartz to keep the sessions alive.
     */
    public int pingAll() {
        // Use listImports since it's the fastest copy method we have.
        List<Process> processes = listProcesses(null);
        int errors = 0;
        for (Process process: processes) {
            try {
                process.ping();
            } catch (Throwable t) {
                errors++;
                log.info(String.format("Removing process [%s] due to error: %s",
                        process, t));
                log.debug(t.toString()); // slf4j migration: toString()
            }
        }
        return errors;
    }

    /**
     * Called on shutdown of the server.
     */
    public int shutdownAll() {
        // Use listImports since it's the fastest copy method we have.
        final List<Process> processes = listProcesses(null);
        int errors = 0;
        for (Process process: processes) {
            try {
                process.shutdown();
            } catch (Throwable t) {
                errors++;
                log.error("Ignoring error on process shutdown", t);
            }
        }
        return errors;
    }

}
