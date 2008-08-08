 /*
 * treeModel.FieldEditorDisplay 
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
package treeModel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import treeEditingComponents.EditingComponentFactory;
import treeEditingComponents.FieldEditorPanel;
import treeModel.fields.IField;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A container to display a FieldEditorPanel for the currently highlighted 
 * field of the JTree. 
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
	implements TreeSelectionListener { 
	
	/**
	 * The JTree to which this panel will listen for selection changes, and
	 * display the EditorPanel for the currently selected node. 
	 */
	private JTree tree;
	
	private JComponent currentDisplay;
	
	public FieldEditorDisplay(JTree tree) {
		
		
		this.tree = tree;
		
		tree.addTreeSelectionListener(this);
		
		setPanel();
	}


	public void valueChanged(TreeSelectionEvent e) {
		
		if (tree.getSelectionCount() == 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getSelectionPath().getLastPathComponent();
			IField field = (IField)node.getUserObject();
			JPanel fe = new FieldEditorPanel(field, tree, node);
			setPanel(fe);
		}
		else {
			setPanel();
		}
	}
	
	private void setPanel() {
		setPanel(new FieldEditorPanel());
	}

	private void setPanel(JComponent panel) {
		
		if (currentDisplay != null)
			this.remove(currentDisplay);
		
		currentDisplay = panel;
		
		this.add(currentDisplay);
		this.validate();
		this.repaint();
	}
}
