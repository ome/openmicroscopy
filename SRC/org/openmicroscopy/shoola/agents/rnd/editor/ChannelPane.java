/*
 * org.openmicroscopy.shoola.agents.rnd.model.WavelengthPane
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

package org.openmicroscopy.shoola.agents.rnd.editor;

//Java imports
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;
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
class ChannelPane
	extends JPanel
{
	
	private static final int				ROW_HEIGHT = 25;
	private static final Dimension			DIM_SCROLL_TABLE = 
													new Dimension(40, 60);
	
	private ChannelEditorManager			manager;
	private JTextArea						interpretationArea;
	private JTextField						excitation, fluor;
	
	ChannelPane(ChannelEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}

	JTextField getFluor() { return fluor; }
	
	JTextArea getInterpretationArea() { return interpretationArea; }
	
	JTextField getExcitation() { return excitation; }
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildSummaryPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}

	/** Build the panel with info. */
	private JPanel buildSummaryPanel()
	{
		JPanel  p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildTable());
		p.setOpaque(false);
		return p;
	}
	
	/** 
	 * A <code>2x4</code> table model to view channel summary.
	 * The first column contains the property names 
	 * (emission, interpretation, excitation, fluorescence)
	 * and the second column holds the corresponding values. 
	 * <code>interpretation</code>, <code>excitation</code>, 
	 * <code>fluorescence</code> values are marked as editable. 
	 */
	private TableComponent buildTable()
	{
		TableComponent table = new TableComponent(4, 2);
		setTableLayout(table);
		ChannelData wd = manager.getChannelData();
		
		//First row 
		JLabel label = new JLabel(" Emission (in nano.)");
		table.setValueAt(label, 0, 0);
		table.setValueAt(new JLabel(""+wd.getNanometer()), 0, 1);
		
		//Third row.
		label = new JLabel(" Excitation (in nano.)");
	  	excitation = new JTextField(""+wd.getExcitation());
	  	excitation.setForeground(ChannelEditor.STEELBLUE);
	  	excitation.setEnabled(true);

	  	table.setValueAt(label, 1, 0);	
	  	table.setValueAt(excitation, 1, 1);	
	  	
		//Second row
		label = new JLabel(" Interpretation");
		interpretationArea = new JTextArea(wd.getInterpretation());
		interpretationArea.setForeground(ChannelEditor.STEELBLUE);
		interpretationArea.setEditable(true);
		interpretationArea.setLineWrap(true);
		interpretationArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(interpretationArea);
		scrollPane.setPreferredSize(DIM_SCROLL_TABLE);
		table.setValueAt(label, 2, 0);
		table.setValueAt(scrollPane, 2, 1);
		
		//Fourth row.
		label = new JLabel(" Fluorescence");
		fluor = new JTextField(wd.getFluor());
		fluor.setForeground(ChannelEditor.STEELBLUE);
		fluor.setEnabled(true);
		table.setValueAt(label, 3, 0);
		table.setValueAt(fluor, 3, 1);
		return table;
	}
	
	/** Set the layout of the table. */
	private void setTableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}
	
}
