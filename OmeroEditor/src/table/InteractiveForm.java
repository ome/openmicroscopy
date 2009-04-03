/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;

public class InteractiveForm extends JPanel implements ActionListener {
    public static final String[] columnNames = {
        "Will's", "Test", "Table", "Columns"
    };

    protected JTable table;
    protected JScrollPane scroller;
    protected InteractiveTableModel tableModel;

    public InteractiveForm() {
        initComponent();
        
         tableModel.addEmptyColumn();
    }

    public void initComponent() {
        tableModel = new InteractiveTableModel(columnNames);
        // tableModelListener tells the table how to respond to changes in Model
        tableModel.addTableModelListener(new InteractiveTableModelListener());
        table = new JTable();
        table.setModel(tableModel);
        table.setSurrendersFocusOnKeystroke(true);
        if (!tableModel.hasEmptyRow()) {
            tableModel.addEmptyRow();
        }

        scroller = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(500, 300));
   
        JButton outputResults = new JButton("Output Results");
        outputResults.addActionListener(this);
        
        
        setLayout(new BorderLayout());
        add(scroller, BorderLayout.CENTER);
        add(outputResults, BorderLayout.SOUTH);
    }

    public void highlightLastRow(int row) {
        int lastrow = tableModel.getRowCount();
        if (row == lastrow - 1) {
            table.setRowSelectionInterval(lastrow - 1, lastrow - 1);
        } else {
            table.setRowSelectionInterval(row + 1, row + 1);
        }

        table.setColumnSelectionInterval(0, 0);
    }

    // moves active cell one column to the right, when cell is updated (table changed)
    public class InteractiveTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent evt) {
            if (evt.getType() == TableModelEvent.UPDATE) {
                int column = evt.getColumn();
                int row = evt.getFirstRow();
                if (column < 0) column = 0;
                if (row < 0) row = 0;
                System.out.println("row: " + row + " column: " + column);
                
                if (column < table.getColumnCount()-1) {
                	table.setColumnSelectionInterval(column + 1, column + 1);
                	table.setRowSelectionInterval(row, row);
                } else if (!tableModel.hasEmptyRow()) {
                	tableModel.addEmptyRow();
                	highlightLastRow(row);
                } else {
                	table.setRowSelectionInterval(row + 1, row + 1);
                	table.setColumnSelectionInterval(0, 0);
                }
               
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            JFrame frame = new JFrame("Interactive Form");
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });
            frame.getContentPane().add(new InteractiveForm());
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void actionPerformed(ActionEvent arg0) {
		outputResults();
	}
	
	public void outputResults() {
		ArrayList<ArrayList<String>> data = tableModel.getData();
		
		for (int row=0; row<data.size(); row++) {
			ArrayList<String> rowDataArray = data.get(row);
			String rowData = "rowData" + row ;
			for (String cellData: rowDataArray) {
				rowData = rowData + ", " + cellData;
			}
			System.out.println(rowData);
		}
	}
}
