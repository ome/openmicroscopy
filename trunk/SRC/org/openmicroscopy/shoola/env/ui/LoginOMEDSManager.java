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
import org.openmicroscopy.shoola.env.data.login.LoginConfig;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;

/** 
 * The Controller of the {@link LoginOMEDS} dialog.
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
class LoginOMEDSManager
	implements ActionListener, DocumentListener
{
	
	/** Reference to the Container's registry. */
	private Registry			registry;
	
	/** Reference to the View. */
	private LoginOMEDS			view;
	
	private IconManager			im;
	
	private boolean				isUser, isPass;
	
	private Document 			dUser, dPass;
	

    /** Sets the icon of the {@link LoginOMEDS#loginButton}. */
    private void setLoginButtonIcon()
    {
        if (isUser && isPass) 
            view.loginButton.setIcon(im.getIcon(IconManager.LOGIN));
        else view.loginButton.setIcon(im.getIcon(IconManager.LOGIN_INIT));
    }
    
    /** 
     * Handles the selection of a new item. Allows the user to enter
     * the name of a new server if the selected item is 
     * the last one displayed
     *
     */
    private void handleServerSelection()
    {
        view.server.setEditable(
                (view.server.getSelectedItem().equals(
                        LoginConfig.DEFAULT_SERVER)));
    }
    
	/** 
	 * Creates a new instance.
	 * 
	 * @param registry		Reference to the Container's registry.
	 * @param view			Reference to the View.
	 */
	LoginOMEDSManager(Registry registry, LoginOMEDS view)
	{
		this.registry = registry;
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
		dPass = view.pass.getDocument();
		dUser.addDocumentListener(this);
		dPass.addDocumentListener(this);
		view.loginButton.addActionListener(this);
        view.server.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e)
            {
                handleServerSelection();
            }
        
        });
        view.server.setSelectedIndex(0);
	}
	
	/** 
	 * Handles action events fired by the login fields and button.
	 * Once user name and password have been entered, the login fields and
	 * button will be disabled and the Login Service will  be invoked to
     * log onto <i>OMEDS</i> with the given credentials.  The login dialog
     * is then disposed.
     * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(view.pass.getPassword());
		String usr = view.user.getText(), psw = buf.toString();
        String s = ""; //TODO
        try {
            UserCredentials uc = new UserCredentials(usr, psw, s);
            view.dispose();
            LoginService ls = (LoginService) registry.lookup(LookupNames.LOGIN);
            ls.login(uc);
        } catch (IllegalArgumentException iae) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Login Incomplete", iae.getMessage());
        }
	}

	/** 
     * Required by {@link DocumentListener} interface. 
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc)) isUser = true;
		if (dPass.equals(doc)) isPass = true;
		setLoginButtonIcon();
	}

	/** 
     * Required by {@link DocumentListener} interface. 
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc)) isUser = true;
		if (dPass.equals(doc)) isPass = true;
		setLoginButtonIcon();
	}

	/** 
     * Required by {@link DocumentListener} interface. 
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e)
	{
		Document doc = e.getDocument();
		if (dUser.equals(doc)) isUser = false;
		if (dPass.equals(doc)) isPass = false;
		setLoginButtonIcon();
	}

}
