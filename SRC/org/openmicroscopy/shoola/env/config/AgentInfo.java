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
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */
class AgentInfo 
{
	static final String         NAME = "name", 
									CLASS = "class", 
									CONFIG = "config";
	
	/** The value of the <code>name</code> tag. */																									
    private String		name; 
    
	/** The value of the <code>class</code> tag. */
	private String		agentClass;
	
	/** The value of the <code>config</code> tag. */
	private String		configPath;
	
    
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
    String getName()
    {
        return name;
    }
    
	/**  
	 * Returns the value of the <code>class</code> tag.
	 *
	 * @return	See above.
	 */
    String getAgentClass()
    {
        return agentClass;
    }
        
	/**
	 * Returns the value of the <code>config</code> tag. 
	 *
	 * @return	See above.
	 */
    String getConfigPath()
    {
        return configPath;
    }
}
