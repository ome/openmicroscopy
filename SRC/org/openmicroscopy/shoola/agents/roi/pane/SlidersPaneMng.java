/*
 * org.openmicroscopy.shoola.agents.roi.pane.SlidersPaneMng
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

package org.openmicroscopy.shoola.agents.roi.pane;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgt;
import org.openmicroscopy.shoola.agents.roi.defs.ROISettings;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.GraphicSlider;
import org.openmicroscopy.shoola.util.ui.events.ChangeEventSlider;

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
public class SlidersPaneMng
    implements ActionListener, FocusListener, ChangeListener,  ItemListener 
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
    
    private int                 maxT, maxZ;
    
    private JTextField          startTField, startZField, endTField, endZField;
    
    private JCheckBox           zSelect, tSelect, ztSelect;
    
    private ROISettings         settings;
    
    /** Reference to the {@link MoviePane view}. */
    private SlidersPane         view;
    
    private Registry            registry;
    
    SlidersPaneMng(SlidersPane view, Registry registry, 
                    int maxT, int maxZ, ROISettings settings)
    {
        this.view = view;
        this.maxT = maxT;
        this.maxZ = maxZ;
        this.settings = settings;
        this.registry = registry;
        curStartT = settings.getStartT();
        curEndT = settings.getEndT();
        curStartZ = settings.getStartZ();
        curEndZ = settings.getEndZ();
    }
    
    public int getStartZ() { return curStartZ; }
    
    public int getEndZ() { return curEndZ; }
    
    public int getStartT() { return curStartT; }
    
    public int getEndT() { return curEndT; }
    
    /** Attach listeners. */
    void attachListeners()
    {
        //TexField
        startTField = view.getStartT();
        endTField = view.getEndT();
        startZField = view.getStartZ();
        endZField = view.getEndZ();
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
        //CheckBox
        zSelect = view.getZSelection(); 
        tSelect = view.getTSelection();
        ztSelect = view.getZTSelection();
        zSelect.addItemListener(this);
        tSelect.addItemListener(this);
        ztSelect.addItemListener(this);
        view.getSliderT().addChangeListener(this);
        view.getSliderZ().addChangeListener(this);
    }
    
    /** Handle graphicSlider stateChanged. */
    public void stateChanged(ChangeEvent e)
    {
        GraphicSlider source = (GraphicSlider) e.getSource();
        if (source == view.getSliderT()) 
            handleSliderTStateChanged((ChangeEventSlider) e);
        else handleSliderZStateChanged((ChangeEventSlider) e);
    }
    
    /** Handle events fired by JTextFields. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case START_T:
                    startActionHandler(startTField, endTField, maxT, 
                                            ROIAgt.INDEX_T);
                    break;
                case END_T:
                    endActionHandler(startTField, endTField, maxT, 
                                            ROIAgt.INDEX_T); 
                    break;
                case START_Z:
                    startActionHandler(startZField, endZField, maxZ, 
                                            ROIAgt.INDEX_Z); 
                    break;
                case END_Z:
                    endActionHandler(startZField, endZField, maxZ, 
                                            ROIAgt.INDEX_Z);
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
        Object source = e.getSource();
        if (source == startZField) {          
            start = ""+curStartZ;
            startVal = startZField.getText();
            startField = startZField;
        } else if (source == endTField) {
            end = ""+curEndZ;
            endVal = endZField.getText();
            endField = endZField;
        }
        if (startVal == null || !startVal.equals(start))
            startField.setText(start);        
        if (endVal == null || !endVal.equals(end)) 
            endField.setText(end);
    }
    
    /** Handle events fired by CheckBox. */
    public void itemStateChanged(ItemEvent e)
    {
        Object source = e.getItemSelectable();
        boolean b = false;
        if (e.getStateChange() == ItemEvent.SELECTED) b = true;
        if (source == zSelect)  handleCheckBoxZ(b);
        else if (source == tSelect) handleCheckBoxT(b);
        else if (source == ztSelect) handleCheckBoxZT(b);
    }
    
    private void handleCheckBoxZT(boolean b)
    {
        
        if (b) {
            zSelect.setSelected(!b);
            tSelect.setSelected(!b);
            view.getSliderT().removeMouseListeners();
            view.getSliderZ().removeMouseListeners();
            startTField.setEnabled(!b);
            endTField.setEnabled(!b);
            startZField.setEnabled(!b);
            endZField.setEnabled(!b);
        }
        ztSelect.setSelected(b);
        settings.setTSelected(!b);
        settings.setZSelected(!b);
        settings.setZTSelected(b);
    }
    
    private void handleCheckBoxT(boolean b)
    {
        if (maxT == 0) {
            if (b) {
                tSelect.setSelected(false);
                UserNotifier un = registry.getUserNotifier();
                un.notifyInfo("Invalid selection", 
                    "The selected image has only one timepoint. ");  
            }
        } else {
            ztSelect.setSelected(false);
            if (b) view.getSliderT().attachMouseListeners();
            else view.getSliderT().removeMouseListeners();
            startTField.setEnabled(b);
            endTField.setEnabled(b); 
            settings.setTSelected(b);
            settings.setZTSelected(false);
        }
    }
    
    private void handleCheckBoxZ(boolean b)
    {
        if (maxZ == 0) {
            if (b) {
                zSelect.setSelected(false);
                UserNotifier un = registry.getUserNotifier();
                un.notifyInfo("Invalid selection", 
                    "The selected image has only one z-section. ");
            }
        } else {
            ztSelect.setSelected(false);
            if (b) view.getSliderZ().attachMouseListeners();
            else view.getSliderZ().removeMouseListeners();
            startZField.setEnabled(b);
            endZField.setEnabled(b);  
            settings.setZSelected(b);
            settings.setZTSelected(false);
        }
    }
    
    private void handleSliderTStateChanged(ChangeEventSlider e)
    {
        if (e.isStart())
            setStartT(view.getSliderT().getStartValue());
        else
            setEndT(view.getSliderT().getEndValue());
    }
    
    private void handleSliderZStateChanged(ChangeEventSlider e)
    {
        if (e.isStart())
            setStartZ(view.getSliderZ().getStartValue());
        else
            setEndZ(view.getSliderZ().getEndValue());
    }

    /** 
     * Handles the action event fired by the starting text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void startActionHandler(JTextField start, JTextField end, 
                                            int max, int index)
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
            if (index == ROIAgt.INDEX_T) {
                //curStartT = val;
                setStartT(val);
                view.getSliderT().setStartValue(val);
            } else if (index == ROIAgt.INDEX_Z) {
                //curStartZ = val;
                setStartZ(val);
                view.getSliderZ().setStartValue(val);
            }
        } 
    }
    
    /** 
     * Handles the action event fired by the end text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void endActionHandler(JTextField start, JTextField end, 
                                        int max, int index)
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
            if (index == ROIAgt.INDEX_T) {
                //curEndT = val;
                setEndT(val);
                view.getSliderT().setEndValue(val);
            } else if (index == ROIAgt.INDEX_Z) {
                //curEndZ = val;
                setEndZ(val);
                view.getSliderZ().setEndValue(val);
            }
        } 
    }
    
    private void setStartZ(int v) 
    {
        curStartZ = v;
        startZField.setText(""+v);
        settings.setStartZ(v);
    }
    
    private void setEndZ(int v) 
    {
        curEndZ = v;
        endZField.setText(""+v);
        settings.setEndZ(v);
    }
    
    private void setStartT(int v) 
    {
        curStartT = v;
        startTField.setText(""+v);
        settings.setStartT(v);
    }
    
    private void setEndT(int v) 
    {
        curEndT = v;
        endTField.setText(""+v);
        settings.setEndT(v);
    }
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */ 
    public void focusGained(FocusEvent e) {}

}

