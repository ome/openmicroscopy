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
 * Holds the configration information for an agent entry in the container's
 * configuration file.
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
	static final String         NAME = "name", 
								CLASS = "class", 
								CONFIG = "config";
	
	/** The value of the <code>name</code> tag. */																									
    private String				name; 
    
	/** The value of the <code>class</code> tag. */
	private String				agentClass;
	
	/** The value of the <code>config</code> tag. */
	private String				configPath;
	
	private Agent				agent;
	
	private Registry			registry;
	/** Set the pair (name, value).
	* 
	* @param value		tag's value.
	* @param tag		tag's name.
	*/
    void setValue(String value, String tag)
    {
        try {
            if (tag.equals(NAME))           name = value;
            else if (tag.equals(CLASS))     agentClass = value;
            else if (tag.equals(CONFIG))    configPath = value;
        } catch (Exception ex) { 
        	throw new RuntimeException(ex);  //TODO: proper exception handling 
        }
    }
    
	/**
	 * Returns the value of the <code>name</code> tag.
	 *
	 * @return	See above.
	 */
    public String getName()
    {
        return name;
    }
    
	/**  
	 * Returns the value of the <code>class</code> tag.
	 *
	 * @return	See above.
	 */
    public String getAgentClass()
    {
        return agentClass;
    }
        
	/**
	 * Returns the value of the <code>config</code> tag. 
	 *
	 * @return	See above.
	 */
    public String getConfigPath()
    {
        return configPath;
    }
    
	/**
	 * Return the {@link Agent}.
	 * 
	 * @return See above.
	 */
	public Agent getAgent() {
		return agent;
	}

	/**
	 * Return the  {@link Registry}.
	 * 
	 * @return See above.
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * Set the {@link Registry}.
	 * 
	 * @param registry 
	 */
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
	/**
	 * Set the {@link Agent}.
	 * 
	 * @param agent
	 */
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

}
