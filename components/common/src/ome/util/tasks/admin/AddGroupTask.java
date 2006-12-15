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
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.AddGroupTask.Keys.*;

/**
 * {@link SimpleTask} which creates a {@link ExperimenterGroup} with the given
 * name, and optionally with the given description and leader ({@link Experimenter}).
 * 
 * Understands the parameters:
 * <ul>
 * <li>name</li>
 * <li>description</li>
 * <li>leader</li>
 * </ul>
 * 
 * Must be logged in as an administrator. See {@link Configuration} on how to do
 * this.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see SimpleTask
 * @since 3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class AddGroupTask extends SimpleTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link AddGroupTask}.
     */
    public enum Keys {
        name, description, leader
    }

    /** Delegates to super */
    public AddGroupTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the actual {@link ExperimenterGroup} creation.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IAdmin admin = getServiceFactory().getAdminService();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(enumValue(name));
        g.setDescription(enumValue(description));
        long gid = admin.createGroup(g);
        getLogger().info(
                String.format("Added group %s with id %d", g.getName(), gid));
        final String leaderName = enumValue(leader);
        if (leaderName != null && leaderName.trim().length() > 0) {
            Experimenter e = admin.lookupExperimenter(leaderName); // TODO need
                                                                    // id only
            admin.setGroupOwner(new ExperimenterGroup(gid, false), e);
        }
    }

}
