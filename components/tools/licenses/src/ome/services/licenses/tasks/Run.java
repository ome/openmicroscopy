/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.tasks;

import java.util.Properties;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.services.blitz.tasks.BlitzTask;
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
 * Use <code>
 *    blitz=true
 * </code> to configure the task for blitz.
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
        Config opts = new Config(getProperties(args));
        Run run = new Run(opts);
        run.run();
    }

    /** 
     * Passes the {@link Configuration}-subclass instance (see {@link Config}) 
     * to the {@link ome.util.tasks.Run#Run(Configuration)} constructor.
     * @param config
     */
    public Run(Config config) {
        super(config);
    }
    
    /**
     * Acquires a license during {@link ome.util.tasks.Run#setup()}
     */
    @Override
    protected void setup() {
        acquireLicense();
        super.setup();
    }
    
    /**
     * Releases a license during {@link ome.util.tasks.Run#cleanup()}
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        releaseLicense();
    }

    protected void acquireLicense() {
        BlitzTask bt = (BlitzTask)this.task;
        if (!bt.isBlitz()) {
            getServiceFactory().acquireLicense();
        } else {
            try {
                bt.getBlitzServiceFactory().createSession();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    protected void releaseLicense() {
        BlitzTask bt = (BlitzTask)this.task;
        if (!bt.isBlitz()) {
            getServiceFactory().releaseLicense();
        } else {
            try {
                bt.getBlitzServiceFactory().destroy();
            } catch (Exception e) {
                // ignore. will timeout eventually
            }
        }
    }
    
    protected LicensedServiceFactory getServiceFactory() {
        return (LicensedServiceFactory) this.task.getServiceFactory();
    }
}

