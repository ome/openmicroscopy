 /*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldRenderer 
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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


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
	extends DefaultTreeCellRenderer 
{
	/**
	 * The controller. This is passed to new tree nodes (fields) so that
	 * they have access to Actions etc. 
	 */
	private BrowserControl 			controller;
	
	/**
	 * Creates an instance.
	 * 
	 * @param controller	The controller that is passed to new tree nodes 
	 * 						(fields) so that they have access to Actions etc. 
	 */
	public FieldRenderer(BrowserControl controller) 
	{
		super();
		
		this.controller = controller;
	}

	/**
	 * Returns a {@link FieldPanel} to display at the node of the JTree.
	 * This method is also used to provide the CellEditor:
	 * @see DefaultFieldEditor#getTreeCellEditorComponent(JTree, Object, 
	 * 			boolean, boolean, boolean, int)
	 * 
	 * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
	 * 			boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) 
	{	
		String toolTipText;
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object object = node.getUserObject();
			if (object instanceof IField) {
				IField field = (IField)object;
				
				toolTipText = field.getToolTipText();
				
				FieldPanel fieldPanel = new FieldPanel(field, tree, 
						node, controller);
				
				fieldPanel.setSelected(selected);
				
				if ((toolTipText != null) && (toolTipText.trim().length() > 0))
		        { 
					toolTipText = UIUtilities.formatToolTipText(toolTipText);
		        	fieldPanel.setToolTipText(toolTipText); 
		        }
				
				return fieldPanel;
			}
		}
		
		return new JLabel(value.toString());
	}
	
	/**
	 * Return Null. Don't want any other icons
	 * 
	 * @see DefaultTreeCellRenderer#getLeafIcon()
	 */
	public Icon getLeafIcon() { return null; }
	
	/**
	 * Return Null. Don't want any other icons
	 * 
	 * @see DefaultTreeCellRenderer#getOpenIcon()
	 */
	public Icon getOpenIcon() { return null; }
	
	/**
	 * Return Null. Don't want any other icons
	 * 
	 * @see DefaultTreeCellRenderer#getClosedIcon()
	 */
	public Icon getClosedIcon() { return null; }
	
	/**
	 * Return 0. No icons: No gap! 
	 * 
	 * @see DefaultTreeCellRenderer#getIconTextGap()
	 */
	public int getIconTextGap() { return 0; }

}
