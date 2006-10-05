/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenView
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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies

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
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

class SplashScreenView 
	extends JFrame
{

	/** 
	 * The width of the splash screen window. 
	 * This value must be the same as the width of the splash screen image.
	 */
	private static final int		WIN_W = 404;  
		
	/** 
	 * The height of the splash screen window.
	 * This value must be the same as the height of the splash screen image.
	 */
	private static final int		WIN_H = 404;
	
	/** Absolute positioning and size of the task name. */
	private static final Rectangle	TASK_BOUNDS = 
											new Rectangle(110, 190, 200, 20);
											//TODO: use font metrics.
	
	/** Absolute positioning and size of the progress bar. */
	private static final Rectangle	PROGRESS_BOUNDS = 
											new Rectangle(95, 210, 200, 10);
		
	/** Absolute positioning and size of the user text field. */
	private static final Rectangle	USER_BOUNDS = 
											new Rectangle(149, 242, 115, 15);
											
	/** Absolute positioning and size of the password text field. */
	private static final Rectangle	PASS_BOUNDS = 
											new Rectangle(149, 259, 115, 15);
	
    /** Absolute positioning and size of the password text field. */
    private static final Rectangle  SERVER_BOUNDS = 
                                            new Rectangle(149, 276, 230, 15);
    
	/** Absolute positioning and size of the login button. */
	private static final Rectangle	LOGIN_BOUNDS = 
											new Rectangle(242, 320, 50, 20);
	
    /** Absolute positioning and size of the cancel button. */
    private static final Rectangle  CANCEL_BOUNDS = 
                                            new Rectangle(172, 320, 50, 20);
    
    /** Absolute positioning of the version label. */
    private static final Rectangle  VERSION_BOUNDS =
                                            new Rectangle(62, 347, 250, 20);
    
	/** Font for progress bar label and text fields. */
	private static final Font		FONT = 
										new Font("SansSerif", Font.PLAIN, 10);
    
	/** Font for progress bar label and text fields. */
	private static final Font		TASK_FONT = 
										new Font("SansSerif", Font.PLAIN, 8);
    
    /** Font for the version label. */
    private static final Font       VERSION_FONT = 
                                        new Font("SansSerif", 
                                                Font.PLAIN+Font.BOLD, 12);
    
    /** The font color for the version label. */
    private static final Color      VERSION_FONT_COLOR = 
                                        new Color(225, 225, 225);
    
	/** The font color for the login text fields. */
	private static final Color		FONT_COLOR = new Color(250, 100, 0);
		
	/** The font color for the login text fields. */
	private static final Color		TASK_FONT_COLOR = new Color(102, 0, 204);	
		
    /** The client's version. */
    private static final String     VERSION = "3.0_M3 ";
    
    /** The server's version. For developers' purpose only. */
    private static final String     OMERO_VERSION = " server: OMERO M3";

	/** Text field to enter the login user name. */
	JTextField          user;
	
	/** Password field to enter login password. */
	JPasswordField      pass;
	
	/** Login button. */
	JButton             login;
	
    /** Cancel button. */
    JButton             cancel;
    
	/** Displays the name of the task that is currently being executed. */
	JLabel              currentTask;
	
	/** Provides feedback on the state of the initialization process. */
	JProgressBar        progressBar;
	
    /** Label hosting the version of shoola. */
	JLabel              versionLabel;
    
    /** Box displaying the user preferred server on the current machine. */
    JComboBox           server;
    
	/** Creates the splash screen UI. */
	SplashScreenView() 
	{
		super("Open Microscopy Environment");
		initProgressDisplay();
        initBox();
		initFields();
		initButtons();
		buildGUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
	}
	
	/**
	 * Initializes the widgets for displaying feedback on the initialization
	 * state.
	 */
	private void initProgressDisplay()
	{
		currentTask = new JLabel();
		currentTask.setFont(FONT);
		currentTask.setForeground(TASK_FONT_COLOR);
		progressBar = new JProgressBar();
		progressBar.setFont(TASK_FONT);
		progressBar.setStringPainted(true);
	}
	
	/** Creates and initializes the login fields and the version label. */
	private void initFields()
	{
		user = new JTextField();
		user.setFont(FONT);
		user.setForeground(FONT_COLOR);
		user.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		pass = new JPasswordField();
		pass.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		pass.setFont(FONT);
		pass.setForeground(FONT_COLOR);
        versionLabel = new JLabel();
        versionLabel.setDoubleBuffered(false);
        String version = "$Rev$";
        Pattern p = Pattern.compile("\\d{1,9}");
        Matcher m = p.matcher(version);
        m.find();
        versionLabel.setText(VERSION+"(rev "+m.group()+")"+OMERO_VERSION);
        versionLabel.setForeground(VERSION_FONT_COLOR);
        versionLabel.setFont(VERSION_FONT);
	}
    
    /** 
     * Creates and initializes the box displaying the list of available
     * servers.
     */
    private void initBox()
    {
        server = new JComboBox(UIFactory.getServersAsArray());
        server.setFont(FONT);
        server.setForeground(FONT_COLOR);
    }
    
    /**
     * Removes border and margin for the specified button and sets the default
     * cursor to {@link Cursor#HAND_CURSOR}.
     * 
     * @param button        The button to set the default for.
     * @param rollOverIcon  The rollover icon for the specified button.
     */
    private void setButtonDefault(JButton button, Icon rollOverIcon)
    {
        //Next two statements get rid of surrounding border.
        button.setBorder(null);
        button.setMargin(null);  
        button.setRolloverIcon(rollOverIcon);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
	/** Creates and initializes the login button and the cancel button. */
	private void initButtons()
	{
		login = new JButton(IconManager.getLoginButton());
        setButtonDefault(login, IconManager.getLoginButtonOver());
        cancel = new JButton(IconManager.getCancelButton());
        setButtonDefault(cancel, IconManager.getCancelButtonOver());
	}
    
	/** 
	 * Lays out the widgets and positions the window at the centre of
	 * the screen.
	*/
	private void buildGUI()
	{
		setIconImage(IconManager.getOMEImageIcon());  //Frame icon.
		
		//Position window at center of screen and size it to splash image.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width-WIN_W)/2, (screenSize.height-WIN_H)/2,
					WIN_W, WIN_H);
		
		//Get the splash screen image.
		JLabel splash = new JLabel(IconManager.getSplashScreen());
		
		//Layer components.
		JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
		layers.add(splash, new Integer(0));
		layers.add(currentTask, new Integer(1));
		layers.add(progressBar, new Integer(1));
		layers.add(user, new Integer(1));
		layers.add(pass, new Integer(1));
        layers.add(cancel, new Integer(1));
		layers.add(login, new Integer(1));
		layers.add(versionLabel, new Integer(1));
        layers.add(server, new Integer(1));
		//Add components to content pane.
		getContentPane().setLayout(null);  //Absolute layout.
		getContentPane().add(layers);
		
		//Do layout.
		layers.setBounds(0, 0, WIN_W, WIN_H);
		splash.setBounds(0, 0, WIN_W, WIN_H);
		currentTask.setBounds(TASK_BOUNDS);
		progressBar.setBounds(PROGRESS_BOUNDS);
		user.setBounds(USER_BOUNDS);
		pass.setBounds(PASS_BOUNDS);
        server.setBounds(SERVER_BOUNDS);
        cancel.setBounds(CANCEL_BOUNDS);
		login.setBounds(LOGIN_BOUNDS);
        versionLabel.setBounds(VERSION_BOUNDS);
	}

}
