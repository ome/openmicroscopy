/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.ControlsPaneMng
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

package org.openmicroscopy.shoola.agents.viewer.movie.pane;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.movie.Player;
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
class ControlsPaneMng
    implements ActionListener, FocusListener, ChangeListener
{

    /** Action command ID to play the movie. */
    private static final int        PLAY_CMD = 0;
    
    /** Action command ID to stop playing the movie. */
    private static final int        PAUSE_CMD = 1;
    
    /** Action command ID to stop playing the movie. */
    private static final int        STOP_CMD = 2;
    
    /** 
     * Action command ID to sync JSpinner and the text field editor.
     */
    private static final int        EDITOR_CMD = 3;
    
    /** Action command ID to set the movieType. */
    private static final int        MOVIE_TYPE_CMD = 4;
    
    private ControlsPane            view;
    
    private PlayerUIMng             playerUIMng;
    
    private Registry                registry;
    
    /** Maximum value for the JSpinner and the current rate. */
    private int                     maxValue, curRate;
    
    ControlsPaneMng(ControlsPane view, PlayerUIMng playerUIMng, Registry reg, 
                    int max)
    {
        this.view = view;
        maxValue = max;
        this.playerUIMng = playerUIMng;
        registry = reg;
        attachListeners();
    }
    
    /** Attach the listeners. */
    private void attachListeners()
    {
        JTextField editor = view.getEditor();
        editor.addActionListener(this);
        editor.setActionCommand(""+EDITOR_CMD);
        
        //JButton
        attachButtonListener(view.getPlay(), PLAY_CMD);
        attachButtonListener(view.getPause(), PAUSE_CMD);
        attachButtonListener(view.getStop(), STOP_CMD);
        
        //JComboBox
        JComboBox box = view.getMovieType();
        box.setActionCommand(""+MOVIE_TYPE_CMD); 
        box.addActionListener(this);
        
        //JSpinner
        view.getFPS().addChangeListener(this);
    }
    
    /** Attach listener and setActionCommand to a JButton.*/
    private void attachButtonListener(JButton button, int id)
    {
        button.setActionCommand(""+id);
        button.addActionListener(this);  
    }
    
    /** Handle events fired by JButtons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case PLAY_CMD:
                    handlePlay(); break;
                case PAUSE_CMD:
                    handlePause(); break;    
                case STOP_CMD:
                    handleStop(); break;
                case EDITOR_CMD:
                    editorActionHandler(); break; 
                case MOVIE_TYPE_CMD:
                    JComboBox box = (JComboBox) e.getSource();
                    playerUIMng.setMovieType(box.getSelectedIndex()); break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        } 
    }
    
    /** Handle events fired by the spinner. */
    public void stateChanged(ChangeEvent e)
    {
        int v = ((Integer) view.getFPS().getValue()).intValue();
        view.getEditor().setText(""+v);
        if (v != curRate) synchSpinner(v);
    }
    
    /** 
     * Handles the lost of focus on the timepoint text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     */
    public void focusLost(FocusEvent e)
    {
        String edit = view.getEditor().getText(), ed = ""+curRate;
        if (edit == null || !edit.equals(ed)) view.getEditor().setText(ed);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */ 
    public void focusGained(FocusEvent e) {}

    private void handlePlay()
    {
        view.getPlay().setBorderPainted(true);
        playerUIMng.play();
    }
    
    private void handlePause()
    {
        view.getPlay().setBorderPainted(false);
        playerUIMng.pause(); 
    }
    
    private void handleStop()
    {
        view.getPlay().setBorderPainted(false);
        playerUIMng.stop();
    }
    
    /** 
     * Synchronizes the spinner, and the text editor.
     * 
     * @param val   The value that the slider, text field and the current 
     *              Scale will be set to.
     */
    private void synchSpinner(int val)
    { 
        curRate = val;
        view.getFPS().setValue(new Integer(val));  
        view.getEditor().setText(""+val);
        playerUIMng.setTimerDelay(curRate); 
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
        //playerUIMng.stopTimer();    //freeze
        boolean valid = false;
        int val = Player.FPS_MIN;
        try {
            val = Integer.parseInt(view.getEditor().getText());
            if (Player.FPS_MIN <= val && val <= maxValue) {
                valid = true;
            } else if (val < Player.FPS_MIN) {
                val = Player.FPS_MIN;
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
                    "between "+Player.FPS_MIN+" and "+maxValue);
        }
    } 
}
