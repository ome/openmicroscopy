/*
 * org.openmicroscopy.shoola.util.ui.TableSorter
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

package org.openmicroscopy.shoola.util.ui.table;



//Java imports
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
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
public class TableSorter
	extends TableMap
{
    
	int             indexes[];
	Vector          sortingColumns = new Vector();
	boolean         ascending = true;
	int 			compares;

	public TableSorter()
	{
		indexes = new int[0]; 
	}

	public TableSorter(TableModel model)
	{
		setModel(model);
	}

	public void setModel(TableModel model)
	{
		super.setModel(model); 
		reallocateIndexes(); 
	}

	public int compareRowsByColumn(int row1, int row2, int column)
	{
		TableModel data = model;

		// Check for null.
		Object o1 = data.getValueAt(row1, column);
		Object o2 = data.getValueAt(row2, column); 

		// If both values are null, return 0.
		if (o1 == null && o2 == null) return 0; 
		else if (o1 == null) return -1; 
		else if (o2 == null) return 1; 
		int result = 0;

        if (o1 instanceof Number || o1 instanceof Integer || 
                o1 instanceof Double || o1 instanceof Float)
            result = compareNumbers((Number) o1, (Number) o2);
		else if (o1 instanceof Date) 
			result = compareDates((Date) o1, (Date) o2);
		else if (o1 instanceof String)
			result = compareStrings((String) o1, (String) o2);
		else if (o1 instanceof Boolean)
			result = compareBooleans((Boolean) o1, (Boolean) o2);	
		else 
			result = compareObjects(o1, o2);
			
		return result;
	}

	/** Compare two Number objects. */
	private int compareNumbers(Number n1, Number n2)
	{
		double d1 = n1.doubleValue();
		double d2 = n2.doubleValue();
		int v = 0;
		if (d1 < d2) v = -1;
		else if (d1 > d2)	v = 1;
		return v;
	}
	
	/** Compare two Date objects. */
	private int compareDates(Date d1, Date d2)
	{
		long n1 = d1.getTime();
		long n2 = d2.getTime();
		int v = 0;
		if (n1 < n2) v = -1;
		else if (n1 > n2) v = 1;
		return v;
	}
	
	/** Compare two String objects. */
	private int compareStrings(String s1, String s2)
	{
		int v = 0;
		int result = s1.compareTo(s2);
		if (result < 0) v = -1;
		else if (result > 0) v = 1;
		return v;
	}
	
	/** Compare two Objects. */
	private int compareObjects(Object o1, Object o2)
	{
		String s1 = o1.toString();
		String s2 = o2.toString();
		int result = s1.compareTo(s2);
		int v = 0;
		if (result < 0) v = -1;
		else if (result > 0) v = 1;
		return v;
	}
	
	/** Compare two Boolean obejcts. */
	private int compareBooleans(Boolean bool1, Boolean bool2)
	{
		boolean b1 = bool1.booleanValue();
		boolean b2 = bool2.booleanValue();
		int v = 0;
		if (b1 == b2) v = 0;
		else if (b1) v =  -1; //1
		else v = 1;//-1
		return v;
	}
	
	/** Compare the values of two rows. */
	public int compare(int row1, int row2)
	{
		compares++;
        int result;
		for (int level = 0; level < sortingColumns.size(); level++) {
			Integer column = (Integer) sortingColumns.elementAt(level);
			result = compareRowsByColumn(row1, row2, column.intValue());
			if (result != 0) return ascending ? result : -result;
		}
		return 0;
	}

	/** Allocate indexed array. */
	public void reallocateIndexes()
	{
		int rowCount = model.getRowCount();
		indexes = new int[rowCount];
		for (int row = 0; row < rowCount; row++)
			indexes[row] = row;
	}

	public void tableChanged(TableModelEvent e)
	{
		reallocateIndexes();
		super.tableChanged(e);
	}

	public void checkModel()
	{
		if (indexes.length != model.getRowCount())
			throw new RuntimeException("Sorter not informed of a " +
										"change in model.");
	}

	public void sort()
	{
		checkModel();
		compares = 0;
		shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
	}

	// This is a home-grown implementation which we have not had time
	// to research - it may perform poorly in some circumstances. It
	// requires twice the space of an in-place algorithm and makes
	// NlogN assigments shuttling the values between the two
	// arrays. The number of compares appears to vary between N-1 and
	// NlogN depending on the initial order but the main reason for
	// using it here is that, unlike qsort, it is stable.
	public void shuttlesort(int from[], int to[], int low, int high)
	{
		if (high-low < 2) return;
		int middle = (low+high)/2;
		shuttlesort(to, from, low, middle);
		shuttlesort(to, from, middle, high);

		int p = low, q = middle;

		/* This is an optional short-cut; at each recursive call,
		check to see if the elements in this subset are already
		ordered.  If so, no further comparisons are needed; the
		sub-array can just be copied.  The array must be copied rather
		than assigned otherwise sister calls in the recursion might
		get out of sinc.  When the number of elements is three they
		are partitioned so that the first set, [low, mid), has one
		element and and the second, [mid, high), has two. We skip the
		optimisation when the number of elements is three or less as
		the first compare in the normal merge will produce the same
		sequence of steps. This optimisation seems to be worthwhile
		for partially ordered lists but some analysis is needed to
		find out how the performance drops to Nlog(N) as the initial
		order diminishes - it may drop very quickly.  */

		if (high-low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
			for (int i = low; i < high; i++)
				to[i] = from[i];
			return;
		}

		for (int i = low; i < high; i++) {
			if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
				to[i] = from[p++];
			else to[i] = from[q++];	
		}
	}

	public void swap(int i, int j)
	{
		int tmp = indexes[i];
		indexes[i] = indexes[j];
		indexes[j] = tmp;
	}

	public Object getValueAt(int aRow, int aColumn)
	{
		checkModel();
		return model.getValueAt(indexes[aRow], aColumn);
	}

	public void setValueAt(Object aValue, int aRow, int aColumn)
	{
		checkModel();
		model.setValueAt(aValue, indexes[aRow], aColumn);
	}

	public void sortByColumn(int column) { sortByColumn(column, true); }

	public void sortByColumn(int column, boolean ascending)
	{
		this.ascending = ascending;
		sortingColumns.removeAllElements();
		sortingColumns.addElement(new Integer(column));
		sort();
		super.tableChanged(new TableModelEvent(this)); 
	}

	/** Add listener. */
	public void addMouseListenerToHeaderInTable(JTable table)
	{ 
		final TableSorter sorter = this; 
		final JTable tableView = table; 
		tableView.setColumnSelectionAllowed(false); 
		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				onClick(e, tableView, sorter);	
			}
		};
		JTableHeader th = tableView.getTableHeader(); 
		th.addMouseListener(listMouseListener); 
	}
	
	private void onClick(MouseEvent e, JTable tableView, TableSorter sorter) 
	{
		TableColumnModel columnModel = tableView.getColumnModel();
		JTableHeader header = tableView.getTableHeader();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
		int column = tableView.convertColumnIndexToModel(viewColumn); 
		if (e.getClickCount() == 1 && column != -1) {
			TableColumn tc = columnModel.getColumn(viewColumn);
			Object headerValue = tc.getHeaderValue();
			boolean ascending;
			if (headerValue instanceof TableHeaderTextAndIcon) {
				TableHeaderTextAndIcon value = 
							(TableHeaderTextAndIcon) headerValue;
				ascending = value.isAscending();
				value.setAscending(!ascending);
				header.repaint();
			} else {
				int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK; 
				ascending = (shiftPressed == 0); 
			}
			sorter.sortByColumn(column, ascending); 				
		}
	}
	
}	

