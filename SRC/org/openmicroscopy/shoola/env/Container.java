/*
 * org.openmicroscopy.shoola.env.Container
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env;

//Java imports
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.data.login.LoginManager;
import org.openmicroscopy.shoola.env.init.Initializer;
import org.openmicroscopy.shoola.env.init.StartupException;
import org.openmicroscopy.shoola.env.rnd.RenderingEngine;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/** 
 * Oversees the functioning of the whole container, holds the container's
 * configuration and manages agents life-cycle. 
 * Also delegates intitialization tasks to 
 * {@link org.openmicroscopy.shoola.config.Initializer}.
 * 
 * <p>This class is a Singleton.  The singleton object can't be retrieved by 
 * arbitrary classes, it's only meant to be used during initialization and
 * linked to some of the container's services.</p>
 * <p>Initialization tasks use the singleton to access the registry and the 
 * agents' pool, so that these may be properly initialized. Initialization tasks
 * that bring up the container's services also link the singleton to the service
 * implementation where needed.</p> 
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

public final class Container
{

	/** 
	 * Points to the configuration directory.
	 * The path is relative to the installation directory.
	 */
	public static final String		CONFIG_DIR = "config";
	
	/** The name of the container's configuration file. */
	public static final String		CONFIG_FILE = "container.xml";
	
	/**
	 * The sole instance.
	 * This object is passed around at initialization so that services'
	 * initialization tasks may link it to the service implementation. 
	 */
	private static Container		singleton;
	
	
	/**
	 * Performs the start up procedure.
	 * 
	 * @param home	Path to the installation directory.  If <code>null<code> or
	 * 				empty, then the user directory is assumed.
	 */
	private static void runStartupProcedure(String home)
	{
		AbnormalExitHandler.configure();
		Initializer initManager = null;
		try {
			singleton = new Container(home);
			initManager = new Initializer(singleton);
			initManager.configure();
			initManager.doInit();
			//startService() called by Initializer at end of doInit().
		} catch (StartupException se) {
			if (initManager != null)	initManager.rollback();
			AbnormalExitHandler.terminate(se);
		} 
		//Any other exception will be handled automatically by
		//AbnormalExitHandler.  In this case, we don't rollback,
		//as something completely unforeseen happened, so it's 
		//better not to make assumptions on the state of the
		//initialization manager.
	}
	
	/**
	 * Returns the singleton instance.
	 * Only used by the {@link AbnormalExitHandler}.
	 * 
	 * @return	See above.
	 */
	static Container getInstance()
	{
		return singleton;
	}
	
	/**
	 * Entry point to launch the container and bring up the whole client.
	 * <p>The absolute path to the installation directory is obtained from
	 * <code>home</code>.  If this parameter doesn't specify an absolute path,
	 * then it'll be translated into an absolute path.  Translation is system 
	 * dependent -- in many cases, the path is resolved against the user 
	 * directory (typically the directory in which the JVM was invoked).</p>
	 * <p>This method rolls back all executed tasks and terminates the program
	 * if an error occurs during the initialization procedure.</p>
	 * 
	 * @param home	Path to the installation directory.  If <code>null<code> or
	 * 				empty, then the user directory is assumed.
	 */
	public static void startup(final String home)
	{
		if (singleton != null)	return;
		ThreadGroup root = new RootThreadGroup();
		Runnable r = new Runnable() {
			public void run() { runStartupProcedure(home); }
		};
		Thread t = new Thread(root, r, "Initializer");
		t.start();
		//Now the main thread exits and the initialization procedure is run
		//within the Initializer thread which belongs to root.  As a consequence
		//of this, any other thread created thereafter will belong to root or
		//a subgroup of root.
	}
	
	
	
	
	/** Absolute path to the installation directory. */
	private String		homeDir;
	
	/** The container's registry. */
	private Registry	registry;
	
	/** All managed agents. */
	private Set			agentsPool;
	
	
	/** 
	 * Intializes the member fields. 
	 * <p>The absolute path to the installation directory is obtained from
	 * <code>home</code>.  If this parameter doesn't specify an absolute path,
	 * then it'll be translated into an absolute path.  Translation is system 
	 * dependent -- in many cases, the path is resolved against the user 
	 * directory (typically the directory in which the JVM was invoked).</p>
	 * 
	 * @param home	Path to the installation directory.  If <code>null<code> or
	 * 				empty, then the user directory is assumed.
	 * @throws StartupException	If <code>home</code> can't be resolved to a
	 * 			valid and existing directory. 				
	 */
	private Container(String home)
		throws StartupException
	{
		//Convert to abstract pathname. 
		//(empty string leads to empty abstract pathname)
		File f = new File(home == null ? "" : home);
		
		//Now make it absolute. If the original path wasn't absolute, then
		//translation is system dependent. 
		f = f.getAbsoluteFile();
		homeDir = f.getAbsolutePath();
		
		//Make sure that what we've got is a directory. 
		if (!f.exists() || !f.isDirectory())
			throw new StartupException("Can't locate home dir: "+homeDir);
		
		agentsPool = new HashSet();
		registry = RegistryFactory.makeNew();
	}
	 
	/**
	 * Returns the absolute path to the installation directory.
	 * 
	 * @return	See above.
	 */
	public String getHomeDir() { return homeDir; }
	
	/**
	 * Returns the absolute path to the container's configuration file.
	 * 
	 * @return	See above.
	 */
	public String getConfigFile() { return resolveConfigFile(CONFIG_FILE); }
	
	/**
	 * Resolves <code>fileName</code> against the configuration directory.
	 * 
	 * @param fileName The name of a configuration file.
	 * @return	Returns the absolute path to the specified file.
	 */
	public String resolveConfigFile(String fileName)
	{
		if (fileName == null)	throw new NullPointerException();
		StringBuffer relPath = new StringBuffer(CONFIG_DIR);
		relPath.append(File.separatorChar);
		relPath.append(fileName);
		File f = new File(homeDir, relPath.toString());
		return f.getAbsolutePath();
	}

	/**
	 * Returns the container's registry.
	 * 
	 * @return	See above.
	 */
	public Registry getRegistry() { return registry; }

	/**
	 * Adds the specified agent to the pool of managed agents.
	 * 
	 * @param a	The agent.
	 * @return	<code>true</code> if <code>a</code> is not already in the pool,
	 * 			<code>false</code> otherwise.
	 * @throws	NullPointerException If <code>null</code>is passed in. 
	 */
	public boolean addAgent(Agent a) 
	{
		if (a == null)	throw new NullPointerException();
		return agentsPool.add(a);
	}
	
	/**
	 * Activates all services, all agents and starts interacting with the
	 * user. 
	 */
	public void startService()
	{
		List agents = (List) singleton.registry.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		Agent a;
		
		//Agents linking phase.
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			a = agentInfo.getAgent();
			a.setContext(agentInfo.getRegistry());
		}
		
		//Agents activation phase.
		i = agents.iterator();
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			a = agentInfo.getAgent();
			a.activate();
		}
	
		//TODO: activate services (EventBus, what else?).
		RenderingEngine re = RenderingEngine.getInstance(this);
		re.activate();
		LoginManager lm = LoginManager.getInstance(this);
		lm.activate();
		//TODO: RE threads should be spawn during an init task.
			
		//Get ready to interact with the user.
		TopFrame tf = singleton.registry.getTopFrame();
		tf.open();
	}
	
	/**
	 * Shuts down all agents, all services and exits the JVM process.
	 */
	public void exit()
	{
		//TODO: implement.
	}

}
