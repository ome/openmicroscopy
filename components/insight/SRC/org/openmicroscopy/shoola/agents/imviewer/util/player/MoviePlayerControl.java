/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerControl
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

package org.openmicroscopy.shoola.agents.imviewer.util.player;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * The movie player controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class MoviePlayerControl
    implements ActionListener, ChangeListener, FocusListener, 
            PropertyChangeListener
{

	/** Identifies the entering of a starting timepoint. */
    private static final int    START_T = 0;
    
    /** Identifies the entering of a ending timepoint. */
    private static final int    END_T = 1;
    
    /** Identifies the entering of a starting z-section. */
    private static final int    START_Z = 2;
    
    /** Identifies the entering of a ending z-section. */
    private static final int    END_Z = 3;
    
    /** Identifies the play movie action. */
    private static final int    PLAY_CMD = 4;
    
    /** Identifies the play pause action. */
    private static final int    PAUSE_CMD = 5;
    
    /** Identifies the stop movie action. */
    private static final int    STOP_CMD = 6;
    
    /** Identifies the change of movie type. */
    private static final int    MOVIE_TYPE_CMD = 7;
    
    /** 
     * Action command ID to sync JSpinner and the text field editor.
     */
    private static final int    EDITOR_CMD = 8;
    
    /** Indicates that the movie is played across z-sections. */
    private static final int    ACROSS_Z_CMD = 9;
    
    /** Indicates that the movie is played across timepoints. */
    private static final int    ACROSS_T_CMD = 10;
    
    /** Indicates that the movie is played across z-sections and timepoints. */
    private static final int    ACROSS_ZT_CMD = 11;
    
    /** Indicates that a new start/end z-section has been entered. */
    private static final int    TYPE_Z = 200;
    
    /** Indicates that a new start/end timepoint has been entered. */
    private static final int    TYPE_T = 201;
    
    /** Reference to the View. */
    private MoviePlayer     model;

    /** Reference to the View. */
    private MoviePlayerUI   view;
    
    /** Adds an {@link ActionListener} to an {@link AbstractButton}. 
     * 
     * @param button    The component to attach the listener to.
     * @param id        The action command ID.
     */
    private void attachButtonListener(AbstractButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Adds listeners to a {@link JTextField}. 
     * 
     * @param field The component to attach the listeners to.
     * @param id    The action command ID.
     */
    private void attachFieldListeners(JTextField field, int id)
    {
        field.setActionCommand(""+id);  
        field.addActionListener(this);
        field.addFocusListener(this);
    }
    
    /** Adds listeners to the UI components. */
    private void initListeners()
    {
        JTextField editor = view.editor;
        editor.addKeyListener(new KeyAdapter() {
        	
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					editorActionHandler();
			}
		});
        //JButton
        attachButtonListener(view.play, PLAY_CMD);
        attachButtonListener(view.pause, PAUSE_CMD);
        attachButtonListener(view.stop, STOP_CMD);
        //JComboBox
        JComboBox box = view.movieTypes;
        box.setActionCommand(""+MOVIE_TYPE_CMD); 
        box.addActionListener(this);
        //JSpinner
        view.fps.addChangeListener(this);
        //MoviePane
        attachFieldListeners(view.startT, START_T);
        attachFieldListeners(view.endT, END_T);
        attachFieldListeners(view.startZ, START_Z);
        attachFieldListeners(view.endZ, END_Z);
        attachButtonListener(view.acrossZ, ACROSS_Z_CMD);
        attachButtonListener(view.acrossT, ACROSS_T_CMD);
        //attachButtonListener(view.acrossZT, ACROSS_ZT_CMD);
        view.tSlider.addPropertyChangeListener(this);
        view.zSlider.addPropertyChangeListener(this);
    }

    /**
     * Sets the start value depending on the specified type.
     * 
     * @param start The start value either z-section or timepoint depending on 
     *              the type.
     * @param end   The end value either z-section or timepoint depending on 
     *              the type.
     * @param type  One of the following constants {@link #TYPE_T} or
     *              {@link #TYPE_Z}.
     */
    private void movieStartActionHandler(JTextField start, JTextField end,
                                        int type)
    {
        boolean valid = false;
        int val = 1;
        int valEnd = 1;
        if (type == TYPE_T) valEnd = model.getMaxT();
        else if (type == TYPE_Z) valEnd = model.getMaxZ();
        if (valEnd < 1) valEnd = 1;
        try {
            val = Integer.parseInt(start.getText());
            valEnd = Integer.parseInt(end.getText());
            if (1 <= val && val < valEnd) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            int v = valEnd+1; 
            start.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid start point", 
                "Please enter a value between 1 and "+v);
            return;
        }
        if (type == TYPE_T) {
            model.setStartT(val);
            view.setStartT(val);
        } else if (type == TYPE_Z) {
            model.setStartZ(val-1);
            view.setStartZ(val-1);
        }
    }
    
    /**
     * Sets the end value depending on the specified type.
     * 
     * @param start The start value either z-section or timepoint depending on 
     *              the type.
     * @param end   The end value either z-section or timepoint depending on 
     *              the type.
     * @param type  One of the following constants {@link #TYPE_T} or
     *              {@link #TYPE_Z}.
     */
    private void movieEndActionHandler(JTextField start, JTextField end,
                                        int type)
    {
        boolean valid = false;
        int val = 1;
        int valStart = 1;
        int max = 1;
        if (type == TYPE_T) max = model.getMaxT();
        else if (type == TYPE_Z) max = model.getMaxZ();
        if (max < 1) max = 1;
        try {
            val = Integer.parseInt(end.getText())-1;
            valStart = Integer.parseInt(start.getText())-1;
            if (valStart < val && val <= max) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            end.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            int v = valStart+1;
            un.notifyInfo("Invalid end point", "Please enter a value between "+
                            v+" and "+(max+1));
            return;
        }
        if (type == TYPE_T) {
            model.setEndT(val);
            view.setEndT(val);
        } else if (type == TYPE_Z) {
            model.setEndZ(val);
            view.setEndZ(val);
        }
    }
    
    /**
     * Checks if the delay entered for the timer is valid.
     * If so, sets the value and updates the UI.
     */
    private void editorActionHandler()
    {
    	int val = ((Integer) view.fps.getValue()).intValue();
        Number n = view.editor.getValueAsNumber();
        if (n != null) {
        	val = n.intValue();
            model.setTimerDelay(val);
            view.setTimerDelay(val);
        } else {
        	view.editor.setText(""+val);
        	 model.setTimerDelay(val);
             view.setTimerDelay(val);
        }
    } 
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    MoviePlayerControl(MoviePlayer model, MoviePlayerUI view)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (view == null) throw new IllegalArgumentException("No view.");
        this.view = view;
        this.model = model;
        initListeners();
    }
    
    /**
     * Sets the values required to play the movie. Each time a value is set
     * and the movie is playing, the movie is first stopped.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        try {
            int index = Integer.parseInt(ae.getActionCommand());
            switch (index) {
                case START_T:
                    movieStartActionHandler(view.startT, view.endT, TYPE_T);
                    break;
                case END_T:
                    movieEndActionHandler(view.startT, view.endT, TYPE_T); 
                    break;
                case START_Z:
                    movieStartActionHandler(view.startZ, view.endZ, TYPE_Z);  
                    break;
                case END_Z:
                    movieEndActionHandler(view.startZ, view.endZ, TYPE_Z); 
                    break;
                case ACROSS_T_CMD:
                	if (view.acrossZ.isSelected()) {
                		if (view.acrossT.isSelected())
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_ZT);
                		else 
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_Z);
                	} else {
                		if (view.acrossT.isSelected())
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_T);
                		else 
                			view.acrossT.setSelected(true);
                	}
                    break;
                case ACROSS_Z_CMD:
                	if (view.acrossT.isSelected()) {
                		if (view.acrossZ.isSelected())
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_ZT);
                		else 
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_T);
                	} else {
                		if (view.acrossZ.isSelected())
                			model.setMovieIndex(MoviePlayerDialog.ACROSS_Z);
                		else 
                			view.acrossZ.setSelected(true);
                	}
                    break;
                case ACROSS_ZT_CMD:
                    model.setMovieIndex(MoviePlayerDialog.ACROSS_ZT);
                    break;
                case PLAY_CMD:
                    model.setPlayerState(Player.START); 
                    break;
                case PAUSE_CMD:
                    model.setPlayerState(Player.PAUSE); 
                    break;    
                case STOP_CMD:
                    model.setPlayerState(Player.STOP); 
                    break;
                case EDITOR_CMD:
                    editorActionHandler(); 
                    break; 
                case MOVIE_TYPE_CMD:
                    int i = ((JComboBox) ae.getSource()).getSelectedIndex();
                    model.setMovieType(view.getMovieType(i));
                    break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+ae.getActionCommand(), nfe); 
        }
    }

    /**
     * Sets the timer delay.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        int v = ((Integer) view.fps.getValue()).intValue();
        model.setTimerDelay(v);
        view.setTimerDelay(v);
    }

    /** 
     * Handles the lost of focus on the various text fields.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * value.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent e)
    {
        String edit = view.editor.getText(),
        ed = ""+model.getTimerDelay();
        if (edit == null || !edit.equals(ed)) 
            view.editor.setText(ed);
        String startT = ""+(model.getStartT()+1);
        String endT = ""+(model.getEndT()+1);
        String startVal = view.startT.getText(), endVal = view.endT.getText();
        if (startVal == null || !startVal.equals(startT))
             view.startT.setText(startT);
        if (endVal == null || !endVal.equals(endT)) 
            view.endT.setText(endT);
        String startZ = ""+(model.getStartZ()+1);
        String endZ = ""+(model.getEndZ()+1);
        startVal = view.startZ.getText();
        endVal = view.endZ.getText();
        if (startVal == null || !startVal.equals(startZ))
            view.startZ.setText(startZ);
        if (endVal == null || !endVal.equals(endZ)) 
            view.endZ.setText(endZ);
    }

    /**
     * Sets the z-section interval and timepoint interval.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        Object source = evt.getSource();
        int s = -1;
        int e = -1;
        if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)) {
        	if (source.equals(view.zSlider)) {
        		s = view.zSlider.getStartValue();
        		model.setStartZ(s);
        		view.setStartZ(s);
        	} else if (source.equals(view.tSlider)) {
        		s = view.tSlider.getStartValue();
        		model.setStartT(s);
        		view.setStartT(s);
        	}
        } else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
            if (source.equals(view.zSlider)) {
                e = view.zSlider.getEndValue();
                model.setEndZ(e);
                view.setEndZ(e);
            } else if (source.equals(view.tSlider)) {
                e = view.tSlider.getEndValue();
                model.setEndT(e);
                view.setEndT(e);
            }
        }
    }

    /** 
     * Required by {@link FocusListener} I/F but not actually needed in
     * our case, no operation implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
