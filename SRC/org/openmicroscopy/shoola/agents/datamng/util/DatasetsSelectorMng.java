/*
 * org.openmicroscopy.shoola.agents.datamng.util.DatasetsSelectorMng
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

package org.openmicroscopy.shoola.agents.datamng.util;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;


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
class DatasetsSelectorMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int        LOAD = 0;
    
    /** Action command ID. */
    private static final int        SELECT_ALL = 1;
    
    /** Action command ID. */
    private static final int        RESET = 2;
    
    /** Reference to the view. */
    private DatasetsSelector        view;
    
    /** Reference to the {@link DataManagerCtrl agentControl}. */
    private DataManagerCtrl         agentCtrl;
    
    private IDatasetsSelectorMng    selectorMngRef;
    
    /** List of selected datasets. */
    private List                    datasetsSelected;

    private int                     index;
    
    private DataObject              object;
    
    DatasetsSelectorMng(DatasetsSelector view, DataManagerCtrl agentCtrl, 
                        IDatasetsSelectorMng selectorMngRef, int index, 
                        DataObject object)
    {
        this.view = view;
        this.agentCtrl = agentCtrl;
        this.selectorMngRef = selectorMngRef;
        this.index = index;
        this.object = object;
        datasetsSelected = new ArrayList();
        attachListeners();
    }
    
    /** 
     * Add (resp. remove) the dataset summary to (resp. from) the list of
     * dataset summary objects.
     * 
     * @param value     boolean value true if the checkBox is selected
     *                  false otherwise.
     * @param ds        dataset summary to add or remove
     */
    void addDataset(boolean value, DatasetSummary ds) 
    {
        if (value)  {
            if (!datasetsSelected.contains(ds)) datasetsSelected.add(ds);
        } else  datasetsSelected.remove(ds);
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        attachButtonListener(view.selectButton, SELECT_ALL);
        attachButtonListener(view.resetButton, RESET);
        attachButtonListener(view.loadButton, LOAD);
    }
    
    /** Attach listener to a JButton. */
    private void attachButtonListener(AbstractButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handles events fired by JButtons. */
    public void actionPerformed(ActionEvent e)
    {
        try {
            int index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case LOAD:
                    loadImages(); break;
                case SELECT_ALL:
                    selectAllDatasets(); break;
                case RESET:
                    resetSelection(); break;
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
        } 
    }
    
    /** De-select all datasets. */
    private void resetSelection()
    {
        view.selectButton.setEnabled(true);
        view.setSelection(Boolean.FALSE);
    }
    
    /** Select all datasets. */
    private void selectAllDatasets()
    {
        view.selectButton.setEnabled(false);
        view.setSelection(Boolean.TRUE);
    }
    
    /** Load the images. */
    private void loadImages()
    {
        if (datasetsSelected.size() == 0) return;
        view.setVisible(false);
        view.dispose();
        agentCtrl.loadImagesInDatasets(datasetsSelected, selectorMngRef, 
                                        index, object);
    }
    
}
