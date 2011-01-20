 /*
 * org.openmicroscopy.shoola.agents.editor.browser.DefaultFieldEditor 
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
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is the CellEditor for the Editable JTree.
 * This class merely delegates to a TreeCellRenderer to supply a UI
 * component for editing purposes. 
 * It plays no role in saving the edited values to the data model. 
 * This is handled by the UI components themselves. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DefaultFieldEditor 
	extends AbstractCellEditor 
	implements TreeCellEditor 
{

	/**
	 * The TreeCellRenderer used to delegate the call to 
	 * getTreeCellRendererComponent();
	 */
	private TreeCellRenderer 		fieldRenderer;
    
    /**
     * This constructor takes a TreeCellRenderer, which is used to delegate
     * the getTreeCellEditorComponent. 
     */
    public DefaultFieldEditor(TreeCellRenderer fieldRenderer) 
    {
		this.fieldRenderer = fieldRenderer;
    }
	
    /**
     * This method is not used (returns null).
     * Instead, editing of the Fields directly saves new values to the 
     * data objects they represent.
     * 
     * @see CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() { return null; }

    /**
     * This will always return true.
     * Fields are always editable.
     * This method will not get called if the tree is not editable. 
     * 
     * @see AbstractCellEditor#isCellEditable(EventObject)
     */
    public boolean isCellEditable(EventObject anEvent) { return true; }
    
    /**
     * This always returns true. 
     * Cell selection is always allowed.
     * 
     * @see AbstractCellEditor#shouldSelectCell(EventObject)
     */
    public boolean shouldSelectCell(EventObject anEvent) { 
    	return true;
    }

    /**
     * This method returns true and has no other effect.
     * The saving of edited values is managed by the editing component itself.
     * 
     * @see AbstractCellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() { return true; }

    /**
     * This method has no effect.
     * The saving of edited values is managed by the editing component itself.
     * 
     * @see AbstractCellEditor#cancelCellEditing(EventObject)
     */
    public void cancelCellEditing() {}

    
	/**
	 * This method delegates to the TreeCellEditor specified in the constructor.
	 * 
	 * @see TreeCellEditor#getTreeCellEditorComponent(JTree, 
	 * 				Object, boolean, boolean, boolean, int)
	 */
	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) 
	{
		return fieldRenderer.getTreeCellRendererComponent(tree, 
				value, 
				isSelected, 
				expanded, 
				leaf, 
				row, 
				true);
	}	
}
