/*
 * org.openmicroscopy.shoola.agents.viewer.transform.lens.LensBarMng
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

package org.openmicroscopy.shoola.agents.viewer.transform.lens;


//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

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
public class LensBarMng
    implements ActionListener, ItemListener
{
    
    private static final Color[]        colorSelection;
    
    static {
        colorSelection = new Color[LensBar.MAX_COLOR+1];
        colorSelection[LensBar.RED] = Color.RED;
        colorSelection[LensBar.GREEN] = Color.GREEN;
        colorSelection[LensBar.BLUE] = Color.BLUE;
        colorSelection[LensBar.CYAN] = Color.CYAN;
        colorSelection[LensBar.MAGENTA] = Color.MAGENTA;
        colorSelection[LensBar.ORANGE] = Color.ORANGE;
        colorSelection[LensBar.PINK] = Color.PINK;
        colorSelection[LensBar.YELLOW] = Color.YELLOW;
    }
    
    private static final int        incrementWidth = 10;
    
    private static final double     incrementMag = 0.5;
    
    /** Action command ID: increase size of the lens. */
    private static final int        SIZE_PLUS = 0;
    
    /** Action command ID: decrease size of the lens. */
    private static final int        SIZE_MINUS = 1;
    
    /** Action command ID: increase magnification factor. */
    private static final int        MAG_PLUS = 2;
    
    /** Action command ID: decrease magnification factor. */
    private static final int        MAG_MINUS = 3;
    
    /** Action command ID: pick a color. */
    private static final int        COLOR_SELECT = 4;
    
    private int                     maxWidth;
    
    private double                  magFactor;
    
    private int                     width;
    
    private LensBar                 view;
    
    private ImageInspectorManager   control;
    
    public LensBarMng(LensBar view, ImageInspectorManager control, int w, int h)
    {
        this.view = view;
        this.control = control;
        magFactor = ViewerUIF.DEFAULT_MAG;
        width = ViewerUIF.DEFAULT_WIDTH;
        setMaxWidth(w, h);
    }

    public void setLensEnabled(boolean b)
    {
        JButton sizePlus = view.getSizePlus(), sizeMinus = view.getSizeMinus(), 
            magPlus = view.getMagPlus(), magMinus = view.getMagMinus();
        sizePlus.setEnabled(b);
        sizeMinus.setEnabled(b);
        magPlus.setEnabled(b);
        magMinus.setEnabled(b);
        view.getColors().setEnabled(b);
        view.getOnOff().setEnabled(b);
        view.getPin().setEnabled(b);
        view.getPainting().setEnabled(b);
    }
    
    double getMagFactor() { return magFactor; }
    
    /** Attach listeners. */
    void attachListeners()
    {
        //button
        attachButtonListeners(view.getSizePlus(), SIZE_PLUS);
        attachButtonListeners(view.getSizeMinus(), SIZE_MINUS);
        attachButtonListeners(view.getMagPlus(), MAG_PLUS);
        attachButtonListeners(view.getMagMinus(), MAG_MINUS);
        //ComboBox
        JComboBox box = view.getColors();
        box.addActionListener(this);
        box.setActionCommand(""+COLOR_SELECT);
        //CheckBox
        view.getOnOff().addItemListener(this);
        view.getPin().addItemListener(this);
        view.getPainting().addItemListener(this);
    }

    /** Handle event fired by button. */
    public void actionPerformed(ActionEvent e) 
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case SIZE_PLUS:
                    incrementWidth(); break;
                case SIZE_MINUS:
                    decrementWidth(); break;
                case MAG_PLUS:
                    incrementMagFactor(); break;
                case MAG_MINUS:
                    decrementMagFactor(); break;
                case COLOR_SELECT:
                    selectColor(); 
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe);
        }
    }

    /** Handle event fired by CheckBox. */
    public void itemStateChanged(ItemEvent e)
    {
        Object source = e.getSource();
        boolean b = false;
        if (e.getStateChange() == ItemEvent.SELECTED) b = true;
        if (source == view.getOnOff()) control.setLensOnOff(b);
        else if (source == view.getPin()) control.setPin(b);
        else if (source == view.getPainting()) {
            // grab the color.
            int index = view.getColors().getSelectedIndex();
            control.setPainting(b, colorSelection[index]);
        }  
    }
    
    private void selectColor()
    {
        if (view.getPainting().isSelected()) {
            int index = view.getColors().getSelectedIndex();
            control.setPainting(true, colorSelection[index]);
        }
    }
    
    private void incrementMagFactor()
    { 
        magFactor += incrementMag; 
        if (magFactor > ViewerUIF.MAX_MAG) magFactor = ViewerUIF.MAX_MAG;
        control.setLensMagFactor(magFactor);
        view.getMagFactorField().setText(""+magFactor);
    }
    
    private void decrementMagFactor()
    { 
        magFactor -= incrementMag;
        if (magFactor < ViewerUIF.MIN_MAG) magFactor = ViewerUIF.MIN_MAG;
        control.setLensMagFactor(magFactor);
        view.getMagFactorField().setText(""+magFactor);
    }
    
    private void decrementWidth()
    { 
        width -= incrementWidth;
        if (width < ViewerUIF.MIN_WIDTH) width = ViewerUIF.MIN_WIDTH;
        control.setLensWidth(width);
    }
    
    private void incrementWidth()
    {
        width += incrementWidth;
        if (width > maxWidth) width = maxWidth;
        control.setLensWidth(width);
    }
    
    private void setMaxWidth(int w, int h)
    {
        int min = Math.min(w, h);
        maxWidth = min/3;
    }
    
    /** Attach listener to a JButton. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
}

