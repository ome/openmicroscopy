/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomMenuManager
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

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
public class ZoomMenuManager
	implements ActionListener
{
	
	/** Action command ID. */
	static final int				ZOOM_25 = 0;
	static final int				ZOOM_50 = 1;
	static final int				ZOOM_75 = 2;
	static final int				ZOOM_100 = 3;
	static final int				ZOOM_125 = 4;
	static final int				ZOOM_150 = 5;
	static final int				ZOOM_175 = 6;
	static final int				ZOOM_200 = 7;
	static final int				ZOOM_225 = 8;
	static final int				ZOOM_250 = 9;
	static final int				ZOOM_275 = 10;
	static final int				ZOOM_300 = 11;
	              
	private static final HashMap	values, inverseValues;
    
	static {
		values = new HashMap();
		values.put(new Double(0.25), new Integer(ZOOM_25));
		values.put(new Double(0.5), new Integer(ZOOM_50));
		values.put(new Double(0.75), new Integer(ZOOM_75));
		values.put(new Double(1.0), new Integer(ZOOM_100));
		values.put(new Double(1.25), new Integer(ZOOM_125));
		values.put(new Double(1.5), new Integer(ZOOM_150));
		values.put(new Double(1.75), new Integer(ZOOM_175));
		values.put(new Double(2.0), new Integer(ZOOM_200));
		values.put(new Double(2.25), new Integer(ZOOM_225));
		values.put(new Double(2.5), new Integer(ZOOM_250));
		values.put(new Double(2.75), new Integer(ZOOM_275));
		values.put(new Double(3.0), new Integer(ZOOM_300));
		inverseValues = new HashMap();
		inverseValues.put(new Integer(ZOOM_25), new Double(0.25));
		inverseValues.put(new Integer(ZOOM_50), new Double(0.5));
		inverseValues.put(new Integer(ZOOM_75), new Double(0.75));
		inverseValues.put(new Integer(ZOOM_100), new Double(1.0));
		inverseValues.put(new Integer(ZOOM_125), new Double(1.25));
		inverseValues.put(new Integer(ZOOM_150), new Double(1.5));
		inverseValues.put(new Integer(ZOOM_175), new Double(1.75));
		inverseValues.put(new Integer(ZOOM_200), new Double(2.0));
		inverseValues.put(new Integer(ZOOM_225), new Double(2.25));
		inverseValues.put(new Integer(ZOOM_250), new Double(2.5));
		inverseValues.put(new Integer(ZOOM_275), new Double(2.75));
		inverseValues.put(new Integer(ZOOM_300), new Double(3.0));
	}
	
	private ImageInspectorManager 	control;
	
	private HashMap					items;
	
	public ZoomMenuManager(ImageInspectorManager control)
	{
		this.control = control;
		items = new HashMap();
	}

	/** Attach listener to a menu Item. */
	void attachItemListener(AbstractButton item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
		items.put(new Integer(id), item);
	}
	
	/** Handle events fired but menuItem. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		Double value = (Double) inverseValues.get(new Integer(index));
		if (value != null) control.setZoomLevel(value.doubleValue());
		else throw new Error("Invalid Action ID "+index);
	}
	
	public void setItemSelected(double level)
	{
		Integer index = (Integer) values.get(new Double(level));
		Iterator i = items.keySet().iterator();
		AbstractButton item;
		Integer j;
		if (index != null) {
			while (i.hasNext()) {
				j = (Integer) i.next();
				item = (AbstractButton) items.get(j);
				item.setSelected(j.equals(index));
			}
		} else {
			while (i.hasNext()) {
				item = (AbstractButton) items.get(i.next());
				item.setSelected(false);
			}
		}
	}
	
}
