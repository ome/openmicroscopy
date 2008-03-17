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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.util.PixelsInfoDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * 
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
 
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
	
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
	
	/** The component displayed in the top left-hand side. */
	private JComponent					topLeftPane;
	
	/** The left-hand side panel. */
	//private JPanel 						leftPane;
	
	/** The component hosting the {@link #viewedByUI}. */
	private TreeComponent 				viewByTree;
	
	/** The tool bar with various control. */
	private ToolBar						toolBar;
	
	/** The left hand side component. */
	private JPanel 						leftPane;

	/** Collection of annotations UI components. */
	private List<AnnotationUI>			components;
	
	/** 
	 * Flag indicating that an external component has been added 
	 * to the display.
	 */
	private boolean 					added;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		leftPane = new JPanel();
		toolBar = new ToolBar(model, this);
		propertiesUI = new PropertiesUI(model);
		attachmentsUI = new AttachmentsUI(model);
		linksUI = new LinksUI(model);
		rateUI = new RateUI(model);
		tagsUI = new TagsUI(model);
		textualAnnotationsUI = new TextualAnnotationsUI(model);
		viewedByUI = new ViewedByUI(model);
		topLeftPane = null;
		
		viewByTree = new TreeComponent();
		
		viewByTree.insertNode(viewedByUI, viewedByUI.getCollapseComponent(),
				false);
		viewByTree.addPropertyChangeListener(new PropertyChangeListener()
			{
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
						boolean b = (Boolean) evt.getNewValue();
						viewedByUI.setExpanded(b);
						if (!model.isThumbnailsLoaded() &&
								model.getRefObject() instanceof ImageData) {
							if (b) controller.loadThumbnails();
							else model.cancelThumbnailsLoading();
						}
						viewedByUI.buildUI();
					}
				}
			
			});
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
		JPanel viewTreePanel = new JPanel();
		viewTreePanel.setLayout(new BoxLayout(viewTreePanel, BoxLayout.X_AXIS));
		double[][] tl = {{TableLayout.PREFERRED, 20, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.FILL} }; //rows
		viewTreePanel.setLayout(new TableLayout(tl));
		
		viewTreePanel.add(rateUI, "0, 0");
		
		viewTreePanel.add(viewByTree, "2, 0, 2, 1");
		TreeComponent left = new TreeComponent();
		left.insertNode(propertiesUI, propertiesUI.getCollapseComponent());
		left.insertNode(linksUI, linksUI.getCollapseComponent(), false);
		left.insertNode(attachmentsUI, 
						attachmentsUI.getCollapseComponent(), false);

		double[][] leftSize = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.PREFERRED} }; //rows
		leftPane.setLayout(new TableLayout(leftSize));
		
		leftPane.add(viewTreePanel, "0, 1");
		leftPane.add(left, "0, 2");

		double[][] rigthSize = {{TableLayout.FILL}, //columns
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		JPanel rightPane = new JPanel();
		rightPane.setLayout(new TableLayout(rigthSize));
		TreeComponent tree = new TreeComponent();
		tree.insertNode(textualAnnotationsUI, 
						textualAnnotationsUI.getCollapseComponent());
		tree.insertNode(tagsUI, tagsUI.getCollapseComponent());
		rightPane.add(tree, "0, 0");
		
		JPanel content = new JPanel();
		double[][] finalSize = {{TableLayout.FILL, 5, TableLayout.FILL}, 
								{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		
		content.setLayout(new TableLayout(finalSize));
		content.add(toolBar, "0, 0, 2, 0");
		content.add(leftPane, "0, 1");
		content.add(rightPane, "2, 1");
		
		content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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
     */
    void initialize(EditorModel model, EditorControl controller)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /** Lays out the UI when data are loaded. */
    void layoutUI()
    {
    	rateUI.buildUI();
    	viewedByUI.buildUI();
    	linksUI.buildUI();
    	rateUI.buildUI();
    	textualAnnotationsUI.buildUI();
    	tagsUI.buildUI();
    	attachmentsUI.buildUI();
    	propertiesUI.buildUI();
    	toolBar.buildGUI();
    	toolBar.setControls();
    	setDataToSave(false);
    	if (added) addTopLeftComponent(topLeftPane);
    	revalidate();
    	repaint();
    }

    /** Save data. */
	void saveData()
	{
		propertiesUI.updateDataObject();
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		List<AnnotationData> toRemove = new ArrayList<AnnotationData>();
		List<AnnotationData> l = attachmentsUI.getAnnotationToSave();
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
		model.fireAnnotationSaving(toAdd, toRemove);
	}
	
    /** Updates display when the new root node is set. */
	void setRootObject()
	{
		//removeAll();
		//buildGUI();
		if (model.getRefObject() instanceof ImageData) {
    		viewByTree.setVisible(true);
    	} else {
    		viewByTree.collapseNodes();
    		viewByTree.setVisible(false);
    		viewedByUI.setExpanded(false);
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
    	toolBar.setDataToSave(b);
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
	
}
