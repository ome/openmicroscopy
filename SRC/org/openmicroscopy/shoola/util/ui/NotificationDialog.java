/*
 * org.openmicroscopy.shoola.util.ui.NotificationDialog
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

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


//Third-party libraries
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXHeader.IconPosition;
import org.jdesktop.swingx.painter.RectanglePainter;

//Application-internal dependencies

/** 
 * A general-purpose modal dialog to display a notification message.
 * An icon can be specified to display by the message and an <i>OK</i>
 * button is provided to close the dialog.  The dialog is brought up by the
 * {@link #setVisible(boolean)} method and is automatically disposed after the
 * user closes it.
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
public class NotificationDialog
	extends JDialog
{

	/** 
	 * The preferred size of the widget that displays the notification message.
	 * Only the part of text that fits into this display area will be displayed.
	 */
	protected static final Dimension	MSG_AREA_SIZE = new Dimension(300, 50);
	
	/** 
	 * The size of the invisible components used to separate widgets
	 * horizontally.
	 */
	protected static final Dimension	H_SPACER_SIZE = new Dimension(20, 1);
	
	/** 
	 * The size of the invisible components used to separate widgets
	 * vertically.
	 */
	protected static final Dimension	V_SPACER_SIZE = new Dimension(1, 20);

    /** 
	 * The outmost container.  
	 * All other widgets are added to this panel, which, in turn, is then 
	 * added to the dialog's content pane.
	 */
	protected JXHeader	contentPanel;
	
	/** Contains the message and the message icon, if any. */
	protected JPanel	messagePanel;
	
	/** Contains the {@link #okButton}. */
	protected JPanel	controlsPanel;
	
	/** Hides and disposes of the dialog. */
	protected JButton	okButton;
		
	/** Panel hosting the UI components. */
	private JPanel 		mainPanel;
	
	/** Creates the various UI components that make up the dialog. */
	private void createComponents()
	{
		mainPanel = new JPanel();
		contentPanel = new JXHeader();
		contentPanel.setBackgroundPainter(
    			new RectanglePainter(getBackground(), null));
		messagePanel = new JPanel();
        messagePanel.setOpaque(true);
		controlsPanel = new JPanel();
		okButton = new JButton("OK");
		//okButton.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		getRootPane().setDefaultButton(okButton);
	}
	
	/**
	 * Binds the {@link #close() close} action to the exit event generated
	 * either by the close icon or by the {@link #okButton}.
	 */
	private void attachListeners()
	{
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { close(); }
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		});
	}
	
	/** Hides and disposes of the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Builds and lays out the {@link #contentPanel}.
	 * It will contain the notification message along with the message icon, 
	 * if any.
	 * 
	 * @param msg		The notification message.
	 * @param icon		The icon to display by the message.
	 * @return See above.
	 */
	private JPanel buildCommentPanel(String msg, Icon icon)
	{
		/*
		contentPanel.setBackgroundPainter(
    			new RectanglePainter(UIUtilities.WINDOW_BACKGROUND_COLOR, 
    					null));
    					*/
		contentPanel.setDescription(msg);
		contentPanel.setIcon(icon);
		contentPanel.setIconPosition(IconPosition.LEFT);
		return contentPanel;
	}
	
	/**
	 * Builds and lays out the {@link #controlsPanel}.
	 * The {@link #okButton} will be added to this panel.
	 * 
	 * @return See above.
	 */
	private JPanel buildControlPanel()
	{
		//controlsPanel.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		controlsPanel.setBorder(null);
		controlsPanel.add(okButton);
		controlsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		JPanel p = UIUtilities.buildComponentPanelRight(controlsPanel);
		//p.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		return p;
	}
	
	/**
	 * Builds and lays out the {@link #contentPanel}, then adds it to the
	 * content pane.
	 * 
	 * @param message		The notification message.
	 * @param messageIcon	The icon to display by the message.
	 */
	private void buildGUI(String message, Icon messageIcon)
	{
		//mainPanel.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		//mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    mainPanel.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
		mainPanel.add(buildCommentPanel(message, messageIcon), c);
		c.gridy++;
		mainPanel.add(Box.createVerticalStrut(5), c);
		c.gridy++;
		mainPanel.add(buildControlPanel(), c);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Initializes the UI.
	 * 
	 * @param message		The message to display.
	 * @param messageIcon   The icon laid out next to the message.
	 */
	private void initiliaze(String message, Icon messageIcon)
	{
		createComponents();
		attachListeners();
		setAlwaysOnTop(true);
		setModal(true);
		buildGUI(message, messageIcon);
		pack();
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public NotificationDialog(JFrame owner, String title, String message, 
															Icon messageIcon) 
	{
		super(owner, title);
		//setResizable(false);  
		//Believe it or not the icon from owner won't be displayed if the
		//dialog is not resizable. 
		initiliaze(message, messageIcon);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public NotificationDialog(JDialog owner, String title, String message, 
															Icon messageIcon) 
	{
		super(owner, title);
		initiliaze(message, messageIcon);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public NotificationDialog(String title, String message, Icon messageIcon) 
	{
		setTitle(title);
		initiliaze(message, messageIcon);
	}

}
