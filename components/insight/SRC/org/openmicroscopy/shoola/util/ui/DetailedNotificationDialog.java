/*
 * org.openmicroscopy.shoola.util.ui.DetailedNotificationDialog
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

//Third-party libraries

//Application-internal dependencies

/** 
 * A general-purpose modal dialog to display a notification message and
 * show a more detailed message on request.
 * This class builds on the capabilities of {@link NotificationDialog} by
 * adding a <i>details</i> button which shows/hides a given textual
 * explanation.
 *
 * @see	org.openmicroscopy.shoola.util.ui.NotificationDialog
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
public class DetailedNotificationDialog
	extends NotificationDialog
{
	
	/** Used as text of the {@link #detailsButton}. */
	private static final String		SHOW_DETAILS = "Details >>";
	
	/** Used as text of the {@link #detailsButton}. */
	private static final String		HIDE_DETAILS = "<< Details";

    /** Used as text of the {@link #sendButton}. */
    private static final String     SEND = "Send";
    
    /** Used as tooltip of the {@link #sendButton}. */
    private static final String     SEND_DESCRIPTION = "Send the error to "+
                                        "the development team.";
                                    
	/** 
	 * The preferred size of the scroll pane containing the explanation of
	 * the notification message.
	 */
	private static final Dimension	SCROLL_PANE_SIZE = new Dimension(300, 150);
	
	/**
	 * A reduced size for the invisible components used to separate widgets
	 * vertically.
	 */
	private static final Dimension	SMALL_V_SPACER_SIZE = new Dimension(1, 6);
	
    /** Button to send a notification to the OME team. */
    private JButton         sendButton;
    
	/** Shows/hides the explanation message. */
	private JButton			detailsButton;
	
	/** Contains the textual explanation of the notification message. */
	private JPanel			explanationPanel;
	
	/** Tells whether the {@link #explanationPanel} is showing. */
	private boolean			isExplanationShowing;
	
	/** Creates the various UI components that make up the dialog. */
	private void createComponents()
	{
		detailsButton = new JButton(SHOW_DETAILS);
        sendButton = new JButton(SEND);
        sendButton.setToolTipText(SEND_DESCRIPTION);
		explanationPanel = new JPanel();
	}
	
	/**
	 * Builds and lays out the {@link #explanationPanel}.
	 * 
	 * @param expl	A detailed explanation of the notification message.
	 */
	private void buildExplanationPanel(String expl)
	{
		MultilineLabel explanation = new MultilineLabel(expl);
		explanation.setLineWrap(false);
		buildExplanationPanel(explanation);
	}
	
	/**
	 * Builds and lays out the explanation string
	 *
	 * @param component The component to go in the explanation panel.
	 */
	private void buildExplanationPanel(Component component)
    {
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setPreferredSize(SCROLL_PANE_SIZE);
		explanationPanel.setLayout(
							new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
		explanationPanel.setBorder(
								BorderFactory.createEmptyBorder(0, 10, 0, 10));
		explanationPanel.add(Box.createRigidArea(V_SPACER_SIZE));
		explanationPanel.add(new JSeparator());
		explanationPanel.add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
		explanationPanel.add(scrollPane);
	}
	
	/**
	 * Binds the {@link #handleClick() handleClick} action to the event
	 * generated when the {@link #detailsButton} is pressed and binds 
     * the {@link #handleClick() handleClick} action to the event
     * generated when the {@link #sendButton} is pressed
	 */
	private void attachListeners()
	{
		detailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleClick(); }
		});
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { handleSend(); }
        });
	}
	
	/**
	 * Handles mouse clicks on the {@link #detailsButton}.
	 * The {@link #explanationPanel} is shown/hidden depending on the current 
	 * value of {@link #isExplanationShowing}, which is then modified to
	 * reflect the new state.  Also the {@link #detailsButton} text is changed
	 * accordingly.
	 */
	private void handleClick()
	{
		if (isExplanationShowing) {
			detailsButton.setText(SHOW_DETAILS);
			contentPanel.remove(explanationPanel);
		} else {
			detailsButton.setText(HIDE_DETAILS);
			contentPanel.add(explanationPanel);
		}
		isExplanationShowing = !isExplanationShowing;
		pack();
	}
    
    /**
     * Handles mouse clicks on the {@link #sendButton}.
     * A message is then sent to the development team.
     */
    private void handleSend()
    {
        
    }
	
	/**
	 * Hooks up the {@link #detailsButton} and the {@link #explanationPanel} to
	 * the parent's GUI.
	 * 
	 * @param explanation	A detailed explanation of the notification message.
	 */
	private void buildGUI(String explanation)
	{
		controlsPanel.add(detailsButton);
        //buttonPanel.add(sendButton);
		controlsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		buildExplanationPanel(explanation);
	}
	
	/**
	 * Hooks up the {@link #detailsButton} and the {@link #explanationPanel} to
	 * the parent's GUI.
	 * 
	 * @param component A component containing the description of the
     *                  notification.
	 */
	private void buildGUI(Component component)
    {
		controlsPanel.add(detailsButton);
		controlsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		buildExplanationPanel(component);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner        The parent window.
	 * @param title        The title to display on the title bar.
	 * @param message      The notification message.
	 * @param messageIcon  An optional icon to display by the message.
	 * @param explanation  A detailed explanation of the notification message.
	 */
	public DetailedNotificationDialog(JFrame owner, String title,
										String message, Icon messageIcon,
										String explanation)
	{
		super(owner, title, message, messageIcon);
		createComponents();
		attachListeners();
		buildGUI(explanation);
		isExplanationShowing = false;
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
	 * 
	 * @param owner	The parent window.
	 * @param title	The title to display on the title bar.
	 * @param message	The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 * @param component A component holding the details
	 */
	public DetailedNotificationDialog(JFrame owner, String title,
										String message, Icon messageIcon,
										Component component)
	{
		super(owner, title, message, messageIcon);
		createComponents();
		attachListeners();
		buildGUI(component);
		isExplanationShowing = false;
	}
	
}
