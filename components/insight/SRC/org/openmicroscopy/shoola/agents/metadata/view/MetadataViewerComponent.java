/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.util.BasicAnalyseDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.MovieExportDialog;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellSampleData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
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
	 * Initializes a message dialog.
	 * 
	 * @return See above.
	 */
	private MessageBox initMessageDialog()
	{
		MessageBox dialog = new MessageBox(view, "Save Annotations", 
        "Do you want to attach the annotations to: ");
		dialog.setNoText("Cancel");
		dialog.setYesText("OK");
		return dialog;
	}
	
	/**
	 * Creates the movie.
	 * 
	 * @param parameters The parameters used to create the movie.
	 */
	private void createMovie(MovieExportParam parameters)
	{
		if (parameters == null) return;
		/*
		firePropertyChange(CREATING_MOVIE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
				*/
		
		
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
		MovieActivityParam activity = new MovieActivityParam(parameters, null,
				img);
		IconManager icons = IconManager.getInstance();
		activity.setIcon(icons.getIcon(IconManager.MOVIE_22));
		un.notifyActivity(activity);
		
		//model.createMovie(parameters);
	}
	
	/**
	 * Analyzes the data.
	 * 
	 * @param n The id of the image.
	 */
	private void analyseData(Number n)
	{
		UserNotifier un = 
			MetadataViewerAgent.getRegistry().getUserNotifier();
		if (n == null) {
			un.notifyInfo("Analyse", "Please enter a valid ID.");
			return;
		}
		FileAnnotationData fa = model.getIRF();
		if (fa == null) {
			un.notifyInfo("Analyse", "No function linked to the image.");
			return;
		}
		model.analyseData(n.longValue());
		firePropertyChange(ANALYSE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
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

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#activate(Map)
	 */
	public void activate(Map channelData)
	{
		switch (model.getState()) {
			case NEW:
				model.getEditor().setChannelsData(channelData, false);
				setRootObject(model.getRefObject());
				break;
			case DISCARDED:
				throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		} 
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#discard()
	 */
	public void discard()
	{
		model.discard();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#cancel(TreeBrowserDisplay)
	 */
	public void cancel(TreeBrowserDisplay refNode) { model.cancel(refNode); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#loadMetadata(TreeBrowserDisplay)
	 */
	public void loadMetadata(TreeBrowserDisplay node)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (node == null)
			throw new IllegalArgumentException("No node specified.");
		Object userObject = node.getUserObject();
		if (userObject instanceof DataObject) {
			if (model.isSingleMode()) {
				model.fireStructuredDataLoading(node);
				fireStateChange();
			}
		} else if (userObject instanceof File) {
			File f = (File) userObject;
			if (f.isDirectory() && model.isSingleMode()) {
				model.fireStructuredDataLoading(node);
				fireStateChange();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setMetadata(TreeBrowserDisplay, Object)
	 */
	public void setMetadata(TreeBrowserDisplay node, Object result)
	{
		if (node == null)
			throw new IllegalArgumentException("No node specified.");
		Object userObject = node.getUserObject();
		Object refObject = model.getRefObject();
		if (refObject == userObject) {
			Browser browser = model.getBrowser();
			if (result instanceof StructuredDataResults) {
				model.setStructuredDataResults((StructuredDataResults) result);
				browser.setParents(node, 
						model.getStructuredData().getParents());
				model.getEditor().setStructuredDataResults();
				view.setOnScreen();
				fireStateChange();
				return;
			}
			if (!(userObject instanceof String)) return;
			String name = (String) userObject;
			
			if (browser == null) return;
			if (Browser.DATASETS.equals(name) || Browser.PROJECTS.equals(name)) 
				browser.setParents((TreeBrowserSet) node, (Collection) result);
			model.notifyLoadingEnd(node);
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
	 * @see MetadataViewer#setRootObject(Object)
	 */
	public void setRootObject(Object root)
	{
		if (root == null) root = "";
		if (root instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) root;
			if (ws.getId() < 0) root = null;
		}
		model.setRootObject(root);
		view.setRootObject();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setParentRootObject(Object)
	 */
	public void setParentRootObject(Object parent)
	{
		model.setParentRootObject(parent);
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
	public Collection getRelatedNodes()
	{
		return model.getRelatedNodes();
	}
	
	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#saveData(List, List, List, DataObject)
	 */
	public void saveData(List<AnnotationData> toAdd, 
				List<AnnotationData> toRemove, List<Object> metadata,
				DataObject data)
	{
		if (data == null) return;
		Object refObject = model.getRefObject();
		List<DataObject> toSave = new ArrayList<DataObject>();
		
		if (refObject instanceof ExperimenterData) {
			model.fireExperimenterSaving((ExperimenterData) data);
			return;
		}
		Collection nodes = model.getRelatedNodes();
		Iterator n;
		toSave.add(data);
		if (!model.isSingleMode()) {
			if (nodes != null) {
				n = nodes.iterator();
				while (n.hasNext()) 
					toSave.add((DataObject) n.next());
			}
		}
		
		MessageBox dialog;
		if (refObject instanceof ProjectData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
		} else if (refObject instanceof ScreenData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
		} else if (refObject instanceof PlateData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
			/*
			if ((toAdd.size() == 0 && toRemove.size() == 0)) {
				model.fireSaving(toAdd, toRemove, metadata, toSave);
				return;
			}
			dialog = initMessageDialog();
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			ButtonGroup group = new ButtonGroup();
			JRadioButton single = new JRadioButton();
			single.setText("The selected plate");
			single.setSelected(true);
			group.add(single);
			p.add(single);
			JRadioButton batchAnnotation = new JRadioButton();
			group.add(batchAnnotation);
			p.add(batchAnnotation);
			batchAnnotation.setText("All the wells");
			dialog.addBodyComponent(p);
			int option = dialog.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				//toSave.add(data);
				if (single.isSelected()) 
					model.fireSaving(toAdd, toRemove, metadata, toSave);
				else
					model.fireBatchSaving(toAdd, toRemove, toSave);
			}
			*/
		} else if (refObject instanceof DatasetData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
			//Only update properties.
			/*
			if ((toAdd.size() == 0 && toRemove.size() == 0)) {
				model.fireSaving(toAdd, toRemove, metadata, toSave);
				return;
			}
			dialog = initMessageDialog();
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			ButtonGroup group = new ButtonGroup();
			JRadioButton single = new JRadioButton();
			single.setText("The selected dataset");
			single.setSelected(true);
			group.add(single);
			p.add(single);
			JRadioButton batchAnnotation = new JRadioButton();
			group.add(batchAnnotation);
			p.add(batchAnnotation);
			batchAnnotation.setText("The images contained in the " +
					                "selected dataset");
			dialog.addBodyComponent(p);
			int option = dialog.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				//toSave.add(data);
				if (single.isSelected()) 
					model.fireSaving(toAdd, toRemove, metadata, toSave);
				else
					model.fireBatchSaving(toAdd, toRemove, toSave);
			}
			*/
		} else if (refObject instanceof ImageData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
		} else if (refObject instanceof WellSampleData) {
			model.fireSaving(toAdd, toRemove, metadata, toSave);
		} else if (refObject instanceof TagAnnotationData) {
			//Only update properties.
			if ((toAdd.size() == 0 && toRemove.size() == 0)) {
				model.fireSaving(toAdd, toRemove, metadata, toSave);
				return;
			}	
			/*
			TagAnnotationData tag = (TagAnnotationData) refObject;
			Set set = tag.getTags();
			if (set != null) {
				model.fireSaving(toAdd, toRemove, metadata, toSave);
				return;
			}
			set = tag.getDataObjects();
			boolean toAsk = false;
			if (set != null && set.size() > 0) toAsk = true;
			if (!toAsk) {
				model.fireSaving(toAdd, toRemove, metadata, toSave);
				return;
			}
			dialog = initMessageDialog();
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			ButtonGroup group = new ButtonGroup();
			JRadioButton single = new JRadioButton();
			single.setText("The selected tag");
			single.setSelected(true);
			group.add(single);
			p.add(single);
			JRadioButton batchAnnotation = new JRadioButton();
			group.add(batchAnnotation);
			p.add(batchAnnotation);
			batchAnnotation.setText("The images linked to the " +
			                       "selected tag");
			dialog.addBodyComponent(p);
			int option = dialog.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				//toSave.add(data);
				if (single.isSelected()) 
					model.fireSaving(toAdd, toRemove, metadata, toSave);
				else
					model.fireBatchSaving(toAdd, toRemove, toSave);
			}
			*/
		}
		fireStateChange();
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
		firePropertyChange(SAVE_DATA_PROPERTY, Boolean.FALSE, Boolean.TRUE);
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
			setRootObject(model.getRefObject());
			firePropertyChange(ON_DATA_SAVE_PROPERTY, null, dataObject);
		} else
			firePropertyChange(ON_DATA_SAVE_PROPERTY, null, data);
		model.setState(READY);
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
	public boolean isSingleMode()
	{
		return model.isSingleMode();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setRelatedNodes(Collection)
	 */
	public void setRelatedNodes(Collection nodes)
	{
		model.setRelatedNodes(nodes);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onExperimenterUpdated(ExperimenterData)
	 */
	public void onExperimenterUpdated(ExperimenterData data)
	{
		firePropertyChange(EXPERIMENTER_UPDATED_PROPERTY, null, data);
		setRootObject(data);
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
			firePropertyChange(LOADING_PARENTS_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
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
    	int defaultT = 1;
    	int defaultZ = 1;
    	String name = EditorUtil.getPartialName(img.getName());
    	JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
    	MovieExportDialog dialog = new MovieExportDialog(f, name, 
    			maxT, maxZ, defaultZ, defaultT);
    	dialog.setScaleBarDefault(scaleBar, overlayColor);
    	dialog.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (MovieExportDialog.CREATE_MOVIE_PROPERTY.equals(name)) {
					Object src = evt.getSource();
					if (src instanceof MovieExportDialog) {
						MovieExportDialog d = (MovieExportDialog) src;
						createMovie(d.getParameters());
					}
				}
			}
		});
		dialog.centerDialog();
	}
	
	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#uploadMovie(FileAnnotationData, File)
	 */
	public void uploadMovie(FileAnnotationData data, File folder)
	{
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (data == null) {
			if (folder == null) 
				un.notifyInfo("Movie Creation", "A problem occured while " +
					"creating the movie");
		} else {
			if (folder == null) folder = UIUtilities.getDefaultFolder();
			OriginalFile f = (OriginalFile) data.getContent();
			IconManager icons = IconManager.getInstance();
			
			DownloadActivityParam activity = new DownloadActivityParam(f,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			un.notifyActivity(activity);
			//un.notifyDownload(data, folder);
		}
		firePropertyChange(CREATING_MOVIE_PROPERTY, Boolean.valueOf(true), 
				Boolean.valueOf(false));
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
				firePropertyChange(RENDER_THUMBNAIL_PROPERTY, -1, imageID);
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
	 * @see MetadataViewer#analyse()
	 */
	public void analyse()
	{
		Object refObject = model.getRefObject();
		if (!(refObject instanceof ImageData)) return;
		IconManager icons = IconManager.getInstance();
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		BasicAnalyseDialog d = new BasicAnalyseDialog(f, 
				icons.getIcon(IconManager.ANALYSE_48));
		d.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				Number n = (Number) evt.getNewValue();
				analyseData(n);
			}
		});
		UIUtilities.centerAndShow(d);
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#uploadFret(FileAnnotationData, File)
	 */
	public void uploadFret(FileAnnotationData data, File folder)
	{
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (data == null) {
			if (folder == null) 
				un.notifyInfo("Data Analysis", "A problem occured while " +
					"analyzing the data.");
		} else {
			if (folder == null) folder = UIUtilities.getDefaultFolder();
			if (data == null) return;
			OriginalFile f = (OriginalFile) data.getContent();
			IconManager icons = IconManager.getInstance();
			
			DownloadActivityParam activity = new DownloadActivityParam(f,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			un.notifyActivity(activity);

			//un.notifyDownload(data, folder);
		}
		firePropertyChange(ANALYSE_PROPERTY, Boolean.valueOf(true), 
				Boolean.valueOf(false));
		
	}

	/**
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onRndSettingsCopied(Collection)
	 */
	public void onRndSettingsCopied(Collection imageIds)
	{
		if (imageIds == null || imageIds.size() == 0) return;
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
		rnd.reloadUI(false);
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
	 * @see MetadataViewer#export()
	 */
	public void export()
	{
		Object ref = model.getRefObject();
		if ((ref instanceof ImageData) || (ref instanceof WellSampleData)) {
			firePropertyChange(EXPORT_PROPERTY, Boolean.valueOf(false), 
					Boolean.valueOf(true));
		}
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
	 * @see MetadataViewer#onChannelColorChanged(int)
	 */
	public void onChannelColorChanged(int index)
	{
		view.onChannelColorChanged(index);
		firePropertyChange(CHANNEL_COLOR_CHANGED_PROPERTY, -1, index);
	}
	
}
