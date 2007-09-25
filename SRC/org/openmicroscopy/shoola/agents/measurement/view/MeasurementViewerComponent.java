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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;

//Application-internal dependencies
import ome.model.core.Pixels;
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.FileMap;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
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
            	model.fireChannelMetadataLoading();
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
     * @see MeasurementViewer#setDataChanged()
     */
	public void setDataChanged() { model.setDataChanged(); }
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#cancel()
     */
	public void cancel()
	{
		model.cancel();
		view.setStatus("");
		fireStateChange();
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#getState()
     */
	public int getState() { return model.getState(); }

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
			Logger log = reg.getLogger();
			if (e instanceof ParsingException) {
				log.error(this, "Cannot parse the ROI for "+model.getImageID());
			} else {
				
			}
			try {
				if (input != null) input.close();
			} catch (Exception io) {
				log.warn(this, "Cannot close the stream "+io.getMessage());
			}
			
			return;
		}

		view.rebuildManagerTable();
		view.updateDrawingArea();
		fireStateChange();
		//Now we are ready to go. We can post an event to add component to
		//Viewer
		postEvent(MeasurementToolLoaded.ADD);
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
				view.handleROIException(e, MeasurementViewerUI.RETRIEVE_MSG);
			}
			view.setStatus(MeasurementViewerUI.DEFAULT_MSG);
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
		//Sets the dimension of the drawing canvas;
		double f = model.getMagnification();
		Dimension d = new Dimension((int) (model.getSizeX()*f), 
							(int) (model.getSizeY()*f));
		UIUtilities.setDefaultSize(model.getDrawingView(), d);
		model.getDrawingView().setSize(d);
		model.fireROILoading(null);
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
		if (!model.isDataSaved())
		{ 
			JOptionPane pane = new JOptionPane(
				"Exit without saving changes to your ROI?");
			Object[] options = new String[] { "Yes", "No" };
			pane.setOptions(options);
			JDialog dialog = pane.createDialog(new JFrame(), "Dialog");
			dialog.setVisible(true);
			Object obj = pane.getValue(); 
			int result = -1;
			for (int k = 0; k < options.length; k++)
				if (options[k].equals(obj))
					result = k;
			if(result == 1)
				return;
	    }
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
		try
		{
			String savedFileString=FileMap.getSavedFile(model.getPixelsID());
			File savedFile = new File(savedFileString);
			chooser.setCurrentDirectory(savedFile);
			chooser.setSelectedFile(savedFile);
		}
		catch (ParsingException e)
		{
			// Do nothing as we're really only looking to see if the default 
			// directory or filename should be set for loading.
		}
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
	
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new XMLFilter();
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		File f = UIUtilities.getDefaultFolder();
	    if (f != null) chooser.setCurrentDirectory(f);
		try
		{
			String savedFileString=FileMap.getSavedFile(model.getPixelsID());
			File savedFile = new File(savedFileString);
			chooser.setCurrentDirectory(savedFile);
			chooser.setSelectedFile(savedFile);
		}
		catch (ParsingException e)
		{
			// Do nothing as we're really only looking to see if the default 
			// directory or filename should be set for loading.
		}
		int results = chooser.showSaveDialog(view.getParent());
		if (results != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		if (!file.getAbsolutePath().endsWith(XMLFilter.XML))
		{
			String fileName = file.getAbsolutePath()+"."+XMLFilter.XML;
			file = new File(fileName);
		}
		if (file.exists()) 
		{
			int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file?","Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
	        if (response == JOptionPane.CANCEL_OPTION) return;
	    }

		try {
			model.saveROI(file.getAbsolutePath());
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
		if (saved)
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
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (view.inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view");
			return;
		}
		
		Collection<ROI> roiList = model.getSelectedROI();
		if (roiList.size() == 0)
		{
			un.notifyInfo("ROI Assistant", "Select a Figure to modify " +
			"using the ROI Assistant.");
			return;
		}
		if (roiList.size() > 1)
		{
			un.notifyInfo("ROI Assistant", "The ROI Assistant can" +
					"only be used on one ROI" +
			"at a time.");
			return;
		}
		ROI currentROI = roiList.iterator().next();
			
    	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), currentROI, view);
    	UIUtilities.setLocationRelativeToAndShow(view, assistant);
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showMeasurementsInMicrons(boolean)
	 */
	public void showMeasurementsInMicrons(boolean inMicrons)
	{
		model.showMeasurementsInMicrons(inMicrons);
		view.updateDrawingArea();
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#setActiveChannels(Map)
	 */
	public void setActiveChannels(Map activeChannels)
	{
		int state = model.getState();
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_DATA:
				throw new IllegalStateException("This method cannot be " +
						"invoked in the DISCARDED, LOADING_DATA " +
						"state: "+state);
		}
		model.setActiveChannels(activeChannels);
		if (view.inDataView()){
			Collection<ROIFigure> collection = getSelectedFigures();
			if (collection.size() != 1) return;
			ROIFigure figure = collection.iterator().next();
			analyseShape(figure.getROIShape());
		}
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#setActiveChannelsColor(Map)
	 */
	public void setActiveChannelsColor(Map channels)
	{
		int state = model.getState();
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_DATA:
				throw new IllegalStateException("This method cannot be " +
						"invoked in the DISCARDED, LOADING_DATA " +
						"state: "+state);
		}
		model.setActiveChannels(channels);
		if (view.inDataView()) {
			Collection<ROIFigure> collection = getSelectedFigures();
			if (collection.size() != 1) return;
			ROIFigure figure = collection.iterator().next();
			analyseShape(figure.getROIShape());
		}
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#setStatsShapes(Map)
	 */
	public void setStatsShapes(Map result)
	{
		int state = model.getState();
		if (state != ANALYSE_SHAPE) {
			MeasurementAgent.getRegistry().getLogger().debug(this, 
					"This method can only be invoked " +
					"in the ANALYSE_SHAPE state: "+state);
			return;
		}
			//throw new IllegalStateException("This method can only be invoked " +
			//		"in the ANALYSE_SHAPE state: "+state);
		if (result == null || result.size() == 0) {
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Sets stats results", "No result to display.");
			return;
		}
		model.setAnalysisResults(result);
		view.displayAnalysisResults();
		fireStateChange();
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#analyseShape(ROIShape)
	 */
	public void analyseShape(ROIShape shape)
	{
		if (shape == null)
			throw new IllegalArgumentException("No shape specified.");
		int state = model.getState();
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_DATA:
			case LOADING_ROI:
				throw new IllegalStateException("This method cannot be " +
						"invoked in the DISCARDED, LOADING_DATA or " +
						"LOADING_ROI state: "+state);
				
			case ANALYSE_SHAPE:
				model.cancel();
				break;
		}
		if (model.getActiveChannels().size() == 0) return;
		model.fireAnalyzeShape(shape);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#setChannelMetadata(List)
	 */
	public void setChannelMetadata(List list)
	{
		if (model.getState() != LOADING_DATA) return;
		model.setChannelMetadata(list);
		model.firePixelsLoading();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#getSelectedFigures()
	 */
	public Collection getSelectedFigures()
	{
		return model.getSelectedFigures();
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#attachListeners(List)
	 */
	public void attachListeners(List<ROI> roiList)
	{
		ROI roi;
		Iterator<ROIShape> shapeIterator;
		ROIShape shape;
		for (int i = 0; i < roiList.size(); i++)
		{
			roi = roiList.get(i);
			shapeIterator = roi.getShapes().values().iterator();
			while (shapeIterator.hasNext())
			{
				shape = shapeIterator.next();
				shape.getFigure().addFigureListener(controller);
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#createSingleFigure(List)
	 */
	public void createSingleFigure(boolean createSingleFig)
	{
		view.createSingleFigure(createSingleFig);
	}
	
}
