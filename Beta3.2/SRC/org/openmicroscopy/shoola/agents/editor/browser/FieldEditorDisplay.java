 /*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldEditorDisplay 
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
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.FieldEditorPanel;
import org.openmicroscopy.shoola.agents.editor.model.IField;

/** 
 * A container to display a {@link FieldEditorPanel} for the currently 
 * highlighted field of the JTree. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldEditorDisplay 
	extends JPanel 
	implements TreeSelectionListener,
	TreeModelListener,
	PropertyChangeListener
{ 
	
	/**
	 * The JTree to which this panel will listen for selection changes, and
	 * display the EditorPanel for the currently selected node. 
	 */
	private JTree 						tree;
	
	/**
	 * A reference to the controller, for editing the data,
	 * managing undo/redo, etc.
	 */
	private BrowserControl	 			controller;
	
	/**
	 * A scroll pane to hold the {@link FieldEditorPanel}
	 */
	private JScrollPane 				scrollPane;
	
	/**
	 * The currently displayed panel. Keep a reference to this so that
	 * when it disappears, this class can be removed as a 
	 * {@link PropertyChangeListener}
	 */
	private JComponent 					currentDisplay;
	
	/**
	 * Refreshes the content of this panel, based on the currently selected
	 * node of the {@link #tree}
	 */
	private void refreshEditorDisplay() 
	{
		if (tree.getSelectionCount() == 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getSelectionPath().getLastPathComponent();
			IField field = (IField)node.getUserObject();
			FieldEditorPanel fe = new FieldEditorPanel(field, tree, node,
					controller);
			setPanel(fe);
		}
		else {
			setPanel();
		}
	}

	/**
	 * Sets the content of this panel with a blank {@link JPanel}.
	 * Calls {@link #setPanel(JComponent)}
	 * 
	 * @see #setPanel(JComponent)
	 */
	private void setPanel() {
		setPanel(new JPanel());
	}

	/**
	 * Sets the content of this UI with the specified panel, replacing the 
	 * current component.
	 * 
	 * @param panel		The new panel to display
	 */
	private void setPanel(JComponent panel) 
	{	
		if (currentDisplay != null) {
			currentDisplay.removePropertyChangeListener
				(FieldEditorPanel.PANEL_CHANGED_PROPERTY, this);
		}
		
		currentDisplay = panel;
		currentDisplay.addPropertyChangeListener
		(FieldEditorPanel.PANEL_CHANGED_PROPERTY, this);
		
		scrollPane.setViewportView(currentDisplay);
		validate();
		repaint();
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI() 
	{
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.white);
		
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Creates an instance.
	 * 
	 * @param tree			The JTree that contains the Field we're editing.
	 * 				Listen to selection changes and display the current field
	 * @param controller	A reference to the BrowserControl, for editing etc
	 */
	public FieldEditorDisplay(JTree tree, BrowserControl controller) 
	{
		this.controller = controller;
		this.tree = tree;
		
		tree.addTreeSelectionListener(this);
		
		buildUI();
		
		setPanel();
	}

	/**
	 * Listen for changes in selection of the JTree, and call
	 * {@link #refreshEditorDisplay()}
	 * 
	 * Implemented as specified by the {@link TreeSelectionListener} interface
	 * 
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) 
	{	
		refreshEditorDisplay();
	}

	/**
	 * Listens for changes in the displayed {@link FieldEditorPanel}.
	 * If the {@link FieldEditorPanel#PANEL_CHANGED_PROPERTY} property
	 * changes, the panel needs to be refreshed and
	 * {@link #refreshEditorDisplay()} is called.
	 * 
	 * Implemented as specified by the {@link PropertyChangeListener} interface
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (FieldEditorPanel.PANEL_CHANGED_PROPERTY
				.equals(evt.getPropertyName())) {
			refreshEditorDisplay();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshEditorDisplay()} to update the node, even if 
	 * there is no change in node selection.
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) 
	{
		refreshEditorDisplay();
	}

	/**
	 * Implemented as required by the {@link TreeModelListener} interface.
	 * No operation implementation in this case
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {}

	/**
	 * Implemented as required by the {@link TreeModelListener} interface.
	 * No operation implementation in this case
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {}

	/**
	 * Implemented as required by the {@link TreeModelListener} interface.
	 * No operation implementation in this case
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {}
}
