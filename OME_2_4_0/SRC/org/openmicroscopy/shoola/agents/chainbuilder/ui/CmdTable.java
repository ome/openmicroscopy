/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.CmdTable
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */



 
package org.openmicroscopy.shoola.agents.chainbuilder.ui;

//Java imports
import java.util.Hashtable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


//Third-party libraries

//Application-internal dependencies

/** 
 * A mapping to decouple ui controls from their implemenations.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */ 

public class CmdTable {
	
	protected Hashtable actionMap;
	
	/**
	 * 
	 * @param c the {@link Controller} object for the current instance
	 */
	public CmdTable(final UIManager uiManager) {
		
		actionMap = new Hashtable();
			
		actionMap.put("new chain",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiManager.newChain(); 
			}
		}); 
		
		actionMap.put("save chain",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiManager.saveCurrentChainFrame();
			}
		}); 
	}	
	
	/**
	 * 
	 * @param key the name of an action
	 * @return the corresponding {@link ActionListener}
	 */
	public ActionListener lookupActionListener(String key) {
		return (ActionListener)actionMap.get(key);
	}
	
	
}