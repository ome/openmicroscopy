/*
 * org.openmicroscopy.shoola.env.init.AgentsInit
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

package org.openmicroscopy.shoola.env.init;

//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;

/** 
 * This task creates all agents specified in the container's configuration file
 * and, for each of them, populates their own registry.
 *
 * @see	InitializationTask
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
public final class AgentsInit
	extends InitializationTask
{

	/**
	 * Constructor required by superclass.
	 */
	public AgentsInit() {}

	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName()
	{
		return "Loading Agents";
	}

	/** 
	 * Does nothing, as this task requires no set up.
	 * @see InitializationTask#configure()
	 */
	void configure() {}

	/** 
	 * Carries out this task.
	 * @see InitializationTask#execute()
	 */
	void execute() 
		throws StartupException
	{
		Registry reg = container.getRegistry();
		List agents = (List) reg.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			createAgent(agentInfo);
		}
	}

	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}
	
	/**
	 * Instantiates, by reflection, the specified agent and populates its
	 * registry.
	 * This method will set the new agent instance and its registry into the
	 * passed <code>info</code> object.
	 * 
	 * @param info	Specifies which class to instantiate and collects the 
	 * 				agent instance as well as its registry.  
	 * @throws StartupException If the agent couldn't be instantiated or its
	 * 							registry couldn't be populated.
	 */
	private void createAgent(AgentInfo info)
		throws StartupException
	{
		Class agentClass;
		Object agentInstance;
		Registry reg;
		try {
			//Load agent's class.
			agentClass = Class.forName(info.getAgentClass());
			
			//Make sure it implements the Agent I/F.
			if (!Agent.class.isAssignableFrom(agentClass))
				throw new Exception(agentClass+"'s type is not Agent.");
			
			//Create a new instance.
			agentInstance = agentClass.newInstance();
			
			//Create the agent's registry.
			reg = createAgentRegistry(info.getConfigPath());
			
			//Fill up info. (Recall that this object is already in the
			//agents list within the container's registry.)
			info.setAgent((Agent) agentInstance);
			info.setRegistry(reg);
		} catch (Exception e) {
			throw new StartupException("Couldn't create agent: "+
										info.getName(), e);
		}
	}
	
	/**
	 * Creates a new registry from the specified configuration file.
	 * The new registry is populated with all entries from the configuration
	 * file plus links to the container's services.
	 * 
	 * @param configFile	Relative pathname to the configuration file.  The
	 * 						pathname is resolved against the configuration
	 * 						directory. 
	 * @return A new registry, populated as specified above.
	 * @throws Exception If the configuration file couldn't be read in and
	 * 						parsed correclty.
	 */
	private Registry createAgentRegistry(String configFile)
		throws Exception
	{
		String absPathName = container.resolveConfigFile(configFile);
		Registry agentReg = RegistryFactory.makeNew(absPathName),
					containerReg = container.getRegistry();
		RegistryFactory.linkEventBus(containerReg.getEventBus(), agentReg);
		RegistryFactory.linkLogger(containerReg.getLogger(), agentReg);
		RegistryFactory.linkDMS(containerReg.getDataManagementService(),
								agentReg);
		RegistryFactory.linkSTS(containerReg.getSemanticTypesService(),
								agentReg);
        RegistryFactory.linkPS(containerReg.getPixelsService(),
                               agentReg);
		RegistryFactory.linkTaskBar(containerReg.getTaskBar(), agentReg);
		RegistryFactory.linkUserNotifier(containerReg.getUserNotifier(),
											agentReg);
		//TODO: Link Image Service when ready.
		return agentReg;
	}

}
