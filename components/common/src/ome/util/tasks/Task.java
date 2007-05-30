/*
 * ome.util.tasks.Task
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports
import java.util.Properties;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.system.ServiceFactory;

/**
 * Base task class which defines the template methods for running a task. The
 * base class ensures that a {@link ServiceFactory} and a {@link Properties}
 * instance will be available in the task runtime.
 * 
 * {@link Task} implements {@link Runnable} and therefore can be placed directly
 * in a {@link Thread} for execution. The {@link Runnable#run()} method,
 * however, has been marked final and instead other {@link Task} methods must be
 * overwritten.
 * 
 * Execution begins with a call to {@link #init()}, then {@link #doTask()} is
 * called in a try/catch/finally block. If {@link #doTask()} returns normally,
 * the value of {@link #completedSuccessfully()} will be true, otherwise it will
 * remain false and execution will be passed to
 * {@link #handleException(RuntimeException)}. Finally, {@link #close()} is
 * called. Subclasses should most likely check the value of
 * {@link #completedSuccessfully()} before attempting {@link #close()};
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see Runnable
 * @since 3.0-Beta1
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public abstract class Task implements Runnable {

    final private ServiceFactory sf;

    final private Properties props;

    final private Log log = LogFactory.getLog(this.getClass());

    private boolean completedSuccessfully = false;

    /** 
     * Protected constructor for subclasses who may not wish to use {@link #sf}.  
     */
    protected Task(Properties properties) {
        sf = null;
        props = properties == null ? System.getProperties() : properties;
    }
    
    /**
     * Sole constructor. Requires a non-null {@link ServiceFactory}. If a null
     * {@link Properties} instance is provided, the
     * {@link System#getProperties() System properties} will be used.
     * 
     * @param serviceFactory
     *            Cannot be null.
     * @param properties
     *            Context variables for the task. Optional (can be null).
     */
    public Task(ServiceFactory serviceFactory, Properties properties) {
        if (serviceFactory == null) {
            throw new IllegalArgumentException("ServiceFactory cannot be null.");
        }
        this.sf = serviceFactory;
        this.props = properties == null ? System.getProperties() : properties;
    }

    /**
     * returns the {@link ServiceFactory} instance provided during construction.
     * 
     * @return {@link ServiceFactory}. Never null.
     */
    final public ServiceFactory getServiceFactory() {
        return this.sf;
    }

    /**
     * returns the {@link Properties} instance defined during construction. If
     * none was provided at construction, the System properties will be used.
     * 
     * @return {@link Properties}. Never null.
     */
    final public Properties getProperties() {
        return this.props; // TODO should this be defensively copied?
    }

    /**
     * returns the logger instance active for this class. This is not a static
     * instance.
     * 
     * @return logger. Not null.
     */
    final public Log getLogger() {
        return log;
    }

    /**
     * returns true if {@link #doTask()} returned normally or false if an
     * exception was thrown.
     */
    final public boolean completedSuccessfully() {
        return completedSuccessfully;
    }

    /**
     * 
     * Marked final so that the {@link Task} lifecycle will never vary.
     */
    public final void run() {
        init();
        try {
            doTask();
            completedSuccessfully = true;
        } catch (RuntimeException re) {
            handleException(re);
        } finally {
            close();
        }
    }

    // ~ Lifecycle methods
    // =========================================================================

    /**
     * Performs initialization of the {@link Task}.
     * 
     * Subclasses should make a best effort attempt to not throw any exceptions.
     */
    public abstract void init();

    /**
     * Performs the actual work of the {@link Task}. Execution takes place in a
     * try/catch/finally-block, and subclasses can throw any
     * {@link RuntimeException}.
     * 
     * @throws RuntimeException
     */
    public abstract void doTask() throws RuntimeException;

    /**
     * If an exception is thrown during {@link #doTask()}, then it will be
     * passed to the {@link #handleException(RuntimeException)} method during
     * the catch-block of {@link #run()}.
     * 
     * Subclasses can handle the exception or rethrow.
     * 
     * @param re
     *            Non-null.
     */
    public abstract void handleException(RuntimeException re);

    /**
     * Performs cleanup of the {@link Task}.
     * 
     * Subclasses should make a best effort attempt to not throw any exceptions.
     */
    public abstract void close();

    // ~ Helpers
    // =========================================================================

    /**
     * returns the value of the string representation of the enum argument from
     * the {@link Properties} instance for this task.
     * 
     * @param e
     *            Any enumeration. May not be null.
     */
    protected String enumValue(Enum e) {
        if (e == null) {
            throw new ApiUsageException("Enum cannot be null.");
        }
        return getProperties().getProperty(e.toString());
    }
}
