/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerComponent 
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
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;

/** 
 * Implements the {@link MeasurementViewer} interface to provide the 
 * functionality required of the Measurement viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerModel
 * @see org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerUI
 * @see org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerControl
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
class MeasurementViewerComponent 
	extends AbstractComponent
	implements MeasurementViewer
{

	/** The Model sub-component. */
    private MeasurementViewerModel 		model;
	
    /** The Control sub-component. */
    private MeasurementViewerControl	controller;
    
    /** The View sub-component. */
    private MeasurementViewerUI          view;
    
    /**
     * Posts an event to indicating to add or remove the component 
     * from the display.
     * 
     * @param index Either {@link MeasurementToolLoaded#ADD} or
     * 				{@link MeasurementToolLoaded#REMOVE}.
     */
    private void postEvent(int index)
    {
    	MeasurementToolLoaded response = 
			new MeasurementToolLoaded(
					MeasurementViewerFactory.getRequest(model.getPixelsID()), 
					model.getDrawingView(), index);
		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
		bus.post(response);
    }
    
    
	
	/**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component. Mustn't be <code>null</code>.
     */
	MeasurementViewerComponent(MeasurementViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new MeasurementViewerControl();
        view = new MeasurementViewerUI(model.getImageName());
	}
	
	/** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(controller, model);
    }
    
    /**
     * Returns the Model sub-component.
     * 
     * @return See above.
     */
    MeasurementViewerModel getModel() { return model; }

    /** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#activate()
     */
	public void activate()
	{
		int state = model.getState();
        switch (state) {
            case NEW:
                model.firePixelsLoading();
                fireStateChange();
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            default:
            	if (!view.isVisible()) postEvent(MeasurementToolLoaded.ADD);
                view.deIconify();
                view.setVisible(true);
        }
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#getUI()
     */
	public JFrame getUI() { return view; }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#activate()
     */
	public void discard()
	{
		// TODO Auto-generated method stub
		
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#cancel()
     */
	public void cancel()
	{

	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#getState()
     */
	public int getState() { return model.getState(); }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setPixelsDimensions(PixelsDimensions)
     */
	public void setPixelsDimensions(PixelsDimensions dims)
	{
		if (model.getState() == LOADING_DATA) {
			model.setPixelsDimensions(dims);
			//Sets the dimension of the drawing canvas;
			double f = model.getMagnification();
			Dimension d = new Dimension((int) (model.getSizeX()*f), 
								(int) (model.getSizeY()*f));
			UIUtilities.setDefaultSize(model.getDrawingView(), d);
			model.getDrawingView().setSize(d);
			model.fireROILoading(null);
			fireStateChange();
		}
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setROI(InputStream)
     */
	public void setROI(InputStream input)
	{
		if (model.getState() != LOADING_ROI) return;
		try {
			model.setROI(input);
		} catch (Exception e) {
			
			//TODO register and notify user. close Input
			Registry reg = MeasurementAgent.getRegistry();
			if (e instanceof ParsingException) {
				reg.getLogger().error(this, "Cannot parse the ROI for " 
						+model.getImageID());
				
			} else {
				
			}
			
				//TODO: notify 
			return;
		}

		view.rebuildManagerTable();
		view.updateDrawingArea();
		fireStateChange();
		//Now we are ready to go. We can post an event to add component to
		//Viewer
		//TODO: Review that code
		if (!model.getToolSent())
		{
			model.setToolSent(true);
			postEvent(MeasurementToolLoaded.ADD);
		}
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setMagnifiedPlane(int, int, double)
     */
	public void setMagnifiedPlane(int defaultZ, int defaultT, 
				double magnification)
	{
		int z = model.getDefaultZ();
		int t = model.getDefaultT();
		double f = model.getMagnification();
		if (z == defaultZ && t == defaultT) {
			if (f != magnification) model.setMagnification(magnification);
		} else {
			model.setPlane(defaultZ, defaultT);
			Drawing drawing = model.getDrawing();
			drawing.removeDrawingListener(controller);
			drawing.clear();
			ShapeList list = null;
			try {
				list = model.getShapeList();
			} catch (Exception e) {
				view.handleROIException(e);
			}
			if (list != null) {
				TreeMap map = list.getList();
				Iterator i = map.values().iterator();
				ROIShape shape;
				while (i.hasNext()) {
					shape = (ROIShape) i.next();
					if (shape != null) drawing.add(shape.getFigure());
				}
			}
			
			model.getDrawingView().setDrawing(drawing);
			drawing.addDrawingListener(controller);
			if (f != magnification) model.setMagnification(magnification);
		}
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setPixels(Pixels)
     */
	public void setPixels(Pixels pixels)
	{
		if (model.getState() != LOADING_DATA) return;
		model.setPixels(pixels);
		model.firePixelsDimensionsLoading();
		fireStateChange();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#close(boolean)
     */
	public void close(boolean post)
	{
		if (model.getState() == DISCARDED) 
			throw new IllegalStateException("This method shouldn't be " +
					"invoked in the DISCARDED state:"+model.getState());
		if (post) postEvent(MeasurementToolLoaded.REMOVE);
		view.setVisible(false);
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#iconified(boolean)
     */
	public void iconified(boolean b)
	{
		if (model.getState() == DISCARDED) 
			throw new IllegalStateException("This method shouldn't be " +
					"invoked in the DISCARDED state:"+model.getState());
		view.setVisible(b);
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#loadROI()
     */
	public void loadROI()
	{
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new XMLFilter();
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		File f = UIUtilities.getDefaultFolder();
	    if (f != null) chooser.setCurrentDirectory(f);
		int results = chooser.showOpenDialog(view.getParent());
		if (results != JFileChooser.APPROVE_OPTION) return;
		model.fireROILoading(chooser.getSelectedFile().getAbsolutePath());
		fireStateChange();
		view.updateDrawingArea();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#saveROI()
     */
	public void saveROI() 
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		try {
			model.saveROI();
		} catch (ParsingException e) {
			reg.getLogger().error(this, "Cannot save the ROI "+e.getMessage());
			un.notifyInfo("Save ROI", "Cannot save ROI " +
										"for "+model.getImageID());
		}
		un.notifyInfo("Save ROI", "The Regions of Interests have been " +
									"successfully saved. ");
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#refreshResultsTable()
     */
	public void refreshResultsTable() 
	{
		view.refreshResultsTable();
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#rebuildManagerTable()
     */
	public void rebuildManagerTable() 
	{
		view.rebuildManagerTable();
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#saveResultsTable()
	 */
	public void saveResultsTable() 
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		boolean saved = false;
		try {
			saved = view.saveResultsTable();
		} catch (Exception e) {
			reg.getLogger().error(this, 
					"Cannot save the results "+e.getMessage());
			un.notifyInfo("Save ROI results", "Cannot save the ROI results");
		}
		if(saved)
			un.notifyInfo("Save ROI results", "The ROI results have been " +
											"successfully saved.");
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#figureAttributeChanged(AttributeKey, ROIFigure)
	 */
	public void figureAttributeChanged(AttributeKey key, ROIFigure figure)
	{
		model.figureAttributeChanged(key, figure);
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showResultsWizard()
	 */
	public void showResultsWizard()
	{
		view.showResultsWizard();
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showROIAssistant()
	 */
	public void showROIAssistant()
	{
		view.showROIAssistant();
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showMeasurementsInMicrons(boolean)
	 * 
	 */
	public void showMeasurementsInMicrons(boolean inMicrons)
	{
		model.showMeasurementsInMicrons(inMicrons);
		view.updateDrawingArea();
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#createSingleFigure(boolean)
	 * 
	 */
	public void createSingleFigure(boolean option)
	{
		view.createSingleFigure(option);
	}
}
