/*
 * org.openmicroscopy.shoola.agents.viewer.movie.ToolBarManager
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

package org.openmicroscopy.shoola.agents.viewer.movie;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
public class ToolBarManager
	implements ActionListener, FocusListener, ChangeListener
{
	
	/** Action command ID to be used with the play button. */
	private static final int   					PLAY_CMD = 0;
	
	/** Action command ID to be used with the stop button. */
	private static final int   					STOP_CMD = 1;
	
	/** Action command ID to be used with the rewind button. */
	private static final int   					REWIND_CMD = 2;
	
	/** 
	 * Action command ID to be used to sync JSpinner 
	 * and the text field editor.
	 */
	private static final int   					EDITOR_CMD = 3;
	
	/** Action command ID to be used with the saveAs button. */
	private static final int   					SAVEAS_CMD = 4;
	
	/** Action command ID to be used with the pause button. */
	private static final int   					PAUSE_CMD = 5;
	
	/** Action command ID to be used with the viewer3D button. */
	private static final int   					MOVIE_START_CMD = 6;
	
	/** Action command ID to be used with the viewer3D button. */
	private static final int   					MOVIE_END_CMD = 7;
    
    /** Action command ID to be used with the viewer3D button. */
    private static final int                    PLAY_END_CMD = 8;
    
    /** Action command ID to be used with the viewer3D button. */
    private static final int                    PLAY_START_CMD = 9;
	
    private JButton                             play, stop, rewind, 
                                                pause, playStart,
                                                playEnd;
    
	private int									maxValue, curR;
	
	private int									curMovieStart, curMovieEnd;
	
	private PlayerManager						control;
	
	private ToolBar								view;
	
	private Registry							registry;

	public ToolBarManager(PlayerManager control, Registry registry, 
						ToolBar view, int max)
	{
		this.control = control;
		this.registry = registry;
		this.view = view;
		maxValue = max;
		curR = PlayerManager.FPS_INIT;
		curMovieStart = 0;
		curMovieEnd = maxValue;
	}
	
	int getCurMovieStart() { return curMovieStart; }
	
	int getCurMovieEnd() { return curMovieEnd; }
	
	/** Attach the listeners. */
	void attachListeners()
	{
		//textfield
		JTextField editor = view.getEditor(), start = view.getMovieStart(),
					end = view.getMovieEnd();
		editor.addActionListener(this);
		editor.setActionCommand(""+EDITOR_CMD);
		
		start.setActionCommand(""+MOVIE_START_CMD);  
		start.addActionListener(this);
		start.addFocusListener(this);
		end.setActionCommand(""+MOVIE_END_CMD);  
		end.addActionListener(this);
		end.addFocusListener(this);
		//button
		play = view.getPlay();
        stop = view.getStop();
		rewind = view.getRewind(); 
		pause = view.getPause();
        playStart = view.getPlayStart();
        //saveAs = view.getSaveAs();
        playEnd = view.getPlayEnd();
		play.setActionCommand(""+PLAY_CMD);
		play.addActionListener(this);
		stop.setActionCommand(""+STOP_CMD); 
		stop.addActionListener(this);
		rewind.setActionCommand(""+REWIND_CMD);
		rewind.addActionListener(this);
		//saveAs.setActionCommand(""+SAVEAS_CMD);
		//saveAs.addActionListener(this);
		pause.setActionCommand(""+PAUSE_CMD);
		pause.addActionListener(this);
        playStart.setActionCommand(""+PLAY_START_CMD);
        playStart.addActionListener(this);
        playEnd.setActionCommand(""+PLAY_END_CMD);
        playEnd.addActionListener(this);
		//spinner
		view.getFPS().addChangeListener(this);
	}
	
	public void setLabel(int v)
    { 
        view.getLabel().setText("value: "+v+"/"+maxValue);
    }
	
	/** 
	 * Synchronizes the spinner, and the text editor.
	 * 
	 * @param val	The value that the slider, text field and the current 
	 * 				Scale will be set to.
	 */
	private void synchSpinner(int val)
	{ 
		curR = val;
		view.getFPS().setValue(new Integer(val));  
		view.getEditor().setText(""+val);
		control.setTimerDelay(curR); 
	} 
	
	/** 
	 * Handles the action event fired by the editor text field when the 
	 * user enters some text. If the entered text can be  converted to a valid 
	 * blacklevel, the {@link #synch(int) synch} method is invoked in order to 
	 * set all elements to the new blacklevel value. 
	 * If that text doesn't evaluate to a valid blacklevel, then we simply 
	 * suggest the user to enter a valid one.
	 */     
	private void editorActionHandler()
	{
		control.stopTimer();	//freeze
		boolean valid = false;
		int val = PlayerManager.FPS_MIN;
		try {
			val = Integer.parseInt(view.getEditor().getText());
			if (PlayerManager.FPS_MIN <= val && val <= maxValue) {
				valid = true;
			} else if (val < PlayerManager.FPS_MIN) {
				val = PlayerManager.FPS_MIN;
				valid = true;
			} else if (val > maxValue) {
				val = maxValue;
				valid = true;
			}
		} catch(NumberFormatException nfe) {}
		if (valid) synchSpinner(val);  
		else {
			view.getEditor().selectAll();
			UserNotifier un = registry.getUserNotifier();
			un.notifyInfo("Invalid value", "Please enter a value " +
                    "between "+PlayerManager.FPS_MIN+" and "+maxValue);
		}
	} 
	
	/** 
	 * Handles the action event fired by the starting text field when the user 
	 * enters some text. 
	 * If that text doesn't evaluate to a valid timepoint, then we simply 
	 * suggest the user to enter a valid one.
	 */
	private void movieStartActionHandler()
	{
		control.stopTimer();	//freeze
		boolean valid = false;
		int val = 0;
		int valEnd = maxValue;
		try {
			val = Integer.parseInt(view.getMovieStart().getText());
			valEnd = Integer.parseInt(view.getMovieEnd().getText());
			if (0 <= val && val < valEnd) valid = true;
		} catch(NumberFormatException nfe) {}
		if (!valid) {
			int v = valEnd-1; 
			view.getMovieStart().selectAll();
			UserNotifier un = registry.getUserNotifier();
			un.notifyInfo("Invalid start point", 
				"Please enter a value between 0 and "+v);
		} else {
			curMovieStart = val;
			control.setStartMovie(curMovieStart);
		} 
	}
	
	/** 
	 * Handles the action event fired by the end text field when the user 
	 * enters some text. 
	 * If that text doesn't evaluate to a valid timepoint, then we simply 
	 * suggest the user to enter a valid one.
	 */
	private void movieEndActionHandler()
	{
		control.stopTimer();	//freeze
		boolean valid = false;
		int val = 0;
		int valStart = 0;
		try {
			val = Integer.parseInt(view.getMovieEnd().getText());
			valStart = Integer.parseInt(view.getMovieStart().getText());
			if (valStart < val && val <= maxValue) valid = true;
		} catch(NumberFormatException nfe) {}
		if (!valid) {
			view.getMovieEnd().selectAll();
			UserNotifier un = registry.getUserNotifier();
			int v = valStart+1;
			un.notifyInfo("Invalid end point", 
				"Please enter a timepoint between "+ v+" and "+maxValue);
		} else {
			curMovieEnd = val;
			control.setEndMovie(curMovieEnd);
		} 
	}
	
	/** Handle events fired byt text field and buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) {
				case EDITOR_CMD:
					editorActionHandler(); break;
				case MOVIE_START_CMD:
					movieStartActionHandler(); break;
				case MOVIE_END_CMD:
					movieEndActionHandler(); break;	
				case SAVEAS_CMD:
				    control.saveAs(); break;
				case PLAY_CMD:
                    handlePlay(); break;
				case REWIND_CMD:	
                    handleRewind(); break;
				case PAUSE_CMD:	
                    handlePause(); break;
				case STOP_CMD:
                    handleStop(); break;
                case PLAY_END_CMD:
                    handlePlayEnd(); break;
                case PLAY_START_CMD:
                    handlePlayStart(); break;
			}
		} catch(NumberFormatException nfe) { 
			throw new Error("Invalid Action ID "+index, nfe); 
		}
	}

    /** Handle the play movie action. */
    private void handlePlay()
    {
        paintBorder();
        play.setBorderPainted(true);
        control.play();
    }
    
    private void handleStop()
    {
        
        paintBorder();
        stop.setBorderPainted(true);
        control.stop();
    }
    
    private void handleRewind()
    {
        paintBorder();
        rewind.setBorderPainted(true);
        control.rewind();
    }
    
    private void handlePlayStart()
    {
        paintBorder();
        playStart.setBorderPainted(true);
        control.playStart();
    }
    
    private void handlePlayEnd()
    {
        paintBorder();
        playEnd.setBorderPainted(true);
        control.playEnd();
    }
    
    private void handlePause()
    {
        paintBorder();
        pause.setBorderPainted(true);
        control.pause();
    }
    
    private void paintBorder() 
    {
        play.setBorderPainted(false);
        pause.setBorderPainted(false);
        stop.setBorderPainted(false);
        rewind.setBorderPainted(false);
        playStart.setBorderPainted(false);
        playEnd.setBorderPainted(false);
    }
    
	/** Handle events fired by the spinner. */
	public void stateChanged(ChangeEvent e)
	{
		control.stopTimer();	//freeze
		int v = ((Integer) view.getFPS().getValue()).intValue();
		view.getEditor().setText(""+v);
		if (v != curR) synchSpinner(v);
	}
	
	/** 
	 * Handles the lost of focus on the timepoint text field.
	 * If focus is lost while editing, then we don't consider the text 
	 * currently displayed in the text field and we reset it to the current
	 * timepoint.
	 */
	public void focusLost(FocusEvent e)
	{
		String edit = view.getEditor().getText(), ed = ""+curR;
		String startVal = view.getMovieStart().getText(), 
				start = ""+curMovieStart;
		String endVal = view.getMovieEnd().getText(), end = ""+curMovieEnd;
		if (edit == null || !edit.equals(ed)) view.getEditor().setText(ed);
		if (startVal == null || !startVal.equals(start))
			view.getMovieStart().setText(start);		
		if (endVal == null || !endVal.equals(end)) 
			view.getMovieEnd().setText(end);
	}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void focusGained(FocusEvent e) {}
	
}
