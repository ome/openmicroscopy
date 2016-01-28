/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.ActivationAction;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import omero.gateway.model.ChannelData;
import omero.gateway.model.PixelsData;

/** 
 * Factory to create {@link MeasurementViewer} components.
 * This class keeps track of all {@link MeasurementViewer} instances that have 
 * been created and are not yet {@link MeasurementViewer#DISCARDED discarded}. 
 * A new component is only created if none of the <i>tracked</i> ones is already
 * displaying the given hierarchy. Otherwise, the existing component is
 * recycled.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MeasurementViewerFactory
	implements ChangeListener
{

	/** The name of the windows menu. */
	private static final String MENU_NAME = "ROI Tool";
	
	/** The sole instance. */
    private static final MeasurementViewerFactory  
    					singleton = new MeasurementViewerFactory();
    
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
		TaskBar tb = MeasurementAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}
	
	/** 
	 * Adds all the {@link MeasurementViewer} components that this factory is
	 * currently tracking to the passed menu.
	 * 
	 * @param menu The menu to add the components to. 
	 */
	static void register(JMenu menu)
	{ 
		if (menu == null) return;
		Iterator<MeasurementViewer> i = singleton.viewers.iterator();
		menu.removeAll();
		while (i.hasNext()) 
			menu.add(new JMenuItem(new ActivationAction(i.next())));
	}
	
    /**
     * Returns a viewer to display the image corresponding to the specified id.
     * Recycles or creates a viewer.
     * 
     * @param ctx The security context.
     * @param pixels The pixels set the measurement tool is for.
     * @param imageID The id of the image.
     * @param name The name of the image.
     * @param bounds The bounds of the component invoking the 
     *               {@link MeasurementViewer}.
     * @param z The selected z-section.
     * @param t The selected time-point.
     * @param magnification The image's magnification factor.
     * @param activeChannels Collection of active channels.
     * @param channelsData The channels metadata.
     * @return See above.
     */
	public static MeasurementViewer getViewer(SecurityContext ctx,
			PixelsData pixels, long imageID, String name, Rectangle bounds, 
			int z, int t, double magnification,
			Map activeChannels, List<ChannelData> channelsData)
	{
		MeasurementViewerModel model = new MeasurementViewerModel(ctx, imageID,
				pixels, name, bounds, channelsData);
		model.setPlane(z, t);
		model.setMagnification(magnification);
		model.setActiveChannels(activeChannels);
		return singleton.createROIViewer(model);
	}
	
	/**
	 * Returns a viewer or <code>null</code> if not previously created.
	 * 
	 *  @param ctx The security context.
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 */
	public static MeasurementViewer getViewer(SecurityContext ctx,
			long pixelsID)
	{
		Iterator<MeasurementViewer> v = singleton.viewers.iterator();
        MeasurementViewerComponent comp;
        while (v.hasNext()) {
            comp = (MeasurementViewerComponent) v.next();
            if (comp.getModel().getPixelsID() == pixelsID) return comp;
        }
		return null;
	}
	
	/**
	 * Returns a viewer or <code>null</code> if not previously created.
	 * 
	 * @param ctx The security context.
	 * @param imageID The id of the image.
	 * @return See above.
	 */
	public static MeasurementViewer getViewerFromImage(SecurityContext ctx,
			long imageID)
	{
		Iterator<MeasurementViewer> v = singleton.viewers.iterator();
        MeasurementViewerComponent comp;
        while (v.hasNext()) {
            comp = (MeasurementViewerComponent) v.next();
            if (comp.getModel().getImageID() == imageID) return comp;
        }
		return null;
	}
	
	
	/**
	 * Adds the passed request to the collection.
	 * 
	 * @param request 	The request to add.
	 */
	public static void addRequest(MeasurementTool request)
	{
		if (request == null) return;
		singleton.requests.add(request);
	}
	
	/**
	 * Returns the request, if any, identified by the pixels ID.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 */
	public static MeasurementTool getRequest(long pixelsID)
	{
		Iterator<MeasurementTool> v = singleton.requests.iterator();
		MeasurementTool request;
		PixelsData pixels;
        while (v.hasNext()) {
        	request = (MeasurementTool) v.next();
        	pixels = request.getPixels();
        	if (pixels != null) {
        		if (pixels.getId() == pixelsID) return request;
        	}
        }
		return null;
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
		Iterator<MeasurementViewer> i = singleton.viewers.iterator();
		MeasurementViewerComponent comp;
		while (i.hasNext()) {
			comp = (MeasurementViewerComponent) i.next();
			if (comp.hasROIToSave())
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
		if (instances != null) {
			Iterator<Object> i = instances.iterator();
			Object o;
			MeasurementViewerComponent comp;
			while (i.hasNext()) {
				o = i.next();
				if (o instanceof MeasurementViewerComponent) {
					comp = (MeasurementViewerComponent) o;
					comp.saveAndDiscard();
				}
			}
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
		if (!success) return;
		singleton.clear();
	}
	
	/**
	 * Notifies the model that the ROIs have been deleted 
	 * 
	 * @param imageID The identifier of the image.
	 */
	public static void onROIDeleted(long imageID)
	{
		if (singleton.viewers.size() == 0) return;
		Iterator<MeasurementViewer> i = singleton.viewers.iterator();
		MeasurementViewerComponent comp;
		while (i.hasNext()) {
			comp = (MeasurementViewerComponent) i.next();
			comp.onROIDeleted(imageID);
			if (comp.hasROIToSave())
				comp.saveROIToServer(false);
		}
	}

	/** All the tracked components. */
    private Set<MeasurementViewer> viewers;
    
    /** All the tracked requests. */
    private Set<MeasurementTool> requests;
    
    /** The windows menu. */
	private JMenu windowMenu;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean isAttached;

    /** Creates a new instance. */
	private MeasurementViewerFactory()
	{
		viewers = new HashSet<MeasurementViewer>();
		requests = new HashSet<MeasurementTool>();
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
	}
	
	/** Discards the tracked viewers. */
	private void clear()
	{
		if (viewers.size() == 0) return;
		Iterator<MeasurementViewer> i = viewers.iterator();
		MeasurementViewerComponent comp;
		
		while (i.hasNext()) {
			comp = (MeasurementViewerComponent) i.next();
			comp.removeChangeListener(this);
			comp.discard();
		}
		viewers.clear();
		requests.clear();
		handleViewerDiscarded();
	}
	
	/**
     * Creates or recycles a viewer component for the specified 
     * <code>model</code>.
     * 
     * @param model The component's Model.
     * @return A {@link MeasurementViewer} for the specified <code>model</code>.
     */
	private MeasurementViewer createROIViewer(MeasurementViewerModel model)
	{
		Iterator<MeasurementViewer> v = viewers.iterator();
        MeasurementViewerComponent comp;
        while (v.hasNext()) {
            comp = (MeasurementViewerComponent) v.next();
            if (model.isSameDisplay(comp.getModel())) return comp;
        }
        comp = new MeasurementViewerComponent(model);
        comp.initialize();
        comp.addChangeListener(this);
        viewers.add(comp);
        return comp;
	}

	/**
	 * Checks the list of opened viewers before removing the entry from the
	 * menu.
	 */
	private void handleViewerDiscarded()
	{
		if (!singleton.isAttached) return;
		if (singleton.viewers.size() != 0) return;
		TaskBar tb = MeasurementAgent.getRegistry().getTaskBar();
		tb.removeFromMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = false;
	}
	
	/**
	 * Removes a viewer from the {@link #viewers} set when it is
	 * {@link MeasurementViewer#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		MeasurementViewerComponent comp = 
						(MeasurementViewerComponent) e.getSource();
		if (comp.getState() == MeasurementViewer.DISCARDED) {
			viewers.remove(comp);
			handleViewerDiscarded();
		}
	}

}
