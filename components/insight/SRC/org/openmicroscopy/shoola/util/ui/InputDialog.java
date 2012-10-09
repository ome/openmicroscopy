/*
 * org.openmicroscopy.shoola.util.ui.InputDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries

//Application-internal dependencies

/** 
 * Basic dialog displaying a text area.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class InputDialog 
	extends JDialog
	implements ActionListener, DocumentListener
{
	
	/** Bound property indicating the tag has been edited. */
	public static final String	EDIT_PROPERTY = "edit";
	
	/** Bound property indicating the tag has been edited. */
	public static final String	CLOSE_PROPERTY = "close";
	
	/** Action command id to cancel the edition. */
	public static final int	CANCEL = 0;
	
	/** Action command id to save the edition. */
	public static final int	SAVE = 1;
	
	/** The default size of the dialog. */
	private static final Dimension DEFAULT_SIZE = new Dimension(300, 150);
	
	/** Button to cancel the edition. */
	private JButton			cancel;
	
	/** Button to save the data. */
	private JButton			save;
	
	/** The field where to enter the text. */
	private JTextField		textValue;
	
	/** The text entered in the {@link #textValue} when initialized. */
	private String			originalText;
	
	/** The icon displayed on the left-hand side. */
	private Icon			icon;
	
	/** The option returned when the dialog is shown. */
	private int 			option;
	
	/** Initializes the components. */
	private void initComponents()
	{
		option = -1;
		cancel = new JButton("Cancel");
		cancel.setToolTipText("Cancel edition.");
		cancel.addActionListener(this);
		cancel.setActionCommand(""+CANCEL);
		save = new JButton("OK");
		save.setToolTipText("Save edition.");
		save.addActionListener(this);
		save.setActionCommand(""+SAVE);
		save.setEnabled(false);
		//area = new MultilineLabel(originalText);
		textValue = new JTextField(originalText);
		textValue.setCaretPosition(originalText.length());
		textValue.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		textValue.getDocument().addDocumentListener(this);
		textValue.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			}
		});
		
		getRootPane().setDefaultButton(save);
	}
	
	/** 
	 * Enables or not the {@link #save} button depending on the 
	 * text entered.
	 */
	private void handleTextModification()
	{
		String text = textValue.getText();
		text = text.trim();
		save.setEnabled(!originalText.equals(text));
	}
	
	/** Closes the window and disposes. */
	private void cancel()
	{
		firePropertyChange(CLOSE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		setVisible(false);
		dispose();
	}
	
	/** Saves the edition. */
	private void save()
	{
		String text = textValue.getText();
		firePropertyChange(EDIT_PROPERTY, originalText, text);
		cancel();
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildBar()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(cancel);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(save);
		bar.add(Box.createHorizontalStrut(5));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		top.setLayout(new BorderLayout());
		JPanel body = new JPanel(new GridBagLayout());
		Container realBody = new JPanel(new BorderLayout());
		realBody.add(body, BorderLayout.CENTER);
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = cons.gridy = 0;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.gridheight = 1;
		cons.anchor = GridBagConstraints.CENTER;
		cons.insets = new Insets(0, 0, 3, 0);
		cons.fill = GridBagConstraints.HORIZONTAL;
	    cons.weightx = 1;
	    body.add(textValue, cons);
		top.add(realBody, BorderLayout.CENTER);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		if (icon != null) {
			JLabel iconLabel = new JLabel(icon);
			iconLabel.setVerticalAlignment(SwingConstants.CENTER);
			p.add(iconLabel);
			p.add(Box.createHorizontalStrut(10));
		}
		top.add(p, BorderLayout.BEFORE_LINE_BEGINS);
		top.add(buildBar(), BorderLayout.SOUTH);
		
		getContentPane().add(top);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param owner 		The owner of the dialog.
	 * @param title			The title of the frame.
	 */
	public InputDialog(JFrame owner, String title)
	{
		this(owner, title, null, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner 		The owner of the dialog.
	 * @param title			The title of the frame.
	 * @param originalText	The text to edit. 
	 */
	public InputDialog(JFrame owner, String title, String originalText)
	{
		this(owner, title, originalText, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner 		The owner of the dialog.
	 * @param title			The title of the frame.
	 * @param originalText	The text to edit. 
	 * @param icon			The icon to display on the left-hand side.
	 */
	public InputDialog(JFrame owner, String title, String originalText, Icon
			icon)
	{
		super(owner);
		if (icon == null)
			icon= IconManager.getInstance().getIcon(
					IconManager.QUESTION_ICON_48);
		this.icon = icon;
		if (originalText == null) originalText = "";
		this.originalText = originalText;
		setTitle(title);
		setModal(true);
		initComponents();
		buildGUI();
		setSize(DEFAULT_SIZE);
	}
	
	/**
	 * Returns the option.
	 * 
	 * @return See above.
	 */
	public int getOption() { return option; }

	/** 
	 * Returns the text.
	 * 
	 * @return See above.
	 */
	public String getText() { return textValue.getText(); }
	
	/**
	 * Sets the text.
	 * 
	 * @param text The value to set.
	 */
	public void setText(String text) { textValue.setText(text); }
	
	/**
     * Shows the message box at the passed location and returns the option 
     * selected by the user. 
     * 
     * @param location The location of the <code>Dialog</code>.
     * @return The option selected by the user. 
     */
    public int showMsgBox(Point location)
    {
    	if (location == null) location = getParent().getLocation();
    	setLocation(location);
    	setVisible(true);
    	return option;	
    }
    
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int showMsgBox() { return showMsgBox(getParent().getLocation()); }
   
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int centerMsgBox()
    {
    	UIUtilities.centerAndShow(this);
    	return option;	
    }
    
	/**
	 * Saves or cancels.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		option = index;
		switch (index) {
			case CANCEL:
				cancel();
				break;
			case SAVE:
				save();
		}
	}
	
	/**
	 * Enables or not the {@link #save} control.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { handleTextModification(); }

	/**
	 * Enables or not the {@link #save} control.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { handleTextModification(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
