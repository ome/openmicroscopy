/*
 * org.openmicroscopy.shoola.agents.metadata.editor.GeneralPaneUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.TagAnnotationData;

/** 
 * Component displaying the annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class GeneralPaneUI 
	extends JScrollPane
{

	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
	/** Reference to the Model. */
	private EditorUI					view;
	
	/** The UI component displaying the object's properties. */
	private PropertiesUI				propertiesUI;
	
	/** The UI component displaying the textual annotations. */
	private TextualAnnotationsUI		textualAnnotationsUI;
	
	/** Component hosting the tags, rating, urls and attachments. */
	private AnnotationDataUI			annotationUI;
	
	/** The component hosting the {@link #browser}. */
	private JXTaskPane 					browserTaskPane;

	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** Main component. */
	private JPanel						content;
	
	/**
	 * Loads or cancels any on-going loading of containers hosting
	 * the edited object.
	 * 
	 * @param b Pass <code>true</code> to load, <code>false</code> to cancel.
	 */
	private void loadParents(boolean b)
	{
		if (b) controller.loadParents();
		else {
			view.setStatus(false);
			model.cancelParentsLoading();
		}
	}
	
    /** Initializes the UI components. */
	private void initComponents()
	{
		if (model.getBrowser() != null) {
			browserTaskPane = EditorUtil.createTaskPane(Browser.TITLE);
			browserTaskPane.add(model.getBrowser().getUI());
			browserTaskPane.addPropertyChangeListener(controller);
		}
		
		propertiesUI = new PropertiesUI(model);
		textualAnnotationsUI = new TextualAnnotationsUI(model);
		annotationUI = new AnnotationDataUI(model, controller);

		components = new ArrayList<AnnotationUI>();
		components.add(propertiesUI);
		components.add(textualAnnotationsUI);
		components.add(annotationUI);
		Iterator<AnnotationUI> i = components.iterator();
		while (i.hasNext()) {
			i.next().addPropertyChangeListener(EditorControl.SAVE_PROPERTY,
											controller);
		}
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		content = new JPanel();
		content.setBackground(UIUtilities.BACKGROUND);
		double[][]	size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
				TableLayout.PREFERRED, 0}};
		int i = 0;
		content.setLayout(new TableLayout(size));

		content.add(propertiesUI, "0, "+i);
		i++;
		content.add(new JSeparator(), "0, "+i);
		i++;
		content.add(annotationUI, "0, "+i);
		i++;
		content.add(new JSeparator(), "0, "+i);
		i++;
		content.add(textualAnnotationsUI, "0, "+i);
		i++;
		content.add(browserTaskPane, "0, "+i);
		getViewport().add(content);
	}
    
	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 */
	GeneralPaneUI(EditorUI view, EditorModel model, EditorControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.view = view;
		initComponents();
        buildGUI();
	}

	
	/** Lays out the UI when data are loaded. */
	void layoutUI()
	{
		propertiesUI.buildUI();
		annotationUI.buildUI();

		TableLayout layout = (TableLayout) content.getLayout();
		int n = layout.getNumRow();
		double h = 0;
		String s = "";
		Object refObject = model.getRefObject();
		if (refObject instanceof TagAnnotationData) {
			propertiesUI.setObjectDescription();
			browserTaskPane.setCollapsed(true);
			if (model.hasTagsAsChildren()) {
				//tagsTaskPane.setCollapsed(true);
				//tagsTaskPane.setTreeEnabled(false);
			}
		} else if  (refObject instanceof DatasetData) {
			//tagsTaskPane.setCollapsed(true);
			//tagsTaskPane.setTreeEnabled(false);
			h = TableLayout.PREFERRED;
			s = "Contained in Projects";
		}  else if (refObject instanceof ImageData) {
			h = TableLayout.PREFERRED;
			s = "Contained in Datasets";
			controller.loadChannelData();
		}
		browserTaskPane.setTitle(s);
		layout.setRow(n-1, h);
	}
   
	/** Save data. */
	void saveData()
	{
		if (!model.isMultiSelection()) propertiesUI.updateDataObject();
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		List<AnnotationData> toRemove = new ArrayList<AnnotationData>();
		List<AnnotationData> l = annotationUI.getAnnotationToSave();
		//To add
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = textualAnnotationsUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		//To remove
		l = annotationUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		l = textualAnnotationsUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		model.fireAnnotationSaving(toAdd, toRemove);
	}
	
	/** Updates display when the new root node is set. */
	void setRootObject()
	{
		clearData();
		textualAnnotationsUI.clearDisplay();
		propertiesUI.clearDisplay();
		annotationUI.clearDisplay();
    	textualAnnotationsUI.buildUI();
    	if (!model.isMultiSelection())
    		propertiesUI.buildUI();
		revalidate();
    	repaint();
	}
	
	/** Lays out the thumbnails. */
	void setThumbnails() { annotationUI.setThumbnails(); }
	
	/** Sets the existing tags. */
	void setExistingTags()
	{
		annotationUI.setExistingTags();
		revalidate();
    	repaint();
	}
	
	/** Shows the image's info. */
    void setChannelData()
    { 
    	Object refObject = model.getRefObject();
    	if (refObject instanceof ImageData) 
    		propertiesUI.setChannelData(model.getChannelData());
    }
    
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		Iterator<AnnotationUI> i = components.iterator();
		boolean b = false;
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			if (ui.hasDataToSave()) {
				b = true;
				break;
			}
		}
		return b;
	}
	
	/** Clears data to save. */
	void clearData()
	{
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			ui.clearData();
			ui.clearDisplay();
		}
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * Handles the expansion or collapsing of the passed component.
	 * 
	 * @param source The component to handle.
	 */
	void handleTaskPaneCollapsed(JXTaskPane source)
	{
		if (source == null) return;
		if  (source.equals(browserTaskPane)) 
			loadParents(!browserTaskPane.isCollapsed());
	}

	/**
	 * Attaches the passed file.
	 * 
	 * @param file The file to attach.
	 */
	void attachFile(File file) { annotationUI.attachFile(file); }

	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(File file)
	{ 
		annotationUI.removeAttachedFile(file);
	}
	
}
