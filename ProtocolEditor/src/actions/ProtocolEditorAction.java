/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.AbstractComponent;
import ui.Controller;
import ui.IModel;

public class ProtocolEditorAction 
	extends AbstractAction 
	implements ChangeListener{

	protected IModel model;
	//protected AbstractComponent publisher;
	
	// might use this later if this class knows about view
	protected JFrame frame = null;
	
	public ProtocolEditorAction(IModel model) {
		this.model = model;
		if (model instanceof AbstractComponent) {
			((AbstractComponent)model).addChangeListener(this);
		}
	}
	
	/**
	 * Reacts to changes in the view
	 * subclasses override this method
	 */
	public void stateChanged(ChangeEvent e) {}

	
    /** 
     * Subclasses should implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e) {}

}
