/*
 * org.openmicroscopy.shoola.agents.datamng.util.FilterMng
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
import java.sql.Timestamp;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Manager of the {@link Filter} UI component. 
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
class FilterMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int    APPLY = 0, CANCEL = 1;
    
    private static final int    DAY = 0, MONTH = 1, YEAR = 2;
    
    private static String       TIME = "00:00:00";
    
    private Filter              view;
    
    private ISelector           selector;
    
    private DataManagerCtrl     agentCtrl;
    
    FilterMng(Filter view, ISelector selector, DataManagerCtrl agentCtrl)
    {
        this.view = view;
        this.selector = selector;
        this.agentCtrl = agentCtrl;
        attachListeners();
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        attachButtonListeners(view.applyButton, APPLY);
        attachButtonListeners(view.cancelButton, CANCEL);
    }

    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle events fired by JButton. */
    public void actionPerformed(ActionEvent e)
    {
        try {
            int index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case APPLY:
                    applyFilters(); break;
                case CANCEL:
                    cancel(); 
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
        } 
    }
    
    /** Apply the filters. */
    private void applyFilters()
    {
        HashMap filters = new HashMap(), complexFilters = new HashMap();
        if (checkDate()) {  //set the date
            HashMap date = new HashMap();
            int index = view.dates_types.getSelectedIndex();
            Object key = DataManager.GREATER;
            switch (index) {
                case Filter.LESS:
                    key = DataManager.LESS; break;
                case Filter.GREATER:
                    key = DataManager.GREATER; 
            }
            date.put(key, getDate());
            complexFilters.put(DataManager.FILTER_DATE, date);
        }
        //Name
        String name = view.name.getText();
        if (name != null && name.length() > 0) {
            int index = view.name_types.getSelectedIndex();
            HashMap map = new HashMap();
            Object key = DataManager.CONTAIN;
            switch (index) {
                case Filter.CONTAIN:
                    key = DataManager.CONTAIN; break;
                case Filter.NOT_CONTAIN:
                    key = DataManager.NOT_CONTAIN; 
            }
            map.put(key, name);
            complexFilters.put(DataManager.FILTER_NAME, map);
        }
            
        if (view.annotation.isSelected())
            filters.put(DataManager.FILTER_ANNOTATED, Boolean.TRUE);
        if (view.limit.isSelected()) {
            int value = Filter.LIMIT;
            try {
                value = Integer.parseInt(view.visibleItem.getText());
                if (value < 0 || value > Filter.LIMIT_MAX)
                    value = Filter.LIMIT;
            } catch(NumberFormatException nfe) {}
            filters.put(DataManager.FILTER_LIMIT, new Integer(value));
        }  
        
        if (filters.size() > 0) 
            selector.setFilters(filters);
        if (complexFilters.size() > 0) 
            selector.setComplexFilters(complexFilters);
        cancel();
    }
    
    /** Close and dispose. */
    private void cancel()
    {
        view.dispose();
        view.setVisible(false);
    }
    
    /** Build the timeStamp corresponding to the date. */
    private Timestamp getDate()
    {
        String s = view.year.getText()+"-"+view.month.getText()
                    +"-"+view.day.getText()+" "+TIME;
        return Timestamp.valueOf(s);
    }
    
    /** Check if the values entered in the date textField are valid. */
    private boolean checkDate()
    {
        String day = view.day.getText(), month = view.month.getText(),
                year = view.year.getText();
        if (day.length() == 0 && month.length() == 0 && year.length() == 0)
            return false;
        if (!checkDate(DAY, day)) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid day", 
                    "Please enter a value between 1 and 31");
            view.day.setText("");
            return false;
        }
        if (!checkDate(MONTH, month)) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid month", 
                "Please enter a value between 1 and 12");
            view.month.setText("");
            return false;
        }
        if (!checkDate(YEAR, year)) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid year", 
                "Please enter a value greater than 1999");
            view.year.setText("");
            return false;
        }
        return true;
        
    }
    private boolean checkDate(int index, String value)
    {
        boolean valid = false;
        int val = 0;
        int max = 1, min = 1;
        switch (index) {
            case DAY:
                max = 31;
                if (value.length() > 2) return false;
                break;
            case MONTH:
                max = 12;
                if (value.length() > 2) return false;
                break;
            case YEAR:
                min = 2000;
                max = 2050;
                if (value.length() > 4) return false;
        }
        
        try {
            val = Integer.parseInt(value);
            if (min <= val && val <= max) valid = true;
        } catch(NumberFormatException nfe) {}
        return valid;
    }
    
}
