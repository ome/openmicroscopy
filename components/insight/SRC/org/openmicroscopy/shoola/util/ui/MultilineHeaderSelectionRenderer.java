/*
 * org.openmicroscopy.shoola.util.ui.MultilineHeaderSelectionRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

//Application-internal dependencies

/** 
 * Displays the header with text displayed over several and add a selection box.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MultilineHeaderSelectionRenderer
	extends JPanel
	implements TableCellRenderer
{

	/** The list displaying the text. */
	private JList list;
	
	/** The check box if any. */
	private JCheckBox box;
	
	/** Flag indicating that the mouse has been pressed. */
	private boolean mousePressed;
	
	/** The selected column. */
	private int column;
	
	/** Flag indicating that the listeners have been installed. */
	private boolean initListener;
	
	/** The table of reference if set. */
	private JTable table;
	
	/**
	 * Handles the mouse clicked event. 
	 * 
	 * @param event The event to handle.
	 */
	private void handleClickEvent(MouseEvent event)
	{  
		if (!mousePressed) return;
		mousePressed = false;
		JTableHeader header = (JTableHeader) (event.getSource());
		if (header == null) return;
		if (table == null) table =  header.getTable();
		if (table == null) return;
		TableColumnModel columnModel = table.getColumnModel();
		int vc = columnModel.getColumnIndexAtX(event.getX());
		int c = table.convertColumnIndexToModel(vc);
		if (vc == this.column && event.getClickCount() == 1 && c != -1)
			box.doClick();
		header.repaint();
	} 
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param table The table of reference if set.
	 * @param box The check box to handle.
	 */
	public MultilineHeaderSelectionRenderer(JTable table, JCheckBox box)
	{
		this.table = table;
		setOpaque(true);
	    setForeground(UIManager.getColor("TableHeader.foreground"));
	    JLabel l = new JLabel();
	    l.setOpaque(true);
	    setBackground(l.getBackground());
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    list = new JList();
	    ListCellRenderer renderer = list.getCellRenderer();
	    ((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
	    list.setCellRenderer(renderer);
	    list.setBackground(l.getBackground());
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(list);
		this.box = box;
		if (box != null) {
			box.setBackground(l.getBackground());
			add(box);
		}
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param box The check box to handle.
	 */
	public MultilineHeaderSelectionRenderer(JCheckBox box)
	{
	    this(null, box);
	}
	
	/** Creates a new instance. */
	public MultilineHeaderSelectionRenderer()
	{
	    this(null, null);
	}
	
	/**
	 * Adds listener and format the text of the table header.
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, 
	 * boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable t, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (table == null) table = t;
		if (table != null && !initListener && box != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				initListener = true;
				header.addMouseListener(new MouseAdapter() {
					
					public void mouseClicked(MouseEvent e) {  
					    handleClickEvent(e);  
					} 
					
					public void mousePressed(MouseEvent e) {  
						mousePressed = true;  
					}  
				});
			}
		}
		this.column = column;
		if (table != null) setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
		Vector<String> v = new Vector<String>();
		try {
			while ((line = br.readLine()) != null) 
				v.addElement(line);
		} catch (Exception ex) {}
		list.setListData(v);
		return this;
	}
	
}
