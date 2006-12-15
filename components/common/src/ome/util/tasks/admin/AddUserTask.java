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
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.AddGroupTask.Keys.leader;
import static ome.util.tasks.admin.AddUserTask.Keys.*;

/**
 * {@link SimpleTask} which creates a {@link Experimenter} with the given login
 * name, first name, and last name, and optionally with the given email, middle
 * name, institution, and email.
 * 
 * Understands the parameters:
 * <ul>
 * <li>omename</li>
 * <li>firstname</li>
 * <li>lastname</li>
 * <li>middlename</li>
 * <li>institution</li>
 * <li>email</li>
 * <li>group</li>
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
public class AddUserTask extends SimpleTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link AddUserTask}.
     */
    public enum Keys {
        omename, firstname, lastname, middlename, institution, email, group
    }

    /** Delegates to super */
    public AddUserTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    // TODO if we want to use this directly in AdminImpl we'll need to override
    // the slow property lookups.

    /**
     * Performs the actual {@link Experimenter} creation.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs
        final IAdmin admin = getServiceFactory().getAdminService();
        final String groupName = enumValue(group);
        Experimenter e = new Experimenter();
        e.setOmeName(enumValue(omename));
        e.setFirstName(enumValue(firstname));
        e.setMiddleName(enumValue(middlename));
        e.setLastName(enumValue(lastname));
        e.setInstitution(enumValue(institution));
        e.setEmail(enumValue(email));
        long uid = admin.createUser(e, groupName);
        getLogger().info(
                String.format("Added user %s with id %d", e.getOmeName(), uid));
    }

}
