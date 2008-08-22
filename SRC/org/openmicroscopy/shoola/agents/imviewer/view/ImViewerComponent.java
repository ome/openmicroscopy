/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerComponent
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ChannelSelection;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurePlane;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerState;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PlayMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.PreferencesDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.SaveEventBox;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ExperimenterData;
import pojos.ImageData;

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
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
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

	/** List of active channels before switching between color mode. */
	private List                			historyActiveChannels;

	/** Collection of events to display. */
	private Map<String, SaveRelatedData>	events;
	
	/** Flag indicating that a new z-section or timepoint is selected. */
	private boolean							newPlane;

	/** Listener attached to the rendering node. */
	private MouseAdapter					nodeListener;

	/** Flag indicating that the rendering settings have been modified. */
	private boolean							rndToSave;
	
	/** 
	 * Flag indicating that the rendering settings have been saved
	 * before copying the 
	 */
	private boolean							saveBeforeCopy;

	/** The possible projections options. */
	private Map<Integer, String>			projections;
	
	/** The projection dialog. */
	private ProjectionDialog				projection;
	
	/** Creates an history item. */
	private void createHistoryItem()
	{
		//if (!addHistoryItem) return;
		if (model.isHistoryItemReplacement()) {
			model.setHistoryItemReplacement(false);
			return;
		}
		rndToSave = true;
		HistoryItem node = model.createHistoryItem();
		node.addPropertyChangeListener(controller);
		//add Listener to node.
		model.setHistoryItemReplacement(false);
		if (nodeListener == null) {
			nodeListener = new MouseAdapter() {

				public void mousePressed(MouseEvent evt) {
					HistoryItem item = findParentDisplay(evt.getSource());
					try {
						if (!model.isHistoryItemReplacement()) {
							HistoryItem node = model.createHistoryItem();
							node.addPropertyChangeListener(controller);
							view.addHistoryItem(node);
							node.addMouseListenerToComponents(nodeListener);
							model.setHistoryItemReplacement(true);
						}
						List nodes = model.getHistory();
						Iterator i = nodes.iterator();
						while (i.hasNext()) {
							((HistoryItem) i.next()).setHighlight(null);
						}
						item.setHighlight(Color.BLUE);
						view.setCursor(Cursor.getPredefinedCursor(
								Cursor.WAIT_CURSOR));
						model.resetMappingSettings(item.getRndSettings(), true);
						view.setCursor(Cursor.getPredefinedCursor(
								Cursor.DEFAULT_CURSOR));
						renderXYPlane();
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			};
		}
		node.addMouseListenerToComponents(nodeListener);
		view.addHistoryItem(node);
	}
	
	/** 
	 * Returns the description displayed in the status bar.
	 * 
	 * @return See above
	 */
	private String getStatusText()
	{
		String text = "";
		text += "Z="+(model.getDefaultZ()+1)+"/"+(model.getMaxZ()+1);
		text += " T="+(model.getDefaultT()+1)+"/"+(model.getMaxT()+1);
		return text;
	}

	/**
	 * Posts a {@link MeasurePlane} event to indicate that a new plane is
	 * rendered or a new magnification factor has been selected.
	 */
	private void postMeasurePlane()
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		MeasurePlane event = new MeasurePlane(model.getPixelsID(), 
				model.getDefaultZ(), model.getDefaultT(), 
				model.getZoomFactor());
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
	 * a new channel is selected or deselected; or that a channel is mapped
	 * to a new color.
	 * 
	 * @param index One of the constants defined by {@link ChannelSelection}.
	 */
	private void postActiveChannelSelection(int index)
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		ChannelSelection event = new ChannelSelection(model.getPixelsID(), 
				model.getActiveChannelsMap(), index);
		bus.post(event);
	}

	/**
	 * Finds the first {@link HistoryItem} in <code>x</code>'s containement
	 * hierarchy.
	 * 
	 * @param x A component.
	 * @return The parent {@link HistoryItem} or <code>null</code> if none
	 *         was found.
	 */
	private HistoryItem findParentDisplay(Object x)
	{
		while (true) {
			if (x instanceof HistoryItem) return (HistoryItem) x;
			if (x instanceof JComponent) x = ((JComponent) x).getParent();
			else break;
		}
		return null;
	}
	
	/** 
	 * Displays message bebofe closing the viewer. 
	 * Returns <code>true</code> if we need to close the viewer,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean saveOnClose()
	{
		if (saveBeforeCopy) {
			HistoryItem item = model.getFirstHistoryItem();
			try {
				model.resetMappingSettings(item.getRndSettings(), false);
				model.saveRndSettings();
			} catch (Exception e) {
				LogMessage logMsg = new LogMessage();
				logMsg.println("Cannot save rendering settings. ");
				logMsg.print(e);
				ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
			}
		}
		boolean showBox = false;
		MessageBox msg = new MessageBox(view, "Save Data", 
						"Before closing the viewer, do you want to save: ");
		msg.addCancelButton();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JCheckBox rndBox = null;
		if (rndToSave) {
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
		msg.addBodyComponent(p);
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
		if (!showBox) return true;
		int option = msg.centerMsgBox();
		if (option == MessageBox.YES_OPTION) {
			if (rndBox != null && rndBox.isSelected()) {
				try {
					model.saveRndSettings();
				} catch (Exception e) {
					LogMessage logMsg = new LogMessage();
					logMsg.println("Cannot save rendering settings. ");
					logMsg.print(e);
					ImViewerAgent.getRegistry().getLogger().error(this, logMsg);
				}
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
		} else if (option == MessageBox.CANCEL) return false;
		return true;
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	ImViewerComponent(ImViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new ImViewerControl();
		view = new ImViewerUI(model.getImageName()+
				" [ID: "+model.getImageID()+"]");
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
	ImViewerModel getModel() { return model; }

	/**
	 * Sets the ids used to copy rendering settings.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rndSettings 	The rendering settings to copy. 
	 * 						Mustn't be <code>null</code>.
	 */
	void setRndSettings()
	{
		model.copyRndSettings();
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
	 * Returns <code>true</code> if there are annotations to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAnnotationToSave() 
	{ 
		return model.hasMetadataToSave();
	}
	
	/**
	 * Returns <code>true</code> if there are rendering settings to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndToSave() { return rndToSave; }
	
	/**
	 * Returns the id of the pixels set thie viewer is for.
	 * 
	 * @return See above.
	 */
	long getPixelsID() { return model.getPixelsID(); }
	
	/**
	 * Returns the title associated to the viewer.
	 * 
	 * @return See above.
	 */
	String getTitle() { return view.getTitle(); }
	
	/** Resets the rendering engine. */
	void reset()
	{
		//TODO: check state.
		model.fireRenderingControlResetting();
		fireStateChange();
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#activate(RndProxyDef, long)
	 */
	public void activate(RndProxyDef settings, long userID)
	{
		int state = model.getState();
		switch (state) {
			case NEW:
				model.setAlternativeSettings(settings, userID);
				model.fireRenderingControlLoading(model.getPixelsID());
				fireStateChange();
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
			default:
				view.deIconify();
			UIUtilities.centerOnScreen(view);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#discard()
	 */
	public void discard()
	{
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_RENDERING_CONTROL:
				model.discard();
				fireStateChange();
				break;
			default:
				controller.setPreferences();
				if (!saveOnClose()) return;
				postViewerState(ViewerState.CLOSE);
				model.discard();
				fireStateChange();
		}
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
		view.setStatus(description);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setZoomFactor(double)
	 */
	public void setZoomFactor(double factor, int zoomIndex)
	{
		if (factor != -1 && (factor > ZoomAction.MAX_ZOOM_FACTOR ||
				factor < ZoomAction.MIN_ZOOM_FACTOR))
			throw new IllegalArgumentException("The zoom factor is value " +
					"between "+ZoomAction.MIN_ZOOM_FACTOR+" and "+
					ZoomAction.MAX_ZOOM_FACTOR);
		model.setZoomFitToWindow(factor == -1);
		model.setZoomFactor(factor);
		view.setZoomFactor(factor, zoomIndex);
		if (view.isLensVisible() && 
				model.getTabbedIndex() == ImViewer.VIEW_INDEX) {
			view.setImageZoomFactor((float) model.getZoomFactor());
			view.scrollLens();	
		}
		controller.setPreferences();
		postMeasurePlane();
	}

	/**
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isZoomFitToWindow()
	 */
	public boolean isZoomFitToWindow() { return model.getZoomFitToWindow(); }

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setColorModel(int)
	 */
	public void setColorModel(int key)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		try {
			rndToSave = true;
			Iterator i;
			List channels = model.getActiveChannels();
			int index;
			switch (key) {
				case ColorModelAction.GREY_SCALE_MODEL:
					historyActiveChannels = model.getActiveChannels();
					model.setColorModel(GREY_SCALE_MODEL);
					
					if (channels != null && channels.size() > 1) {
						i = channels.iterator();
						int j = 0;
						while (i.hasNext()) {
							index = ((Integer) i.next()).intValue();
							setChannelActive(index, j == 0);
							j++;
						}
					} else if (channels == null || channels.size() == 0) {
						//no channel so one will be active.
						setChannelActive(0, true);
					}
					if (channels != null) {
						i = channels.iterator();
						while (i.hasNext()) {
							index = ((Integer) i.next()).intValue();
							view.setChannelActive(index, ImViewerUI.GRID_ONLY);
						}
					}
					break;
				case ColorModelAction.RGB_MODEL:
				case ColorModelAction.HSB_MODEL:
					model.setColorModel(HSB_MODEL);
					if (historyActiveChannels != null && 
							historyActiveChannels.size() != 0) {
						i = historyActiveChannels.iterator();
						while (i.hasNext()) {
							index = ((Integer) i.next()).intValue();
							setChannelActive(index, true);
						}
	
					} else {
						if (channels == null || channels.size() == 0) {
							//no channel so one will be active.
							setChannelActive(0, true);
						} else {
							i = channels.iterator();
							while (i.hasNext()) {
								index = ((Integer) i.next()).intValue();
								setChannelActive(index, true);
							}
						}
					}
					break;
				default:
					throw new IllegalArgumentException("Color model not " +
					"supported");
			}
			//need
			firePropertyChange(COLOR_MODEL_CHANGE_PROPERTY, new Integer(1), 
					new Integer(-1));
			view.setColorModel(key);
			renderXYPlane();
		} catch (Exception ex) {
			reload(ex);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setSelectedXYPlane(int, int)
	 */
	public void setSelectedXYPlane(int z, int t)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		int defaultZ = model.getDefaultZ();
		int defaultT = model.getDefaultT();

		if (defaultZ == z && defaultT == t) return;
		try {
			if (defaultZ != z) {
				firePropertyChange(ImViewer.Z_SELECTED_PROPERTY, 
						new Integer(defaultZ), new Integer(z));
			}
			if (defaultT != t) {
				firePropertyChange(ImViewer.T_SELECTED_PROPERTY, 
						new Integer(defaultT), new Integer(t));
			}
			newPlane = true;
			model.setSelectedXYPlane(z, t);
			renderXYPlane();
		} catch (Exception ex) {
			reload(ex);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setImage(BufferedImage)
	 */
	public void setImage(BufferedImage image)
	{
		if (model.getState() != LOADING_IMAGE) 
			throw new IllegalStateException("This method can only be invoked " +
			"in the LOADING_IMAGE state.");
		if (newPlane) postMeasurePlane();
		newPlane = false;
		model.setImage(image);
		/*
		boolean init = model.isInitMagnificationFactor();
		double factor = model.setImage(image);
		Browser browser = model.getBrowser();
		if (factor > 0) {
			double f = Math.round(factor*100)/100.0;
	    	if (f < ZoomAction.MIN_ZOOM_FACTOR)
	    		f = ZoomAction.MIN_ZOOM_FACTOR;
	    	if (f > ZoomAction.MAX_ZOOM_FACTOR)
	    		f = ZoomAction.MAX_ZOOM_FACTOR;
	    	setZoomFactor(f, ZoomAction.getIndex(f));
	    	browser.setComponentsSize((int) (model.getMaxX()*f), 
	    			(int) (model.getMaxY()*f));
		}
		if (!init) browser.setComponentsSize(model.getMaxX(), model.getMaxY());
		
		*/
		view.setStatus(getStatusText());
		if (model.isPlayingChannelMovie())
			model.setState(ImViewer.CHANNEL_MOVIE);
		if (!model.isPlayingMovie())
			view.setIconImage(model.getImageIcon());
		if (view.isLensVisible()) view.setLensPlaneImage();
		List history = model.getHistory();
		if (history == null || history.size() == 0) {
			createHistoryItem();
			HistoryItem node = (HistoryItem) model.getHistory().get(0);
			node.allowClose(false);
			rndToSave = false;
			ViewerPreferences pref = ImViewerFactory.getPreferences();
			if (pref != null) {
				if (pref.isFieldSelected(ViewerPreferences.RENDERER) &&
						pref.isRenderer()) {
					if (image != null)
						view.setRestoreSize(image.getWidth(), image.getHeight());
					//boolean oldValue = view.isHistoryShown();
					view.showRenderer(true);
					//firePropertyChange(HISTORY_VISIBLE_PROPERTY, oldValue, 
					//					!oldValue);
				}
				if (pref.isFieldSelected(ViewerPreferences.HISTORY) &&
					pref.isHistory()) {
					if (image != null)
						view.setRestoreSize(image.getWidth(), 
								image.getHeight());
					//boolean oldValue = view.isHistoryShown();
					view.showHistory(true);
					//firePropertyChange(HISTORY_VISIBLE_PROPERTY, oldValue, 
					//					!oldValue);
				}
				if (pref.isFieldSelected(ViewerPreferences.ZOOM_FACTOR)) {
					int index = pref.getZoomIndex();
					double f = ZoomAction.getZoomFactor(index);
					setZoomFactor(f, index);
				}
			}
		} //else createHistoryItem();
		
		view.setCursor(Cursor.getDefaultCursor());
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		//if (model.getState() != READY || model.getState() != CHANNEL_MOVIE) 
			//return;
		try {
			model.playMovie(play);
			view.playChannelMovie(!play);
			if (!play) {
				displayChannelMovie();
				controller.setHistoryState(READY);
			}
			fireStateChange();
		} catch (Exception ex) {
			reload(ex);
		} 
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setChannelColor(int, Color)
	 */
	public void setChannelColor(int index, Color c)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		try {
			rndToSave = true;
			model.setChannelColor(index, c);
			view.setChannelColor(index, c);
			if (!model.isChannelActive(index)) {
				setChannelActive(index, true);
				view.setChannelActive(index, ImViewerUI.GRID_AND_VIEW);
			}

			if (GREY_SCALE_MODEL.equals(model.getColorModel()))
				setColorModel(ColorModelAction.RGB_MODEL);
			else renderXYPlane();
		} catch (Exception e) {
			Registry reg = ImViewerAgent.getRegistry();
			LogMessage msg = new LogMessage();
			msg.println("Cannot set the color of channel "+index);
			msg.print(e);
			reg.getLogger().error(this, msg);
			reg.getUserNotifier().notifyError("Set channel color", 
					"Cannot set the color of channel "+index, e);
		}

		//view.setChannelColor(index, c);
		firePropertyChange(CHANNEL_COLOR_CHANGE_PROPERTY, new Integer(index-1),
				new Integer(index));
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		//depends on model
		try {
			int uiIndex = -1;
			if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
				if (model.getTabbedIndex() == ImViewer.GRID_INDEX) {
					//if (view.getActiveChannelsInGrid().size() == 1) b = true;
					/*
					model.setChannelActive(index, b); 
					if (b) 
						firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
								new Integer(index-1), new Integer(index));
								*/
					//view.setChannelsSelectionGridView();
					//uiIndex = ImViewerUI.GRID_ONLY;
					List<Integer> l = new ArrayList<Integer>();
					List selectedChannels = view.getActiveChannelsInGrid();
					for (int i = 0; i < model.getMaxC(); i++) {
						if (i == index) {
							if (b) l.add(i);
						} else {
							if (selectedChannels.contains(i))
								l.add(i);
						}
					}
					historyActiveChannels = l;
					view.setChannelsSelection(l);
				} else {
					if (model.isChannelActive(index)) return;
					boolean c;
					for (int i = 0; i < model.getMaxC(); i++) {
						c = i == index;
						model.setChannelActive(i, c);  
						if (c) 
							firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
									new Integer(index-1), new Integer(index));
					}
					uiIndex = ImViewerUI.VIEW_ONLY;
					//view.setChannelsSelection();
				}
			} else {
				uiIndex = ImViewerUI.GRID_AND_VIEW;
				model.setChannelActive(index, b);
				firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
						new Integer(index-1), new Integer(index));
				//view.setChannelsSelection();
			}
			rndToSave = true;
			view.setChannelsSelection(uiIndex);
			//view.setChannelsSelection();
			renderXYPlane();
			postActiveChannelSelection(ChannelSelection.CHANNEL_SELECTION);
		} catch (Exception ex) {
			reload(ex);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setRenderingControl(RenderingControl)
	 */
	public void setRenderingControl(RenderingControl result)
	{
		if (model.getState() != LOADING_RENDERING_CONTROL)
			throw new IllegalStateException(
					"This method can't be invoked in the " +
					"LOADING_RENDERING_CONTROL.");
		Object rnd = model.getRenderer();
		model.setRenderingControl(result);
		//Register the renderer
		model.getRenderer().addPropertyChangeListener(controller);
		if (rnd == null) { //initial 
			//LoadingWindow window = view.getLoadingWindow();
			//window.setStatus("rendering settings. Loading: metadata");
			//window.setProgress(50);
			//User preference
			view.buildComponents();
			view.setOnScreen();
			view.setStatus(getStatusText());
		} else {
			//TODO
			//clean history, reset UI element
			model.resetHistory();
			view.switchRndControl();
			model.getRenderer().switchRndControl();
			/*
			model.getRenderer().resetRndSettings();
			view.resetDefaults();
			*/
		}
		
		renderXYPlane();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#renderXYPlane()
	 */
	public void renderXYPlane()
	{
		//Check state
		switch (model.getState()) {
			case NEW:
			case DISCARDED:
				throw new IllegalStateException(
				"This method can't be invoked in the DISCARDED, NEW or" +
				" state.");
		} 
		model.fireImageRetrieval();
		newPlane = false;
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setChannelActive(int, boolean)
	 */
	public void setChannelActive(int index, boolean b)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW or" +
				"LOADING_RENDERING_CONTROL state.");
		}
		rndToSave = true;
		try {
			model.setChannelActive(index, b);
			if (b)
				firePropertyChange(CHANNEL_ACTIVE_PROPERTY, 
						new Integer(index-1), new Integer(index));
		} catch (Exception ex) {
			reload(ex);
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#displayChannelMovie()
	 */
	public void displayChannelMovie()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW or" +
				"LOADING_RENDERING_CONTROL state.");
		}
		view.setChannelsSelection(ImViewerUI.GRID_AND_VIEW);
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxC();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxT()
	 */
	public int getMaxT()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
					"LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxT();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getMaxZ()
	 */
	public int getMaxZ()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
					"LOADING_RENDERING_CONTROL state.");
		}
		return model.getMaxZ();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showRenderer()
	 */
	public void showRenderer()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
					"LOADING_RENDERING_CONTROL state.");
		}
		//boolean oldValue = view.isHistoryShown();
		view.showRenderer(false);
		controller.setPreferences();
		//view.setDisplayMode(ImViewerUI.RENDERER, false);
		
		//firePropertyChange(HISTORY_VISIBLE_PROPERTY, oldValue, !oldValue);
		//JFrame f = model.getRenderer().getUI();
		//UIUtilities.setLocationRelativeToAndShow(view, f);
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
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
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
					"This method can't be invoked in the DISCARDED, NEW state.");
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
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
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
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
		}
		return model.getDefaultT();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageComponents(String)
	 */
	public List getImageComponents(String colorModel)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW or" +
				"LOADING_RENDERING_CONTROL state.");
		}
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List l = model.getActiveChannels();
		if (l.size() < 2) return null;
		Iterator i = l.iterator();
		int index;
		String oldColorModel = model.getColorModel();
		List<BufferedImage> images = new ArrayList<BufferedImage>(l.size());
		try {
			model.setColorModel(colorModel);
			BufferedImage img = null, splitImage;
			while (i.hasNext()) {
				index = ((Integer) i.next()).intValue();
				for (int j = 0; j < model.getMaxC(); j++)
					model.setChannelActive(j, j == index); 
				splitImage = model.getSplitComponentImage();
				if (splitImage != null)
					img = Factory.magnifyImage(splitImage, 
							model.getZoomFactor(), 0);
				images.add(img);
			}
			model.setColorModel(oldColorModel);
			i = l.iterator();
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
			model.setChannelActive(index, true);
			}
		} catch (Exception ex) {
			reload(ex);
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		int index;
		List active = model.getActiveChannels();
		int maxC = model.getMaxC();
		List<BufferedImage> images = new ArrayList<BufferedImage>(maxC);

		try {
			if (model.getColorModel().equals(GREY_SCALE_MODEL)) {
				active = view.getActiveChannelsInGrid();
				//Iterator i = active.iterator();
				for (int k = 0; k < maxC; k++) {
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
				Iterator i = active.iterator();

				while (i.hasNext()) { //reset values.
					index = ((Integer) i.next()).intValue();
					model.setChannelActive(index, true);
				}
				if (active.size() != 0) {
					model.setColorModel(HSB_MODEL);
					images.add(model.getSplitComponentImage());
					model.setColorModel(GREY_SCALE_MODEL);
				}
				
				/*
				while (i.hasNext()) { //reset values.
					index = ((Integer) i.next()).intValue();
					model.setChannelActive(index, false);
				}
				*/
				active = model.getActiveChannels();
				while (i.hasNext()) { //reset values.
					index = ((Integer) i.next()).intValue();
					model.setChannelActive(index, true);
				}
			} else {
				Iterator i;
				for (int j = 0; j < maxC; j++) {
					if (model.isChannelActive(j)) {
						for (int k = 0; k < maxC; k++) {
							model.setChannelActive(k, k == j);
						}
						images.add(model.getSplitComponentImage());
						i = active.iterator();
						while (i.hasNext()) { //reset values.
							index = ((Integer) i.next()).intValue();
							model.setChannelActive(index, true);
						}
					} else {
						images.add(null);
					}
				}
			}

		} catch (Exception ex) {
			reload(ex);
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
						"NEW or LOADING_RENDERING_CONTROL state.");
		}
		//view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (!model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		int index;
		List active = view.getActiveChannelsInGrid();
		BufferedImage image = null;
		try {
			Iterator i = active.iterator();
			for (int k = 0; k < model.getMaxC(); k++) {
				model.setChannelActive(k, false);
			}
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
				model.setChannelActive(index, true);
			}
			if (active.size() != 0) {
				model.setColorModel(HSB_MODEL);
				image = model.getSplitComponentImage();
				model.setColorModel(GREY_SCALE_MODEL);
			}
			
			active = model.getActiveChannels();
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
				model.setChannelActive(index, true);
			}
		} catch (Exception e) {
			reload(e);
		}
		return image;
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageForGrid(int)
	 */
	public BufferedImage getImageForGrid(int index)
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, " +
				"NEW or LOADING_RENDERING_CONTROL state.");
		}
		if (!model.isChannelActive(index)) return null;
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		BufferedImage image = null;

		try {

			for (int k = 0; k < model.getMaxC(); k++) {
				model.setChannelActive(k, k == index);
			}
			image = model.getSplitComponentImage();
			List active = model.getActiveChannels();
			Iterator i = active.iterator();
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
				model.setChannelActive(index, true);
			}


		} catch (Exception ex) {
			reload(ex);
		}
		return image;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getDisplayedImage()
	 */
	public BufferedImage getDisplayedImage()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		return model.getDisplayedImage();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeX()
	 */
	public float getPixelsSizeX()
	{
		switch (model.getState()) {
		case NEW:
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
		}
		return model.getPixelsSizeX();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeY()
	 */
	public float getPixelsSizeY()
	{
		switch (model.getState()) {
		case NEW:
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
		}
		return model.getPixelsSizeY();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getPixelsSizeZ()
	 */
	public float getPixelsSizeZ()
	{
		switch (model.getState()) {
			case NEW:
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		return model.getPixelsSizeZ();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getViewTitle()
	 */
	public String getViewTitle()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		return view.getTitle();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getChannelMetadata(int)
	 */
	public ChannelMetadata getChannelMetadata(int index)
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED, NEW " +
						"or LOADING_RENDERING_CONTROL state.");
		}
		return model.getActiveChannels();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isUnitBar()
	 */
	public boolean isUnitBar()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		return model.isUnitBar();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setUnitBar(boolean)
	 */
	public void setUnitBar(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
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
		// TODO Check state
		return model.getChannelColor(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setUnitBarSize(double)
	 */
	public void setUnitBarSize(double size)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		model.getBrowser().setUnitBarSize(size);
		controller.setPreferences();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showUnitBarSelection()
	 */
	public void showUnitBarSelection()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		UnitBarSizeDialog d = new UnitBarSizeDialog(view);
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
		view.setStatus(getStatusText());
		view.resetDefaults(); 
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarValue()
	 */
	public String getUnitBarValue()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		return model.getBrowser().getUnitBarValue();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarSize()
	 */
	public double getUnitBarSize()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		return model.getBrowser().getUnitBarSize();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getUnitBarColor()
	 */
	public Color getUnitBarColor()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		if (model.getBrowser() == null) return null;
		return model.getBrowser().getUnitBarColor();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getImageIcon()
	 */
	public ImageIcon getImageIcon()
	{
		return new ImageIcon(model.getImageIcon());
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isLensVisible()
	 */
	public boolean isLensVisible()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		return view.isLensVisible();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setLensVisible(boolean)
	 */
	public void setLensVisible(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		view.setLensVisible(b, model.getTabbedIndex());
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getZoomedLensImage()
	 */
	public BufferedImage getZoomedLensImage()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		return view.getZoomedLensImage();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showMenu(int, Component, Point)
	 */
	public void showMenu(int menuID, Component source, Point location)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("The method cannot be invoked in " +
			"the DISCARDED state.");
		if (source == null) throw new IllegalArgumentException("No component.");
		if (location == null) throw new IllegalArgumentException("No point.");
		switch (menuID) {
			case COLOR_PICKER_MENU:
				if (model.getMaxC() == 1) controller.showColorPicker(0);
				else view.showMenu(menuID, source, location);
				break;
			case CATEGORY_MENU:
				view.showMenu(menuID, source, location);
				break;
			default:
				throw new IllegalArgumentException("Menu not supported.");
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setRenderingControlReloaded(RenderingControl, boolean)
	 */
	public void setRenderingControlReloaded(boolean updateView)
	{
		if (model.getState() != LOADING_RENDERING_CONTROL)
			throw new IllegalStateException("The method can only be invoked " +
			"in the LOADING_RENDERING_CONTROL state.");
		if (updateView) {
			model.getRenderer().resetRndSettings();
			view.resetDefaults();
		}
		renderXYPlane();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#reload(Exception)
	 */
	public void reload(Exception e)
	{
		Logger logger = ImViewerAgent.getRegistry().getLogger();
		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
		if (e instanceof RenderingServiceException) {
			RenderingServiceException rse = (RenderingServiceException) e;
			LogMessage logMsg = new LogMessage();
			logMsg.print("Rendering Exception:");
			logMsg.println(rse.getExtendedMessage());
			logMsg.print(rse);
			logger.error(this, logMsg);
			if (newPlane) {
				MessageBox msg = new MessageBox(view, "Invalid Plane", 
						"The selected plane contains invalid value. " +
				"Do you want to reload it?");
				if (msg.centerMsgBox() == MessageBox.YES_OPTION) {
					logger.debug(this, "Reload rendering Engine.");
					model.fireRenderingControlReloading();
					fireStateChange();
				} else {
					logger.debug(this, e.getMessage());
					discard();
				}
				/*
				if (msg.centerMsgBox() == MessageBox.YES_OPTION) 
					discard();
				else {
					//TODO: reset to previous history item.
					setImage(null);
				}
				*/
			} else {
				un.notifyError(ImViewerAgent.ERROR, logMsg.toString(), 
						e.getCause());
			}
			newPlane = false;
		} else if (e instanceof DSOutOfServiceException) {
			logger.debug(this, "Reload rendering Engine.");
			model.fireRenderingControlReloading();
			fireStateChange();
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED," +
						" NEW or LOADING_RENDERING_CONTROL state.");
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
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, NEW or" +
			"LOADING_RENDERING_CONTROL state.");
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
			case LOADING_RENDERING_CONTROL:
			case DISCARDED:
				return;
		}
		MoviePlayerDialog d = controller.getMoviePlayer();
		boolean doClick = false;
		if (visible) { // we have to play the movie
			controller.getAction(
					ImViewerControl.PLAY_MOVIE_T).setEnabled(false);
			controller.getAction(
					ImViewerControl.PLAY_MOVIE_Z).setEnabled(false);
			play = true;
			UIUtilities.setLocationRelativeToAndShow(view, d);
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
						controller.getAction(
								ImViewerControl.PLAY_MOVIE_T).setEnabled(!play);
						break;
					case PlayMovieAction.ACROSS_T:
						controller.getAction(
								ImViewerControl.PLAY_MOVIE_Z).setEnabled(!play);
						break;
					default:
						controller.getAction(
								ImViewerControl.PLAY_MOVIE_T).setEnabled(true);
						controller.getAction(
								ImViewerControl.PLAY_MOVIE_Z).setEnabled(true);
						
				}
				doClick = true;
				if (index != -1) d.setMovieIndex(index);
				d.setTimeRange(model.getDefaultT(), model.getMaxT());
			}
		}
		
		model.setPlayingMovie(play, index);
		view.enableSliders(!play);
		controller.getAction(ImViewerControl.CHANNEL_MOVIE).setEnabled(!play);
		if (doClick) {
			if (play) {
				d.addPropertyChangeListener(
						MoviePlayerDialog.STATE_CHANGED_PROPERTY,
						controller);
				d.doClick(MoviePlayerDialog.DO_CLICK_PLAY);
			} else {
				d.removePropertyChangeListener(
						MoviePlayerDialog.STATE_CHANGED_PROPERTY,
						controller);
				d.doClick(MoviePlayerDialog.DO_CLICK_PAUSE);
			}
		} else {
			d.removePropertyChangeListener(
					MoviePlayerDialog.STATE_CHANGED_PROPERTY,
					controller);
		}
		if (!play) {
			model.setState(READY);
			fireStateChange();
		}
			
			
		/*
		controller.getAction(ImViewerControl.PLAY_MOVIE_Z).setEnabled(doClick);
		controller.getAction(ImViewerControl.PLAY_MOVIE_T).setEnabled(doClick);
		if (wasVisible)
			controller.getAction(ImViewerControl.PLAY_MOVIE_T).setEnabled(true);
			*/
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#getGridImage()
	 */
	public BufferedImage getGridImage()
	{
		//TODO:Check state
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
		case LOADING_RENDERING_CONTROL:
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED, " +
			"NEW or LOADING_RENDERING_CONTROL state.");
		}
		if (model.getColorModel().equals(GREY_SCALE_MODEL)) return null;
		List l = model.getActiveChannels();
		if (l.size() < 2) return null;
		Iterator i = l.iterator();
		int index;
		String oldColorModel = model.getColorModel();
		List<BufferedImage> images = new ArrayList<BufferedImage>(l.size());
		try {
			model.setColorModel(colorModel);
			while (i.hasNext()) {
				index = ((Integer) i.next()).intValue();
				for (int j = 0; j < model.getMaxC(); j++)
					model.setChannelActive(j, j == index); 
				images.add(view.createZoomedLensImage(
						model.getSplitComponentImage()));
			}
			model.setColorModel(oldColorModel);
			i = l.iterator();
			while (i.hasNext()) { //reset values.
				index = ((Integer) i.next()).intValue();
				model.setChannelActive(index, true);
			}
			//view.setLensPlaneImage(model.getOriginalImage());
		} catch (Exception ex) {
			reload(ex);
		}
		return images;
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isTextVisible()
	 */
	public boolean isTextVisible()
	{
		return model.isTextVisible();
	}

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
	 * @see ImViewer#showMeasurementTool()
	 */
	public void showMeasurementTool()
	{
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		MeasurementTool request = new MeasurementTool(model.getImageID(), 
				model.getPixelsID(), model.getImageName(), 
				model.getDefaultZ(), model.getDefaultT(),
				model.getActiveChannelsMap(), model.getZoomFactor(), 
				view.getBounds());
		bus.post(request);
		view.selectTabbedPane(ImViewer.VIEW_INDEX);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showImageDetails()
	 */
	public void showImageDetails()
	{
		model.showImageDetails();
		/*
		if (model.getState() != READY) return;
		ImageDetailsDialog d = new ImageDetailsDialog(view, model.getMaxX(), 
				model.getMaxY(), model.getPixelsSizeX(), model.getPixelsSizeY(), 
				model.getPixelsSizeZ());
		UIUtilities.centerAndShow(d);
		*/
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#addToView(JComponent)
	 */
	public void addToView(JComponent comp)
	{
		if (model.getState() != READY) return;
		if (comp == null) return;
		model.getBrowser().addComponent(comp, ImViewer.VIEW_INDEX);
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
		//TODO: check state
		return model.isChannelRed(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelGreen(int)
	 */
	public boolean isChannelGreen(int index)
	{
		return model.isChannelGreen(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#isChannelBlue(int)
	 */
	public boolean isChannelBlue(int index)
	{
		return model.isChannelBlue(index);
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
	 * @see ImViewer#addHistoryItem()
	 */
	public void addHistoryItem()
	{
		createHistoryItem();
		
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
			model.saveRndSettings();
			model.copyRenderingSettings();
			saveBeforeCopy = true;
		} catch (Exception e) {
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
				//throw new IllegalStateException(
				//"This method can't be invoked in the DISCARDED, NEW state.");
				return;
		}
		if (!model.hasRndToPaste()) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste rendering settings", "No rendering settings" +
							" to paste.");
			return;
		}

		try {
			view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			addHistoryItem();
			boolean b = model.resetSettings();
			if (b) {
				view.resetDefaults();
				renderXYPlane();
			} else {
				UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Paste rendering settings", "Pixels sets" +
				" not compatible.");
			}
			view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception e) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();

			Logger logger = ImViewerAgent.getRegistry().getLogger();
			LogMessage logMsg = new LogMessage();
			logMsg.print("Rendering Exception:");
			logMsg.println(e.getMessage());
			logMsg.print(e);
			logger.error(this, logMsg);
			un.notifyError("Paste Rendering settings", "An error occured " +
					"while pasting the rendering settings.", e);
			view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
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
    	try {
    		addHistoryItem();
    		model.resetDefaultRndSettings();
    		view.resetDefaults();
    		model.getRenderer().resetRndSettings();
			renderXYPlane();
		} catch (Exception ex) {
			model.removeLastHistoryItem();
			reload(ex);
		}
    }
    
    /** 
     * Implemented as specified by the {@link ImViewer} interface.
     * @see ImViewer#saveRndSettings()
     */
    public void saveRndSettings()
    {
    	try {
    		model.saveRndSettings();
    		rndToSave = false;
		} catch (Exception ex) {
			reload(ex);
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
	 * @see ImViewer#setRenderingSettings(Map)
	 */
	public void setRenderingSettings(Map map)
	{
		if (model.getState() == DISCARDED) return;
		model.setRenderingSettings(map);
		view.showUsersList();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#retrieveRelatedSettings(Component, Point)
	 */
	public void retrieveRelatedSettings(Component source, Point location)
	{
		//TODO: Check state
		Map m = model.getRenderingSettings();
		view.setLocationAndSource(source, location);
		if (m == null)
			model.fireRenderingSettingsRetrieval();
		else {
			view.showUsersList();
		}
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setGridMagnificationFactor(double)
	 */
	public void setGridMagnificationFactor(double factor)
	{
		//TODO: Check state
		view.setGridMagnificationFactor(factor);
		model.getBrowser().setGridRatio(factor);
		if (view.isLensVisible() && 
				model.getTabbedIndex() == ImViewer.GRID_INDEX) {
			view.setImageZoomFactor((float) model.getBrowser().getGridRatio());
			view.scrollLens();	
		}
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
			rndToSave = true;
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
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		view.showView(index);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#loadRenderingControl(long)
	 */
	public void loadRenderingControl(long pixelsID)
	{
		//TODO: check state.
		if (pixelsID == model.getPixelsID()) return;
		model.fireRenderingControlLoading(pixelsID);
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
    	try {
    		addHistoryItem();
    		model.setOriginalRndSettings();
    		view.resetDefaults();
    		model.getRenderer().resetRndSettings();
			renderXYPlane();
		} catch (Exception ex) {
			model.removeLastHistoryItem();
			reload(ex);
		}
		
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#showProjection()
	 */
	public void showProjection()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		if (projections == null) {
			projections = new LinkedHashMap<Integer, String>();
			projections.put(RenderingControl.MAX_INTENSITY, 
					"Maximum Intensity");
			projections.put(RenderingControl.MEAN_INTENSITY, 
					"Mean Intensity");
			projections.put(RenderingControl.SUM_INTENSITY, 
					"Sum Intensity");
		}
		if (projection == null) {
			projection = new ProjectionDialog(view, projections,
									model.getMaxZ()+1, 
									model.getBrowser().getBackgroundColor(),
									model.getImageName(), 
									model.getOriginalImage());
			projection.addPropertyChangeListener(controller);
			projection.setProjectedImage(model.getOriginalImage());
			UIUtilities.incrementRelativeToAndShow(view.getBounds(), 
					projection);
		} else {
			projection.setVisible(true);
		}
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
		if (projection == null) return;
		if (!projection.isVisible()) return;
		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
		un.notifyInfo("Projection preview", "Not yet implemented");
		projection.setVisible(false);
		projection.dispose();
		projection = null;
		//model.fireProjectImage(ref);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setRenderProjected(BufferedImage)
	 */
	public void setRenderProjected(BufferedImage image)
	{
		if (image == null) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Projection preview", "An error has occurred " +
					"while projecting the data.");
		}
		projection.setProjectedImage(image);
	}
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#projectionPreview(ProjectionRef)
	 */
	public void projectionPreview(ProjectionRef ref)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		if (projection == null) return;
		if (!projection.isVisible()) return;
		
		/*
		BufferedImage img = null;
		try {
			img = model.renderProjected(ref.getStartZ(), ref.getEndZ(), 
									ref.getStepping(), ref.getType());
		} catch (Exception e) {
			projection.setProjectedImage(null);
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Projection preview", "An error has occurred " +
					"while projecting the data.");
		}
		projection.setProjectedImage(img);
		*/
		model.fireRenderProjected(ref);
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setContainers(Collection)
	 */
	public void setContainers(Collection containers)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be invoked" +
					" in the DISCARDED state.");
		if (projection == null) return;
		if (!projection.isVisible()) return;
		projection.setContainers(containers);
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
		if (projection == null) return;
		if (!projection.isVisible()) return;
		model.fireContainersLoading();
	}

	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see ImViewer#setProjectedImage(ImageData)
	 */
	public void setProjectedImage(ImageData image)
	{
		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
		String message;
		if (image == null) 
			message = "An error has occurred while creating the " +
					"projected image.";
		else message = "The image, "+image.getName() +" has been successfully "
		       +"created";
		un.notifyInfo("Projection", message);
		projection.setVisible(false);
		projection.dispose();
		projection = null;
	}

}
