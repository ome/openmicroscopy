/*
 * org.openmicroscopy.shoola.env.ui.LoginManager
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;

/** 
 * 
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
public class LoginOMEDSManager
	implements ActionListener, DocumentListener
{

	/**
	 * Tells whether or not the orignal service activation request succeded.
	 */
	private boolean				activationSuccessful;
	
	/** Reference to the registry. */
	private Registry			registry;
	
	/** Reference to the DataServicesFactory. */
	private DataServicesFactory	dsf;
	
	/** Reference to the view. */
	private LoginOMEDS			view;
	
	private IconManager			im;
	
	private boolean				isUser, isPass;
	
	private Document 			dUser, dPass;
	
	/** 
	 * Create a new instance.
	 * 
	 * @param container		Reference to the container.
	 * @param view			Reference to the view.
	 */
	LoginOMEDSManager(Registry registry, DataServicesFactory dsf,
					LoginOMEDS view)
	{
		this.registry = registry;
		this.dsf = dsf;
		this.view = view;
		im = IconManager.getInstance(registry);
		isUser = false;
		isPass = false;
	}
	
	/** Initializes the listeners. */
	void initListeners()
	{
		view.user.addActionListener(this);
		view.pass.addActionListener(this);
		dUser = view.user.getDocument(); 
		dPass =  view.pass.getDocument();
		dUser.addDocumentListener(this);
		dPass.addDocumentListener(this);
		view.loginButton.addActionListener(this);
	}

	public boolean isActivationSuccessful() { return activationSuccessful; }
	
	/** 
	 * Handles action events fired by the login fields and button.
	 * Once user name and password have been entered, the login fields and
	 * button will be disabled. 
	 */
	public void actionPerformed(ActionEvent e)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(view.pass.getPassword());
		String usr = view.user.getText(), psw = buf.toString();
		if (usr == null || usr.length() == 0) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Login Incomplete", "Please enter a user name");
		} else {
			if (psw == null || psw.length() == 0) {
				UserNotifier un = registry.getUserNotifier();
				un.notifyError("Login Incomplete", "Please enter a password");
			} else connect(usr, psw);
		}
	}
	
	/** 
	 * Try to connect to OMEDS.
	 * 
	 * @param usr		User's name.
 	 * @param psw		User's password.
	 */
	private void connect(String usr, String psw) 
	{
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);
		uc.set(usr, psw);
		try { 
			dsf.connect();
			activationSuccessful = true;
		} catch (Exception e) {
			String s = "Can't connect to the server. "+e;
			registry.getLogger().error(this, s);
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Login to OMEDS Incomplete", 
							"Can't connect to the server. Please try later.",
							e);
			activationSuccessful = false;
		}
		view.dispose();
	}

	/** Set the icon of the loginButton. */
	private void setLoginButtonIcon()
	{
		if (isUser && isPass) 
			view.loginButton.setIcon(im.getIcon(IconManager.LOGIN));
		else view.loginButton.setIcon(im.getIcon(IconManager.LOGIN_INIT));
	}
	
	/** Require by {@link DocumentListener} interface. */
	public void changedUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc))	isUser = true;
		if (dPass.equals(doc))	isPass = true;
		setLoginButtonIcon();
	}

	/** Require by {@link DocumentListener} interface. */
	public void insertUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc))	isUser = true;
		if (dPass.equals(doc))	isPass = true;
		setLoginButtonIcon();
	}

	/** Require by {@link DocumentListener} interface. */
	public void removeUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc))	isUser = false;
		if (dPass.equals(doc))	isPass = false;
		setLoginButtonIcon();
	}

}
