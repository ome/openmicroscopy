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
package treeModel.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import treeModel.fields.AbstractParam;
import treeModel.fields.DateTimeParam;
import treeModel.fields.IField;
import treeModel.fields.IParam;
import treeModel.fields.ImageParam;
import treeModel.fields.LinkParam;
import treeModel.fields.SingleParam;
import treeModel.fields.TableParam;
import treeModel.fields.TimeParam;
import util.ImageFactory;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
		ImageFactory imF = ImageFactory.getInstance();
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object object = node.getUserObject();
			if (object instanceof IField) {
				IField field = (IField)object;
				
				if (field.getParamCount() < 1) {
					//paramIcon = imF.getIcon(ImageFactory.)
				} else {
					IParam param1 = field.getParamAt(0);
					String paramType = param1.getAttribute(
							AbstractParam.PARAM_TYPE);
					
					if (SingleParam.TEXT_LINE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.TEXT_LINE_ICON);
					else if (SingleParam.TEXT_BOX_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.TEXT_BOX_ICON);
					else if (DateTimeParam.DATE_TIME_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.CALENDAR_ICON);
					else if (TimeParam.TIME_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.TIMER_ICON);
					else if (TableParam.TABLE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.TABLE_ICON);
					else if (ImageParam.IMAGE_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.IMAGE_ICON);
					else if (SingleParam.BOOLEAN_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.CHECK_BOX);
					else if (SingleParam.ENUM_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.DROP_DOWN);
					else if (LinkParam.LINK_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.WWW_ICON);
					else if (SingleParam.NUMBER_PARAM.equals(paramType)) 
						paramIcon = imF.getIcon(ImageFactory.NUMBER);
				}
			}
		}
		
		String text = getText();
		text = "<html>" + text + "<html>";
		setText(text);
		
        setIcon(paramIcon);

        return this;
	}
}
