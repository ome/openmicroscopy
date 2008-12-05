/*
 * org.openmicroscopy.shoola.agents..editor.browser. 
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
package org.openmicroscopy.shoola.agents.editor.browser;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.tree.TreeModel;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.ScrollablePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;

/** 
 * This UI contains a {@link JXTaskPane} showing the metadata preview for an 
 * OMERO.editor file.
 * Based on code from 
 * {@link org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionDataUI}
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
class MetadataUI 
	extends ScrollablePanel
	implements PropertyChangeListener
{
	/** Reference to the controller. */
	private BrowserControl						controller;
	
	/** Reference to the Model. */
	private TreeModel							model;
		
	/** Reference to the Model. */
	private BrowserUI							view;
	
	/** The component hosting the image acquisition data. */
	private MetadataPanelsComponent			imageAcquisition;
	
	/** The component hosting the image info. */
	private JXTaskPane 							imagePane;
	
	/** The UI component hosting the <code>JXTaskPane</code>s. */
	private JPanel								container;
	
	/** The constraints used to lay out the components in the container. */
	private GridBagConstraints					constraints;

	/** Default text if no data entered. */
	static final String DEFAULT_TEXT = "None";
	
	/** Initializes the UI components. */
	private void initComponents()
	{  
		container = new JPanel();
		
		imageAcquisition = new MetadataPanelsComponent(this, model);
		imageAcquisition.addPropertyChangeListener(this);
		imagePane = EditorUtil.createTaskPane("OMERO.editor");
		imagePane.setCollapsed(false);
		imagePane.add(imageAcquisition);
		imagePane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
	}
	
	/**
	 * Allows the model to be set after this UI has been created.
	 * 
	 * @param treeModel		new model
	 */
	void setTreeModel(TreeModel treeModel)
	{	
		model = treeModel;
		imageAcquisition.setTreeModel(treeModel);
		
		String rootName = model.getRoot().toString();
		imagePane.setTitle(rootName);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BorderLayout(0, 0));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.weightx = 1.0;
        container.add(imagePane, constraints);
        
        add(container, BorderLayout.NORTH);
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
	MetadataUI(BrowserUI view, TreeModel model, 
				BrowserControl controller)
	{
		//if (model == null)
		//	throw new IllegalArgumentException("No model.");
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
	
	/** 
	 * Lays out the passed component.
	 * 
	 * @param pane 		The main component.
	 * @param button	The button to show or hide the unset fields.
	 * @param fields	The fields to lay out.
	 * @param shown		Pass <code>true</code> to show the unset fields,
	 * 					<code>false</code> to hide them.
	 */
	void layoutFields(JPanel pane, JButton button, 
			List<MetadataComponent> fields, boolean shown)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
        
		for (MetadataComponent comp : fields) {
            c.gridx = 0;
            if (comp.isSetField() || shown) {
            	 ++c.gridy;
            	 c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
                 c.fill = GridBagConstraints.NONE;      //reset to default
                 c.weightx = 0.0;  
                 pane.add(comp.getLabel(), c);
                 c.gridx++;
                 pane.add(Box.createHorizontalStrut(5), c); 
                 c.gridx++;
                 c.gridwidth = GridBagConstraints.REMAINDER;     //end row
                 c.fill = GridBagConstraints.HORIZONTAL;
                 c.weightx = 1.0;
                 System.out.println("AcquisitionDataUI layoutFields comp: " + 
                		 comp.getLabel().getText());
                 pane.add(comp.getArea(), c);  
            } 
        }
        ++c.gridy;
        c.gridx = 0;
        //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        if (button != null) pane.add(button, c);
	}
	
	
	/**
	 * Loads the acquisition metadata.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (!UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) return;
		
		
	}
}
