/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.env.data.events.RemoveGroupEvent;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.ui.TaskBar;

/** 
 * Factory to create {@link Importer} component.
 * This class keeps track of the {@link Importer} instance that has been 
 * created and is not yet in the {@link Importer#DISCARDED} state.
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
public class ImporterFactory 
	implements ChangeListener
{

	/** The name associated to the component. */
	private static final String NAME = "Importer";
	
	/** The name of the windows menu. */
	private static final String MENU_NAME = "Importer";
	
	/** The sole instance. */
	private static final ImporterFactory  singleton = new ImporterFactory();

	/**
	 * Returns <code>true</code> if the importer already exists,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean doesImporterExist()
	{
		return singleton.importer != null;
	}

	/**
	 * Returns a {@link Importer}.
	 * 
	 * @param groupId The identifier of the current group.
	 * @param master Pass <code>true</code> if the importer is used a 
	 *               stand-alone application, <code>false</code> otherwise.
	 * @param displayMode Group/Experimenter view.
	 * @return See above.
	 */
	public static Importer getImporter(long groupId, boolean master, int
			displayMode)
	{
		ImporterModel model = new ImporterModel(groupId, master, displayMode);
		return singleton.getImporter(model);
	}
	
	/**
	 * Returns a {@link Importer}.
	 * 
	 * @param groupId The identifier of the current group.
	 * @param displayMode Group/Experimenter view.
	 * @return See above.
	 */
	public static Importer getImporter(long groupId, int displayMode)
	{
		return getImporter(groupId, false, displayMode);
	}

	/**
	 * Notifies the model that the user's group has successfully be modified
	 * if the passed value is <code>true</code>, unsuccessfully 
	 * if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, <code>false</code>
	 * 					otherwise.
	 */
	public static void onGroupSwitched(boolean success)
	{
		if (!success)  return;
		if (singleton.importer != null && 
				((ImporterComponent) singleton.importer).isMaster()) {
			((ImporterComponent) singleton.importer).onGroupSwitched(success);
			return;
		}
		singleton.clear();
	}

	   /** Close all the instances.*/
    public static void terminate()
    {
        if (singleton.importer != null) {
            ((ImporterComponent) singleton.importer).shutDown();
        }
    }
    
	/** Invokes when a new user has reconnected.*/
	public static void onReconnected()
	{
		if (singleton.importer != null) {
			singleton.importer.discard();
			singleton.windowMenu.removeAll();
			singleton.isAttached = false;
			TaskBar tb = ImporterAgent.getRegistry().getTaskBar();
			tb.removeFromMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
			singleton.importer = null;
		}
	}
	
	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	public static void setDiplayMode(int displayMode)
	{
		if (singleton.importer == null) return;
		((ImporterComponent) singleton.importer).setDisplayMode(displayMode);
	}

	/**
	 * Checks if there are on-going imports into the specified group
	 * before closing it.
	 *
	 * @param ctx The context to handle.
	 */
	public static void hasOnGoingImport(SecurityContext ctx)
	{
	    if (singleton.importer == null || ctx == null) return;
	    //no import so we can close the group
	    if (!((ImporterComponent) singleton.importer).hasOnGoingImport(ctx)) {
	        ImporterAgent.getRegistry().getEventBus().post(
	                new RemoveGroupEvent(ctx));
	    }
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
		TaskBar tb = ImporterAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}
	
	/** The tracked component. */
	private Importer importer;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean isAttached;

	/** The windows menu. */
	private JMenu windowMenu;
	
	/** Creates a new instance. */
	private ImporterFactory()
	{
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
	}

	/**
	 * Creates or recycles a importer component for the specified 
	 * <code>model</code>.
	 * 
	 * @param model	The Model.
	 * @return A {@link Importer}.
	 */
	private Importer getImporter(ImporterModel model)
	{
		if (importer != null) {
			((ImporterComponent) importer).resetGroup(model.getGroupId());
			return importer;
		}
		ImporterComponent comp = new ImporterComponent(model);
		model.initialize(comp);
		comp.initialize();
		importer = comp;
		return importer;
	}
	
	/** Clears the tracked component. */
	private void clear()
	{
		if (importer == null) return;
		importer.removeChangeListener(this);
		importer.discard();
		importer = null;
		handleViewerDiscarded();
	}
	
	/**
	 * Checks the list of opened viewers before removing the entry from the
	 * menu.
	 */
	private void handleViewerDiscarded()
	{
		if (!singleton.isAttached) return;
		if (singleton.importer != null) return;
		TaskBar tb = ImporterAgent.getRegistry().getTaskBar();
		tb.removeFromMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = false;
	}
	
	/**
	 * Sets the {@link #viewer} to <code>null</code> when it is
	 * {@link Importer#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */ 
	public void stateChanged(ChangeEvent ce)
	{
		ImporterComponent comp = (ImporterComponent) ce.getSource();
		if (comp.getState() == Importer.DISCARDED) {
			importer = null;
			handleViewerDiscarded();
		}
	}
	
}
