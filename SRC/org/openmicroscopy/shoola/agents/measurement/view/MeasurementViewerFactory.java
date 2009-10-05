/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.ActivationAction;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.ChannelData;
import pojos.PixelsData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
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
		//return singleton.viewers; 
		if (menu == null) return;
		Iterator<MeasurementViewer> i = singleton.viewers.iterator();
		menu.removeAll();
		while (i.hasNext()) 
			menu.add(new JMenuItem(new ActivationAction(i.next())));
		
		/*
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
		*/
	}
	
    /**
     * Returns a viewer to display the image corresponding to the specified id.
     * Recycles or creates a viewer.
     * 
     * @param pixels 			The pixels set the measurement tool is for.
     * @param imageID   		The id of the image.
     * @param name      		The name of the image.
     * @param bounds    		The bounds of the component invoking the 
     *                  		{@link MeasurementViewer}.
     * @param z					The selected z-section.
     * @param t					The selected timepoint.
     * @param magnification		The image's magnification factor.
     * @param activeChannels	Collection of active channels.
     * @param channelsData		The channels metadata.
     * @return See above.
     */
	public static MeasurementViewer getViewer(PixelsData pixels, long imageID, 
										String name, Rectangle bounds, 
										int z, int t, double magnification,
										Map activeChannels, List<ChannelData>
										channelsData)
	{
		MeasurementViewerModel model = new MeasurementViewerModel(imageID, 
				pixels, name, bounds, channelsData);
		model.setPlane(z, t);
		model.setMagnification(magnification);
		model.setActiveChannels(activeChannels);
		return singleton.createROIViewer(model);
	}
	
	/**
	 * Returns a viewer or <code>null</code> if not previously created.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 */
	public static MeasurementViewer getViewer(long pixelsID)
	{
		Iterator v = singleton.viewers.iterator();
        MeasurementViewerComponent comp;
        while (v.hasNext()) {
            comp = (MeasurementViewerComponent) v.next();
            if (comp.getModel().getPixelsID() == pixelsID) return comp;
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
		Iterator v = singleton.requests.iterator();
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
	
	/** All the tracked components. */
    private Set<MeasurementViewer>	viewers;
    
    /** All the tracked requests. */
    private Set<MeasurementTool>	requests;
    
    /** The windows menu. */
	private JMenu   				windowMenu;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean 				isAttached;

    /** Creates a new instance. */
	private MeasurementViewerFactory()
	{
		viewers = new HashSet<MeasurementViewer>();
		requests = new HashSet<MeasurementTool>();
		isAttached = false;
		windowMenu = new JMenu(MENU_NAME);
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
		Iterator v = viewers.iterator();
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
     * Removes a viewer from the {@link #viewers} set when it is
     * {@link MeasurementViewer#DISCARDED discarded}. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		MeasurementViewerComponent comp = 
						(MeasurementViewerComponent) e.getSource(); 
		if (comp.getState() == MeasurementViewer.DISCARDED) 
			viewers.remove(comp);
	}

}
