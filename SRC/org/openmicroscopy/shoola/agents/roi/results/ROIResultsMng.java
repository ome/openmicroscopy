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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane;
import org.openmicroscopy.shoola.env.config.Registry;

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
    
    private ROIAgtCtrl          control;
    
    public ROIResultsMng(ROIResults view, ROIAgtCtrl control)
    {
        this.view = view;
        this.control = control;
        attachListeners();
    }

    public Registry getRegistry() { return control.getRegistry(); }
    
    public ROIAgtUIF getReferenceFrame() { return control.getReferenceFrame(); }
    
    public ROIResults getParent() { return view; }
    
    public int getAnalyzedChannel(int index)
    {
        return control.getAnalyzedChannel(index);
    }
   
    public String[] getListROIs() { return view.listROIs; }
    
    /** Handle the close event. */
    public void handleClose()
    {
        //close allDialog
        view.setVisible(false);
        view.dispose();
    }
    
    /**
     * 
     * @param viewIndex
     * @param channelIndex
     */
    public Number[][] getDataForROI(int viewIndex, int channelIndex)
    {
        StatsResultsPane 
        pane = (StatsResultsPane) view.paneMap.get(new Integer(viewIndex));
        return pane.getManager().getDataToDisplay(channelIndex);
    }
    
    /** 
     * 
     * @param viewIndex
     * @param index         channel index.
     */
    public void showResultsForROI(int viewIndex, int channelIndex)
    {
        StatsResultsPane 
        pane = (StatsResultsPane) view.paneMap.get(new Integer(viewIndex));
        pane.getManager().displayResult(
                control.getAnalyzedChannel(channelIndex));
    }
    
    private void handleTabPaneSelection()
    {
        int tabIndex = view.tabs.getSelectedIndex();
        StatsResultsPane 
            pane = (StatsResultsPane) view.paneMap.get(new Integer(tabIndex));
        pane.getManager().synchDialog();
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        view.tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                handleTabPaneSelection();
            }});
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { handleClose(); }
        });
    }
 
}
