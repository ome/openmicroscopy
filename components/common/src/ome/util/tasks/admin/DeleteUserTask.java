/*
 * ome.util.tasks.admin.AddUserTask
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
import ome.api.IAdmin;
import ome.conditions.SecurityViolation;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;
import ome.util.tasks.TaskFailure;

import static ome.util.tasks.admin.DeleteUserTask.Keys.*;

/**
 * {@link AdminTask} which delets a {@link Experimenter} if possible.
 * 
 * Understands the parameters:
 * <ul>
 * <li>user</li>
 * </ul>
 * 
 * Must be logged in as an administrator. See {@link Configuration} on how to do
 * this.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see AdminTask
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public class DeleteUserTask extends AdminTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link DeleteUserTask}.
     */
    public enum Keys {
        user
    }

    /** Delegates to super */
    public DeleteUserTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the actual {@link Experimenter} deletion.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IAdmin admin = getServiceFactory().getAdminService();
        final String name = enumValue(user);
        Experimenter e = admin.lookupExperimenter(name);
        e.unload();

        // Clearing password table first.
        admin.deleteExperimenter(e);
        getLogger().info(
                String.format("Deleted user %s with id %d", name, e.getId()));
    }
    
    @Override
    public void handleException(RuntimeException re) {
        if (SecurityViolation.class.isAssignableFrom(re.getClass())) {
            String msg = re.getMessage();
            if (msg.contains("Deleting") && msg.contains("not allowed")) {
                throw new TaskFailure("SecurityViolation:" + msg
                        + "\n You may try running this task as \"root\"");
            }
        }
        super.handleException(re);    
    }
}
