/*
 * org.openmicroscopy.shoola.agents.roi.pane.ROIViewerMng
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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies;

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
public class ROIViewerMng
    implements ActionListener
{
    /** Constant to fix the magFactor for the Lens. */
    public static final double      MIN_MAG = 1.0;
    
    /** Action ID. */
    private static final int        MAG_PLUS = 0, MAG_MINUS = 1, 
                                    MAG_FIT = 2;
    
    private static final double     incrementMag = 0.5;
    
    private ROIViewer               view;
    
    private double                  factor, oldFactor;
    
    public ROIViewerMng(ROIViewer view)
    {
        this.view = view;
        factor = MIN_MAG;
        oldFactor = MIN_MAG;
        attachListeners();
    }

    public Rectangle getViewportBounds() 
    {
        return view.scrollPane.getViewportBorderBounds();
    }
    
    public double getFactor() { return factor; }
    
    public double getOldFactor() { return oldFactor; }
    
    void resetMagnificationFactor()
    {
        oldFactor = MIN_MAG;
        factor = MIN_MAG;    
        String s = ""+(int)(factor*100)+"%";
        view.magText.setText(s);
    }
    
    private void attachListeners()
    {
        attachButtonListener(view.magPlus, MAG_PLUS);
        attachButtonListener(view.magMinus, MAG_MINUS);
        attachButtonListener(view.magFit, MAG_FIT);
    }
    
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case MAG_PLUS:
                    incrementMagFactor(); break;
                case MAG_MINUS:
                    decrementMagFactor(); break;
                case MAG_FIT:
                    resetMagFactor();     
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    private void resetMagFactor()
    {
        oldFactor = factor;
        factor = MIN_MAG;
        //oldFactor = MIN_MAG;
        magnify();
    }
    private void incrementMagFactor()
    {
        oldFactor = factor;
        factor += incrementMag; 
        magnify();
    }
    
    private void decrementMagFactor()
    {
        oldFactor = factor;
        factor -= incrementMag;
        if (factor < MIN_MAG) factor = MIN_MAG;
        magnify();
    }
    
    private void magnify()
    {
        view.magnify(factor);
        String s = ""+(int)(factor*100)+"%";
        view.magText.setText(s);
    }
    
}
