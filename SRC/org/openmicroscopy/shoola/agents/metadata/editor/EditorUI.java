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
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TreeComponent;

import pojos.ImageData;

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

	/**
	 * A reduced size for the invisible components used to separate widgets
	 * vertically.
	 */
    static final Dimension      SMALL_V_SPACER_SIZE = new Dimension(1, 5);
    
    
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
	private JPanel 						leftPane;
	
	/** The component hosting the {@link #viewedByUI}. */
	private TreeComponent 				viewByTree;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		propertiesUI = new PropertiesUI(model);
		attachmentsUI = new AttachmentsUI(model);
		linksUI = new LinksUI(model);
		rateUI = new RateUI(model);
		tagsUI = new TagsUI(model);
		textualAnnotationsUI = new TextualAnnotationsUI(model);
		viewedByUI = new ViewedByUI(model);
		topLeftPane = propertiesUI;
		leftPane = new JPanel();
		double[][] leftSize = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.PREFERRED} }; //rows
		leftPane.setLayout(new TableLayout(leftSize));
		viewByTree = new TreeComponent();
	}

	/** Builds and lays out the components. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, 20, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.FILL} }; //rows
		p.setLayout(new TableLayout(tl));
		
		p.add(rateUI, "0, 0");
		viewByTree.insertNode(viewedByUI, viewedByUI.getCollapseComponent(),
					false);
		viewByTree.addPropertyChangeListener(new PropertyChangeListener()
			{
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (TreeComponent.EXPANDED_PROPERTY.equals(name)) {
						boolean b = (Boolean) evt.getNewValue();
						if (!model.isThumbnailsLoaded()) {
							if (b) controller.loadThumbnails();
							else model.cancelThumbnailsLoading();
						}
					}
				}
			
			});
		p.add(viewByTree, "2, 0, 2, 1");
		TreeComponent left = new TreeComponent();
		left.insertNode(linksUI, linksUI.getCollapseComponent(), false);
		left.insertNode(attachmentsUI, 
						attachmentsUI.getCollapseComponent(), false);
		leftPane.add(topLeftPane, "0, 0");
		leftPane.add(p, "0, 1");
		leftPane.add(left, "0, 2");
		
		double[][] rigthSize = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
		JPanel rightPane = new JPanel();
		rightPane.setLayout(new TableLayout(rigthSize));
		TreeComponent tree = new TreeComponent();
		tree.insertNode(textualAnnotationsUI, 
						textualAnnotationsUI.getCollapseComponent());
		tree.insertNode(tagsUI, tagsUI.getCollapseComponent());
		rightPane.add(tree, "0, 0");
		
		
		double[][] finalSize = {{TableLayout.FILL, 5, TableLayout.FILL}, //columns
				{TableLayout.FILL} }; //rows
		setLayout(new TableLayout(finalSize));
		add(leftPane, "0, 0");
		add(rightPane, "2, 0");
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
    	viewByTree.setVisible((model.getRefObject() instanceof ImageData));
    	rateUI.buildUI();
    	viewedByUI.buildUI();
    	linksUI.buildUI();
    	rateUI.buildUI();
    	textualAnnotationsUI.buildUI();
    	tagsUI.buildUI();
    	attachmentsUI.buildUI();
    	propertiesUI.buildUI();
    	revalidate();
    	repaint();
    }

    /** Updates display when the new root node is set. */
	void setRootObject()
	{
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
	
}
