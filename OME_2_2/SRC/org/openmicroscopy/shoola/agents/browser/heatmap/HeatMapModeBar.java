/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapModeBar
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.browser.datamodel.DisplayValueMode;

/**
 * Indicates which mode to use in drawing the heat map.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapModeBar extends JPanel
{
    private DisplayValueMode currentMode = HeatMapModes.MEAN_MODE;
    private Set modeListeners;
    private JComboBox box;
    
    private final Object[] modes = new Object[]{HeatMapModes.MINIMUM_MODE,
                                                HeatMapModes.MEAN_MODE,
                                                HeatMapModes.MEDIAN_MODE,
                                                HeatMapModes.MAXIMUM_MODE};
    
    public HeatMapModeBar()
    {
        setLayout(new BorderLayout());
        modeListeners = new HashSet();
        
        JLabel label = new JLabel("View by: ");
        box = new JComboBox(modes);
        
        box.setSelectedItem(currentMode);
        box.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    currentMode = (DisplayValueMode)e.getItem();
                    for(Iterator iter = modeListeners.iterator(); iter.hasNext();)
                    {
                        HeatMapModeListener listener =
                            (HeatMapModeListener)iter.next();
                        listener.modeChanged(currentMode);
                    }
                }
            }
        });
        
        add(label,BorderLayout.WEST);
        add(box,BorderLayout.CENTER);
    }
    
    /**
     * Gets the current mode. (this could be abstracted into a model, but this
     * is simple enough)
     * @return
     */
    public DisplayValueMode getCurrentMode()
    {
        return currentMode;
    }
    
    public void addListener(HeatMapModeListener listener)
    {
        if(listener != null)
        {
            modeListeners.add(listener);
        }
    }
    
    public void removeListener(HeatMapModeListener listener)
    {
        if(listener != null)
        {
            modeListeners.remove(listener);
        }
    }
    
    public void removeAllListeners()
    {
        modeListeners.clear();
    }
    
    public void setEnabled(boolean enable)
    {
        super.setEnabled(enable);
        box.setEnabled(enable);
    }
}
