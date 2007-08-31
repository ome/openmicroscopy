/*
 * org.openmicroscopy.shoola.util.ui.HistoryDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic popu-menu displaying collection of objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class HistoryDialog
    extends JPopupMenu    
{
    
    /** Bound property indicating that an item is selected in the list. */
    public static final String SELECTION_PROPERTY ="selection";
    
    /** The maximum height of the menu. */
    private static final int	MAX_HEIGHT = 100;
    
    /** The list hosting the data. */
    private JList       			history;
    
    /** The width of the component. */
    private int         			width;
    
    /** The data to display. */
    private Object[]    			data;

    /** Listener added to the selection model. */
    private ListSelectionListener	listener;
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel list = new JPanel();
        list.setLayout(new BorderLayout());
        list.add(history);
        FontMetrics fm = getFontMetrics(history.getFont());
        int  height = fm.getHeight()*data.length+4;
        if (height > MAX_HEIGHT) height = MAX_HEIGHT;
        Dimension d = new Dimension(width, height);
        setPopupSize(d);
        add(new JScrollPane(list));
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        history = new JList(data);
        listener = new ListSelectionListener() {
        
            public void valueChanged(ListSelectionEvent e)
            {
                ListSelectionModel model = (ListSelectionModel) e.getSource();
                if (!model.isSelectionEmpty() && e.getValueIsAdjusting()) {
                    int minIndex = model.getMinSelectionIndex(),
                    maxIndex = model.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++)
                    if (model.isSelectedIndex(i)) {
                        firePropertyChange(SELECTION_PROPERTY, null, data[i]);
                        setVisible(false);
                    }
                }
            }
        };
        history.getSelectionModel().addListSelectionListener(listener);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param data  The data to display.
     * @param width The width of the component.
     */
    public HistoryDialog(Object[] data, int width)
    {
        this.data = data;
        this.width = width;
        initComponents();
        buildGUI();
    }
    
    /**
     * Returns the selected object or <code>null</code>
     * and hides the menu.
     * 
     * @return See above.
     */
    public Object getSelectedTextValue()
    {
    	ListSelectionModel model = history.getSelectionModel();
    	if (model.isSelectionEmpty()) return null;
    	int minIndex = model.getMinSelectionIndex(),
        maxIndex = model.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++)
            if (model.isSelectedIndex(i)) {
            	setVisible(false);
            	return data[i];
                
            }
        return null;
    }
    
    /**
     * Selects an item in the list if one of the elements starts with
     * the passed string. Returns <code>true</code> if an item is selected,
     * <code>false</code> otherwise.
     * 
     * @param v The value to handle.
     * @return See above.
     */
    public boolean setSelectedTextValue(String v)
    {
    	if (v == null) return false;
    	String value;
    	Object r = null;
    	for (int i = 0; i < data.length; i++) {
			value = data[i].toString();
			if (value.startsWith(v)) {
				r = data[i];
				break;
			}
		}
    	if (r != null) {
    		history.getSelectionModel().removeListSelectionListener(listener);
    		history.setSelectedValue(r, true);
    		history.getSelectionModel().addListSelectionListener(listener);
    		return true;
    	}
    	return false;
    }
    
    /**
     * Sets the selected value. 
     * 
     * @param increase 	Pass <code>true</code> to increase the index,
     * 					<code>false</code> to decrease the index.
     */
    public void setSelectedIndex(boolean increase)
    {
    	ListSelectionModel model = history.getSelectionModel();
    	if (model.isSelectionEmpty()) return;
    	int index = history.getSelectedIndex();
    	Object r = null;
    	int l = data.length-1;
    	if (increase) {
    		if (index < l) index +=1;
    		else index = 0;
    	} else {
    		if (index > 0) index -=1;
    		else index = l;
    	}
    	r = data[index];
    	if (r != null) {
    		history.getSelectionModel().removeListSelectionListener(listener);
    		history.setSelectedValue(r, true);
    		history.getSelectionModel().addListSelectionListener(listener);
    	} 
    }
    
}
