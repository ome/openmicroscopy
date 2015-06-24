/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.util.ui.NumericalTextField;
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
    
    /**
     * Adds listeners to a {@link JTextField}. 
     *
     * @param field The component to attach the listeners to.
     */
    private void attachFieldListeners(JTextField field)
    {
        field.addFocusListener(this);
        field.addPropertyChangeListener(this);
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
        attachFieldListeners(view.startT);
        attachFieldListeners(view.endT);
        attachFieldListeners(view.startZ);
        attachFieldListeners(view.endZ);
        attachButtonListener(view.acrossZ, ACROSS_Z_CMD);
        attachButtonListener(view.acrossT, ACROSS_T_CMD);
        //attachButtonListener(view.acrossZT, ACROSS_ZT_CMD);
        view.tSlider.addPropertyChangeListener(this);
        view.zSlider.addPropertyChangeListener(this);
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
        if (CommonsLangUtils.isBlank(startVal) || !startVal.equals(startT))
             view.startT.setText(startT);
        if (CommonsLangUtils.isBlank(endVal) || !endVal.equals(endT)) 
            view.endT.setText(endT);
        String startZ = ""+(model.getStartZ()+1);
        String endZ = ""+(model.getEndZ()+1);
        startVal = view.startZ.getText();
        endVal = view.endZ.getText();
        if (CommonsLangUtils.isBlank(startVal) || !startVal.equals(startZ))
            view.startZ.setText(startZ);
        if (CommonsLangUtils.isBlank(endVal) || !endVal.equals(endZ)) 
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
        		s = view.zSlider.getStartValueAsInt();
        		model.setStartZ(s);
        		view.setStartZ(s);
        	} else if (source.equals(view.tSlider)) {
        		s = view.tSlider.getStartValueAsInt();
        		model.setStartT(s);
        		view.setStartT(s);
        	}
        } else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
            if (source.equals(view.zSlider)) {
                e = view.zSlider.getEndValueAsInt();
                model.setEndZ(e);
                view.setEndZ(e);
            } else if (source.equals(view.tSlider)) {
                e = view.tSlider.getEndValueAsInt();
                model.setEndT(e);
                view.setEndT(e);
            }
        } else if (NumericalTextField.TEXT_UPDATED_PROPERTY.equals(name)) {
            Number n;
            if (source.equals(view.startT)) {
                n = view.startT.getValueAsNumber();
                if (n == null) return;
                s = n.intValue()-1;
                if (s >= model.getEndT() || s < 0) return;
                model.setStartT(s);
                view.tSlider.setStartValue(s);
            } else if (source.equals(view.startZ)) {
                n = view.startZ.getValueAsNumber();
                if (n == null) return;
                s = n.intValue()-1;
                if (s >= model.getEndZ() || s < 0) return;
                model.setStartZ(s);
                view.zSlider.setStartValue(s);
            } else if (source.equals(view.endZ)) {
                n = view.endZ.getValueAsNumber();
                if (n == null) return;
                s = n.intValue()-1;
                if (s <= model.getStartZ() || s > model.getMaxZ()) return;
                model.setEndZ(s);
                 view.zSlider.setEndValue(s);
            } else if (source.equals(view.endT)) {
                n = view.endT.getValueAsNumber();
                if (n == null) return;
                s = n.intValue()-1;
                if (s <= model.getStartT() || s > model.getMaxT()) return;
                model.setEndT(s);
                view.tSlider.setEndValue(s);
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
