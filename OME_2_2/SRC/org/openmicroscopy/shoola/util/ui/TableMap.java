/*
 * org.openmicroscopy.shoola.util.ui.TableMap
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

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
public class TableMap 
	extends AbstractTableModel 
	implements TableModelListener
{
	protected TableModel model; 

	public TableModel getModel() {	return model; }

	public void setModel(TableModel model)
	{
		this.model = model; 
		model.addTableModelListener(this); 
	}
	
	public Object getValueAt(int aRow, int aColumn)
	{
		return model.getValueAt(aRow, aColumn); 
	}
        
	public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		model.setValueAt(aValue, aRow, aColumn); 
	}

	public int getRowCount()
	{
		return (model == null) ? 0 : model.getRowCount(); 
	}

	public int getColumnCount()
	{
		return (model == null) ? 0 : model.getColumnCount(); 
	}
        
	public String getColumnName(int aColumn)
	{
		return model.getColumnName(aColumn); 
	}

	public Class getColumnClass(int aColumn)
	{
		return model.getColumnClass(aColumn); 
	}
        
	public boolean isCellEditable(int row, int column)
	{ 
		 return model.isCellEditable(row, column); 
	}
	
	/** By default forward all events to all the listeners. */
	public void tableChanged(TableModelEvent e)
	{
		fireTableChanged(e);
	}
	
}

