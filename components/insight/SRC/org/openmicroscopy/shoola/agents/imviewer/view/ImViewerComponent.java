/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.events.iviewer.ChannelSelection;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageRendered;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurePlane;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.ResetRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerCreated;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerState;
import org.openmicroscopy.shoola.agents.events.treeviewer.NodeToRefreshEvent;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PlayMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.PreferencesDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjSavingDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.event.EventBus;

import omero.log.LogMessage;
import omero.log.Logger;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Tile;
import org.openmicroscopy.shoola.env.ui.SaveEventBox;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;


/** 
 * Implements the {@link ImViewer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerModel
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerUI
 * @see org.openmicroscopy.shoola.agents.imviewer.view.ImViewerControl
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
class ImViewerComponent
	extends AbstractComponent
	implements ImViewer
{

	/** The message if rendering setting to save. */
	static final String						RND = "The last rendering settings";
	
	/** The message if rendering setting to annotation. */
	static final String						ANNOTATION = "The annotations";
	
	/** The Model sub-component. */
	private ImViewerModel       			model;

	/** The Control sub-component. */
	private ImViewerControl     			controller;

	/** The View sub-component. */
	private ImViewerUI          			view;

	/** Collection of events to display. */
	private Map<String, SaveRelatedData>	events;
	
	/** Flag indicating that a new z-section or timepoint is selected. */
	private boolean							newPlane;

	/** 
	 * Flag indicating that the rendering settings have been saved
	 * before copying.
	 */
	private boolean							saveBeforeCopy;

    /** The color model used. */
    private String							colorModel;
    
    /** The projection dialog. */
    private ProjSavingDialog 				projDialog;
    
    /** The list of component added to the main viewer. */
    private List<JComponent>				layers;
    
    /** The color changes preview.*/
    private Map<Integer, Color>				colorChanges;
    
    /** Flag indicating that it was not possible to save the settings.*/
    private boolean failureToSave;
    
    /** The ImageAcquisitionData */
    private ImageAcquisitionData acquisitionData;
    
	/**
	 * Creates and returns an image including the ROI
	 * 
	 * @param image The image to handle.
	 * @return See above.
	 */
	private BufferedImage createImageWithROI(BufferedImage image)
	{
		Iterator<JComponent> i = layers.iterator();
		JComponent c;
		BufferedImage img = Factory.copyBufferedImage(image);
		DrawingCanvasView canvas;
		while (i.hasNext()) {
			c = i.next();
			if (c instanceof DrawingCanvasView) {
				canvas = (DrawingCanvasView) c;
				canvas.print(img.getGraphics());
			}
		}
		return img;
	}
	
	/** 
	 * Brings up the dialog used to set the parameters required for the
	 * projection.
	 */
	private void showProjectionDialog()
	{
		if (projDialog == null) {
			projDialog = new ProjSavingDialog(view, model.getParent(),
					model.getGrandParent());
			projDialog.initialize(view.getProjectionType(), model.getRealT(),
					model.getPixelsType(), model.getImageName(), 
					model.getContainers(), model.getMaxZ()+1, 
					view.getProjectionStartZ()+1, view.getProjectionEndZ()+1);
			projDialog.addPropertyChangeListener(controller);
			projDialog.pack();
			Dimension minimumSize = new Dimension(projDialog.getWidth(),
					projDialog.getHeight());
			projDialog.setMinimumSize(minimumSize);
		} else {
			projDialog.setProjectionInterval(view.getProjectionStartZ()+1,
					view.getProjectionEndZ()+1);
		}
		UIUtilities.centerAndShow(projDialog);
	}

	/**
	 * Posts a {@link MeasurePlane} event to indicate that a new plane is
	 * rendered or a new magnification factor has been selected.
	 */
	private void postMeasurePlane()
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		double f = model.getZoomFactor()*model.getOriginalRatio();
		if (model.isBigImage()) {
			f = view.getBigImageMagnificationFactor();
		}
		MeasurePlane event = new MeasurePlane(model.getPixelsID(), 
				model.getDefaultZ(), model.getDefaultT(), f);
		if (model.isBigImage()) {
			event.setSize(model.getTiledImageSizeX(),
					model.getTiledImageSizeY());
		}
		bus.post(event);
	}

	/**
	 * Posts an {@link ViewerState} event to indicate that the frame state
	 * of this component has changed.
	 * 
	 * @param index One of the constants defined by {@link ViewerState}.
	 */
	private void postViewerState(int index)
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		ViewerState event = new ViewerState(model.getPixelsID(), index);
		bus.post(event);
	}

	/**
	 * Posts an {@link ChannelSelection} event to indicate that the 
	 * a new channel is selected or unselected; or that a channel is mapped
	 * to a new color.
	 * 
	 * @param index One of the constants defined by {@link ChannelSelection}.
	 */
	private void postActiveChannelSelection(int index)
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		ChannelSelection event = new ChannelSelection(model.getPixelsID(), 
				model.getActiveChannelsColorMap(), index);
		bus.post(event);
		view.setPlaneInfoStatus();
	}
	
	/** Saves the plane quietly i.e. no question asked to the user. */
	private void savePlane()
	{
		if (model.isOriginalPlane()) return;
		try {
			saveRndSettings(true);
		} catch (Exception e) {
			LogMessage logMsg = new LogMessage();
			logMsg.println("Cannot save rendering settings. ");
			logMsg.print(e);
			ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
		}
	}
	
	/** 
	 * Displays message before closing the viewer. 
	 * Returns <code>true</code> if we need to close the viewer,
	 * <code>false</code> otherwise.
	 * 
	 * @param notifyUser Pass <code>true</code> to notify the user, 
	 * 					<code>false</code> otherwise.
	 * @return See above.
	 */
	private boolean saveOnClose(boolean notifyUser)
	{
		if (!canAnnotate()) return true;
		if (failureToSave) return true;
		if (!notifyUser) {
			saveRndSettings(true);
			return true;
		}
		boolean showBox = false;
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JCheckBox rndBox = null;
		if (!model.isOriginalSettings(false)) {
			rndBox = new JCheckBox(RND);
			rndBox.setSelected(true);
			p.add(rndBox);
			showBox = true;
		}
		JCheckBox annotationBox = null;
		if (model.hasMetadataToSave()) {
			annotationBox = new JCheckBox(ANNOTATION);
			annotationBox.setSelected(true);
			p.add(annotationBox);
			showBox = true;
		}
		
		List<SaveEventBox> boxes = null;
		SaveEventBox box;
		Iterator j;
		if (events != null) {
			boxes = new ArrayList<SaveEventBox>(events.size());
			j = events.keySet().iterator();
			SaveRelatedData value;
			while (j.hasNext()) {
				value = events.get(j.next());
				if (value.isToSave()) {
					showBox = true;
					box = new SaveEventBox(value);
					boxes.add(box);
					p.add(box);
				}
			}
		}
		if (!showBox) {
			if (saveBeforeCopy) {
				try {
					model.saveRndSettings(false);
				} catch (Exception e) {
					LogMessage logMsg = new LogMessage();
					logMsg.println("Cannot save rendering settings. ");
					logMsg.print(e);
					ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
				}
			}
			return true;
		}
		MessageBox msg = new MessageBox(view, "Save Data", 
		"Before closing the viewer, do you want to save: ");
		msg.addCancelButton();
		msg.addBodyComponent(p);
		
		int option = msg.centerMsgBox();
		if (option == MessageBox.YES_OPTION) {
			if (saveBeforeCopy) {
				try {
					model.saveRndSettings(false);
				} catch (Exception e) {
					LogMessage logMsg = new LogMessage();
					logMsg.println("Cannot save rendering settings. ");
					logMsg.print(e);
					ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
				}
			}
			if (rndBox != null && rndBox.isSelected()) {
				try {
					saveRndSettings(true);
				} catch (Exception e) {
					LogMessage logMsg = new LogMessage();
					logMsg.println("Cannot save rendering settings. ");
					logMsg.print(e);
					ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
				}
			}
			if (rndBox == null) {
				savePlane();
			}
			if (annotationBox != null && annotationBox.isSelected())
				model.saveMetadata();
			if (boxes != null) {
				j = boxes.iterator();
				EventBus bus = ImViewerAgent.getRegistry().getEventBus();
				SaveRelatedData event;
				while (j.hasNext()) {
					box = (SaveEventBox) j.next();
					if (box.isSelected()) {
						event = (SaveRelatedData) box.getEvent();
						if (event.isToSave()) {
							bus.post(event.getSaveEvent());
						}
					}
				}
			}
			return true;
		} else if (option == MessageBox.CANCEL) {
			return false;
		} else if (option == MessageBox.NO_OPTION) {
			if (saveBeforeCopy) {
				try {
					model.saveRndSettings(false);
				} catch (Exception e) {
					LogMessage logMsg = new LogMessage();
					logMsg.println("Cannot save rendering settings. ");
					logMsg.print(e);
					ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
				}
			}
			//post an event
			ResetRndSettings evt = new ResetRndSettings(model.getImageID(),
			        model.getOriginalDef());
			ImViewerAgent.getRegistry().getEventBus().post(evt);
			model.resetMappingSettings(model.getOriginalDef());
		}
		return true;
	}

	/**
	 * Notifies that the projected image has been created and asks if the
	 * user wants to launch a viewer with the projected image.
	 * 
	 * @param message 	The message to display.
	 * @param image		The projected image.
	 */
	private void notifyProjection(String message, ImageData image)
	{
		model.setState(READY);
		String text = message;
		text += "\n";
		text += "Do you want to launch a viewer for the projected image?";
		
		MessageBox msg = new MessageBox(view, "Projection", text);
		int option = msg.centerMsgBox();
		if (option == MessageBox.YES_OPTION) {
			EventBus bus = ImViewerAgent.getRegistry().getEventBus();
			bus.post(new ViewImage(model.getSecurityContext(),
					new ViewImageObject(image), null));
		}
	}
	
	/** Fires an asynchronous call to preview the projection. */
	private void previewProjection()
	{
		switch (model.getState()) {
			case DISCARDED:
			case PROJECTING:
			case PROJECTION_PREVIEW: 
				return;
		}
		model.fireRenderProjected(view.getProjectionStartZ(), 
				view.getProjectionEndZ(), view.getProjectionStepping(), 
				view.getProjectionType());
		fireStateChange();
	}
	
	/** 
	 * Returns <code>true</code> if it is the same projection parameters,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean isSameProjectionParam()
	{
		ProjectionParam ref = model.getLastProjRef();
		if (ref == null) return true;
		if (ref.getStartZ() != view.getProjectionStartZ()) return false;
		if (ref.getEndZ() != view.getProjectionEndZ()) return false;
		if (ref.getAlgorithm() != view.getProjectionType()) return false;
		if (ref.getStepping() != view.getProjectionStepping()) return false;
		return true;
	}
	
	/**
	 * Posts an event to bring up the measurement tool.
	 * 
	 * @param measurements The measurements to load.
	 */
	private void postMeasurementEvent(List<FileAnnotationData> measurements)
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		double f = 
			model.getZoomFactor()*model.getOriginalRatio();
		if (model.isBigImage()) f = view.getBigImageMagnificationFactor();
		MeasurementTool request = new MeasurementTool(
				model.getSecurityContext(), model.getImageID(),
				model.getPixelsData(), model.getImageName(),
				model.getDefaultZ(), model.getDefaultT(),
				model.getActiveChannelsColorMap(),f,
				view.getBounds(), model.getChannelData());
		if (model.isBigImage()) {
			request.setSize(model.getTiledImageSizeX(),
					model.getTiledImageSizeY());
		}
		request.setThumbnail(model.getImageIcon());
		request.setRenderedImage(model.getBrowser().getRenderedImage());
		request.setMeasurements(measurements);
		request.setHCSData(model.isHCSImage());
		request.setBigImage(model.isBigImage());
		bus.post(request);
		int tabbedIndex = model.getTabbedIndex();
		if (tabbedIndex != ImViewer.VIEW_INDEX) {
			view.selectTabbedPane(ImViewer.VIEW_INDEX);
			renderXYPlane();
		}
	}

	/**
	 * Posts an event to indicate to embed the image viewer.
	 * 
	 * @param toAdd  Pass <code>true</code> to embed it, <code>false</code>
	 * 				 to remove it.
	 * @param detach Pass <code>true</code> to detach it, <code>false</code>
	 * 				 otherwise.
	 */
	private void postViewerCreated(boolean toAdd, boolean detach)
	{
		JComponent c = null;
		if (toAdd) {
			showView(RENDERER_INDEX);
			c = view.asComponent();
		} 
		ViewerCreated evt = new ViewerCreated(c, 
				model.getMetadataViewer().getEditorUI(), toAdd);
		evt.setDetach(detach);
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		bus.post(evt);
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	ImViewerComponent(ImViewerModel model)
	{
		if (model == null) 
		    throw new NullPointerException("No model.");
		
		this.model = model;
		controller = new ImViewerControl();
		view = new ImViewerUI(model.getImageTitle());
	}

	/** Links up the MVC triad. */
	void initialize()
	{
		model.initialize(this);
		controller.initialize(this, view);
		view.initialize(controller, model);
		if (model.getMetadataViewer() != null)
			model.getMetadataViewer().addPropertyChangeListener(controller);
	}

	/**
	 * Returns the Model sub-component.
	 * 
	 * @return See above.
	 */
	ImViewerModel getModel() { return model; }

	/** Sets the image to copy the rendering settings from. */
	void copyRndSettings()
	{
		view.enablePasteButton(true);
		firePropertyChange(RND_SETTINGS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Stored the event to display.
	 * 
	 * @param evt The event to store.
	 */
	void storeEvent(SaveRelatedData evt)
	{
		if (events == null) events = new HashMap<String, SaveRelatedData>();
 		events.put(evt.toString(), evt);
	}
	
	/**
	 * Returns a map with events to save.
	 * 
	 * @return See above.
	 */
	Map<String, SaveRelatedData> getSaveEvents() 
	{
		return events;
	}
	
	/**
	 * Returns the title associated to the viewer.
	 * 
	 * @return See above.
	 */
	String getTitle() { return view.getTitle(); }

	/** 
     * Invokes when the rendering settings has been saved using another way.
     * 
     * @param settings The save rendering settings.
     */
    void onRndSettingsSaved(RndProxyDef settings)
    {
    	if (settings == null) return;
    	model.resetOriginalSettings(settings);
    	refresh();
    	fireStateChange();
    }
    
    /**
     * Returns <code>true</code> if some settings to save, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
	boolean hasSettingsToSave()
	{
		if (failureToSave) return false;
		return !isOriginalSettings();
	}
	
	/**
	 * Sets the display mode.
	 * 
	 * @param displayMode The value to set.
	 */
	void setDisplayMode(int displayMode)
	{
		
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#activate(RndProxyDef, long, int, long)
	 */
	public void activate(RndProxyDef settings, long userID, int displayMode,
	        long selectedRndDefID)
	{
		model.setDisplayMode(displayMode);
		model.setSelectedRndDef(selectedRndDefID);
		switch (model.getState()) {
			case NEW:
				model.setAlternativeSettings(settings, userID);
				if (!model.isImageLoaded()) {
					model.fireImageLoading();
				} else {
					model.setState(ImViewer.LOADING_IMAGE_DATA);
					setImageData(model.getImage());
				}
				fireStateChange();
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
			default:
				if (view != null) {
					if (model.isSeparateWindow()) {
						UIUtilities.centerOnScreen(view);
						view.toFront();
						view.requestFocusInWindow();
					} else {
						postViewerCreated(true, false);
					}
				}
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#discard()
	 */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#changeUserGroup(long, long)
	 */
	public void changeUserGroup(long groupID, long oldGroupID)
	{
		
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setStatus(String, int)
	 */
	public void setStatus(String description, int perc)
	{
		if (model.getState() == DISCARDED) return;
		view.setLeftStatus(description);
		if (perc == 100) view.setLeftStatus();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setZoomFactor(double, int)
	 */
	public void setZoomFactor(double factor, int zoomIndex)
	{
		if (factor != ZoomAction.ZOOM_FIT_FACTOR &&
			(factor > ZoomAction.MAX_ZOOM_FACTOR ||
					factor < ZoomAction.MIN_ZOOM_FACTOR))
			throw new IllegalArgumentException("The zoom factor is a value " +
					"between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
					ZoomAction.MAX_ZOOM_FACTOR);
		switch (model.getState()) {
			case NEW:
			case LOADING_IMAGE:
			case LOADING_TILES:
			case DISCARDED:
				return;
		}
		if (model.isBigImage()) {
			double ox = (double) model.getTiledImageSizeX();
			double oy = (double) model.getTiledImageSizeY();
			
			model.setSelectedResolutionLevel(zoomIndex);
			view.setZoomFactor(factor, zoomIndex);
			int w = model.getTiledImageSizeX();
			int h = model.getTiledImageSizeY();
			double nx = (double) w/ox;
			double ny = (double) h/oy;
			model.getBrowser().setComponentsSize(w, h);
			model.getBrowser().setViewLocation(nx, ny);
			postMeasurePlane();
			return;
		}
		double oldFactor = model.getZoomFactor();
		if (oldFactor == factor && factor != ZoomAction.ZOOM_FIT_FACTOR) return;
		try {
			model.setZoomFactor(factor, false);
		} catch (Exception e) {
			Logger logger = ImViewerAgent.getRegistry().getLogger();
			logger.debug(this, "Cannot zoom image. Magnification: "+factor);
			model.setZoomFactor(factor, true);
		}
		view.setZoomFactor(factor, zoomIndex);
		
		if (view.isLensVisible()) {
			switch (model.getTabbedIndex()) {
				case ImViewer.VIEW_INDEX:
				case ImViewer.PROJECTION_INDEX:
					view.setImageZoomFactor((float) model.getZoomFactor());
					view.scrollLens();
			}
		}
		controller.setPreferences();
		postMeasurePlane();
	}

	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isZoomFitToWindow()
	 */
	public boolean isZoomFitToWindow() { return model.isZoomFitToWindow(); }

	/**
         * Implemented as specified by the {@link ImViewer} interface.
         * @see ImViewer#isRendererLoaded()
         */
	public boolean isRendererLoaded() {
	    return model.isRendererLoaded();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setColorModel(int)
	 */
	public void setColorModel(int key)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		switch (key) {
			case ColorModelAction.GREY_SCALE_MODEL:
				model.setColorModel(GREY_SCALE_MODEL, true);
				break;
			case ColorModelAction.RGB_MODEL:
				model.setColorModel(RGB_MODEL, true);
				break;
			default:
				throw new IllegalArgumentException("Color Model not supported");		
		}
		view.onColorModelChanged();
		//Remove 21/09
		//firePropertyChange(COLOR_MODEL_CHANGED_PROPERTY, 
		//		Integer.valueOf(1), Integer.valueOf(-1));
		view.setColorModel(key);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSelectedXYPlane(int, int, int)
	 */
	public void setSelectedXYPlane(int z, int t, int bin)
	{
	    boolean enableSave = z != model.getDefaultZ()
                || t != model.getDefaultT();
	    
	    if (z < 0) z = model.getDefaultZ();
	    if (t < 0) t = model.getRealSelectedT();
	    switch (model.getState()) {
	    case NEW:
	    case DISCARDED:
	        return;
	    }
	    int defaultZ = model.getDefaultZ();
	    int defaultT = model.getRealSelectedT();
	    if (bin >= 0) { //lifetime
	        int v = model.getSelectedBin();
	        firePropertyChange(ImViewer.BIN_SELECTED_PROPERTY,
	                Integer.valueOf(v), Integer.valueOf(bin));
	        if (defaultT != t) {
                firePropertyChange(ImViewer.T_SELECTED_PROPERTY,
                        Integer.valueOf(defaultT), Integer.valueOf(t));
            }
	        if (defaultZ != z) {
                firePropertyChange(ImViewer.Z_SELECTED_PROPERTY,
                        Integer.valueOf(defaultZ), Integer.valueOf(z));
            }
	    } else {
	        if (defaultZ == z && defaultT == t) return;
	        if (defaultZ != z) {
	            firePropertyChange(ImViewer.Z_SELECTED_PROPERTY,
	                    Integer.valueOf(defaultZ), Integer.valueOf(z));
	        }
	        if (defaultT != t) {
	            firePropertyChange(ImViewer.T_SELECTED_PROPERTY,
	                    Integer.valueOf(defaultT), Integer.valueOf(t));
	        }
	        newPlane = true;
	    }
	    model.setSelectedXYPlane(z, t, bin);
	    
	    if (enableSave)
            controller.getAction(ImViewerControl.SAVE_RND_SETTINGS).setEnabled(true);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSelectedXYPlane(int, int, int)
	 */
	public void setSelectedRegion(int z, int t, Rectangle region)
	{
		if (z < 0) z = model.getDefaultZ();
		if (t < 0) t = model.getRealSelectedT();
		if (region == null || !model.isBigImage()) {
			setSelectedXYPlane(z, t);
			return;
		}
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				return;
		}
		int defaultZ = model.getDefaultZ();
		int defaultT = model.getRealSelectedT();
		boolean reset = false;
		if (defaultZ != z) {
			reset = true;
			firePropertyChange(ImViewer.Z_SELECTED_PROPERTY, 
					Integer.valueOf(defaultZ), Integer.valueOf(z));
		}
		if (defaultT != t) {
			reset = true;
			firePropertyChange(ImViewer.T_SELECTED_PROPERTY, 
					Integer.valueOf(defaultT), Integer.valueOf(t));
		}
		Rectangle r = model.getBrowser().getVisibleRectangle();
		double f = view.getBigImageMagnificationFactor();
		Rectangle transformRegion = new Rectangle(
				(int) (region.x*f), (int) (region.y*f), (int) (region.width*f), 
				(int) (region.height*f));
				
		if (r.contains(transformRegion)) return;
		//Now determine the view size so the ROI is displayed.
		Rectangle r2 = new Rectangle(
				transformRegion.x, transformRegion.y, r.width, r.height);
		if (reset) {
			model.fireBirdEyeViewRetrieval(true);
			model.resetTiles();
			model.getBrowser().setSelectedRegion(r2);
		} else {
			model.getBrowser().setSelectedRegion(r2);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSelectedXYPlane(int, int)
	 */
	public void setSelectedXYPlane(int z, int t)
	{
		setSelectedXYPlane(z, t, -1);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setImage(Object)
	 */
	public void setImage(Object image)
	{
		if (model.getState() == LOADING_IMAGE_CANCELLED) return;
		if (model.getState() != LOADING_IMAGE) 
			throw new IllegalStateException("This method can only be invoked " +
			"in the LOADING_IMAGE state.");
		if (image == null) { //no need to notify.
			model.setImage(null);
			return;
		}
		if (!(image instanceof BufferedImage)) {
			model.setImage(null);
			return;
		}
		view.removeComponentListener(controller);
		if (newPlane) postMeasurePlane();
		newPlane = false;
		Object originalImage;
		originalImage = model.getOriginalImage();
        model.setImage((BufferedImage) image);
		view.handleUnitBar();
		view.setLeftStatus();
		view.setPlaneInfoStatus();
		if (originalImage == null && model.isZoomFitToWindow()) {
			controller.setZoomFactor(ZoomAction.ZOOM_FIT_TO_WINDOW);
		}
		if (model.isPlayingChannelMovie())
			model.setState(ImViewer.CHANNEL_MOVIE);
		if (!model.isPlayingMovie()) {
			//Post an event
			EventBus bus = ImViewerAgent.getRegistry().getEventBus();
			BufferedImage icon = model.getImageIcon();
			bus.post(new ImageRendered(model.getPixelsID(), icon, 
					model.getBrowser().getRenderedImage()));
			//if (icon != null) view.setIconImage(icon);
		}
			
		if (!model.isPlayingMovie() && !model.isPlayingChannelMovie()) {
			if (view.isLensVisible()) view.setLensPlaneImage();
			view.createHistoryItem(null);
		}
		view.setCursor(Cursor.getDefaultCursor());
		view.addComponentListener(controller);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#playChannelMovie(boolean)
	 */
	public void playChannelMovie(boolean play)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		//if (model.getState() != READY || model.getState() != CHANNEL_MOVIE) 
			//return;
		model.playMovie(play);
		view.playChannelMovie(!play);
		if (!play) {
			displayChannelMovie();
			controller.setHistoryState(READY);
		}
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setChannelColor(int, Color, boolean)
	 */
	public void setChannelColor(int index, Color c, boolean preview)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		if (preview) {
			if (colorChanges == null)
				colorChanges = new HashMap<Integer, Color>();
			if (c == null) {
				c = colorChanges.get(index); //reset the color.
				colorChanges.clear();
			} else {
				if (!colorChanges.containsKey(index))
					colorChanges.put(index, model.getChannelColor(index));
			}
		} else {
			if (colorChanges != null)
				colorChanges.remove(index);
		}
		if (c == null) return;
		try {
			model.setChannelColor(index, c);
			view.setChannelColor(index, c);
			if (!model.isChannelActive(index)) {
				setChannelActive(index, true);
				view.setChannelActive(index, ImViewerUI.ALL_VIEW);
			}
			if (GREY_SCALE_MODEL.equals(model.getColorModel()))
				setColorModel(ColorModelAction.RGB_MODEL);
		} catch (Exception e) {
			Registry reg = ImViewerAgent.getRegistry();
			LogMessage msg = new LogMessage();
			msg.println("Cannot set the color of channel "+index);
			msg.print(e);
			reg.getLogger().error(this, msg);
			reg.getUserNotifier().notifyError("Set channel color", 
					"Cannot set the color of channel "+index, e);
		}

		firePropertyChange(CHANNEL_COLOR_CHANGED_PROPERTY, -1, index);
		postActiveChannelSelection(ChannelSelection.COLOR_SELECTION);
		//if (model.isChannelActive(index)) renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setChannelSelection(int, boolean)
	 */
	public void setChannelSelection(int index, boolean b)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or " +
						"NEW state.");
		}
		//depends on model
		model.setLastSettingsRef(model.getTabbedIndex());
		int uiIndex = -1;
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
			if (model.getTabbedIndex() == ImViewer.GRID_INDEX) {
				List<Integer> l = new ArrayList<Integer>();
				List selectedChannels = view.getActiveChannelsInGrid();
				for (int i = 0; i < model.getMaxC(); i++) {
					if (i == index) {
						if (b) l.add(i);
					} else {
						if (selectedChannels.contains(i)) l.add(i);
					}
				}
				view.setChannelsSelection(l);
			} else if (model.getTabbedIndex() == ImViewer.PROJECTION_INDEX) {
				if (model.isChannelActive(index)) return;
				boolean c;
				for (int i = 0; i < model.getMaxC(); i++) {
					c = i == index;
					model.setChannelActive(i, c);  
					if (c) 
						firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
								Integer.valueOf(index-1), 
								Integer.valueOf(index));
				}
				uiIndex = ImViewerUI.PROJECTION_ONLY;
			} else {
				if (model.isChannelActive(index)) return;
				boolean c;
				for (int i = 0; i < model.getMaxC(); i++) {
					c = i == index;
					model.setChannelActive(i, c);  
					if (c) 
						firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
								Integer.valueOf(index-1), 
								Integer.valueOf(index));
				}
				uiIndex = ImViewerUI.VIEW_ONLY;
			}
		} else {
			uiIndex = ImViewerUI.ALL_VIEW;
			model.setChannelActive(index, b);
			firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
					Integer.valueOf(index-1), Integer.valueOf(index));
		}
		view.setChannelsSelection(uiIndex);
		model.setSelectedChannel(index);
		renderXYPlane();
		postActiveChannelSelection(ChannelSelection.CHANNEL_SELECTION);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#renderXYPlane()
	 */
	public void renderXYPlane()
	{
	    switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case LOADING_IMAGE:
			case DISCARDED:
			//case LOADING_BIRD_EYE_VIEW:
			case LOADING_RND:
				return;
		} 
		if (model.isBigImage()) {
			model.resetTiles();
			loadTiles(null);
			return;
		}
		int compression = view.getUICompressionLevel();
		boolean stop = false;
		int index = model.getTabbedIndex();
		RndProxyDef def;
		if (index == PROJECTION_INDEX) {
			def = model.getLastProjDef();
			boolean b = false;
			if (def != null) b = model.isSameSettings(def, false);
			if (b && isSameProjectionParam()) stop = true;
		} else {
			def = model.getLastMainDef();
			if (def != null) stop = model.isSameSettings(def, true);
		}
		//if (stop) return;
		
		if (index == PROJECTION_INDEX) {
			//if (stop) return;
			previewProjection();
			fireStateChange();
		} else if (index == GRID_INDEX) {
			if (GREY_SCALE_MODEL.equals(model.getColorModel())) {
				model.getBrowser().onColorModelChange();
			} else {
				model.fireImageRetrieval(compression);
				newPlane = false;
				fireStateChange();
			}
		} else {
			//if (stop) return;
			model.fireImageRetrieval(compression);
			newPlane = false;
			fireStateChange();
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setChannelActive(int, boolean)
	 */
	public void setChannelActive(int index, boolean b)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW or" +
				"LOADING_RENDERING_CONTROL state.");
		}
		model.setChannelActive(index, b);
		if (b)
			firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
					Integer.valueOf(index-1), Integer.valueOf(index));
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#displayChannelMovie()
	 */
	public void displayChannelMovie()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW" +
				"state.");
		}
		view.setChannelsSelection(ImViewerUI.ALL_VIEW);
		renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxC()
	 */
	public int getMaxC()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or " +
						"NEW state.");
		}
		return model.getMaxC();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getRealT()
	 */
	public int getRealT()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
					"LOADING_RENDERING_CONTROL state.");
		}
		return model.getRealT();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxZ()
	 */
	public int getMaxZ()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
					"LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxZ();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageName()
	 */
	public String getImageName()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		return model.getImageName();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getColorModel()
	 */
	public String getColorModel()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW"
						+" state.");
		}
		return model.getColorModel();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUI()
	 */
	public JFrame getUI()
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or" +
					" NEW state.");
		}
		return view;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#iconified(boolean)
	 */
	public void iconified(boolean b)
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			//throw new IllegalStateException(
			//"This method can't be invoked in the DISCARDED, NEW state.");
			return;
		}
		Boolean newValue =  Boolean.FALSE;
		Boolean oldValue = Boolean.TRUE;
		int index = ViewerState.DEICONIFIED;
		if (b) {
			newValue = Boolean.TRUE;
			oldValue = Boolean.FALSE;
			index = ViewerState.ICONIFIED;
		} 
		postViewerState(index);
		firePropertyChange(ICONIFIED_PROPERTY, oldValue, newValue);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDefaultZ()
	 */
	public int getDefaultZ()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW"+
				" state.");
		}
		return model.getDefaultZ();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDefaultT()
	 */
	public int getDefaultT()
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or NEW"+
			" state.");
		}
		return model.getDefaultT();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageComponents(String)
	 */
	public List getImageComponents(String colorModel, boolean includeROI)
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or NEW"+
			" state.");
		}
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List l = model.getActiveChannels();
		int n = l.size();
		List<BufferedImage> images = new ArrayList<BufferedImage>(n);
		if (n == 0) return images;
		else if (n == 1) {
			images.add(getDisplayedImage(includeROI));
			return images;
		}
		Iterator i = l.iterator();
		int index;
		String oldColorModel = model.getColorModel();
		Map<Integer, BufferedImage> map = 
			new HashMap<Integer, BufferedImage>(n);
		model.setColorModel(colorModel, false);
		BufferedImage img = null, splitImage;
		while (i.hasNext()) {
			index = (Integer) i.next();
			for (int j = 0; j < model.getMaxC(); j++)
				model.setChannelActive(j, j == index); 
			splitImage = model.getSplitComponentImage();
			if (splitImage != null)
				img = Factory.magnifyImage(splitImage, 
						model.getZoomFactor(), 0, model.isInterpolation());
			if (includeROI && layers != null) {
				img = createImageWithROI(img);
			}
			map.put(index, img);
		}
		model.setColorModel(oldColorModel, false);
		i = l.iterator();
		while (i.hasNext()) { //reset values.
			index = ((Integer) i.next()).intValue();
			model.setChannelActive(index, true);
		}
		List<ChannelData> channels = model.getChannelData();
		Iterator<ChannelData> k = channels.iterator();
		ChannelData channel;
		while (k.hasNext()) {
			channel = k.next();
			if (map.containsKey(channel.getIndex()))
				images.add(map.get(channel.getIndex()));
		}
		return images;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getGridImages()
	 */
	public List getGridImages()
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or NEW"+
			" state.");
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List active = model.getActiveChannels();
		int maxC = model.getMaxC();
		List<BufferedImage> images = new ArrayList<BufferedImage>(maxC);
		List<ChannelData> list = getSortedChannelData();
		Iterator<ChannelData> i = list.iterator();
		int k;
		Iterator w;
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
			active = view.getActiveChannelsInGrid();
			//Iterator i = active.iterator();
			while (i.hasNext()) {
				k = i.next().getIndex();
				if (active.contains(k)) {
					model.setChannelActive(k, true);
					for (int j = 0; j < maxC; j++) {
						if (j != k) model.setChannelActive(j, false);
					}
					images.add(model.getSplitComponentImage());
				} else {
					images.add(null);
				}
			}

			w = active.iterator();
			while (w.hasNext()) { //reset values.
				model.setChannelActive((Integer) w.next(), true);
			}
			if (active.size() != 0) {
				model.setColorModel(RGB_MODEL, false);
				images.add(model.getSplitComponentImage());
				model.setColorModel(GREY_SCALE_MODEL, false);
			}

		} else {
			while (i.hasNext()) {
				k = i.next().getIndex();
				if (model.isChannelActive(k)) {
					for (int l = 0; l < maxC; l++) {
						model.setChannelActive(l, k == l);
					}
					images.add(model.getSplitComponentImage());
					w = active.iterator();
					while (w.hasNext()) { //reset values.
						model.setChannelActive((Integer) w.next(), true);
					}
				} else {
					images.add(null);
				}
			}
		}

		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		return images;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getCombinedGridImage()
	 */
	public BufferedImage getCombinedGridImage()
	{
		switch (model.getState()) {
		case NEW:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or NEW"+
			" state.");
		}
		//view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (!GREY_SCALE_MODEL.equals(model.getColorModel())) return null;
		List active = view.getActiveChannelsInGrid();
		BufferedImage image = null;
		Iterator i = active.iterator();
		for (int k = 0; k < model.getMaxC(); k++) {
			model.setChannelActive(k, false);
		}
		while (i.hasNext()) { //reset values.
			model.setChannelActive(((Integer) i.next()).intValue(), true);
		}
		if (active.size() != 0) {
			model.setColorModel(RGB_MODEL, false);
			image = model.getSplitComponentImage();
			model.setColorModel(GREY_SCALE_MODEL, false);
		}

		
		active = model.getActiveChannels();
		i = active.iterator();
		while (i.hasNext()) { //reset values.
			model.setChannelActive(((Integer) i.next()).intValue(), true);
		}
		return image;
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDisplayedImage(boolean)
	 */
	public BufferedImage getDisplayedImage(boolean includeROI)
	{
		switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case DISCARDED:
				return null;
		}
		if (includeROI) {
			if (layers == null) return model.getDisplayedImage();
			return createImageWithROI(model.getDisplayedImage());//model.getBrowser().getRenderedImage());
		}
		return model.getDisplayedImage();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDisplayedProjectedImage()
	 */
	public BufferedImage getDisplayedProjectedImage()
	{
		switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case DISCARDED:
				return null;
		}
		return model.getBrowser().getDisplayedProjectedImage();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeX()
	 */
	public Length getPixelsSizeX()
	{
		switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case DISCARDED:
				return new LengthI(1, UnitsLength.PIXEL);
		}
		return model.getPixelsSizeX();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeY()
	 */
	public Length getPixelsSizeY()
	{
		switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case DISCARDED:
			    return new LengthI(1, UnitsLength.PIXEL);
		}
		return model.getPixelsSizeY();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeZ()
	 */
	public Length getPixelsSizeZ()
	{
		switch (model.getState()) {
			case NEW:
				throw new IllegalStateException(
						"This method can't be invoked in the NEW state.");
			case DISCARDED:
			    return new LengthI(1, UnitsLength.PIXEL);
		}
		return model.getPixelsSizeZ();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getViewTitle()
	 */
	public String getViewTitle()
	{
		if (model.getState() == DISCARDED) return "";
		return view.getTitle();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getChannelMetadata(int)
	 */
	public ChannelData getChannelMetadata(int index)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		return model.getChannelData(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getActiveChannels()
	 */
	public List getActiveChannels()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW "+
						"state.");
		}
		return model.getActiveChannels();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isUnitBar()
	 */
	public boolean isUnitBar()
	{
		if (model.getState() == DISCARDED) return false;
		return model.isUnitBar();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setUnitBar(boolean)
	 */
	public void setUnitBar(boolean b)
	{
		if (model.getState() == DISCARDED) return;
		model.getBrowser().setUnitBar(b);
		controller.setPreferences();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getHistoryState()
	 */
	public int getHistoryState()
	{
		return controller.getHistoryState();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getChannelColor(int)
	 */
	public Color getChannelColor(int index)
	{
		if (model.getState() == DISCARDED) return null;
		return model.getChannelColor(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setUnitBarSize(double)
	 */
	public void setUnitBarSize(double size)
	{
		if (model.getState() == DISCARDED) 
		    return;
		model.getBrowser().setUnitBarSize(size, model.getScaleBarUnit());
		controller.setPreferences();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showUnitBarSelection()
	 */
	public void showUnitBarSelection()
	{
		if (model.getState() == DISCARDED) return;
		UnitBarSizeDialog d = new UnitBarSizeDialog(view, model.getScaleBarUnit());
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#resetDefaults()
	 */
	public void resetDefaults()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		view.setLeftStatus();
		view.setPlaneInfoStatus();
		view.resetDefaults(); 
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarValue()
	 */
	public String getUnitBarValue()
	{
		if (model.getState() == DISCARDED) return "";
		return model.getBrowser().getUnitBarValue();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarSize()
	 */
	public double getUnitBarSize()
	{
		if (model.getState() == DISCARDED) return 0;
		return model.getBrowser().getUnitBarSize();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarColor()
	 */
	public Color getUnitBarColor()
	{
		if (model.getState() == DISCARDED) return null;
		if (model.getBrowser() == null) return null;
		return model.getBrowser().getUnitBarColor();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageIcon()
	 */
	public ImageIcon getImageIcon()
	{
		BufferedImage img = model.getImageIcon();
		if (img == null) return null;
		return new ImageIcon(img);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showLens()
	 */
	public void showLens()
	{
		if (model.getState() == DISCARDED) return;
		view.setLensVisible(!view.isLensVisible(), model.getTabbedIndex());
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getZoomedLensImage()
	 */
	public BufferedImage getZoomedLensImage()
	{
		if (model.getState() == DISCARDED) return null;
		return view.getZoomedLensImage();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showMenu(int, Component, Point)
	 */
	public void showMenu(int menuID, Component source, Point location)
	{
	    if (model.getState() == DISCARDED) return;
	    if (source == null) throw new IllegalArgumentException("No component.");
	    if (location == null) {
	        Point p = source.getLocation();
	        location = new Point(p.x+source.getWidth(), Math.abs(
	                p.y-source.getHeight()));
	    }
	    switch (menuID) {
	    case COLOR_PICKER_MENU:
	        if (model.getMaxC() == 1) showColorPicker(0);
	        else view.showMenu(menuID, source, location);
	        break;
	    case ACTIVITY_MENU:
	        model.activityOptions(source, location);
	        break;
	    default:
	        throw new IllegalArgumentException("Menu not supported.");
	    }
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxX()
	 */
	public int getMaxX() 
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or" +
						" NEW state.");
		}
		return model.getMaxX();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxY()
	 */
	public int getMaxY() 
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW" +
				"state.");
		}
		return model.getMaxY();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getSelectedIndex()
	 */
	public int getSelectedIndex() { return model.getTabbedIndex(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#playMovie(boolean, boolean, int)
	 */
	public void playMovie(boolean play, boolean visible, int index)
	{
	    switch (model.getState()) {
	    case NEW:
	    case LOADING_METADATA:
	    case DISCARDED:
	        return;
	    }
	    MoviePlayerDialog d = controller.getMoviePlayer();
	    boolean doClick = false;
	    if (visible) { // we have to play the movie
	        if (!d.isVisible()) {
	            controller.getAction(
	                    ImViewerControl.PLAY_MOVIE_T).setEnabled(false);
	            controller.getAction(
	                    ImViewerControl.PLAY_MOVIE_Z).setEnabled(false);
	            play = true;
	            UIUtilities.setLocationRelativeToAndShow(view, d);
	        }
	    } else {
	        if (d.isVisible()) {
	            controller.getAction(
	                    ImViewerControl.PLAY_MOVIE_T).setEnabled(true);
	            controller.getAction(
	                    ImViewerControl.PLAY_MOVIE_Z).setEnabled(true);
	            play = false;
	            d.setVisible(false);
	        } else {
	            switch (index) {
	            case PlayMovieAction.ACROSS_Z:
	                d.setZRange(model.getDefaultZ(), model.getMaxZ());
	                controller.getAction(
	                        ImViewerControl.PLAY_MOVIE_T).setEnabled(!play);
	                break;
	            case PlayMovieAction.ACROSS_T:
	                controller.getAction(
	                        ImViewerControl.PLAY_MOVIE_Z).setEnabled(!play);
	                break;
	            case PlayMovieAction.ACROSS_LIFETIME:
	                d.setBinRange(model.getSelectedBin(),
	                        model.getMaxLifetimeBin()-1);
	                controller.getAction(
	                        ImViewerControl.PLAY_LIFETIME_MOVIE).setEnabled(!play);
	                break;
	            default:
	                controller.getAction(
	                        ImViewerControl.PLAY_MOVIE_T).setEnabled(true);
	                controller.getAction(
	                        ImViewerControl.PLAY_MOVIE_Z).setEnabled(true);

	            }
	            doClick = true;
	            if (index != -1) d.setMovieIndex(index);
	            d.setTimeRange(model.getRealSelectedT(), model.getRealT()-1);
	        }
	    }

	    model.setPlayingMovie(play, index);
	    view.enableSliders(!play);
	    controller.getAction(ImViewerControl.CHANNEL_MOVIE).setEnabled(!play);
	    if (doClick) {
	        if (play) {
	            d.addPropertyChangeListener(
	                    MoviePlayerDialog.MOVIE_STATE_CHANGED_PROPERTY,
	                    controller);
	            d.doClick(MoviePlayerDialog.DO_CLICK_PLAY);
	        } else {
	            d.removePropertyChangeListener(
	                    MoviePlayerDialog.MOVIE_STATE_CHANGED_PROPERTY,
	                    controller);
	            d.doClick(MoviePlayerDialog.DO_CLICK_PAUSE);
	        }
	    } else {
	        d.removePropertyChangeListener(
	                MoviePlayerDialog.MOVIE_STATE_CHANGED_PROPERTY,
	                controller);
	    }
	    if (!play) {
	        if (view.isLensVisible()) view.setLensPlaneImage();
	        switch (view.getTabbedIndex()) {
	        case ImViewer.VIEW_INDEX:
	            view.createHistoryItem(null);
	            break;
	        case ImViewer.PROJECTION_INDEX:
	            view.createHistoryItem(view.getLastProjRef());
	        }
	        if (model.getState() != LOADING_IMAGE)
	            model.setState(READY);
	        fireStateChange();
	    }
	}

	/** 
	 * 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getGridImage()
	 */
	public BufferedImage getGridImage()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or NEW" +
						"state.");
		}
		return model.getBrowser().getGridImage();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getLensImageComponents(String)
	 */
	public List getLensImageComponents(String colorModel)
	{
		if (!view.hasLensImage()) return null;
		if (model.getTabbedIndex() != ImViewer.VIEW_INDEX) return null;
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or " +
				"NEW state.");
		}
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List l = model.getActiveChannels();
		if (l.size() < 2) return null;
		Iterator i = l.iterator();
		int index;
		String oldColorModel = model.getColorModel();
		List<BufferedImage> images = new ArrayList<BufferedImage>(l.size());
		model.setColorModel(colorModel, false);
		while (i.hasNext()) {
			index = ((Integer) i.next()).intValue();
			for (int j = 0; j < model.getMaxC(); j++)
				model.setChannelActive(j, j == index); 
			images.add(view.createZoomedLensImage(
					model.getSplitComponentImage()));
		}
		model.setColorModel(oldColorModel, false);
		i = l.iterator();
		while (i.hasNext()) { //reset values.
			index = ((Integer) i.next()).intValue();
			model.setChannelActive(index, true);
		}
		return images;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isTextVisible()
	 */
	public boolean isTextVisible() { return model.isTextVisible(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setTextVisible(boolean)
	 */
	public void setTextVisible(boolean b)
	{
		model.setTextVisible(b);
		model.getBrowser().viewSplitImages();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showMeasurementTool(Point)
	 */
	public void showMeasurementTool(Point point)
	{
		//TODO: Review for HCS.
		if (!model.isHCSImage()) {
			postMeasurementEvent(null);
			return;
		}
		Collection measurements = model.getMeasurements();
		if (measurements == null || measurements.size() == 0) {
			postMeasurementEvent(null);
			return;
		}
		Map<JCheckBox, FileAnnotationData> boxes = 
			new LinkedHashMap<JCheckBox, FileAnnotationData>();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		Iterator i;
		if (measurements != null) {
			i = measurements.iterator();
			FileAnnotationData fa;
			JCheckBox box;
			Object object;
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof FileAnnotationData) {
					fa = (FileAnnotationData) object;
					box = new JCheckBox(fa.getDescription());
					box.setSelected(true);
					p.add(box);
					boxes.put(box, fa);
				}
			}
		}
		
		if (boxes.size() == 0)  return;
		
		view.setMeasurementLaunchingStatus(true);
		MessageBox msg = new MessageBox(view, "Measurements", 
		"Select the measurements to display alongside the image.");
		msg.setNoText("Cancel");
		msg.setYesText("Display");
		msg.addBodyComponent(p);
		int option;
		if (point != null) option = msg.showMsgBox(point);
		else option = msg.centerMsgBox();
		List<FileAnnotationData> files = new ArrayList<FileAnnotationData>();
		
		if (option == MessageBox.YES_OPTION) {
			Entry entry;
			i = boxes.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				if (((JCheckBox) entry.getKey()).isSelected())
					files.add((FileAnnotationData) entry.getValue());
			}
			if (files.size() > 0) postMeasurementEvent(files);
		} else {
			view.setMeasurementLaunchingStatus(false);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#addToView(JComponent)
	 */
	public void addToView(JComponent comp)
	{
		if (model.getState() != READY) return;
		if (comp == null) return;
		if (layers == null) layers = new ArrayList<JComponent>();
		layers.add(comp);
		view.setMeasurementLaunchingStatus(false);
		model.getBrowser().addComponent(comp, ImViewer.VIEW_INDEX, false);
		comp.setVisible(true);
		view.repaint();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#removeFromView(JComponent)
	 */
	public void removeFromView(JComponent comp)
	{
		if (model.getState() != READY) return;
		if (comp == null) return;
		if (layers != null) layers.remove(comp);
		model.getBrowser().removeComponent(comp, ImViewer.VIEW_INDEX);
		view.repaint();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#hasLens()
	 */
	public boolean hasLens()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		if (model.getState() != READY) return false;
		return view.hasLensImage(); 
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getZoomFactor()
	 */
	public double getZoomFactor()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		if (model.getState() != READY) return -1;
		return model.getZoomFactor();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isPlayingMovie()
	 */
	public boolean isPlayingMovie() { return model.isPlayingMovie(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelRed(int)
	 */
	public boolean isChannelRed(int index)
	{
		return model.isColorComponent(Renderer.RED_BAND, index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelGreen(int)
	 */
	public boolean isChannelGreen(int index)
	{
		return model.isColorComponent(Renderer.GREEN_BAND, index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelBlue(int)
	 */
	public boolean isChannelBlue(int index)
	{
		return model.isColorComponent(Renderer.BLUE_BAND, index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelActive(int)
	 */
	public boolean isChannelActive(int index)
	{
		return model.isChannelActive(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#copyRenderingSettings()
	 */
	public void copyRenderingSettings()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				//throw new IllegalStateException(
				//"This method can't be invoked in the DISCARDED, NEW state.");
				return;
		}
		try {
			model.copyRenderingSettings();
			saveBeforeCopy = false;
		} catch (Exception e) {
			saveBeforeCopy = false;
			failureToSave = true;
			Logger logger = ImViewerAgent.getRegistry().getLogger();
			LogMessage logMsg = new LogMessage();
			logMsg.print("Rendering Exception:");
			logMsg.println(e.getMessage());
			logMsg.print(e);
			logger.error(this, logMsg);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#pasteRenderingSettings()
	 */
	public void pasteRenderingSettings()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				return;
		}
		if (!model.hasRndToPaste()) {
			return;
		}
		model.fireLoadRndSettingsToPaste();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#reloadRenderingThumbs()
	 */
	public void reloadRenderingThumbs() {
	    model.reloadRenderingThumbs();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#hasSettingsToPaste()
	 */
	public boolean hasSettingsToPaste()
	{
		switch (model.getState()) {
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
		}
		return model.hasRndToPaste();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isHistoryShown()
	 */
	public boolean isHistoryShown()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
			"This method can't be invoked in the DISCARDED state.");
		return view.isHistoryShown();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showHistory(boolean)
	 */
	public void showHistory(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
			"This method can't be invoked in the DISCARDED state.");
		view.showHistory(b);
		controller.setPreferences();
	}
	
	/** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#resetDefaultRndSettings()
     */
    public void resetDefaultRndSettings()
    {
    	if (model.getState() == DISCARDED)
			throw new IllegalStateException(
			"This method can't be invoked in the DISCARDED state.");
    	//addHistoryItem();
		model.resetDefaultRndSettings();
		view.resetDefaults();
		renderXYPlane();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#saveRndSettings(boolean)
     */
    public void saveRndSettings(boolean post)
    {
    	try {
    		model.saveRndSettings(true);
    		failureToSave = false;
		} catch (Exception e) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save settings", "Cannot save rendering settings. ");
			Logger logger = ImViewerAgent.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
	        msg.print("Save rendering settings");
	        msg.print(e);
	        logger.error(this, msg);
	        failureToSave = true;
	        //post event indicating no settings to save
		}
		if (post) {
		      EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		        bus.post(new RndSettingsCopied(Arrays.asList(model.getImageID()),
		                getPixelsID()));
		        fireStateChange();
		}
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#saveRndSettings()
     */
    public void toFront()
    {
    	if (model.getState() == DISCARDED) return;
    	controller.toFront();
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getMovieIndex()
     */
	public int getMovieIndex()
	{
		if (model.getState() == DISCARDED || !model.isPlayingMovie() ||
			model.isPlayingChannelMovie()) return -1;
		return model.getMovieIndex();
	}

	 /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getActiveChannelsInGrid()
     */
	public List getActiveChannelsInGrid()
	{
		if (model.getState() == DISCARDED) return null;
		return view.getActiveChannelsInGrid();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showPreferences()
	 */
	public void showPreferences()
	{
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		PreferencesDialog d = new PreferencesDialog(view, pref);
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setRenderingSettings(Map, long)
	 */
	public void setRenderingSettings(Map map, long userID)
	{
		if (model.getState() == DISCARDED) return;
		model.setRenderingSettings(map);
		if (userID >= 0) {
			if (map != null) {
				Entry entry;
				Iterator i = map.entrySet().iterator();
				ExperimenterData exp;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					exp = (ExperimenterData) entry.getKey();
					if (exp.getId() == userID) {
						model.setUserSettings(exp);
						break;
					} 
				}
			}
		} else {
			view.showUsersList();
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#retrieveRelatedSettings(Component, Point)
	 */
	public void retrieveRelatedSettings(Component source, Point location)
	{
		if (model.getState() == DISCARDED) return;
		Map m = model.getRenderingSettings();
		view.setLocationAndSource(source, location);
		if (m == null) model.fireRenderingSettingsRetrieval();
		else view.showUsersList();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setGridMagnificationFactor(double)
	 */
	public void setGridMagnificationFactor(double factor)
	{
		if (model.getTabbedIndex() != ImViewer.GRID_INDEX)  return;
		view.setGridMagnificationFactor(factor);
		model.getBrowser().setGridRatio(factor);
		if (view.isLensVisible()) {
			view.setImageZoomFactor((float) model.getBrowser().getGridRatio());
			view.scrollLens();	
		}
		/*
		if (view.isLensVisible() && 
				model.getTabbedIndex() == ImViewer.GRID_INDEX) {
			view.setImageZoomFactor((float) model.getBrowser().getGridRatio());
			view.scrollLens();	
		}
		*/
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setUserRndSettings(ExperimenterData)
	 */
	public void setUserRndSettings(ExperimenterData exp)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		try {
			view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			model.setUserSettings(exp);
			view.resetDefaults();
			renderXYPlane();
		} catch (Exception e) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set User rendering settings", "Could not apply " +
					"the settings set by "+exp.getFirstName()+
					" "+exp.getLastName());
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUserDetails()
	 */
	public ExperimenterData getUserDetails()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		return model.getUserDetails();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showView(int)
	 */
	public void showView(int index)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or" +
					"NEW state.");
		}
		if (index == RENDERER_INDEX || index == METADATA_INDEX) {
			view.showRenderer(false, index);
			controller.setPreferences();
		} else {
			view.showView(index);
			setSelectedPane(index);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setOriginalRndSettings()
	 */
	public void setOriginalRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		model.setOriginalRndSettings();
		view.resetDefaults();
		renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#projectImage(ProjectionRef)
	 */
	public void projectImage(ProjectionRef ref)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");

		if (model.getTabbedIndex() != PROJECTION_INDEX) return;
		if (ref == null) 
			throw new IllegalArgumentException("No projection object");
		model.fireImageProjection(ref.getStartZ(), ref.getEndZ(),
				view.getProjectionStepping(), view.getProjectionType(),
				view.getProjectionTypeName(), ref);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setProjectionPreview(Object)
	 */
	public void setProjectionPreview(Object image)
	{
		if (image == null) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Projection preview", "An error has occurred " +
					"while projecting the data.");
			return;
		}
		if (model.getTabbedIndex() != PROJECTION_INDEX) return;
		model.setRenderProjected(image);
		
		view.setLeftStatus();
		view.setPlaneInfoStatus();	
		if (!model.isPlayingMovie() && !model.isPlayingChannelMovie()) {
			if (view.isLensVisible()) view.setLensPlaneImage();
			view.createHistoryItem(model.getLastProjRef());
		}
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setContainers(Collection)
	 */
	public void setContainers(Collection containers)
	{
		/*
		if (model.getState() != LOADING_PROJECTION_DATA)
			throw new IllegalArgumentException("This method can only be " +
					"invoked in the LOADING_PROJECTION_DATA state.");
					*/
		//Create a modal dialog.	
		if (model.getTabbedIndex() != PROJECTION_INDEX) return;
		model.setContainers(containers);
		fireStateChange();
		if (projDialog == null) showProjectionDialog();
		else projDialog.setContainers(model.getContainers());
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#loadContainers()
	 */
	public void loadContainers()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		if (model.getTabbedIndex() != PROJECTION_INDEX) return;
		if (model.getContainers() == null) {
			model.fireContainersLoading();
			fireStateChange();
		} else showProjectionDialog();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setProjectedImage(ImageData, List, List, boolean)
	 */
	public void setProjectedImage(ImageData image, List<Integer> indexes,
				List<DataObject> containers, boolean applySettings)
	{
		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
		String message;
		if (image == null) {
			message = "An error occurred while creating the projected image.";
			un.notifyInfo("Projection", message);
			model.setState(READY);
		} else {
			if (applySettings) 
				model.fireProjectedRndSettingsCreation(indexes, image);
			else
				notifyProjection("The projected image has been " +
						"successfully created.", image);
			if (containers != null) {
				EventBus bus = ImViewerAgent.getRegistry().getEventBus();
				bus.post(new NodeToRefreshEvent(containers, true));
			}
		}
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setProjectedRenderingSettings(Boolean, ImageData)
	 */
	public void setProjectedRenderingSettings(Boolean result, ImageData image)
	{
		String message;
		if (result)
			message = "The projected image and the rendering settings\nhave" +
					" been successfully created.";
		else
			message = "An error has occurred while copying the " +
			"rendering settings of the projected image.";
		
		notifyProjection(message, image);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setContext(DataObject, DataObject)
	 */
	public void setContext(DataObject parent, DataObject grandParent)
	{
		if (model.getState() == DISCARDED) return;
		model.setContext(parent, grandParent);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setPlaneInfo(Collection)
	 */
	public void setPlaneInfo(Collection collection)
	{
		if (collection == null) return;
		model.setPlaneInfo(collection);
		view.setPlaneInfoStatus();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setImageData(ImageData)
	 */
	public void setImageData(ImageData data)
	{
		if (model.getState() != LOADING_IMAGE_DATA)
			throw new IllegalArgumentException("This method can only be " +
					"invoked in the LOADING_IMAGE_DATA.");
		if (data == null)
			throw new IllegalArgumentException("No image to set.");
		model.setImageData(data);
		view.setImageData();
		if (model.getMetadataViewer() != null)
			model.getMetadataViewer().addPropertyChangeListener(controller);
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSelectedPane(int)
	 */
	public void setSelectedPane(int index)
	{
		if (model.getState() == DISCARDED) return;
		int oldIndex = model.getTabbedIndex();
		if (oldIndex == index) return;
		
		view.setSelectedPane(index);
		if (oldIndex == ImViewer.GRID_INDEX) {
			int key = ColorModelAction.RGB_MODEL;
			if (GREY_SCALE_MODEL.equals(colorModel))
				key = ColorModelAction.GREY_SCALE_MODEL;
			setColorModel(key);
		}
		
		firePropertyChange(TAB_SELECTION_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
		if (oldIndex == ImViewer.PROJECTION_INDEX 
				&& index == ImViewer.VIEW_INDEX) {
			//check if settings have changed.
			//model.setLastSettingsRef(oldIndex);
			renderXYPlane();
		} else if (index == ImViewer.PROJECTION_INDEX && 
				oldIndex == ImViewer.VIEW_INDEX) {
			//model.setLastSettingsRef(oldIndex);
			double f = model.getZoomFactor();
			if (model.getBrowser().hasProjectedPreview()) {
				RndProxyDef def = model.getLastProjDef();
				boolean b = true;
				if (def != null) b = model.isSameSettings(def, false);
				if (!b || !isSameProjectionParam())
					renderXYPlane();
				else {
					BufferedImage image = model.getProjectedImage();
					if (image != null) {
						int x = (int) (model.getMaxX()*f);
						if (x != image.getWidth())
							model.setZoomFactor(f, false);
					}
				}
			} else {
				model.getBrowser().setZoomFactor(model.getZoomFactor(), false);
			}
		} else {
			renderXYPlane();
		}
		model.getBrowser().getUI().setVisible(true);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#loadMetadata()
	 */
	public void loadMetadata()
	{
		if (model.getState() == DISCARDED) return;
		model.loadMetadata();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setColorModel(int)
	 */
	public void setCompressionLevel()
	{
		switch (model.getState()) {
			case DISCARDED:
			case PROJECTION_PREVIEW:
				throw new IllegalArgumentException("This method cannot be " +
				"invoked in the DISCARDED or PROJECTION_PREVIEW state.");
		}
		int old = view.convertCompressionLevel();
		int index = view.getUICompressionLevel();
		if (old == index) return;
		view.setCompressionLevel(index);
		if (!model.isLargePlane())
			ImViewerFactory.setCompressionLevel(index);
		renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setColorModel(int)
	 */
	public void clearHistory()
	{
		switch (model.getState()) {
			case DISCARDED:
			case PROJECTION_PREVIEW:
				throw new IllegalArgumentException("This method cannot be " +
				"invoked in the DISCARDED or PROJECTION_PREVIEW state.");
		}
		model.clearHistory();
		view.clearHistory();
		setSelectedPane(VIEW_INDEX);
		//renderXYPlane();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isOriginalSettings()
	 */
	public boolean isOriginalSettings()
	{
		switch (model.getState()) {
			case DISCARDED:
				throw new IllegalArgumentException("This method cannot be " +
				"invoked in the DISCARDED state.");
		}
		return model.isOriginalSettings(true);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSettingsToPaste(RndProxyDef)
	 */
	public void setSettingsToPaste(RndProxyDef rndProxyDef)
	{
		if (model.getState() != PASTING)
			throw new IllegalArgumentException("This method should be " +
					"invoked in the PASTING state.");
		try {
			model.resetMappingSettings(rndProxyDef);
			view.resetDefaults();
			renderXYPlane();
		} catch (Exception e) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();

			Logger logger = ImViewerAgent.getRegistry().getLogger();
			LogMessage logMsg = new LogMessage();
			logMsg.print("Rendering Exception:");
			logMsg.println(e.getMessage());
			logMsg.print(e);
			logger.error(this, logMsg);
			un.notifyError("Paste Rendering settings", "An error occurred " +
					"while pasting the rendering settings.", e);
			view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getSortedChannelData()
	 */
	public List<ChannelData> getSortedChannelData()
	{
		return model.getChannelData();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showColorPicker(int)
	 */
	public void showColorPicker(int index)
	{
		if (model.getState() == DISCARDED) return;
		controller.showColorPicker(index);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#loadAllContainers()
	 */
	public void loadAllContainers()
	{
		if (model.getState() == DISCARDED) return;
		model.loadAllContainers();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#makeMovie()
	 */
	public void makeMovie()
	{
		if (model.getState() == DISCARDED) return;
		model.makeMovie();
	}

	/** Build the view.*/
	private void buildView()
	{
		int index = UnitBarSizeAction.DEFAULT_UNIT_INDEX;
		setUnitBarSize(UnitBarSizeAction.getValue(index));
		view.setDefaultScaleBarMenu(index);
		colorModel = model.getColorModel();
		view.buildComponents();
		view.onRndLoaded();
		if (model.isSeparateWindow()) {
			view.setOnScreen();
			view.toFront();
			view.requestFocusInWindow();
		} else {
			postViewerCreated(true, false);;
		}
		if (ImViewerAgent.isFastConnection())
			model.firePlaneInfoRetrieval();
		view.setLeftStatus();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#onRndLoaded(boolean)
	 */
	public void onRndLoaded(boolean reload)
	{
		if (model.getState() == DISCARDED) return;
		model.onRndLoaded();
		if (!reload) {
			if (model.isBigImage()) {
				view.setCompressionLevel(ToolBar.LOW);
				view.resetCompressionLevel(view.convertCompressionLevel());
				model.fireBirdEyeViewRetrieval(true);
				fireStateChange();
				return;
			}
			buildView();
		} else {
			//TODO
			//clean history, reset UI element
			model.resetHistory();
			view.switchRndControl();
		}
		renderXYPlane();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#onChannelSelection(int)
	 */
	public void onChannelSelection(int index)
	{
		int uiIndex = ImViewerUI.ALL_VIEW;
		firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
				Integer.valueOf(index-1), Integer.valueOf(index));
		view.setChannelsSelection(uiIndex);
		postActiveChannelSelection(ChannelSelection.CHANNEL_SELECTION);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#allowSplitView()
	 */
	public boolean allowSplitView()
	{
		switch (model.getState()) {
			case DISCARDED:
			case NEW:
				throw new IllegalArgumentException("This method cannot be " +
				"invoked in the DISCARDED or NEW state.");
			}
		return model.allowSplitView();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#selectAllChannels(boolean)
	 */
	public void selectAllChannels(boolean selection)
	{
		String cm = model.getColorModel();
		if (ImViewer.GREY_SCALE_MODEL.equals(cm)) return;
		for (int i = 0; i < model.getMaxC(); i++) {
			model.setChannelActive(i, selection);
			firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
					Integer.valueOf(i-1), Integer.valueOf(i));
			
			model.setSelectedChannel(i);		
		}
		view.setChannelsSelection(ImViewerUI.ALL_VIEW);
		renderXYPlane();
		postActiveChannelSelection(ChannelSelection.CHANNEL_SELECTION);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setMeasurements(Collection)
	 */
	public void setMeasurements(Collection result)
	{
		if (model.getState() == DISCARDED) return;
		model.setMeasurements(result);
		
		Collection measurements = model.getMeasurements();
		boolean enabled = true;
		if (measurements == null || measurements.size() == 0) {
			enabled = false;
		} else {
			Iterator i;
			i = measurements.iterator();
			FileAnnotationData fa;
			JCheckBox box;
			Object object;
			List<Object> l = new ArrayList<Object>();
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof FileAnnotationData)
					l.add(object);
			}
			if (l.size() == 0) enabled = false;
		}
		
		if (!enabled) {
			Action a = controller.getAction(ImViewerControl.MEASUREMENT_TOOL);
			a.setEnabled(false);
		}
		//Notify UI to build overlays 
		view.buildOverlays();
		
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#scrollToViewport(Rectangle)
	 */
	public void scrollToViewport(Rectangle bounds)
	{
		if (bounds == null) return;
		model.getBrowser().scrollTo(bounds, false);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isMappedImageRGB(List)
	 */
	public boolean isMappedImageRGB(List channels)
	{
		switch (model.getState()) {
			case DISCARDED:
				return false;
		}
		return model.isMappedImageRGB(channels);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#renderOverlays(int, boolean)
	 */
	public void renderOverlays(int index, boolean selected)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or " +
						"NEW state.");
		}
		view.renderOverlays(index, selected);
		if (!view.isOverlayActive()) return;
		Map<Long, Integer> m = null;
		if (index < 0) {
			if (selected) m = view.getSelectedOverlays();
		} else m = view.getSelectedOverlays();
		model.renderOverlays(m);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#renderOverlays(int, boolean)
	 */
	public void onChannelColorChanged(int index)
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED or " +
						"NEW state.");
		}
		view.setChannelColor(index, model.getChannelColor(index));
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isBigImage()
	 */
	public boolean isBigImage()
	{
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
			case LOADING_IMAGE_DATA:
				return false;
		}
		return model.isBigImage();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#refresh()
	 */
	public void refresh()
	{
		view.refresh();
		renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#close(boolean)
	 */
	public void close(boolean notifyUser)
	{
		if (model.getState() == DISCARDED) return;
		if (!view.isVisible()) return;
		if (notifyUser) postViewerCreated(false, false);
		switch (model.getState()) {
			//case DISCARDED:
			default:
				controller.setPreferences();
				//tmp store compression
				if (!model.isLargePlane())
				ImViewerFactory.setCompressionLevel(
						view.getUICompressionLevel());
				if (!saveOnClose(notifyUser)) {
					return;
				}
				if (notifyUser) {
					postViewerState(ViewerState.CLOSE);
					ImViewerRecentObject object = new ImViewerRecentObject(
						model.getSecurityContext(),
						model.getImageID(), model.getImageTitle(),
						getImageIcon());
					firePropertyChange(RECENT_VIEWER_PROPERTY, null, object);
				}
				
		}
		view.setVisible(false);
		view.dispose();
		discard();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#detach()
	 */
	public void detach()
	{
		if (model.getState() == DISCARDED) return;
		if (model.isSeparateWindow()) return; //Option not available.
		postViewerCreated(false, true);
		model.setSeparateWindow(true);
		view.rebuild();
		view.setOnScreen();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#canAnnotate()
	 */
	public boolean canAnnotate()
	{
		if (isUserOwner()) return true;
		return model.getImage().canAnnotate();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isUserOwner()
	 */
	public boolean isUserOwner()
	{
		return model.isUserOwner();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setRangeAllChannels(boolean)
	 */
	public void setRangeAllChannels(boolean absolute)
	{
		model.setRangeAllChannels(absolute);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setOwnerSettings()
	 */
	public void setOwnerSettings()
	{
		Map m = model.getRenderingSettings();
		if (m == null) model.fireOwnerSettingsRetrieval();
		else setRenderingSettings(m, model.getOwnerID());
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#includeROI()
	 */
	public boolean includeROI()
	{
		if (layers == null) return false;
		Iterator<JComponent> i = layers.iterator();
		while (i.hasNext()) {
			if (i.next() instanceof DrawingCanvasView) return true;
		}
		return false;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setBirdEyeView(Object)
	 */
	public void setBirdEyeView(BufferedImage image, boolean scaled)
	{
		switch (model.getState()) {
			case LOADING_BIRD_EYE_VIEW:
				boolean set = false;
				if (!view.isVisible()) {
					buildView();
					set = true;
				}
				model.setBirdEyeView(image, scaled);
				if (set)
					controller.setZoomFactor(model.getSelectedResolutionLevel());
				return;
		}
		
		model.setBirdEyeView(image, scaled);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getRows()
	 */
	public int getRows() { return model.getRows(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getColumns()
	 */
	public int getColumns() { return model.getColumns(); }
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getTiles()
	 */
	public Map<Integer, Tile> getTiles()
	{
		if (model.getState() == DISCARDED) return null;
		return model.getTiles();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setTile(int)
	 */
	public void setTileCount(int count)
	{
		if (model.getState() == DISCARDED) return;
		model.getBrowser().getUI().repaint();
		if (model.isTileLoaded(count)) {
			view.addComponentListener(controller);
			model.setState(READY);
			fireStateChange();
		}
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#loadTiles(Rectangle)
	 */
	public void loadTiles(Rectangle region)
	{
		if (model.getState() == DISCARDED || !model.isRendererLoaded()) 
		    return;
		if (region == null) 
			region = model.getBrowser().getVisibleRectangle();
		Map<Integer, Tile> tiles = getTiles();
    	if (tiles == null) return;
    	//invalidate images.
    	Dimension d = model.getTileSize();
    	int width = d.width;
    	int height = d.height;
    	int cs = region.x/width;
    	int rs = region.y/height;
    	int ih = region.width/width;
    	int iv = region.height/height;
    	int columns = getColumns();
    	int index;
    	Tile t;
    	
    	int h = rs+iv+1;
    	int w = cs+ih+1;
    	cs = cs-1;
    	rs = rs-1;
    	if (cs < 0) cs = 0;
    	if (rs < 0) rs = 0;
    	List<Tile> l = new ArrayList<Tile>();
    	List<Tile> toKeep = new ArrayList<Tile>();
    	for (int i = rs; i <= h; i++) {
			for (int j = cs; j <= w; j++) {
				index = i*columns+j;
				t = tiles.get(index);
				if (t != null) {
					if (t.isImageLoaded()) {
						if (!toKeep.contains(t))
							toKeep.add(t);
					} else {
						if (!l.contains(t))
							l.add(t);
					}
				}
			}
		}
    	List<Tile> toClear = new ArrayList<Tile>();
    	Iterator<Tile> k = tiles.values().iterator();
    	while (k.hasNext()) {
			t = k.next();
			if (t.isImageLoaded() && !toClear.contains(t) && 
					!toKeep.contains(t))
				toClear.add(t);
		}
    	model.clearTileImages(toClear);
		if (l.size() > 0) {
			view.removeComponentListener(controller);
			model.fireTileLoading(l);
			fireStateChange();
		}
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getTiledImageSizeX()
	 */
	public int getTiledImageSizeX()
	{ 
		return model.getTiledImageSizeX();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getTiledImageSizeY()
	 */
	public int getTiledImageSizeY()
	{ 
		return model.getTiledImageSizeY();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#cancelInit()
	 */
	public void cancelInit()
	{
		switch (model.getState()) {
			case LOADING_RND:
				if (model.isBigImage()) {
					model.cancelBirdEyeView(); 
					view.dispose();
					fireStateChange();
				} else {
					model.cancelRendering();
					discard();
					fireStateChange();
				}
				break;
			case LOADING_BIRD_EYE_VIEW:
				model.cancelBirdEyeView(); 
				view.dispose();
				fireStateChange();
		}
	}

	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext()
	{ 
		return model.getSecurityContext();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isCompressed()
	 */
	public boolean isCompressed()
	{
		return model.getCompressionLevel() != RenderingControl.UNCOMPRESSED;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#onUpdatedChannels(List)
	 */
	public void onUpdatedChannels(List<ChannelData> channels)
	{
		model.setChannels(channels);
		view.onChannelUpdated();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDisplayMode()
	 */
	public int getDisplayMode() { return model.getDisplayMode(); }
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsID()
	 */
	public long getPixelsID() { return model.getPixelsID(); }
	
	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getSelectedResolutionLevel()
	 */
	public int getSelectedResolutionLevel()
	{
		return model.getSelectedResolutionLevel();
	}
	
	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getResolutionLevels()
	 */
	public int getResolutionLevels()
	{
		return model.getResolutionLevels();
	}

	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getResolutionLevels()
	 */
	public int getSelectedBin() { return model.getSelectedBin(); }

	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxLifetimeBin()
	 */
	public int getMaxLifetimeBin() { return model.getMaxLifetimeBin(); }

	/**
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#getRealSelectedT()()
     */
    public int getRealSelectedT() { return model.getRealSelectedT(); }

	/** 
	 * Overridden to return the name of the instance to save. 
	 * @see #toString()
	 */
	public String toString()
	{ 
		return "Image's Settings: "+EditorUtil.truncate(model.getImageName());
	}

    /**
     * Implemented as specified by the {@link ImViewer} interface.
     * 
     * @see ImViewer#isInterpolation()
     */
    public boolean isInterpolation() {
        return model.isInterpolation();
    }

    /**
     * Implemented as specified by the {@link ImViewer} interface.
     * 
     * @see ImViewer#setInterpolation(boolean)
     */
    public void setInterpolation(boolean interpolation) {
        model.setInterpolation(interpolation);
    }

    /**
     * Implemented as specified by the {@link ImViewer} interface.
     * 
     * @see ImViewer#setImageAcquisitionData(ImageAcquisitionData)
     */
    public void setImageAcquisitionData(ImageAcquisitionData data) {
        this.acquisitionData = data;
        view.setMagnificationStatus();
    }

    /**
     * Implemented as specified by the {@link ImViewer} interface.
     * 
     * @see ImViewer#getImageAcquisitionData()
     */
    public ImageAcquisitionData getImageAcquisitionData() {
        return this.acquisitionData;
    }

    void onSettingsChanged()
    {
        view.resetDefaults();
    }
}
