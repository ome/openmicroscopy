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
package org.openmicroscopy.shoola.agents.metadata.view;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.actions.AddAction;
import org.openmicroscopy.shoola.agents.metadata.actions.BrowseAction;
import org.openmicroscopy.shoola.agents.metadata.actions.MetadataViewerAction;
import org.openmicroscopy.shoola.agents.metadata.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.metadata.actions.RemoveAction;
import org.openmicroscopy.shoola.agents.metadata.actions.RemoveAllAction;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.util.ChannelSelectionDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisActivityParam;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.ChannelData;
import omero.gateway.model.ImageData;

/** 
 * The MetadataViewer's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class MetadataViewerControl
	implements ChangeListener, PropertyChangeListener
{

	/** Identifies the <code>Refresh</code> action. */
	static final Integer		REFRESH = Integer.valueOf(0);
	
	/** Identifies the <code>Browse</code> action. */
	static final Integer		BROWSE = Integer.valueOf(1);
	
	/** Identifies the <code>Remove</code> action. */
	static final Integer		REMOVE = Integer.valueOf(2);
	
	/** Identifies the <code>Remove all</code> action. */
	static final Integer		REMOVE_ALL = Integer.valueOf(3);
	
	/** Identifies the <code>Add</code> action. */
	static final Integer		ADD = Integer.valueOf(4);
	
	/** 
	 * Reference to the {@link MetadataViewer} component, which, in this
	 * context, is regarded as the Model.
	 */
	private MetadataViewer						model;

	/** Reference to the View. */
	private MetadataViewerUI					view;
	
	/** Maps actions identifiers onto actual <code>Action</code> object. */
	private Map<Integer, MetadataViewerAction>	actionsMap;
	
	/** The loading window. */
	private LoadingWindow   					loadingWindow;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(REFRESH, new RefreshAction(model));
		actionsMap.put(BROWSE, new BrowseAction(model));
		actionsMap.put(ADD, new AddAction(model));
		actionsMap.put(REMOVE, new RemoveAction(model));
		actionsMap.put(REMOVE_ALL, new RemoveAllAction(model));
	}
	
	/**
	 * Creates a new instance.
	 * The
	 * {@link #initialize(MetadataViewer, MetadataViewerUI) initialize} 
	 * method should be called straight
	 * after to link this Controller to the other MVC components.
	 */
	MetadataViewerControl() {}
	
	/**
	 * Links this Controller to its Model and its View.
	 * 
	 * @param model  Reference to the {@link ImViewer} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 * @param view   Reference to the View.  Mustn't be <code>null</code>.
	 */
	void initialize(MetadataViewer model, MetadataViewerUI view)
	{
		if (model == null) throw new NullPointerException("No model.");
		if (view == null) throw new NullPointerException("No view.");
		this.model = model;
		this.view = view;
		actionsMap = new HashMap<Integer, MetadataViewerAction>();
		createActions();
		Registry reg = MetadataViewerAgent.getRegistry();
		loadingWindow = new LoadingWindow(reg.getTaskBar().getFrame());
		loadingWindow.setTitle("Saving Data");
		loadingWindow.setStatus("Batch annotations");
		model.addChangeListener(this);
	}
	
	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	MetadataViewerAction getAction(Integer id) { return actionsMap.get(id); }

	/**
	 * Analyzes the currently selected image. 
	 * 
	 * @param channelIndex The channel to analyze.
	 */
	void analyseFRAP(int channelIndex)
	{
		List<Long> ids = new ArrayList<Long>();
		ImageData img = (ImageData) model.getRefObject();
		ids.add(img.getId());
		AnalysisParam param = new AnalysisParam(ids, ImageData.class,
				AnalysisParam.FRAP);
		List<Integer> channels = new ArrayList<Integer>();
		channels.add(channelIndex);
		param.setChannels(channels);
		IconManager icons = IconManager.getInstance();
		AnalysisActivityParam activity = new AnalysisActivityParam(param, 
				icons.getIcon(IconManager.ANALYSE_FRAP_22));
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), activity);
	}
	
	/**
	 * Reacts to state changes in the {@link MetadataViewer}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent ce)
	{
		switch (model.getState()) {
			case MetadataViewer.READY:
				model.setStatus(false);
				loadingWindow.setVisible(false);
				break;
			case MetadataViewer.BATCH_SAVING:
				UIUtilities.centerAndShow(loadingWindow);
				break;
			case MetadataViewer.LOADING_METADATA:
			case MetadataViewer.SAVING:
				model.setStatus(true);
				break;
		}
	}

	/**
	 * Reacts to properties fired by the renderer.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Renderer.RENDER_PLANE_PROPERTY.equals(name)) {
			model.renderPlane();
		} else if (Renderer.APPLY_TO_ALL_PROPERTY.equals(name)) {
			model.applyToAll(); 
			model.loadViewedBy();
		} else if (Renderer.SELECTED_CHANNEL_PROPERTY.equals(name)) {
			model.onChannelSelected((Integer) evt.getNewValue());
		} else if (Renderer.CHANNEL_COLOR_PROPERTY.equals(name)) {
			model.onChannelColorChanged((Integer) evt.getNewValue());
		} else if (Renderer.RELOAD_PROPERTY.equals(name)) {
			model.reloadRenderingControl((Boolean) evt.getNewValue());
		} else if 
		(ChannelSelectionDialog.CHANNEL_ANALYSIS_SELECTION_PROPERTY.equals(
				name)) {
			List l = (List) evt.getNewValue();
			ChannelData data = (ChannelData) l.get(0);
			int index = (Integer) l.get(1);
			switch (index) {
				case AnalysisParam.FRAP:
					analyseFRAP(data.getIndex());
					break;
			}
		} else if (Renderer.VIEWED_BY_PROPERTY.equals(name)) {
		        model.loadViewedBy();
		} else if (Renderer.SAVE_SETTINGS_PROPERTY.equals(name)) {
			model.saveSettings();
		}
	}
	
}
