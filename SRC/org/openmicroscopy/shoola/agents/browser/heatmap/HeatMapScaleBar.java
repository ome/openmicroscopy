/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapScaleBar
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

/**
 * UI component for selecting linear or log comparison.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapScaleBar extends JPanel
{
    private Set modelListeners;
    private String selectedScaleType = Scale.LINEAR_SCALE;
    
    private static final String[] scaleTypes = {Scale.LINEAR_SCALE,
                                                Scale.LOGARITHMIC_SCALE};
    
    private JComboBox comboBox;
    
    public HeatMapScaleBar()
    {
        setLayout(new BorderLayout());
        modelListeners = new HashSet();
        
        JLabel label = new JLabel("Scale: ");
        comboBox = new JComboBox(scaleTypes);
        comboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    selectedScaleType = (String)e.getItem();
                    for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
                    {
                        HeatMapModeListener listener =
                            (HeatMapModeListener)iter.next();
                        listener.scaleChanged(selectedScaleType);
                    }
                }
            }

        });
        
        add(label,BorderLayout.WEST);
        add(comboBox,BorderLayout.CENTER);
    }
    
    public String getCurrentScaleType()
    {
        return selectedScaleType;
    }
    
    public void addListener(HeatMapModeListener listener)
    {
        if(listener != null)
        {
            modelListeners.add(listener);
        }
    }
    
    public void removeListener(HeatMapModeListener listener)
    {
        if(listener != null)
        {
            modelListeners.remove(listener);
        }
    }
    
    public void removeAllListeners()
    {
        modelListeners.clear();
    }
    
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
    }
    
}
