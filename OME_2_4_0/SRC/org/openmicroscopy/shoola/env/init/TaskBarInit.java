/*
 * org.openmicroscopy.shoola.env.init.TaskBarInit
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
import javax.swing.JFrame;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UIFactory;

/** 
 * Creates the {@link TaskBar} and links it to the container's
 * {@link Registry}.  
 * Also configures the look and feel.
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
public final class TaskBarInit
	extends InitializationTask
{

	/** Default value of the L&F entry in the container's configuration file. */
	private static final String		SYSTEM_LF = "system";
	

	/**
	 * Constructor required by superclass.
	 */
	public TaskBarInit() {}

	/**
	 * Returns the name of this task.
	 * @see InitializationTask#getName()
	 */
	String getName() 
	{
		return "Creating Task Bar";
	}

	/** 
	 * Does nothing, as this task requires no set up.
	 * @see InitializationTask#configure()
	 */
	void configure() {}

	/** 
	 * Loads the L&F, creates the task bar and then links it to the registry.
	 * The L&F is chosen as follows:
	 * <ul>
	 *  <li>If the look and feel configuration entry in the container's
	 *  configuration file is set to "system" (case insensitive), then
	 *  the system's L&F is loaded.</li>
	 *  <li>If the look and feel configuration entry in the container's
	 *  configuration file specifies a valid class name, then that L&F is
	 *  loaded.</li>
	 *  <li>Failing the above, the dafault L&F is loaded.</li>
	 * </ul>
	 * 
	 * @see InitializationTask#execute()
	 */
	void execute() 
		throws StartupException
	{
		Registry reg = container.getRegistry();
		String lookAndFeelClass = null;
		try {
			lookAndFeelClass = (String) reg.lookup(LookupNames.LOOK_N_FEEL);
			if (SYSTEM_LF.equalsIgnoreCase(lookAndFeelClass))
				lookAndFeelClass = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lookAndFeelClass);
			JFrame.setDefaultLookAndFeelDecorated(true);
			reg.getLogger().info(this, "Loaded L&F: "+lookAndFeelClass);
		} catch(Exception e) { 
			reg.getLogger().warn(this, "Can't load L&F: "+lookAndFeelClass+
									", will use default.");
		}
		TaskBar tb = UIFactory.makeTaskBar(container);
		RegistryFactory.linkTaskBar(tb, reg);
	}
	
	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}

}
