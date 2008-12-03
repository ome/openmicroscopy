/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;



//Java imports
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.FSImporterAgent;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.DataObject;

/** 
 * Factory to create {@link FSImporter} component.
 * This class keeps track of the {@link FSImporter} instance that has been 
 * created and is not yet in the {@link FSImporter#DISCARDED} state.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FSImporterFactory 
	implements ChangeListener
{

	/** The name associated to the component. */
	private static final String NAME = "FS importer";
	
	/** The name of the windows menu. */
	private static final String MENU_NAME = "FS importer";
	
	/** The sole instance. */
	private static final FSImporterFactory  singleton = new FSImporterFactory();
	
	/**
	 * Returns a {@link FSImporter}.
	 *  
	 * @return See above.
	 */
	public static FSImporter getImporter()
	{
		FSImporterModel model = new FSImporterModel();
		return singleton.getImporter(model);
	}
	
	/**
	 * Returns a {@link FSImporter}.
	 *  
	 * @param container The container where to import the images into.
	 * @return See above.
	 */
	public static FSImporter getImporter(DataObject container)
	{
		FSImporterModel model = new FSImporterModel();
		model.setContainer(container);
		return singleton.getImporter(model);
	}
	
	/** 
	 * Returns the <code>window</code> menu. 
	 * 
	 * @return See above.
	 */
	static JMenu getWindowMenu() { return singleton.windowMenu; }
	
	/**
	 * Returns <code>true</code> is the {@link #windowMenu} is attached 
	 * to the <code>TaskBar</code>, <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	static boolean isWindowMenuAttachedToTaskBar()
	{
		return singleton.isAttached;
	}

	/** Attaches the {@link #windowMenu} to the <code>TaskBar</code>. */
	static void attachWindowMenuToTaskBar()
	{
		if (isWindowMenuAttachedToTaskBar()) return;
		TaskBar tb = FSImporterAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}
	
	/** The tracked component. */
	private FSImporter importer;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean		isAttached;

	/** The windows menu. */
	private JMenu   	windowMenu;
	
	/** Creates a new instance. */
	private FSImporterFactory()
	{
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
	}

	/**
	 * Creates or recycles a importer component for the specified 
	 * <code>model</code>.
	 * 
	 * @param model	The Model.
	 * @return A {@link FSImporter}.
	 */
	private FSImporter getImporter(FSImporterModel model)
	{
		if (importer != null) return importer;
		FSImporterComponent comp = new FSImporterComponent(model);
		model.initialize(comp);
		//comp.addChangeListener(this);
		importer = comp;
		return importer;
	}
	
	/**
	 * Sets the {@link #viewer} to <code>null</code> when it is
	 * {@link TreeViewer#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */ 
	public void stateChanged(ChangeEvent ce)
	{
		FSImporterComponent comp = (FSImporterComponent) ce.getSource();
		if (comp.getState() == FSImporter.DISCARDED) importer = null;
	}
	
}
