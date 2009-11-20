/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerFactory
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ActivateRecentAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import pojos.DataObject;
import pojos.ImageData;

/** 
* Factory to create {@link ImViewer} components.
* This class keeps track of all {@link ImViewer} instances that have been
* created and are not yet {@link ImViewer#DISCARDED discarded}. A new
* component is only created if none of the <i>tracked</i> ones is already
* displaying the given hierarchy. Otherwise, the existing component is
* recycled.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
public class ImViewerFactory
  	implements ChangeListener, PropertyChangeListener
{

	/** 
	 * The name of the property, temporary solution before we have preferences.
	 */
	private static final String	OMERO_VIEWER_COMPRESSION = 
		"omeroViewerCompression";
	
	/** The name associated to the component. */
	private static final String NAME = "Viewer: ";
	
	/** The name of the windows menu. */
	private static final String MENU_NAME = "Image Viewer";
	
	/** The name of the recent menu. */
	private static final String RECENT_MENU = "Open Recent";
	
	/** The name of the recent menu. */
	private static final String CLEAR_MENU = "Clear menu";
	
	/** The maximum number of recent items. */
	private static final int	MAX_RECENT = 10;
	
	/** The sole instance. */
	private static final ImViewerFactory  singleton = new ImViewerFactory();

	/** 
	 * Adds all the {@link ImViewer} components that this factory is
	 * currently tracking to the passed menu.
	 * 
	 * @param menu The menu to add the components to. 
	 */
	static void register(JMenu menu)
	{ 
		//return singleton.viewers; 
		if (menu == null) return;
		Iterator<ImViewer> i = singleton.viewers.iterator();
		menu.removeAll();
		while (i.hasNext()) 
			menu.add(new JMenuItem(new ActivationAction(i.next())));
		
		int n = singleton.recentViewers.size();
		if (n > 0) {
			Iterator<ImViewerRecentObject> 
				j = singleton.recentViewers.iterator();
			singleton.recentMenu.removeAll();
			while (j.hasNext()) {
				singleton.recentMenu.add(new JMenuItem(
						new ActivateRecentAction(j.next())));
			}
			singleton.recentMenu.add(new JSeparator());
			singleton.recentMenu.add(singleton.clearMenu);
			menu.add(singleton.recentMenu);
		}
	}

	/** 
	 * Returns the <code>window</code> menu. 
	 * 
	 * @return See above.
	 */
	static JMenu getWindowMenu() { return singleton.windowMenu; }

	/** Attaches the {@link #windowMenu} to the <code>TaskBar</code>. */
	static void attachWindowMenuToTaskBar()
	{
		if (singleton.isAttached) return;
		TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}

	/**
	 * Returns a viewer to display the image corresponding to the specified id.
	 * 
	 * @param image  	The image or wellSample to view.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise.           
	 * @return See above.
	 */
	public static ImViewer getImageViewer(DataObject image, Rectangle bounds, 
			boolean separateWindow)
	{
		ImViewerModel model = new ImViewerModel(image, bounds, separateWindow);
		return singleton.getViewer(model);
	}

	/**
	 * Returns a viewer to display the image corresponding to the specified id.
	 * 
	 * @param imageID  	The image to view.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise. 
	 * @return See above.
	 */
	public static ImViewer getImageViewer(long imageID, Rectangle bounds, 
			boolean separateWindow)
	{
		ImViewerModel model = new ImViewerModel(imageID, bounds, 
				separateWindow);
		return singleton.getViewer(model);
	}
	
	/**
	 * Returns the viewer if any, identified by the passed pixels ID.
	 * 
	 * @param pixelsID The Id of the pixels set.
	 * @return See above.
	 */
	public static ImViewer getImageViewer(long pixelsID)
	{
		Iterator v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getPixelsID() == pixelsID)  return comp;
		}
		return null;
	}

	/**
	 * Copies the rendering settings.
	 * 
	 * @param image	The image to copy the rendering settings from.
	 */
	public static void copyRndSettings(ImageData image)
	{
		singleton.refImage = image;
		Iterator v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getImageID() != image.getId()) 
				comp.copyRndSettings();
		}
	}

	/**
	 * Stores the passed event in the correct viewer.
	 * 
	 * @param evt The event to store.
	 */
	public static void storeEvent(SaveRelatedData evt)
	{
		Iterator v = singleton.viewers.iterator();
		ImViewerComponent comp;
		long pixelsID = evt.getPixelsID();
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getPixelsID() == pixelsID) 
				comp.storeEvent(evt);
		}
	}

	/**
	 * Returns map containing the event to post if selected.
	 * 
	 * @return See above.
	 */
	public static Map<String, Set> hasDataToSave()
	{
		Set<SaveEventRequest> events;
		Iterator i = singleton.viewers.iterator();
		ImViewerComponent comp;
		SaveData event;
		Map<String, SaveRelatedData> saveEvents;
		Iterator j;
		SaveRelatedData value;
		Map<String, Set> m =  new HashMap<String, Set>();
		while (i.hasNext()) {
			events = new HashSet<SaveEventRequest>();
			comp = (ImViewerComponent) i.next();
			if (comp.hasRndToSave()){
				event = new SaveData(comp.getPixelsID(), 
									SaveData.VIEWER_RND_SETTINGS);
				event.setMessage(ImViewerComponent.RND);
				events.add(new SaveEventRequest(comp, event));
			}
			if (comp.hasAnnotationToSave()){
				event = new SaveData(comp.getPixelsID(), 
									SaveData.VIEWER_ANNOTATION);
				event.setMessage(ImViewerComponent.RND);
				events.add(new SaveEventRequest(comp, event));
			}
			saveEvents = comp.getSaveEvents();
			if (saveEvents != null) {
				j = saveEvents.keySet().iterator();
				while (j.hasNext()) {
					value = saveEvents.get(j.next());
					if (value.isToSave()) {
						event = value.getSaveEvent();
						event.setMessage(value.toString());
						events.add(new SaveEventRequest(comp, event));
					}
				}
			}
			if (events.size() != 0)
				m.put(NAME+comp.getTitle(), events);
		}
		return m;
	}
	
	/**
	 * Resets the rendering engine if necessary.
	 * 
	 * @param pixelsIDs The collection of pixels set whose rendering settings 
	 * 					have been updated.
	 * @param refID		The if of the pixels of reference.
	 */
	public static void reloadRenderingEngine(Collection pixelsIDs, long refID)
	{
		if (pixelsIDs == null || pixelsIDs.size() == 0) return;
		Iterator i = singleton.viewers.iterator();
		ImViewerComponent comp;
		long id;
		while (i.hasNext()) {
			comp = (ImViewerComponent) i.next();
			id = comp.getPixelsID();
			if (pixelsIDs.contains(id) && id != refID)
				comp.reset();
		}
	}
	
	/** 
	 * Returns the id of the pixels set to copy the rendering settings.
	 * 
	 * @return See above.
	 */
	static ImageData getRefImage() { return singleton.refImage; }
	
	/**
	 * Returns the user preferences.
	 * 
	 * @return See above.
	 */
	static ViewerPreferences getPreferences() { return singleton.pref; }

	/**
	 * Sets the preferences.
	 * 
	 * @param pref The value to set.
	 */
	static void setPreferences(ViewerPreferences pref)
	{
		singleton.pref = null;//pref;
	}
	
	/**
	 * Sets the compression level.
	 * 
	 * @param level The value to set.
	 */
	static void setCompressionLevel(int level)
	{
		Preferences p = Preferences.userNodeForPackage(ImViewerFactory.class);
		p.put(OMERO_VIEWER_COMPRESSION, ""+level);
	}
	
	/**
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	static int getCompressionLevel()
	{
		Preferences p = Preferences.userNodeForPackage(ImViewerFactory.class);
		String value = p.get(OMERO_VIEWER_COMPRESSION, null);
		if (value != null && value.trim().length() > 0) 
			return Integer.parseInt(value);
		return -1;
	}
	
	/** All the tracked components. */
	private Set<ImViewer>     				viewers;

	/** Collection of image recently viewed. */
	private List<ImViewerRecentObject>     	recentViewers;
	
	/** The windows menu. */
	private JMenu   						windowMenu;

	/** The recent windows menu. */
	private JMenu   						recentMenu;
	
	/** The clear menu items. */
	private JMenuItem   					clearMenu;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean 						isAttached;

	/** The image data to copy the rendering settings from. */
	private ImageData						refImage;

	/** The user preferences for the viewer. */
	private ViewerPreferences				pref;
	
	/** Creates a new instance. */
	private ImViewerFactory()
	{
		viewers = new HashSet<ImViewer>();
		recentViewers = new ArrayList<ImViewerRecentObject>();
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
		recentMenu = new JMenu(RECENT_MENU);
		clearMenu = new JMenuItem(CLEAR_MENU);
		clearMenu.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				
				singleton.recentViewers.clear();
			}
		});
	}

	/**
	 * Creates or recycles a viewer component for the specified 
	 * <code>model</code>.
	 * 
	 * @param model The component's Model.
	 * @return A {@link ImViewer} for the specified <code>model</code>.  
	 */
	private ImViewer getViewer(ImViewerModel model)
	{
		Iterator v = viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getPixelsID() == model.getPixelsID())  
				return comp;
			//if (model.isSameDisplay(comp.getModel())) return comp;
		}
		comp = new ImViewerComponent(model);
		comp.initialize();
		comp.addChangeListener(this);
		comp.addPropertyChangeListener(this);
		viewers.add(comp);
		//
		long id = model.getImageID();
		if (id < 0) return null;
		Iterator<ImViewerRecentObject> j = recentViewers.iterator();
		ImViewerRecentObject obj;
		ImViewerRecentObject toRemove = null;
		while (j.hasNext()) {
			obj = j.next();
			if (obj.getImageID() == id) toRemove = obj;
		}
		if (toRemove != null) recentViewers.remove(toRemove);
		return comp;
	}
	
	/**
	 * Removes a viewer from the {@link #viewers} set when it is
	 * {@link ImViewer#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent ce)
	{
		ImViewerComponent comp = (ImViewerComponent) ce.getSource(); 
		if (comp.getState() == ImViewer.DISCARDED) {
			viewers.remove(comp);
		}
		/*
		if (viewers.size() == 0) {
			TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
			tb.removeFromMenu(TaskBar.WINDOW_MENU, windowMenu);
			isAttached = false;
		}*/
	}

	/**
	 * Listens to the {@link ImViewer#RECENT_VIEWER_PROPERTY}
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ImViewer.RECENT_VIEWER_PROPERTY.equals(name)) {
			Iterator<ImViewerRecentObject> i = recentViewers.iterator();
			ImViewerRecentObject v = (ImViewerRecentObject) evt.getNewValue();
			ImViewerRecentObject old;
			ImViewerRecentObject exist = null;
			while (i.hasNext()) {
				old = i.next();
				if (old.getImageID() == v.getImageID())
					exist = old;
			}
			if (exist != null) recentViewers.remove(exist);
			if (recentViewers.size() >= MAX_RECENT)
				recentViewers.remove(0);
			recentViewers.add(v);
		}
	}
	
}
