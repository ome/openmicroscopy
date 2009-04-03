/*
 * org.openmicroscopy.shoola.env.ui.LoginOMEDS
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The dialog used by the Login Service to ask for user credentials.
 * This is mainly a dummy UI that only creates the widgets and does layout.
 * Its Controller listens to the input fields so to call the Login Service
 * when the user name and password have been entered.
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
public class LoginOMEDS
	extends JDialog
{
	
	/** The font color for the login text fields. */
	private static final Color		FONT_COLOR = new Color(0x4682B4);
	
	/** Width of the login text fields. */
	private static int 				TEXTFIELD_WIDTH = 140;
	
	/** Horizontal space between the cells in the grid. */
	private static int				H_SPACE = 40;
	
	/** Top padding. */
	private static Insets			TOP_PADDING = new Insets(10, 0, 0, 0); 
	 
	/** 
	 * The size of the invisible components used to separate widgets
	 * vertically.
	 */
	private  static final Dimension	V_SPACER_SIZE = new Dimension(1, 20);
	
	/** 
	 * The size of the invisible components used to separate widgets
	 * horizontally.
	 */
	private static final Dimension	H_SPACER_SIZE = new Dimension(20, 1);
					
    
	/** Text field to enter the login user name. */
	JTextField      				user;

	/** Password field to enter login password. */
	JPasswordField 					pass;
	
	/** Login button. */
	JButton     					loginButton;
	
    /** The Controller. */
	private LoginOMEDSManager		manager;
	
    /** Reference to the Container's registry. */
	private Registry				registry;
	
    /** Creates and initializes the login fields. */
    private void initLoginFields()
    {
        Font font = (Font) registry.lookup("/resources/fonts/Titles");
        user = new JTextField();
        user.setFont(font);
        user.setForeground(FONT_COLOR);
        user.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        pass = new JPasswordField();
        pass.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        pass.setFont(font);
        pass.setForeground(FONT_COLOR);
    }
    
    /** Creates and initializes the login button. */
    private void initLoginButton()
    {
        IconManager im = IconManager.getInstance(registry);
        loginButton = new JButton("login", im.getIcon(IconManager.LOGIN_INIT));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setToolTipText(
            UIUtilities.formatToolTipText("Connect to the OME data server."));
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        IconManager im = IconManager.getInstance(registry);
        TitlePanel tp = new TitlePanel("OMEDS Login", 
                                    "Connect to the OME data server.", 
                                    im.getIcon(IconManager.CONNECT_DS_BIG));
        //set layout and add components
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);                           
        getContentPane().add(buildBody(), BorderLayout.CENTER); 
        getContentPane().add(buildButtonsPanel(), BorderLayout.SOUTH);  
    }

    
    /** 
     * Builds the body panel.
     * 
     *  @return See above.
     */
    private JPanel buildBody()
    {
        
        JPanel body = new JPanel();
        body.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagLayout gridbag = new GridBagLayout();
        body.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel label = UIUtilities.setTextFont(" Name: ");
        c.ipadx = H_SPACE;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.insets = TOP_PADDING;
        gridbag.setConstraints(label, c);
        body.add(label);
        c.gridy = 1;
        label = UIUtilities.setTextFont(" Password: ");
        gridbag.setConstraints(label, c);
        body.add(label);
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = TEXTFIELD_WIDTH;
        gridbag.setConstraints(user, c);
        body.add(user);
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(pass, c);
        body.add(pass);
        return body;
    }
    
    /**
     * Builds and lays out the buttons.
     * The {@link #loginButton} will be added to this panel.
     * 
     * @return See above.
     */
    private JPanel buildButtonsPanel()
    {
        JPanel buttonPanel = new JPanel(), contents = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
        contents.add(Box.createRigidArea(V_SPACER_SIZE));
        contents.add(buttonPanel);
        contents.add(Box.createRigidArea(V_SPACER_SIZE));
        return contents;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent The parent of this window.
     * @param registry The Container's registry. Mustn't be <code>null</code>.
     */
	public LoginOMEDS(JFrame parent, Registry registry)
	{
		super(parent, "Login", true);
        if (registry == null) throw new NullPointerException("No registry");
		this.registry = registry;
		manager = new LoginOMEDSManager(registry, this);
		initLoginFields();
		initLoginButton();
		buildGUI();
		manager.initListeners();
		pack();
	}

}
