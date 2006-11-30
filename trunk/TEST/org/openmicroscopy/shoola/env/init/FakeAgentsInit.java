/*
 * org.openmicroscopy.shoola.env.init.FakeAgentsInit
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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Fake intialization task.
 * Binds the container's registry to the agents list specified by its
 * static field.
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
public class FakeAgentsInit
    extends InitializationTask
{
    
    /**
     * Default an empty list, so no agent will show up.
     * Change it to whatever implementation is required by your tests &#151;
     * the list must contain an 
     * {@link org.openmicroscopy.shoola.env.config.AgentInfo} object for
     * each agent you want to create.
     */
    public static List     agentsList = new ArrayList();

    
    /**
     * Constructor required by superclass.
     */
    public FakeAgentsInit() {}

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
        reg.bind(LookupNames.AGENTS, agentsList);
    }

    /** 
     * Does nothing.
     * @see InitializationTask#rollback()
     */
    void rollback() {}

}
