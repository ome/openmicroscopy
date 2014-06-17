/*
 * org.openmicroscopy.shoola.util.ui.treetable.TreeTable 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnHeaderRenderer;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableModel;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.editors.BooleanCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.NumberCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.editors.StringCellEditor;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.BooleanCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.IconCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.SelectionHighLighter;

public class SearchResultTable
	extends JXTable
{	
	
	/** A map of the default cell editors in the table.  */
	protected static final Map<Class<?>, DefaultCellEditor> DEFAULT_EDITORS;


	/** A map of the default cell renderers in the table. */
	protected static final Map<Class<?>, TableCellRenderer> DEFAULT_RENDERERS;
	
	static
	{
		DEFAULT_RENDERERS = new HashMap<Class<?>, TableCellRenderer>();
		DEFAULT_RENDERERS.put(Boolean.class, new BooleanCellRenderer());
		DEFAULT_RENDERERS.put(Long.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Integer.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Float.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(Double.class, new NumberCellRenderer());
		DEFAULT_RENDERERS.put(String.class, new NumberCellRenderer(SwingConstants.LEFT));
		DEFAULT_RENDERERS.put(Icon.class, new IconCellRenderer());
		
		DEFAULT_EDITORS = new HashMap<Class<?>, DefaultCellEditor>();
		DEFAULT_EDITORS.put(Boolean.class, new BooleanCellEditor((JCheckBox) DEFAULT_RENDERERS.get(Boolean.class)));
		DEFAULT_EDITORS.put(Integer.class, new NumberCellEditor(new JTextField()));
		DEFAULT_EDITORS.put(String.class, new StringCellEditor(new JTextField()));
	}
	
	/** Initializes the table. */
	private void initialize()
	{
		ColumnHeaderRenderer l = 
			(ColumnHeaderRenderer) getTableHeader().getDefaultRenderer();
		l.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/** Creates a new instance. */
	public SearchResultTable()
	{
		super();
		initialize();
	}
	
	/**
	 * Create an instance of the treetable.
	 *  
	 * @param model The tree model.
	 */
	public SearchResultTable(TableModel model)
	{
		super(model);
		initialize();
	}
	
	/**
	 * Return <code>true</code> if the left button was clicked, 
	 * <code>false</code> otherwise.
	 * 
	 * @param e The mouse event to handle.
	 * @return See above.
	 */
	protected boolean leftClick(MouseEvent e)
	{
		return (e.getButton() == MouseEvent.BUTTON1);
	}
	
	/**
	 * Sets the mouse listener for mouse events and attach it to the methods
	 * onLeftMouseDown(), onRightMouseDown()
	 */
	protected void setListeners()
	{
		MouseListener mouseListener = new MouseListener()
		{

			public void mouseClicked(MouseEvent e) { onMouseClicked(e); }

			public void mouseEntered(MouseEvent e) { onMouseEnter(e); }

			public void mouseExited(MouseEvent e) { onMouseExit(e); }

			public void mousePressed(MouseEvent e) { onMousePressed(e); }

			public void mouseReleased(MouseEvent e) { onMouseReleased(e); }
			
		};
		addMouseListener(mouseListener);
	}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouseClicked events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseClicked(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse released events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseReleased(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse pressed events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMousePressed(MouseEvent e) {}
	
	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouse enter events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseEnter(MouseEvent e) {}

	/**
	 * MouseEvent called from mouseListener. 
	 * This Event responds to mouseExit events.
	 * 
	 * @param e The mouse event to handle.
	 */
	protected void onMouseExit(MouseEvent e) {}

	
	/**
	 * Sets the default editors for the cells in the table. This includes
	 * editors for cells containing: int, long, string, booleans,
	 * floats, longs, doubles. 
	 */
	protected void setDefaultEditors()
	{
		Iterator<Class<?>> classIterator = DEFAULT_EDITORS.keySet().iterator();
		Class<?> classType;
		DefaultCellEditor editorType;
		while(classIterator.hasNext()) {
			classType = classIterator.next();
			editorType = DEFAULT_EDITORS.get(classType);
			this.setDefaultEditor(classType, editorType);
		}
	}
	
	/**
	 * Sets the default renderers for the cells in the table. This includes
	 * renderers for cells containing: dates, int, long, string, booleans,
	 * floats, longs, doubles, colour. 
	 */
	protected void setDefaultRenderers()
	{
		Iterator<Class<?>> 
			classIterator = DEFAULT_RENDERERS.keySet().iterator();
		Class<?> classType;
		TableCellRenderer rendererType;
		while (classIterator.hasNext()) {
			classType = classIterator.next();
			rendererType = DEFAULT_RENDERERS.get(classType);
			setDefaultRenderer(classType, rendererType);
		}
	}
	
}


