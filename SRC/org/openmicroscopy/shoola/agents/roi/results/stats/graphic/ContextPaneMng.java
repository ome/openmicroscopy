/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.graphic.ContextPaneMng
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

package org.openmicroscopy.shoola.agents.roi.results.stats.graphic;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractButton;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng;
import org.openmicroscopy.shoola.util.ui.ColoredButton;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;

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
class ContextPaneMng
    implements ActionListener
{
    
    private static final int    SHOW = 0;
    
    private ContextPane         view;
    
    private StatsResultsPaneMng mng;
    
    ContextPaneMng(ContextPane view, StatsResultsPaneMng mng)
    {
        this.view = view;
        this.mng = mng;
        attachListeners();
    }
    
    private void attachListeners()
    {
        buttonListener(view.show, SHOW);
    }
    
    private void buttonListener(AbstractButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle events fired by button. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case SHOW:
                    show();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }
    
    /** Prepare the drawing context. */
    private void show()
    {
        int i = view.statObject.getSelectedIndex()+StatsResultsPane.MINUS;
        Map zSelected = new TreeMap(), tSelected = new TreeMap();
        prepareSelection(view.zPane.table, zSelected);
        prepareSelection(view.tPane.table, tSelected);
        int axis = StatsResultsPaneMng.T_AXIS;
        if (view.zAsAxis.isSelected()) axis = StatsResultsPaneMng.Z_AXIS;
        mng.showGraphic(i, axis, zSelected, tSelected);
        closeWidget();
    }
    
    private void prepareSelection(TableComponent table, Map map)
    {
        boolean selected;
        int v;
        JPanel p;
        ColoredButton b;
        for (int i = 0; i < table.getRowCount(); i++) {
            selected = ((Boolean) 
                    (table.getValueAt(i, ContextPane.BOOLEAN))).booleanValue();
            if (selected) {
               v =  ((Integer) (
                       table.getValueAt(i, ContextPane.NAME))).intValue();
               p =  ((JPanel) (
                       table.getValueAt(i, ContextPane.BUTTON)));
               b = (ColoredButton) p.getComponent(0);
               map.put(new Integer(v), b.getBackground());
            }
        }
    }
    
    private void closeWidget()
    {
        view.dispose();
        view.setVisible(false);
    }
    
}
