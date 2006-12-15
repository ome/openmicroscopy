/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorSaverDialog
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;





//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;

/** 
 * Asks the user if she/he wants to save the edited data before switching
 * to another item.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class EditorSaverDialog
    extends JDialog
{
    
    /** Bounds poperty to save the data. */
    public static final String  SAVING_DATA_EDITOR_PROPERTY =
                            "savingDataEditor";
    
    /** The message displayed. */
    private static final String MESSAGE = "Do you want to save the modified " +
            "data before selecting a new item?";
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
	protected JPanel	  contentPanel;
	
	/** Contains the message and the message icon, if any. */
	protected JPanel	  messagePanel;
	
	/** Contains the {@link #noButton} and {@link #yesButton}. */
	protected JPanel	  buttonPanel;
	
	/** Controls to ask a confirmation question */
	private JButton	       noButton;
	
	/** Controls to ask a confirmation question */
	private JButton	       yesButton;
	
    /** Action performed when the {@link #yesButton} is pressed. */
    private void yesSelection()
    { 
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	firePropertyChange(SAVING_DATA_EDITOR_PROPERTY, Boolean.FALSE, 
        					Boolean.TRUE);
    }
    
    /** Action performed when the {@link #noButton} is pressed. */
    private void noSelection()
    { 
    	firePropertyChange(SAVING_DATA_EDITOR_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
        close();
    }
    
    /** Creates the various UI components that make up the dialog. */
    private void createComponents()
    {
        contentPanel = new JPanel();
        messagePanel = new JPanel();
        buttonPanel = new JPanel();
        noButton = new JButton("No");
        yesButton = new JButton("Yes");
    }
    
    /**
     * Binds the {@link #close() close} action to the exit event generated
     * either by the close icon or by the {@link #yesButton} or
     * the {@link #noButton}.
     */
    private void attachListeners()
    {
    	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { noSelection(); }
        });
        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { yesSelection(); }
        });
    }
    
    /**
     * Builds and lays out the {@link #messagePanel}.
     * It will contain the notification message along with the message icon, 
     * if any.
     * 
     * @param msg       The notification message.
     * @param msgIcon   The icon to display by the message.
     */
    private void buildMessagePanel(String msg, Icon msgIcon)
    {
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        if (msgIcon != null) {
            JLabel iconLabel = new JLabel(msgIcon);
            iconLabel.setAlignmentY(TOP_ALIGNMENT);
            messagePanel.add(iconLabel);
            messagePanel.add(Box.createRigidArea(H_SPACER_SIZE));
        }
        MultilineLabel message = new MultilineLabel(msg);
        message.setPreferredSize(MSG_AREA_SIZE);
        message.setAlignmentY(TOP_ALIGNMENT);
        messagePanel.add(message);
    }
    
    /**
     * Builds and lays out the {@link #buttonPanel}.
     * The {@link #noButton} and {@link #yesButton} will be added to this panel.
     */
    private void buildButtonsPanel()
    {
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(yesButton);
        buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        buttonPanel.add(noButton);
    }

    /**
     * Builds and lays out the {@link #contentPanel}, then adds it to the
     * content pane.
     * 
     * @param message       The notification message.
     * @param messageIcon   The icon to display by the message.
     */
    private void buildGUI(String message, Icon messageIcon)
    {
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createEtchedBorder(),
                                BorderFactory.createEmptyBorder(5, 5, 15, 10)));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        buildMessagePanel(message, messageIcon);
        contentPanel.add(messagePanel);
        JPanel vSpacer = new JPanel();
        vSpacer.add(Box.createRigidArea(V_SPACER_SIZE));
        contentPanel.add(vSpacer);
        buildButtonsPanel();
        contentPanel.add(buttonPanel);
        getContentPane().add(contentPanel);
        pack();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the frame.
     * @param icon  The icon displayed next to the message.
     */
    public EditorSaverDialog(JFrame owner, Icon icon)
    {
    	super(owner, "Save Edited data", true);
		createComponents();
		attachListeners();
		buildGUI(MESSAGE, icon);
    }
    
    /** Closes and disposes. */
    public void close()
    {
    	setVisible(false);
    	dispose();
    }
    
}
