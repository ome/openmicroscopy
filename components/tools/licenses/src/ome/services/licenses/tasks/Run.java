/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.tasks;

import java.util.Properties;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.services.licenses.LicensedServiceFactory;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.Task;

/**
 * Command-line adapter which can run any task. {@link ServiceFactory} and
 * {@link Task} configuration can be specified as arguments in the form
 * "key=value". The only mandatory argument for all tasks is the task name:
 * <code>
 *   java Run task=org.example.MyTask
 * </code> However a search for tasks
 * will also be performed under "ome.util.tasks". E.g. <code>
 *   java Run task=admin.AddUserTask
 * </code>
 * resolves to ome.util.tasks.admin.AddUserTask.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see Configuration
 * @see Task
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public class Run extends ome.util.tasks.Run {

    /**
     * Parses the command line into a {@link Properties} instance which gets
     * passed to {@link Configuration}. {@link Configuration#createTask()} is
     * called and the returned {@link Task} instance is {@link Task#run() run}.
     */
    public static void main(String[] args) {
        Properties props = parseArgs(args);
        Configuration opts = new Config(props);
        Task task = opts.createTask();
        ((LicensedServiceFactory) task.getServiceFactory()).acquireLicense();
        try {
            task.run();
        } finally {
            ((LicensedServiceFactory) task.getServiceFactory())
                    .releaseLicense();
        }
    }

}

