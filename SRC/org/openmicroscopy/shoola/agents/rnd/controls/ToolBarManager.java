/*
 * org.openmicroscopy.shoola.agents.rnd.controls.ToolBarManager
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

package org.openmicroscopy.shoola.agents.rnd.controls;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;

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
class ToolBarManager
	implements ActionListener
{

	/** Action command ID. */
	private static final int	GREY = RenderingDef.GS;
	private static final int	RGB = RenderingDef.RGB;
	private static final int	HSB = RenderingDef.HSB;
	
	private static final int	SAVE = 100;
	
	private ToolBar				view;
	private RenderingAgtCtrl 	control;
	
	public ToolBarManager(RenderingAgtCtrl control, ToolBar view)
	{
		this.control = control;
		this.view = view;
		attachListeners();
	}
	
	/** Attach the listeners. */
	private void attachListeners()
	{
		JButton greyButton = view.getGreyButton(), 
				rgbButton = view.getRgbButton(),
				hsbButton = view.getHsbButton(),
				saveButton = view.getSave();
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);		
		greyButton.setActionCommand(""+GREY);
		greyButton.addActionListener(this);
		rgbButton.setActionCommand(""+RGB);
		rgbButton.addActionListener(this);
		hsbButton.setActionCommand(""+HSB);
		hsbButton.addActionListener(this);
	}

	/** Handle events fired by buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) {
				case SAVE:
					control.saveDisplayOptions();
					break;
				case GREY:
				case RGB:
				case HSB:
					control.activateRenderingModel(index);
					break;
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		}
	}
	
}
