/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.tasks;

import java.util.Properties;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;
import ome.util.tasks.SimpleTask;

/**
 * Extends {@link SimpleTask} in order to allow calling tasks on blitz servers
 * as well as application servers.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SimpleTask
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public abstract class BlitzTask extends SimpleTask {

    final private omero.client client;

    /**
     * Defines whether or not this task was initialized for blitz or for an
     * application server.
     */
    final public boolean useBlitz;

    /**
     * Delegates to super constructor.
     */
    public BlitzTask(ServiceFactory serviceFactory, Properties properties) {
        super(serviceFactory, properties);
        client = null;
        useBlitz = false;
    }

    /**
     * Requires a non-null {@link omero.client}. If a null {@link Properties}
     * instance is provided, the
     * {@link System#getProperties() System properties} will be used.
     * 
     * @param serviceFactory
     *            Cannot be null.
     * @param properties
     *            Context variables for the task. Optional (can be null).
     */
    public BlitzTask(omero.client client, Properties properties) {
        super(new ServiceFactory(), properties);
        if (client == null) {
            throw new IllegalArgumentException("ServiceFactory cannot be null.");
        }
        this.client = client;
        this.useBlitz = true;
    }

    public boolean isBlitz() {
        return useBlitz;
    }

    public omero.client getBlitzServiceFactory() {
        return this.client;
    }

}
