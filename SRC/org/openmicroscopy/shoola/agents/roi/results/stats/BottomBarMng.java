/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.BottomBarMng
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

package org.openmicroscopy.shoola.agents.roi.results.stats;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class BottomBarMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int        SAVE = 0, GRAPHIC = 1, BACK_TO_TABLE = 2,
                                    BACK_TO_GRAPHIC = 3, BACKGROUND = 4,
                                    RATIO = 5, INITIAL = 6;
    
    private BottomBar               view;
    
    private StatsResultsPaneMng     mng;
    
    BottomBarMng(BottomBar view, StatsResultsPaneMng mng)
    {
        this.view = view;
        this.mng = mng;
        attachListeners();
    }
    
    /** Attach Listeners. */
    private void attachListeners()
    {
        attachButtonListeners(view.save, SAVE);
        attachButtonListeners(view.graphic, GRAPHIC);
        attachButtonListeners(view.back, BACK_TO_TABLE);
        attachButtonListeners(view.forward, BACK_TO_GRAPHIC);
        attachButtonListeners(view.background, BACKGROUND);
        attachButtonListeners(view.ratio, RATIO);
        attachButtonListeners(view.initial, INITIAL);
    }

    /** Attach a {@link ActionListener listener} to a button. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id); 
    }
    
    /** Handle radioButton events. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case SAVE:
                    mng.saveResult(); break;
                case GRAPHIC:
                    mng.showGraphicSelection(); break;
                case BACK_TO_TABLE:
                    mng.backToTable(); break;
                case BACK_TO_GRAPHIC:
                    mng.backToGraphic(); break;
                case BACKGROUND:
                    showROISelector(StatsResultsPaneMng.TABLE_BACKGROUND, 
                            "Background");
                    break;
                case RATIO:
                    showROISelector(StatsResultsPaneMng.TABLE_RATIO, "Ratio"); 
                    break;
                case INITIAL:
                    view.setEnabledMoveButtons(false);
                    mng.displayInitialData();
                    break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }
    
    private void showROISelector(int tableIndex, String s)
    {
        view.setEnabledMoveButtons(false);
        mng.setTableIndex(tableIndex);
        UIUtilities.centerAndShow(new ROISelector(mng, s));
    }

}
