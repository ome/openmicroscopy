/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.model.jobs.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ProcessorSkeleton implements Processor {

    private static Logger log = LoggerFactory.getLogger(ProcessorSkeleton.class);

    private IQuery query;
    private ITypes types;
    private IUpdate update;

    public void setQueryService(IQuery queryService) {
        this.query = queryService;
    }

    public void setTypesService(ITypes typesService) {
        this.types = typesService;
    }

    public void setUpdateService(IUpdate updateService) {
        this.update = updateService;
    }

    // Main methods ~
    // =========================================================================

    public Process process(long id) {
        Job job = lookup(id);
        if (accept(job)) {
            return process(job);
        } else {
            return null;
        }
    }

    public Job lookup(long id) {
        return query.find(Job.class, id);
    }

    public boolean accept(Job job) {
        return false;
    }

    public Process process(Job job) {
        return null;
    }

}
