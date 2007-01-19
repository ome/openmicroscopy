/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenView
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;

//Third-party libraries
import layout.TableLayout;
import layout.TableLayoutConstants;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The splash screen UI. 
 * This class is completely dummy, the logic to control the widgets is in 
 * {@link SplashScreenManager}. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author Brian Loranger &nbsp;&nbsp;&nbsp;&nbsp;
 * 		<a href="mailto:brian.loranger@lifesci.dundee.ac.uk">
 * 			brian.loranger@lifesci.dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

class SplashScreenView 
	extends JFrame
{

	/** The title of the splash screens. */
	static final String				TITLE = "Open Microscopy Environment";
	
	/** 
	 * The width of the splash screen window. 
	 * This value must be the same as the width of the splash screen image.
	 */
	static final int				LOGIN_WIDTH = 551;  
		
	/** 
	 * The width of the splash screen window. 
	 * This value must be the same as the width of the splash screen image.
	 */
	static final int				LOGIN_HEIGHT = 113;  
    
	/** Font for progress bar label. */
	static final Font				FONT = new Font("SansSerif", Font.PLAIN, 
													10);
    
	/** Font for progress bar label. */
	static final Font				TASK_FONT = new Font("SansSerif", 
													Font.PLAIN, 8);
    
	/** The font color for text. */
    static final Color      		TEXT_COLOR   = Color.WHITE;
		
    /** The size of the font for the version. */
    private static final float		VERSION_FONT_SIZE   = 10;
    
    /** The size of the font for the text. */
    private static final int      	TEXT_FONT_SIZE   = 18;
    
    /** The login text. */
    private static final String		TEXT_LOGIN = "Please Log In";
    
    /** The username text. */
    private static final String		USER_TEXT = "Username: ";
    
    /** The password text. */
    private static final String		PASSWORD_TEXT = "Password: ";
    
    /** Default text if no server. */
    private static final String		DEFAULT_SERVER = "Add a new server ->";
	
    /** The client's version. */
    private static final String     VERSION = "3.0_M3_Beta1/OMERO M3";
    
	/** Text field to enter the login user name. */
	JTextField          user;
	
	/** Password field to enter login password. */
	JPasswordField      pass;
	
	/** Config button. */
	JButton				configButton;
	
	/** Login button. */
	JButton             login;
	
    /** Cancel button. */
    JButton             cancel;
	
    /** Label hosting the version of shoola. */
	JLabel              versionLabel;
    
    /** Box displaying the user preferred server on the current machine. */
    JComboBox           server;
    
    /** Collection of available servers. */
    List				servers;
    
    /** The name of the server or default value if none already defined. */
    String				serverName;
    
    /** Field hosting the server text. */
    JTextPane 			serverText;

    /** Creates and initializes the login fields and the version label. */
    private void initFields()
    {
    	user = new JTextField(20);
    	user.setToolTipText("Enter your username.");
    	pass = new JPasswordField();
    	pass.setToolTipText("Enter your password.");
    	
    	servers = UIFactory.getServers();
    	if (servers == null || servers.size() == 0)
    		serverName = DEFAULT_SERVER;
    	else serverName = (String) servers.get(0);
        serverText = UIUtilities.buildTextPane(serverName, TEXT_COLOR);
    }
    
    /**
     * Removes border and margin for the specified button and sets the default
     * cursor to {@link Cursor#HAND_CURSOR}.
     * 
     * @param button        The button to set the default for.
     * @param rollOverIcon  The rollover icon for the specified button.
     */
    private void setButtonDefault(JButton button)
    {
        //Next two statements get rid of surrounding border.
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    /** Creates and initializes the login button and the cancel button. */
    private void initButtons()
    {
    	login = new JButton("Login");
    	login.setMnemonic('L');
    	login.setToolTipText("Login");
    	setButtonDefault(login);
    	UIUtilities.enterPressesWhenFocused(login);
    	cancel = new JButton("Quit");
    	cancel.setMnemonic('Q');
    	cancel.setToolTipText("Quit the Application");
    	setButtonDefault(cancel);
    	configButton = new JButton();
    	configButton.setMnemonic('X');
    	configButton.setToolTipText("Config Server");
    	configButton.setBorderPainted(false);
    	configButton.setBorder(null);
    	configButton.setMargin(new Insets(0, 0, 0, 0));
    	configButton.setFocusPainted(false);
    	configButton.setContentAreaFilled(false);
    	configButton.setIcon(IconManager.getConfigButton());
    	configButton.setPressedIcon(IconManager.getConfigButtonPressed());
    }
    
    /**
     * Builds and lays out the specified text field.
     * 
     * @param field		The field to lay out.
     * @param mnemonic	The mnemonic value.
     * @param s			The value to display in front of the field.
     * @return See above.
     */
    private JPanel buildTextPanel(JTextField field, int mnemonic, String s)
    {
    	double[][] size = new double[][]{{TableLayoutConstants.PREFERRED, 
    										TableLayout.FILL}, {30}};
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);       

        JLabel label = UIUtilities.setTextFont(s);
        label.setForeground(TEXT_COLOR);
        label.setDisplayedMnemonic(mnemonic);
        
        label.setLabelFor(field);
        label.setOpaque(false);
        panel.add(label, "0, 0, r, c");        
        panel.add(field, "1, 0, f, c");
        return panel;
    }
    
    /**
     * Builds and lays out the panel hosting the login information.
     * 
     * @return See above.
     */
    private JPanel buildTopPanel()
    {
    	double topTable[][] =  {{245, 18, 220, 28}, // columns
    							{32, TableLayout.FILL}}; // rows
    	JPanel topPanel = new JPanel();
    	topPanel.setOpaque(false);
        topPanel.setLayout(new TableLayout(topTable));   
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JTextPane pleaseLogIn = UIUtilities.buildTextPane(TEXT_LOGIN, 
        												TEXT_COLOR);
        Font f = pleaseLogIn.getFont();
        Font newFont = f.deriveFont(Font.BOLD, TEXT_FONT_SIZE);
        pleaseLogIn.setFont(newFont);
        topPanel.add(pleaseLogIn, "0, 0, l, c"); //Add to panel.
        
        topPanel.add(serverText, "2, 0, r, c"); //Add to panel.
        topPanel.add(configButton, "3, 0, c, c");
        topPanel.add(buildTextPanel(user, 'U', USER_TEXT), "0, 1, 0, 1");
        topPanel.add(buildTextPanel(pass, 'P', PASSWORD_TEXT), "2, 1, 3, 1");
    	return topPanel;
    }
    
    /**
     * Builds the UI component hosting the buttons.
     * 
     * @return See above.
     */
    private JPanel buildMainPanel()
    {
        double mainTable[][] =
                {{TableLayoutConstants.FILL, 100, 5, 100}, // columns
                {TableLayoutConstants.FILL, 30}}; // rows
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new TableLayout(mainTable));       
    	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    	mainPanel.setOpaque(false);
    	mainPanel.add(login, "1, 1, f, c");
    	mainPanel.add(cancel, "3, 1, f, c");

    	JTextPane versionInfo = UIUtilities.buildTextPane(VERSION, TEXT_COLOR);
    	Font f = versionInfo.getFont();
    	Font newFont = f.deriveFont(VERSION_FONT_SIZE);
    	versionInfo.setFont(newFont);
    	mainPanel.add(versionInfo, "0, 1, l, b");
    	mainPanel.add(buildTopPanel(), "0, 0, 3, 0");
    	return mainPanel;
    }
    
    /** 
     * Lays out the widgets and positions the window at the centre of
     * the screen.
    */
    private void buildGUI()
    {
        setIconImage(IconManager.getOMEImageIcon());  //Frame icon.
        JLabel background = new JLabel(IconManager.getLoginBackground());
        background.setBorder(BorderFactory.createEmptyBorder());
        JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
        layers.setBounds(0, 0, LOGIN_WIDTH, LOGIN_HEIGHT);
        JPanel p = buildMainPanel();
        background.setBounds(0, 0, LOGIN_WIDTH, LOGIN_HEIGHT);
        p.setBounds(0, 0, LOGIN_WIDTH, LOGIN_HEIGHT);
        
        layers.add(background, new Integer(0));
        layers.add(p, new Integer(1));
        getContentPane().add(layers);
    }
    
	/** Creates the splash screen UI. */
	SplashScreenView() 
	{
		super(TITLE);
		initFields();
		initButtons();
		buildGUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
	}

    
    /** 
     * Sets the value of the new server
     * 
     * @param s The value to set.
     */
    void setNewServer(String s)
    {
    	if (s == null || s.length() == 0) return;
    	serverText.setText(s);
    	serverText.repaint();
    }
    
}
