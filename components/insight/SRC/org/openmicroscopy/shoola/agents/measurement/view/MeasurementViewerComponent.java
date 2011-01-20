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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.FileMap;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import pojos.FileAnnotationData;

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
     * Creates a file chooser corresponding to the passed type.
     * 
     * @param type The type of the file chooser.
     * @return See above.
     */
	private FileChooser createChooserDialog(int type)
	{
		String word = "Save ";
		if (type == FileChooser.LOAD) word = "Load ";
		String title = word+"the ROI File";
		String text = word+"the ROI data in the file associate with the image.";
		
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new XMLFilter());
		FileChooser chooser = new FileChooser(view, type, title, text, filters,
					false, true);
		File f = UIUtilities.getDefaultFolder();
		if (f != null) chooser.setCurrentDirectory(f);
		try
		{
			String s = FileMap.getSavedFile(model.getServerName(), 
						model.getUserName(), model.getPixelsID());
			File savedFile;
			if (s != null) {
				savedFile = new File(s);
				chooser.setCurrentDirectory(savedFile);
				chooser.setSelectedFile(savedFile);
			} else {
				if (type == FileChooser.SAVE) {
					s = model.getImageName();
					savedFile = new File(s);
					chooser.setSelectedFile(savedFile.getName());
				}
			}
		} catch (ParsingException e) {
			// Do nothing as we're really only looking to see if the default
			// directory or filename should be set for loading.
		}
		
		return chooser;
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
        view = new MeasurementViewerUI(model.getImageTitle());
	}
	
	/** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(this, controller, model);
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
    public void activate() { activate(model.getMeasurements()); }
    
    /** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#activate(List)
     */
	public void activate(List<FileAnnotationData> measurements)
	{
		int state = model.getState();
        switch (state) {
            case NEW:
            	//Sets the dimension of the drawing canvas;
        		double f = model.getMagnification();
        		Dimension d = new Dimension((int) (model.getSizeX()*f), 
        							(int) (model.getSizeY()*f));
        		UIUtilities.setDefaultSize(model.getDrawingView(), d);
        		model.getDrawingView().setSize(d);
        		//Load ROI from server or not.
        		Boolean location = (Boolean) 
        			MeasurementAgent.getRegistry().lookup(
        					LookupNames.SERVER_ROI);
        		if (location) model.fireLoadROIFromServer(measurements);
        		else model.fireROILoading(null);
        		//model.fireROILoading(null);
        		//fireStateChange();
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
     * @see MeasurementViewer#discard()
     */
	public void discard()
	{
		if (model.getState() != DISCARDED) {
			model.discard();
			fireStateChange();
		}
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setDataChanged()
     */
	public void setDataChanged()
	{ 
		model.nofityDataChanged(true);
		firePropertyChange(ROI_CHANGED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#cancel()
     */
	public void cancel()
	{
		model.cancel();
		view.setReadyStatus();
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
		//if (model.getState() != LOADING_ROI || input == null) return;
		if (model.getState() != LOADING_ROI) return;
		if (input == null) {
			try {
				model.setROI(input);
			} catch (Exception e) {}
			
			view.rebuildManagerTable();
			view.updateDrawingArea();
			view.setReadyStatus();
			fireStateChange();
			//Now we are ready to go. We can post an event to add component to
			//Viewer
			postEvent(MeasurementToolLoaded.ADD);
			return;
		}
		Registry reg = MeasurementAgent.getRegistry();
		Logger log = reg.getLogger();
		try {
			boolean valid = model.setROI(input);
			if (!valid) {
				reg.getUserNotifier().notifyInfo("ROI", "The ROI are not " +
						"compatible with the image.");
				try {
					input.close();
				} catch (Exception io) {
					log.warn(this, "Cannot close the stream "+io.getMessage());
				}
				fireStateChange();
				return;
			}
		} catch (Exception e) {
			
			if (e instanceof ParsingException) {
				log.error(this, "Cannot parse the ROI for "+model.getImageID());
			} else {
				
			}
			try {
				input.close();
			} catch (Exception io) {
				log.warn(this, "Cannot close the stream "+io.getMessage());
			}
			return;
		}
		view.rebuildManagerTable();
		view.updateDrawingArea();
		view.setReadyStatus();
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
					if (shape != null)
					{
						drawing.add(shape.getFigure());
						shape.getFigure().addFigureListener(controller);
					}
				}
			}
			model.getDrawingView().setDrawing(drawing);
			drawing.addDrawingListener(controller);
			if (f != magnification) model.setMagnification(magnification);
		}
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#close()
     */
	public void close()
	{
		if (model.getState() == DISCARDED) {
			return;
		}
		/*
		if (!model.isDataSaved()) { 
			String title = "Discard Changes";
		    String message = "Do you want to exit and discard changes?";
		  
			MessageBox dialog = new MessageBox(view, title, message);
				
			if (dialog.showMsgBox() == MessageBox.NO_OPTION) return;
		}
		model.setDataDiscarded();
		*/
		//Post event indicating that we don't care about saving.
		postEvent(MeasurementToolLoaded.REMOVE);
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
		FileChooser chooser = createChooserDialog(FileChooser.LOAD);
		if (chooser.showDialog() != JFileChooser.APPROVE_OPTION) return;
		File f = chooser.getSelectedFile();
		if (f == null) return;
		model.fireROILoading(f.getAbsolutePath());
		fireStateChange();
		view.updateDrawingArea();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#saveROI()
     */
	public void saveROI()
	{
		FileChooser chooser = createChooserDialog(FileChooser.SAVE);
		if (chooser.showDialog() != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		if (file == null) return;
		String s = file.getAbsolutePath();
		if (s == null || s.trim().length() == 0) return;
		if (!s.endsWith(XMLFilter.XML)) {
			String fileName = s+"."+XMLFilter.XML;
			file = new File(fileName);
		}			
		saveBackROI(file.getAbsolutePath());
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
	 * @see MeasurementViewer#figureAttributeChanged(AttributeKey, ROIFigure)
	 */
	public void figureAttributeChanged(AttributeKey key, ROIFigure figure)
	{
		model.figureAttributeChanged(key, figure);
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
	 * @see MeasurementViewer#showROIAssistant(ROI)
	 */
	public void showROIAssistant(ROI roi)
	{
		view.showROIAssistant(roi);
	}
	
	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#showMeasurementsInMicrons(boolean)
	 */
	public void showMeasurementsInMicrons(boolean inMicrons)
	{
		model.showMeasurementsInMicrons(inMicrons);
		view.updateDrawingArea();
		view.refreshResultsTable();
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
		
		if (!view.inDataView() || !view.isVisible()) return;
		Collection<ROIFigure> collection = getSelectedFigures();
		if (collection.size() != 1) return;
		ROIFigure figure = collection.iterator().next();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi = figure.getROI();
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
		while (shapeIterator.hasNext())
			shapeList.add(shapeMap.get(shapeIterator.next()));
		
		if (shapeList.size() != 0) analyseShapeList(shapeList);
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
		if (!view.inDataView() || !view.isVisible()) return;
		Collection<ROIFigure> collection = getSelectedFigures();
		if (collection.size() != 1) return;
		
		ROIFigure figure = collection.iterator().next();
		ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi = figure.getROI();
		TreeMap<Coord3D, ROIShape> shapeMap = roi.getShapes();
		Iterator<Coord3D> shapeIterator = shapeMap.keySet().iterator();
		while(shapeIterator.hasNext())
			shapeList.add(shapeMap.get(shapeIterator.next()));
		if (shapeList.size()!=0) analyseShapeList(shapeList);
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
	 * @see MeasurementViewer#analyseShapeList(List)
	 */
	public void analyseShapeList(List<ROIShape> shapeList)
	{
		if (shapeList == null)
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
				return;
		}
		if(!validShapeList(shapeList))
			return;
		
		if (model.getActiveChannels().size() == 0) {
			//view.displayAnalysisResults();
			view.displayAnalysisResults();
		} else {
			model.fireAnalyzeShape(shapeList);
			fireStateChange();
		}
		
	}

	/**
	 * Check to see if the selected figure contains textFigure
	 * @param shapeList see above.
	 * @return see above.
	 */
	private boolean validShapeList(List<ROIShape> shapeList)
	{
		for(ROIShape shape : shapeList)
			if(shape.getFigure() instanceof MeasureTextFigure)
				return false;
		return true;
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
	 * @see MeasurementViewer#createSingleFigure(boolean)
	 */
	public void createSingleFigure(boolean createSingleFig)
	{
		view.createSingleFigure(createSingleFig);
	}

	/** 
	 * Saves the ROI without displaying a file chooser. 
	 * 
	 * @param path The absolute path to the file.
	 */
	private void saveBackROI(String path)
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		try {
			model.saveROI(path, false);
		} catch (ParsingException e) {
			reg.getLogger().error(this, "Cannot save the ROI "+e.getMessage());
			un.notifyInfo("Save ROI", "Cannot save ROI " +
										"for "+model.getImageID());
		}
		un.notifyInfo("Save ROI", "The Regions of Interests have been " +
									"successfully saved. ");
		firePropertyChange(ROI_CHANGED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/** 
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#saveAndDiscard()
	 */
	public void saveAndDiscard()
	{
		String fileSaved = model.getFileSaved();
		if (fileSaved != null) {
			saveBackROI(fileSaved);
			return;
		}
		//TODO: Externalize the UI code in a customized FileChooser Dialog
		List<FileFilter> filterList = new ArrayList<FileFilter>();
		filterList.add(new XMLFilter());
		FileChooser chooser =
				new FileChooser(
					view, FileChooser.SAVE, "Save the ROI File",
					"Save the ROI data in the file associate with this image.",
					filterList);
		File f = UIUtilities.getDefaultFolder();
	    if (f != null) chooser.setCurrentDirectory(f);
		try
		{
			String savedFileString = FileMap.getSavedFile(model.getServerName(), 
							model.getUserName(), model.getPixelsID());
			File savedFile;
			if (savedFileString == null)
			{
				savedFileString = model.getImageName();
				savedFile = new File(savedFileString);
				chooser.setSelectedFile(savedFile.getName());
			}
			else
			{
				savedFile = new File(savedFileString);
				chooser.setCurrentDirectory(savedFile);
				chooser.setSelectedFile(savedFile);
			}
		}	
		catch (ParsingException e)
		{
			// Do nothing as we're really only looking to see if the default 
			// directory or filename should be set for loading.
		}
		int results = chooser.showDialog();
		if (results != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		if (!XMLFilter.XML.endsWith(file.getAbsolutePath())) {
			String fileName = file.getAbsolutePath()+"."+XMLFilter.XML;
			file = new File(fileName);
		}
		saveBackROI(file.getAbsolutePath());
		discard();
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#toFront()
     */
    public void toFront()
    {
    	if (model.getState() == DISCARDED) return;
    	controller.toFront();
    }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setIconImage(BufferedImage)
     */
	public void setIconImage(BufferedImage thumbnail)
	{
		if (model.getState() == DISCARDED) return;
		//view.setIconImage(thumbnail);
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setRndImage(Object)
     */
	public void setRndImage(Object rndImage)
	{
		if (model.getState() == DISCARDED) return;
		model.setRenderedImage(rndImage);
	}
    
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#hasROIToSave()
     */
	public boolean hasROIToSave() 
	{
		if (model.getState() == DISCARDED) return false;
		return model.hasROIToSave();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setServerROI(Collection)
     */
	public void setServerROI(Collection result)
	{		
		if (model.getState() != LOADING_ROI)
			throw new IllegalArgumentException("The method can only " +
					"be invoked in the LOADING_ROI state.");
		try {
			if (result != null) { //some ROI previously saved.
				model.setServerROI(result, true);
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
		//bring up the UI.
		view.layoutUI();
		//view.rebuildManagerTable();
		
		
		
		
		view.updateDrawingArea();
		view.setReadyStatus();
		fireStateChange();
		//Now we are ready to go. We can post an event to add component to
		//Viewer
		postEvent(MeasurementToolLoaded.ADD);
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#isServerROI()
     */
	public boolean isServerROI() { return model.isServerROI(); }

	public String getViewTitle() {
		// TODO Auto-generated method stub
		return model.getImageTitle();
	}
	
}
