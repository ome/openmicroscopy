/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenViewTop 
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * The splash screen UI. 
 * This class is completely dummy, the logic to control the widgets is in 
 * {@link SplashScreenManager}. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class SplashScreenViewTop 
	extends JFrame
{

	/** 
	 * The height of the splash screen window.
	 * This value must be the same as the height of the splash screen image.
	 */
	static final int				WIN_H = 361;
	
	/** The gap between the two splash screens. */
	static final int				GAP = 5;
	
	/** 
	 * The width of the splash screen window. 
	 * This value must be the same as the width of the splash screen image.
	 */
	private static final int		WIN_W = 551;  
	
	/** Absolute positioning and size of the task name. */
	private static final Rectangle	TASK_BOUNDS = 
											new Rectangle(175, 295, 250, 20);
	
	/** Absolute positioning and size of the progress bar. */
	private static final Rectangle	PROGRESS_BOUNDS = 
											new Rectangle(160, 315, 250, 10);
	
	/** Font for progress bar label. */
	private static final Font	FONT = new Font("SansSerif", Font.PLAIN, 10);
    
	/** Font for progress bar label. */
	private static final Font	TASK_FONT = 
										new Font("SansSerif", Font.PLAIN, 8);
	
    
	/** Displays the name of the task that is currently being executed. */
	JLabel              currentTask;
	
	/** Provides feedback on the state of the initialization process. */
	JProgressBar        progressBar;
	
    /**
     * Initializes the widgets for displaying feedback on the initialization
     * state.
     */
    private void initProgressDisplay()
    {
        currentTask = new JLabel();
        currentTask.setFont(FONT);
        currentTask.setForeground(SplashScreenView.TEXT_COLOR);
        progressBar = new JProgressBar();
        progressBar.setFont(TASK_FONT);
        progressBar.setStringPainted(true);
    }
    
    /** Builds and lays out the UI. */
	private void buildGUI()
	{
		 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 int totalHeight = WIN_H+SplashScreenView.LOGIN_HEIGHT+GAP;
	     setBounds((screenSize.width-WIN_W)/2, 
	    		 	(screenSize.height-totalHeight)/2, WIN_W, WIN_H);
	     JLabel splash = new JLabel(IconManager.getSplashScreen());
	     JLayeredPane layers = new JLayeredPane();  
	     layers.add(splash, new Integer(0));
	     layers.add(currentTask, new Integer(1));
	     layers.add(progressBar, new Integer(1));
	     getContentPane().add(layers);
	     layers.setBounds(0, 0, WIN_W, WIN_H);
	     splash.setBounds(0, 0, WIN_W, WIN_H);
	     currentTask.setBounds(TASK_BOUNDS);
	     progressBar.setBounds(PROGRESS_BOUNDS);
	}
	
	/** Creates the splash screen UI. */
	SplashScreenViewTop() 
	{
		super(SplashScreenView.TITLE);
		initProgressDisplay();
		buildGUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
	}
	
}
