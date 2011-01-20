/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.PasswordDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog to reset the password of the selected user. 
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
public class PasswordDialog 
	extends JDialog
	implements ActionListener, DocumentListener
{

	/** Bound property indicating to reset the password. */
	public static final String 	RESET_PASSWORD_PROPERTY = "resetPassword";
	
	/** The title of the dialog. */
	private static final String TITLE = "Reset Password";
	
	/** The default text. */
	private static final String TEXT = "Reset the password of the " +
			"selected experimenters.";

	/** Action indicating to close the dialog. */
	private static final int 	CANCEL = 0;
	
	/** Action indicating to reset the password. */
	private static final int 	SAVE = 1;
	
	/** Button to close and dispose. */
	private JButton 		cancelButton;
	
	/** Button to reset the password. */
	private JButton 		saveButton;
	
	/** Component used to reset the password. */
	private JPasswordField 	field;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
		setResizable(false);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		saveButton = new JButton("OK");
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		saveButton.setEnabled(false);
		field = new JPasswordField(40);
		field.getDocument().addDocumentListener(this);
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(saveButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		bar.add(cancelButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/**
	 * Builds the main pane.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel p = new JPanel();
		double[][] size = {{TableLayout.FILL, TableLayout.FILL}, 
				{TableLayout.PREFERRED, 
			TableLayout.PREFERRED}};
		p.setLayout(new TableLayout(size));
		p.add(UIUtilities.setTextFont("Password:"), "0, 0");
		p.add(field, "0, 1, 1, 1");
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, TEXT, 
				icons.getIcon(IconManager.PASSWORD_48));
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Fires a property indicating to reset the password. */
	private void save()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(field.getPassword());
		firePropertyChange(RESET_PASSWORD_PROPERTY, null, buf.toString());
		close();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frame.
	 */
	public PasswordDialog(JFrame owner)
	{
		super(owner);
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Closes the dialog or resets the password.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case SAVE:
				save();
		}
	}

	/**
	 * Sets the enabled flag of the {@link #saveButton}.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		if (field == null) return;
		char[] values = field.getPassword();
		saveButton.setEnabled(values != null && values.length > 0); 
	}

	/**
	 * Sets the enabled flag of the {@link #saveButton}.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		if (field == null) return;
		char[] values = field.getPassword();
		saveButton.setEnabled(values != null && values.length > 0);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
