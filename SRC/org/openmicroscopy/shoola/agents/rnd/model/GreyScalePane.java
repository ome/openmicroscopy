/*
 * org.openmicroscopy.shoola.agents.rnd.model.GreyScaleMapping
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
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
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
public class GreyScalePane
	extends ModelPane
{
								
	/** Number of columns of the JTable. */
	private static final int		NUM_COLUMNS = 3;
	
	/** ID to position the specified component in the table. */
	static final int 				POS_INFO = 0;
	static final int 				POS_LABEL = 1;
	static final int 				POS_RADIO = 2;
	
	private IconManager 			im;
	
	private GreyScalePaneManager	manager;
	
	public GreyScalePane()
	{ 
		manager = new GreyScalePaneManager(this);
	}
	
	/** Re-build the component. */
	public void buildComponent()
	{
		manager.setEventManager(eventManager);
		im = IconManager.getInstance(eventManager.getRegistry());
		buildGUI();
	}

	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildTable());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	/** Build the JTable. */
	private TableComponent buildTable()
	{
		ChannelData[] channelData = eventManager.getChannelData();
		TableComponent table = new TableComponent(channelData.length, 
													NUM_COLUMNS);
		tableLayout(table);
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < channelData.length; i++)
			addRow(table, group, i, channelData[i], eventManager.isActive(i));
			
		return table;
	}
	
	/** Build a row in the table. */
	private void addRow(TableComponent table, ButtonGroup group, 
								int index, ChannelData data, boolean active)
	{
		//init JButton
		JButton b = new JButton();
		b.setIcon(im.getIcon(IconManager.INFO));
		
		//init JLabel
		String s = " Wavelength "+data.getNanometer();
		JLabel label = new JLabel(s);
		
		//init radioButton
		JRadioButton rb = new JRadioButton();
		rb.setSelected(active);
		group.add(rb);
		table.setValueAt(buttonPanel(b), index, POS_INFO);
		table.setValueAt(label, index, POS_LABEL);
		table.setValueAt(rb, index, POS_RADIO);
		//attach listeners to the object
		manager.attachObjectListener(b, index);
		manager.attachObjectListener(rb, index);
	}
	
	/** Display a button in a JPanel. */
	private JPanel buttonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.setOpaque(false);
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
	private void tableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setOpaque(false);
		table.setShowGrid(false);
		TableColumnModel columns = table.getColumnModel();
		TableColumn column= columns.getColumn(POS_INFO);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		column = columns.getColumn(POS_LABEL);
		column.setPreferredWidth(WIDTH_LABEL);
		column.setWidth(WIDTH_LABEL);
		column = columns.getColumn(POS_RADIO);
		column.setPreferredWidth(DEFAULT_WIDTH);
		column.setWidth(DEFAULT_WIDTH);
		
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}	
	
}
