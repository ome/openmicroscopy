/*
 * org.openmicroscopy.shoola.agents.rnd.model.RBGMapping
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

package org.openmicroscopy.shoola.agents.rnd.model;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.BorderFactory;
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
import org.openmicroscopy.shoola.util.ui.ColoredButton;
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
class RGBMapping
	extends JPanel
{
	/** Color of the table grid. */
	private static final Color		GRID_COLOR = Color.WHITE;
	
	/** Border of the color button. */
	private static final Color		BORDER_COLOR = Color.BLACK;
	
	/** Height of a cell in the table. */
	private static final int		ROW_HEIGHT = 25;
	
	/** Default width of a cell. */
	private static final int		DEFAULT_WIDTH = 20;
	
	/** Width of the label cell. */
	private static final int		WIDTH_LABEL = 100;
	
	/** Default Height of the JButton. */
	private static final int		BUTTON_HEIGHT = 15;
	
	/** Default width of the JButton. */
	private static final int		BUTTON_WIDTH = 15;
			
	private static final Dimension	DIM_BUTTON = 
									new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
									
	/** Number of columns of the JTable. */
	private static final int		NUM_COLUMNS = 4;
	
	/** ID to position the specified component in the table. */
	static final int 				POS_INFO = 0;
	static final int 				POS_LABEL = 1;
	static final int 				POS_CHECKBOX = 2;
	static final int 				POS_COLOR = 3;
	
	private JTable					table;
	private IconManager 			IM;
	private RGBMappingManager		manager;
	
	RGBMapping(Registry registry, TreeMap wavelengths)
	{
		manager = new RGBMappingManager(this);
		IM = IconManager.getInstance(registry);
		buildGUI(wavelengths);
		manager.attachListeners();
	}

	/** Build and layout the GUI. */
	private void buildGUI(TreeMap wavelengths)
	{
		setLayout(new GridLayout(1, 1));
		buildTable(wavelengths);
	}
	
	/** Build the JTable. */
	private void buildTable(TreeMap wavelengths)
	{
		table = new TableComponent(wavelengths.size(), NUM_COLUMNS);
		tableLayout();
		
		Iterator   e = wavelengths.keySet().iterator();
		WavelengthData data;
		String wave;
		while (e.hasNext()) {
			wave = (String) e.next(); // index 
			data = (WavelengthData) wavelengths.get(wave);
			addRow(wave, data);
		}
	}
	
	/** Build a row in the table. */
	private void addRow(String wave, WavelengthData data)
	{
		int i = Integer.parseInt(wave);
		//init JButton
		JButton b = new JButton();
		b.setIcon(IM.getIcon(IconManager.INFO));
		
		//init JLabel
		String s = " Wavelength "+data.nanometer;
		JLabel label = new JLabel(s);
		
		//init CheckBox
		JCheckBox box = new JCheckBox();
		//TODO: check with user DisplayOptions.
		box.setSelected(false);
		
		//init Color button
		ColoredButton colorButton = new ColoredButton();
		colorButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
		//TODO: set the color of the specified wavelength.
		//part of the displayOptions class
		colorButton.setBackground(Color.white);
		
		table.setValueAt(buttonPanel(b), i, POS_INFO);
		table.setValueAt(label, i, POS_LABEL);
		table.setValueAt(box, i, POS_CHECKBOX);
		table.setValueAt(buttonPanel(colorButton), i, POS_COLOR);
	}
	
	/** Display a button in a JPanel. */
	private JPanel buttonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setBorder(null);
		button.setPreferredSize(DIM_BUTTON);
		button.setBounds(0, 0, BUTTON_WIDTH,BUTTON_HEIGHT);
		button.setContentAreaFilled(false);
		p.setPreferredSize(DIM_BUTTON);
		p.setSize(DIM_BUTTON);
		p.add(button);
	
		return p;
	}
	
	/** Table layout. */
	private void tableLayout()
	{
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setGridColor(GRID_COLOR);
		TableColumnModel columns = table.getColumnModel();
		TableColumn column= columns.getColumn(POS_INFO);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		column = columns.getColumn(POS_LABEL);
		column.setPreferredWidth(WIDTH_LABEL);
		column.setWidth(WIDTH_LABEL);
		column = columns.getColumn(POS_CHECKBOX);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		column = columns.getColumn(POS_COLOR);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}	
	
}
