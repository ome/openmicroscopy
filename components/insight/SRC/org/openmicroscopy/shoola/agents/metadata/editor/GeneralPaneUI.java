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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.WellSampleData;

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
	extends JPanel//JScrollPane
{

	/** The default text. */
	private static final String			DETAILS = "'s details";
	
	/** The protocols title. */
	private static final String			PROTOCOL = "Protocols and Experiments";
	
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
	
	/** Component hosting the tags, rating, URLs and attachments. */
	private AnnotationDataUI			annotationUI;
	
	/** The component hosting the {@link #browser}. */
	private JXTaskPane 					browserTaskPane;

	/** The component hosting the {@link #propertiesUI}. */
	private JXTaskPane 					propertiesTaskPane;
	
	/** The component hosting the annotation component. */
	private JXTaskPane 					annotationTaskPane;
	
	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;

	/** The component hosting the various protocols. */
	private JXTaskPane					protocolTaskPane;
	
	/** Collection of preview panels. */
	private List<PreviewPanel>			previews;
	
	/** Flag indicating to build the UI once. */
	private boolean 					init;

	/** The container hosting the <code>JXTaskPane</code>. */
	private JXTaskPaneContainer 		container;
	
	/** The tool bar.*/
	private ToolBar toolbar;
	
	/**;
	 * Loads or cancels any on-going loading of containers hosting
	 * the edited object.
	 * 
	 * @param b Pass <code>true</code> to load, <code>false</code> to cancel.
	 */
	private void loadParents(boolean b)
	{
		if (b) controller.loadParents();
		else {
			model.cancelParentsLoading();
		}
	}
	
    /** Initializes the UI components. */
	private void initComponents()
	{
		container  = new JXTaskPaneContainer();
		container.setBackground(UIUtilities.BACKGROUND);
		if (container.getLayout() instanceof VerticalLayout) {
			VerticalLayout vl = (VerticalLayout) container.getLayout();
			vl.setGap(0);
		}
		if (model.getBrowser() != null) {
			browserTaskPane = EditorUtil.createTaskPane(Browser.TITLE);
			browserTaskPane.add(model.getBrowser().getUI());
			browserTaskPane.addPropertyChangeListener(controller);
		}
		
		protocolTaskPane = EditorUtil.createTaskPane(PROTOCOL);
		
		propertiesUI = new PropertiesUI(model, controller);
		textualAnnotationsUI = new TextualAnnotationsUI(model, controller);
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
		previews = new ArrayList<PreviewPanel>();
		propertiesTaskPane = EditorUtil.createTaskPane("");
		propertiesTaskPane.setCollapsed(false);
		propertiesTaskPane.add(propertiesUI);
		annotationTaskPane = EditorUtil.createTaskPane("Annotations");
		annotationTaskPane.setCollapsed(false);
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(annotationUI);
		p.add(textualAnnotationsUI);
		annotationTaskPane.add(p);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		container.add(propertiesTaskPane);
		container.add(annotationTaskPane);
		JScrollPane pane = new JScrollPane();
		JViewport viewport = pane.getViewport();
		viewport.add(container);
		viewport.setBackground(UIUtilities.BACKGROUND_COLOR);
		add(toolbar, BorderLayout.NORTH);
    	add(pane, BorderLayout.CENTER);
	}
	
	/** 
	 * Lays out the protocols files. Returns the components hosting the 
	 * files.
	 * 
	 * @return See above.
	 */
	private JXTaskPaneContainer buildProtocolTaskPanes()
	{
		Collection list = model.getAttachments();
		if (list.size() == 0) return null;
		JXTaskPaneContainer paneContainer = new JXTaskPaneContainer();
		VerticalLayout vl = (VerticalLayout) paneContainer.getLayout();
		vl.setGap(0);
		paneContainer.setBackground(UIUtilities.BACKGROUND_COLOR);

		Iterator i = list.iterator();
		FileAnnotationData fa;
		JXTaskPane pane;
		PreviewPanel preview;
		String description;
		String ns;
		int index = 0;
		previews.clear();
		boolean b;
		while (i.hasNext()) {
			fa = (FileAnnotationData) i.next();
			ns = fa.getNameSpace();
			b = annotationUI.isEditorFile(fa.getFileName());
			if (!b) b = annotationUI.isEditorFile(ns);
			if (fa.getId() > 0 && b) {
				description = fa.getDescription();
				if (description != null) {
					preview = new PreviewPanel(description, fa.getId());
					previews.add(preview);
					preview.addPropertyChangeListener(controller);
					pane = EditorUtil.createTaskPane(fa.getFileName());
					pane.addPropertyChangeListener(controller);
					pane.add(preview);
					paneContainer.add(pane);
					index++;
				}
			}
		}
		if (index == 0) return null;
		return paneContainer;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 * @param tooBar 		The tool Bar
	 */
	GeneralPaneUI(EditorUI view, EditorModel model, EditorControl controller, 
			ToolBar toolBar)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.toolbar = toolBar;
		this.view = view;
		initComponents();
		init = false;
	}

	/** Lays out the UI when data are loaded. */
	void layoutUI()
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		propertiesUI.buildUI();
		annotationUI.buildUI();
		textualAnnotationsUI.buildUI();
		propertiesTaskPane.setTitle(propertiesUI.getText()+DETAILS);

	
		//TableLayout layout = (TableLayout) content.getLayout();
		
		double h = 0;
		String s = "";
		boolean multi = model.isMultiSelection();
		Object refObject = model.getRefObject();
		if (refObject instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) refObject;
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
					tag.getNameSpace())) {
				browserTaskPane.setCollapsed(true);
			} else {
				if (!multi) {
					h = 1;
					s = "Contained in Tag Sets";
				}
			}
		} else if (refObject instanceof FileAnnotationData) {
			if (!multi) {
				h = 1;
				s = "Attached to...";
			}
		} else if (refObject instanceof DatasetData) {
			if (!multi) {
				h = 1;
				s = "Contained in Projects";
			}
		} else if (refObject instanceof ImageData) {
			if (!multi) {
				h = 1;
				s = "Contained in Datasets";
				controller.loadChannelData();
			}
		} else if (refObject instanceof WellSampleData) {
			if (!multi) controller.loadChannelData();
		} else if ((refObject instanceof ProjectData) || 
				(refObject instanceof ScreenData) ||
				(refObject instanceof WellSampleData)) {
			browserTaskPane.setCollapsed(true);
		}
		browserTaskPane.setTitle(s);
		container.remove(browserTaskPane);
		if (protocolTaskPane != null) {
			container.remove(protocolTaskPane);
			protocolTaskPane.removeAll();
		}
		JComponent n = buildProtocolTaskPanes();
		if (n != null) {
			protocolTaskPane.add(n);
			container.add(protocolTaskPane);
		}
		
		
		if (h > 0) {
			container.add(browserTaskPane);
			if (!browserTaskPane.isCollapsed())
				loadParents(true);
		}
	}
	
	/** 
	 * Returns an array of size 2 with the collection of 
	 * annotation to save. 
	 * 
	 * @return See above.
	 */
	Map<Integer, List<AnnotationData>> prepareDataToSave()
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
		Map<Integer, List<AnnotationData>> 
			map = new HashMap<Integer, List<AnnotationData>>();
		map.put(EditorUI.TO_ADD, toAdd);
		map.put(EditorUI.TO_REMOVE, toRemove);
		return map;
	}
	
	/** Updates display when the parent of the root node is set. */
	void setParentRootObject()
	{
		propertiesUI.setParentRootObject();
	}
	
	/** Updates display when the new root node is set. */
	void setRootObject()
	{
		if (!init) {
			buildGUI();
			init = true;
		}	
		clearData();
		textualAnnotationsUI.clearDisplay();
		propertiesUI.clearDisplay();
		annotationUI.clearDisplay();
    	textualAnnotationsUI.clearDisplay();
    	//propertiesUI.buildUI();
    	Object uo = model.getRefObject();
    	
    	int annotation = 0;
    	if (!(uo instanceof AnnotationData)) { //hide everything
    		annotation = 1;
    	}
		container.remove(annotationTaskPane);
		//container.remove(protocolTaskPane);
		if (annotation > 0) 
			container.add(annotationTaskPane);
		/*
		if (protocolTaskPane.getComponentCount() > 0)
			container.add(protocolTaskPane);
		*/
		revalidate();
		repaint();
	}
	
	/**
	 * Returns the list of tags currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> getCurrentTagsSelection()
	{
		return annotationUI.getCurrentTagsSelection();
	}
	
	/**
	 * Returns the list of attachments currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getCurrentAttachmentsSelection() 
	{
		return annotationUI.getCurrentAttachmentsSelection();
	}
	
	/** Shows the image's info. */
    void setChannelData()
    { 
    	Object refObject = model.getRefObject();
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData))
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
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			if (ui.hasDataToSave())
				return true;
		}
		Iterator<PreviewPanel> p = previews.iterator();
		PreviewPanel pp;
		while (p.hasNext()) {
			pp = p.next();
			if (pp.hasDataToSave()) return true;
		}
		return false;
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
	 * Attaches the passed files.
	 * Returns <code>true</code> if the files
	 * do not already exist, <code>false</code> otherwise.
	 * 
	 * @param files The files to attach.
	 * @return See above
	 */
	boolean attachFiles(File[] files)
	{ 
		return annotationUI.attachFiles(files); 
	}

	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{ 
		annotationUI.removeAttachedFile(file);
	}
	
	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> removeAttachedFiles()
	{
		return annotationUI.removeAttachedFiles();
	}
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> removeTags()
	{
		return annotationUI.removeTags();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlink, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAttachmentsToUnlink()
	{ 
		return annotationUI.hasAttachmentsToUnlink();
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlink, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasTagsToUnlink()
	{
		return annotationUI.hasTagsToUnlink();
	}
	
	/**
	 * Removes the annotation from the view.
	 * 
	 * @param annotation The annotation to remove.
	 */
	void removeObject(DataObject annotation)
	{
		if (annotation == null) return;
		if (annotation instanceof TagAnnotationData)
			annotationUI.removeTag((TagAnnotationData) annotation);
		else if (annotation instanceof TextualAnnotationData)
			textualAnnotationsUI.removeTextualAnnotation(
					(TextualAnnotationData) annotation);
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type		The type of objects to handle.
	 * @param objects   The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
		if (objects == null) return;
		annotationUI.handleObjectsSelection(type, objects, true);
	}
	
}
