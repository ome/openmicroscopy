/*
 * adminTool.ui.PasswordDialog 
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package adminTool.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PasswordDialog 
	extends JDialog
	implements ActionListener
{
	private JLabel			passwordLabel;
	private JPasswordField 	passwordField;
	private	JButton			okBtn;
	private JButton			cancelBtn;
	
	private	boolean			okSelected;
	
	public PasswordDialog()
	{
		super();
		this.setModal(true);
		createUI();
		okSelected = false;
		setSize(240,100);
	}
	
	public String getPassword()
	{
		return new String(passwordField.getPassword());
	}
	
	public boolean OKSelected()
	{
		return okSelected;
	}
	
	private void createUI()
	{
		createControls();
		attachListeners();
		buildUI();
	}
	
	private void createControls()
	{
		passwordLabel = new JLabel("Enter new Password");
		passwordField = new JPasswordField();
		okBtn = new JButton("Ok");
		cancelBtn = new JButton("Cancel");
	}
	
	private void attachListeners()
	{
		okBtn.addActionListener(this);
		okBtn.setActionCommand("OK");
		cancelBtn.addActionListener(this);
		cancelBtn.setActionCommand("Cancel");
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand()=="OK")
		{
			okSelected=true;
			this.setVisible(false);
		}
		if(e.getActionCommand()=="Cancel")
		{
			okSelected=false;
			this.setVisible(false);
		}
	}
	
	public void buildUI()
	{
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel passwordPanel = new JPanel();
		passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
		passwordPanel.add(Box.createHorizontalStrut(10));
		passwordPanel.add(passwordLabel);
		passwordPanel.add(Box.createHorizontalStrut(10));
		passwordPanel.add(passwordField);
		passwordPanel.add(Box.createHorizontalStrut(10));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(okBtn);
		buttonPanel.add(cancelBtn);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(passwordPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(buttonPanel);
		this.add(mainPanel);
	}
}


