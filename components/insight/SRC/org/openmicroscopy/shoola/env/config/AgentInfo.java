/*
 * org.openmicroscopy.shoola.env.config.AgentInfo
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
 
package org.openmicroscopy.shoola.env.config;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;

/** 
 * Holds the configuration information for an <i>agent</i> tag in the
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

	/** Identifies the <code>true</code> active value. */
	public static final String TRUE = "true";
	
	/** Identifies the <code>true</code> active value. */
	public static final String TRUE_SHORT = "t";
	
	/** Identifies the <code>false</code> active value. */
	public static final String FALSE = "false";
	
	/** Identifies the <code>false</code> active value. */
	public static final String FALSE_SHORT = "f";
	
	/** The value of the <code>name</code> tag. */
    private String				name; 
    
	/** The value of the <code>class</code> tag. */
	private String				agentClass;
	
	/** The value of the <code>config</code> tag. */
	private String				configPath;
	
	/** The value of the <code>active</code> tag. */
	private boolean				active;
	
	/** The value of the <code>number</code> tag if present.*/
	private int				number;
	
	/** The Agent. */
	private Agent				agent;
	
	/** The Agent's registry. */
	private Registry			registry;
	
	/** Creates a new instance.*/
	AgentInfo()
	{
		number = -1;
		active = true;
	}
	
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
    public String getConfigPath() { return configPath; }
    
    /** 
	 * Returns the value of the <code>active</code> tag. 
	 * 
	 * @return See above.
	 */
    public boolean isActive() { return active; }
    
    /** 
	 * Returns the value of the <code>number</code> tag. 
	 * 
	 * @return See above.
	 */
    public int getNumber() { return number; }
    
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
	 * Sets the {@link #name} field. 
	 * 
	 * @param name The field to set.
	 */
	void setName(String name) { this.name = name; }
	
	/** 
	 * Sets {@link #agentClass} field.
	 * 
	 * @param agentClass The field to set.
	 */
	void setAgentClass(String agentClass) { this.agentClass = agentClass; }

	/** 
	 * Sets the {@link #configPath} field.
	 * 
	 * @param configPath The field to set.
	 */
	void setConfigPath(String configPath) { this.configPath = configPath; }

	/** 
	 * Sets the {@link #active} field.
	 * 
	 * @param active The field to set.
	 */
	void setActive(String active)
	{ 
		if (active == null) this.active = true; 
		else {
			active = active.toLowerCase();
			if (TRUE.equals(active) || TRUE_SHORT.equals(active))
				this.active = true;
			else if (FALSE.equals(active) || FALSE_SHORT.equals(active))
				this.active = false;
			else this.active = true; 
		}
	}
	
	/** 
	 * Sets the {@link #master} field.
	 * 
	 * @param master The field to set.
	 */
	void setNumber(String number)
	{ 
		if (number != null) {
			try {
				this.number = Integer.parseInt(number);
			} catch (Exception e) {
				this.number = -1;
			}
		}
	}
	
}
