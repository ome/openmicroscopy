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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.init.Initializer;
import org.openmicroscopy.shoola.env.init.StartupException;

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
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class Container
{

	/** 
	 * Points to the container's configuration file.
	 * The path is relative to the installation directory.
	 */
	public static final String		CONFIG_FILE = "config"+File.separator+
													"/container.xml";
	
	/**
	 * The sole instance.
	 * This object is passed around at initialization so that services'
	 * initialization tasks may link it to the service implementation. 
	 * Other
	 */
	private static Container		singleton;
	
	/**
	 * Entry point to launch the container and bring up the whole client.
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
	public static void startup(String home)
		throws StartupException
	{
		if (singleton == null) {
			singleton = new Container(home);
			Initializer initManager = new Initializer(singleton);
			initManager.configure();
			initManager.doInit();
			//startService() called by Initializer at end of doInit().
		}
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
		File f = new File(home==null ? "" : home);
		
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
	public String getHomeDir() 
	{
		return homeDir;
	}
	
	/**
	 * Returns the absolute path to the container's configuration file.
	 * 
	 * @return	See above.
	 */
	public String getConfigFile() 
	{
		File f = new File(homeDir, CONFIG_FILE);
		return f.getAbsolutePath();
	}

	/**
	 * Returns the container's registry.
	 * 
	 * @return	See above.
	 */
	public Registry getRegistry() 
	{
		return registry;
	}

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
		//TODO: implement.
	}
	
	/**
	 * Shuts down all agents, all services and exits the JVM process.
	 */
	public void exit()
	{
		//TODO: implement.
	}

}
