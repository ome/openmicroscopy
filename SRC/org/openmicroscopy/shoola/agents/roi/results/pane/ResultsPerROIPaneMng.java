/*
 * org.openmicroscopy.shoola.agents.roi.results.pane.ResultsPerROIMng
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

package org.openmicroscopy.shoola.agents.roi.results.pane;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;

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
class ResultsPerROIPaneMng
    implements ActionListener
{
    
    private static final int    CHANNEL = 0;
    
    private ResultsPerROIPane   view;
    
    private int                 viewIndex;
    
    private ROIResultsMng       mng;
    
    ResultsPerROIPaneMng(ResultsPerROIPane view, ROIResultsMng mng, 
                        int viewIndex)
    {
        this.view = view;
        this.viewIndex = viewIndex;
        this.mng = mng;
        attachListeners();
    }
    
    private void attachListeners()
    {
        view.channels.addActionListener(this);
        view.channels.setActionCommand(""+CHANNEL);
    }

    /** Handle events. ONLY comboBox but eventually the Annotation??*/
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case CHANNEL:
                    mng.showResultsForROI(viewIndex, 
                            view.channels.getSelectedIndex());
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }
    
}
