/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.CBSearchTabViewMng
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClearCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindAnnotatedCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd;

/** 
 * The {@link CBSearchTabView}'s controller. 
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CBSearchTabViewMng
    implements ActionListener
{

    /** Identifies the <code>Search</code> action. */
    private static final int SEARCH = 0;
    
    /** Indicates that some text has been entered in the text area. */
    private static final int SEARCH_SELECTION = 1;
    
    /** Identifies the <code>Clear</code> action. */
    private static final int CLEAR = 2;
    
    /** A reference to the View. */
    private CBSearchTabView view;
    
    /**
     * Adds an {@link ActionListener} to the specified component.
     * 
     * @param button The component.
     * @param id The action command ID.
     */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id); 
    }
    
    /** Adds listener to the UI components. */
    private void attachListeners()
    {
        view.searchType.addActionListener(this);
        view.searchType.setActionCommand(""+SEARCH_SELECTION);
        attachButtonListener(view.searchButton, SEARCH);
        attachButtonListener(view.clearButton, CLEAR);
    }
    
    /**
     * Performs the search action. Notes that the search action is 
     * not done at the Image level.
     */
    private void performSearch()
    {
        int index = view.getSearchType();
        String regEx = view.getSearchValue();
        FindRegExCmd cmd = new FindRegExCmd(view.model.getParentModel(), regEx,
                            index);
        cmd.execute();
    }
    
    /** Clears the previous search results. */
    private void clearSearch()
    {
        ClearCmd cmd = new ClearCmd(view.model.getParentModel());
        cmd.execute();
        view.clearSearchValue();
    }
    
    /**
     * Reacts to the {@link CBSearchTabView#ALL_ANNOTATED} action.
     */
    private void handleSelection()
    {
        if (view.searchType.getSelectedIndex() == CBSearchTabView.ALL_ANNOTATED)
        {
            FindAnnotatedCmd cmd = new FindAnnotatedCmd(
                                        view.model.getParentModel());
            cmd.execute();
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view  A reference to the {@link CBSearchTabView} view. Mustn't be
     *              <code>null</code>.
     */
    CBSearchTabViewMng(CBSearchTabView view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        attachListeners();
    }

    /**
     * Reacts to the actions.
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case SEARCH:
                    performSearch();
                    break;
                case CLEAR:
                    clearSearch();
                    break;
                case SEARCH_SELECTION:
                    handleSelection();
            } 
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }
    
}
