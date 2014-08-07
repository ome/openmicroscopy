/*
 * org.openmicroscopy.shoola.util.ui.login.ServerEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

//Third-party libraries

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
	
	/** Bound property indicating to remove the warning message. */
	public static final String	REMOVE_MESSAGE_PROPERTY = "removeMessage";
	
	/** Bound property indicating to add the warning message. */
	public static final String	ADD_MESSAGE_PROPERTY = "addMessage";
	
	/** Bound property indicating that the edition is finished. */
	static final String			EDIT_PROPERTY = "edit";

	/** Bound property indicating that a server is removed from the list. */
	static final String 		REMOVE_PROPERTY = "remove";
    
	/** Bound property indicating that a server is added to the list. */
	static final String 		ADD_PROPERTY = "add";
	
	/** Bound property indicating to apply the selection. */
	static final String 		APPLY_SERVER_PROPERTY = "applyServer";
	
	/** Separator used when storing various servers. */
    static final String			SERVER_PORT_SEPARATOR = ":";
    
    /** Separator used when storing various servers. */
    static final String			SERVER_NAME_SEPARATOR = ",";
    
    /** The minimum port value. */
    static final int			MIN_PORT = 0;
    
    /** The minimum port value. */
    static final int			MAX_PORT = 64000;
    
    /** The old port value. */
    private static final List<String>	OLD_PORTS;
    
    /** Example of a new server. */
    private static final String	EXAMPLE = "e.g. test.openmicroscopy.org " +
    										"or 134.20.12.33";
    
    /** The note. */
    private static final String NOTE = "You should not have to modify the port.";
    
    /** The header of the table. */
    private static final String HEADER = "Server Address and Port";

    /** The property name for the host to connect to <i>OMERO</i>. */
    private static final String	OMERO_SERVER = "omeroServer";
    
	/** Font for progress bar label. */
	private static final Font	FONT = new Font("SansSerif", Font.ITALIC, 10);
	
	static {
		OLD_PORTS = new ArrayList<String>();
		OLD_PORTS.add(""+1099);
		OLD_PORTS.add(""+4063);
	}
	
	/** Button to remove server from the list. */
	private JButton			removeButton;
	
	/** Button to add new server to the list. */
	private JButton			addButton;
	
	/** Button to edit an existing server. */
	private JButton			editButton;
	
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
	
    /** The port used to connect to the active server. */
	private String			activePort;
	
	/** The default port value. */
	private String			defaultPort;
	
	/** The original row selected corresponding to the server. */
	private int				originalRow;
	
	/** Indicates the edited row. */
	private int				editedRow;
	
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
		String oldValue = null;
		if (row < model.getRowCount())
			oldValue = (String) model.getValueAt(row, 1);
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) editor.stopCellEditing();
			
		table.removeRow(row);
		//model.removeRow(row);
		int m = model.getRowCount()-1;
		String newValue = null;
		if (m > -1 && table.getColumnCount() > 1) {
			table.setManual(true);
			table.changeSelection(m, 1, false, false);
			newValue = (String) model.getValueAt(m, 1);
			table.setManual(false);
			requestFocusInWindow();
		}
		editor = table.getCellEditor();
		
		if (editor != null) editor.stopCellEditing();
		if (model.getRowCount() == 0) setEditing(false);
		handleServers(activeServer, activePort);
		editing = false;
		fireEditProperty(model.getRowCount() != 0);
		firePropertyChange(REMOVE_PROPERTY, oldValue, newValue);
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param servers   Collection of servers to display.
	 * @param enabled 	Pass <code>true</code> to allow edition,
	 * 					
	 */
	private void initComponents(Map<String, String> servers, boolean enabled)
	{
		editedRow = -1;
		table = new ServerTable(this, servers, 
				icons.getIcon(IconManager.SERVER_22));
		removeButton = new JButton(icons.getIcon(IconManager.REMOVE));
		removeButton.setName("remove server button");
		UIUtilities.unifiedButtonLookAndFeel(removeButton);
		removeButton.setToolTipText("Remove the selected server " +
									"from the list of servers.");
		addButton = new JButton(icons.getIcon(IconManager.ADD));
		addButton.setName("add server button");
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
			public void actionPerformed(ActionEvent e) { addRow(""); }
		});
		editButton = new JButton(icons.getIcon(IconManager.EDIT));
		editButton.setName("edit server button");
		UIUtilities.unifiedButtonLookAndFeel(editButton);
		editButton.setToolTipText("Edit an existing server.");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
				int row = table.getSelectedRow();
				if (editedRow == row) requestFocusOnEditedCell(row, 2);
				else requestFocusOnEditedCell(row, 1);
			}
		});
		//addButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
        JPanel labels = new JPanel();
        labels.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
		JLabel label = UIUtilities.setTextFont(HEADER);
        labels.add(label, c);  
        label = new JLabel(EXAMPLE);
        label.setFont(FONT);
        c.gridy++;// = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        labels.add(label, c); 
        label = new JLabel(NOTE);
        label.setFont(FONT);
        c.gridy++;// = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        labels.add(label, c); 
        if (activeServer != null) {
        	c.gridx = 0;
    		c.gridy++;
    		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
    		label = UIUtilities.setTextFont("Connected to ");
            labels.add(label, c);  
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            //c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.gridx = 1;
            label = new JLabel(activeServer+SERVER_PORT_SEPARATOR+activePort);
            labels.add(label, c); 
        }
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JScrollPane pane = new JScrollPane(table);
        Dimension d = pane.getPreferredSize();
        pane.setPreferredSize(new Dimension(d.width, 150));
        p.add(pane);
        p.add(buildControls());
       
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel content = UIUtilities.buildComponentPanel(labels);
        add(content); 
        add(p);
	}
	
	/**
	 * Builds the component hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(addButton);
        bar.add(removeButton);
        bar.add(editButton);
        return UIUtilities.buildComponentPanel(bar);
	}
	
	/**
	 * Sets the <code>enabled</code> flag of the 
	 * {@link #addButton} and {@link #removeButton}.
	 * 
	 * @param b The value to set.
	 */
	private void setButtonsEnabled(boolean b)
	{
		//addButton.setEnabled(b);
		removeButton.setEnabled(b);
		editButton.setEnabled(b);
	}
	
	/**
	 * Fires a property to 
	 * 
	 * @param b	Pass <code>true</code> when editing, 
	 * 			<code>false</code> otherwise.
	 */
	private void fireEditProperty(boolean b)
	{
		firePropertyChange(EDIT_PROPERTY, Boolean.valueOf(!b), 
				Boolean.valueOf(b));
	}
	
	/** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setOpaque(false);
        emptyMessagePanel.setBorder(new PartialLineBorder(Color.BLACK));
        emptyMessagePanel.setLayout(new BoxLayout(emptyMessagePanel,
         BoxLayout.X_AXIS));
    }

	/** Requests focus if no server address at initialization time. */
	private void initFocus()
	{
		int n = 0;
		Map<String, String> servers = getServers();
		if (servers != null) n = servers.size();
		originalRow = -1;
		if (n == 0) {
			requestFocusOnEditedCell(table.getRowCount()-1, 1);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		} else {
			originalRow = n-1;
			table.setRowSelectionInterval(originalRow, originalRow);
		}
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param defaultPort The default port to use.
	 */
	ServerEditor(String defaultPort)
	{
		this(null, null, defaultPort);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param activeServer  The server the user is currently connected to.
	 * @param defaultPort The default port to use.
	 */
	ServerEditor(String activeServer, String defaultPort)
	{
		this(activeServer, null, defaultPort);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param activeServer  The server the user is currently connected to.
	 * @param activePort	The port used by the server.
	 * @param defaultPort	The default port to use.
	 */
	ServerEditor(String activeServer, String activePort, String defaultPort)
	{
		icons = IconManager.getInstance();
		this.activeServer = activeServer;
		if (defaultPort == null) defaultPort = "";
		this.defaultPort = defaultPort;
		if (activePort == null || activePort.trim().length() == 0)
			this.activePort = defaultPort;
		int n = 0; 
		Map<String, String> servers = getServers();
		if (servers != null) n = servers.size();
		initComponents(servers, n != 0);
		editing = false;
		editedRow = -1;
		buildGUI();
	}
	
    
    /**
     * Returns the default port value.
     * 
     * @return See above.
     */
    String getDefaultPort() { return defaultPort; }
    
	/**
	 * Sets the editing mode.
	 * 
	 * @param b Pass <code>true</code> if the editing mode is turned on,
	 * 			<code>false</code> otherwise.
	 */
	void setEditing(boolean b)
	{
		//addButton.setEnabled(!b);
		editButton.setEnabled(!b);
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
	 * @param row The selected row.
	 * @param col The selected column.
	 */
	void requestFocusOnEditedCell(int row, int col)
	{
		if (col == 0) return;
		if (table.getColumnCount() > 1) {
			editing = true;
			editedRow = row;
			TableCellEditor editor = table.getCellEditor();
			if (editor != null) editor.stopCellEditing();
			table.editCellAt(row, col);
			table.changeSelection(row, col, false, false);
			table.requestFocus();
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
		//if (previousRow == row) return;
		if (previousRow == -1 || previousRow == row) return;
		//if (!editing) return;
		editing = false;
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (i != previousRow) {
				values.add((String) table.getValueAt(i, 1)); 
			}
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
		handleServers(activeServer, activePort);
		if (found || text == null || text.trim().length() == 0) {
			if (previousRow != -1) removeRow(previousRow);
			//showMessagePanel(false);
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
        	firePropertyChange(REMOVE_MESSAGE_PROPERTY, null, 
        			emptyMessagePanel);
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
		handleServers(activeServer, activePort);
	}
	
	/** Stops the cell editing. */
	void stopEdition()
	{
		setEditing(false);
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
		String trim = v.trim();
		if (trim.length() == 0) return null;
		return trim;
	}
	
	/**
	 * Returns the value of the selected port.
	 * 
	 * @return See above.
	 */
	String getSelectedPort()
	{
		int row = table.getSelectedRow();
		if (row == -1) return null;
		String v = (String) table.getValueAt(row, 2);
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
	 * Returns the value of the active port.
	 * 
	 * @return See above.
	 */
	String getActivePort() { return activePort; }
	
	/**
	 * Returns the list of existing servers.
	 * 
	 * @return See above.
	 */
	Map<String, String> getServers()
	{
    	Preferences prefs = Preferences.userNodeForPackage(ServerEditor.class);
        String servers = prefs.get(OMERO_SERVER, null);
        if (servers == null || servers.length() == 0)  return null;
        String[] l = servers.split(SERVER_NAME_SEPARATOR, 0);
        
        if (l == null) return null;
        Map<String, String> listOfServers = new LinkedHashMap<String, String>();
        int index;
        String server;
        String name, p;
        String[] values;
        for (index = 0; index < l.length; index++) {
        	server = l[index].trim();
        	if (server.length() > 0) {
        		values = server.split(SERVER_PORT_SEPARATOR, 0);
            	name = values[0];
            	if (values.length > 1) {
            		p = values[1];
            		if (OLD_PORTS.contains(p))
            			p = defaultPort;
            	}
            	else p = defaultPort;
            	if (!name.equals(activeServer))
            		listOfServers.put(name, p);
        	}
        }	
        return listOfServers; 
	}
	
	/** 
	 * Saves the collection of servers. 
	 * 
	 * @param serverName 	The name of the server which has to be added at 
	 * 						the end of the list.
	 * @param port
	 */
	void handleServers(String serverName, String port)
	{
		Map<String, String> l = new LinkedHashMap<String, String>();
		String v;
		for (int i = 0; i < table.getRowCount(); i++) {
			v = (String) table.getValueAt(i, 1);
			if (v != null && v.trim().length() > 0)
				l.put(v, (String) table.getValueAt(i, 2)); 
		}
			
		if (activeServer != null && l.get(activeServer) == null) 
			l.put(activeServer, activePort);
		
		
		Preferences prefs = Preferences.userNodeForPackage(ServerEditor.class);
		if (l == null || l.size() == 0) {
		//if (l != null) {
			prefs.put(OMERO_SERVER, "");
			return;
		}
		Map<String, String> 
		servers = new LinkedHashMap<String, String>(l.size());
		Set set = l.entrySet();
		Entry entry;
		Iterator i = set.iterator();
		String name;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			name = (String) entry.getKey();
			if (!name.equals(serverName))
				servers.put(name, (String) entry.getValue());
		}
		if (serverName != null && serverName.length() != 0)
			servers.put(serverName, port);
		i = servers.entrySet().iterator();
		int n = servers.size()-1;
		int index = 0;
		String list = "";
		String value;
		StringBuffer buffer = new StringBuffer();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			buffer.append((String) entry.getKey());
			buffer.append(SERVER_PORT_SEPARATOR);
			if (entry.getValue() != null)
				buffer.append((String) entry.getValue());
			else buffer.append(defaultPort);
			
			if (index != n) buffer.append(SERVER_NAME_SEPARATOR);
			index++;
		}
		list = buffer.toString();
		if (list.length() != 0) prefs.put(OMERO_SERVER, list);
	}

	/**
	 * Sets the focus on the row corresponding to the passed server.
	 * 
	 * @param server The server to handle.
	 */
	void setFocus(String server)
	{
		if (server == null || server.equals(ScreenLogin.DEFAULT_SERVER)) {
			initFocus();
			return;
		}
			
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		int m = model.getRowCount();
		String value;
		int row = -1;
		for (int i = 0; i < m; i++) {
			value = (String) model.getValueAt(i, 1);
			if (server.equals(value)) {
				row = i;
			}
		}
		if (row > -1) table.setRowSelectionInterval(row, row);
	}
	
	/** Makes sure to remove rows without server address.*/
	void onApply()
	{
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		int m = model.getRowCount();
		if (m <= 1) return;
		String value;
		List<Integer> rowToDelete = new ArrayList<Integer>();
		for (int i = 0; i < m; i++) {
			value = (String) model.getValueAt(i, 1);
			if (value == null || value.trim().length() == 0)
				rowToDelete.add(i);
		}
		Iterator<Integer> j = rowToDelete.iterator();
		while (j.hasNext()) {
			removeRow(j.next());
		}
	}
	
	/**
	 * Returns the row corresponding to the file originally selected.
	 * 
	 * @return See above.
	 */
	int getOriginalRow() { return originalRow; }
	
	/**
	 * Returns <code>true</code> if the selected row is the original one,
	 * <code>false</code> otherwise.
	 * 
	 * @param value The server selected.
	 * @return See above.
	 */
	boolean isOriginal(String value)
	{ 
		DefaultTableModel model = ((DefaultTableModel) table.getModel());
		if (table.getSelectedRow() < 0) return false;
		String v = (String) model.getValueAt(table.getSelectedRow(), 1);
		if (value == null) return false;
		return (value.equals(v));
	}
	
	/** 
	 * Adds a new server to the list.
	 * 
	 *  @param hostName The name of the server.
	 */
	void addRow(String hostName)
	{
		if (editing) {
			TableCellEditor editor = table.getCellEditor();
			if (editor != null) editor.stopCellEditing();
			
			//if (model.getRowCount() == 0) {
				editing = false;
				//if (hostName != null && hostName.trim().length() != 0)
				addRow(hostName);
			//}
			return;
		}
		//addButton.setEnabled(false);
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		int m = model.getRowCount();
		Object[] newRow = new Object[3];
		newRow[0] = icons.getIcon(IconManager.SERVER_22);
		boolean editing = true;
		if (hostName != null && hostName.trim().length() != 0) {
			newRow[1] = hostName;
			editing = false;
		} else newRow[1] = "";
		setButtonsEnabled(true);
		//addButton.setEnabled(true);
		newRow[2] = defaultPort;
		model.insertRow(m, newRow);
		model.fireTableDataChanged();
		requestFocusOnEditedCell(m, 1);
		setEditing(editing);
	}
	
	/** Removes the last row. */
	void removeLastRow()
	{
		DefaultTableModel model= ((DefaultTableModel) table.getModel());
		int m = model.getRowCount();
		if (m > 0) {
			model.removeRow(m-1);
			model.fireTableDataChanged();
		}	
	}
	
	/**
	 * Returns the number of rows.
	 * 
	 * @return See above.
	 */
	int getRowCount() { return table.getRowCount(); }
	
}
