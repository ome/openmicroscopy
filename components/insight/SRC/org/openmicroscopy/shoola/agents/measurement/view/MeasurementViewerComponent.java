/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerComponent 
 *
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
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections.CollectionUtils;
//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.FileMap;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;

import omero.gateway.SecurityContext;
import omero.gateway.model.ROIResult;

import org.openmicroscopy.shoola.env.event.EventBus;

import omero.log.LogMessage;
import omero.log.Logger;

import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ROIData;
import pojos.WorkflowData;

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
 * @since OME3.0
 */
class MeasurementViewerComponent
	extends AbstractComponent
	implements MeasurementViewer
{
	
	/** The Model sub-component. */
    private MeasurementViewerModel model;
	
    /** The Control sub-component. */
    private MeasurementViewerControl controller;
    
    /** The View sub-component. */
    private MeasurementViewerUI view;
    
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
					model.getSecurityContext(),
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
		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) chooser.setCurrentDirectory(f);
		} catch (Exception ex) {}
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

    /** Saves the ROI (not asynchronously) and discards. */
    void saveAndDiscard()
    {
    	model.saveROIToServer(false, false);
    	model.saveWorkflowToServer(false);	
    	discard();
    }
    
    /**
     * Invokes when the ROI has been deleted.
     * 
     * @param imageID The image's identifier.
     */
    void onROIDeleted(long imageID)
    { 
    	model.onROIDeleted(imageID);
    	fireStateChange();
    }

    /** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#activate()
     */
    public void activate()
    { 
    	activate(model.getMeasurements(), model.isHCSData(),
    			model.isBigImage());
    }
    
    /** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#activate(List, boolean, boolean)
     */
	public void activate(List<FileAnnotationData> measurements, boolean HCSData,
			boolean isBigImage)
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
        		model.setHCSData(HCSData);
        		model.setBigImage(isBigImage);
        		view.buildGUI();
        		if (HCSData) {
        			if (measurements == null) {
        				model.setHCSData(false);
        				model.fireLoadWorkflow();
                		model.fireLoadROIServerOrClient(false);
        			} else 
        				model.fireLoadROIFromServer(measurements);
        		} else {
        			model.fireLoadWorkflow();
            		model.fireLoadROIServerOrClient(false);
        		}
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
			view.setVisible(false);
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
		model.notifyDataChanged(true);
		firePropertyChange(ROI_CHANGED_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
		fireStateChange();
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
			model.setState(MeasurementViewer.READY);
			view.refreshToolBar();
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
				//reset
				
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
		view.refreshToolBar();
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
			if (f != magnification) {
			    model.setMagnification(magnification);
			    view.onMagnificationChanged();
			}
			if (!model.isBigImage()) return;
		}
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
			ROIFigure fig;
			while (i.hasNext()) {
				shape = (ROIShape) i.next();
				if (shape != null)
				{
					fig = shape.getFigure();
					drawing.add(fig);
					if (fig.canAnnotate())
						fig.addFigureListener(controller);
				}
			}
		}
		//Reset the result.
		view.displayAnalysisResults();
		model.getDrawingView().setDrawing(drawing);
		drawing.addDrawingListener(controller);
		if (f != magnification)
			model.setMagnification(magnification);
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
		if (model.isHCSData()) {
			List<FileAnnotationData> list = model.getMeasurements();
			if (list == null || list.size() == 0) view.setVisible(false);
			else discard();
		} else view.setVisible(false);
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
		chooser.setCheckOverride(false);
		if (chooser.showDialog() != JFileChooser.APPROVE_OPTION) return;
		File f = chooser.getSelectedFile();
		if (f == null) return;
		model.fireROILoading(f.getAbsolutePath());
		fireStateChange();
		//view.updateDrawingArea();
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
     * @see MeasurementViewer#saveROIToServer(boolean)
     */
	public void saveROIToServer(boolean close)
	{
		if (!canAnnotate()) return;
		List<ROI> l = model.getROIToDelete();
		if (l != null && l.size() > 0) {
			List<DeletableObject> objects = new ArrayList<DeletableObject>();
			Iterator<ROI> i = l.iterator();
			ROI roi;
			ROIData data;
			SecurityContext ctx = model.getSecurityContext();
			DeletableObject d;
			while (i.hasNext()) {
				roi = i.next();
				if (!roi.isClientSide() && roi.canDelete()) {
					data = new ROIData();
					data.setId(roi.getID());
					data.setImage(model.getImage().asImage());
					d = new DeletableObject(data);
					d.setSecurityContext(ctx);
					objects.add(d);
				}
			}
			if (objects.size() == 0) {
				model.saveROIToServer(true, close);
			} else {
				model.deleteAllROIs(objects);
			}
		} else {
			model.saveROIToServer(true, close);
		}
		fireStateChange();
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
		//Show or hide some shapes if they are visible on a channel or not
		TreeMap<Long, ROI> rois = model.getROI();
		Collection<ROIFigure> figures = model.getAllFigures();
		ROIFigure figure, f;
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeMap;
		
		ROIShape shape;
		Entry entry;
		if (rois != null) {
			Iterator j = rois.entrySet().iterator();
			Iterator k;
			Coord3D coord;
			int c;
			while (j.hasNext()) {
				entry = (Entry) j.next();
				roi = (ROI) entry.getValue();
				shapeMap = roi.getShapes();
				k = shapeMap.entrySet().iterator();
				while (k.hasNext()) {
					entry = (Entry) k.next();
					shape = (ROIShape) entry.getValue();
					coord = shape.getCoord3D();
					f = shape.getFigure();
					c = coord.getChannel();
					if (c >= 0) {
						if (f.canAnnotate()) {
							f.removeFigureListener(controller);
							f.setVisible(model.isChannelActive(c));
							f.addFigureListener(controller);
						}
					}
				}
			}
			view.repaint();
		}
		if (!view.inDataView() || !view.isVisible()) return;
		figures = getSelectedFigures();
		if (figures.size() != 1) return;
		figure = figures.iterator().next();
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		roi = figure.getROI();
		shapeMap = roi.getShapes();
		Iterator j = shapeMap.entrySet().iterator();
		while (j.hasNext()) {
			entry = (Entry) j.next();
			shapeList.add( (ROIShape) entry.getValue());
		}
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
		if (!validShapeList(shapeList))
			return;
		
		if (model.getActiveChannels().size() == 0) {
			model.setAnalysisResults(null);
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
		firePropertyChange(ROI_CHANGED_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
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
				model.setServerROI(result);
			} 	
		} catch (Exception e) {
			String s = "Cannot convert server ROI into UI objects:";
			MeasurementAgent.getRegistry().getLogger().error(this, s+e);
		}
		//bring up the UI.
		view.layoutUI();
		view.updateDrawingArea();
		fireStateChange();
		//Now we are ready to go. We can post an event to add component to
		//Viewer
		postEvent(MeasurementToolLoaded.ADD);
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#isHCSData()
     */
	public boolean isHCSData() { return model.isHCSData(); }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#getViewTitle()
     */
	public String getViewTitle() { return model.getImageTitle(); }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setLoadingFromServerClient(Collection)
     */
	public void setLoadingFromServerClient(Collection result) 
	{
		if (model.getState() != LOADING_ROI)
			throw new IllegalArgumentException("The method can only " +
					"be invoked in the LOADING_ROI state.");
		try 
		{
			boolean hasResult = false;
			if (result != null) {
				Iterator<ROIResult> i = result.iterator();
				ROIResult roiResult;
				if (i.hasNext())
				{
					roiResult = i.next();
					if (CollectionUtils.isNotEmpty(roiResult.getROIs()))
						hasResult = true;
				}
			}
			
			if (hasResult) {
				//some ROI previously saved.
				//result.ge
				model.setServerROI(result);	
			} else {
				model.fireROILoading(null);
				return;
			}
		} catch (Exception e) {
			String s = "Cannot convert server ROI into UI objects:";
			MeasurementAgent.getRegistry().getLogger().error(this, s+e);
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Load ROI", "Cannot display the ROI.");
		}
		view.refreshToolBar();
		view.rebuildManagerTable();
		view.refreshResultsTable();
		view.updateDrawingArea();
		view.setReadyStatus();
		fireStateChange();
		//Now we are ready to go. We can post an event to add component to
		//Viewer
		postEvent(MeasurementToolLoaded.ADD);
		return;
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setUpdateROIComponent(Collection)
     */
	public void setUpdateROIComponent(Collection result) 
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		try {
			model.removeAllROI();
			view.rebuildManagerTable();
			view.clearInspector();
			view.refreshResultsTable();
			view.updateDrawingArea();
		} catch (NoSuchROIException e) {
			reg.getLogger().error(this, "Cannot save the ROI "+e.getMessage());
			un.notifyInfo("Save ROI", "Cannot save ROI " +
										"for "+model.getImageID());
		}
		model.fireLoadROIServerOrClient(false);
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#createWorkflow()
     */
	public void createWorkflow()
	{
		view.createWorkflow();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setWorkflow(String)
     */
	public void setWorkflow(String workflowNamespace)
	{
		workflowNamespace = view.getWorkflowFromDisplay(workflowNamespace);
		model.setWorkflow(workflowNamespace);
		view.updateWorkflow();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setWorkflow(List)
     */
	public void setKeyword(List<String> keyword)
	{
		model.setKeyword(keyword);
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#canAnnotate()
     */
	public boolean canAnnotate()
	{
		if (model.getState() == DISCARDED) return false;
		//Check if current user can write in object
		ExperimenterData exp = 
			(ExperimenterData) MeasurementAgent.getUserDetails();
		long id = exp.getId();
		Object ref = model.getRefObject();
		boolean b = EditorUtil.isUserOwner(ref, id);
		if (b) return b;
		if (ref instanceof DataObject) {
			return ((DataObject) ref).canAnnotate();
		}
		return false;
	}
	
	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#canDelete()
     */
	public boolean canDelete()
	{
		if (model.getState() == DISCARDED) return false;
		//Check if current user can write in object
		ExperimenterData exp = 
			(ExperimenterData) MeasurementAgent.getUserDetails();
		long id = exp.getId();
		Object ref = model.getRefObject();
		boolean b = EditorUtil.isUserOwner(ref, id);
		if (b) return b;
		if (ref instanceof DataObject) {
			return ((DataObject) ref).canDelete();
		}
		return false;
	}
	
	/** 
	 * Overridden to return the name of the instance to save.
	 * @see #toString()
	 */
	public String toString()
	{ 
		return "ROI for: "+EditorUtil.truncate(model.getImageName());
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setWorkflowList(List)
     */
	public void setWorkflowList(List<WorkflowData> workflows)
	{
		for(WorkflowData workflow : workflows)
			model.addWorkflow(workflow);
		view.addedWorkflow();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#deleteAllROIs()
     */
	public void deleteAllROIs(int level)
	{
		if (!canDelete()) return;
		List<ROIData> list;
		if (model.isMember()) level = MeasurementViewer.ME;
		list = model.getROIData(level);
		if (list.size() == 0) return;
		List<DeletableObject> l = new ArrayList<DeletableObject>();
		Iterator<ROIData> i = list.iterator();
		ROIData roi;
		SecurityContext ctx = model.getSecurityContext();
		DeletableObject d;
		while (i.hasNext()) {
			roi = i.next();
			if (roi.getId() > 0) {
				d = new DeletableObject(roi);
				d.setSecurityContext(ctx);
				l.add(d);
			}
		}
		//if (l.size() == 0) return;
		//clear view. and table.
		ExperimenterData exp = 
			(ExperimenterData) MeasurementAgent.getUserDetails();
		try {
			List<ROIFigure> figures = model.removeAllROI(exp.getId(), level);
			if (figures != null) {
				//clear all tables.
				view.deleteROIs(figures);
				model.getROIComponent().reset();
			}
			
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Delete ROI");
			msg.print(e);
			MeasurementAgent.getRegistry().getLogger().error(this, msg);
		}
		model.deleteAllROIs(l);
		fireStateChange();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#hasROIToDelete()
     */
	public boolean hasROIToDelete()
	{
		if (model.getState() == DISCARDED) return false;
		return model.hasROIToDelete();
	}

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#isMember()
     */
	public boolean isMember() { return model.isMember(); }

	/** 
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#isMember()
     */
	public void onUpdatedChannels(List<ChannelData> channels)
	{
		if (model.getState() == DISCARDED) return;
		model.setChannelData(channels);
		view.displayAnalysisResults();
	}

	/**
	 * Implemented as specified by the {@link MeasurementViewer} interface.
	 * @see MeasurementViewer#exportGraph()
	 */
	public void exportGraph() {
	    view.exportGraph();
	}

	/**
     * Implemented as specified by the {@link MeasurementViewer} interface.
     * @see MeasurementViewer#setROIAnnotations(Map)
     */
	public void setROIAnnotations(Map<Long, List<AnnotationData>> result)
	{
	    if (model.getState() == DISCARDED) return;

	}
}
