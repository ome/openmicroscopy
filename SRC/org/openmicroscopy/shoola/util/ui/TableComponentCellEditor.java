/*
 * org.openmicroscopy.shoola.util.ui.TableComponentCellEditor
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TableComponentCellEditor 
	implements TableCellEditor, TreeCellEditor, Serializable 
{
	
	protected EventListenerList listenerList = new EventListenerList();
	transient protected ChangeEvent changeEvent = null;
	
	protected JComponent editorComponent = null;
	protected JComponent container = null;		// Can be tree or table
	
	
	public Component getComponent()
	{
		return editorComponent;
	}
	
	public Object getCellEditorValue()
	{
		return editorComponent;
	}
	
	public boolean isCellEditable(EventObject anEvent)
	{
		return true;
	}
	
	public boolean shouldSelectCell(EventObject anEvent) {
		if (editorComponent != null && anEvent instanceof MouseEvent
			&& ((MouseEvent) anEvent).getID() == MouseEvent.MOUSE_PRESSED)
		{
		 	Component dispatchComponent = 
		 		SwingUtilities.getDeepestComponentAt(editorComponent, 3, 3 );
			MouseEvent e = (MouseEvent)anEvent;
			MouseEvent e2 = new MouseEvent(dispatchComponent, 
								MouseEvent.MOUSE_RELEASED,
								e.getWhen() + 100000, e.getModifiers(),
								3, 3, e.getClickCount(), e.isPopupTrigger());
			dispatchComponent.dispatchEvent(e2); 
			e2 = new MouseEvent(dispatchComponent, MouseEvent.MOUSE_CLICKED,
						e.getWhen() + 100001, e.getModifiers(), 3, 3, 1,
						e.isPopupTrigger());
			dispatchComponent.dispatchEvent(e2); 
		}
		return false;
	}
	
	public boolean stopCellEditing()
	{
		fireEditingStopped();
		return true;
	}
	
	public void cancelCellEditing()
	{
		fireEditingCanceled();
	}
	
	public void addCellEditorListener(CellEditorListener l)
	{
		listenerList.add(CellEditorListener.class, l);
	}
	
	public void removeCellEditorListener(CellEditorListener l)
	{
		listenerList.remove(CellEditorListener.class, l);
	}
	
	protected void fireEditingStopped()
	{
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i >= 0; i-= 2) {
			if (listeners[i] == CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener) listeners[i+1]).editingStopped(
															changeEvent);
			}	       
		}
	}
	
	protected void fireEditingCanceled()
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i >= 0; i-=2) {
			if (listeners[i]==CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener)listeners[i+1]).editingCanceled(
															changeEvent);
			}	       
		}
	}
	
	// implements javax.swing.tree.TreeCellEditor
	public Component getTreeCellEditorComponent(JTree tree, Object value,
		boolean isSelected, boolean expanded, boolean leaf, int row)
	{
		//String   stringValue = tree.convertValueToText(value, isSelected,
		//	expanded, leaf, row, false);
		editorComponent = (JComponent) value;
		container = tree;
		return editorComponent;
	}
	
	// implements javax.swing.table.TableCellEditor
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column)
		{
		editorComponent = (JComponent)value;
		container = table;
		return editorComponent;
	}
	
} 
