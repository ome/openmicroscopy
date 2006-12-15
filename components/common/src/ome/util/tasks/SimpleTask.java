/*
 * ome.util.tasks.SimpleTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

/**
 * Simplest possible concrete subclass of {@link Task} which has null methods
 * for all of the required methods. Therefore, does nothing by default (though
 * it logs the nothing that it's doing).
 * 
 * {@link Task} writers can override any or all of the 4 methods.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see Task
 * @since 3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class SimpleTask extends Task {

    /**
     * Sole constructor. Delegates to {@link Task}
     * 
     * @see Task#Task(ServiceFactory, Properties)
     */
    public SimpleTask(ServiceFactory serviceFactory, Properties properties) {
        super(serviceFactory, properties);
    }

    /**
     * Does nothing.
     */
    @Override
    public void init() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Initializing task:" + this);
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void doTask() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Running task:" + this);
        }
    }

    /**
     * Rethrows the {@link RuntimeException}.
     */
    @Override
    public void handleException(RuntimeException re) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Handling exception in:" + this, re);
        }
        throw re;
    }

    /**
     * Does nothing.
     */
    @Override
    public void close() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Closing task:" + this);
        }
    }

}
