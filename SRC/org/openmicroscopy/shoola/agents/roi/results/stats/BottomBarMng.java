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

import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies

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
    
    /** Action ID, aggregate on Z. */
    private static final int        Z = 0;
    
    /** Action ID, aggregate on T. */
    private static final int        T = 1;
    
    /** Action ID, aggregate on Z and T. */
    private static final int        ZT = 2;
    
    /** Action ID, display results for both Z and T. */
    private static final int        ZANDT = 3;
    
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
        attachButtonListeners(view.zButton, Z);
        attachButtonListeners(view.tButton, T);
        attachButtonListeners(view.ztButton, ZT);
        attachButtonListeners(view.zAndtButton, ZANDT);
    }

    /** Attach a {@link ActionListener listener} to a button. */
    private void attachButtonListeners(JRadioButton button, int id)
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
                case Z:
                    mng.aggregateOnZ(); break;
                case T:
                    mng.aggregateOnT(); break;
                case ZT:
                    mng.aggregateOnZAndT(); break;
                case ZANDT:
                    mng.displayZandT();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }
    
}
