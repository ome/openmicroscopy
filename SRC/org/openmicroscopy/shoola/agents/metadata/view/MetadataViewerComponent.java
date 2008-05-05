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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

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
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
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
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
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
			//if (!model.isSameObject((DataObject) userObject))
				model.fireStructuredDataLoading(node);
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
		//
		Object userObject = node.getUserObject();
		Object refObject = model.getRefObject();
		if (refObject == userObject) {
			Browser browser = model.getBrowser();
			if (result instanceof StructuredDataResults) {
				browser.setStructuredDataResults(node, 
												(StructuredDataResults) result);
				model.getEditor().setStructuredDataResults( 
									(StructuredDataResults) result);
				model.setStructuredDataResults((StructuredDataResults) result);
				view.setOnScreen();
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
		model.setRootObject(root);
		view.setRootObject();
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
		browser.setParents((TreeBrowserSet) node, (Collection) result);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#saveData(List, List, DataObject)
	 */
	public void saveData(List<AnnotationData> toAdd, 
				List<AnnotationData> toRemove, DataObject data)
	{
		if (data == null) return;
		Collection<DataObject> siblings = model.getSiblings();
		List<DataObject> toSave = new ArrayList<DataObject>();
		if (siblings == null || siblings.size() <= 1) {
			toSave.add(data);
			model.fireSaving(toAdd, toRemove, toSave);
		} else {
			MessageBox dialog = new MessageBox(view, "Save Annotations", 
								"Do you want to annotate: ");
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			
			String name = "";
			String parentName = null;
			if (data instanceof ImageData) {
				name = "image";
				parentName = "dataset";
			} else if (data instanceof DatasetData) name = "dataset";
			else if (data instanceof ProjectData) name = "project";
			
			ButtonGroup group = new ButtonGroup();
			JRadioButton single = new JRadioButton();
			//single.setText("Only "+name+" "+model.getRefObjectName());
			single.setText("The selected "+name);
			single.setSelected(true);
			p.add(single);
			group.add(single);
			JRadioButton batchAnnotation = new JRadioButton();
			if (parentName != null) {
				batchAnnotation.setText("All "+name+"s in "+parentName);
				p.add(batchAnnotation);
				group.add(batchAnnotation);
			}
			JRadioButton all = new JRadioButton();
			all.setText("All selected "+name+"s");
			p.add(all);
			group.add(all);
			dialog.addBodyComponent(p);
			int option = dialog.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				if (all.isSelected()) toSave.addAll(siblings);
				else toSave.add(data);
				model.fireSaving(toAdd, toRemove, toSave);
			} else if (option == MessageBox.NO_OPTION) {
				clearDataToSave();
			}
		}
		
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
	 * @see MetadataViewer#addExternalComponent(JComponent, int)
	 */
	public void addExternalComponent(JComponent external, int location)
	{
		if (external == null)
			throw new IllegalArgumentException("No component to add.");
		Editor editor = model.getEditor();
		if (editor == null) return;
		editor.addComponent(external, location);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#showImageInfo()
	 */
	public void showImageInfo()
	{
		Editor editor = model.getEditor();
		if (editor == null) return;
		editor.showImageInfo();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#onDataSave(Collection)
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
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setSiblings(Collection)
	 */
	public void setSiblings(Collection<DataObject> siblings)
	{
		model.setSiblings(siblings);
	}
	
}
