/*
 * org.openmicroscopy.shoola.env.config.AgentInfo
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
 
package org.openmicroscopy.shoola.env.config;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;

/** 
 * Holds the configration information for an <i>agent</i> tag in the
 * container's configuration file.
 * The content of each tag is stored by a member field.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AgentInfo 
{
	
	/** The value of the <code>name</code> tag. */																									
    private String				name; 
    
	/** The value of the <code>class</code> tag. */
	private String				agentClass;
	
	/** The value of the <code>config</code> tag. */
	private String				configPath;
	
	/** The Agent. */
	private Agent				agent;
	
	/** The Agent's registry. */
	private Registry			registry;
	 
	/** 
	 * Returns the value of the <code>name</code> tag. 
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
    
	/** 
	 * Returns the value of the <code>class</code> tag. 
	 * 
	 * @return See above.
	 */
    public String getAgentClass() { return agentClass; }
        
	/** 
	 * Returns the value of the <code>config</code> tag. 
	 * 
	 * @return See above.
	 */
    public String getConfigPath() { return configPath;}
    
	/** 
	 * Returns the {@link Agent}. 
	 * 
	 * @return See above.
	 */
	public Agent getAgent() { return agent; }

	/** 
	 * Returns the  {@link Registry}. 
	 * 
	 * @return See above.
	 */
	public Registry getRegistry() { return registry; }

	/** 
	 * Sets the {@link Registry}. 
	 * 
	 * @param registry The {@link Registry}.
	 */
	public void setRegistry(Registry registry) { this.registry = registry; }
	
	/** 
	 * Sets the {@link Agent}. 
	 * 
	 * @param agent The {@link Agent}. 
	 */
	public void setAgent(Agent agent) { this.agent = agent; }
	
	/** 
	 * Setter for the {@link #name} field. 
	 * 
	 * @param name The field to set.
	 */
	void setName(String name) { this.name = name; }
	
	/** 
	 * Setter for the {@link #agentClass} field.
	 * 
	 * @param agentClass The field to set.
	 */
	void setAgentClass(String agentClass) { this.agentClass = agentClass; }

	/** 
	 * Setter for the {@link #configPath} field.
	 * 
	 * @param configPath The field to set.
	 */
	void setConfigPath(String configPath) { this.configPath = configPath; }

}
