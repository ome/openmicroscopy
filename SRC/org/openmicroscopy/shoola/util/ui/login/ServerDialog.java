/*
 * org.openmicroscopy.shoola.util.ui.login.ServerDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.login;



//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog used to manage servers.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ServerDialog 
	extends JDialog
{

	/** Bound property indicating that a new server is selected. */
	static final String 				SERVER_PROPERTY = "server";

	/** Bound property indicating that the window is closed. */
	static final String 				CLOSE_PROPERTY = "close";

	/** Bound property indicating that the window is closed. */
	static final String 				REMOVE_PROPERTY = "remove";

	/** The default size of the window. */
	private static final Dimension		WINDOW_DIM = new Dimension(400, 450);
	
	/** Font for progress bar label. */
	private static final Font			FONT = new Font("SansSerif",
													Font.ITALIC, 10);
	
	/** The window's title. */
	private static final String			TITLE = "Servers";
	
	/** The textual decription of the window. */
	private static final String 		TEXT = "Enter a new server or \n" +
										"select an existing one.";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  	H_SPACER_SIZE = new Dimension(5, 10);
    
	/** 
	 * The size of the invisible components used to separate widgets
	 * vertically.
	 */
	protected static final Dimension	V_SPACER_SIZE = new Dimension(1, 20);
	
    /** Example of a new server. */
    private static final String			EXAMPLE = "e.g. " +
    											"test.openmicroscopy.org";
	
	
	/** Button to close and dispose of the window. */
	private JButton		cancelButton;
	
	/** Button to select a new server. */
	private JButton		finishButton;
	/** Button to remove server from the list. */
	private JButton		removeButton;
	
	/** Button to add new server to the list. */
	private JButton		addButton;
	
	/** Component displaying the collection of available servers. */
	private ServerTable	table;
	
	/** Helper reference to the icons manager. */
	private IconManager	icons;
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
		//List of servers.

		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		Set<String> l = new HashSet<String>();
		if (model.getColumnCount() > 1) {
			String v, trim;
			for (int i = 0; i < table.getRowCount(); i++) {
				v = (String) model.getValueAt(i, 1);
				if (v != null) {
					trim = v.trim();
					if (trim.length() > 0) l.add(trim); 
				}
			}
		}
		firePropertyChange(CLOSE_PROPERTY, null, l);
	}
	
	/** Fires a property indicating that a new server is selected. */
	private void apply()
	{
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
		int n = table.getSelectedRow();
		if (n == -1) return;
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		String value = null;
		if (model.getColumnCount() > 1)
			value = (String) model.getValueAt(n, 1);
		firePropertyChange(SERVER_PROPERTY, null, value);
		close();
	}
	
	/** Removes the selected server from the list. */
	private void remove()
	{
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		int n = table.getSelectedRow();
		if (n < 0) return;
		if (model.getColumnCount() < 2) return; 
		String oldValue = (String) model.getValueAt(n, 1);
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
		model.removeRow(n);
		int m = model.getRowCount()-1;
		String newValue = null;
		if (m > -1 && table.getColumnCount() > 1) {
			table.changeSelection(m, 1, false, false);
			newValue = (String) model.getValueAt(m, 1);
			requestFocusInWindow();
		}
		
		finishButton.setEnabled(model.getRowCount() != 0);
		//table.requestFocusInWindow();
		firePropertyChange(REMOVE_PROPERTY, oldValue, newValue);
	}

	/** Adds a new server to the list. */
	private void add()
	{
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		//First check if we already have
		finishButton.setEnabled(true);
		int m = model.getRowCount();
		//
		String v, trim;
		boolean added = false;
		for (int i = 0; i < m; i++) {
			v = (String) model.getValueAt(i, 1);
			if (v != null) {
				trim = v.trim();
				if (trim.length() == 0) {
					added = true;
					break;
				}
			}
		}
		if (added) {
			//requesFocusOnEditedCell(m);
			requestFocusInWindow();
			return;
		}
		Object[] newRow = new Object[2];
		newRow[0] = icons.getIcon(IconManager.SERVER);
		newRow[1] = "";
		model.insertRow(m, newRow);
		model.fireTableDataChanged();
		requesFocusOnEditedCell(m);
	}
	
	/**
	 * Requests the focus on the edited cell.
	 * 
	 * @param m The row of the edited cell.
	 */
	private void requesFocusOnEditedCell(int m)
	{
		if (table.getColumnCount() > 1) {
			TableCellEditor editor = table.getCellEditor();
			if (editor != null) editor.stopCellEditing();
			table.editCellAt(m, 1);
			//table.setEditingRow(m);
			//table.setEditingColumn(1);
			table.changeSelection(m, 1, false, false);
		}
	}
	
	/** Sets the window's properties. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
		//setResizable(false);
	}
	
	/** 
	 * Attaches the various listeners.
	 * 
	 * @param n The number of servers displayed.
	 */
	private void initListeners(final int n)
	{
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		
		});
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { apply(); }

		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { remove(); }
		});
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { add(); }
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e) { close(); }
        	public void windowOpened(WindowEvent e) {
        		if (n == 0)
        			requesFocusOnEditedCell(table.getRowCount()-1);
        	} 
        });
	}
	
	/** 
	 * Initializes the UI components. 
	 * 
	 * @param servers Collection of servers. Can be <code>null</code>.
	 */
	private void initComponents(List servers)
	{
		table = new ServerTable(servers, 
								icons.getIcon(IconManager.SERVER));
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the window.");
		
		finishButton =  new JButton("Apply");
		getRootPane().setDefaultButton(finishButton);
		IconManager icons = IconManager.getInstance();
		removeButton = new JButton(icons.getIcon(IconManager.REMOVE));
		removeButton.setToolTipText("Remove the selected server " +
									"from the list of servers.");
		addButton = new JButton(icons.getIcon(IconManager.ADD));
		addButton.setToolTipText("Add a new server to the list of servers.");
		//if (servers == null || servers.size() == 0) {
		//	requesFocusOnEditedCell(table.getRowCount()-1);
		//}
	}
	
	/**
	 * Builds the main UI component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel label = UIUtilities.setTextFont("Server Address");
        labels.add(label);  
        label = new JLabel(EXAMPLE);
        label.setFont(FONT);
        labels.add(label); 
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        p.add(new JScrollPane(table));
        
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.add(addButton);
		//p.add(Box.createRigidArea(V_SPACER_SIZE));
        bar.add(removeButton);
        p.add(UIUtilities.buildComponentPanel(bar));
        
        content.add(UIUtilities.buildComponentPanel(labels));  
        content.add(p);  
        return content;
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setBorder(BorderFactory.createEtchedBorder());
        p.setOpaque(true);
        return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel titlePanel = new TitlePanel(TITLE, 
                				TEXT, icons.getIcon(IconManager.CONFIG_48));
        Container c = getContentPane();
        setLayout(new BorderLayout(0, 0));
        c.add(titlePanel, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param servers Collection of predefined servers or <code>null</code>.
	 */
	ServerDialog(List servers)
	{ 
		super();
		icons = IconManager.getInstance();
		setProperties();
		initComponents(servers);
		int n = 0;
		if (servers !=  null) n = servers.size();
		initListeners(n);
		buildGUI();
		setSize(WINDOW_DIM);
	}
	
}
