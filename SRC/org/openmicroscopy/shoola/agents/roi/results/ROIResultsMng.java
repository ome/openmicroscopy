/*
 * org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng
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

package org.openmicroscopy.shoola.agents.roi.results;

//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.roi.results.pane.ResultsPerROIPane;


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
public class ROIResultsMng
{
    
    private ROIResults          view;
    
    private int                 algorithmIndex;
    
    public ROIResultsMng(ROIResults view)
    {
        this.view = view;
        attachListeners();
    }

    public void setAlgorithmIndex(int index) 
    {
        algorithmIndex = index;
        //MUST BE ONE OF THE INDEX DEFINED IN ROIAgtUIF
    }
    
    public String[] getChannels() { return null; }
    
    /** Handle the close event. */
    public void handleClose()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    public void channelSelectedForROI(int viewIndex, int channelIndex)
    {
        ResultsPerROIPane 
            roiPane = (ResultsPerROIPane) view.roiPaneList.get(viewIndex);
        // retrieve data according channelIndex and algoIndex
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { handleClose(); }
        });
    }
    
    
    
}
