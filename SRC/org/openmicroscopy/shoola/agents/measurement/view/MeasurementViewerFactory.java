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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;

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

	 /** The sole instance. */
    private static final MeasurementViewerFactory  
    					singleton = new MeasurementViewerFactory();
    
    /**
     * Returns a viewer to display the image corresponding to the specified id.
     * Recycles or creates a viewer.
     * 
     * @param pixelsID  	The id of the pixels set.
     * @param imageID   	The id of the image.
     * @param name      	The name of the image.
     * @param bounds    	The bounds of the component invoking the 
     *                  	{@link MeasurementViewer}.
     * @param z				The selected z-section.
     * @param t				The selected timepoint.
     * @param magnification	The image's magnification factor.
     * @return See above.
     */
	public static MeasurementViewer getViewer(long pixelsID, long imageID, 
										String name, Rectangle bounds, 
										int z, int t, double magnification)
	{
		MeasurementViewerModel model = new MeasurementViewerModel(imageID, 
											pixelsID, name, bounds);
		model.setPlane(z, t);
		model.setMagnification(magnification);
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
        while (v.hasNext()) {
        	request = (MeasurementTool) v.next();
            if (request.getPixelsID() == pixelsID) return request;
        }
		return null;
	}
	
	/** All the tracked components. */
    private Set<MeasurementViewer>     	viewers;
    
    /** All the tracked requests. */
    private Set<MeasurementTool>	requests;
    
    /** Creates a new instance. */
	private MeasurementViewerFactory()
	{
		viewers = new HashSet<MeasurementViewer>();
		requests = new HashSet<MeasurementTool>();
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
        //comp.addChangeListener(this);
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
		// TODO Auto-generated method stub
		
	}
	
}
