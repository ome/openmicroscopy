/*
 * org.openmicroscopy.shoola.agents.measurement.view.IntensityValuesDialog 
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the intensity values of ROI.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class IntensityValuesDialog
	extends JDialog
{	
	
	/** The minimum width of the row header. */
	private final static int ROWWIDTHMIN = 24;
	
	/** Spacer for the row header. */
	private final static String ROWSPACER = "  ";
	
	/** Table view. */
	private IntensityTable	table;
	
	/** The scroll pane for the intensityDialog. */
	private JScrollPane 	intensityTableScrollPane;
	
	/** The Row header for the intensityTableScrollPane. */
	private JList 			intensityTableRowHeader;
	
	/** Builds and lays out the UI. */
	private void buildUI(JComboBox channels)
	{
		JPanel infoPanel = new TitlePanel("Intensity Values", 
				"This table shows the Intensity values for the " +
				"selected channel of the selected ROI.",
				IconManager.getInstance().getIcon(IconManager.WIZARD_48));
		JPanel row = UIUtilities.buildComponentPanel(
				new JLabel("Selet the channel:"));
		row.add(channels);
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.add(infoPanel);
		top.add(row);
		getContentPane().add(top, BorderLayout.NORTH);

		intensityTableRowHeader = new JList(new HeaderListModel(
				table.getRowCount()));
		intensityTableRowHeader.setFixedCellHeight(table.getRowHeight());
		
		intensityTableRowHeader.setFixedCellWidth(table.getColumnWidth());
		intensityTableRowHeader.setCellRenderer(new RowHeaderRenderer(table));
		intensityTableScrollPane = new JScrollPane(table);
	    intensityTableScrollPane.setRowHeaderView(intensityTableRowHeader);
	    intensityTableScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, 
	    									new JPanel());
		getContentPane().add(intensityTableScrollPane, BorderLayout.CENTER);
		JViewport viewPort = intensityTableScrollPane.getViewport();
  		viewPort.setViewPosition(new Point(1,1)); 
	}
	
	/** 
	 * Create the initial dialog.
	 * 
	 * @param owner The owner of the dialog.
	 * @param model The table model of the first dialog.
	 */
	IntensityValuesDialog(JFrame owner, IntensityModel model, 
			JComboBox channels)
	{
		super(owner);
		table = new IntensityTable(model);
		buildUI(channels);
	}

	/**
	 * Set the model of the table to a new Model.
	 * @param model see above.
	 */
	void setModel(IntensityModel model)
	{
		table.setModel(model);
		Vector<Integer> listData = new Vector<Integer>();
		for(int i = 0 ; i < model.getRowCount(); i++)
			listData.add(i);
		intensityTableRowHeader.setListData(listData);
		Font font = table.getFont();
		FontMetrics metrics = getFontMetrics(font);
		int w = metrics.stringWidth(ROWSPACER+model.getRowCount() + ROWSPACER);
		int rowWidth = Math.max(w, ROWWIDTHMIN);
		intensityTableRowHeader.setFixedCellWidth(rowWidth);
		intensityTableScrollPane.setRowHeaderView(intensityTableRowHeader);
	}

	/**
	 * Class to define the row header data, this is the Z section 
	 * count in the ROIAssistant.
	 */
	class HeaderListModel
		extends AbstractListModel
	{

		/** The header values. */
		private String[] headers;
    
		/**
		 * Instantiate the header values with a count from n to 1. 
		 * @param n see above.
		 */
		HeaderListModel(int n)
		{
			headers = new String[n];
			for (int i = 0; i< n; i++) 
				headers[i] = ""+(n-i);
		}
    
		/** 
		 * Get the size of the header. 
		 * @return see above.
		 */
		public int getSize() { return headers.length; }
    
		/** 
		 * Get the header object at index.
		 * @param index see above. 
		 * @return see above.
		 */
		public Object getElementAt(int index) { return headers[index]; }
    
	}

	/**
	 * The renderer for the row header. 
	 */
	class RowHeaderRenderer
    	extends JLabel 
    	implements ListCellRenderer
    {
    
		/** 
		 * Instantiate row renderer for table.
		 * 
		 * @param table see above.
		 */
		RowHeaderRenderer(JTable table)
		{
			if (table != null) 
			{
				JTableHeader header = table.getTableHeader();
				setOpaque(true);
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				setHorizontalAlignment(CENTER);
				setHorizontalTextPosition(CENTER);
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}
    
		/**
		 * Returns the component for the renderer.
		 * 
		 * @param list The list containing the headers render context.
		 * @param value The value to be rendered.
		 * @param index The index of the rendered object. 
		 * @param isSelected is the  current header selected.
		 * @param cellHasFocus has the cell focus.
		 * @return the render component. 
		 */
		public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus)
		{
			setText((value == null) ? "" : value.toString());
			return this;
		}
    }
	
}


