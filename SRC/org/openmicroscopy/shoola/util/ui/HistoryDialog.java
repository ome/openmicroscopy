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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic popu-menu displaying the previously searched pattern.
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
    
    /** The list hosting the data. */
    private JList       history;
    
    /** The width of the component. */
    private int         width;
    
    /** The data to display. */
    private Object[]    data;

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel list = new JPanel();
        list.setLayout(new BorderLayout());
        list.add(history);
        FontMetrics fm = getFontMetrics(history.getFont());
        int  height = fm.getHeight()*data.length+4;
        Dimension d = new Dimension(width, height);
        list.setSize(d);
        list.setPreferredSize(d);
        add(list);
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        history = new JList(data);
        history.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
        
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
        });
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
    
}
