/*
 * ome.util.tasks.SimpleTask
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks.admin;

// Java imports
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.system.ServiceFactory;
import ome.util.tasks.SimpleTask;
import ome.util.tasks.TaskFailure;

/**
 * Simplest possible concrete subclass of {@link Task} which has null methods
 * for all of the required methods. Therefore, does nothing by default (though
 * it logs the nothing that it's doing).
 * 
 * {@link Task} writers can override any or all of the 4 methods.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1208 $, $Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan
 *          2007) $
 * @see Task
 * @since 3.0-Beta1
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public abstract class AdminTask extends SimpleTask {

    /**
     * Sole constructor. Delegates to {@link Task}
     * 
     * @see Task#Task(ServiceFactory, Properties)
     */
    public AdminTask(ServiceFactory serviceFactory, Properties properties) {
        super(serviceFactory, properties);
    }

    /**
     * Does nothing.
     */
    @Override
    public void init() {
        super.init();
    }

    /**
     * Rethrows the {@link RuntimeException}.
     */
    @Override
    public void handleException(RuntimeException re) {
        if (re.getClass().getName().contains("EJBAccessException")) {
            if (re.getMessage().contains("Authentication failure")) {
                throw new TaskFailure("Admin login to server failed.");
            } else if (re.getMessage().contains("Authorization failure")) {
                String user = getProperties().getProperty("omero.user");
                throw new TaskFailure(user
                        + " is not authorized for this task.");
            }
        } else if (SecurityViolation.class.isAssignableFrom(re.getClass())) {
            throw new TaskFailure("SecurityViolation:" + re.getMessage());
        } else if (ApiUsageException.class.isAssignableFrom(re.getClass())) {
            throw new TaskFailure("ApiUsage:" + re.getMessage());
        }

        super.handleException(re);
    }

}
