/*
 * org.openmicroscopy.shoola.env.ui.TopWindowManager
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Controller and model of a top window.
 * <p>This component is normally used with a window that is linked to the
 * {@link TaskBar} by means of one or more display-trigger buttons.  It keeps
 * track of the display state of its window and reacts to mouse clicks on the
 * display buttons accordingly.</p>
 * <p>The display state of the window is broken down as follows:
 *  <ul>
 *   <li><i>Unlinked</i>: The window is not connected to any native screen
 * 		resource.</li>
 *   <li><i>Displayable</i>: The window is connected to a native peer, but
 * 		is not showing on screen.</li>
 *   <li><i>On Screen</i>: The window has been painted on screen.  This doesn't
 * 		necessarily imply a visible window.  It may be hidden behind other
 * 		windows or may be iconified.</li>
 *  </ul>
 * </p>
 * <p>When the window is off-screen (either in the <i>Unlinked</i> or 
 * <i>Displayable</i> state), this controller reacts to a call to set the window
 * visible (<code>window.setVisible(true)</code>) or a click on any of the 
 * display buttons by showing the window at the center of the screen, possibly
 * on top of all other windows and making it the active window &#151; this
 * attempt might just partially succeed depending on the OS window manager.  If
 * one of those events occur while the window is in the <i>On Screen</i> state,
 * a similar action is performed, but the window is restored to the previous 
 * screen location, which is not going to be, generally speaking, the
 * center of the screen &#151; for example, if the window is moved from the
 * center of the screen to another location and then iconified, it'll be
 * restored to that location.</p>
 * <p>If the window is in either the <i>On Screen</i> or <i>Displayable</i>
 * state and its <code>dispose</code> method is invoked, then this controller
 * will set the window's state to <i>Unlinked</i>.  Notice that this could
 * happen implicitely, for example if the window's default close operation is
 * set to {@link JFrame#DISPOSE_ON_CLOSE}.</p>
 * <p>Finally, while in the <i>On Screen</i> state, any event that causes the
 * window to be hidden (a <code>window.setVisible(false)</code> call or a click
 * on the system-provided window-close button) results in a transition to the
 * <i>Displayble</i> state.</p>    
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
public class TopWindowManager 
{

	/** Identifies the <i>Unlinked</i> state. */
	public static final int		UNLINKED = 1;
	
	/** Identifies the <i>Displayable</i> state. */
	public static final int		DISPLAYABLE = 2;
	
	/** Identifies the <i>On Screen</i> state. */
	public static final int		ON_SCREEN = 3;


	/** The managed window. */
	private ManageableTopWindow	window;
	
	/**
	 * The set of buttons that have to trigger a Display event whenever clicked.
	 * If there are no such buttons, then this array will be <code>0</code>-
	 * length.
	 */
	private AbstractButton[]	displayButtons;
	
	/** Holds the current state of the managed {@link #window}. */
	private int					state;

	/**
	 * Finds out what is the state of the {@link #window} at the time
	 * that was passed to this class' constructor and sets {@link #state}
	 * accordingly.
	 */
	private void setInitialState()
	{
		if (window.isDisplayable()) 
			state = (window.isShowing() ? ON_SCREEN : DISPLAYABLE);
		else 
			state = UNLINKED;
	}
	
	/**
	 * Registers event handlers to react to Display, Hide, and Dispose events
	 * fired by the {@link #window}.
	 */
	private void attachWindowListeners()
	{
		WindowAdapter wa = new WindowAdapter() {
			public void windowClosed(WindowEvent we) { handleDispose(); }
			public void windowClosing(WindowEvent we) { handleHide(); }
		};
		window.addWindowListener(wa);
		ComponentAdapter ca = new ComponentAdapter() {
			public void componentHidden(ComponentEvent ce) { handleHide(); }
			public void componentShown(ComponentEvent ce) { handleDisplay(); }
		};
		window.addComponentListener(ca);
	}

	/**
	 * Registers event handlers to react to Display events fired by the 
	 * {@link #displayButtons}.
	 */	
	private void attachButtonsListeners()
	{
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) { handleDisplay(); }
		};
		for (int i = 0; i < displayButtons.length; ++i)
			displayButtons[i].addActionListener(al);
	}
	
	/**
	 * Handles the <i>Display</i> event.
	 * A <i>Display</i> event is caused by a call to set the {@link #window} 
	 * visible (<code>window.setVisible(true)</code>) or a click on any of the 
	 * {@link #displayButtons}.
	 */
	private void handleDisplay()
	{
		if (state == ON_SCREEN)	toForeground();
		else {  //DISPLAYABLE or UNLINKED.
			window.preHandleDisplay(this); 
		
		}
	}
	
	/** 
	 * 
	 * Continues handling of the display - does the actual work after the window 
	 * says that it is ready. should be called via a callback from
	 * (<code>window.preHandleDisplay(this)</code>)
	 *
	 */
	public void continueHandleDisplay() {
		setOnScreen();
		toForeground();
		window.postHandleDisplay();
		state = ON_SCREEN;
		
		//NOTE: setOnScreen() shows the window on screen, which will
		//eventually cause another call to componentShown() and, in turn,
		//another call to this method.  However, the state is now ON_SCREEN
		//so we just invoke toForeground().  This could be avoided if we
		//kept track of the fg/bg state of the window as well, but it would
		//make the implementation more complex.  As an extra call to
		//toForeground() is harmless, we favour an easier implementation
		//over logic soundness.
	}
	
	/**
	 * Handle the <i>Hide</i> event.
	 * A <i>Hide</i> event is caused by a <code>window.setVisible(false)</code>
	 * call or a click on the system-provided window-close button.
	 */
	private void handleHide()
	{
		if (state == ON_SCREEN)	state = DISPLAYABLE;
		//Do nothing if DISPLAYABLE or UNLINKED. 
		
		//NOTE: This method will be invoked twice if the action associated to
		//the close button of the window is to hide the window -- this happens
		//b/c hanldeHide() is called in both windowClosing() and
		//componentHidden().  However, the same considerations made in the note
		//to handleDisplay() apply.
	}
	
	/**
	 * Handle the <i>Dispose</i> event.
	 * This is caused by a call to the window's <code>dispose</code> method.
	 */
	private void handleDispose()
	{
		state = UNLINKED; 
	}
	
	/**
	 * Packs and shows the {@link #window} at the center of the screen.
	 */
	private void setOnScreen()
	{
		window.pack();
		UIUtilities.centerAndShow(window);
	}
	
	/**
	 * Attempts to bring the {@link #window} on top of all other windows and
	 * make it the active window.
	 * This attempt might just partially succeed depending on the OS window
	 * manager.
	 */
	private void toForeground()
	{
		window.setExtendedState(JFrame.NORMAL);  //Deiconify if iconified.
		window.toFront();
	}
	
	/**
	 * Creates a new instance to manage the specified window.
	 * 
	 * @param window	The window to manage.
	 * @param displayButtons	The set of buttons that have to trigger a
	 * 							Display event whenever clicked.  Pass  
	 * 							<code>null</code> if there are no such buttons.
	 */
	public TopWindowManager(ManageableTopWindow window,
			AbstractButton[]	displayButtons) 
	{
		if (window == null)	throw new NullPointerException("No window.");
		this.window = window;
		if (displayButtons != null) {
			ArrayList actualButtons = new ArrayList(displayButtons.length);
			for (int i = 0; i < displayButtons.length; ++i)
				if (displayButtons[i] != null)
					actualButtons.add(displayButtons[i]);
			this.displayButtons = new AbstractButton[actualButtons.size()];
			actualButtons.toArray(this.displayButtons);		
		} else {
			this.displayButtons = new AbstractButton[0];
		}
		setInitialState();
		attachWindowListeners();
		attachButtonsListeners();
	}
	
	/**
	 * Returns the current state of the managed window.
	 * 
	 * @return One of the state identifiers defined by the static fields of
	 * 			this class.
	 */
	public int getState()
	{
		return state;
	}

}
