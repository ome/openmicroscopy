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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

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
											new Rectangle(110, 170, 200, 20);
											//TODO: use font metrics.
	
	/** Absolute positioning and size of the progress bar. */
	private static final Rectangle	PROGRESS_BOUNDS = 
											new Rectangle(95, 190, 200, 10);
		
	/** Absolute positioning and size of the user text field. */
	private static final Rectangle	USER_BOUNDS = 
											new Rectangle(173, 283, 115, 15);
											
	/** Absolute positioning and size of the password text field. */
	private static final Rectangle	PASS_BOUNDS = 
											new Rectangle(173, 300, 115, 15);
											
	/** Absolute positioning and size of the login button. */
	private static final Rectangle	LOGIN_BOUNDS = 
											new Rectangle(242, 323, 50, 20);
	
	/** Font for progress bar label and text fields. */
	private static final Font		FONT = 
										new Font("SansSerif", Font.PLAIN, 10);
	/** Font for progress bar label and text fields. */
	private static final Font		FONT_TASK = 
										new Font("SansSerif", Font.PLAIN, 8);										
	/** The font color for the login text fields. */
	private static final Color		FONT_COLOR = new Color(250, 100, 0);
		
	/** The font color for the login text fields. */
	private static final Color		FONT_COLOR_TASK = new Color(102, 0, 204);	
		
	/** Text field to enter the login user name. */
	JTextField      user;
	
	/** Password field to enter login password. */
	JPasswordField  pass;
	
	/** Login button. */
	JButton         login;
	
	/** Displays the name of the task that is currently being executed. */
	JLabel			currentTask;
	
	/** Provides feedback on the state of the initialization process. */
	JProgressBar	progressBar;
	
	/** 
	 * The image icons needed to build the GUI.
	 * The first icon is the OME logo, the second is the splash screen 
	 * background image, the third is the login button and the forth is the
	 * login button when the mouse rolls over the button.
	*/
	private Icon[]  images;

	/** 
	 * Creates the splash screen UI.
	 */
	SplashScreenView() 
	{
		super("Open Microscopy Environment");
		loadImages();
		initProgressDisplay(); 
		initLoginFields();
		initLoginButton();
		buildGUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
	}
	
	/**
	 * Fills up the <code>images</code> array.
	 */
	private void loadImages()
	{
		images = new Icon[4];
		//Paths relative to UIFactory.
		images[0] = UIFactory.createIcon("graphx/OME16.png");
		images[1] = UIFactory.createIcon("graphx/splash.jpg");
		images[2] = UIFactory.createIcon("graphx/login_out.jpg");
		images[3] = UIFactory.createIcon("graphx/login_over.jpg");
		//TODO: handle nulls (image not loaded).
	}
	
	/**
	 * Initializes the widgets for displaying feedback on the initialization
	 * state.
	 */
	private void initProgressDisplay()
	{
		currentTask = new JLabel();
		currentTask.setFont(FONT);
		//currentTask.setForeground(FONT_COLOR);
		currentTask.setForeground(FONT_COLOR_TASK);
		progressBar = new JProgressBar();
		progressBar.setFont(FONT_TASK);
		progressBar.setStringPainted(true);
	}
	
	/** 
	 * Creates and initializes the login fields.
	 */
	private void initLoginFields()
	{
		user = new JTextField();
		user.setFont(FONT);
		user.setForeground(FONT_COLOR);
		pass = new JPasswordField();
		pass.setFont(FONT);
		pass.setForeground(FONT_COLOR);
	}
    
	/** 
	 * Creates and initializes the login button.
	 */
	private void initLoginButton()
	{
		login = new JButton(images[2]);
		login.setRolloverIcon(images[3]);
		//Next two statements get rid of surrounding border.
		login.setBorder(null);
		login.setMargin(null);  
		login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
    
	/** 
	 * Lays out the widgets and positions the window at the centre of
	 * the screen.
	*/
	private void buildGUI()
	{
		setIconImage(((ImageIcon)images[0]).getImage());  //Frame icon.
		
		//Position window at center of screen and size it to splash image.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width-WIN_W)/2, (screenSize.height-WIN_H)/2,
					WIN_W, WIN_H);
		
		//Get the splash screen image.
		JLabel  splash = new JLabel(images[1]);
		
		//Layer components.
		JLayeredPane layers = new JLayeredPane();  //Default is absolute layout.
		layers.add(splash, new Integer(0));
		layers.add(currentTask, new Integer(1));
		layers.add(progressBar, new Integer(1));
		layers.add(user, new Integer(1));
		layers.add(pass, new Integer(1));
		layers.add(login, new Integer(1));
		
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
		login.setBounds(LOGIN_BOUNDS);
	}

}
