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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import org.openmicroscopy.shoola.util.ui.border.PartialLineBorder;

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
	implements ComponentListener
{

	/** Bound property indicating that a new server is selected. */
	static final String 				SERVER_PROPERTY = "server";

	/** Bound property indicating that the window is closed. */
	static final String 				CLOSE_PROPERTY = "close";

	/** Bound property indicating that the window is closed. */
	static final String 				REMOVE_PROPERTY = "remove";

    /** The default height of the <code>TitlePanel</code>. */
    public static final int    	    	TITLE_HEIGHT = 70;
    
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
	
    /** The message displayed when the entered server address already exists. */
    private static final String     	EMPTY_MSG = "Server address already " +
    												"exists.";
    
	/** Button to close and dispose of the window. */
	private JButton			cancelButton;
	
	/** Button to select a new server. */
	private JButton			finishButton;
	
	/** Button to remove server from the list. */
	private JButton			removeButton;
	
	/** Button to add new server to the list. */
	private JButton			addButton;
	
	/** Component displaying the collection of available servers. */
	private ServerTable		table;
	
	/** Helper reference to the icons manager. */
	private IconManager		icons;
	
    /** The panel displaying the message when no name is entered. */
    private JPanel          emptyMessagePanel;
    
    /** The component hosting the title and the warning messages if required. */
    private JLayeredPane    titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
    /** 
     * Sets to <code>true</code> if the message is displayed, 
     * <code>false</code> otherwise.
     */
    private boolean			warning;
    
    /** Flag indicating if we are in the editing mode. */
    private boolean			editing;
    
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
	
	/** 
	 * Removes the selected server from the list. 
	 * 
	 * @param row The row to remove.
	 */
	private void removeRow(int row)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		//int row = table.getSelectedRow();
		if (row < 0) return;
		//DefaultTableModel model= (DefaultTableModel) table.getModel();
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
		editing = false;
		finishButton.setEnabled(model.getRowCount() != 0);
		//table.requestFocusInWindow();
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
		/*
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
		System.err.println()
		if (added) {
			requestFocusInWindow();
			return;
		}
		*/
		Object[] newRow = new Object[2];
		newRow[0] = icons.getIcon(IconManager.SERVER);
		newRow[1] = "";
		model.insertRow(m, newRow);
		model.fireTableDataChanged();
		requesFocusOnEditedCell(m);
		setEditing(true);
		//table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}

	/** Sets the window's properties. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
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
			public void actionPerformed(ActionEvent e) 
			{ 
				removeRow(table.getSelectedRow()); 
			}
		});
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { addRow(); }
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e) { close(); }
        	public void windowOpened(WindowEvent e) {
        		if (n == 0) {
        			requesFocusOnEditedCell(table.getRowCount()-1);
        		}
        	} 
        });
		addComponentListener(this);
	}
	
	/** 
	 * Initializes the UI components. 
	 * 
	 * @param servers 	Collection of servers. Can be <code>null</code>.
	 * @param n			The number of servers, <code>0</code> if none.
	 */
	private void initComponents(List servers, int n)
	{
		table = new ServerTable(this, servers, 
								icons.getIcon(IconManager.SERVER));
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the window.");
		
		finishButton =  new JButton("Apply");
		finishButton.setEnabled(false);
		getRootPane().setDefaultButton(finishButton);
		removeButton = new JButton(icons.getIcon(IconManager.REMOVE));
		removeButton.setToolTipText("Remove the selected server " +
									"from the list of servers.");
		addButton = new JButton(icons.getIcon(IconManager.ADD));
		addButton.setToolTipText("Add a new server to the list of servers.");
		//addButton.setEnabled(n != 0);
		//removeButton.setEnabled(n != 0);
		//layer hosting title and empty message
		titleLayer = new JLayeredPane();
		titlePanel = new TitlePanel(TITLE, TEXT, 
									icons.getIcon(IconManager.CONFIG_48));
		titleLayer.add(titlePanel, new Integer(0));
		setEditing(n == 0);
	}
	
	/** Creates the {@link #emptyMessagePanel} if required. */
    private void buildEmptyPanel()
    {
        if (emptyMessagePanel != null) return;
        emptyMessagePanel = new JPanel();
        emptyMessagePanel.setOpaque(false);
        emptyMessagePanel.setBorder(new PartialLineBorder(Color.BLACK));
        Rectangle r = titlePanel.getBounds();
        
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
        emptyMessagePanel.setBounds(2, r.height-h-1, 3*w/2, h);
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
        p.setOpaque(true);
        return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
        Container c = getContentPane();
        setLayout(new BorderLayout(0, 0));
        c.add(titleLayer, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Sets the <code>enabled</code> flag of the {@link #finishButton},
	 * {@link #addButton} and {@link #removeButton}.
	 * 
	 * @param b The value to set.
	 */
	private void setButtonsEnabled(boolean b)
	{
		finishButton.setEnabled(b);
		addButton.setEnabled(b);
		removeButton.setEnabled(b);
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
		int n = 0;
		if (servers !=  null) n = servers.size();
		initComponents(servers, n);
		initListeners(n);
		buildGUI();
		setSize(WINDOW_DIM);
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
		finishButton.setEnabled(row != -1);
		if (previousRow == -1 || previousRow == row) return;
		if (!editing) return;
		editing = false;
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (i != previousRow) values.add((String) table.getValueAt(i, 1)); 
		}
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
		setButtonsEnabled(!warning);
		if (warning) {
			if (emptyMessagePanel != null) return;
			buildEmptyPanel();
            titleLayer.add(emptyMessagePanel, new Integer(1));
            titleLayer.validate();
            titleLayer.repaint();
        } else {
        	if (emptyMessagePanel == null) return;
        	titleLayer.remove(emptyMessagePanel);
            titleLayer.repaint();
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
		if (warning || text == null || text.length() == 0) {
			removeRow(table.getSelectedRow());
			showMessagePanel(false);
		}
		setButtonsEnabled(true);
	}
	
	/** 
     * Resizes the layered pane hosting the title when the window is resized.
     * @see ComponentListener#componentResized(ComponentEvent)
     */
	public void componentResized(ComponentEvent e) 
	{
		Rectangle r = getBounds();
		if (titleLayer == null) return;
		Dimension d  = new Dimension(r.width, TITLE_HEIGHT);
	    titlePanel.setSize(d);
	    titlePanel.setPreferredSize(d);
	    titleLayer.setSize(d);
	    titleLayer.setPreferredSize(d);
	    titleLayer.validate();
	    titleLayer.repaint();
	}

	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentShown(ComponentEvent)
     */
	public void componentShown(ComponentEvent e) {}
	
	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentHidden(ComponentEvent)
     */
	public void componentHidden(ComponentEvent e) {}

	/** 
     * Required by {@link ComponentListener} interface but no-op implementation 
     * in our case. 
     * @see ComponentListener#componentMoved(ComponentEvent)
     */
	public void componentMoved(ComponentEvent e) {}
    
}
