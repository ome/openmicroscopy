/*
 * org.openmicroscopy.shoola.env.init.Initializer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.init;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;

/** 
 * Manages the initialization procedure -- creates all needed initialization 
 * tasks, configures them, queues them up for execution, and finally executes
 * all those tasks.  
 * <p>Tasks are encapsulated by objects (concrete instances of 
 * {@link InitializationTask}) and treated as commands -- this class manages
 * their execution, acting as a Command Processor.</p>
 * <p>This class is also responsible for configuring the initialization
 * procedure, which involves creating all tasks, calling their 
 * <code>configure</code> method and decide on the execution order -- thus, it
 * also takes on the role of a Controller.</p>
 * <p>Finally, this class allows {@link InitializationListener}s to subscribe
 * for notification of initialization procedure progress -- in this regard, it
 * plays the role of a Publisher.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class Initializer
{
    
    /**
     * Lists the tasks that will make up the initialization sequence.
     * Note that this list can be accessed by subclasses.  This is key in
     * testing environments because initialization tasks can be replaced
     * with tasks that provide service stubs to remove problematic 
     * dependencies on external resources.
     */
    protected static final List<Class<?>>     initList = new ArrayList<Class<?>>();
    
    static {
        //This must be the first task to run b/c it will bring up
        //the splash screen.
        //initList.add(SplashScreenInit.class);
        
        //NB: All tasks require this one to be run first 
        //(b/c it creates and fills up the container's config).
    	
        initList.add(ContainerConfigInit.class);
        initList.add(SplashScreenInit.class);
        initList.add(LoggerInit.class);
        initList.add(CacheInit.class);
        initList.add(EventBusInit.class);
        initList.add(CmdProcessorInit.class);
        initList.add(DataServicesInit.class);
        initList.add(RenderingServicesInit.class);
        initList.add(LoginServiceInit.class);
        initList.add(TaskBarInit.class);
        initList.add(UserNotifierInit.class);
        initList.add(AgentsInit.class);
    }
    
    /**
     * Instantiates the specified initialization task and links it to the
     * passed {@link Container} and {@link Initializer}.
     * 
     * @param taskType  The class of the task.  Mustn't be <code>null</code>.
     * @param c         Reference to the {@link Container}.
     *                  Mustn't be <code>null</code>.
     * @param i         Reference to the {@link Initializer}.
     *                  Mustn't be <code>null</code>.
     * @return An instance of the requested task type.
     * @throws StartupException If the task couldn't be created.
     */
    private static InitializationTask createInitTask(Object taskType, 
                                                    Container c, Initializer i) 
        throws StartupException
    {
        //Sanity checks.
        if (taskType == null) throw new NullPointerException("No task type.");
        if (c == null) throw new NullPointerException("No container.");
        if (i == null) throw new NullPointerException("No initializer.");
        
        //Instantiate through reflection, then link to c and i.
        InitializationTask task = null;
        try {
            Class<?> taskClass = (Class<?>) taskType;
            task = (InitializationTask) taskClass.newInstance();
            task.linkContainer(c);
            task.linkInitializer(i);
        } catch (InstantiationException ie) {
            throw new StartupException(
                    "Couldn't instantiate initialization task.", ie);
        } catch (IllegalAccessException iae) {
            throw new StartupException(
                    "Couldn't instantiate initialization task.", iae);
        } catch (ClassCastException cce) {
            throw new StartupException(
                    "Invalid initialization task: "+
                    taskType+" doesn't extends InitializationTask.");
        }
        return task;
    }

	/** Queue to order the tasks to be executed. */
	private List<InitializationTask>	processingQueue;
	
	/** The task that is currently processed. */
	private InitializationTask			currentTask;
	
	/** The tasks that have currently been executed. */
	private Stack<InitializationTask>	doneTasks;
	
	/** The notification set for initialization progress. */
	private Set<InitializationListener>	initListeners;
	
	/** A reference to the singleton {@link Container}. */
	private Container					container;
	 
    /**
     * Calls the <code>onStart</code> method of each subscriber in the
     * notification set.
     */
    private void notifyStart()
    {
        int size = processingQueue.size()-2;
        Iterator<InitializationListener> i = initListeners.iterator();
        while (i.hasNext())
            i.next().onStart(size);
    }
    
    /**
     * Calls the <code>onExecute</code> method of each subscriber in the
     * notification set.
     */
    private void notifyExecute()
    {
        String name = currentTask.getName();
        Iterator<InitializationListener> i = initListeners.iterator();
        while (i.hasNext()) 
            i.next().onExecute(name);
    }
    
    /**
     * Calls the <code>onEnd</code> method of each subscriber in the
     * notification set.
     */
    public void notifyEnd()
    {
        Iterator<InitializationListener> i = initListeners.iterator();
        while (i.hasNext())
            i.next().onEnd();

		//Allow for referenced objects to be garbage collected (see below).
		currentTask = null;
		processingQueue = null;
		initListeners = null;
		//Now control goes into container object, so this object won't be
		//garbage collected until startService() returns, which may be on exit.
		container.startService();
    }

    /**
     * Only for the benefit of subclasses that want to modify the
     * {@link #initList}.
     * This should only be done for the purpose of testing.
     */
    protected Initializer() {}
    
    /**
	 * Creates a new instance.
	 * The {@link Container} is the only class that can possibly create this
	 * object. In fact, the {@link Container} is the only class to have
	 * a reference to the singleton {@link Container}.
	 * 
	 * @param c	A reference to the singleton {@link Container}.
	 */
	public Initializer(Container c)
	{
		this(c, false);
	}
	/**
	 * Creates a new instance.
	 * The {@link Container} is the only class that can possibly create this
	 * object. In fact, the {@link Container} is the only class to have
	 * a reference to the singleton {@link Container}.
	 * 
	 * @param c	A reference to the singleton {@link Container}.
	 * @param headless Flag indicating to start the application head-less or not.
	 */
	public Initializer(Container c, boolean headless)
	{
		if (c == null) throw new NullPointerException();
		processingQueue = new ArrayList<InitializationTask>();
		doneTasks = new Stack<InitializationTask>(); 
		initListeners = new HashSet<InitializationListener>();
		if (headless) initList.remove(SplashScreenInit.class);
		container = c;
	}
	
	/**
	 * Creates all needed initialization tasks, configures them, and queues
	 * them up for execution.
	 * This method has to be called before {@link #doInit()}.
     * 
     * @throws StartupException If an error occurs while configuring an
     *                          initialization task.
	 */
	public void configure()
        throws StartupException
	{
		InitializationTask task;
        Iterator<Class<?>> type = initList.iterator();
        while (type.hasNext()) {
            task = createInitTask(type.next(), container, this); 
            task.configure();
            processingQueue.add(task);
        }
	}
	
	/**
	 * Performs the initialization procedure.
	 * 
	 * @throws StartupException	If an error occurs while executing an
	 * 							initialization task.
	 */
	public void doInit()
		throws StartupException
	{
		Iterator<InitializationTask> i = processingQueue.iterator();
		
		//Tell all listeners that we're about to start.
		notifyStart();
		
		//Execute all pending tasks. 
		while (i.hasNext()) {
			currentTask = i.next();
			notifyExecute();  //Tell listeners we're about to exec a task.
			currentTask.execute();
			doneTasks.push(currentTask);  //For later rollback if needed.
		}
		
		/*
		//Tell all listeners that we're finished.
		notifyEnd();
		
		//Allow for referenced objects to be garbage collected (see below).
		currentTask = null;
		processingQueue = null;
		initListeners = null;
		//Now control goes into container object, so this object won't be
		//garbage collected until startService() returns, which may be on exit.
		container.startService();
		*/
	}
	
	/**
	 * Rolls back all tasks that have been executed at the time this method
	 * is invoked.
	 */
	public void rollback()
	{
		Iterator<InitializationTask> i = doneTasks.iterator();
		while (i.hasNext())
			i.next().rollback();
	}
	
	/**
	 * Adds the specified initialization listener to the notification set.
	 * 
	 * @param subscriber	The listener.
	 * @return 	<code>true</code> if <code>subscriber</code> is not already in 
	 * 			the notification set, <code>false</code> otherwise.
	 * @throws	NullPointerException If <code>null</code> is passed in. 
	 */
	public boolean register(InitializationListener subscriber) 
	{
		if (subscriber == null) throw new NullPointerException();
		return initListeners.add(subscriber);
	}
	
}
