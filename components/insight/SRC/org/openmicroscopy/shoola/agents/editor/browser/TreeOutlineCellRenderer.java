 /*
 * treeModel.view.TreeOutlineCellRenderer 
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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextBoxParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * This TreeCellRenderer class merely extends DefaultTreeCellRenderer,
 * in order to provide the correct Icon for the Node in JTree.
 * 
 * This renderer is used by the Navigation tree. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeOutlineCellRenderer 
	extends DefaultTreeCellRenderer {

	/**
	 * The maximum length of characters to display in the node label
	 */
	private static final int 		MAX_CHARS = 25;
	
	/**
	 * Overrides {@link DefaultTreeCellRenderer#getTreeCellRendererComponent
	 * (JTree, Object, boolean, boolean, boolean, int, boolean)}
	 * to display the name of the field (or field contents if no name).
	 * Can also set Icon depending on type of parameters. 
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
	
		super.getTreeCellRendererComponent(tree, value, selected, expanded, 
				leaf, row, hasFocus);
		
		
		Icon paramIcon = null;
		String toolTipText = null;
		IconManager imF = IconManager.getInstance();
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object object = node.getUserObject();
			if (object instanceof IField) {
				IField field = (IField)object;
				toolTipText = field.getToolTipText();
				
				String text = getFieldDisplayName(field, node);
				
				if (field.getContentCount() < 1) {
					paramIcon = imF.getIcon(IconManager.TEXT_LINE_ICON);
					
				} else {
					IFieldContent content = field.getContentAt(0);
					
					String paramType = TextBoxParam.TEXT_BOX_PARAM;
					
					if (content instanceof IParam) {
						paramType = ((IParam)content).getAttribute(
								AbstractParam.PARAM_TYPE);
					}
					
					if (TextParam.TEXT_LINE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TEXT_LINE_ICON);
					else if (TextBoxParam.TEXT_BOX_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TEXT_BOX_ICON);
					else if (DateTimeParam.DATE_TIME_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.CALENDAR_ICON);
					else if (BooleanParam.BOOLEAN_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.CHECK_BOX);
					else if (EnumParam.ENUM_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.DROP_DOWN);
					else if (NumberParam.NUMBER_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.NUMBER);
				}
				
				setText(text);
				
			}
		}
		
        setIcon(paramIcon);
         
        if ((toolTipText != null) && (toolTipText.trim().length() > 0))
        { 
        	toolTipText = UIUtilities.formatToolTipText(toolTipText);
        	setToolTipText(toolTipText); 
        }

        return this;
	}
	
	/**
	 * A method for getting a nice display name for the field, using the 
	 * name attribute, or (if not set) use some content. 
	 * 
	 * @param field			The field. 
	 * @return				A display String
	 */
	public static String getFieldDisplayName(IField field, 
			DefaultMutableTreeNode node)
	{
		String text = field.getAttribute(Field.FIELD_NAME);
		
		if (text == null) {
			text = TreeModelMethods.getNodeName(node);
		}
		
		if (text.length() > MAX_CHARS) { 
			text = text.substring(0, MAX_CHARS-1) + "..";
		}
		
		return text;
	}
}
