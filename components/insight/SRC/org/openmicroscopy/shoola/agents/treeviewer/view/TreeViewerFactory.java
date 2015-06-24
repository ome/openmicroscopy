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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.data.events.SaveEventResponse;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.util.StringComparator;

import pojos.DataObject;
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

	/** Keeps track of viewers used to view archived images. */
	static final String	IMAGE_ARCHIVED = "imageNotArchived";//"imageArchived";
	
	/** Keeps track of viewers used to view images. */
	static final String	IMAGE_NOT_ARCHIVED = "imageNotArchived";
	
	/** The name of the file. */
	private static final String FILE_NAME = "externalApplication.txt";
	
	/** The name of the windows menu. */
	private static final String MENU_NAME = "Data Manager";
	
	/** The terms used to separate the file ID from the external application. */
	private static final String SEPARATOR = "=";
	
	/** The sole instance. */
	private static final TreeViewerFactory  singleton = new TreeViewerFactory();

	/**
	 * Registers the application.
	 * 
	 * @param data The application to register.
	 * @param mimeType The mimeType of the file.
	 */
	static void register(ApplicationData data, String mimeType)
	{
		if (mimeType == null) return;
		List<ApplicationData> list = singleton.applications.get(mimeType);
		if (list == null) {
			list = new ArrayList<ApplicationData>();
			list.add(data);
			singleton.applications.put(mimeType, list);
		} else { //Add the applications if needed.
			String path = data.getApplicationPath();
			Iterator<ApplicationData> i = list.iterator();
			ApplicationData app;
			boolean registered = false;
			while (i.hasNext()) {
				app = i.next();
				if (app.getApplicationPath().equals(path)) {
					registered = true;
					break;
				}
			}
			if (!registered) {
				list.add(data);
				Collections.sort(list, singleton.comparator);
			}
		}
	}

	/**
	 * Returns the collection of external applications used to 
	 * open the document.
	 * 
	 * @param type The MIME type of the document.
	 * @return See above.
	 */
	static List<ApplicationData> getApplications(String type)
	{
		return singleton.applications.get(type);
	}
	
	//static 
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
		tb.addToMenu(TaskBar.WINDOW_MENU, getWindowMenu());
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
	 * @param exp The experiment the TreeViewer is for.
	 * @return See above.
	 */
	public static TreeViewer getTreeViewer(ExperimenterData exp)
	{
		TreeViewerModel model = new TreeViewerModel(exp);
		return singleton.getTreeViewer(model, null);
	}

	/**
	 * Stores the image to copy the rendering settings from.
	 * 
	 * @param image The image to copy the rendering settings from.
	 * @param settings Copied 'pending' rendering settings
	 */
	public static void copyRndSettings(ImageData image, RndProxyDef settings)
	{
		Iterator v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.setRndSettings(image, settings);
		}
	}

	/**
	 * Notifies that the rendering settings have been copied.
	 * 
	 * @param imageIds The collection of updated images
	 */
	public static void onRndSettingsCopied(Collection<Long> imageIds)
	{
		Iterator<TreeViewer> v = singleton.viewers.iterator();
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
		Iterator<TreeViewer> v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.onGroupSwitched(success);
		}
	}
	
	/**
	 * Notifies the model that the user is reconnected.
	 * 
	 * @return Returns <code>true</code> if some viewers are already stored, 
	 *         <code>false</code> otherwise.
	 */
	public static boolean onReconnected()
	{
		Iterator<TreeViewer> v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.onReconnected();
		}
		return singleton.viewers.size() > 0;
	}
	
	/**
	 * Notifies the model that the user has annotated data.
	 * 
	 * @param containers The objects to handle.
	 * @param count A positive value if annotations are added, a negative value
	 * if annotations are removed.
	 */
	public static void onAnnotated(List<DataObject> containers, int count)
	{
		Iterator v = singleton.viewers.iterator();
		TreeViewerComponent comp;
		while (v.hasNext()) {
			comp = (TreeViewerComponent) v.next();
			comp.onAnnotated(containers, count);
		}
	}
	
	/**
	 * Returns the {@link TreeViewer}.
	 * 
	 * @param exp The experiment the TreeViewer is for.
	 * @param bounds The bounds of the component invoking a new 
	 * {@link TreeViewer}. 
	 * @return See above.
	 */
	public static TreeViewer getTreeViewer(ExperimenterData exp, 
			Rectangle bounds)
	{
		TreeViewerModel model = new TreeViewerModel(exp);
		return singleton.getTreeViewer(model, bounds);
	}

	/** Close all the instances.*/
	public static void terminate()
	{
		singleton.shutDown();
	}
	
	/** Writes the external applications used to open document. */
	public static void writeExternalApplications()
	{
		if (singleton.applications == null || 
				singleton.applications.size() == 0) return;
		try {
			Environment env = (Environment) 
				TreeViewerAgent.getRegistry().lookup(LookupNames.ENV);
			String name = env.getOmeroHome()+File.separator+FILE_NAME;
			File f = new File(name);
			if (f.exists()) f.delete();
			BufferedWriter output = new BufferedWriter(new FileWriter(name));
			Entry entry;
			Iterator i = singleton.applications.entrySet().iterator();
			String format;
			List list;
			Iterator j;
			ApplicationData data;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				format = (String) entry.getKey();
				list = (List) entry.getValue();
				if (list != null) {
					j = list.iterator();
					while (j.hasNext()) {
						data = (ApplicationData) j.next();
						output.write(format+SEPARATOR+data.getApplicationPath());
						output.newLine();
					}
				}
			}
			if (output != null) output.close();
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
	        msg.print("An error occurred while writing the external " +
	        		"applications back to the file.");
	        msg.print(e);
			TreeViewerAgent.getRegistry().getLogger().error(
					TreeViewerFactory.class, msg);
		}
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

	/** The external applications used to open file or images. */
	private Map<String, List<ApplicationData>> applications;
	
	/** The comparator used to sort the applications. */
	private StringComparator comparator;
	
	/** Creates a new instance. */
	private TreeViewerFactory()
	{
		//viewer = null;
		applications = null;
		viewers = new HashSet<TreeViewer>();
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
		comparator = new StringComparator();
	}

	/** Shuts donw the components.*/
	private void shutDown()
	{
		Set<TreeViewer> viewers = singleton.viewers;
		Iterator<TreeViewer> i = viewers.iterator();
		TreeViewer viewer;
		while (i.hasNext()) {
			viewer = i.next();
			((TreeViewerComponent) viewer).shutDown();
		}
		viewers.clear();
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
			comp.setRecycled(true);
			return comp;
		}
		//if (viewer != null) return viewer;
		readExternalApplications();
		comp = new TreeViewerComponent(model);
		model.initialize(comp);
		comp.initialize(bounds);
		//viewer = component;
		comp.addChangeListener(this);
		viewers.add(comp);
		return comp;
	}

	/** Reads the file hosting the external applications. */
	private void readExternalApplications()
	{
		if (applications != null) return;
		applications = new HashMap<String, List<ApplicationData>>();
		
		Environment env = (Environment) 
		TreeViewerAgent.getRegistry().lookup(LookupNames.ENV);
		String name = FilenameUtils.concat(env.getOmeroHome(),FILE_NAME);
		
		File f = new File(name);
		if (!f.exists()) return;
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			try {
				String line = null;
				String mimeType;
				String[] values;
				int index;
				StringBuffer buffer;
				List<ApplicationData> list;
				while ((line = input.readLine()) != null) {
					if (line.contains(SEPARATOR)) {
						values = line.split(SEPARATOR);
						if (values.length >= 2) {
							mimeType = values[0];
							index = 1;
							buffer = new StringBuffer();
							for (int i = 1; i < values.length; i++) {
								buffer.append(values[i]);
								if (index != values.length-1)
									buffer.append(SEPARATOR);
								index++;
							}
							list = applications.get(mimeType);
							if (list == null) {
								list = new ArrayList<ApplicationData>();
								applications.put(mimeType, list);
							}
							File application = new File(buffer.toString());
							list.add(new ApplicationData(application));
						}
					}
				}
				
				//sort out the list.
				Iterator<String> k = applications.keySet().iterator();
				while (k.hasNext()) {
					list = applications.get(k.next());
					Collections.sort(list, singleton.comparator);
				}
			} finally {
				input.close();
			}
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
	        msg.print("An error occurred while reading the external " +
	        		"applications file.");
	        msg.print(e);
			TreeViewerAgent.getRegistry().getLogger().error(
					TreeViewerFactory.class, msg);
		}
		
		
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
