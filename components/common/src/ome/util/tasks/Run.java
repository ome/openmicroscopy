/*
 * ome.util.tasks.Run
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.tasks;

// Java imports
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

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
 * @since 3.0-Beta1
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class Run {

    /**
     * Parses the command line into a {@link Properties} instance which gets
     * passed to {@link Configuration}. {@link Configuration#createTask()} is
     * called and the returned {@link Task} instance is {@link Task#run() run}.
     */
    public static void main(String[] args) {
        Run run = new Run(args);
        run.run();
    }
 
    protected Configuration opts;
    
    protected Task task;
    
    public Run(String[] args) {
        this(new Configuration(getProperties(args)));
    }
    
    public Run(Configuration config) {
        opts = config;
        task = opts.createTask();
    }

    public Run(Task taskInstance) {
        task = taskInstance;
    }
    
    public void run() {

        setup();
        try {
            task.run();
        } catch (TaskFailure te) {
            throw te;
        } catch (RuntimeException re) {
            System.err.println(task + " failed with a RuntimeException:");
            throw re;
        } finally {
            cleanup();
        }

    }
    
    // ~ Helpers
    // =========================================================================


    protected void setup() {
        // do nothing
    }
    
    protected void cleanup() {
        // do nothing
    }
    
    protected static Properties getProperties(String[] args) {
        Properties props = readStdin();
        props.putAll(parseArgs(args));
        return props;
    }
    
    protected static Properties readStdin() {
        Properties p = new Properties();
        int available = 0;
        try {
            available = System.in.available();
        } catch (IOException e1) {
            System.err.println("Could not check available bytes on standard in.");
            e1.printStackTrace();
        }
        if (available > 0) {
            try {
                p.load(System.in);
            } catch (IOException e) {
                System.err.println("Invalid properties file on standard in:");
                e.printStackTrace();
            }
        } else {
            System.err.println("No input on standard in.");
        }
        return p;
    }
    
    protected static Properties parseArgs(String[] args) {
        Properties p = new Properties();
        if (args == null || args.length == 0) {
            return p; // Early exit.
        }

        List<String> argList = Arrays.asList(args);
        for (int i = 0; i < args.length; i++) {
            String[] parts = args[i].split("=");
            if (parts.length == 1) {
                p.put(parts[0], "");
            } else if (parts.length == 2) {
                p.put(parts[0], parts[1]);
            } else {
                throw new IllegalArgumentException(
                        "Arguments can only have one \"=\".");
            }
        }
        return p;
    }

}
