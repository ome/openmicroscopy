/*
 * ome.util.tasks.admin.AddGroupTask
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
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.AddUserToGroupTask.Keys.*;

/**
 * {@link SimpleTask} which creates a {@link GroupExperimenterMap} between the
 * specified {@link ExperimenterGroup} and {@link Experimenter}.
 * 
 * Understands the parameters:
 * <ul>
 * <li>user</li>
 * <li>group</li>
 * </ul>
 * 
 * Must be logged in as an administrator. See {@link Configuration} on how to do
 * this.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SimpleTask
 * @since 3.0-Beta2
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public class AddUserToGroupTask extends SimpleTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link AddUserToGroupTask}.
     */
    public enum Keys {
        user, group
    }

    /** Delegates to super */
    public AddUserToGroupTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the actual {@link GroupExperimenterMap} creation.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IAdmin admin = getServiceFactory().getAdminService();
                
        ExperimenterGroup g = admin.lookupGroup(enumValue(group));
        Experimenter e = admin.lookupExperimenter(enumValue(user));
        
        g.unload();
        e.unload();
        
        admin.addGroups(e, g);

        getLogger().info(
                String.format("Added user %d to group %d", e.getId(), g.getId()));
        
    }
}
