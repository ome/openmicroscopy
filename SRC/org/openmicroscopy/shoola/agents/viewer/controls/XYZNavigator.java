/*
 * org.openmicroscopy.shoola.agents.viewer.controls.XYZNavigator
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

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
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
class XYZNavigator
	extends JPanel
{
	/** Default width of a cell. */
	private static final int		DEFAULT_WIDTH = 80;
	
	/** Width of the second column. */
	private static final int		WIDTH_THIRD = 110;
	
	/** Width of the second column. */
	private static final int		WIDTH_SECOND = 30;
	
	/** Default height of a cell. */
	private static final int		ROW_HEIGHT = 60;
	
	/** Default width of a cell. */
	private static final int		ROW_HEIGHT_2 = 40;
	
	/** Dimension of the JPanel which contains the slider. */
	private static final int		PANEL_HEIGHT = 40;
	private static final int		PANEL_WIDTH = 100;
	
	private static final Dimension	DIM = new Dimension(PANEL_WIDTH, 
														PANEL_HEIGHT);
														
	/** Dimension of the panel containing the textField. */
	private static final int		FIELD_HEIGHT = 25;
	private static final int		FIELD_WIDTH = 30;
	private static final Dimension	DIM_FIELD = new Dimension(FIELD_WIDTH, 
														FIELD_HEIGHT);
	/** The slider used to move across the Z stack. */
	private JSlider         		zSlider;
	
	/** Text field to allow user to specify a Z point. */
	private JTextField      		zField;
	
	
	private XYZNavigatorManager		manager;
	
	private IconManager				im;
	
	XYZNavigator(NavigationPaletteManager topManager, int sizeX, int sizeY, 
				int sizeZ, int z)
	{
		manager = new XYZNavigatorManager(this, topManager, sizeZ, z);
		im = IconManager.getInstance(topManager.getRegistry());
		initSlider(sizeZ-1, z);
		initTextField(sizeZ-1, z);
		manager.attachListeners();
		buildGUI(sizeX, sizeY, sizeZ);
	}
	
	public JTextField getZField()
	{
		return zField;
	}

	public JSlider getZSlider()
	{
		return zSlider;
	}

	/** 
	 * Instantiates and initializes to <code>curZ</code> the Z slider.
	 * 
	 * @param max     Total number of ticks.
	 */
	private void initSlider(int maxZ, int z)
	{
		zSlider = new JSlider(JSlider.HORIZONTAL, 0, maxZ, z);
		zSlider.setToolTipText("Move the slider to navigate across Z stack");
		zSlider.setMinorTickSpacing(1);
		zSlider.setMajorTickSpacing(10);
		zSlider.setPaintTicks(true);
		zSlider.setOpaque(false);
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel(""+0) );
		labelTable.put(new Integer(maxZ), new JLabel(""+maxZ));
		zSlider.setLabelTable(labelTable);
		zSlider.setPaintLabels(true);

	}
    
	/** 
	 * Instantiates and initializes to <code>curZ</code> the Z text field.
	 *
	 * @param sizeZ		Total number of planes in the Z-stack.
	 * @param z 		Default z.
	 */
	private void initTextField(int maxZ, int z)
	{
		zField = new JTextField(""+z, (""+maxZ).length());
		zField.setForeground(NavigationPalette.STEELBLUE);
		zField.setToolTipText("Enter a Z point");
	}
    

	/** Build and layout the GUI. */
	private void buildGUI(int sizeX, int sizeY, int sizeZ)
	{
		//add(buildDimsPanel(sizeX, sizeY, sizeZ), BorderLayout.WEST);
		add(buildTable(sizeX, sizeY, sizeZ));
		//add(buildSliderPanel());
	}
	
	private JPanel buildTable(int sizeX, int sizeY, int sizeZ)
	{
		JPanel p = new JPanel();
		JTable table = new TableComponent(2, 3);
		tableLayout(table);
		
		//First row
		JLabel label = new JLabel("");
		table.setValueAt(buildDimsPanel(sizeX, sizeY, sizeZ), 0, 0);
		table.setValueAt(label, 0, 1);
		table.setValueAt(label, 0, 2);
		
		//Second row
		table.setRowHeight(1, ROW_HEIGHT_2);
		label = new JLabel("Current Z: ");
		label.setForeground(NavigationPalette.STEELBLUE);
		table.setValueAt(label, 1, 0);
		table.setValueAt(buildFieldPanel(), 1, 1);
		table.setValueAt(buildSliderPanel(), 1, 2);
		
		p.add(table);
		p.setOpaque(false);
		return p;
	}
	
	/**
	 * Build a panel containing the a text field along with current selection.
	 * @return	See above.
	 */
	private JPanel buildFieldPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(null);
		p.setOpaque(false);
		p.setPreferredSize(DIM_FIELD);
		p.setSize(DIM_FIELD);
		zField.setPreferredSize(DIM_FIELD);
		zField.setBounds(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
		p.add(zField);
		return p;
	}
	
	/**
	 * Build a panel containing a slider along with current selection
	 * 
	 * @return See above.
	 */
	private JPanel buildSliderPanel()	
	{
		//JPanel p = new JPanel(), field = new JPanel(), slider = new JPanel();
		JPanel slider = new JPanel();
		slider.setLayout(null);
		slider.setOpaque(false);
		slider.setPreferredSize(DIM);
		slider.setSize(DIM);
		zSlider.setPreferredSize(DIM);
		zSlider.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		slider.add(zSlider);
		
		//JLabel current = new JLabel("Current Z: ");
		//current.setForeground(NavigationPalette.STEELBLUE);
		///field.add(current);
		//field.add(zField);
		//field.setAlignmentX(LEFT_ALIGNMENT);
		//p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		//p.add(field);
		//p.add(slider);
		//return p;
		return slider;
	}
	
	/** 
 	 * Builds a panel containing a label indicating 
 	 * the image dimensions across X, Y, and Z. 
	 *
	 * @param sizeX	The maximum X.
	 * @param sizeY	The maximum Y.
	 * @return	The above mentioned panel.
	 */
	private JPanel buildDimsPanel(int sizeX, int sizeY, int sizeZ)
	{
		JPanel  p = new JPanel();
		JLabel dimsInfo = new JLabel();
		String  html = "<html><table colspan=0 rowspan=0 border=0><tr>";
		html += "<td>Size X:<br>Size Y:<br>Size Z:</td>";
		html += "<td align=right>"+sizeX+"<br>"+sizeY+"<br>"+sizeZ+"</td>";
		html += "</tr></table><html>";
		dimsInfo.setText(html);
		p.add(dimsInfo);
		return p;
	}
	
	/** Table layout. */
	private void tableLayout(JTable table)
	{
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setOpaque(false);
		table.setShowGrid(false);
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		column = columns.getColumn(1);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		column = columns.getColumn(2);
		column.setPreferredWidth(WIDTH_THIRD);
		column.setWidth(WIDTH_THIRD);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}
		
}
