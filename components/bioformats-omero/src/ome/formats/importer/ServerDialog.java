/*
 * org.openmicroscopy.shoola.env.ui.ServerDialog 
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
package ome.formats.importer;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ome.formats.importer.util.TitlePanel;
import ome.formats.importer.util.UIUtilities;

//Third-party libraries

/** 
 * Modal dialog displayed to enter a new server name or select an
 * existing one.
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
	private static final Dimension		WINDOW_DIM = new Dimension(450, 250);
	
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
    											"test.openmicroscopy.org.uk";
    
    /** Field hosting the new server's name. */
	private JTextField 	serverName;
	
	/** Component used to select existing server. */
	private JComboBox	server;
	
	/** Button to close and dispose of the window. */
	private JButton		cancelButton;
	
	/** Button to select a new server. */
	private JButton		finishButton;
	
	/** Button to display/hide the bookmarks panel. */
	private JButton		moreOptions;
	
	/** Button to remove server from the list. */
	private JButton		removeButton;
	
	/** UI component hosting the various servers. */
	private JPanel		bookmarks;
	
	/** The panel hosting the main information. */
	private JPanel		body;
	
	/** Flag indicating if the bookmarks panel is shown or hidden. */
	private boolean		isBookmarksShowing;
	
	/** Collection of predefined servers or <code>null</code>. */
	private List		existingServers;
	
	/** Available server. */
	private JList		servers;
	
	/**
	 * Handles mouse clicks on the {@link #moreOptions}.
	 * The {@link #bookmarks} is shown/hidden depending on the current 
	 * value of {@link #isBookmarksShowing}, which is then modified to
	 * reflect the new state.  Also the {@link #moreOptions} icon is changed
	 * accordingly.
	 */
	private void handleClick()
	{
		if (isBookmarksShowing) {
			moreOptions.setIcon(getImageIcon("gfx/nuvola_1rightarrow16.png"));
			body.remove(bookmarks);
		} else {
			moreOptions.setIcon(getImageIcon("gfx/nuvola_1downarrow16.png"));
			populateBookmarks();
			body.add(bookmarks);
		}
		isBookmarksShowing = !isBookmarksShowing;
		setSize(WINDOW_DIM.width, getPreferredSize().height);
		serverName.requestFocus();
	}
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
		firePropertyChange(CLOSE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
	/** Fires a property indicating that a new server is selected. */
	private void apply()
	{
		String text = serverName.getText();
		String value = null;
		if (text == null || text.trim().length() == 0) {
			if (server != null) value = (String) server.getSelectedItem();
		} else value = text.trim();
			
		firePropertyChange(SERVER_PROPERTY, null, value);
		close();
	}
	
	/** Removes the selected server from the list. */
	private void remove()
	{
		int index = servers.getLeadSelectionIndex();
		if (index == -1) return;
		Object[] obj = (Object[]) servers.getModel().getElementAt(index);
		if (obj == null) return;
		String v = (String) obj[1];
		existingServers.remove(v);
		body.removeAll();	
		String newValue = null;
		if (existingServers.size() != 0) {
			server.setModel(new DefaultComboBoxModel(listToArray()));
			newValue = (String) server.getSelectedItem();
		}
		body.add(buildBody());
		if (existingServers.size() != 0) {
			populateBookmarks();
			body.add(bookmarks);
		}
		body.validate();
		body.repaint();
		setSize(WINDOW_DIM.width, getPreferredSize().height);
		firePropertyChange(REMOVE_PROPERTY, v, newValue);
	}
	
	/** Sets the window's properties. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
		//setResizable(false);
	}
	
	/** Adds the list of existing servers to the {@link #bookmarks}. */
	private void populateBookmarks()
	{
		//bookmarks.removeAll();
		final Object[][] objects = new Object[existingServers.size()][2];
		Icon icon = getImageIcon("gfx/server_connect16.png");
		Iterator i = existingServers.iterator();
		int index = 0;
		while (i.hasNext()) {
			objects[index][0] = icon;
			objects[index][1] = i.next();
			index++;
		}
		AbstractListModel model = new AbstractListModel() {
            public int getSize() { return objects.length; }
            public Object getElementAt(int i) { return objects[i]; }
        };
		servers.setModel(model);
	}
	
	/** 
	 * Turns the collection of existing servers into a String array. 
	 * 
	 * @return See above.
	 */
	private String[] listToArray()
	{
		if (existingServers != null && existingServers.size() != 0) {
			String[] array = new String[existingServers.size()];
			Iterator i = existingServers.iterator();
			int index = 0;
			while (i.hasNext()) {
				array[index] = (String) i.next();
				index++;
			}
			return array;
		}
		return null;
	}
	
	/** Attaches the various listeners. */
	private void initListeners()
	{
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		
		});
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { apply(); }

		});
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e) {
				serverName.requestFocus();
			} 
		});
		moreOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleClick(); }
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { remove(); }
		});
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		String[] array = listToArray();
		if (array != null) {
			server = new JComboBox(array);
	        server.setOpaque(false);
		}
		serverName = new JTextField();
		serverName.setEditable(true);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the window.");
		
		finishButton =  new JButton("Apply");
		getRootPane().setDefaultButton(finishButton);
		
		moreOptions = new JButton(getImageIcon("gfx/nuvola_1rightarrow16.png"));
		UIUtilities.unifiedButtonLookAndFeel(moreOptions);
		removeButton = new JButton(getImageIcon("gfx/remove.png"));
		removeButton.setToolTipText("Remove the selected server " +
									"from the list.");
		buildBookmarks();
	}
	
	/**
	 * Builds the main UI component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        JLabel label;
        c.gridy = 0;
        if (existingServers != null && existingServers.size() != 0) {
        	label = UIUtilities.setTextFont("Existing servers:");
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            
            content.add(server, c);  
            c.gridy++;
            c.gridx = 0;
        }
        label = UIUtilities.setTextFont("New server:");
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(serverName, c);  
        c.gridy++;
        label = new JLabel(EXAMPLE);
        label.setFont(FONT);
        content.add(label, c);
        
        if (existingServers != null && existingServers.size() != 0) {
        	c.gridy++;
            c.gridx = 0;
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(moreOptions);
            p.add(new JLabel("More Options"));
            content.add(p, c);
        }
        
        return content;
	}
	
	/** Builds and lays out the bookmarks panel. */
	private void buildBookmarks()
	{
		bookmarks = new JPanel();
		bookmarks.setLayout(new BoxLayout(bookmarks, BoxLayout.Y_AXIS));
		bookmarks.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		bookmarks.add(Box.createRigidArea(V_SPACER_SIZE));
		bookmarks.add(new JSeparator());
		bookmarks.add(Box.createRigidArea(V_SPACER_SIZE));	
		servers = new JList();
		servers.setCellRenderer(new ServerListRenderer());
		servers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		servers.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scrollpane = new JScrollPane(servers);
		bookmarks.add(UIUtilities.buildComponentPanel(
						new JLabel("List of existing servers.")));
		bookmarks.add(scrollpane);
		bookmarks.add(UIUtilities.buildComponentPanel(removeButton));
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
        Icon icon = getImageIcon("gfx/nuvola_configure48.png");
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, icon);
        Container c = getContentPane();
        setLayout(new BorderLayout(0, 0));
        c.add(titlePanel, BorderLayout.NORTH);
        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildBody());
        c.add(body, BorderLayout.CENTER);
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
		existingServers = servers;
		setProperties();
		initComponents();
		initListeners();
		buildGUI();
		setSize(WINDOW_DIM);
	}

    public String getCurrentServer()
    {
        if (serverName.getText().length() > 0)
        {
            return serverName.getText().trim();
        } else if (existingServers != null && existingServers.size() != 0){
            return server.getSelectedItem().toString().trim();
        }
        else {
            return "";
        }
    }
    
    private ImageIcon getImageIcon(String path)
    {
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null) { return new ImageIcon(imgURL); } 
        else { System.err.println("Couldn't find icon: " + imgURL); }
        return null;
    }
}
