 /*
 * editorDynamicTree.FieldRenderer 
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

//Java imports

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

import fields.FieldPanel;
import fields.IField;



/** 
 * The Renderer for JTree, to build the "Form" of hierarchical fields.
 * 
 * The getTreeCellRendererComponent() method returns a JPanel, which is 
 * used to build the Non-Editable tree UI (before editing starts). 
 * This JPanel is created by obtaining the UserObject for the node of
 * the tree. If this is an instance of IField, it is used to create
 * a JPanel. Otherwise it returns a JLabel. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldRenderer 
	extends DefaultTreeCellRenderer {
	
	public FieldRenderer() {
		super();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object object = node.getUserObject();
			if (object instanceof IField) {
				IField field = (IField)object;
				FieldPanel fieldPanel = new FieldPanel(field, tree, node);
				
				
				//fieldPanel.setTree(tree);
				//fieldPanel.setTreeNode(node);
				
				fieldPanel.setSelected(selected);
				
				return fieldPanel;
			}
		}
		
		System.out.println(value.getClass());
		return new JLabel(value.toString());
	}
	
	public Icon getLeafIcon() {
		return null;
	}
	
	public Icon getOpenIcon() {
		return null;
	}
	
	public Icon getClosedIcon() {
		return null;
	}
	
	public int getIconTextGap() {
		return 0;
	}

}
