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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.ImageParam;
import org.openmicroscopy.shoola.agents.editor.model.params.LinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TableParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TimeParam;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
				
				if (field.getContentCount() < 1) {
					//paramIcon = imF.getIcon(ImageFactory.)
				} else {
					IFieldContent content = field.getContentAt(0);
					String paramType = SingleParam.TEXT_BOX_PARAM;
					
					if (content instanceof IParam) {
						paramType = ((IParam)content).getAttribute(
								AbstractParam.PARAM_TYPE);
					}
					
					if (SingleParam.TEXT_LINE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TEXT_LINE_ICON);
					else if (SingleParam.TEXT_BOX_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TEXT_BOX_ICON);
					else if (DateTimeParam.DATE_TIME_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.CALENDAR_ICON);
					else if (TimeParam.TIME_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TIMER_ICON);
					else if (TableParam.TABLE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.TABLE_ICON);
					else if (ImageParam.IMAGE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.IMAGE_ICON);
					else if (SingleParam.BOOLEAN_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.CHECK_BOX);
					else if (SingleParam.ENUM_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.DROP_DOWN);
					else if (LinkParam.LINK_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.WWW_ICON);
					else if (SingleParam.NUMBER_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(IconManager.NUMBER);
				}
			}
		}
		
		String text = getText();
		//text = "<html>" + text + "<html>";
		setText(text);
		
        setIcon(paramIcon);
         
        if ((toolTipText != null) && (toolTipText.trim().length() > 0))
        { 
        	toolTipText = UIUtilities.formatToolTipText(toolTipText);
        	setToolTipText(toolTipText); 
        }

        return this;
	}
}
