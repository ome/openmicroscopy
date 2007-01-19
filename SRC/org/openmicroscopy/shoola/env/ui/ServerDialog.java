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
package org.openmicroscopy.shoola.env.ui;


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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	static final String 			SERVER_PROPERTY = "server";

	/** Bound property indicating that the window is closed. */
	static final String 			CLOSE_PROPERTY = "close";

	
	/** The default size of the window. */
	private static final Dimension	WINDOW_DIM = new Dimension(400, 250);
	
	/** Font for progress bar label. */
	private static final Font		FONT = new Font("SansSerif", Font.ITALIC, 
													10);
	/** The window's title. */
	private static final String		TITLE = "Servers";
	
	/** The textual decription of the window. */
	private static final String 	TEXT = "Enter a new server or \n" +
										"select an existing one.";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** Example of a new server. */
    private static final String		EXAMPLE = "e.g. test.openmicroscopy.org.uk";
    
    /** Field hosting the new server's name. */
	private JTextField 	serverName;
	
	/** Component used to select existing server. */
	private JComboBox	server;
	
	/** Button to close and dispose of the window. */
	private JButton		cancelButton;
	
	/** Button to select a new server. */
	private JButton		finishButton;
	
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
	
	/** Sets the window's properties. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
		//setResizable(false);
	}
	
	/**
	 * Initializes the UI components.
	 * 
	 * @param servers  Collection of predefined servers or <code>null</code>.
	 */
	private void initComponents(List servers)
	{
		if (servers != null && servers.size() != 0) {
			String[] array = new String[servers.size()];
			Iterator i = servers.iterator();
			int index = 0;
			while (i.hasNext()) {
				array[index] = (String) i.next();
				index++;
			}
			server = new JComboBox(array);
	        server.setOpaque(false);
		}
		serverName = new JTextField();
		serverName.setEditable(true);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the window.");
		cancelButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) { close(); }
		
		});
		finishButton =  new JButton("Apply");
		finishButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { apply(); }
		
		});
		addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		serverName.requestFocus();
        	} 
        });
		getRootPane().setDefaultButton(finishButton);
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
        if (server != null) {
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
		TitlePanel titlePanel = new TitlePanel(TITLE, 
                				TEXT, IconManager.getConfigLogo());
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
		setProperties();
		initComponents(servers);
		buildGUI();
		setSize(WINDOW_DIM);
	}

	
}
