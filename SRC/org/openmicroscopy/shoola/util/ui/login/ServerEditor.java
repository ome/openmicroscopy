/*
 * org.openmicroscopy.shoola.util.ui.login.ServerEditor 
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
package org.openmicroscopy.shoola.util.ui.login;



//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.PartialLineBorder;

/** 
 * UI component display controls and list of servers.
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
public class ServerEditor 
	extends JPanel
{
	
	/** The default height of the <code>TitlePanel</code>. */
    public static final int		TITLE_HEIGHT = 70;
    
	/** Bound property indicating to remove the warning message. */
	public static final String	REMOVE_MESSAGE_PROPERTY = "removeMessage";
	
	/** Bound property indicating to add the warning message. */
	public static final String	ADD_MESSAGE_PROPERTY = "addMessage";
	
	/** Bound property indicating that the edition is finished. */
	static final String			EDIT_PROPERTY = "edit";
	
	/** Bound property indicating that a new server is selected. */
	static final String 		SERVER_PROPERTY = "server";
	
	/** Bound property indicating that a server is removed from the list. */
	static final String 		REMOVE_PROPERTY = "remove";
    
	/** Bound property indicating that a server is added to the list. */
	static final String 		ADD_PROPERTY = "add";
	
    /** The message displayed when the entered server address already exists. */
    private static final String	EMPTY_MSG = "Server address already " +
    												"exists.";
    
    /** Example of a new server. */
    private static final String	EXAMPLE = "(e.g. test.openmicroscopy.org " +
    										"or 134.20.12.33)";
    
    /** Separator used when storing various servers. */
    private static final String	SERVER_NAME_SEPARATOR = ",";
    
    /** The property name for the host to connect to <i>OMERO</i>. */
    private static final String	OMERO_SERVER = "omeroServer";
    
	/** Font for progress bar label. */
	private static final Font	FONT = new Font("SansSerif", Font.ITALIC, 10);
	
	/** Button to remove server from the list. */
	private JButton			removeButton;
	
	/** Button to add new server to the list. */
	private JButton			addButton;
	
	/** Component displaying the collection of available servers. */
	private ServerTable		table;
	
    /** The panel displaying the message when no name is entered. */
    private JPanel          emptyMessagePanel;
    
	/** Helper reference to the icons manager. */
	private IconManager		icons;
	
    /** 
     * Sets to <code>true</code> if the message is displayed, 
     * <code>false</code> otherwise.
     */
    private boolean			warning;
    
    /** Flag indicating if we are in the editing mode. */
    private boolean			editing;

    /** 
	 * The server the user is currently connected to or <code>null</code>
	 * if not connected.
	 */
	private String			activeServer;
	
	/** 
	 * Removes the selected server from the list. 
	 * 
	 * @param row The row to remove.
	 */
	private void removeRow(int row)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (row < 0) return;
		if (model.getColumnCount() < 2) return; 
		String oldValue = (String) model.getValueAt(row, 1);
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
			
		table.removeRow(row);
		//model.removeRow(row);
		int m = model.getRowCount()-1;
		String newValue = null;
		if (m > -1 && table.getColumnCount() > 1) {
			table.changeSelection(m, 1, false, false);
			newValue = (String) model.getValueAt(m, 1);
			requestFocusInWindow();
		}
		editor = table.getCellEditor();
		
		if (editor != null) {
			editor.stopCellEditing();
		}
		if (model.getRowCount() == 0) setEditing(false);
		handleServers(activeServer);
		editing = false;
		fireEditProperty(model.getRowCount() != 0);
		firePropertyChange(REMOVE_PROPERTY, oldValue, newValue);
	}
	
	/** Adds a new server to the list. */
	private void addRow()
	{
		if (editing) return;
		addButton.setEnabled(false);
		//table.putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		//First check if we already have
		//finishButton.setEnabled(true);
		int m = model.getRowCount();
		Object[] newRow = new Object[2];
		newRow[0] = icons.getIcon(IconManager.SERVER);
		newRow[1] = "";
		model.insertRow(m, newRow);
		model.fireTableDataChanged();
		requesFocusOnEditedCell(m);
		setEditing(true);
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param servers Collection of servers to display.
	 */
	private void initComponents(List servers)
	{
		table = new ServerTable(this, servers, 
				icons.getIcon(IconManager.SERVER));
		removeButton = new JButton(icons.getIcon(IconManager.REMOVE));
		UIUtilities.unifiedButtonLookAndFeel(removeButton);
		removeButton.setToolTipText("Remove the selected server " +
									"from the list of servers.");
		addButton = new JButton(icons.getIcon(IconManager.ADD));
		addButton.setToolTipText("Add a new server to the list of servers.");
		addButton.setBorder(new TitledBorder(""));
		UIUtilities.unifiedButtonLookAndFeel(addButton);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
				removeRow(table.getSelectedRow()); 
			}
		});
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { addRow(); }
		});
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        JPanel labels = new JPanel();
        double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
        double[] rows;
        if (activeServer != null) {
        	rows = new double[3];
        	rows[0] = TableLayout.FILL;
        	rows[1] = 5;
        	rows[2] = TableLayout.FILL;
        } else {
        	rows = new double[1];
        	rows[0] = TableLayout.FILL;
        }
        TableLayout layout = new TableLayout();
        layout.setColumn(columns);
        layout.setRow(rows);
        labels.setLayout(layout);
        //labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel label = UIUtilities.setTextFont("Server Address");
        labels.add(label, "0, 0");  
        label = new JLabel(EXAMPLE);
        label.setFont(FONT);
        labels.add(label, "2, 0"); 
        if (activeServer != null) {
        	labels.add(new JLabel(), "0, 1, 2, 1"); 
        	label = UIUtilities.setTextFont("Connected to ");
            labels.add(label, "0, 2");  
            labels.add(new JLabel(activeServer), "2, 2"); 
        }
        
        
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
        
        add(UIUtilities.buildComponentPanel(labels)); 
        add(p);
	}
	
	/**
	 * Sets the <code>enabled</code> flag of the 
	 * {@link #addButton} and {@link #removeButton}.
	 * 
	 * @param b The value to set.
	 */
	private void setButtonsEnabled(boolean b)
	{
		addButton.setEnabled(b);
		removeButton.setEnabled(b);
	}
	
	/**
	 * Fires a property to 
	 * 
	 * @param b	Pass <code>true</code> when editing, 
	 * 			<code>false</code> otherwise.
	 */
	private void fireEditProperty(boolean b)
	{
		Boolean newValue = Boolean.TRUE, oldValue = Boolean.FALSE;
		if (!b) {
			newValue = Boolean.FALSE;
			oldValue = Boolean.TRUE;
		}
		firePropertyChange(EDIT_PROPERTY, oldValue, newValue);
	}
	
	/** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setOpaque(false);
        emptyMessagePanel.setBorder(new PartialLineBorder(Color.BLACK));
        //Rectangle r = titlePanel.getBounds();
        
        emptyMessagePanel.setLayout(new BoxLayout(emptyMessagePanel,
                                                BoxLayout.X_AXIS));
        JLabel label = new JLabel(icons.getIcon(IconManager.ERROR));
        emptyMessagePanel.add(label);
        int w = label.getWidth();
        label = new JLabel(EMPTY_MSG);
        int h = label.getFontMetrics(label.getFont()).getHeight();
        w += getFontMetrics(getFont()).stringWidth(EMPTY_MSG);
        emptyMessagePanel.add(label);
        Insets i = emptyMessagePanel.getInsets();
        h += i.top+i.bottom;
        emptyMessagePanel.setBounds(2, TITLE_HEIGHT-h-1, 3*w/2, h);
    }
    
	/**
	 * Sets the editing mode.
	 * 
	 * @param b Pass <code>true</code> if the editing mode is turned on,
	 * 			<code>false</code> otherwise.
	 */
	void setEditing(boolean b)
	{
		addButton.setEnabled(!b);
		editing = b; 
	}
	
	/**
	 * Returns <code>true</code> if we are in the editing mode,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isEditing() { return editing; }
	
	/**
	 * Requests the focus on the edited cell.
	 * 
	 * @param m The row of the edited cell.
	 */
	void requesFocusOnEditedCell(int m)
	{
		if (table.getColumnCount() > 1) {
			//editing = true;
			TableCellEditor editor = table.getCellEditor();
			if (editor != null) editor.stopCellEditing();
			table.editCellAt(m, 1);
			table.changeSelection(m, 1, false, false);
		}
	}
	
	/**
	 * Enables or not the {@link #finishButton}.
	 * 
	 * @param row			The selected row.
	 * @param previousRow 	The previously selected row.
	 * @param text			The text of the previously selected row.
	 */
	void changeSelection(int row, int previousRow, String text)
	{
		fireEditProperty(row != -1);
		if (previousRow == -1 || previousRow == row) return;
		if (!editing) return;
		editing = false;
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (i != previousRow) values.add((String) table.getValueAt(i, 1)); 
		}
		if (activeServer != null && !values.contains(activeServer))
			values.add(activeServer);
		Iterator j = values.iterator();
		String name;
		boolean found = false; 
		while (j.hasNext()) {
			name = (String) j.next();
			if (name.equals(text)) {
				found = true;
				break;
			}
		}
		handleServers(activeServer);
		if (found || text == null) {
			removeRow(previousRow);
			showMessagePanel(false);
		}
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
	}
	
	/**
	 * Shows the warning message if the passed value is <code>true</code>,
	 * hides it otherwise.
	 * 
	 * @param warning 	Pass <code>true</code> to show the message, 
	 * 					<code>false</code> otherwise.			
	 */
	void showMessagePanel(boolean warning)
	{
		this.warning = warning;
		fireEditProperty(!warning);
		setButtonsEnabled(!warning);
		if (warning) {
			if (emptyMessagePanel != null) return;
			buildEmptyPanel();
            firePropertyChange(ADD_MESSAGE_PROPERTY, null, emptyMessagePanel);
        } else {
        	if (emptyMessagePanel == null) return;
        	firePropertyChange(REMOVE_MESSAGE_PROPERTY, null, emptyMessagePanel);
            emptyMessagePanel = null;
        }
	}

	/**
	 * Removes the row if the text entered is <code>null</code> or of length
	 * <code>zero</code> and also if the warning message is displayed.
	 * 
	 * @param text
	 */
	void finishEdition(String text)
	{
		if (!editing) return;
		editing = false;
		setButtonsEnabled(true);
		if (warning || text == null || text.length() == 0) {
			removeRow(table.getSelectedRow());
			showMessagePanel(false);
			return;
		}
		handleServers(activeServer);
	}
	
	/** Stops the cell editing. */
	void stopEdition()
	{
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
	}

	/**
	 * Returns the value of the selected server.
	 * 
	 * @return See above.
	 */
	String getSelectedServer()
	{
		int row = table.getSelectedRow();
		if (row == -1) return null;
		String v = (String) table.getValueAt(row, 1);
		if (v == null) return null;
		return v.trim();
	}
	
	/**
	 * Returns the value of the active server.
	 * 
	 * @return See above.
	 */
	String getActiveServer() { return activeServer; }
	
	/**
	 * Returns the list of existing servers.
	 * 
	 * @return See above.
	 */
	List<String> getServers()
	{
    	Preferences prefs = Preferences.userNodeForPackage(ServerEditor.class);
        String servers = prefs.get(OMERO_SERVER, null);
        if (servers == null || servers.length() == 0)  return null;
        String[] l = servers.split(SERVER_NAME_SEPARATOR, 0);
        
        if (l == null) return null;
        List<String> listOfServers = new ArrayList<String>();
        int index;
        String server;
        for (index = 0; index < l.length; index++) {
        	server = l[index].trim();
        	if (!server.equals(activeServer))
        		listOfServers.add(server);
        }	
        return listOfServers; 
	}
	
	/** Saves the collection of servers. 
	 * 
	 * @param serverName 	The name of the server which has to be added at 
	 * 						the end of the list.
	 */
	void handleServers(String serverName)
	{
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++) 
			l.add((String) table.getValueAt(i, 1)); 
		if (activeServer != null && !l.contains(activeServer)) 
			l.add(activeServer);
		
		
		Preferences prefs = Preferences.userNodeForPackage(ServerEditor.class);
		if (l == null || l.size() == 0) {
			prefs.put(OMERO_SERVER, "");
			return;
		}
		ArrayList<String> servers = new ArrayList<String>(l.size());
		Iterator i = l.iterator();
		String name;
		while (i.hasNext()) {
			name = (String) i.next();
			if (!name.equals(serverName))
				servers.add(name);
			//if (name != null && name.trim().length() != 0)
			
		}
		if (serverName != null && serverName.length() != 0)
			servers.add(serverName);
		i = servers.iterator();
		int n = servers.size()-1;
		int index = 0;
		String list = "";
		while (i.hasNext()) {
			list += (String) i.next();
			if (index != n)  list += SERVER_NAME_SEPARATOR;
			index++;
		}
		if (list.length() != 0) prefs.put(OMERO_SERVER, list);
	}
	
	/** Creates a new instance. */
	public ServerEditor()
	{
		this(null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param activeServer The server the user is currently connected to.
	 */
	public ServerEditor(String activeServer)
	{
		icons = IconManager.getInstance();
		this.activeServer = activeServer;
		int n = 0; 
		List servers = getServers();
		if (servers != null) n = servers.size();
		initComponents(servers);
		setEditing(n == 0);
		buildGUI();
	}
	
	/** Requests focus if no server address at init time. */
	public void initFocus()
	{
		int n = 0;
		List servers = getServers();
		if (servers != null) n = servers.size();
		if (n == 0) {
			requesFocusOnEditedCell(table.getRowCount()-1);
		}
	}
	
}
