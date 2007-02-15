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
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.ChangePasswordTask.Keys.*;

/**
 * {@link AdminTask} which changes the password for a given user.
 * 
 * Understands the parameters:
 * <ul>
 * <li>omename</li>
 * <li>password</li>
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
public class ChangePasswordTask extends AdminTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link ChangePasswordTask}.
     */
    public enum Keys {
        omename, password
    }

    /** Delegates to super */
    public ChangePasswordTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the password change.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IAdmin admin = getServiceFactory().getAdminService();
        final String name = enumValue(omename);
        final String pass = enumValue(password);
        admin.changeUserPassword(name, pass);
        getLogger().info(
                String.format("%s password for user %s",
                        (pass == null ? "Removed" : "Changed"), name));
    }

}
