/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory
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

package org.openmicroscopy.shoola.agents.treeviewer.view;



//Java imports
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.data.events.SaveEventResponse;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Factory to create {@link TreeViewer} component.
 * This class keeps track of the {@link TreeViewer} instance that has been 
 * created and is not yet in the {@link TreeViewer#DISCARDED} state.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeViewerFactory
  	implements ChangeListener
{

	/** The name associated to the component. */
	private static final String NAME = "Data Manager";
	
	/** The name of the windows menu. */
	private static final String MENU_NAME = "Data Manager";
	
	/** The sole instance. */
	private static final TreeViewerFactory  singleton = new TreeViewerFactory();

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
		TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}

	/**
	 * Returns the collection of active viewers.
	 * 
	 * @return See above.
	 */
	static Set getViewers() { return singleton.viewers; }

	/**
	 * Returns <code>true</code> if there is only one {@link TreeViewer}
	 * available, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	static boolean isLastViewer() { return (singleton.viewers.size() <= 1); }

	/**
	 * Returns the {@link TreeViewer}.
	 * 
	 * @param exp	    	The experiment the TreeViewer is for.
	 * @param userGroupID 	The id to the group selected for the current user.
	 * @return See above.
	 */
	public static TreeViewer getTreeViewer(ExperimenterData exp, 
			long userGroupID)
	{
		TreeViewerModel model = new TreeViewerModel(exp, userGroupID);
		return singleton.getTreeViewer(model, null);
	}

	/**
	 * Stores the image to copy the rendering settings from.
	 * 
	 * @param image The image to copy the rendering settings from.
	 */
	public static void copyRndSettings(ImageData image)
	{
		Iterator v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.setRndSettings(image);
		}
	}

	/**
	 * Notifies that the rendering settings have been copied.
	 * 
	 * @param imageIds The collection of updated images
	 */
	public static void onRndSettingsCopied(Collection imageIds)
	{
		Iterator v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.onRndSettingsCopied(imageIds);
		}
	}
	
	/**
	 * Saves the data before closing the application.
	 * 
	 * @param evt
	 * @param agent
	 */
	public static void saveOnClose(SaveEventRequest evt, Object agent)
	{
		//if (!(evt instanceof SaveData)) return;
		Iterator v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		//tmp
		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.saveOnClose((SaveData) evt.getAnswer());
			bus.post(new SaveEventResponse(evt, (Agent) agent));
		}
	}
	
	/**
	 * Returns the {@link TreeViewer}.
	 * 
	 * @param exp	    	The experiment the TreeViewer is for.
	 * @param userGroupID 	The id to the group selected for the current user.
	 * @param bounds    	The bounds of the component invoking a new
	 * 						{@link TreeViewer}. 
	 * @return See above.
	 */
	public static TreeViewer getTreeViewer(ExperimenterData exp, 
			long userGroupID, Rectangle bounds)
	{
		TreeViewerModel model = new TreeViewerModel(exp, userGroupID);
		return singleton.getTreeViewer(model, bounds);
	}

	/**
	 * Returns map containing the event to post if selected.
	 * 
	 * @return See above.
	 */
	public static Map<String, Set> hasDataToSave()
	{
		Set<SaveEventRequest> events = new HashSet<SaveEventRequest>();
		Iterator i = singleton.viewers.iterator();
		TreeViewerComponent comp;
		SaveData event;
		while (i.hasNext()) {
			comp = (TreeViewerComponent) i.next();
			if (comp.hasDataToSave()) {
				event = new SaveData(SaveData.DATA_MANAGER_ANNOTATION);
				event.setMessage("Edited data");
				events.add(new SaveEventRequest(comp, event));
			}
		}
		if (events.size() != 0) {
			Map<String, Set> m =  new HashMap<String, Set>();
			m.put(NAME, events);
			return m;
		}
		return null;
	}
	
	/** The tracked component. */
	//private TreeViewer  	viewer;

	/** The tracked components. */
	private Set<TreeViewer>	viewers;

	/** The windows menu. */
	private JMenu   		windowMenu;

	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean 		isAttached;

	/** Creates a new instance. */
	private TreeViewerFactory()
	{
		//viewer = null;
		viewers = new HashSet<TreeViewer>();
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
	}

	/**
	 * Creates or recycles a viewer component for the specified 
	 * <code>model</code>.
	 * 
	 * @param model 	The Model.
	 * @param bounds	The bounds of the component invoking a new
	 * 						{@link TreeViewer}. 
	 * @return A {@link TreeViewer}.
	 */
	private TreeViewer getTreeViewer(TreeViewerModel model, Rectangle bounds)
	{
		Iterator v = viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			if (model.isSameDisplay(comp.getModel())) {
				comp.setRecycled(true);
				return comp;
			}
		}
		//if (viewer != null) return viewer;
		comp = new TreeViewerComponent(model);
		model.initialize(comp);
		comp.initialize(bounds);
		//viewer = component;
		comp.addChangeListener(this);
		viewers.add(comp);
		return comp;
	}

	/**
	 * Sets the {@link #viewer} to <code>null</code> when it is
	 * {@link TreeViewer#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */ 
	public void stateChanged(ChangeEvent ce)
	{
		TreeViewerComponent comp = (TreeViewerComponent) ce.getSource();
		if (comp.getState() == TreeViewer.DISCARDED) viewers.remove(comp);
	}
  
}
