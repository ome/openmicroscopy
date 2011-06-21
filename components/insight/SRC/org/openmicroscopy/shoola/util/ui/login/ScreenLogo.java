/*
 * org.openmicroscopy.shoola.util.ui.login.ScreenLogo 
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * The frame hosting the logo.
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
public class ScreenLogo 
	extends JFrame
{
	
	/** Bounds property indicating this window is moved to the front. */
	public static final String			MOVE_FRONT_PROPERTY = "moveFront";
	
	/** 
	 * The gap between the two splash windows i.e. <code>Screen Logo</code>
	 * and <code>Screen Login</code>.
	 */
	private static final int			GAP = 5;
	
	/** Indent value for text. */
	private static final int			TEXT_INDENT = 15;
	
	/** The layered hosting the various UI components. */
	private JLayeredPane layers;
	
	/** Displays the name of the task that is currently being executed. */
	private JLabel			currentTask;
	
	/** Provides feedback on the state of the initialization process. */
	private JProgressBar	progressBar;
	
	/** Initializes the UI components composing the display. */
	private void initComponents()
	{
		currentTask = new JLabel();
		Font newFont = currentTask.getFont().deriveFont(8);
		currentTask.setFont(newFont);
		currentTask.setForeground(ScreenLogin.TEXT_COLOR);
		progressBar = new JProgressBar();
		progressBar.setStringPainted(false);
		progressBar.setFont(newFont);
	}
	
	/**
	 * Builds and lays out the UI.
	 * 
	 * @param logo The frame's background logo.
	 */
	private void buildGUI(Icon logo)
	{
		JLabel splash = new JLabel(logo);
		layers = new JLayeredPane(); 
		layers.add(splash, Integer.valueOf(0));
		getContentPane().add(layers);
		int width = logo.getIconWidth();
		int height = logo.getIconHeight();
		layers.setBounds(0, 0, width, height);
		splash.setBounds(0, 0, width, height);
		int h = progressBar.getFontMetrics(progressBar.getFont()).getHeight();
		currentTask.setBounds(TEXT_INDENT, height-3*h, width, h);
		progressBar.setBounds(0, height-2*h, width, h);
		
		addToLayer(currentTask);
		addToLayer(progressBar);
	}
	
	/** 
	 * Sets the default for the window. 
	 * 
	 * @param frameIcon The icon associated to the frame.
	 */
	private void setProperties(Image frameIcon)
	{
		setIconImage(frameIcon);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		toFront();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param title 	The title of the frame.
	 * @param logo		The Frame's background logo. 
	 * 					Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLogo(String title, Icon logo, Image frameIcon)
	{
		super(title);
		if (logo == null)
			throw new NullPointerException("No Frame icon.");
		initComponents();
		Dimension d = new Dimension(logo.getIconWidth(), logo.getIconHeight());
		setSize(d);
		setPreferredSize(d);
		buildGUI(logo);
		setProperties(frameIcon);
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Fires a property to move the window to the front.
			 * @see MouseListener#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				firePropertyChange(MOVE_FRONT_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		});
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param logo		The Frame's logo. Mustn't be <code>null</code>.
	 * @param frameIcon The image icon for the window.
	 */
	public ScreenLogo(Icon logo, Image frameIcon)
	{
		this(null, logo, frameIcon);
	}
	
	/**
	 * Sets the total number of initialization tasks that have to be
     * performed.
     * 
	 * @param maxTask The total number of tasks.
	 */
	public void initProgressBar(int maxTask)
	{
		progressBar.setMinimum(0);
		progressBar.setMaximum(maxTask);
		progressBar.setValue(0);
	}
	
	/**
	 * Adds the specified component to the layeredPane.
	 * 
	 * @param c The component to add.
	 */
	public void addToLayer(JComponent c)
	{
		if (c == null) return;
		layers.add(c, Integer.valueOf(1));
	}
	
	/**
	 * Returns the width of the frame and height of the icon plus the gap.
	 * 
	 * @return See above.
	 * @see #getSize()
	 */
	public Dimension getExtendedSize()
	{
		Dimension d = getSize();
		return new Dimension(d.width, d.height+GAP);
	}
	
	/** Closes and disposes. */
    public void close()
    {
    	setVisible(false);
    	dispose();
    }
	
    /**
     * Sets the value of the progress bar and the status message.
     * 
     * @param value	The status message.
     * @param perc	The value to set.
     */
    public void setStatus(String value, int perc)
    {
    	currentTask.setText(value);
    	progressBar.setValue(perc);
    }
    
    /** 
     * Shows or hides the progress bar and the tasks label. 
     * 
     * @param b Pass <code>true</code> to show, <code>false</code> to hide.
     */
    public void setStatusVisible(boolean b)
    {
    	currentTask.setVisible(b);
    	progressBar.setVisible(b);
    }
    
}
