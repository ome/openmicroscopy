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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingDef;
import org.openmicroscopy.shoola.env.rnd.codomain.PlaneSlicingDef;
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
	private static final int		DEFAULT_WIDTH = 40;
	
	/** width of the first column. */
	private static final int		WIDTH = 100;
	/** background color of the JTable. */ 
	private static final Color		CELL_COLOR = Color.LIGHT_GRAY;

	/** Dimension of the JButton. */
	private static final int		BUTTON_HEIGHT = 20;
	private static final int		BUTTON_WIDTH = 100;		
	private static final Dimension	DIM_BUTTON = new Dimension(BUTTON_WIDTH, 
																BUTTON_HEIGHT);
	
	private JButton					cStretching;
	private JButton					pSlicing;
	private JCheckBox				ri;
	private JCheckBox				cs;
	private JCheckBox				ps;
	
	private CodomainPaneManager		manager;
	public CodomainPane(Registry registry, QuantumMappingManager control,
						ContrastStretchingDef csDef, PlaneSlicingDef psDef)
	{
		manager = new CodomainPaneManager(this, control, csDef, psDef);
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
		IconManager IM = IconManager.getInstance(registry);
		cStretching = new JButton(IM.getIcon(IconManager.STRETCHING));
		pSlicing = new JButton(IM.getIcon(IconManager.SLICING));
	}
	
	/** Initializes the checkboxes. */
	private void initCheckBox()
	{
		ri = new JCheckBox();
		cs = new JCheckBox();
		ps = new JCheckBox();	
	}
	
	/** Builds and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildTable());
	}
	
	/** Build the JTable. */
	private JTable buildTable()
	{
		JTable table = new TableComponent(3, 3);
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setBackground(CELL_COLOR);
		
		//Set the columns' width.
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		
		//Reset the width of the first column
		column = columns.getColumn(0);
		//column.setPreferredWidth(PANEL_WIDTH);
		column.setWidth(WIDTH);

		//First row.
		JLabel label = new JLabel(" Reverse Intensity");
		table.setValueAt(label, 0, 0);
		table.setValueAt(ri, 0, 1);

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
		p.setBackground(Color.LIGHT_GRAY);
		p.setBorder(null);
		button.setPreferredSize(DIM_BUTTON);
		button.setBounds(0, 0, 40, 20);
		button.setContentAreaFilled(false);
		p.setPreferredSize(DIM_BUTTON);
		p.setSize(DIM_BUTTON);
		p.add(button);
		
		return p;
	}

}
