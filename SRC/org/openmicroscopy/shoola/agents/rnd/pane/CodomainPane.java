/*
 * org.openmicroscopy.shoola.agents.rnd.pane.CodomaimPane
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

package org.openmicroscopy.shoola.agents.rnd.pane;

//Java imports
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;

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
class CodomainPane
	extends JPanel
{
	/** row's height. */ 
	private static final int 		ROW_HEIGHT = 25;
	
	/** row's width. */
	private static final int		BWIDTH = 45;
	
	/** width of the first column. */
	private static final int		WIDTH = 140;

	/** Dimension of the JButton. */
	private static final int		BUTTON_HEIGHT = 15, BUTTON_WIDTH = 40;	
		
	private static final Dimension	DIM_BUTTON = new Dimension(BUTTON_WIDTH, 
															BUTTON_HEIGHT);
	
	private JButton					cStretching;
	private JButton					pSlicing;
	private JCheckBox				ri;
	private JCheckBox				cs;
	private JCheckBox				ps;
	
	private CodomainPaneManager		manager;
	
	public CodomainPane(Registry registry, QuantumPaneManager control)
	{
		//TODO: retrieve Data from CodomainMapDefs.
		manager = new CodomainPaneManager(this, control);
		initButton(registry);
		initCheckBox();
		manager.attachListeners();
		buildGUI();
	}

	/** Getters. */
	public CodomainPaneManager getManager()
	{
		return manager;
	}
	
	public JCheckBox getCS()
	{
		return cs;
	}

	public JButton getCStretching()
	{
		return cStretching;
	}

	public JCheckBox getPS()
	{
		return ps;
	}

	public JButton getPSlicing()
	{
		return pSlicing;
	}

	public JCheckBox getRI()
	{
		return ri;
	}
	
	/** Initializes the buttons. */
	private void initButton(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		cStretching = new JButton(im.getIcon(IconManager.STRETCHING));
		pSlicing = new JButton(im.getIcon(IconManager.SLICING));
	}
	
	/** Initializes the checkboxes. */
	private void initCheckBox()
	{
		ri = new JCheckBox();
		cs = new JCheckBox();
		ps = new JCheckBox();	
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.add(buildTable());
		add(p);
		//add(buildTable());
	}
	
	/** Build the JTable. */
	private JTable buildTable()
	{
		JTable table = new TableComponent(3, 3);
		table.setTableHeader(null);
		table.setOpaque(false);
		table.setShowGrid(false);
		table.setRowHeight(ROW_HEIGHT);
		
		//Set the columns' width.
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(WIDTH);
		column.setWidth(WIDTH);
		column = columns.getColumn(2);
		column.setPreferredWidth(BWIDTH);
		column.setWidth(BWIDTH);

		//First row.
		JLabel label = new JLabel(" Reverse Intensity");
		table.setValueAt(label, 0, 0);
		table.setValueAt(ri, 0, 1);
		label = new JLabel("");
		table.setValueAt(label, 0, 2);
		
		//Second row.
		label = new JLabel(" Contrast Stretching");
		table.setValueAt(label, 1, 0);
		table.setValueAt(cs, 1, 1);
		table.setValueAt(buildButtonPanel(cStretching), 1, 2);
		
		//Third row.
		label = new JLabel(" Plane Slicing");
		table.setValueAt(label, 2, 0);
		table.setValueAt(ps, 2, 1);
		table.setValueAt(buildButtonPanel(pSlicing), 2, 2);
		
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
		return table;
	}
	
	/**
	 * Build a JPanel which contains a JButton.
	 * 
	 * @param button
	 * @return See above.
	 */
	private JPanel buildButtonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.setBorder(null);
		button.setPreferredSize(DIM_BUTTON);
		button.setBounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		button.setContentAreaFilled(false);
		p.setPreferredSize(DIM_BUTTON);
		p.setSize(DIM_BUTTON);
		p.add(button);
		
		return p;
	}

}
