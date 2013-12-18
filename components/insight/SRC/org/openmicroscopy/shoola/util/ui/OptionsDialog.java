/*
 * org.openmicroscopy.shoola.util.ui.YesNoDialog
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
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXHeader.IconPosition;
import org.jdesktop.swingx.painter.RectanglePainter;

//Application-internal dependencies

/** 
 * A general-purpose modal dialog to display a notification message and to 
 * ask a confirmation question.
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
public class OptionsDialog
	extends JDialog
{

	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** 
	 * The outmost container.  
	 * All other widgets are added to this panel, which, in turn, is then 
	 * added to the dialog's content pane.
	 */
	private JXHeader		contentPanel;
	
	/** Controls to ask a confirmation question */
	private JButton			noButton;
	
	/** Controls to ask a confirmation question */
	private JButton	       	yesButton;
	
	/** Controls to cancel the operation */
	private JButton	       	cancelButton;
	
	/** Panel hosting the UI components. */
	private JPanel 			mainPanel;
	
	/** Panel hosting the UI buttons. */
	private JPanel 			controlPanel;
	
	/** The component hosting added components. */
	protected JPanel			body;
	
    /** Action performed when the {@link #yesButton} is pressed. */
    private void yesSelection()
    { 
        onYesSelection();
        close(); 
    }
    
    /** Action performed when the {@link #noButton} is pressed. */
    private void noSelection()
    { 
        onNoSelection();
        close();
    }
    
    /** Action performed when the {@link #cancelButton} is pressed. */
    private void cancel()
    { 
        onCancel();
        close();
    }
    
    /** Creates the various UI components that make up the dialog. */
    private void createComponents()
    {
    	body = new JPanel();
    	mainPanel = new JPanel();
    	controlPanel = new JPanel();
        contentPanel = new JXHeader();
        noButton = new JButton("No");
        noButton.setName("no button");
        yesButton = new JButton("Yes");
        yesButton.setName("yes button");
        getRootPane().setDefaultButton(yesButton);
    }
    
    /**
     * Binds the {@link #close() close} action to the exit event generated
     * either by the close icon or by the {@link #yesButton} or
     * the {@link #noButton}.
     */
    private void attachListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { close(); }
        });
        noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { noSelection(); }
        });
        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { yesSelection(); }
        });
    }
    
    /** Hides and disposes of the dialog. */
    private void close()
    {
        setVisible(false);
        dispose();
    }

    /**
	 * Builds and lays out the panel hosting the comments.
	 * 
	 * @param instructions	The message explaining to the user what to do.
	 * @param icon			The icon to display.
	 * @return See above.
	 */
	private JPanel buildCommentPanel(String instructions, Icon icon)
	{
		/*
    	contentPanel.setBackgroundPainter(
    			new RectanglePainter(UIUtilities.WINDOW_BACKGROUND_COLOR, 
    					null));
    					*/
		contentPanel.setBackgroundPainter(
    			new RectanglePainter(getBackground(), null));
		contentPanel.setDescription(instructions);
		contentPanel.setIcon(icon);
		contentPanel.setIconPosition(IconPosition.LEFT);
		return contentPanel;
	}
	
	/**
	 * Builds the panel hosting the components.
	 * 
	 * @return See above.
	 */
	private JPanel buildControlPanel()
	{
		//controlPanel.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		controlPanel.setBorder(null);
		controlPanel.add(yesButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		controlPanel.add(noButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
		//bar.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		bar.add(controlPanel);
		bar.add(Box.createVerticalStrut(10));
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
		//p.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		return p;
	}
	
    /**
     * Builds and lays out the {@link #contentPanel}, then adds it to the
     * content pane.
     * 
     * @param message   The notification message.
     * @param icon  	The icon to display by the message.
     */
    private void buildGUI(String message, Icon icon)
    {
    	JXHeader header = new JXHeader();
    	header.setBackgroundPainter(
    			new RectanglePainter(getBackground(), null));
    	header.setDescription(message);
    	mainPanel.setOpaque(false);
    	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    	double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.FILL},
    			{TableLayout.PREFERRED, TableLayout.FILL, 
    			TableLayout.PREFERRED}};
    	mainPanel.setLayout(new TableLayout(size));
    	mainPanel.add(new JLabel(icon), "0, 0");
    	mainPanel.add(header, "2, 0");
    	mainPanel.add(body, "0, 1, 2, 1");
    	mainPanel.add(buildControlPanel(), "0, 2, 2, 2");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Initializes the components and builds the dialog.
     * 
     * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
     */
    private void initialize(String message, Icon messageIcon)
    {
    	createComponents();
		attachListeners();
		buildGUI(message, messageIcon);
		pack();
		setName("options dialog");
		//setResizable(false);
    }
    
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public OptionsDialog(JFrame owner, String title, String message, 
						Icon messageIcon) 
	{
		super(owner, title, true);
		initialize(message, messageIcon);
	}
	
	/**
	 * Creates a new dialog.
	 * You have to call {@link #setVisible(boolean)} method to actually
     * display it on screen.
	 * 
	 * @param owner			The parent window.
	 * @param title			The title to display on the title bar.
	 * @param message		The notification message.
	 * @param messageIcon	An optional icon to display by the message.
	 */
	public OptionsDialog(JDialog owner, String title, String message, 
						Icon messageIcon) 
	{
		super(owner, title, true);
		initialize(message, messageIcon);
	}
	
	/** 
	 * Adds the specified component to the main panel.
	 * 
	 * @param c The component to add. Mustn't be <code>null</code>.
	 */
	public void addBodyComponent(JComponent c) 
	{
		body.add(c);
		body.validate();
		body.repaint();
		mainPanel.validate();
		mainPanel.repaint();
		pack();
	}
	
	/**
	 * Sets the text of the {@link #yesButton}.
	 * 
	 * @param txt The value to set.
	 */
	public void setYesText(String txt)
	{
		if (txt == null || txt.trim().length() == 0) return;
		yesButton.setText(txt);
	}
	
	/**
	 * Sets the label of the {@link #noButton}.
	 * 
	 * @param txt The value to set.
	 */
	public void setNoText(String txt)
	{
		if (txt == null || txt.trim().length() == 0) return;
		noButton.setText(txt);
	}
	
	/**
	 * Sets the label of the {@link #cancelButton}.
	 * 
	 * @param txt The value to set.
	 */
	public void setCancelText(String txt)
	{
		if (txt == null || txt.trim().length() == 0) return;
		if (cancelButton == null)	return;
		cancelButton.setText(txt);
	}
	
	/** Hides the {@link #noButton}. */
	public void hideNoButton()
	{
		controlPanel.remove(noButton);
		controlPanel.revalidate();
		repaint();
	}
	
	/**
	 * Sets the enabled flag of the {@link #yesButton}.
	 * 
	 * @param enabled The value to set.
	 */
	public void setYesEnabled(boolean enabled)
	{
		yesButton.setEnabled(enabled);
	}
	
	/** Adds the {@link #cancelButton}. */
	public void addCancelButton()
	{
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.setName("cancel button");
			//cancelButton.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
			cancelButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) { cancel(); }
	        });
		}
		controlPanel.add(cancelButton);
		repaint();
	}
	
    /**
     * Subclasses should override the method to perform an action.
     * We cannot fire a property change event b/c the dialog is modal.
     */
    protected void onYesSelection() {}
    
    /**
     * Subclasses should override the method to perform an action.
     * We cannot fire a property change event b/c the dialog is modal.
     */
    protected void onNoSelection() {}
    
    /**
     * Subclasses should override the method to perform an action.
     * We cannot fire a property change event b/c the dialog is modal.
     */
    protected void onCancel() {}
    
}
