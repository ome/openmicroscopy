/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorUI 
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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.TagAnnotationData;

/** 
 * Component hosting the various {@link AnnotationUI} entities.
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
public class EditorUI 
	extends JPanel
{
 
	/** 
	 * Size of the table layout used to lay out vertically components contained
	 * in the {@link #content} component.
	 */
	private static final double[][]		CONTENT_VERTICAL = {{TableLayout.FILL}, 
													{TableLayout.PREFERRED, 
													TableLayout.PREFERRED, 
													TableLayout.PREFERRED,
													TableLayout.PREFERRED}};
	
	/** 
	 * Size of the table layout used to lay out as a grid components contained
	 * in the {@link #content} component.
	 */
	private static final double[][]		CONTENT_GRID = {{TableLayout.FILL, 
														5, TableLayout.FILL}, 
														{TableLayout.PREFERRED, 
														TableLayout.PREFERRED}};
	
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
	/** The UI component displaying information about the image. */
	private ImageInfoUI					infoUI;
	
	/** The UI component displaying the object's properties. */
	private PropertiesUI				propertiesUI;
	
	/** The UI component displaying the attachments. */
	private AttachmentsUI				attachmentsUI;
	
	/** The UI component displaying the links. */
	private LinksUI						linksUI;
	
	/** The UI component displaying the rate. */
	private RateUI						rateUI;
	
	/** The UI component displaying the tags. */
	private TagsUI						tagsUI;
	
	/** The UI component displaying the textual annotations. */
	private TextualAnnotationsUI		textualAnnotationsUI;
	
	/** The UI component displaying the viewed by. */
	private ViewedByUI					viewedByUI;
	
	/** The UI component displaying the user's information. */
	private UserUI						userUI;
	
	/** The component displayed in the top left-hand side. */
	private JComponent					topLeftPane;
	
	/** The left-hand side panel. */
	//private JPanel 						leftPane;
	
	/** The component hosting the {@link #viewedByUI}. */
	private TreeComponent 				viewByTree;
	
	/** The component hosting the {@link #infoUI}. */
	private TreeComponent 				infoTree;

	/** The component hosting the {@link #commentsTree}. */
	private TreeComponent 				commentsTree;
	
	/** The tool bar with various controls. */
	private ToolBar						toolBarTop;
	
	/** The tool bar with various controls. */
	private ToolBar						toolBarBottom;
	
	/** The left hand side component. */
	private JPanel 						leftPane;

	 /** The UI component hosting the {@link #viewByTree}. */
	private JPanel 						viewTreePanel;
	
	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** 
	 * Flag indicating that an external component has been added 
	 * to the display.
	 */
	private boolean 					added;
	
	/** The component hosting all the components. */
	private JPanel 						content;
	
	/** The component layed out on the right-end side.*/
	private JPanel 						rightPane;
	
    /** 
     * Flag indicating that the data has already been saved and no new changes.
     */
    private boolean						saved;
    
    /** One of the layout constants defined by {@link Editor}. */
    private int							layout;
    
    /** Collection of trees. */
    private List<TreeComponent>			trees;
    
	/**
	 * Loads or cancels any on-going thumbnails loading.
	 * 
	 * @param b Pass <code>true</code> to load, <code>false</code> to cancel.
	 */
	private void loadThumbnails(boolean b)
	{
		viewedByUI.setExpanded(b);
		if (model.getRefObject() instanceof ImageData && 
				!model.isThumbnailsLoaded()) {
			if (b) controller.loadThumbnails();
			else model.cancelThumbnailsLoading();
		}
		viewedByUI.buildUI();
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		
		trees = new ArrayList<TreeComponent>();
		infoUI = new ImageInfoUI(model);
		userUI = new UserUI(model, controller);
		leftPane = new JPanel();
		toolBarTop = new ToolBar(model, this, controller, ToolBar.TOP);
		toolBarBottom = new ToolBar(model, this, controller, ToolBar.BOTTOM);
		propertiesUI = new PropertiesUI(model);
		attachmentsUI = new AttachmentsUI(model);
		linksUI = new LinksUI(model);
		rateUI = new RateUI(model);
		tagsUI = new TagsUI(model);
		textualAnnotationsUI = new TextualAnnotationsUI(model);
		viewedByUI = new ViewedByUI(model);
		topLeftPane = null;
		commentsTree = new TreeComponent();
		commentsTree.insertNode(textualAnnotationsUI, 
								textualAnnotationsUI.getCollapseComponent(),
								false);
		viewByTree = new TreeComponent();
		viewByTree.setVisible(false);
		viewByTree.insertNode(viewedByUI, viewedByUI.getCollapseComponent(),
								false);
		viewByTree.addPropertyChangeListener(new PropertyChangeListener()
			{
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
						loadThumbnails((Boolean) evt.getNewValue());
					}
				}
			
			});
		trees.add(viewByTree);
		infoTree = new TreeComponent();
		infoTree.setVisible(false);
		infoTree.insertNode(infoUI, infoUI.getCollapseComponent(), false);
		infoTree.addPropertyChangeListener(new PropertyChangeListener()
			{
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
						controller.showImageInfo();
					}
				}
			
			});
		trees.add(infoTree);
		components = new ArrayList<AnnotationUI>();
		components.add(propertiesUI);
		components.add(attachmentsUI);
		components.add(rateUI);
		components.add(tagsUI);
		components.add(textualAnnotationsUI);
		components.add(linksUI);
		Iterator<AnnotationUI> i = components.iterator();
		while (i.hasNext()) {
			i.next().addPropertyChangeListener(EditorControl.SAVE_PROPERTY,
											controller);
		}
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		viewTreePanel = new JPanel();
		viewTreePanel.setLayout(new BoxLayout(viewTreePanel, BoxLayout.X_AXIS));
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 0} }; //rows
		viewTreePanel.setLayout(new TableLayout(tl));
		
		viewTreePanel.add(rateUI, "0, 0");
		
		viewTreePanel.add(viewByTree, "0, 1");
		TreeComponent propertiesTree = new TreeComponent(); 
		trees.add(propertiesTree);
		propertiesTree.insertNode(propertiesUI, 
							propertiesUI.getCollapseComponent());
		TreeComponent left = new TreeComponent();
		trees.add(left);
		
		double h = TableLayout.PREFERRED;
		boolean expanded = false;
		double[][] leftSize = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
				0, h, TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
		leftPane.setLayout(new TableLayout(leftSize));
		
		leftPane.add(viewTreePanel, "0, 1");
		leftPane.add(infoTree, "0, 2");
		leftPane.add(propertiesTree, "0, 3");
		leftPane.add(commentsTree, "0, 4");
		leftPane.add(left, "0, 5");
		
		
		double[][] rigthSize = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		rightPane = new JPanel();
		rightPane.setLayout(new TableLayout(rigthSize));
		
		TreeComponent tree = new TreeComponent();
		trees.add(tree);
		
		rightPane.add(tree, "0, 0");
		content = new JPanel();
		
		switch (layout) {
			case Editor.VERTICAL_LAYOUT:
				//left.insertNode(textualAnnotationsUI, 
					//	textualAnnotationsUI.getCollapseComponent(), expanded);
				left.insertNode(tagsUI, tagsUI.getCollapseComponent(), 
								expanded);

				tree.insertNode(linksUI, linksUI.getCollapseComponent(), false);
				tree.insertNode(attachmentsUI, 
							attachmentsUI.getCollapseComponent(), false);

				content.setLayout(new TableLayout(CONTENT_VERTICAL));
				content.add(toolBarTop, "0, 0");
				content.add(leftPane, "0, 1");
				content.add(rightPane, "0, 2");
				content.add(toolBarBottom, "0, 3");
				break;
			case Editor.GRID_LAYOUT:
			default:
				left.insertNode(linksUI, linksUI.getCollapseComponent(), false);
				left.insertNode(attachmentsUI, 
							attachmentsUI.getCollapseComponent(), false);
				tree.insertNode(textualAnnotationsUI, 
						textualAnnotationsUI.getCollapseComponent());
				tree.insertNode(tagsUI, tagsUI.getCollapseComponent());
				content.setLayout(new TableLayout(CONTENT_GRID));
				content.add(toolBarTop, "0, 0, 2, 0");
				content.add(leftPane, "0, 1");
				content.add(rightPane, "2, 1");
				content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JScrollPane(content), BorderLayout.CENTER);
	}
	
	/** Creates a new instance. */
	EditorUI() {}
    
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param model         Reference to the Model. 
     * 						Mustn't be <code>null</code>.
     * @param controller	Reference to the Controller.
     * 						Mustn't be <code>null</code>.
     * @param layout		One of the layout constants defined by the 
	 * 						{@link Editor} I/F.
     */
    void initialize(EditorModel model, EditorControl controller, int layout)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        this.layout = layout;
        initComponents();
        buildGUI();
    }
    
    /** Lays out the UI when data are loaded. */
    void layoutUI()
    {
    	if (model.getRefObject() instanceof ExperimenterData)  {
    		toolBarTop.buildGUI();
    		userUI.buildUI();
    		userUI.repaint();
    	} else {
    		infoUI.buildUI();
    		rateUI.buildUI();
        	viewedByUI.buildUI();
        	linksUI.buildUI();
        	rateUI.buildUI();
        	textualAnnotationsUI.buildUI();
        	tagsUI.buildUI();
        	attachmentsUI.buildUI();
        	propertiesUI.buildUI();
        	toolBarTop.buildGUI();
        	toolBarBottom.buildGUI();
        	toolBarTop.setControls();
        	toolBarBottom.setControls();
        	setDataToSave(false);
        	if (added) addTopLeftComponent(topLeftPane);
        	Object refObject = model.getRefObject();
        	commentsTree.setTreeEnabled(true);
        	if (refObject instanceof ImageData) {
        		boolean count =  model.getViewedByCount() > 0;
        		viewByTree.setTreeEnabled(count);
        		if (count) {
        			if (viewedByUI.isExpanded())
            			loadThumbnails(true);
        		} else {
        			viewByTree.collapseNodes();
        			viewedByUI.setExpanded(false);
        		}
        		if (infoUI.isExpanded())
        			controller.showImageInfo();
        	} else if (refObject instanceof TagAnnotationData) {
        		commentsTree.collapseNodes();
        		propertiesUI.setObjectDescription();
        		//commentsTree.setTreeEnabled(false);
        	}
        	toolBarTop.setDecorator();
    	}
    	revalidate();
    	repaint();
    }

    /** Save data. */
	void saveData()
	{
		saved = true;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		toolBarTop.setDataToSave(false);
    	toolBarBottom.setDataToSave(false);
		if (model.getRefObject() instanceof ExperimenterData) {
			ExperimenterData exp = userUI.getExperimenterToSave();
			model.fireDataObjectSaving(exp);
			return;
		}
		propertiesUI.updateDataObject();
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		List<AnnotationData> toRemove = new ArrayList<AnnotationData>();
		List<AnnotationData> l = attachmentsUI.getAnnotationToSave();
		//To add
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = linksUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = rateUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = tagsUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		l = textualAnnotationsUI.getAnnotationToSave();
		if (l != null && l.size() > 0)
			toAdd.addAll(l);
		//To remove
		l = attachmentsUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		l = linksUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		l = rateUI.getAnnotationToRemove();
		if (l != null && l.size() > 0)
			toRemove.addAll(l);
		l = tagsUI.getAnnotationToRemove();
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
		toolBarTop.setDecorator();
		Object object = model.getRefObject();
		content.removeAll();
		//content.revalidate();
		//content.repaint();
	
		switch (layout) {
			case Editor.GRID_LAYOUT:
				content.setLayout(new TableLayout(CONTENT_GRID));
				if (object instanceof ExperimenterData) {
					content.add(toolBarTop, "0, 0, 2, 0");
					content.add(userUI, "0, 1, 2, 1");
					userUI.buildUI();
					revalidate();
			    	repaint();
					return;
				}
				content.add(toolBarTop, "0, 0, 2, 0");
				content.add(leftPane, "0, 1");
				content.add(rightPane, "2, 1");
				break;
			case Editor.VERTICAL_LAYOUT:
				content.setLayout(new TableLayout(CONTENT_VERTICAL));
				if (object instanceof ExperimenterData) {
					content.add(toolBarTop, "0, 0");
					content.add(userUI, "0, 1");
					content.add(toolBarBottom, "0, 2");
					userUI.buildUI();
					revalidate();
			    	repaint();
					return;
				}
				content.add(toolBarTop, "0, 0");
				content.add(leftPane, "0, 1");
				content.add(rightPane, "0, 2");
				content.add(toolBarBottom, "0, 3");
				break;
		}
		
		if (object instanceof ImageData) {
			((TableLayout) viewTreePanel.getLayout()).setRow(1, 
													TableLayout.FILL);
			((TableLayout) leftPane.getLayout()).setRow(2, 
					TableLayout.PREFERRED);
    		viewByTree.setVisible(true);
    		infoTree.setVisible(true);
    	} else {
    		((TableLayout) viewTreePanel.getLayout()).setRow(1, 0);
    		((TableLayout) leftPane.getLayout()).setRow(2, 0);
    		viewByTree.collapseNodes();
    		viewByTree.setVisible(false);
    		viewedByUI.setExpanded(false);
    		infoTree.setVisible(false);
    		infoTree.collapseNodes();
    		infoUI.setExpanded(false);
    	}
		if (topLeftPane != null) leftPane.remove(topLeftPane);
		TableLayout layout = (TableLayout) leftPane.getLayout();
		layout.setRow(0, 0);
		leftPane.revalidate();
		rateUI.clearDisplay();
		viewedByUI.clearDisplay();
    	linksUI.clearDisplay();
    	rateUI.clearDisplay();
    	textualAnnotationsUI.clearDisplay();
    	tagsUI.clearDisplay();
    	attachmentsUI.clearDisplay();
    	propertiesUI.clearDisplay();
    	propertiesUI.buildUI();
    	infoUI.clearDisplay();
		revalidate();
    	repaint();
	}
	
	/** Lays out the thumbnails. */
	void setThumbnails()
	{
		viewedByUI.buildUI();
		revalidate();
    	repaint();
	}

	/** Sets the existing tags. */
	void setExistingTags()
	{
		tagsUI.setExistingTags();
		revalidate();
    	repaint();
	}
	
	/**
	 * Displays the passed image.
	 * 
	 * @param thumbnail
	 */
	void setThumbnail(BufferedImage thumbnail)
	{
		ThumbnailCanvas canvas = new ThumbnailCanvas(model, thumbnail, null);
		if (topLeftPane != null) leftPane.remove(topLeftPane);
		topLeftPane = canvas;
		TableLayout layout = (TableLayout) leftPane.getLayout();
		layout.setRow(0, TableLayout.PREFERRED);
		leftPane.add(topLeftPane, "0, 0");
		leftPane.revalidate();
		revalidate();
    	repaint();
	}

	/** Shows the image's info. */
    void showChannelData()
    { 
    	Object refObject = model.getRefObject();
    	if (refObject instanceof ImageData) {
    		/*
    		PixelsData data = ((ImageData) refObject).getDefaultPixels();
    		Map<String, String> details = EditorUtil.transformPixelsData(data);
    		List waves = model.getChannelData();
            if (waves == null) return;
            String s = "";
            Iterator k = waves.iterator();
            int j = 0;
            while (k.hasNext()) {
                s += ((ChannelMetadata) k.next()).getEmissionWavelength();
                if (j != waves.size()-1) s +=", ";
                j++;
            }
            details.put(EditorUtil.WAVELENGTHS, s);
    		JFrame f = 
    			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
    		PixelsInfoDialog dialog = new PixelsInfoDialog(f, details);
    		UIUtilities.centerAndShow(dialog);
    		*/
    		infoUI.setChannelData(model.getChannelData());
    	}
    }

    /**
     * Enables the saving controls depending on the passed value.
     * 
     * @param b Pass <code>true</code> to save the data,
     * 			<code>false</code> otherwise.
     */
    void setDataToSave(boolean b)
    { 
    	toolBarTop.setDataToSave(b); 
    	toolBarBottom.setDataToSave(b); 
    }
    
    /**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		if (saved) return false;
		Object ref = model.getRefObject();
		if (!(ref instanceof DataObject)) return false;
		if (ref instanceof ExperimenterData)
			return userUI.hasDataToSave();
		if (!propertiesUI.isNameValid()) {
			setDataToSave(false);
			return false;
		}
		//setDataToSave(true);
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
		saved = false;
		Iterator<AnnotationUI> i = components.iterator();
		AnnotationUI ui;
		while (i.hasNext()) {
			ui = i.next();
			ui.clearData();
			ui.clearDisplay();
		}
	}

	/**
	 * Adds the specified component.
	 * 
	 * @param c The component to add.
	 */
	void addTopLeftComponent(JComponent c)
	{
		added = true;
		if (topLeftPane != null) leftPane.remove(topLeftPane);
		topLeftPane = c;
		TableLayout layout = (TableLayout) leftPane.getLayout();
		layout.setRow(0, TableLayout.PREFERRED);
		leftPane.add(topLeftPane, "0, 0");
		leftPane.revalidate();
		revalidate();
    	repaint();
	}
	
	/** Clears the password fields. */
	void passwordChanged() { userUI.passwordChanged(); }

	/** Displays the wizard with the collection of files already uploaded. */
	void setExistingAttachements() { attachmentsUI.showSelectionWizard(); }
	
	/** Displays the wizard with the collection of URLs already uploaded. */
	void setExistingURLs() { linksUI.showSelectionWizard(); }
	 
	/**
	 * Sets the disk space information.
	 * 
	 * @param space The value to set.
	 */
	void setDiskSpace(List space) { userUI.setDiskSpace(space); }
	
	/** Collapses all nodes. */
	void collapseAllNodes()
	{
		Iterator<TreeComponent> i = trees.iterator();
		TreeComponent comp;
		while (i.hasNext()) {
			comp = i.next();
			comp.collapseNodes();
		}
		revalidate();
    	repaint();
	}

}
