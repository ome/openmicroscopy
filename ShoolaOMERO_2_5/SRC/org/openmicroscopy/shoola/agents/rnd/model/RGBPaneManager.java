/*
 * org.openmicroscopy.shoola.agents.rnd.model.RGBMappingManager
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

package org.openmicroscopy.shoola.agents.rnd.model;


//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.agents.rnd.editor.ChannelEditor;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.ColorSelector;
import org.openmicroscopy.shoola.util.ui.ColoredButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class RGBPaneManager
	implements ActionListener, ItemListener
{
	
    private static final int    MAX_CHANNELS = 3;
    
    private static final String MSG = "You can only map 3 channels at a " +
                                        "time in the RGB model.";
    
    private static final String MSG_COLOR = "A channel has already been " +
                                "mapped to the same color band.";
    
	private RGBPane 			view;
	
	private RenderingAgtCtrl 	eventManager;
	
	private HashMap				coloredButtons;
	
	RGBPaneManager(RGBPane view)
	{
		this.view = view;
		coloredButtons = new HashMap();
	}
	
	RGBPane getView(){ return view; }
	
	JFrame getReferenceFrame()
	{
		return eventManager.getRegistry().getTaskBar().getFrame();
	}
	
	void setEventManager(RenderingAgtCtrl eventManager)
	{
		this.eventManager = eventManager;
	}
	
	/** Attach the listeners. */
	void attachObjectListener(Object component, int index)
	{
		AbstractButton ab = null;
		if (component instanceof ColoredButton) {
			ab = (JButton) component;
			ab.addActionListener(this);
			coloredButtons.put(new Integer(index), component);
		} else if (component instanceof JCheckBox) {
			ab = (JCheckBox) component;
			ab.addItemListener(this);
		} else {
			//JButton
			ab = (JButton) component;
			ab.addActionListener(this);
		}
		ab.setActionCommand(""+index);
	}

	/** Handle events fired by button. */
	public void actionPerformed(ActionEvent e)
	{
		Object component = e.getSource();
		int index = -1;
		try {
            index = Integer.parseInt(e.getActionCommand());
			if (component instanceof ColoredButton) 
				UIUtilities.centerAndShow(new ColorSelector(view, 
										eventManager.getRGBA(index), index, 
                                        ColorSelector.RGB));
			else 
				UIUtilities.centerAndShow(new ChannelEditor(eventManager, 
										eventManager.getChannelData(index)));
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		}    
	}

	/** Handle event fired by the Checkbox. */
	public void itemStateChanged(ItemEvent e)
	{
		JCheckBox box = (JCheckBox) e.getSource();
		int w = Integer.parseInt(box.getActionCommand());
        int nbActive = eventManager.getNumberActiveChannels();
        if (nbActive < MAX_CHANNELS) 
            eventManager.setActive(w, e.getStateChange() == ItemEvent.SELECTED);
        else if (nbActive == MAX_CHANNELS && 
                !(e.getStateChange() == ItemEvent.SELECTED))
            eventManager.setActive(w, false);    
        else {
            UserNotifier un = eventManager.getRegistry().getUserNotifier();
            un.notifyInfo("RGB model", MSG);
            box.removeItemListener(this);
            box.setSelected(false);
            box.addItemListener(this);
        }
	}
	
	/** Set the four components of the selected color. */
	void setRGBA(int w, Color c)
	{
        List l = eventManager.getColorActiveChannels();
        Iterator i = l.iterator();
        Color color;
        boolean alreadySelected = false;
        while (i.hasNext()) {
            color = (Color) i.next();
            if (color.equals(c)) {
                alreadySelected = true;
                break;
            }
        }
        if (alreadySelected) {
            UserNotifier un = eventManager.getRegistry().getUserNotifier();
            un.notifyInfo("RGB model", MSG_COLOR);
        } else {
            ColoredButton cb = (ColoredButton) coloredButtons.get(
                                            new Integer(w));
            cb.setBackground(c);
            eventManager.setRGBA(w, c.getRed(), c.getGreen(), c.getBlue(), 
                                c.getAlpha());
        }
	}
	
}
