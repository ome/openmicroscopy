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
import java.net.URL;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;

/** 
 * For all Agents
 *
 * @see	InitializationTask
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
final class AgentsInit
	extends InitializationTask
{

	/**
	 * Constructor required by superclass.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 */
	AgentsInit(Container c)
	{
		super(c);
	}

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
	
	private void createAgent(AgentInfo info)
		throws StartupException
	{
		Class agentClass;
		Object agentInstance;
		URL configFile;
		Registry reg;
		try {
			//Load agent's class.
			agentClass = Class.forName(info.getAgentClass());
			
			//Make sure it implements the Agent I/F.
			if (!Agent.class.isAssignableFrom(agentClass))
				throw new Exception(agentClass+"'s type is not Agent.");
			
			//Create a new instance.
			agentInstance = agentClass.newInstance();
			
			//Resolve cfg file path against agent's class location.
			configFile = agentClass.getResource(info.getConfigPath());
			
			//Create the agent's registry.
			reg = createAgentsRegistry(configFile);
			
			//Fill up info. (Recall that this object is already in the
			//agents list within the container's registry.)
			info.setAgent((Agent) agentInstance);
			info.setRegistry(reg);
		} catch (Exception e) {
			throw new StartupException("Couldn't create agent: "+
										info.getName(), e);
		}
	}
	
	private Registry createAgentsRegistry(URL configFile)
		throws Exception
	{
		Registry agentReg = RegistryFactory.makeNew(configFile.getPath()),
					containerReg = container.getRegistry();
		RegistryFactory.linkEventBus(containerReg.getEventBus(), agentReg);
		RegistryFactory.linkLogger(containerReg.getLogger(), agentReg);
		RegistryFactory.linkDMS(containerReg.getDataManagementService(),
								agentReg);
		RegistryFactory.linkSTS(containerReg.getSemanticTypesService(),
								agentReg);
		RegistryFactory.linkTopFrame(containerReg.getTopFrame(), agentReg);
		RegistryFactory.linkUserNotifier(containerReg.getUserNotifier(),
											agentReg);
		//TODO: Link Image Service when ready.
		return agentReg;
	}

}
