/*
 * org.openmicroscopy.shoola.agents.roi.pane.AnalysisControlsMng
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
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
public class AnalysisControlsMng
    implements ActionListener, ListSelectionListener 
{

    /** Action command ID. */
    private static final int    ANALYSE_STATS = 0;
    
    private List                selectedChannels;
    
    /** Reference to the {@link MoviePane view}. */
    private AnalysisControls    view;
    
    private ROIAgtCtrl          control;
    
    public AnalysisControlsMng(AnalysisControls view, ROIAgtCtrl control)
    {
        this.view = view;
        this.control = control;
        selectedChannels = new ArrayList();
    }
    
    public List getSelectedChannels() { return selectedChannels; }
    
    /** Attach listeners. */
    void attachListeners()
    {
        attachButtonListeners(view.analyseStats, ANALYSE_STATS);
        ListSelectionModel model = view.listChannels.getSelectionModel();
        model.addListSelectionListener(this);
    }

    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Handle multiSelection list. */
    public void valueChanged(ListSelectionEvent e)
    {
        ListSelectionModel model = (ListSelectionModel) e.getSource();
        if (!model.isSelectionEmpty() && e.getValueIsAdjusting()) {
            // Find out which indexes are selected.
            selectedChannels.removeAll(selectedChannels);
            int minIndex = model.getMinSelectionIndex(),
                maxIndex = model.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++)
                if (model.isSelectedIndex(i))
                    selectedChannels.add(new Integer(i)); 
        } 
    }

    /** Handle events fired by JTextFields. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case ANALYSE_STATS:
                    control.analyseStats();
                    break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

}

