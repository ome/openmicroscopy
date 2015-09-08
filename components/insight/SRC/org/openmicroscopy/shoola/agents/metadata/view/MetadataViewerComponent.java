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
package org.openmicroscopy.shoola.agents.metadata.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsSaved;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.util.ChannelSelectionDialog;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.DataObjectRegistration;
import org.openmicroscopy.shoola.agents.util.ui.MovieExportDialog;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.ScriptActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

/** 
 * Implements the {@link MetadataViewer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class MetadataViewerComponent 
	extends AbstractComponent
	implements MetadataViewer
{
	
	/** The Model sub-component. */
	private MetadataViewerModel 	model;
	
	/** The Control sub-component. */
	private MetadataViewerControl	controller;
	
	/** The View sub-component. */
	private MetadataViewerUI 		view;
	
	/**
	 * Creates the movie.
	 * 
	 * @param parameters The parameters used to create the movie.
	 */
	private void createMovie(MovieExportParam parameters)
	{
		if (parameters == null) return;
		Object refObject = model.getRefObject();
		ImageData img = null;
		if (refObject instanceof ImageData)
			img = (ImageData) refObject;
		else if (refObject instanceof WellSampleData) {
			img = ((WellSampleData) refObject).getImage();
		}
		if (img == null) return;
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		MovieActivityParam activity = new MovieActivityParam(parameters, img);
		IconManager icons = IconManager.getInstance();
		activity.setIcon(icons.getIcon(IconManager.MOVIE_22));
		un.notifyActivity(model.getSecurityContext(), activity);
	}

	/**
	 * Deletes the annotations.
	 * 
	 * @param toDelete The annotations to delete.
	 */
	private void deleteAnnotations(List<AnnotationData> toDelete)
	{
		if (toDelete == null || toDelete.size() == 0) return;

		// don't popup the activity dialog when a MapAnnotation is deleted
		boolean silent = containsMapAnnotationsOnly(toDelete);

		//Should only be annotation so content is false;
		List<DeletableObject> l = new ArrayList<DeletableObject>();
		Iterator<AnnotationData> j = toDelete.iterator();
		while (j.hasNext())
			l.add(new DeletableObject(j.next()));
		IconManager icons = IconManager.getInstance();
		DeleteActivityParam p = new DeleteActivityParam(
				icons.getIcon(IconManager.APPLY_22), l);
		p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
		p.setUIRegister(!silent);
		UserNotifier un = 
			TreeViewerAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), p);
	}
	
	/**
	 * Checks if a list contains only MapAnnotations 
	 * 
	 * @param list
	 *            The list to check
	 * @return <code>true</code> if there are only MapAnnotations in the list;
	 *         <code>false</code> otherwise or if list is <code>null</code>
	 */
	private boolean containsMapAnnotationsOnly(List<AnnotationData> list) {
		if (list == null)
			return false;

		boolean mapAnnosOnly = true;
		for (AnnotationData d : list) {
			if (!(d instanceof MapAnnotationData)) {
				mapAnnosOnly = false;
				break;
			}
		}
		return mapAnnosOnly;
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	MetadataViewerComponent(MetadataViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new MetadataViewerControl();
		view = new MetadataViewerUI();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(controller, model);
		if (!(model.getRefObject() instanceof String))
			setSelectionMode(true);
	}

	/** Saves before close. */
	void saveBeforeClose()
	{
		firePropertyChange(SAVE_DATA_PROPERTY, Boolean.valueOf(true),
				Boolean.valueOf(false));
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
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#activate(Map)
	 */
	public void activate(Map channelData)
	{
		switch (model.getState()) {
			case NEW:
				model.getEditor().setChannelsData(channelData, false);
				setRootObject(model.getRefObject(), model.getUserID(),
						model.getSecurityContext());
				break;
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		} 
	}

        /** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#applyRenderingSettings(RndProxyDef)
	 */
	public void applyRenderingSettings(RndProxyDef rndDef) {
	    model.applyRenderingSettings(rndDef);
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#discard()
	 */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#cancel(int)
	 */
	public void cancel(int loaderID)
	{
		if (model.getState() == DISCARDED) return;
		model.cancel(loaderID);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setMetadata(Map<DataObject, StructuredDataResults>)
	 */
	public void setMetadata(Map<DataObject, StructuredDataResults> results,
			int loaderID)
	{
		if (results == null || results.size() == 0) return;
		//Need to check the size of the results map.
		Browser browser = model.getBrowser();
		DataObject node;
		StructuredDataResults data;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = results.entrySet().iterator();
		if (results.size() == 1) { //handle the single selection
			while (i.hasNext()) {
				e = i.next();
				node = e.getKey();
				if (!model.isSameObject(node)) {
					model.setStructuredDataResults(null, loaderID);
					fireStateChange();
					return;
				}
				data = e.getValue();
				Object object = data.getRelatedObject();
				if (object == model.getParentRefObject() ||
					(object instanceof PlateData && node 
							instanceof WellSampleData)) {
					model.setParentDataResults(data, node, loaderID);
					model.fireStructuredDataLoading(node);
				} else {
					model.setStructuredDataResults(results, loaderID);
					browser.setParents(null, data.getParents());
					model.getEditor().setStructuredDataResults();
				}
				fireStateChange();
			}
		} else {
			if (model.isSameSelection(results.keySet())) {
				model.setStructuredDataResults(results, loaderID);
				model.getEditor().setStructuredDataResults();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getSelectionUI()
	 */
	public JComponent getSelectionUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return model.getBrowser().getUI();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getEditorUI()
	 */
	public JComponent getEditorUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return model.getEditor().getUI();
	}
	
	/** 
     * Implemented as specified by the {@link MetadataViewer} interface.
     * @see MetadataViewer#getEditor()
     */
    public Editor getEditor()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
        return model.getEditor();
    }
    
	/** 
         * Implemented as specified by the {@link MetadataViewer} interface.
         * @see MetadataViewer#isRendererLoaded()
         */
	public boolean isRendererLoaded() {
	    return model.getEditor().getRenderer() != null;
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getUI()
	 */
	public JComponent getUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return view.getUI();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getParentUI()
	 */
	public JFrame getParentUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return view;
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setRootObject(Object, long, ctx)
	 */
	public void setRootObject(Object root, long userID, SecurityContext ctx)
	{
		if (root instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) root;
			if (ws.getId() < 0) root = null;
		}
		if (root == null) {
			root = "";
			userID = -1;
		}
		//Previewed the image.
		boolean same = model.isSameObject(root);
		model.setRootObject(root, ctx);
		if (model.isSingleMode()) {
			model.fireStructuredDataLoading(root);
			fireStateChange();
		}
		view.setRootObject();
		//reset the parent.
		model.setUserID(userID);
		//check if save object before setting to null.
		if (!same)
			setParentRootObject(null, null);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#refresh()
	 */
	public void refresh()
	{
		if (model.isSingleMode()) {
			model.fireStructuredDataLoading(model.getRefObject());
		} else {
			model.setRelatedNodes(model.getRelatedNodes());
		}
		fireStateChange();
		view.setRootObject();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setParentRootObject(Object, Object)
	 */
	public void setParentRootObject(Object parent, Object grandParent)
	{
		model.setParentRootObject(parent, grandParent);
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#loadContainers(TreeBrowserDisplay)
	 */
	public void loadContainers(TreeBrowserDisplay node)
	{
		if (node == null)
			throw new IllegalArgumentException("No node specified.");
		model.fireParentLoading((TreeBrowserSet) node);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setContainers(TreeBrowserDisplay, Object)
	 */
	public void setContainers(TreeBrowserDisplay node, Object result)
	{
		Browser browser = model.getBrowser();
		if (node == null) {
			StructuredDataResults data = model.getStructuredData();
			if (data != null) {
				data.setParents((Collection) result);
				browser.setParents(null, (Collection) result);
			}
		} else
			browser.setParents((TreeBrowserSet) node, (Collection) result);
		model.getEditor().setStatus(false);
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getRelatedNodes()
	 */
	public List getRelatedNodes()
	{
		return model.getRelatedNodes();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#saveData(DataObject, List, List, DataObject, boolean)
	 */
	public void saveData(DataToSave object, List<AnnotationData> toDelete,
				List<Object> metadata, DataObject data, boolean asynch)
	{
		if (data == null) return;
		List<AnnotationData> toAdd = null;
		List<Object> toRemove = null;
		if (object != null) {
			toAdd = object.getToAdd();
			toRemove = object.getToRemove();
		}
		Object refObject = model.getRefObject();
		List<DataObject> toSave = new ArrayList<DataObject>();
		if (refObject instanceof FileData) {
			FileData fa = (FileData) data;
			if (fa.getId() > 0) {
				toSave.add(data);
				model.fireSaving(object, metadata, toSave, asynch);
				fireStateChange();
				deleteAnnotations(toDelete);
			} else {
				DataObjectRegistration r = new DataObjectRegistration(toAdd,
						toRemove, toDelete, metadata, data);
				firePropertyChange(REGISTER_PROPERTY, null, r);
			}
			return;
		}
		Collection<DataObject> nodes = model.getRelatedNodes();
		Iterator<DataObject> n;
		if (!model.isSingleMode()) {
			if (nodes != null) {
				n = nodes.iterator();
				DataObject o;
				while (n.hasNext()) {
					o = (DataObject) n.next();
					if (o instanceof WellSampleData) {
						WellSampleData wsd = (WellSampleData) o;
						if (wsd.getImage() != null) {
							toSave.add(wsd.getImage());
						}
					} else toSave.add(o);
				}
			}
		} else toSave.add(data);
		boolean b = true;
		if (refObject instanceof ProjectData || 
			refObject instanceof ScreenData ||
			refObject instanceof PlateData || 
			refObject instanceof DatasetData || 
			refObject instanceof WellSampleData ||
			refObject instanceof PlateAcquisitionData ||
			refObject instanceof WellData) {
			model.fireSaving(object, metadata, toSave, asynch);
		} else if (refObject instanceof ImageData) {
			ImageData img = (ImageData) refObject;
			if (img.getId() < 0) {
				DataObjectRegistration r = new DataObjectRegistration(toAdd, 
						toRemove, toDelete, metadata, data);
				firePropertyChange(REGISTER_PROPERTY, null, r);
				return;
			} else {
				model.fireSaving(object, metadata, toSave, asynch);
			}
		}  else if (refObject instanceof TagAnnotationData) {
			//Only update properties.
			if (CollectionUtils.isEmpty(toAdd) && CollectionUtils.isEmpty(toRemove)) {
				model.fireSaving(object, metadata, toSave, asynch);
				b = false;
			}	
		}
		if (toDelete != null && toDelete.size() > 0)
			deleteAnnotations(toDelete);
		if (b) fireStateChange();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		Editor editor = model.getEditor();
		if (editor == null) return false;
		return editor.hasDataToSave();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#saveData()
	 */
	public void saveData()
	{
		firePropertyChange(SAVE_DATA_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#clearDataToSave()
	 */
	public void clearDataToSave()
	{
		firePropertyChange(CLEAR_SAVE_DATA_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onDataSave(List)
	 */
	public void onDataSave(List<DataObject> data)
	{
		if (data == null) return;
		if (model.getState() == DISCARDED) return;
		DataObject dataObject = null;
		if (data.size() == 1) dataObject = data.get(0);
		if (dataObject != null && model.isSameObject(dataObject)) {
			setRootObject(model.getRefObject(), model.getUserID(),
					model.getSecurityContext());
			model.setState(READY);
			firePropertyChange(ON_DATA_SAVE_PROPERTY, null, dataObject);
		} else {
			if (model.isSameSelection(data))
				model.setRelatedNodes(data);
			else model.setState(READY);
			firePropertyChange(ON_DATA_SAVE_PROPERTY, null, data);
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setSelectionMode(boolean)
	 */
	public void setSelectionMode(boolean single)
	{
		model.setSelectionMode(single);
		model.getEditor().setSelectionMode(single);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#isSingleMode()
	 */
	public boolean isSingleMode() { return model.isSingleMode(); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setRelatedNodes(List)
	 */
	public void setRelatedNodes(List nodes)
	{
		if (CollectionUtils.isEmpty(nodes)) return;
		List<Long> ids = new ArrayList<Long>();
		Iterator i = nodes.iterator();
		List<DataObject> results = new ArrayList<DataObject>();
		DataObject data;
		while (i.hasNext()) {
			Object object = i.next();
			if (object instanceof DataObject) {
				data = (DataObject) object;
				if (!ids.contains(data.getId())) {
					results.add(data);
					ids.add(data.getId());
				}
			}
		}
		model.setRelatedNodes(results);
		firePropertyChange(RELATED_NODES_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onAdminUpdated(Object)
	 */
	public void onAdminUpdated(Object data)
	{
		Object o = data;
		if (data instanceof Map) {
			Map l = (Map) data;
			if (l.size() > 0) {
				UserNotifier un = 
					MetadataViewerAgent.getRegistry().getUserNotifier();
				StringBuffer buf = new StringBuffer();
				buf.append("Unable to update the following experimenters:\n");
				Entry entry;
				Iterator i = l.entrySet().iterator();
				Object node;
				ExperimenterData exp;
				Exception ex;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					node = entry.getKey();
					if (node instanceof ExperimenterData) {
						exp = (ExperimenterData) node;
						ex = (Exception) entry.getValue();
						buf.append(exp.getFirstName()+" "+exp.getLastName());
						buf.append("\n->"+ex.getMessage());
						buf.append("\n");
					}
				}
				un.notifyInfo("Update experimenters", buf.toString());
			}
			firePropertyChange(CLEAR_SAVE_DATA_PROPERTY, null, data);
			setRootObject(null, -1, null);
		} else setRootObject(o, model.getUserID(), model.getAdminContext());
		firePropertyChange(ADMIN_UPDATED_PROPERTY, null, data);
		
		/*
		if (data instanceof ExperimenterData || data instanceof GroupData) {
			firePropertyChange(ADMIN_UPDATED_PROPERTY, null, data);
			setRootObject(data, model.getUserID());
		}
		*/
		
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#loadParents()
	 */
	public void loadParents()
	{
		StructuredDataResults data = model.getStructuredData();
		if (data == null) return;
		if (data.getParents() != null) return;
		Object ho = data.getRelatedObject();
		if (ho != null && ho instanceof DataObject) {
			model.loadParents(ho.getClass(), ((DataObject) ho).getId());
			setStatus(true);
			firePropertyChange(LOADING_PARENTS_PROPERTY, Boolean.valueOf(false),
					Boolean.valueOf(true));
		}
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getStructuredData()
	 */
	public StructuredDataResults getStructuredData()
	{
		//TODO: Check state
		return model.getStructuredData();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getParentStructuredData()
	 */
	public StructuredDataResults getParentStructuredData()
	{
		//TODO: Check state
		return model.getParentStructuredData();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setStatus(boolean)
	 */
	public void setStatus(boolean busy)
	{
		model.getEditor().setStatus(busy);
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#showTagWizard()
	 */
	public void showTagWizard()
	{
		if (model.getState() == DISCARDED) return;
		model.getEditor().loadExistingTags();
		//model.getMetadataViewer().showTagWizard();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getObjectPath()
	 */
	public String getObjectPath()
	{
		return model.getRefObjectPath();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#makeMovie(int, Color)
	 */
	public void makeMovie(int scaleBar, Color overlayColor)
	{
		Object refObject = model.getRefObject();
		if (refObject instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) refObject;
			refObject = wsd.getImage();
		}
		if (!(refObject instanceof ImageData)) return;
		PixelsData data = null;
		ImageData img = (ImageData) refObject;
    	try {
    		data = ((ImageData) refObject).getDefaultPixels();
		} catch (Exception e) {}
		if (data == null) return;
		int maxT = data.getSizeT();
    	int maxZ = data.getSizeZ();
    	int defaultZ = maxZ;
    	int defaultT = maxT;
    	
    	Object value = data.getSizeC();
    	if (model.getEditor().getChannelData() != null)
    		value = model.getEditor().getChannelData();
    	String name = EditorUtil.getPartialName(img.getName());
    	JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
    	MovieExportDialog dialog = new MovieExportDialog(f, name, 
    			maxT, maxZ, defaultZ, defaultT, value);
    	dialog.setBinaryAvailable(MetadataViewerAgent.isBinaryAvailable());
    	dialog.setScaleBarDefault(scaleBar, overlayColor);
    	dialog.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				Object src = evt.getSource();
				if (MovieExportDialog.CREATE_MOVIE_PROPERTY.equals(name)) {
					if (src instanceof MovieExportDialog) {
						MovieExportDialog d = (MovieExportDialog) src;
						createMovie(d.getParameters());
					}
				} else if (
				ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY.equals(name)) {
                    if (src instanceof MovieExportDialog) {
                        String script = (String) evt.getNewValue();
                        ScriptObject object =
                                model.getEditor().getScriptFromName(script);
                        if (object == null) return;
                        manageScript(object, MetadataViewer.VIEW);
                    }
				}
			}
		});
		dialog.centerDialog();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getRndIndex()
	 */
	public int getRndIndex()
	{
		if (model.getState() == MetadataViewer.DISCARDED) return -1;
		return model.getIndex();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#renderPlane()
	 */
	public void renderPlane()
	{
		Object obj = model.getRefObject();
		if (obj instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) obj;
			obj = wsd.getImage();
		}
		if (!(obj instanceof ImageData)) return;
		long imageID = ((ImageData) obj).getId();
		switch (getRndIndex()) {
			case RND_GENERAL:
				model.getEditor().getRenderer().renderPreview();
				break;
			case RND_SPECIFIC:
				firePropertyChange(RENDER_PLANE_PROPERTY, -1, imageID);
			break;
		}
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#applyToAll()
	 */
	public void applyToAll()
	{
		Object obj = model.getRefObject();
		if (obj instanceof ImageData) {
			firePropertyChange(APPLY_SETTINGS_PROPERTY, null, obj);
		} else if (obj instanceof WellSampleData) {
			Object[] objects = new Object[2];
			objects[0] = obj;
			objects[1] = model.getParentRefObject();
			firePropertyChange(APPLY_SETTINGS_PROPERTY, null, objects);
		}
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onSettingsApplied()
	 */
	public void onSettingsApplied()
	{
		firePropertyChange(SETTINGS_APPLIED_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onRndLoaded(boolean)
	 */
	public void onRndLoaded(boolean reload)
	{
		getRenderer().addPropertyChangeListener(controller);
		firePropertyChange(RND_LOADED_PROPERTY, Boolean.valueOf(!reload), 
				Boolean.valueOf(reload));
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getRenderer()
	 */
	public Renderer getRenderer()
	{
		if (model.getEditor() == null) return null;
		return model.getEditor().getRenderer();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onChannelSelected(int)
	 */
	public void onChannelSelected(int index)
	{
		if (getRndIndex() != RND_SPECIFIC) return;
		firePropertyChange(SELECTED_CHANNEL_PROPERTY, -1, index);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getIdealRendererSize()
	 */
	public Dimension getIdealRendererSize()
	{
		Renderer rnd = getRenderer();
		if (rnd == null) return new Dimension(0, 0);
		return rnd.getUI().getPreferredSize();
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#analyse(int)
	 */
	public void analyse(int index)
	{
		if (index != AnalysisParam.FRAP) return;
		Object refObject = model.getRefObject();
		if (!(refObject instanceof ImageData)) return;
		List<ChannelData> channels = new ArrayList<ChannelData>();
		Map m = model.getEditor().getChannelData();
		if (m != null && m.size() == 1) {
			controller.analyseFRAP(0);
			return;
		}
		if (m != null) {
			Iterator j = m.keySet().iterator();
			while (j.hasNext()) {
				channels.add((ChannelData) j.next());
			}
		}
		
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.ANALYSE_48);
		switch (index) {
			case AnalysisParam.FRAP:
				icon = icons.getIcon(IconManager.ANALYSE_FRAP_48);
				break;
		}
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		ChannelSelectionDialog d = new ChannelSelectionDialog(f, icon, channels,
				index);
		d.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onRndSettingsCopied(Collection)
	 */
	public void onRndSettingsCopied(Collection<Long> imageIds)
	{
		if (CollectionUtils.isEmpty(imageIds)) return;
		Renderer rnd = getRenderer();
		if (rnd == null) return;
		Object ob = model.getRefObject();
		ImageData img = null;
		if (ob instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) ob;
			img = wsd.getImage();
		} else if (ob instanceof ImageData)
			img = (ImageData) ob;
		if (img == null) return;
		if (!imageIds.contains(img.getId())) return;
		rnd.refresh();
		rnd.renderPreview();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#isNumerousChannel()
	 */
	public boolean isNumerousChannel() { return model.isNumerousChannel(); }

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setSelectedTab(int)
	 */
	public void setSelectedTab(int index)
	{
		model.getEditor().setSelectedTab(index);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#activityOptions(Component, Point, int)
	 */
	public void activityOptions(Component source, Point location, int index)
	{
		List<Object> l = new ArrayList<Object>();
		l.add(source);
		l.add(location);
		l.add(index);
		firePropertyChange(ACTIVITY_OPTIONS_PROPERTY, null, l);
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#createFigure(Object)
	 */
	public void createFigure(Object value)
	{
		if (value == null) return;
		if (value instanceof FigureParam)
			firePropertyChange(GENERATE_FIGURE_PROPERTY, null, value);
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#manageScript(ScriptObject, int)
	 */
	public void manageScript(ScriptObject value, int index)
	{
		if (value == null) return;
		ScriptActivityParam p = null;
		switch (index) {
			case RUN:
				p = new ScriptActivityParam(value, ScriptActivityParam.RUN);
				break;
			case DOWNLOAD:
				p = new ScriptActivityParam(value, 
						ScriptActivityParam.DOWNLOAD);
				break;
			case VIEW:
				p = new ScriptActivityParam(value, ScriptActivityParam.VIEW);
				break;
		}
		if (p != null)
			firePropertyChange(HANDLE_SCRIPT_PROPERTY, null, p);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#reloadRenderingControl(Boolean)
	 */
	public void reloadRenderingControl(boolean value)
	{
		if (value)
			model.getEditor().loadRenderingControl(
					RenderingControlLoader.RELOAD);
		else {
			firePropertyChange(CLOSE_RENDERER_PROPERTY, null, 
					model.getRefObject());
		}
	}
	
	/**
         * Implemented as specified by the {@link MetadataViewer} interface.
         * @see MetadataViewer#resetRenderingControl()
         */
        public void resetRenderingControl()
        {
              model.getEditor().loadRenderingControl(
                                        RenderingControlLoader.RESET);
        }
        
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#reloadRenderingControl()
	 */
	public void reloadRenderingControl()
	{
	    model.getEditor().loadRenderingControl();
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onChannelColorChanged(int)
	 */
	public void onChannelColorChanged(int index)
	{
		view.onChannelColorChanged(index);
		firePropertyChange(CHANNEL_COLOR_CHANGED_PROPERTY, -1, index);
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getRefObject()
	 */
	public Object getRefObject() { return model.getRefObject(); }

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#updateAdminObject(Object, boolean)
	 */
	public void updateAdminObject(Object data, boolean async)
	{
		if (data instanceof ExperimenterData)
			model.fireExperimenterSaving((ExperimenterData) data, async);
		else if (data instanceof AdminObject)
			model.fireAdminSaving((AdminObject) data, async);
		else if (data instanceof GroupData)
		    model.fireChangeGroup((GroupData) data);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getUserID()
	 */
	public long getUserID() { return model.getUserID(); }
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#resetPassword(String)
	 */
	public void resetPassword(String newPass)
	{
		firePropertyChange(RESET_PASSWORD_PROPERTY, null, newPass);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#loadViewedBy()
	 */
	public void loadViewedBy()
	{
		ImageData ref = model.getImage();
		if (ref != null) {
		    model.fireViewedByLoading();
		}
	}
	
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setViewedBy(Map)
	 */
	public void setViewedBy(Map result)
	{
		model.setViewedBy(result);
		view.createViewedByItems();
		model.fireThumbnailsLoading();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setThumbnails(Map, long)
	 */
	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID)
	{
		Object ref = model.getRefObject();
		ImageData image = null;
		if (ref instanceof ImageData) image = (ImageData) ref;
		else if (ref instanceof WellSampleData) 
			image = ((WellSampleData) ref).getImage();
		
		if (image == null) return;
		if (image.getId() == imageID) {
			view.setThumbnails(thumbnails);
		}
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#uploadScript()
	 */
	public void uploadScript()
	{
		firePropertyChange(UPLOAD_SCRIPT_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}
	
	/** Saves the settings. */
	public void saveSettings() 
	{
		//Previewed the image.
		Renderer rnd = model.getEditor().getRenderer();
		if (rnd != null) {
		    model.fireThumbnailsLoading();
		}
		if (rnd != null && getRndIndex() == RND_GENERAL) {
			//save settings 
			long imageID = -1;
			long pixelsID = -1;
			Object obj = model.getRefObject();
			if (obj instanceof WellSampleData) {
				WellSampleData wsd = (WellSampleData) obj;
				obj = wsd.getImage();
			}
			if (obj instanceof ImageData) {
				ImageData data = (ImageData) obj;
				imageID = data.getId();
				pixelsID = data.getDefaultPixels().getId();
			}
			//check if I can save first
			if (model.canAnnotate()) {
				Registry reg = MetadataViewerAgent.getRegistry();
				RndProxyDef def = null;
				try {
					def = rnd.saveCurrentSettings();
				} catch (Exception e) {
					try {
						
						reg.getImageService().resetRenderingService(
								model.getSecurityContext(), pixelsID);
						def = rnd.saveCurrentSettings();
					} catch (Exception ex) {
						String s = "Data Retrieval Failure: ";
				    	LogMessage msg = new LogMessage();
				        msg.print(s);
				        msg.print(e);
				        reg.getLogger().error(this, msg);
					}
				}
				EventBus bus = 
					MetadataViewerAgent.getRegistry().getEventBus();
				bus.post(new RndSettingsSaved(pixelsID, def));
			}
			
			if (imageID >= 0 && model.canAnnotate()) {
				firePropertyChange(RENDER_THUMBNAIL_PROPERTY, -1, imageID);
				// reload the viewedby thumbnails after new rendering settings were applied
				model.fireViewedByLoading();
			}
		}
	}
    
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onGroupSwitched(boolean)
	 */
	public void onGroupSwitched(boolean success)
	{
		if (!success) return;
		ExperimenterData exp = MetadataViewerAgent.getUserDetails();
		setRootObject(null, exp.getId(), model.getSecurityContext());
		setParentRootObject(null, null);
		model.getEditor().onGroupSwitched(success);
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onGroupSwitched(boolean)
	 */
	public SecurityContext getSecurityContext()
	{ 
		return model.getSecurityContext();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#isSameObject(Object)
	 */
	public boolean isSameObject(Object object)
	{
		return model.isSameObject(object);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getAllStructuredData()
	 */
	public Map<DataObject, StructuredDataResults> getAllStructuredData()
	{
		return model.getAllStructuredData();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getStructuredData()
	 */
	public StructuredDataResults getStructuredData(Object refObject)
	{
		return model.getStructuredData(refObject);
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onUpdatedChannels(List)
	 */
	public void onUpdatedChannels(List<ChannelData> channels)
	{
		Renderer rnd = getRenderer();
		if (rnd != null) rnd.onUpdatedChannels(channels);
		model.getEditor().onUpdatedChannels(channels);
	}
	
	/**
	 * Implemented as specified by the {@link Editor} interface.
	 * @see MetadataViewer#getCurrentUser()
	 */
	public ExperimenterData getCurrentUser()
	{
	    return model.getCurrentUser();
	}
	/** 
	 * Overridden to return the name of the instance to save. 
	 * @see #toString()
	 */
	public String toString() { return model.getInstanceToSave(); }
	
	/**
         * Implemented as specified by the {@link MetadataViewer} interface.
         * @see MetadataViewer#hasRndSettingsCopied()
         */
        public boolean hasRndSettingsCopied() {
            return model.hasRndSettingsCopied();
        }
	
	/**
         * Implemented as specified by the {@link MetadataViewer} interface.
         * @see MetadataViewer#applyCopiedRndSettings()
         */
	public void applyCopiedRndSettings() {
	    if(getRenderer()==null)
	        return;
	    
	    model.fireLoadRndSettings();
	}
}
