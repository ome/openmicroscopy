/*
 * org.openmicroscopy.shoola.agents.zoombrowser.UIManager
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

package org.openmicroscopy.shoola.agents.zoombrowser.ui;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.data.DatasetLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ProjectLoader;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentGroupSubscriber;

/** 
 * Creates and controls the {@link MainWindow}.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * after code by 
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
public class UIManager implements ContentGroupSubscriber
{
	
	private DataManager manager;

	/**
	 * Inherits from {@link TopWindow}, so it's automatically linked to the
	 * {@link TaskBar}.
	 */
	private MainWindow			mainWindow;
		
	/**
	 * Creates a new instance.
	 * 
	 * @param config	A reference to this agent's registry.
	 */
	public UIManager(DataManager  manager)
	{
		this.manager = manager;
		
		mainWindow = new MainWindow(manager);
		ContentGroup group = new ContentGroup(this);
		
		final DatasetLoader dl = new DatasetLoader(manager,group);
		final ProjectLoader pl = new ProjectLoader(manager,group);
		group.setAllLoadersAdded();

	}
	

	/**
	 * Releases all UI resources currently in use and returns them to the OS.
	 */
	public void disposeUI()
	{
		mainWindow.dispose();
	}
	
	public void contentComplete() {
		if (manager.getDatasets() != null || manager.getProjects() != null)
			mainWindow.buildGUI();
	}
}
