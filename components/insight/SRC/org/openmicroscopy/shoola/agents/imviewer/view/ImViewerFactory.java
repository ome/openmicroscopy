/*
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

package org.openmicroscopy.shoola.agents.imviewer.view;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ActivateRecentAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ActivationAction;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

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

	/** The name of the interpolation property */
	private static final String    OMERO_INTERPOLATION = 
	        "omeroViewerInterpolation";
	
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
	 * Notifies the model that the user's group has successfully be modified
	 * if the passed value is <code>true</code>, unsuccessfully 
	 * if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, <code>false</code>
	 * 					otherwise.
	 */
	public static void onGroupSwitched(boolean success)
	{
		if (!success) return;
		singleton.clear();
	}
	
	/**
	 * Returns a viewer to display the image corresponding to the specified id.
	 * 
	 * @param ctx The security context.
	 * @param image  	The image or wellSample to view.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise.
	 * @return See above.
	 */
	public static ImViewer getImageViewer(SecurityContext ctx,
			DataObject image, Rectangle bounds, 
			boolean separateWindow)
	{
		ImViewerModel model = new ImViewerModel(ctx, image, bounds,
				separateWindow);
		return singleton.getViewer(model);
	}

	/**
	 * Returns a viewer to display the image corresponding to the specified id.
	 *
	 * @param ctx The security context.
	 * @param imageID The image to view.
	 * @param bounds The bounds of the component invoking the {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a
	 *                       separate window, <code>false</code> otherwise.
	 * @return See above.
	 */
	public static ImViewer getImageViewer(SecurityContext ctx,
		long imageID, Rectangle bounds, boolean separateWindow)
	{
		ImViewerModel model = new ImViewerModel(ctx, imageID, bounds, 
				separateWindow);
		return singleton.getViewer(model);
	}
	
	/**
	 * Returns the viewer if any, identified by the passed pixels ID.
	 *
	 * @param ctx The security context.
	 * @param pixelsID The Identifier of the pixels set.
	 * @return See above.
	 */
	public static ImViewer getImageViewer(SecurityContext ctx, long pixelsID)
	{
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().isSame(pixelsID, ctx)) return comp;
		}
		return null;
	}

	/**
	 * Returns the viewer if any, identified by the passed image's ID.
	 *
	 * @param ctx The security context.
	 * @param imageID The Identifier of the image.
	 * @return See above.
	 */
	public static ImViewer getImageViewerFromImage(SecurityContext ctx,
			long imageID)
	{
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().isSameImage(imageID, ctx))
				return comp;
		}
		return null;
	}
	
	/**
	 * Returns the viewer if any, identified by the passed pixels ID.
	 * 
	 * @param parent The of the image.
	 * @return See above.
	 */
	public static ImViewer getImageViewerFromParent(DataObject parent)
	{
		if (parent == null) return null;
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().isSameParent(parent))  return comp;
		}
		return null;
	}
		
	/**
	 * Copies the rendering settings.
	 * 
	 * @param image The image to copy the rendering settings from.
	 * @param refRndDef 'Pending' rendering settings to copy (can be null)
	 */
	public static void copyRndSettings(ImageData image, RndProxyDef refRndDef)
	{
		singleton.refImage = image;
		singleton.refRndDef = refRndDef;
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (image != null && comp.getModel().getImageID() != image.getId()) 
				comp.copyRndSettings();
		}
	}
	
	/**
	 * Indicates that rendering settings has been saved using another way.
	 * 
	 * @param pixelsID The Identifier of the pixels set.
	 * @param settings The rendering settings saved.
	 */
	public static void rndSettingsSaved(long pixelsID, RndProxyDef settings)
	{
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getPixelsID() == pixelsID) 
				comp.onRndSettingsSaved(settings);
			
			comp.reloadRenderingThumbs();
		}
	}

	/**
	 * Indicates that rendering settings have been modified.
	 * 
	 * @param imageID The Identifier of the pixels set.
	 */
	public static void rndSettingsChanged(long imageID)
	{
	    Iterator<ImViewer> v = singleton.viewers.iterator();
	    ImViewerComponent comp;
	    while (v.hasNext()) {
	        comp = (ImViewerComponent) v.next();
	        if (comp.getModel().getImageID() == imageID) {
	            comp.onSettingsChanged();
	        }
	    }
	}

	/**
	 * Stores the passed event in the correct viewer.
	 * 
	 * @param evt The event to store.
	 */
	public static void storeEvent(SaveRelatedData evt)
	{
		Iterator<ImViewer> v = singleton.viewers.iterator();
		ImViewerComponent comp;
		long pixelsID = evt.getPixelsID();
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getPixelsID() == pixelsID) 
				comp.storeEvent(evt);
		}
	}

	/**
	 * Returns the instances to save.
	 * 
	 * @return See above.
	 */
	public static List<Object> getInstancesToSave()
	{
		if (singleton.viewers.size() == 0) return null;
		List<Object> instances = new ArrayList<Object>();
		Iterator<ImViewer> i = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (i.hasNext()) {
			comp = (ImViewerComponent) i.next();
			if (comp.hasSettingsToSave())
				instances.add(comp);
		}
		return instances;
	}
	
	/** 
	 * Saves the passed instances and discards them. 
	 * 
	 * @param instances The instances to save.
	 */
	public static void saveInstances(List<Object> instances)
	{
		//if (singleton.viewers.size() == 0) return;
		if (instances != null) {
			Iterator<Object> i = instances.iterator();
			ImViewerComponent comp;
			Object o;
			List<Long> ids = new ArrayList<Long>();
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof ImViewerComponent) {
					comp = (ImViewerComponent) o;
					comp.close(false);
					singleton.viewers.remove(comp);
					ids.add(comp.getModel().getImageID());
				}
			}
			removeRecentViewers(ids);
		}
	}
	
	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	public static void setDisplayMode(int displayMode)
	{
		Iterator<ImViewer> i = singleton.viewers.iterator();
		ImViewerComponent comp;
		while (i.hasNext()) {
			comp = (ImViewerComponent) i.next();
			comp.setDisplayMode(displayMode);
		}
	}
	
	/** 
	 * Returns the image to copy the rendering settings from.
	 * 
	 * @return See above.
	 */
	static ImageData getRefImage() { return singleton.refImage; }
	
	/** 
	 * Returns the copied 'pending' rendering settings.
	 * 
	 * @return See above.
	 */
	static RndProxyDef getRefSettings() {
	    return singleton.refRndDef;
	}
	
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
	
	/**
	 * Sets the interpolation user preference
	 */
	public static void setInterpolation(boolean interpolation) {
	    Preferences p = Preferences.userNodeForPackage(ImViewerFactory.class);
        p.put(OMERO_INTERPOLATION, ""+interpolation);
	}
	
	/**
	 * Returns the interpolation user preference or <code>null</code>
	 * if it hasn't been set.
	 * 
	 * @return See above.
	 */
	public static Boolean isInterpolation() {
	    Preferences p = Preferences.userNodeForPackage(ImViewerFactory.class);
        String value = p.get(OMERO_INTERPOLATION, null);
        if (CommonsLangUtils.isNotEmpty(value)) 
            return new Boolean(value);
        return null;
	}
	
	/**
	 * Removes the viewers from the list of viewers recently opened.
	 * 
	 * @param ids The identifiers of the viewers.
	 */
	private static void removeRecentViewers(List<Long> ids)
	{
		if (ids == null || ids.size() == 0) return;
		Iterator<ImViewerRecentObject> j = singleton.recentViewers.iterator();
		ImViewerRecentObject recent;
		List<ImViewerRecentObject> 
			toRemove = new ArrayList<ImViewerRecentObject>();
		while (j.hasNext()) {
			recent = j.next();	
			if (ids.contains(recent.getImageID()))
				toRemove.add(recent);
		}
		singleton.recentViewers.removeAll(toRemove);
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

        /** 'Pending' rendering settings to copy */
	private RndProxyDef                                             refRndDef;
	
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

	/** Clears the collection of tracked viewers. */
	private void clear()
	{
		Iterator<ImViewer> i = viewers.iterator();
		ImViewerComponent comp;
		while (i.hasNext()) {
			comp = (ImViewerComponent) i.next();
			comp.removeChangeListener(this);
			comp.discard();
		}
		singleton.viewers.clear();
		singleton.recentViewers.clear();
		handleViewerDiscarded();
	}
	
	/**
	 * Checks the list of opened viewers before removing the entry from the
	 * menu.
	 */
	private void handleViewerDiscarded()
	{
		if (!singleton.isAttached) return;
		if (singleton.viewers.size() != 0) return;
		TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
		tb.removeFromMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = false;
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
		Iterator<ImViewer> v = viewers.iterator();
		ImViewerComponent comp;
		while (v.hasNext()) {
			comp = (ImViewerComponent) v.next();
			if (comp.getModel().getImageID() == model.getImageID())
				return comp;
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
		switch (comp.getState()) {
			case ImViewer.DISCARDED:
			case ImViewer.CANCELLED:
				viewers.remove(comp);
				removeRecentViewers(
						Arrays.asList(comp.getModel().getImageID()));
				handleViewerDiscarded();
				break;
			default:
		}
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
				if (old.getImageID() == v.getImageID()) {
					exist = old;
					break;
				}
			}
			if (exist != null) recentViewers.remove(exist);
			if (recentViewers.size() >= MAX_RECENT)
				recentViewers.remove(0);
			recentViewers.add(v);
			handleViewerDiscarded();
		}
	}
	
}
