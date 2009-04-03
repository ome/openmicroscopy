/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.MoviePaneMng
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
import javax.swing.JRadioButton;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.movie.Player;
import org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;



/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class MoviePaneMng
    implements ActionListener, FocusListener
{
    
    /** Action command ID. */
    private static final int    START_T = 0;
    
    /** Action command ID. */
    private static final int    END_T = 1;
    
    /** Action command ID. */
    private static final int    START_Z = 2;
    /** Action command ID. */
    private static final int    END_Z = 3;
    
    private int                 curStartZ, curEndZ, curStartT, curEndT;
    
    private int                 max, maxT, maxZ;
    
    private GraphicSliderMng    gsZMng, gsTMng;
    
    private int                 movieIndex;
    
    private JTextField          startTField, startZField, endTField, endZField;
    
    /** Reference to the {@link MoviePane view}. */
    private MoviePane           view;
    
    private PlayerUIMng         playerUIMng;
    
    private Registry            registry;
    
    MoviePaneMng(MoviePane view, PlayerUIMng playerUIMng, Registry registry,
                int maxT, int maxZ, MovieSettings settings)
    {
        this.view = view;
        this. playerUIMng = playerUIMng;
        this.registry = registry;
        this.maxT = maxT;
        this.maxZ = maxZ;
        max = maxT;
        curStartT = settings.getStartT();
        curEndT = settings.getEndT();
        curStartZ = settings.getStartZ();
        curEndZ = settings.getEndZ();
        movieIndex = settings.getMovieIndex();
    }
    
    /** Attach listeners. */
    void attachListeners()
    {
        //TexField
        startTField = view.getMovieStartT();
        endTField = view.getMovieEndT();
        startZField = view.getMovieStartZ();
        endZField = view.getMovieEndZ();
        startTField.setActionCommand(""+START_T);  
        startTField.addActionListener(this);
        startTField.addFocusListener(this);
        endTField.setActionCommand(""+END_T);  
        endTField.addActionListener(this);
        endTField.addFocusListener(this);
        startZField.setActionCommand(""+START_Z);  
        startZField.addActionListener(this);
        startZField.addFocusListener(this);
        endZField.setActionCommand(""+END_Z);  
        endZField.addActionListener(this);
        endZField.addFocusListener(this);
        //RadioButton
        JRadioButton movieZ = view.getMovieZ(), movieT = view.getMovieT();
        movieZ.addActionListener(this);
        movieZ.setActionCommand(""+Player.MOVIE_Z);
        movieT.addActionListener(this);
        movieT.setActionCommand(""+Player.MOVIE_T);
    }
    
    void setSlidersMng(GraphicSliderMng gsZMng, GraphicSliderMng gsTMng)
    {
        this.gsZMng = gsZMng;
        this.gsTMng = gsTMng;
    }
    
    void setMovieStart(int v) 
    {
        if (movieIndex == Player.MOVIE_T){
            curStartT = v;
            startTField.setText(""+v);
        } else if (movieIndex == Player.MOVIE_Z) {
            curStartZ = v;
            startZField.setText(""+v);
        }
        playerUIMng.setStartMovie(v);
    }

    void setMovieEnd(int v) 
    {
        if (movieIndex == Player.MOVIE_T){
            curEndT = v;
            endTField.setText(""+v);
        } else if (movieIndex == Player.MOVIE_Z) {
            curEndZ = v;
            endZField.setText(""+v);
        }
        playerUIMng.setEndMovie(v);
    }
    
    private void handleIndexChanged(int index)
    {
        if (maxT == 0) {
            view.getMovieZ().setSelected(true);
            UserNotifier un = registry.getUserNotifier();
            un.notifyInfo("Invalid selection", 
                "The selected image has only one timepoint. ");
        }
        if (maxZ == 0) {
            view.getMovieT().setSelected(true);
            UserNotifier un = registry.getUserNotifier();
            un.notifyInfo("Invalid selection", 
                "The selected image has only one z-section. ");
        }
        if (maxZ != 0 && maxT != 0) setMovieIndex(index);
    }
    
    private void setMovieIndex(int index)
    {
        movieIndex = index;
        int start = Integer.parseInt(startTField.getText());
        int end = Integer.parseInt(endTField.getText());
        if (movieIndex == Player.MOVIE_T) {
            max = maxT;
            view.getSliderT().attachListeners();
            view.getSliderZ().removeListeners();
            setTFieldsEnabled(true);
            setZFieldsEnabled(false); 
        } else {
            start = Integer.parseInt(startZField.getText());
            end = Integer.parseInt(endZField.getText());
            max = maxZ;
            view.getSliderT().removeListeners();
            view.getSliderZ().attachListeners();
            setTFieldsEnabled(false);
            setZFieldsEnabled(true);
        }
        playerUIMng.setIndex(movieIndex, max, start, end);
    }
    
    private void setZFieldsEnabled(boolean b)
    {
        startZField.setEnabled(b);
        endZField.setEnabled(b);
    }
    
    private void setTFieldsEnabled(boolean b)
    {
        startTField.setEnabled(b);
        endTField.setEnabled(b);
    }
    
    /** 
     * Handles the action event fired by the starting text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void movieStartActionHandler(JTextField start, JTextField end)
    {
        boolean valid = false;
        int val = 0;
        int valEnd = max;
        try {
            val = Integer.parseInt(start.getText());
            valEnd = Integer.parseInt(end.getText());
            if (0 <= val && val < valEnd) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            int v = valEnd-1; 
            start.selectAll();
            UserNotifier un = registry.getUserNotifier();
            un.notifyInfo("Invalid start point", 
                "Please enter a value between 0 and "+v);
        } else {
            if (movieIndex == Player.MOVIE_T) {
                curStartT = val;
                gsTMng.setStart(val);
            } else {
                curStartZ = val;
                gsZMng.setStart(val);
            }
            playerUIMng.setStartMovie(val);
        } 
    }
    
    /** 
     * Handles the action event fired by the end text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void movieEndActionHandler(JTextField start, JTextField end)
    {
        boolean valid = false;
        int val = 0;
        int valStart = 0;
        try {
            val = Integer.parseInt(end.getText());
            valStart = Integer.parseInt(start.getText());
            if (valStart < val && val <= max) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            end.selectAll();
            UserNotifier un = registry.getUserNotifier();
            int v = valStart+1;
            un.notifyInfo("Invalid end point", 
                "Please enter a value between "+ v+" and "+max);
        } else {
            if (movieIndex == Player.MOVIE_T) {
                curEndT = val;
                gsTMng.setEnd(val);
            } else {
                curEndZ = val;
                gsZMng.setEnd(val);
            }
            playerUIMng.setEndMovie(val);
        } 
    }
    
    /** Handle events fired by JTextFields. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case START_T:
                    movieStartActionHandler(startTField, endTField);
                    break;
                case END_T:
                    movieEndActionHandler(startTField, endTField); 
                    break;
                case START_Z:
                    movieStartActionHandler(startZField, endZField); 
                    break;
                case END_Z:
                    movieEndActionHandler(startZField, endZField);
                    break;
                case Player.MOVIE_T:
                case Player.MOVIE_Z:
                    handleIndexChanged(index);
                    break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

    /** 
     * Handles the lost of focus on the timepoint text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * value.
     */
    public void focusLost(FocusEvent e)
    {
        String start = ""+curStartT, end = ""+curEndT;
        String startVal = startTField.getText(), endVal = endTField.getText();
        JTextField startField = startTField, endField = endTField;
        if (movieIndex == Player.MOVIE_Z) {
            start = ""+curStartZ;
            end = ""+curEndZ;
            startVal = startZField.getText();
            endVal = endZField.getText();
            startField = startZField;
            endField = endZField;
        }
        
        if (startVal == null || !startVal.equals(start))
            startField.setText(start);        
        if (endVal == null || !endVal.equals(end)) 
            endField.setText(end);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */ 
    public void focusGained(FocusEvent e) {}
    
}
