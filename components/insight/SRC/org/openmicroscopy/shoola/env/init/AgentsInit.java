/*
 * org.openmicroscopy.shoola.env.init.AgentsInit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.util.Iterator;
import java.util.List;

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
	 * Instantiates, by reflection, the specified agent and populates its
	 * registry.
	 * This method will set the new agent instance and its registry into the
	 * passed <code>info</code> object.
	 * 
	 * @param info	Specifies which class to instantiate and collects the 
	 * 				agent instance as well as its registry.
	 * @param value The number of the master.
	 * @throws StartupException If the agent couldn't be instantiated or its
	 * 							registry couldn't be populated.
	 */
	private void createAgent(AgentInfo info, int value)
		throws StartupException
	{
		if (!info.isActive()) return;
		Class agentClass;
		Object agentInstance;
		Registry reg;
		try {
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
			Agent agent = (Agent) agentInstance;
			info.setAgent(agent);
			info.setRegistry(reg);
			Registry containerRegistry = container.getRegistry();
			//Register the master
			if (info.isActive()) {
				if (info.getNumber() == value && 
					value == LookupNames.IMPORTER_ENTRY) {
					containerRegistry.bind(LookupNames.MASTER,
							LookupNames.MASTER_IMPORTER);
				}
			}
			reg.bind(LookupNames.DATA_DISPLAY,
					containerRegistry.lookup(LookupNames.DATA_DISPLAY));
            reg.bind(LookupNames.DEBUGGER_ADDRESS,
                    containerRegistry.lookup(LookupNames.DEBUGGER_ADDRESS));
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
	 * 						parsed correctly.
	 */
	private Registry createAgentRegistry(String configFile)
		throws Exception
	{
		String pathName = container.getConfigFileRelative(configFile);
		Registry agentReg = RegistryFactory.makeNew(pathName),
					containerReg = container.getRegistry();
		RegistryFactory.linkEventBus(containerReg.getEventBus(), agentReg);
		RegistryFactory.linkLogger(containerReg.getLogger(), agentReg);
        RegistryFactory.linkIS(containerReg.getImageService(),
                               agentReg);
		RegistryFactory.linkTaskBar(containerReg.getTaskBar(), agentReg);
		RegistryFactory.linkUserNotifier(containerReg.getUserNotifier(),
											agentReg);
        RegistryFactory.linkOS(containerReg.getDataService(), agentReg);
        RegistryFactory.linkMS(containerReg.getMetadataService(), agentReg);
        RegistryFactory.linkAdmin(containerReg.getAdminService(), agentReg);
		return agentReg;
	}
	
	/** Constructor required by superclass. */
	public AgentsInit() {}

	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName() { return "Loading Agents"; }

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
	    Integer v = (Integer) reg.lookup(LookupNames.ENTRY_POINT);
	    int value = LookupNames.INSIGHT_ENTRY;
	    if (v != null) {
	        switch (v.intValue()) {
	            case LookupNames.IMPORTER_ENTRY:
	            case LookupNames.INSIGHT_ENTRY:
	                value = v.intValue();
	        }
	    }

	    List<AgentInfo> agents =
	            (List<AgentInfo>) reg.lookup(LookupNames.AGENTS);
	    Iterator<AgentInfo> i = agents.iterator();
	    while (i.hasNext()) 
	        createAgent(i.next(), value);
	    String name = (String) container.getRegistry().lookup(
	            LookupNames.MASTER);
	    if (name == null) {
	        name = LookupNames.MASTER_INSIGHT;
	    }
	    //check if run as an ij plugin.
	    Integer plugin = (Integer) container.getRegistry().lookup(
	            LookupNames.PLUGIN);
	    if (plugin != null) {
	        switch (plugin) {
	            case LookupNames.IMAGE_J:
	            case LookupNames.IMAGE_J_IMPORT:
	                name = LookupNames.MASTER_IJ;
	        }
	    }
	    container.getRegistry().bind(LookupNames.MASTER, name);
	}

	/**
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}

}
