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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

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
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating the selected connection speed is selected. */
	static final String 				CONNECTION_SPEED_PROPERTY = 
											"connectionSpeed";
	
	/** Bound property indicating that a new server is selected. */
	static final String 				SERVER_PROPERTY = "server";

	/** Bound property indicating that the window is closed. */
	static final String 				CLOSE_PROPERTY = "close";

	/** Bound property indicating that the window is closed. */
	static final String 				REMOVE_PROPERTY = "remove";
    
	/** ID identifying the close action. */
	private static final int			CLOSE = 0;
	
	/** ID identifying the apply action. */
	private static final int			APPLY = 1;
	
	/** ID identifying the selection of a high speed connection. */
	private static final int			HIGH_SPEED = 2;
	
	/** ID identifying the selection of a medium speed connection. */
	private static final int			MEDIUM_SPEED = 3;
	
	/** ID identifying the selection of a low speed connection. */
	private static final int			LOW_SPEED = 4;
	
	/** The default size of the window. */
	private static final Dimension		WINDOW_DIM = new Dimension(400, 450);
	
	/** The window's title. */
	private static final String TITLE = "Servers";
	
	/** The textual description of the window. */
	private static final String TEXT = "Enter a new server or \n" +
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

	/** Button to close and dispose of the window. */
	private JButton			cancelButton;
	
	/** Button to select a new server. */
	private JButton			finishButton;
	
	/** Reference to the editor hosting the table. */
	private ServerEditor	editor;
    
    /** The component hosting the title and the warning messages if required. */
    private JLayeredPane    titleLayer;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
    /** Group hosting the connection speed level. */
    private ButtonGroup 	buttonsGroup;
    
    /** The original Speed connection if set. */
    private int				originalIndexSpeed;

    /** The selected server when the dialog is open. */
    private String			server;
    
	/** Closes and disposes. */
	private void close()
	{
		if (editor != null) editor.stopEdition();
		setVisible(false);
		dispose();
	}
	
	/** Sets the window's properties. */
	private void setProperties()
	{
		setName("server dialog");
		setTitle(TITLE);
		setModal(true);
		setAlwaysOnTop(true);
	}
	
	/** Attaches the various listeners. */
	private void initListeners()
	{
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CLOSE);
		finishButton.addActionListener(this);
		finishButton.setActionCommand(""+APPLY);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e) { close(); }
        	public void windowOpened(WindowEvent e)
        	{ 
        		if (editor != null) editor.setFocus(server);
        	} 
        });
	}
	
	/** 
	 * Initializes the UI components.
	 * 
	 * @param title Pass <code>true</code> to display the title, 
	 * <code>false</code> otherwise.
	 */
	private void initComponents(boolean title)
	{
		if (editor != null) editor.addPropertyChangeListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setName("cancel button");
		cancelButton.setToolTipText("Close the window.");
		finishButton =  new JButton("Apply");
		finishButton.setName("apply button");
		finishButton.setEnabled(false);
		getRootPane().setDefaultButton(finishButton);
		//layer hosting title and empty message
		IconManager icons = IconManager.getInstance();
		if (title) {
			titleLayer = new JLayeredPane();
			titlePanel = new TitlePanel(TITLE, TEXT, icons.getIcon(
					IconManager.CONFIG_48));
		}
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setOpaque(true);
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
	}
	
	/**
	 * Builds and lays out the UI.
	 * 
	 *  @param index The connection speed index.
	 */
	private void buildGUI(int index)
	{
		JPanel mainPanel;
		if (index == -1) mainPanel = editor;
		else mainPanel = buildConnectionSpeed(index, editor);
        Container c = getContentPane();
        if (titlePanel != null)
        	c.add(titlePanel, BorderLayout.NORTH);
        c.add(mainPanel, BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Shows the warning message if the passed value is <code>true</code>,
	 * hides it otherwise.
	 * 
	 * @param warning 	Pass <code>true</code> to show the message, 
	 * 					<code>false</code> otherwise.	
	 * @param p			The component to add or remove.		
	 */
	private void showMessagePanel(boolean warning, JComponent p)
	{
		if (warning) {
            titleLayer.add(p, Integer.valueOf(1));
            titleLayer.validate();
            titleLayer.repaint();
        } else {
        	if (p == null) return;
        	titleLayer.remove(p);
            titleLayer.repaint();
        }
	}

	/** 
	 * Adds the connection speed options to the display. 
	 * 
	 * @param index The default index.
	 * @param comp The component to add to the display.
	 * @return See above.
	 */
	private JPanel buildConnectionSpeed(int index, JComponent comp)
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Connection Speed"));
		buttonsGroup = new ButtonGroup();
		JRadioButton button = new JRadioButton();
		button.setText("LAN");
		button.setActionCommand(""+HIGH_SPEED);
		button.addActionListener(this);
		button.setSelected(index == LoginCredentials.HIGH);
		buttonsGroup.add(button);
		p.add(button);
		button = new JRadioButton();
		button.setText("High (Broadband)");
		button.setActionCommand(""+MEDIUM_SPEED);
		button.setSelected(index == LoginCredentials.MEDIUM);
		button.addActionListener(this);
		buttonsGroup.add(button);
		p.add(button);
		button = new JRadioButton();
		button.setText("Low (Dial-up)");
		button.setActionCommand(""+LOW_SPEED);
		button.setSelected(index == LoginCredentials.LOW);
		button.addActionListener(this);
		buttonsGroup.add(button);
		p.add(button);
		if (comp == null) return p;
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(comp);
		p = UIUtilities.buildComponentPanel(p);
		content.add(p);
		return content;
	}

	/**
	 * Sets the enabled flag of the {@link #finishButton} depending on 
	 * the passed factor.
	 * 
	 * @param index The connection index.
	 */
	private void setControlEnabled(int index)
	{
		int factor = -1;
		switch (index) {
			case HIGH_SPEED:
				factor = LoginCredentials.HIGH;
				break;
			case MEDIUM_SPEED:
				factor = LoginCredentials.MEDIUM;
				break;
			case LOW_SPEED:
				factor = LoginCredentials.LOW;
		}
		if (editor != null) {
			int original = editor.getOriginalRow();
			if (original == -1)
				finishButton.setEnabled(true);
			else {
				if (editor.isOriginal(server))
					finishButton.setEnabled(originalIndexSpeed != factor);
				else finishButton.setEnabled(true);
			}
		} else finishButton.setEnabled(originalIndexSpeed != factor);
	}
	
	/** Fires a property indicating that a new server is selected. */
	private void apply()
	{
		//Check list of servers and remove empty from list
		String server = null;
		if (editor != null) {
			editor.stopEdition();
			server = editor.getSelectedServer();
			editor.onApply();
		}
		if (server != null && server.length() > 0) {
			String port = editor.getSelectedPort();
			editor.handleServers(server, editor.getSelectedPort());
			String value = server+ServerEditor.SERVER_PORT_SEPARATOR+port;
			firePropertyChange(SERVER_PROPERTY, null, value);
		}
		if (buttonsGroup != null) {
			Enumeration en = buttonsGroup.getElements();
			JRadioButton button;
			int index;
			while (en.hasMoreElements()) {
				button = (JRadioButton) en.nextElement();
				if (button.isSelected()) {
					index = Integer.parseInt(button.getActionCommand());
					switch (index) {
						case HIGH_SPEED:
							firePropertyChange(CONNECTION_SPEED_PROPERTY, null, 
									Integer.valueOf(LoginCredentials.HIGH));
							break;
						case MEDIUM_SPEED:
							firePropertyChange(CONNECTION_SPEED_PROPERTY, null, 
									Integer.valueOf(LoginCredentials.MEDIUM));
							break;
						case LOW_SPEED:
							firePropertyChange(CONNECTION_SPEED_PROPERTY, null, 
									Integer.valueOf(LoginCredentials.LOW));
					}
				}
			}
		}
		close();
	}

	/** 
	 * Creates a new instance. 
	 * 
	 * @param frame		 The parent frame. 
	 * @param editor 	 The server editor. Mustn't be <code>null</code>.
	 * @param server	The currently selected server or <code>null</code>.
	 * @param index		 The speed of the connection.
	 */
	ServerDialog(JFrame frame, ServerEditor editor, String server, int index)
	{ 
		super(frame);
		this.server = server;
		originalIndexSpeed = index;
		this.editor = editor;
		setProperties();
		initComponents(true);
		initListeners();
		buildGUI(index);
		pack();
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param frame The parent frame.
	 * @param index The speed of the connection.
	 */
	ServerDialog(JFrame frame, int index)
	{ 
		super(frame);
		originalIndexSpeed = index;
		setProperties();
		initComponents(false);
		initListeners();
		setTitle("");
		Container c = getContentPane();
        c.add(buildConnectionSpeed(index, null), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
		pack();
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param frame		The parent frame. 
	 * @param editor 	The server editor. Mustn't be <code>null</code>.
	 * @param server	The currently selected server or <code>null</code>.
	 */
	ServerDialog(JFrame frame, ServerEditor editor, String server)
	{ 
		this(frame, editor, server, -1);
	}
	
	/**
	 * Reacts to property changes fired by the{@link ServerEditor}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (ServerEditor.EDIT_PROPERTY.equals(name)) {
			Boolean value = (Boolean) evt.getNewValue();
			if (editor.isEditing()) finishButton.setEnabled(value);
			else {
				if (originalIndexSpeed == -1) {
					setControlEnabled(originalIndexSpeed);
				} 
				if (buttonsGroup != null) {
					Enumeration en = buttonsGroup.getElements();
					JRadioButton button;
					int index;
					while (en.hasMoreElements()) {
						button = (JRadioButton) en.nextElement();
						if (button.isSelected()) {
							index = Integer.parseInt(button.getActionCommand());
							setControlEnabled(index);
						}
					}
				}
			}
		} else if (ServerEditor.ADD_MESSAGE_PROPERTY.equals(name)) {
			showMessagePanel(true, (JComponent) evt.getNewValue());
		}  else if (ServerEditor.REMOVE_MESSAGE_PROPERTY.equals(name)) {
			showMessagePanel(false, (JComponent) evt.getNewValue());
		} else if (ServerEditor.APPLY_SERVER_PROPERTY.equals(name)) {
			apply();
		}
	}
	
	/** 
	 * Reacts to the selection of the button.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				close();
				break;
			case APPLY:
				apply();
				break;
			case HIGH_SPEED:
			case MEDIUM_SPEED:
			case LOW_SPEED:
				setControlEnabled(index);
		}
	}
	
}
