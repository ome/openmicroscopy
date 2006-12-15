/*
 * ome.util.tasks.Configuration
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports
import java.lang.reflect.Constructor;
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

import static ome.util.tasks.Configuration.Keys.*;

/**
 * Helper for creating any task from String properties, and can even instantiate
 * {@link ServiceFactory} and {@link Task} objects if given the proper
 * arguments.
 * 
 * Understands the parameters:
 * <ul>
 * <li>task</li>
 * <li>user</li>
 * <li>group</li>
 * <li>type</li>
 * <li>pass</li>
 * <li>host</li>
 * <li>port</li>
 * </ul>
 * 
 * To login as a root, for example, {@link Properties} of the form: <code>
 * {task=mytask, user=root, group=system, type=Task, pass=SECRET}
 * </code>
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see Configuration
 * @see Task
 * @since 3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class Configuration {

    /** Default package in which {@link Task} lookups will be performed. */
    public final static String DEFAULTPKG = "ome.util.tasks";

    /**
     * Enumeration of the string values which will be used directly by
     * {@link Configuration}.
     */
    public enum Keys {
        task, user, group, type, pass, host, port
    }

    final Properties properties;

    Class<Task> taskClass;

    Server server;

    Login login;

    /**
     * Sole constructor. Performs the necessary parsing of the
     * {@link Properties} values.
     * 
     * @param props
     *            Not null.
     */
    public Configuration(Properties props) {
        if (props == null) {
            throw new IllegalArgumentException("Argument cannot be null.");
        }

        this.properties = props;

        if (p(task) == null) {
            throw new IllegalArgumentException("task must be provided.");
        }

        taskClass = parseTask(p(task), "");
        if (taskClass == null) {
            taskClass = parseTask(p(task), DEFAULTPKG);
        }
        if (taskClass == null) {
            throw new IllegalArgumentException("Cannot find task class for:"
                    + task);
        }

        if (p(host) != null || p(port) != null) {
            if (p(host) == null || p(port) == null) {
                throw new IllegalArgumentException(
                        "host and port must be specified together.");
            }
            server = new Server(p(host), Integer.valueOf(p(port)).intValue());
        }

        if (p(user) != null || p(group) != null || p(type) != null
                || p(pass) != null) {
            if (p(user) == null || p(group) == null || p(type) == null
                    || p(pass) == null) {
                throw new IllegalArgumentException(
                        "user, group, pass, and type must be specified together.");
            }

            login = new Login(p(user), p(pass), p(group), p(type));

        }

    }

    /**
     * Returns the Properties instance provided during constuction.
     */
    public Properties getProperties() {
        return properties; // TODO should copy?
    }

    /**
     * Returns the {@link Class} found in the {@link Properties}
     */
    public Class<Task> getTaskClass() {
        return taskClass;
    }

    /**
     * Returns a non-null {@link Server} instance only if server arguments were
     * provided in the {@link Properties}
     */
    public Server getServer() {
        return server;
    }

    /**
     * Returns a non-null {@link Login} instance only if login arguments were
     * provided in the {@link Properties}
     */
    public Login getLogin() {
        return login;
    }

    /**
     * Creates a {@link ServiceFactory} instance based on the values of
     * {@link #getServer()} and {@link #getLogin()} (such that a subclass could
     * override these methods to influence the ServiceFactory).
     * 
     * @return a non-null {@link ServiceFactory} instance.
     */
    public ServiceFactory createServiceFactory() {
        if (getLogin() != null && getServer() != null) {
            return new ServiceFactory(getServer(), getLogin());
        }

        if (getLogin() != null) {
            return new ServiceFactory(getLogin());
        }

        if (getServer() != null) {
            return new ServiceFactory(getServer());
        }

        return new ServiceFactory();
    }

    /**
     * Creates a new {@link Task} based on the values of
     * {@link #getProperties()}, {@link #getTaskClass()}, and
     * {@link #createServiceFactory()}.
     * 
     * @return a non-null {@link Task} instance, ready for execution.
     * @throws RuntimeException
     *             if anything happens during the reflection-based creation of
     *             the {@link Task}
     */
    public Task createTask() {
        Constructor<Task> ctor;
        try {
            ctor = getTaskClass().getConstructor(ServiceFactory.class,
                    Properties.class);
            Task newTask = ctor.newInstance(createServiceFactory(),
                    getProperties());
            return newTask;
        } catch (Exception e) {
            if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    // ~ Helpers
    // =========================================================================

    /**
     * Returns the property for the {@link Keys#toString()} value of the
     * argument.
     */
    protected String p(Keys key) {
        return properties.getProperty(key.toString());
    }

    /**
     * Adds the package to the task name, and returns the class if found.
     * Otherwise, null.
     */
    protected Class<Task> parseTask(String task, String pkg) {
        String fqn = pkg + "." + task;
        try {
            return (Class<Task>) Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
